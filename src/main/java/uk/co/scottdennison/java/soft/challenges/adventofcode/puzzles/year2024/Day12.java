package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2024;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

public class Day12 implements IPuzzle {
    private static final class Coordinate {
        private final int y;
        private final int x;

        public Coordinate(int y, int x) {
            this.y = y;
            this.x = x;
        }

        public int getY() {
            return this.y;
        }

        public int getX() {
            return this.x;
        }

        @Override
        public boolean equals(Object otherObject) {
            if (this == otherObject) return true;
            if (!(otherObject instanceof Coordinate)) return false;

            Coordinate otherCoordinate = (Coordinate) otherObject;

            if (this.y != otherCoordinate.y) return false;
            if (this.x != otherCoordinate.x) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = this.y;
            result = 31 * result + this.x;
            return result;
        }
    }

    private static enum Direction {
        NORTH (-1, 0),
        EAST (0, 1),
        SOUTH (1, 0),
        WEST (0, -1);

        private final int yDelta;
        private final int xDelta;

        private Direction(int yDelta, int xDelta) {
            this.yDelta = yDelta;
            this.xDelta = xDelta;
        }

        public int getYDelta() {
            return this.yDelta;
        }

        public int getXDelta() {
            return this.xDelta;
        }
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        char[][] inputGrid = LineReader.charArraysArray(inputCharacters, true);
        int gridHeight = inputGrid.length;
        int gridWidth = inputGrid[0].length;
        Set<Coordinate> unvisitedCoordinates = new HashSet<>();
        Direction[] directions = Direction.values();
        for (int y=0; y<gridHeight; y++) {
            for (int x=0; x<gridWidth; x++) {
                unvisitedCoordinates.add(new Coordinate(y, x));
            }
        }
        int totalCost = 0;
        while (true) {
            Iterator<Coordinate> unvisitedCoordinatesIterator = unvisitedCoordinates.iterator();
            if (!unvisitedCoordinatesIterator.hasNext()) {
                break;
            }
            Coordinate rootCoordinate = unvisitedCoordinatesIterator.next();
            unvisitedCoordinatesIterator.remove();
            char rootCharacter = inputGrid[rootCoordinate.getY()][rootCoordinate.getX()];
            int area = 0;
            int perimiter = 0;
            Deque<Coordinate> pendingCoordinatesToCheck = new LinkedList<>();
            pendingCoordinatesToCheck.addFirst(rootCoordinate);
            Coordinate coordinateToCheck;
            while ((coordinateToCheck = pendingCoordinatesToCheck.pollFirst()) != null) {
                int y = coordinateToCheck.getY();
                int x = coordinateToCheck.getX();
                area++;
                perimiter += 4;
                for (Direction direction : directions) {
                    int newY = y + direction.getYDelta();
                    int newX = x + direction.getXDelta();
                    if (newY < 0 || newX < 0 || newY >= gridHeight || newX >= gridWidth) {
                        continue;
                    }
                    if (inputGrid[newY][newX] == rootCharacter) {
                        perimiter--;
                        Coordinate newCoordinate = new Coordinate(newY, newX);
                        if (unvisitedCoordinates.remove(newCoordinate)) {
                            pendingCoordinatesToCheck.addLast(newCoordinate);
                        }
                    }
                }
            }
            int cost = area * perimiter;
            printWriter.println("A region of " + rootCharacter + " plants with price " + area + " * " + perimiter + " = " + cost);
            totalCost += cost;
        }
        return new BasicPuzzleResults<>(
            totalCost,
            null
        );
    }
}
