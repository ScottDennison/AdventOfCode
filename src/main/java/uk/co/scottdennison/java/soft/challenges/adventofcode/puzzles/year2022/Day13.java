package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2022;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.PushbackReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Day13 implements IPuzzle {
    private static final boolean DEBUG = false;
    private static final String[] DIVIDER_PACKETS = {"[[2]]","[[6]]"};

    public static enum PacketDataType {
        INTEGER,
        LIST
    }

    private static interface PacketData {
        PacketDataType getType();
    }

    private static class IntegerPacketData implements PacketData {
        private final int value;
        private String stringRepresentation = null;

        private IntegerPacketData(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

        @Override
        public PacketDataType getType() {
            return PacketDataType.INTEGER;
        }

        @Override
        public String toString() {
            if (this.stringRepresentation == null) {
                this.stringRepresentation = Integer.toString(value);
            }
            return this.stringRepresentation;
        }
    }

    private static class ListPacketData implements PacketData {
        private final List<PacketData> values;
        private String stringRepresentation = null;

        private ListPacketData(List<PacketData> values) {
            this.values = new ArrayList<>(values);
        }

        public int getValueCount() {
            return this.values.size();
        }

        public PacketData getValue(int index) {
            return this.values.get(index);
        }

        @Override
        public String toString() {
            if (this.stringRepresentation == null) {
                StringBuilder stringBuilder = new StringBuilder(2);
                boolean isFirst = true;
                stringBuilder.append('[');
                for (PacketData value : this.values) {
                    if (isFirst) {
                        isFirst = false;
                    } else {
                        stringBuilder.append(',');
                    }
                    stringBuilder.append(value);
                }
                stringBuilder.append(']');
                this.stringRepresentation = stringBuilder.toString();
            }
            return this.stringRepresentation;
        }

        @Override
        public PacketDataType getType() {
            return PacketDataType.LIST;
        }
    }

    private static class PacketDataPair {
        private final PacketData leftPacketData;
        private final PacketData rightPacketData;

        public PacketDataPair(PacketData leftPacketData, PacketData rightPacketData) {
            this.leftPacketData = leftPacketData;
            this.rightPacketData = rightPacketData;
        }

        public PacketData getLeftPacketData() {
            return this.leftPacketData;
        }

        public PacketData getRightPacketData() {
            return this.rightPacketData;
        }
    }

    private static int comparePacketData(PrintWriter printWriter, String indent, PacketData leftPacketData, PacketData rightPacketData) {
        PacketDataType leftPacketDataType = leftPacketData.getType();
        PacketDataType rightPacketDataType = rightPacketData.getType();
        if (printWriter != null) {
            printWriter.format("%s- Compare %s vs %s%n", indent, leftPacketData, rightPacketData);
        }
        switch (leftPacketDataType) {
            case INTEGER:
                switch (rightPacketDataType) {
                    case INTEGER:
                        return compareIntegerPacketData(printWriter, indent, (IntegerPacketData)leftPacketData, (IntegerPacketData)rightPacketData);
                    case LIST:
                        return comparePacketData(printWriter, indent, convertIntegerPacketDataToListPacketData(printWriter, indent, (IntegerPacketData)leftPacketData, "left"), rightPacketData);
                }
                break;
            case LIST:
                switch (rightPacketDataType) {
                    case INTEGER:
                        return comparePacketData(printWriter, indent, leftPacketData, convertIntegerPacketDataToListPacketData(printWriter, indent, (IntegerPacketData)rightPacketData, "right"));
                    case LIST:
                        return compareListPacketData(printWriter, indent, (ListPacketData)leftPacketData, (ListPacketData)rightPacketData);
                }
                break;
        }
        throw new IllegalStateException("Unexpected packet data type combination - " + leftPacketDataType + " and " + rightPacketDataType);
    }

    private static ListPacketData convertIntegerPacketDataToListPacketData(PrintWriter printWriter, String indent, IntegerPacketData integerPacketData, String side) {
        ListPacketData listPacketData = new ListPacketData(Collections.singletonList(integerPacketData));
        if (printWriter != null) {
            printWriter.format("%s- Mixed types; convert %s to %s and retry comparison%n", indent, side, listPacketData);
        }
        return listPacketData;
    }

    private static int compareIntegerPacketData(PrintWriter printWriter, String indent, IntegerPacketData leftIntegerPacketData, IntegerPacketData rightIntegerPacketData) {
        int leftValue = leftIntegerPacketData.getValue();
        int rightValue = rightIntegerPacketData.getValue();
        String nextIndent = indent == null ? null : indent + "  ";
        if (leftValue < rightValue) {
            if (printWriter != null) {
                printWriter.format("%s- Left side is smaller, so inputs are in the right order%n", nextIndent);
            }
            return -1;
        } else if (leftValue > rightValue) {
            if (printWriter != null) {
                printWriter.format("%s- Right side is smaller, so inputs are not in the right order%n", nextIndent);
            }
            return 1;
        } else {
            return 0;
        }
    }

    private static int compareListPacketData(PrintWriter printWriter, String indent, ListPacketData leftListPacketData, ListPacketData rightListPacketData) {
        int leftSize = leftListPacketData.getValueCount();
        int rightSize = rightListPacketData.getValueCount();
        int commonSize = Math.min(leftSize,rightSize);
        String nextIndent = indent == null ? null : indent + "  ";
        for (int index=0; index<commonSize; index++) {
            int comparisonResult = comparePacketData(printWriter, nextIndent, leftListPacketData.getValue(index), rightListPacketData.getValue(index));
            if (comparisonResult != 0) {
                return comparisonResult;
            }
        }
        if (leftSize < rightSize) {
            if (printWriter != null) {
                printWriter.format("%s- Left side ran out of items, so inputs are in the right order%n", nextIndent);
            }
            return -1;
        } else if (leftSize > rightSize) {
            if (printWriter != null) {
                printWriter.format("%s- Right side ran out of items, so inputs are not in the right order%n", nextIndent);
            }
            return 1;
        } else {
            return 0;
        }
    }

    private static PacketData parsePacketData(PushbackReader pushbackReader) throws IOException {
        int readValue = (char)pushbackReader.read();
        if (readValue == '[') {
            List<PacketData> values = new ArrayList<>();
            valueLoop: while (true) {
                readValue = pushbackReader.read();
                if (readValue == ']') {
                    break valueLoop;
                } else {
                    pushbackReader.unread(readValue);
                }
                values.add(parsePacketData(pushbackReader));
                characterSwitch: switch (pushbackReader.read()) {
                    case ']':
                        break valueLoop;
                    case ',':
                        // Do nothing
                        break characterSwitch;
                    default:
                        throw new IllegalStateException("Unexpected character");
                }
            }
            return new ListPacketData(values);
        } else if (readValue >= '0' && readValue <= '9') {
            StringBuilder integerBuilder = new StringBuilder(1);
            integerBuilder.append((char)readValue);
            while ((readValue = pushbackReader.read()) != -1) {
                if (readValue < '0' || readValue > '9') {
                    pushbackReader.unread(readValue);
                    break;
                }
                integerBuilder.append((char)readValue);
            }
            return new IntegerPacketData(Integer.parseInt(integerBuilder.toString()));
        } else {
            throw new IllegalStateException("Unexpected character: " + (char)readValue);
        }
    }

    private static PacketData parsePacketData(char[] inputLineCharacters) {
        try {
            PushbackReader pushbackReader = new PushbackReader(new CharArrayReader(inputLineCharacters));
            PacketData packetData = parsePacketData(pushbackReader);
            if (pushbackReader.read() != -1) {
                throw new IllegalStateException("Finished parsing without consuming all characters in line.");
            }
            return packetData;
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        List<PacketDataPair> packetDataPairList = new ArrayList<>();
        int lineIndex = 0;
        PacketData leftPacketData = null;
        for (char[] inputLineCharacters : LineReader.charArrays(inputCharacters)) {
            switch (lineIndex++) {
                case 0:
                    leftPacketData = parsePacketData(inputLineCharacters);
                    break;
                case 1:
                    packetDataPairList.add(new PacketDataPair(leftPacketData, parsePacketData(inputLineCharacters)));
                    break;
                case 2:
                    if (inputLineCharacters.length != 0) {
                        throw new IllegalStateException("Expected a blank line");
                    }
                    lineIndex = 0;
                    break;
            }
        }
        return new BasicPuzzleResults<>(
            solvePartA(printWriter, packetDataPairList),
            solvePartB(printWriter, packetDataPairList)
        );
    }

    private static int solvePartA(PrintWriter printWriter, List<PacketDataPair> packetDataPairList) {
        int packetDataPairCount = packetDataPairList.size();
        int packetPairSum = 0;
        for (int packetDataPairIndex = 0; packetDataPairIndex < packetDataPairCount; packetDataPairIndex++) {
            PacketDataPair packetDataPair = packetDataPairList.get(packetDataPairIndex);
            if (DEBUG) {
                printWriter.format("== Pair %d ==%n", packetDataPairIndex + 1);
            }
            int comparisonResult = comparePacketData(DEBUG?printWriter:null, DEBUG?"":null, packetDataPair.getLeftPacketData(), packetDataPair.getRightPacketData());
            if (comparisonResult == 0) {
                throw new IllegalStateException("Packets are the same.");
            } else if (comparisonResult < 0) {
                packetPairSum += (packetDataPairIndex + 1);
            }
            if (DEBUG) {
                printWriter.println();
            }
        }
        return packetPairSum;
    }

    private static int solvePartB(PrintWriter printWriter, List<PacketDataPair> packetDataPairList) {
        // Part B
        int packetDataPairCount = packetDataPairList.size();
        PacketData[] packetDataArray = new PacketData[packetDataPairCount*2];
        int packetDataIndex = 0;
        for (int packetDataPairIndex=0; packetDataPairIndex<packetDataPairCount; packetDataPairIndex++) {
            PacketDataPair packetDataPair = packetDataPairList.get(packetDataPairIndex);
            packetDataArray[packetDataIndex++] = packetDataPair.getLeftPacketData();
            packetDataArray[packetDataIndex++] = packetDataPair.getRightPacketData();
        }
        int dividerPacketCount = DIVIDER_PACKETS.length;
        PacketData[] dividerPacketDataArray = new PacketData[dividerPacketCount];
        for (int dividerPacketIndex=0; dividerPacketIndex<dividerPacketCount; dividerPacketIndex++) {
            dividerPacketDataArray[dividerPacketIndex] = parsePacketData(DIVIDER_PACKETS[dividerPacketIndex].toCharArray());
        }
        Comparator<PacketData> packetDataComparator = (leftPacketData, rightPacketData) -> comparePacketData(null, null, leftPacketData, rightPacketData);
        Arrays.sort(packetDataArray, packetDataComparator);
        Arrays.sort(dividerPacketDataArray, packetDataComparator);
        int decoderKey = 1;
        for (int dividerPacketIndex=0; dividerPacketIndex<dividerPacketCount; dividerPacketIndex++) {
            PacketData dividerPacketData = dividerPacketDataArray[dividerPacketIndex];
            int binarySearchResult = Arrays.binarySearch(packetDataArray,dividerPacketData,packetDataComparator);
            if (binarySearchResult >= 0) {
                throw new IllegalStateException("Divider packet already exists in recieved packets");
            }
            int decoderKeyPart = (-(binarySearchResult+1))+1+dividerPacketIndex;
            if (DEBUG) {
                printWriter.format("Divider packet #%d (%s) would be packet #%d%n",dividerPacketIndex+1,dividerPacketData,decoderKeyPart);
            }
            decoderKey *= decoderKeyPart;
        }
        return decoderKey;
    }
}
