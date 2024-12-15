package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2024;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class Day12 implements IPuzzle {
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

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        char[][] inputGrid = LineReader.charArraysArray(inputCharacters, true);
        int gridHeight = inputGrid.length;
        int gridWidth = inputGrid[0].length;
        Set<Coordinate> unvisitedCoordinates = new HashSet<>();
        Direction[] directions = Direction.values();
        int directionCount = directions.length;
        for (int y=0; y<gridHeight; y++) {
            for (int x=0; x<gridWidth; x++) {
                unvisitedCoordinates.add(new Coordinate(y, x));
            }
        }
        int totalPartACost = 0;
        int totalPartBCost = 0;
        while (true) {
            Iterator<Coordinate> unvisitedCoordinatesIterator = unvisitedCoordinates.iterator();
            if (!unvisitedCoordinatesIterator.hasNext()) {
                break;
            }
            Coordinate rootCoordinate = unvisitedCoordinatesIterator.next();
            unvisitedCoordinatesIterator.remove();
            char regionCharacter = inputGrid[rootCoordinate.getY()][rootCoordinate.getX()];
            Deque<Coordinate> pendingCoordinatesToCheck = new LinkedList<>();
            pendingCoordinatesToCheck.addFirst(rootCoordinate);
            Map<Direction,Set<Coordinate>> unvisitedCoordinateSides = new HashMap<>();
            for (Direction direction : directions) {
                unvisitedCoordinateSides.put(direction,new HashSet<>());
            }
            int regionArea = 0;
            int regionPerimiter = 0;
            Coordinate currentCoordinate;
            while ((currentCoordinate = pendingCoordinatesToCheck.pollFirst()) != null) {
                int y = currentCoordinate.getY();
                int x = currentCoordinate.getX();
                regionArea++;
                for (Direction direction : directions) {
                    int newY = y + direction.getYDelta();
                    int newX = x + direction.getXDelta();
                    if (newY >= 0 && newX >= 0 && newY < gridHeight && newX < gridWidth && inputGrid[newY][newX] == regionCharacter) {
                        Coordinate newCoordinate = new Coordinate(newY, newX);
                        if (unvisitedCoordinates.remove(newCoordinate)) {
                            pendingCoordinatesToCheck.addLast(newCoordinate);
                        }
                    }
                    else {
                        regionPerimiter++;
                        unvisitedCoordinateSides.get(direction).add(currentCoordinate);
                    }
                }
            }
            // North arbitarily chosen, all shape sides must have at least one coordinate with a west perimiter, one with an north perimiter, etc
            Direction checkDirection = Direction.WEST;
            int regionSides = 0;
            while (true) {
                Iterator<Coordinate> nextCoordinateIterator = unvisitedCoordinateSides.get(checkDirection).iterator();
                if (!nextCoordinateIterator.hasNext()) {
                    if (unvisitedCoordinateSides.values().stream().mapToInt(Set::size).sum() > 0) {
                        throw new IllegalStateException("No sides to iterate, yet still some unvisited sides left.");
                    }
                    break;
                }
                Coordinate startCoordinate = nextCoordinateIterator.next();;
                Direction startDirection = checkDirection;
                currentCoordinate = startCoordinate;
                int shapeSides = 0;
                while (true) {
                    if (shapeSides > 0 && checkDirection == startDirection && currentCoordinate.equals(startCoordinate)) {
                        break;
                    }
                    if (unvisitedCoordinateSides.get(checkDirection).remove(currentCoordinate)) {
                        Direction movementDirectionOrNewCheckDirection = directions[(checkDirection.ordinal() + 1) % directionCount];
                        if (movementDirectionOrNewCheckDirection == startDirection && currentCoordinate.equals(startCoordinate)) {
                            shapeSides++;
                            break;
                        }
                        else if (unvisitedCoordinateSides.get(movementDirectionOrNewCheckDirection).contains(currentCoordinate)) {
                            checkDirection = movementDirectionOrNewCheckDirection;
                            shapeSides++;
                        }
                        else {
                            currentCoordinate = new Coordinate(currentCoordinate.getY() + movementDirectionOrNewCheckDirection.getYDelta(), currentCoordinate.getX() + movementDirectionOrNewCheckDirection.getXDelta());
                        }
                    }
                    else {
                        currentCoordinate = new Coordinate(currentCoordinate.getY() + checkDirection.getYDelta(), currentCoordinate.getX() + checkDirection.getXDelta());
                        checkDirection = directions[(checkDirection.ordinal() -1 + directionCount) % directionCount];
                        shapeSides++;
                    }
                }
                regionSides += shapeSides;
            }
            totalPartACost += regionArea * regionPerimiter;
            totalPartBCost += regionArea * regionSides;
        }
        return new BasicPuzzleResults<>(
            totalPartACost,
            totalPartBCost
        );
    }
}
