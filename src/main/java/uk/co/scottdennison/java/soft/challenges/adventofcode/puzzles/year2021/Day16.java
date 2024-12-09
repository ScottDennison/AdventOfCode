package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2021;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.IntPredicate;

public class Day16 implements IPuzzle {
    private static enum PacketType {
        LITERAL,
        OPERATOR
    }

    private static enum Operator {
        SUM {
            @Override
            public BigInteger getValue(List<Packet> packets) {
                return calculatePacketReductionValue(packets,BigInteger.ZERO,BigInteger::add);
            }
        },
        PRODUCT {
            public BigInteger getValue(List<Packet> packets) {
                return calculatePacketReductionValue(packets,BigInteger.ONE,BigInteger::multiply);
            }
        },
        MINIMUM {
            public BigInteger getValue(List<Packet> packets) {
                return calculateExtremePacketValue(packets, compareResult -> compareResult < 0);
            }
        },
        MAXIMUM {
            public BigInteger getValue(List<Packet> packets) {
                return calculateExtremePacketValue(packets, compareResult -> compareResult > 0);
            }
        },
        SINGLE_VALUE {
            @Override
            public BigInteger getValue(List<Packet> packets) {
                if (packets.size() == 1) {
                    throw new IllegalStateException("Expected only 1 packet");
                }
                return packets.get(0).getValue();
            }
        },
        GREATER_THAN {
            @Override
            public BigInteger getValue(List<Packet> packets) {
                return compareTwoPackets(packets,1);
            }
        },
        LESS_THAN {
            @Override
            public BigInteger getValue(List<Packet> packets) {
                return compareTwoPackets(packets,-1);
            }
        },
        EQUAL_TO {
            @Override
            public BigInteger getValue(List<Packet> packets) {
                return compareTwoPackets(packets,0);
            }
        };

        public abstract BigInteger getValue(List<Packet> packets);
    }

    private static BigInteger calculatePacketReductionValue(List<Packet> packets, BigInteger startValue, BinaryOperator<BigInteger> operator) {
        BigInteger value = startValue;
        for (Packet packet : packets) {
            value = operator.apply(value,packet.getValue());
        }
        return value;
    }

    private static BigInteger calculateExtremePacketValue(List<Packet> packets, IntPredicate comparisonResultPredicate) {
        BigInteger value = packets.get(0).getValue();
        int packetCount = packets.size();
        for (int packetIndex=1; packetIndex<packetCount; packetIndex++) {
            BigInteger packetValue = packets.get(packetIndex).getValue();
            if (comparisonResultPredicate.test(packetValue.compareTo(value))) {
                value = packetValue;
            }
        }
        return value;
    }

    private static BigInteger compareTwoPackets(List<Packet> packets, int requiredComparisonResult) {
        if (packets.size() != 2) {
            throw new IllegalStateException("Expected only 2 packets");
        }
        return packets.get(0).getValue().compareTo(packets.get(1).getValue()) == requiredComparisonResult ? BigInteger.ONE : BigInteger.ZERO;
    }

    private static abstract class Packet {
        private final int version;

        protected Packet(int version) {
            this.version = version;
        }

        public int getVersion() {
            return this.version;
        }

        protected abstract PacketType getPacketType();

        protected abstract BigInteger getValue();
    }

    private static final class LiteralPacket extends Packet {
        private final BigInteger value;

        private LiteralPacket(int version, BigInteger value) {
            super(version);
            this.value = value;
        }

        @Override
        public BigInteger getValue() {
            return this.value;
        }

        @Override
        protected PacketType getPacketType() {
            return PacketType.LITERAL;
        }
    }

    private static final class OperatorPacket extends Packet {
        private final Operator operator;
        private final List<Packet> subPackets;
        private transient BigInteger value;

        protected OperatorPacket(int version, Operator operator, List<Packet> subPackets) {
            super(version);
            this.operator = operator;
            this.subPackets =  new ArrayList<>(subPackets);
        }

        public List<Packet> getSubPackets() {
            return Collections.unmodifiableList(this.subPackets);
        }

        @Override
        protected PacketType getPacketType() {
            return PacketType.OPERATOR;
        }

        @Override
        protected BigInteger getValue() {
            if (value == null) {
                value = operator.getValue(getSubPackets());
            }
            return value;
        }
    }

    private static final class PacketParseResult {
        private final Packet packet;
        private final int bitIndex;

        private PacketParseResult(Packet packet, int bitIndex) {
            this.packet = packet;
            this.bitIndex = bitIndex;
        }

        public Packet getPacket() {
            return this.packet;
        }

        public int getBitIndex() {
            return this.bitIndex;
        }
    }

    private static final Operator[] OPERATORS = Operator.values();

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        inputCharacters = new String(inputCharacters).trim().toCharArray();
        int characterCount = inputCharacters.length;
        BitSet outerBitset = new BitSet(characterCount*4);
        int bitIndex = 0;
        for (int characterIndex=0; characterIndex<characterCount; characterIndex++) {
            char character = inputCharacters[characterIndex];
            int value;
            if (character >= '0' && character <= '9') {
                value = character-'0';
            } else if (character >= 'A' && character <= 'F') {
                value = character-'7'; // 10 before 'A'
            } else if (character >= 'a' && character <= 'f') {
                value = character-'W'; // 10 before 'a'
            } else {
                throw new IllegalStateException("Unexpected character");
            }
            outerBitset.set(bitIndex++, ((value>>3)&1)!=0);
            outerBitset.set(bitIndex++, ((value>>2)&1)!=0);
            outerBitset.set(bitIndex++, ((value>>1)&1)!=0);
            outerBitset.set(bitIndex++, ((value>>0)&1)!=0);
        }
        PacketParseResult outerPacketParseResult = parsePacket(outerBitset);
        if (outerBitset.nextSetBit(outerPacketParseResult.getBitIndex()) >= 0) {
            throw new IllegalStateException("Expected trailing bits to be only 0");
        };
        Packet outerPacket = outerPacketParseResult.getPacket();
        return new BasicPuzzleResults<>(
            recurseSumVersion(outerPacket),
            outerPacket.getValue()
        );
    }

    private PacketParseResult parsePacket(BitSet bitset) {
        return parsePacket(bitset, 0);
    }

    private PacketParseResult parsePacket(BitSet bitset, int bitIndex) {
        int version = convertToIntReversed(bitset,bitIndex,3);
        bitIndex += 3;
        int packetType = convertToIntReversed(bitset,bitIndex,3);
        bitIndex += 3;
        Packet packet;
        if (packetType == 4) {
            BigInteger value = BigInteger.ZERO;
            do {
                value = value.shiftLeft(4);
                bitIndex++;
                for (int bit=3; bit>=0; bit--) {
                    if (bitset.get(bitIndex++)) {
                        value = value.setBit(bit);
                    }
                }
            } while (bitset.get(bitIndex-5));
            packet = new LiteralPacket(version,value);
        } else {
            List<Packet> subPackets;
            if (bitset.get(bitIndex++)) {
                int subPacketCount = convertToIntReversed(bitset, bitIndex, 11);
                bitIndex += 11;
                Packet[] subPacketsArray = new Packet[subPacketCount];
                for (int subPacketIndex=0; subPacketIndex<subPacketCount; subPacketIndex++) {
                    PacketParseResult packetParseResult = parsePacket(bitset, bitIndex);
                    subPacketsArray[subPacketIndex] = packetParseResult.getPacket();
                    bitIndex = packetParseResult.getBitIndex();
                }
                subPackets = Arrays.asList(subPacketsArray);
            } else {
                int bitCount = convertToIntReversed(bitset, bitIndex, 15);
                bitIndex += 15;
                int stopBitIndex = bitIndex + bitCount;
                subPackets = new ArrayList<>();
                while (true) {
                    PacketParseResult packetParseResult = parsePacket(bitset, bitIndex);
                    subPackets.add(packetParseResult.getPacket());
                    bitIndex = packetParseResult.getBitIndex();
                    if (bitIndex == stopBitIndex) {
                        break;
                    }
                    else if (bitIndex >= stopBitIndex) {
                        throw new IllegalStateException("Subpacket exceeded specified bit count");
                    }
                }
            }
            packet = new OperatorPacket(version,OPERATORS[packetType],subPackets);
        }
        return new PacketParseResult(packet,bitIndex);
    }

    private static int convertToIntReversed(BitSet bitSet, int bitIndex, int length) {
        int value = 0;
        int stopBitIndex = bitIndex + length;
        for (int i = bitSet.nextSetBit(bitIndex); i >= 0 && i < stopBitIndex; i = bitSet.nextSetBit(i+1)) {
            value += (1 << (length-1-(i-bitIndex)));
        }
        return value;
    }

    private int recurseSumVersion(Packet packet) {
        int total = packet.getVersion();
        if (packet.getPacketType() == PacketType.OPERATOR) {
            for (Packet subPacket : ((OperatorPacket)packet).getSubPackets()) {
                total += recurseSumVersion(subPacket);
            }
        }
        return total;
    }
}
