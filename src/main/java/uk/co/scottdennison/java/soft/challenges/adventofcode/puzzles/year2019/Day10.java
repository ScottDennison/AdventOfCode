package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2019;

import uk.co.scottdennison.java.libs.math.ExtendedEuclideanAlgorithm;
import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;

public class Day10 implements IPuzzle {
    private static final int PART_2_TARGET_ASTEROID = 200;

    private static class AsteroidPosition {
        private final int x;
        private final int y;

        public AsteroidPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        char[][] inputCharacterGrid = LineReader.charArraysArray(inputCharacters, true);
        int gridHeight = inputCharacterGrid.length;
        int gridWidth = inputCharacterGrid[0].length;
        List<AsteroidPosition> asteroidPositionList = new ArrayList<>();
        for (int y = 0; y < gridHeight; y++) {
            for (int x = 0; x < gridWidth; x++) {
                switch (inputCharacterGrid[y][x]) {
                    case '#':
                        asteroidPositionList.add(new AsteroidPosition(x, y));
                        break;
                    case '.':
                        break;
                    default:
                        throw new IllegalStateException("Unexpected character");
                }
            }
        }
        AsteroidPosition[] asteroidPositionArray = asteroidPositionList.toArray(new AsteroidPosition[0]);

        int bestTargetableAsteroidCount = -1;
        boolean bestTargetableAsteroidCountHasClashes = false;
        NavigableMap<Double,NavigableMap<Double,AsteroidPosition>> bestTargetableAsteroidCountCollisionData = null;
        for (AsteroidPosition monitoringStationAsteroidPosition : asteroidPositionArray) {
            int monitoringStationX = monitoringStationAsteroidPosition.getX();
            int monitoringStationY = monitoringStationAsteroidPosition.getY();
            NavigableMap<Double,NavigableMap<Double,AsteroidPosition>> targetableAsteroidCountCollisionData = new TreeMap<>();
            for (AsteroidPosition otherAsteroidPosition : asteroidPositionArray) {
                if (monitoringStationAsteroidPosition == otherAsteroidPosition) { // Reference equality
                    continue;
                }
                int xDelta = otherAsteroidPosition.getX() - monitoringStationX;
                int yDelta = otherAsteroidPosition.getY() - monitoringStationY;
                targetableAsteroidCountCollisionData.computeIfAbsent(
                    (Math.toDegrees(Math.atan2(yDelta, xDelta)) + 450) % 360,
                    __ -> new TreeMap<>()
                ).put(
                    Math.sqrt((xDelta * xDelta) + (yDelta * yDelta)),
                    otherAsteroidPosition
                );
            }
            int targetableAsteroidCount = targetableAsteroidCountCollisionData.size();
            if (targetableAsteroidCount > bestTargetableAsteroidCount) {
                bestTargetableAsteroidCount = targetableAsteroidCount;
                bestTargetableAsteroidCountHasClashes = false;
                bestTargetableAsteroidCountCollisionData = targetableAsteroidCountCollisionData;
            }
            else if (targetableAsteroidCount == bestTargetableAsteroidCount) {
                bestTargetableAsteroidCountHasClashes = true;
            }
        }
        if (bestTargetableAsteroidCountHasClashes) {
            throw new IllegalStateException("Multiple asteroids are the best location.");
        }
        List<AsteroidPosition> asteroidsInTargetedOrder = new ArrayList<>();
        while (!bestTargetableAsteroidCountCollisionData.isEmpty()) {
            Iterator<NavigableMap<Double,AsteroidPosition>> angleEntryIterator = bestTargetableAsteroidCountCollisionData.values().iterator();
            while (angleEntryIterator.hasNext()) {
                NavigableMap<Double,AsteroidPosition> angleEntry = angleEntryIterator.next();
                NavigableMap.Entry<Double, AsteroidPosition> asteroidPositionEntry = angleEntry.pollFirstEntry();
                if (asteroidPositionEntry == null) {
                    angleEntryIterator.remove();
                }
                else {
                    asteroidsInTargetedOrder.add(asteroidPositionEntry.getValue());
                }
            }
        }
        Integer part2Solution;
        if (asteroidsInTargetedOrder.size() >= PART_2_TARGET_ASTEROID) {
            AsteroidPosition asteroidPosition = asteroidsInTargetedOrder.get(PART_2_TARGET_ASTEROID - 1);
            part2Solution = (asteroidPosition.getX() * 100) + asteroidPosition.getY();
        }
        else {
            part2Solution = null;
        }
        return new BasicPuzzleResults<>(
            bestTargetableAsteroidCount,
            part2Solution
        );
    }
}
