package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2018;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Deque;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day06 implements IPuzzle {
    private static class Coordinate {
        private final int x;
        private final int y;

        public Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }

    private static class PendingEntry {
        private final int x;
        private final int y;
        private final int steps;

        public PendingEntry(int x, int y, int steps) {
            this.x = x;
            this.y = y;
            this.steps = steps;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getSteps() {
            return steps;
        }
    }

    private static final Pattern PATTERN_LINE = Pattern.compile("^(?<x>[0-9]+), (?<y>[0-9]+)$");

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        String[] inputLines = LineReader.stringsArray(inputCharacters, true);
        int areaDistanceLimit = Integer.parseInt(new String(configProvider.getPuzzleConfigChars("area_distance_limit")));
        int areaCount = inputLines.length;
        Coordinate[] areaStartingCoordinates = new Coordinate[areaCount];
        int maxX = 0;
        int maxY = 0;
        for (int areaIndex=0; areaIndex<areaCount; areaIndex++) {
            Matcher matcher = PATTERN_LINE.matcher(inputLines[areaIndex]);
            if (!matcher.matches()) {
                throw new IllegalStateException("Could not parse input line");
            }
            int x = Integer.parseInt(matcher.group("x"));
            int y = Integer.parseInt(matcher.group("y"));
            if (x > maxX) {
                maxX = x;
            }
            if (y > maxY) {
                maxY = y;
            }
            Coordinate coordinate = new Coordinate(x,y);
            areaStartingCoordinates[areaIndex] = new Coordinate(x,y);
        }
        int[][][] distancesFromCoordinate = new int[areaCount][][];
        for (int areaIndex=0; areaIndex<areaCount; areaIndex++) {
            int[][] distancesFromCoordinateForArea = new int[maxY+1][maxX+1];
            Coordinate areaStartingCoordinate = areaStartingCoordinates[areaIndex];
            Deque<PendingEntry> pendingEntries = new LinkedList<>();
            pendingEntries.add(new PendingEntry(areaStartingCoordinate.getX(), areaStartingCoordinate.getY(), 1));
            PendingEntry pendingEntry;
            while ((pendingEntry = pendingEntries.pollFirst()) != null) {
                int steps = pendingEntry.getSteps();
                int x = pendingEntry.getX();
                int y = pendingEntry.getY();
                int currentSteps = distancesFromCoordinateForArea[y][x];
                if (currentSteps == 0 || currentSteps > steps) {
                    distancesFromCoordinateForArea[y][x] = steps;
                    int stepsPlusOne = steps + 1;
                    if (x > 0) {
                        pendingEntries.add(new PendingEntry(x - 1, y, stepsPlusOne));
                    }
                    if (x < maxX) {
                        pendingEntries.add(new PendingEntry(x + 1, y, stepsPlusOne));
                    }
                    if (y > 0) {
                        pendingEntries.add(new PendingEntry(x, y - 1, stepsPlusOne));
                    }
                    if (y < maxY) {
                        pendingEntries.add(new PendingEntry(x, y + 1, stepsPlusOne));
                    }
                }
            }
            distancesFromCoordinate[areaIndex] = distancesFromCoordinateForArea;
        }
        boolean[] areasAreInfinite = new boolean[areaCount];
        int[] areaSizes = new int[areaCount];
        int safeSquareCount = 0;
        for (int y=0; y<=maxY; y++) {
            for (int x=0; x<=maxX; x++) {
                int smallestDistance = Integer.MAX_VALUE;
                int closestAreaIndex = -1;
                boolean clash = false;
                int distanceSum = 0;
                for (int areaIndex=0; areaIndex<areaCount; areaIndex++) {
                    int distance = distancesFromCoordinate[areaIndex][y][x] - 1;
                    distanceSum += distance;
                    if (distance < smallestDistance) {
                        smallestDistance = distance;
                        closestAreaIndex = areaIndex;
                        clash = false;
                    }
                    else if (distance == smallestDistance) {
                        clash = true;
                    }
                }
                if (!clash) {
                    areaSizes[closestAreaIndex]++;
                    if (x == 0 || x == maxX || y == 0 || y == maxY) {
                        areasAreInfinite[closestAreaIndex] = true;
                    }
                }
                if (distanceSum < areaDistanceLimit) {
                    safeSquareCount++;
                }
            }
        }
        int largestNonInfiniteAreaSize = -1;
        for (int areaIndex=0; areaIndex<areaCount; areaIndex++) {
            if (!areasAreInfinite[areaIndex]) {
                int areaSize = areaSizes[areaIndex];
                if (areaSize > largestNonInfiniteAreaSize) {
                    largestNonInfiniteAreaSize = areaSize;
                }
            }
        }
        return new BasicPuzzleResults<>(
            largestNonInfiniteAreaSize,
            safeSquareCount
        );
    }
}
