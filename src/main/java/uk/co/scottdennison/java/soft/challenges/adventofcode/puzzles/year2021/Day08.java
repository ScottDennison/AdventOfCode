package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2021;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Arrays;

public class Day08 implements IPuzzle {
    private static final int SEGMENT_COUNT = 7;
    private static final boolean[] PART_A_KEEP_LENGTHS = {false,false,true,true,true,false,false,true};
    private static final Integer[] SEGMENT_BITFIELD_TO_VALUE = new Integer[1<<SEGMENT_COUNT];
    static {
        SEGMENT_BITFIELD_TO_VALUE[0B1110111] = 0;
        SEGMENT_BITFIELD_TO_VALUE[0B0010010] = 1;
        SEGMENT_BITFIELD_TO_VALUE[0B1011101] = 2;
        SEGMENT_BITFIELD_TO_VALUE[0B1011011] = 3;
        SEGMENT_BITFIELD_TO_VALUE[0B0111010] = 4;
        SEGMENT_BITFIELD_TO_VALUE[0B1101011] = 5;
        SEGMENT_BITFIELD_TO_VALUE[0B1101111] = 6;
        SEGMENT_BITFIELD_TO_VALUE[0B1010010] = 7;
        SEGMENT_BITFIELD_TO_VALUE[0B1111111] = 8;
        SEGMENT_BITFIELD_TO_VALUE[0B1111011] = 9;
    }

    private static final int SIGNAL_PATTERN_COUNT = 10;
    private static final int DIGIT_COUNT = 4;

    private static int[][] parseLine(String line, int startPositionInclusive, int endPositionExclusive, int expectedCount) {
        String[] lineParts = line.substring(startPositionInclusive, endPositionExclusive).trim().split(" ");
        if (lineParts.length != expectedCount) {
            throw new IllegalStateException("Unexpected amount of parts");
        }
        int[][] lineSegments = new int[expectedCount][];
        for (int linePartIndex=0; linePartIndex<expectedCount; linePartIndex++) {
            String linePart = lineParts[linePartIndex].trim();
            int linePartLength = linePart.length();
            int[] linePartSegments = new int[linePartLength];
            for (int linePartCharIndex=0; linePartCharIndex<linePartLength; linePartCharIndex++) {
                char linePartChar = linePart.charAt(linePartCharIndex);
                int linePartSegment = linePartChar - 'a';
                if (linePartSegment < 0 || linePartSegment >= SEGMENT_COUNT) {
                    throw new IllegalStateException("Unexpected character");
                }
                linePartSegments[linePartCharIndex] = linePartSegment;
            }
            lineSegments[linePartIndex] = linePartSegments;
        }
        return lineSegments;
    }

    private static void calculateInOut(int[][] segmentOccuranceCounts, int[] mappings, int mappingDestinationSegment, int inLength, int inCount, int outLength, int outCount) {
        int[] segmentOccuranceCountsIn = segmentOccuranceCounts[inLength];
        int[] segmentOccuranceCountsOut = segmentOccuranceCounts[outLength];
        int mappingSourceSegment = -1;
        for (int segment=0; segment<SEGMENT_COUNT; segment++) {
            if (segmentOccuranceCountsIn[segment] == inCount && segmentOccuranceCountsOut[segment] != outCount) {
                if (mappingSourceSegment == -1) {
                    mappingSourceSegment = segment;
                } else {
                    throw new IllegalStateException("Multiple segments match the criteria");
                }
            }
        }
        if (mappings[mappingSourceSegment] != -1) {
            throw new IllegalStateException("Mapping already determined");
        }
        mappings[mappingSourceSegment] = mappingDestinationSegment;
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        int simpleDigitsCount = 0;
        int totalDisplayedValue = 0;
        for (String line : LineReader.strings(inputCharacters)) {
            int barPosition = line.indexOf('|');
            if (barPosition < 0) {
                throw new IllegalStateException("No |");
            }
            int[][] scrambledSignalPatterns = parseLine(line,0,barPosition,SIGNAL_PATTERN_COUNT);
            int[][] scrambledDigits = parseLine(line,barPosition+1,line.length(),DIGIT_COUNT);
            int[][] segmentOccuranceCounts = new int[SEGMENT_COUNT+1][SEGMENT_COUNT];
            for (int[] scrambledSignalPattern : scrambledSignalPatterns) {
                int scrambledSignalPatternLength = scrambledSignalPattern.length;
                for (int scrambledSignalPatternSegment : scrambledSignalPattern) {
                    segmentOccuranceCounts[scrambledSignalPatternLength][scrambledSignalPatternSegment]++;
                }
            }
            int[] mappings = new int[SEGMENT_COUNT];
            Arrays.fill(mappings,-1);
            calculateInOut(segmentOccuranceCounts,mappings,0,3,1,2,1); // or 3,1,5,2 or 3,1,4,1
            calculateInOut(segmentOccuranceCounts,mappings,1,5,1,6,2);
            calculateInOut(segmentOccuranceCounts,mappings,2,2,1,6,3); // or 5,2,6,3 or 3,1,6,3
            calculateInOut(segmentOccuranceCounts,mappings,3,5,3,6,3);
            calculateInOut(segmentOccuranceCounts,mappings,4,5,1,4,1); // or 5,1,6,3 or 6,2,4,1
            calculateInOut(segmentOccuranceCounts,mappings,5,2,1,6,2); // or 5,2,6,2
            // No such in/out solution for g, let's find the one left.
            // We could have just pre-filled the array with 6, but that would require magic numbers in calculateInOut to tell if there has been a duplicate mapping.
            for (int mappingSourceSegment=0; mappingSourceSegment<SEGMENT_COUNT; mappingSourceSegment++) {
                if (mappings[mappingSourceSegment] == -1) {
                    mappings[mappingSourceSegment] = 6;
                    break;
                }
            }
            int displayedValue = 0;
            for (int digitIndex=0; digitIndex<DIGIT_COUNT; digitIndex++) {
                int[] scrambledDigit = scrambledDigits[digitIndex];
                if (PART_A_KEEP_LENGTHS[scrambledDigit.length]) {
                    simpleDigitsCount++;
                }
                int digitSegmentBitfield = 0;
                for (int scrambledDigitSegment : scrambledDigit) {
                    digitSegmentBitfield |= (1<<(SEGMENT_COUNT-1-mappings[scrambledDigitSegment]));
                }
                Integer digitValue = SEGMENT_BITFIELD_TO_VALUE[digitSegmentBitfield];
                if (digitValue == null) {
                    throw new IllegalStateException("Unrecgonized digit");
                }
                displayedValue = (displayedValue * 10) + digitValue;
            }
            totalDisplayedValue += displayedValue;
        }
        return new BasicPuzzleResults<>(
            simpleDigitsCount,
            totalDisplayedValue
        );
    }
}
