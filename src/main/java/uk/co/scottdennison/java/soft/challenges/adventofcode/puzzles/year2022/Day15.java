package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2022;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day15 implements IPuzzle {
    private static final Pattern PATTERN_LINE = Pattern.compile("^Sensor at x=(?<sensorX>-?[0-9]+), y=(?<sensorY>-?[0-9]+): closest beacon is at x=(?<beaconX>-?[0-9]+), y=(?<beaconY>-?[0-9]+)$");

    private static final class Range {
        private final int startInclusive;
        private final int endExclusive;

        private Range(int startInclusive, int endExclusive) {
            this.startInclusive = startInclusive;
            this.endExclusive = endExclusive;
        }

        public int getStartInclusive() {
            return this.startInclusive;
        }

        public int getEndExclusive() {
            return this.endExclusive;
        }
    }

    private static void addTuningFrequencies(Set<Long> tuningFrequencies, long tuningFrequencyXMultiplier, long y, long minX, long maxX) {
        for (long x=minX; x<=maxX; x++) {
            tuningFrequencies.add((x*tuningFrequencyXMultiplier)+y);
        }
    }

    private static void addRangeToCollection(Collection<Range> ranges, Range newRange) {
        if (!ranges.isEmpty()) {
            boolean rangeRequiresCombineCheck = true;
            while (rangeRequiresCombineCheck) {
                rangeRequiresCombineCheck = false;
                Iterator<Range> existingImpossibleBeaconXRangesIterator = ranges.iterator();
                while (existingImpossibleBeaconXRangesIterator.hasNext()) {
                    Range existingImpossibleBeaconXRange = existingImpossibleBeaconXRangesIterator.next();
                    if ((newRange.getStartInclusive() <= existingImpossibleBeaconXRange.getEndExclusive()) && (newRange.getEndExclusive() >= existingImpossibleBeaconXRange.getStartInclusive())) {
                        existingImpossibleBeaconXRangesIterator.remove();
                        newRange = new Range(Math.min(newRange.getStartInclusive(), existingImpossibleBeaconXRange.getStartInclusive()), Math.max(newRange.getEndExclusive(), existingImpossibleBeaconXRange.getEndExclusive()));
                        rangeRequiresCombineCheck = true;
                        break;
                    }
                }
            }
        }
        ranges.add(newRange);
    }

    private static void iterateRows(Collection<Range>[] impossibleBeaconXRangesPerY, int sensorX, int sensorY, int minBounds, int maxBounds, int manhattenDistanceBetweenSensorAndBeacon, int addition) {
        for (
            int index=0, y=sensorY, minXAtY=sensorX-manhattenDistanceBetweenSensorAndBeacon, maxXAtY=sensorX+manhattenDistanceBetweenSensorAndBeacon;
            index<=manhattenDistanceBetweenSensorAndBeacon && y >= minBounds && y <= maxBounds;
            index++, y += addition, minXAtY++, maxXAtY--
        ) {
            int arrayY = y-minBounds;
            Collection<Range> impossibleBeaconXRangesAtY = impossibleBeaconXRangesPerY[arrayY];
            if (impossibleBeaconXRangesAtY == null) {
                impossibleBeaconXRangesPerY[arrayY] = impossibleBeaconXRangesAtY = new LinkedList<>();
            }
            addRangeToCollection(impossibleBeaconXRangesAtY, new Range(minXAtY,maxXAtY+1));
        }
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        int minBounds = Integer.parseInt(new String(configProvider.getPuzzleConfigChars("min_bounds")));
        int maxBounds = Integer.parseInt(new String(configProvider.getPuzzleConfigChars("max_bounds")));
        int targetRowY = Integer.parseInt(new String(configProvider.getPuzzleConfigChars("target_row_y")));
        int tuningFrequencyXMultiplier = Integer.parseInt(new String(configProvider.getPuzzleConfigChars("tuning_frequency_x_multiplier")));
        if (minBounds > maxBounds || targetRowY < minBounds || targetRowY > maxBounds) {
            throw new IllegalStateException("Invalid configuration");
        }
        @SuppressWarnings("unsafe")
        Collection<Range>[] impossibleBeaconXRangesPerY = new Collection[maxBounds-minBounds+1];
        Map<Integer,Set<Integer>> beaconXLocationsPerY = new HashMap<>();
        for (String inputLine : LineReader.strings(inputCharacters)) {
            Matcher matcher = PATTERN_LINE.matcher(inputLine);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("Could not parse line");
            }
            int sensorX = Integer.parseInt(matcher.group("sensorX"));
            int sensorY = Integer.parseInt(matcher.group("sensorY"));
            int beaconX = Integer.parseInt(matcher.group("beaconX"));
            int beaconY = Integer.parseInt(matcher.group("beaconY"));
            int manhattenDistanceBetweenSensorAndBeacon = Math.abs(beaconX-sensorX)+Math.abs(beaconY-sensorY);
            iterateRows(impossibleBeaconXRangesPerY,sensorX,sensorY,minBounds,maxBounds,manhattenDistanceBetweenSensorAndBeacon,-1);
            iterateRows(impossibleBeaconXRangesPerY,sensorX,sensorY,minBounds,maxBounds,manhattenDistanceBetweenSensorAndBeacon,1);
            beaconXLocationsPerY.computeIfAbsent(beaconY,__ -> new HashSet<>()).add(beaconX);
        }
        int impossibleBeaconXLocationSum = -Optional.ofNullable(beaconXLocationsPerY.get(targetRowY)).map(Set::size).orElse(0);
        Collection<Range> impossibleBeaconXRangesAtTargetRowY = impossibleBeaconXRangesPerY[targetRowY-minBounds];
        if (impossibleBeaconXRangesAtTargetRowY != null) {
            for (Range range : impossibleBeaconXRangesAtTargetRowY) {
                impossibleBeaconXLocationSum += range.getEndExclusive() - range.getStartInclusive();
            }
        }
        Set<Long> tuningFrequencies = new HashSet<>();
        Comparator<Range> rangeComparator = Comparator.comparing(Range::getStartInclusive).thenComparing(Range::getEndExclusive);
        for (int y=minBounds, arrayY=0; y<=maxBounds; y++, arrayY++) {
            Collection<Range> existingImpossibleBeaconXRangesAtY = impossibleBeaconXRangesPerY[arrayY];
            if (existingImpossibleBeaconXRangesAtY == null || existingImpossibleBeaconXRangesAtY.isEmpty()) {
                addTuningFrequencies(tuningFrequencies, tuningFrequencyXMultiplier, y, minBounds, maxBounds);
            } else {
                if (existingImpossibleBeaconXRangesAtY.size() > 1) {
                    List<Range> existingImpossibleBeaconXRangesAtYList = new ArrayList<>(existingImpossibleBeaconXRangesAtY);
                    existingImpossibleBeaconXRangesAtY = existingImpossibleBeaconXRangesAtYList;
                    Collections.sort(existingImpossibleBeaconXRangesAtYList, rangeComparator);
                }
                int minEncounteredX = Integer.MAX_VALUE;
                int maxEncounteredX = Integer.MIN_VALUE;
                int lastRangeMaxX = 0;
                boolean isFirstRange = true;
                for (Range range : existingImpossibleBeaconXRangesAtY) {
                    int rangeMinX = range.getStartInclusive();
                    int rangeMaxX = range.getEndExclusive()-1;
                    if (rangeMaxX < minBounds || rangeMinX > maxBounds) {
                        // Range is entirely outside of bounds, ignore.
                        continue;
                    }
                    int adjustedRangeMinX = Math.min(Math.max(rangeMinX, minBounds), maxBounds);
                    int adjustedRangeMaxX = Math.min(Math.max(rangeMaxX, minBounds), maxBounds);
                    minEncounteredX = Math.min(minEncounteredX, adjustedRangeMinX);
                    maxEncounteredX = Math.max(maxEncounteredX, adjustedRangeMaxX);
                    if (isFirstRange) {
                        isFirstRange = false;
                    } else {
                        addTuningFrequencies(tuningFrequencies, tuningFrequencyXMultiplier, y, lastRangeMaxX+1, adjustedRangeMinX-1);
                    }
                    lastRangeMaxX = adjustedRangeMaxX;
                }
                if (minEncounteredX > minBounds) {
                    addTuningFrequencies(tuningFrequencies, tuningFrequencyXMultiplier, y, minBounds, minEncounteredX-1);
                }
                if (maxEncounteredX < maxBounds) {
                    addTuningFrequencies(tuningFrequencies, tuningFrequencyXMultiplier, y, maxEncounteredX+1, maxBounds);
                }
            }
        }
        int tuningFrequencyCount = tuningFrequencies.size();
        switch (tuningFrequencyCount) {
            case 0:
                throw new IllegalStateException("No tuning frequencies found.");
            case 1:
                return new BasicPuzzleResults<>(
                    impossibleBeaconXLocationSum,
                    tuningFrequencies.iterator().next()
                );
            default:
                throw new IllegalStateException("More than 1 tuning frequency found (Actually " + tuningFrequencyCount + " were found).");
        }
    }
}
