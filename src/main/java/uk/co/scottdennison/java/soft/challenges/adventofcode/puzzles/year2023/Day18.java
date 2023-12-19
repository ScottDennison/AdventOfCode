package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2023;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day18 implements IPuzzle {
    private static enum Direction {
        RIGHT(0,1),
        DOWN(1, 0),
        LEFT(0,-1),
        UP(-1,0);

        private static final Map<Character,Direction> CHARACTER_TO_DIRECTION_MAP = new HashMap<>();
        static {
            for (Direction direction : Direction.values()) {
                CHARACTER_TO_DIRECTION_MAP.put(direction.name().charAt(0), direction);
            }
        }

        private final long yDelta;
        private final long xDelta;

        Direction(long yDelta, long xDelta) {
            this.yDelta = yDelta;
            this.xDelta = xDelta;
        }

        public long getYDelta() {
            return this.yDelta;
        }

        public long getXDelta() {
            return this.xDelta;
        }

        public static Direction getDirectionForCharacter(char character) {
            Direction direction = CHARACTER_TO_DIRECTION_MAP.get(character);
            if (direction == null) {
                throw new IllegalStateException("No such direction");
            }
            return direction;
        }
    }

    private static class Instruction {
        private Direction direction;
        private int amount;

        public Instruction(Direction direction, int amount) {
            this.direction = direction;
            this.amount = amount;
        }

        public Direction getDirection() {
            return this.direction;
        }

        public int getAmount() {
            return this.amount;
        }
    }


    private static class InstructionPair {
        private final Instruction partAInstruction;
        private final Instruction partBInstruction;

        public InstructionPair(Instruction partAInstruction, Instruction partBInstruction) {
            this.partAInstruction = partAInstruction;
            this.partBInstruction = partBInstruction;
        }

        public Instruction getPartAInstruction() {
            return this.partAInstruction;
        }

        public Instruction getPartBInstruction() {
            return this.partBInstruction;
        }
    }

    private static enum BoxState {
        EMPTY,
        FILLED,
        UNVISITED
    }

    private static class Box {
        private final long x;
        private final long y;
        private final long width;
        private final long height;
        private BoxState boxState = BoxState.UNVISITED;

        public Box(long x, long y, long width, long height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public long getX() {
            return this.x;
        }

        public long getY() {
            return this.y;
        }

        public long getWidth() {
            return this.width;
        }

        public long getHeight() {
            return this.height;
        }

        public BoxState getBoxState() {
            return this.boxState;
        }

        public void setBoxState(BoxState boxState) {
            this.boxState = boxState;
        }
    }

    private static class BoxGridIndexPair {
        private final int xIndex;
        private final int yIndex;

        public BoxGridIndexPair(int xIndex, int yIndex) {
            this.xIndex = xIndex;
            this.yIndex = yIndex;
        }

        public int getXIndex() {
            return this.xIndex;
        }

        public int getYIndex() {
            return this.yIndex;
        }

        @Override
        public boolean equals(Object otherObject) {
            if (this == otherObject) return true;
            if (otherObject == null || this.getClass() != otherObject.getClass()) return false;

            BoxGridIndexPair otherBoxGridIndexPair = (BoxGridIndexPair) otherObject;

            if (this.xIndex != otherBoxGridIndexPair.xIndex) return false;
            if (this.yIndex != otherBoxGridIndexPair.yIndex) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = this.xIndex;
            result = 31 * result + this.yIndex;
            return result;
        }
    }

    private static final Pattern PATTERN_INSTRUCTION = Pattern.compile("^(?<direction>[A-Z]) (?<amount>[0-9]+) \\(#(?<colour>[0-9a-f]{6})\\)$");

    private static void addSignificantCoordinates(Set<Long> significantCoordinatesSet, long coordinate) {
        significantCoordinatesSet.add(coordinate);
        significantCoordinatesSet.add(coordinate+1);
    }

    private static void extendByOne(SortedSet<Long> significantCoordinatesSet) {
        significantCoordinatesSet.add(significantCoordinatesSet.first()-1);
        significantCoordinatesSet.add(significantCoordinatesSet.last()+1);
    }

    private static long[] convertBoxedLongSetToUnboxedArray(Set<Long> intSet) {
        return intSet.stream().mapToLong(i -> i).toArray();
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        String[] inputLines = LineReader.stringsArray(inputCharacters, true);
        int instructionCount = inputLines.length;
        InstructionPair[] instructionPairs = new InstructionPair[instructionCount];
        Direction[] directions = Direction.values();
        for (int instructionIndex = 0; instructionIndex < instructionCount; instructionIndex++) {
            Matcher matcher = PATTERN_INSTRUCTION.matcher(inputLines[instructionIndex]);
            if (!matcher.matches()) {
                throw new IllegalStateException("Could not parse line");
            }
            String colourCode = matcher.group("colour");
            instructionPairs[instructionIndex] = new InstructionPair(
                new Instruction(
                    Direction.getDirectionForCharacter(matcher.group("direction").charAt(0)),
                    Integer.parseInt(matcher.group("amount"))
                ),
                new Instruction(
                    directions[colourCode.charAt(5) - '0'],
                    Integer.parseInt(colourCode.substring(0, 5), 16)
                )
            );
        }
        return new BasicPuzzleResults<>(
            solve(instructionPairs,InstructionPair::getPartAInstruction),
            solve(instructionPairs,InstructionPair::getPartBInstruction)
        );
    }

    private static long solve(InstructionPair[] instructionPairs, Function<InstructionPair,Instruction> instructionPairToInstructionMapper) {
        Instruction[] instructions = Arrays.stream(instructionPairs).map(instructionPairToInstructionMapper).toArray(Instruction[]::new);
        long currentX = 0;
        long currentY = 0;
        SortedSet<Long> significantXCoordinatesSet = new TreeSet<>();
        SortedSet<Long> significantYCoordinatesSet = new TreeSet<>();
        for (Instruction instruction : instructions) {
            Direction direction = instruction.getDirection();
            int amount = instruction.getAmount();
            currentX += direction.getXDelta() * amount;
            currentY += direction.getYDelta() * amount;
            addSignificantCoordinates(significantXCoordinatesSet,currentX);
            addSignificantCoordinates(significantYCoordinatesSet,currentY);
        }
        if (!(currentX == 0 && currentY == 0)) {
            throw new IllegalStateException("Instructions do not form a loop");
        }
        extendByOne(significantXCoordinatesSet);
        extendByOne(significantYCoordinatesSet);
        long[] significantXCoordinatesArray = convertBoxedLongSetToUnboxedArray(significantXCoordinatesSet);
        long[] significantYCoordinatesArray = convertBoxedLongSetToUnboxedArray(significantYCoordinatesSet);
        int significantXCoordinatesLastIndex = significantXCoordinatesArray.length-1;
        int significantYCoordinatesLastIndex = significantYCoordinatesArray.length-1;
        long minX = significantXCoordinatesArray[0];
        long minY = significantYCoordinatesArray[0];
        long maxX = significantXCoordinatesArray[significantXCoordinatesLastIndex];
        long maxY = significantYCoordinatesArray[significantYCoordinatesLastIndex];
        Box[][] boxGrid = new Box[significantYCoordinatesLastIndex][significantXCoordinatesLastIndex];
        long lastSignificantY = minY;
        for (int significantYCoordinatesIndex = 1; significantYCoordinatesIndex <= significantYCoordinatesLastIndex; significantYCoordinatesIndex++) {
            long currentSignificantY = significantYCoordinatesArray[significantYCoordinatesIndex];
            long lastSignificantX = minX;
            for (int significantXCoordinatesIndex = 1; significantXCoordinatesIndex <= significantXCoordinatesLastIndex; significantXCoordinatesIndex++) {
                long currentSignificantX = significantXCoordinatesArray[significantXCoordinatesIndex];
                boxGrid[significantYCoordinatesIndex-1][significantXCoordinatesIndex-1] = new Box(
                    lastSignificantX,
                    lastSignificantY,
                    currentSignificantX-lastSignificantX,
                    currentSignificantY-lastSignificantY
                );
                lastSignificantX = currentSignificantX;
            }
            lastSignificantY = currentSignificantY;
        }
        int currentBoxYIndex = 0;
        int currentBoxXIndex = 0;
        while (boxGrid[currentBoxYIndex][0].getY() != 0) {
            currentBoxYIndex++;
        }
        while (boxGrid[0][currentBoxXIndex].getX() != 0) {
            currentBoxXIndex++;
        }
        Box currentBox = boxGrid[currentBoxYIndex][currentBoxXIndex];
        for (Instruction instruction : instructions) {
            Direction direction = instruction.getDirection();
            long amount = instruction.getAmount();
            long xDelta = direction.getXDelta();
            long yDelta = direction.getYDelta();
            long targetX = currentBox.getX() + (xDelta * amount);
            long targetY = currentBox.getY() + (yDelta * amount);
            do {
                currentBox.setBoxState(BoxState.FILLED);
                currentBoxXIndex += xDelta;
                currentBoxYIndex += yDelta;
                currentBox = boxGrid[currentBoxYIndex][currentBoxXIndex];
            } while (!(currentBox.getX() == targetX && currentBox.getY() == targetY));
        }
        Deque<BoxGridIndexPair> pendingBoxGridIndexPairs = new LinkedList<>();
        pendingBoxGridIndexPairs.addFirst(new BoxGridIndexPair(0,0));
        long emptyVolume = 0;
        while (true) {
            BoxGridIndexPair boxGridIndexPair = pendingBoxGridIndexPairs.pollFirst();
            if (boxGridIndexPair == null) {
                break;
            }
            currentBoxXIndex = boxGridIndexPair.getXIndex();
            currentBoxYIndex = boxGridIndexPair.getYIndex();
            if (currentBoxXIndex >= 0 && currentBoxXIndex < significantXCoordinatesLastIndex && currentBoxYIndex >= 0 && currentBoxYIndex < significantYCoordinatesLastIndex) {
                Box box = boxGrid[currentBoxYIndex][currentBoxXIndex];
                if (box.getBoxState() == BoxState.UNVISITED) {
                    box.setBoxState(BoxState.EMPTY);
                    emptyVolume += box.getWidth() * box.getHeight();
                    pendingBoxGridIndexPairs.addLast(new BoxGridIndexPair(currentBoxXIndex - 1, currentBoxYIndex));
                    pendingBoxGridIndexPairs.addLast(new BoxGridIndexPair(currentBoxXIndex + 1, currentBoxYIndex));
                    pendingBoxGridIndexPairs.addLast(new BoxGridIndexPair(currentBoxXIndex, currentBoxYIndex - 1));
                    pendingBoxGridIndexPairs.addLast(new BoxGridIndexPair(currentBoxXIndex, currentBoxYIndex + 1));
                }
            }
        }
        return ((maxX-minX)*(maxY-minY))-emptyVolume;
    }
}
