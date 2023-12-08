package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2016;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day20 implements IPuzzle {
    private static class Range {
        private final long startInclusive;
        private final long endInclusive;

        public Range(long startInclusive, long endInclusive) {
            this.startInclusive = startInclusive;
            this.endInclusive = endInclusive;
        }

        public long getStartInclusive() {
            return this.startInclusive;
        }

        public long getEndInclusive() {
            return this.endInclusive;
        }
    }

    private static Pattern LINE_PATTERN = Pattern.compile("^(?<startInclusive>[0-9]+)-(?<endInclusive>[0-9]+)$");

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        long possibleIPs = Math.addExact(1,Long.parseLong(new String(configProvider.getPuzzleConfigChars("highest_ip")).trim()));
        NavigableMap<Long,Range> rangesByStart = new TreeMap<>();
        for (String inputLine : LineReader.strings(inputCharacters)) {
            Matcher matcher = LINE_PATTERN.matcher(inputLine);
            if (!matcher.matches()) {
                throw new IllegalStateException("Could not parse input line");
            }
            Range newRange = new Range(Long.parseLong(matcher.group("startInclusive")),Long.parseLong(matcher.group("endInclusive")));
            newRange = possiblyMergeRange(rangesByStart,newRange,NavigableMap::floorEntry);
            newRange = possiblyMergeRange(rangesByStart,newRange,NavigableMap::ceilingEntry);
            rangesByStart.put(newRange.getStartInclusive(),newRange);
        }
        Range firstRange = rangesByStart.firstEntry().getValue();
        Range lastRange = rangesByStart.lastEntry().getValue();
        long lowestValueIP;
        if (firstRange.getStartInclusive() > 0) {
            lowestValueIP = 0;
        } else {
            lowestValueIP = firstRange.getEndInclusive()+1;
        }
        long nonBlacklistedIPs = possibleIPs;
        for (Range range : rangesByStart.values()) {
            nonBlacklistedIPs -= (range.getEndInclusive()-range.getStartInclusive()+1);
        }
        return new BasicPuzzleResults<>(
            lowestValueIP,
            nonBlacklistedIPs
        );
    }

    private static Range possiblyMergeRange(NavigableMap<Long, Range> rangesByStart, Range newRange, BiFunction<NavigableMap<Long, Range>,Long,Map.Entry<Long,Range>> navigableMapSearchMethod) {
        while (true) {
            long newRangeStartInclusive = newRange.getStartInclusive();
            long newRangeEndInclusive = newRange.getEndInclusive();
            Map.Entry<Long,Range> possiblyOverlappingRangeEntry = navigableMapSearchMethod.apply(rangesByStart, newRangeStartInclusive);
            if (possiblyOverlappingRangeEntry == null) {
                break;
            }
            Range thatRange = possiblyOverlappingRangeEntry.getValue();
            long thatRangeStartInclusive = thatRange.getStartInclusive();
            long thatRangeEndInclusive = thatRange.getEndInclusive();
            if ((newRangeStartInclusive <= thatRangeEndInclusive && thatRangeStartInclusive <= newRangeEndInclusive) || (Math.max(newRangeStartInclusive,thatRangeStartInclusive)-Math.min(newRangeEndInclusive,thatRangeEndInclusive)) == 1) {
                rangesByStart.remove(thatRangeStartInclusive);
                long combinedRangeStartInclusive = Math.min(newRangeStartInclusive,thatRangeStartInclusive);
                long combinedRangeEndInclusive = Math.max(newRangeEndInclusive,thatRangeEndInclusive);
                newRange = new Range(combinedRangeStartInclusive,combinedRangeEndInclusive);
            }
            else {
                break;
            }
        }
        return newRange;
    }
}
