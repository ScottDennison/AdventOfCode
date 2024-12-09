package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2023;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day05 implements IPuzzle {
    private static class EntryRange {
        private final long startInclusive;
        private final long stopExclusive;

        public EntryRange(long startInclusive, long stopExclusive) {
            this.startInclusive = startInclusive;
            this.stopExclusive = stopExclusive;
        }

        public long getStartInclusive() {
            return this.startInclusive;
        }

        public long getStopExclusive() {
            return this.stopExclusive;
        }
    }

    private static class MapRange {
        private final long sourceRangeStartInclusive;
        private final long sourceRangeStopExclusive;
        private final long sourceToDestinationDifference;

        public MapRange(long sourceRangeStartInclusive, long sourceRangeStopExclusive, long sourceToDestinationDifference) {
            this.sourceRangeStartInclusive = sourceRangeStartInclusive;
            this.sourceRangeStopExclusive = sourceRangeStopExclusive;
            this.sourceToDestinationDifference = sourceToDestinationDifference;
        }

        public long getSourceRangeStartInclusive() {
            return this.sourceRangeStartInclusive;
        }

        public long getSourceRangeStopExclusive() {
            return this.sourceRangeStopExclusive;
        }

        public long getSourceToDestinationDifference() {
            return this.sourceToDestinationDifference;
        }
    }

    private static final Pattern PATTERN_SEEDS = Pattern.compile("^seeds: (?<seeds>(?:[0-9]+ )+[0-9]+)$");
    private static final Pattern PATTERN_SPACE = Pattern.compile(" +");
    private static final Pattern PATTERN_MAP_NAME = Pattern.compile("^(?<fromRangeName>[a-z]+)-to-(?<toRangeName>[a-z]+) map:$");
    private static final Pattern PATTERN_RANGE = Pattern.compile("^(?<destinationRangeStart>[0-9]+) (?<sourceRangeStart>[0-9]+) (?<rangeLength>[0-9]+)$");

    private static enum ParseState {
        SEEDS,
        BLANK_LINE_BEFORE_MAPS,
        MAP_NAME,
        RANGE_OR_BLANK_LINE_OR_EOF,
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        String inputLines[] = LineReader.stringsArray(inputCharacters,true);
        ParseState parseState = ParseState.SEEDS;
        String expectedFromRangeName = "seed";
        EntryRange[] currentPartAEntryRanges = null;
        EntryRange[] currentPartBEntryRanges = null;
        List<MapRange> currentMapRanges = null;
        for (String inputLine : inputLines) {
            Matcher matcher;
            switch (parseState) {
                case SEEDS:
                    matcher = PATTERN_SEEDS.matcher(inputLine);
                    if (!matcher.matches()) {
                        throw new IllegalStateException("Could not parse seeds line");
                    }
                    long[] values = PATTERN_SPACE.splitAsStream(matcher.group("seeds")).mapToLong(Long::parseLong).toArray();
                    int valuesCount = values.length;
                    if (valuesCount % 2 != 0) {
                        throw new IllegalStateException("Part B requires the raw seed values to in groups of 2.");
                    }
                    currentPartAEntryRanges = new EntryRange[valuesCount];
                    currentPartBEntryRanges = new EntryRange[valuesCount/2];
                    int valueIndex = 0;
                    int currentPartAEntryIndex = 0;
                    int currentPartBEntryIndex = 0;
                    while (valueIndex < valuesCount) {
                        long value1 = values[valueIndex++];
                        currentPartAEntryRanges[currentPartAEntryIndex++] = new EntryRange(value1, value1+1);
                        long value2 = values[valueIndex++];
                        currentPartAEntryRanges[currentPartAEntryIndex++] = new EntryRange(value2, value2+1);
                        currentPartBEntryRanges[currentPartBEntryIndex++] = new EntryRange(value1, value1+value2);
                    }
                    parseState = ParseState.BLANK_LINE_BEFORE_MAPS;
                    break;
                case BLANK_LINE_BEFORE_MAPS:
                    if (!inputLine.isEmpty()) {
                        throw new IllegalStateException("Expected an empty line");
                    }
                    parseState = ParseState.MAP_NAME;
                    break;
                case MAP_NAME:
                    matcher = PATTERN_MAP_NAME.matcher(inputLine);
                    if (!matcher.matches()) {
                        throw new IllegalStateException("Could not parse map name line");
                    }
                    String fromRangeName = matcher.group("fromRangeName");
                    if (!fromRangeName.equals(expectedFromRangeName)) {
                        throw new IllegalStateException("Unexpected map \"from\" name. Expected \"" + expectedFromRangeName + "\", saw \"" + fromRangeName + "\".");
                    }
                    expectedFromRangeName = matcher.group("toRangeName");
                    currentMapRanges = new ArrayList<>();
                    parseState = ParseState.RANGE_OR_BLANK_LINE_OR_EOF;
                    break;
                case RANGE_OR_BLANK_LINE_OR_EOF:
                    if (inputLine.isEmpty()) {
                        currentPartAEntryRanges = processEntriesAndRanges(currentPartAEntryRanges, currentMapRanges);
                        currentPartBEntryRanges = processEntriesAndRanges(currentPartBEntryRanges, currentMapRanges);
                        currentMapRanges = null;
                        parseState = ParseState.MAP_NAME;
                    } else {
                        matcher = PATTERN_RANGE.matcher(inputLine);
                        if (!matcher.matches()) {
                            throw new IllegalStateException("Could not parse range line");
                        }
                        long destinationRangeStart = Long.parseLong(matcher.group("destinationRangeStart"));
                        long sourceRangeStart = Long.parseLong(matcher.group("sourceRangeStart"));
                        long rangeLength = Long.parseLong(matcher.group("rangeLength"));
                        currentMapRanges.add(new MapRange(sourceRangeStart,sourceRangeStart+rangeLength,destinationRangeStart-sourceRangeStart));
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected parse state.");
            }
        }
        if (parseState != ParseState.RANGE_OR_BLANK_LINE_OR_EOF) {
            throw new IllegalStateException("Unexpected parse state");
        }
        if (!expectedFromRangeName.equals("location")) {
            throw new IllegalStateException("Expected the final map to the location");
        }
        if (!currentMapRanges.isEmpty()) {
            currentPartAEntryRanges = processEntriesAndRanges(currentPartAEntryRanges, currentMapRanges);
            currentPartBEntryRanges = processEntriesAndRanges(currentPartBEntryRanges, currentMapRanges);
        }
        return new BasicPuzzleResults<>(
            getMinimum(currentPartAEntryRanges),
            getMinimum(currentPartBEntryRanges)
        );
    }

    private static EntryRange[] processEntriesAndRanges(EntryRange[] sourceEntryRanges, List<MapRange> currentMapRanges) {
        NavigableMap<Long,MapRange> mapRangesByStart = new TreeMap<>();
        for (MapRange mapRange : currentMapRanges) {
            mapRangesByStart.put(mapRange.getSourceRangeStartInclusive(),mapRange);
        }
        List<EntryRange> destinationEntryRanges = new ArrayList<>();
        for (EntryRange sourceEntryRange : sourceEntryRanges) {
            long currentEntryRangeStartInclusive = sourceEntryRange.getStartInclusive();
            long currentEntryRangeStopExclusive = sourceEntryRange.getStopExclusive();
            boolean processingNeeded = true;
            do {
                Map.Entry<Long,MapRange> potentialContainingEntry = mapRangesByStart.floorEntry(currentEntryRangeStartInclusive);
                if (potentialContainingEntry != null) {
                    MapRange mapRange = potentialContainingEntry.getValue();
                    long mapRangeStopExclusive = mapRange.getSourceRangeStopExclusive();
                    long mapRangeSourceToDestinationDifference = mapRange.getSourceToDestinationDifference();
                    if (mapRangeStopExclusive > currentEntryRangeStartInclusive) {
                        if (mapRangeStopExclusive >= currentEntryRangeStopExclusive) {
                            // Entire entry range covered by map range
                            destinationEntryRanges.add(new EntryRange(currentEntryRangeStartInclusive+mapRangeSourceToDestinationDifference,currentEntryRangeStopExclusive+mapRangeSourceToDestinationDifference));
                            processingNeeded = false;
                        }
                        else {
                            // First part of entry range covered by map range, second part of entry range not covered by this map range (but may be covered by another)
                            destinationEntryRanges.add(new EntryRange(currentEntryRangeStartInclusive+mapRangeSourceToDestinationDifference,mapRangeStopExclusive+mapRangeSourceToDestinationDifference));
                            currentEntryRangeStartInclusive = mapRangeStopExclusive;
                        }
                        continue;
                    }
                }
                // Outside a map range
                Map.Entry<Long,MapRange> potentialNextEntry = mapRangesByStart.higherEntry(currentEntryRangeStartInclusive);
                if (potentialNextEntry == null) {
                    // No map range covers this entry range at all
                    destinationEntryRanges.add(new EntryRange(currentEntryRangeStartInclusive, currentEntryRangeStopExclusive));
                    processingNeeded = false;
                } else {
                    MapRange mapRange = potentialNextEntry.getValue();
                    long mapRangeStartInclusive = mapRange.getSourceRangeStartInclusive();
                    if (mapRangeStartInclusive >= currentEntryRangeStopExclusive) {
                        // No map range covers this entry range at all
                        destinationEntryRanges.add(new EntryRange(currentEntryRangeStartInclusive, currentEntryRangeStopExclusive));
                        processingNeeded = false;
                    } else {
                        // First part of entry range not covered by any map range, second part of entry range covered by this map range
                        destinationEntryRanges.add(new EntryRange(currentEntryRangeStartInclusive, mapRangeStartInclusive));
                        currentEntryRangeStartInclusive = mapRangeStartInclusive;
                    }
                }
            } while (processingNeeded);
        }
        return destinationEntryRanges.toArray(new EntryRange[0]);
    }

    private static long getMinimum(EntryRange[] entryRanges) {
        return Arrays.stream(entryRanges).mapToLong(EntryRange::getStartInclusive).min().orElseThrow(() -> new IllegalStateException("No answer"));
    }
}
