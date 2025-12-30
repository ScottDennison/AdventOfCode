package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2025;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Day05 implements IPuzzle {
    private static class Range {
        private final long start;
        private final long end;

        public Range(long start, long end) {
            if (start > end) {
                throw new IllegalArgumentException("start > end");
            }
            this.start = start;
            this.end = end;
        }

        public long getStart() {
            return start;
        }

        public long getEnd() {
            return end;
        }
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        List<Range> rangesList = new ArrayList<>();
        Range[] rangesArray = null;
        int rangesCount = 0;
        int ingredientsFresh = 0;
        for (String inputLine : LineReader.strings(inputCharacters)) {
            if (rangesArray == null) {
                if (inputLine.isEmpty()) {
                    rangesCount = rangesList.size();
                    rangesArray = rangesList.toArray(new Range[rangesCount]);
                }
                else {
                    int dashPosition = inputLine.indexOf('-');
                    if (dashPosition == -1) {
                        throw new IllegalStateException("No dash");
                    }
                    rangesList.add(
                        new Range(
                            Long.parseLong(inputLine.substring(0, dashPosition)),
                            Long.parseLong(inputLine.substring(dashPosition+1))
                        )
                    );
                }
            }
            else {
                boolean fresh = false;
                long ingredientID = Long.parseLong(inputLine);
                for (Range range : rangesArray) {
                    if (ingredientID >= range.getStart() && ingredientID <= range.getEnd()) {
                        fresh = true;
                        break;
                    }
                }
                if (fresh) {
                    ingredientsFresh++;
                }
            }
        }
        Arrays.sort(rangesArray, Comparator.comparing(Range::getStart).thenComparing(Range::getEnd));
        Range firstRange = rangesArray[0];
        long currentStart = firstRange.getStart();
        long currentEnd = firstRange.getEnd();
        long possibleIDsCount = 0;
        for (int rangeIndex=1; rangeIndex<rangesCount; rangeIndex++) {
            Range thisRange = rangesArray[rangeIndex];
            long thisRangeStart = thisRange.getStart();
            long thisRangeEnd = thisRange.getEnd();
            if (thisRangeStart > currentEnd) {
                possibleIDsCount += (currentEnd - currentStart) + 1;
                currentStart = thisRangeStart;
                currentEnd = thisRangeEnd;
            }
            else {
                currentEnd = Math.max(currentEnd, thisRangeEnd);
            }
        }
        possibleIDsCount += (currentEnd - currentStart) + 1;
        return new BasicPuzzleResults<>(
            ingredientsFresh,
            possibleIDsCount
        );
    }
}
