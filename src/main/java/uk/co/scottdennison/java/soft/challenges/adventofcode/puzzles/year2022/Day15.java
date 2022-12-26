package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2022;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day15 implements IPuzzle {
    private static final Pattern PATTERN_LINE = Pattern.compile("^Sensor at x=(?<sensorX>-?[0-9]+), y=(?<sensorY>-?[0-9]+): closest beacon is at x=(?<beaconX>-?[0-9]+), y=(?<beaconY>-?[0-9]+)$");

    private static class Sensor {
        private final int sensorX;
        private final int sensorY;
        private final int manhattanDistanceToNearestBeacon;

        public Sensor(int sensorX, int sensorY, int manhattanDistanceToNearestBeacon) {
            this.sensorX = sensorX;
            this.sensorY = sensorY;
            this.manhattanDistanceToNearestBeacon = manhattanDistanceToNearestBeacon;
        }

        public int getSensorX() {
            return this.sensorX;
        }

        public int getSensorY() {
            return this.sensorY;
        }

        public int getManhattanDistanceToNearestBeacon() {
            return this.manhattanDistanceToNearestBeacon;
        }
    }

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

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        // Setup and Parsing
        int minBounds = Integer.parseInt(new String(configProvider.getPuzzleConfigChars("min_bounds")));
        int maxBounds = Integer.parseInt(new String(configProvider.getPuzzleConfigChars("max_bounds")));
        int targetRowY = Integer.parseInt(new String(configProvider.getPuzzleConfigChars("target_row_y")));
        int tuningFrequencyXMultiplier = Integer.parseInt(new String(configProvider.getPuzzleConfigChars("tuning_frequency_x_multiplier")));
        String[] inputLines = LineReader.stringsArray(inputCharacters, true);
        int sensorCount = inputLines.length;
        Sensor[] sensors = new Sensor[sensorCount];
        Map<Integer,Set<Integer>> beaconXLocationsPerY = new HashMap<>();
        for (int sensorIndex=0; sensorIndex<sensorCount; sensorIndex++) {
            Matcher matcher = PATTERN_LINE.matcher(inputLines[sensorIndex]);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("Could not parse line");
            }
            int sensorX = Integer.parseInt(matcher.group("sensorX"));
            int sensorY = Integer.parseInt(matcher.group("sensorY"));
            int beaconX = Integer.parseInt(matcher.group("beaconX"));
            int beaconY = Integer.parseInt(matcher.group("beaconY"));
            int manhattenDistanceBetweenSensorAndBeacon = Math.abs(beaconX - sensorX) + Math.abs(beaconY - sensorY);
            sensors[sensorIndex] = new Sensor(sensorX, sensorY, manhattenDistanceBetweenSensorAndBeacon);
            beaconXLocationsPerY.computeIfAbsent(beaconY,__ -> new HashSet<>()).add(beaconX);
        }

        // Part A
        Collection<Range> impossibleBeaconXRangesForTargetRow = new ArrayList<>();
        for (Sensor sensor : sensors) {
            int sensorX = sensor.getSensorX();
            int sensorY = sensor.getSensorY();
            int yDistanceBetweenSensorAndTargetRow = Math.abs(targetRowY-sensorY);
            int sensorManhattanDistanceToNearestBeacon = sensor.getManhattanDistanceToNearestBeacon();
            int sensorRangeXStartInclusiveForTargetRow = sensorX - sensorManhattanDistanceToNearestBeacon + yDistanceBetweenSensorAndTargetRow;
            int sensorRangeXEndInclusiveForTargetRow = sensorX + sensorManhattanDistanceToNearestBeacon - yDistanceBetweenSensorAndTargetRow;
            if (sensorRangeXStartInclusiveForTargetRow <= sensorRangeXEndInclusiveForTargetRow) {
                addRangeToCollection(impossibleBeaconXRangesForTargetRow, new Range(sensorRangeXStartInclusiveForTargetRow, sensorRangeXEndInclusiveForTargetRow+1));
            }
        }
        int impossibleBeaconXLocationCountForTargetRow = -Optional.ofNullable(beaconXLocationsPerY.get(targetRowY)).map(Set::size).orElse(0);
        for (Range impossibleBeaconXRangeForTargetRow : impossibleBeaconXRangesForTargetRow) {
            impossibleBeaconXLocationCountForTargetRow += impossibleBeaconXRangeForTargetRow.getEndExclusive() - impossibleBeaconXRangeForTargetRow.getStartInclusive();
        }

        // Part B
        Set<Long> tuningFrequencies = new HashSet<>();
        for (int y=minBounds; y<=maxBounds; y++) {
            int x = minBounds;
            do {
                boolean inSensorRange = false;
                int nextXOfInterest = maxBounds + 1;
                for (Sensor sensor : sensors) {
                    int sensorX = sensor.getSensorX();
                    int sensorY = sensor.getSensorY();
                    int sensorManhattanDistanceToNearestBeacon = sensor.getManhattanDistanceToNearestBeacon();
                    int xDistanceToSensor = Math.abs(x - sensorX);
                    int yDistanceToSensor = Math.abs(y - sensorY);
                    int manhattenDistanceToSensor = xDistanceToSensor + yDistanceToSensor;
                    if (manhattenDistanceToSensor <= sensorManhattanDistanceToNearestBeacon) {
                        nextXOfInterest = sensorX + sensorManhattanDistanceToNearestBeacon - yDistanceToSensor + 1;
                        inSensorRange = true;
                        break;
                    } else {
                        int sensorRangeXStartInclusiveForY = sensorX - sensorManhattanDistanceToNearestBeacon + yDistanceToSensor;
                        if (sensorRangeXStartInclusiveForY > x && sensorRangeXStartInclusiveForY < nextXOfInterest) {
                            nextXOfInterest = sensorRangeXStartInclusiveForY;
                        }
                    }
                }
                if (!inSensorRange) {
                    addTuningFrequencies(tuningFrequencies, tuningFrequencyXMultiplier, y, x, nextXOfInterest - 1);
                }
                x = nextXOfInterest;
            } while (x <= maxBounds);
        }
        long tuningFrequency;
        int tuningFrequencyCount = tuningFrequencies.size();
        switch (tuningFrequencyCount) {
            case 0:
                throw new IllegalStateException("No tuning frequencies found.");
            case 1:
                tuningFrequency = tuningFrequencies.iterator().next();
                break;
            default:
                throw new IllegalStateException("More than 1 tuning frequency found (Actually " + tuningFrequencyCount + " were found).");
        }

        // Result
        return new BasicPuzzleResults<>(
            impossibleBeaconXLocationCountForTargetRow,
            tuningFrequency
        );
    }
}
