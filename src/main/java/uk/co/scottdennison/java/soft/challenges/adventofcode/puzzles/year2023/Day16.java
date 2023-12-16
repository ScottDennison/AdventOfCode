package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2023;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class Day16 implements IPuzzle {
    private static enum Direction {
        NORTH(-1,0),
        EAST(0,1),
        SOUTH(1, 0),
        WEST(0,-1);

        private final int yDelta;
        private final int xDelta;

        Direction(int yDelta, int xDelta) {
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

    private static class BeamPoint {
        private final int y;
        private final int x;
        private final Direction direction;

        public BeamPoint(int y, int x, Direction direction) {
            this.y = y;
            this.x = x;
            this.direction = direction;
        }

        public int getY() {
            return this.y;
        }

        public int getX() {
            return this.x;
        }

        public Direction getDirection() {
            return this.direction;
        }

        @Override
        public boolean equals(Object otherObject) {
            if (this == otherObject) return true;
            if (otherObject == null || this.getClass() != otherObject.getClass()) return false;

            BeamPoint otherBeamPoint = (BeamPoint) otherObject;

            if (this.y != otherBeamPoint.y) return false;
            if (this.x != otherBeamPoint.x) return false;
            if (this.direction != otherBeamPoint.direction) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = this.y;
            result = 31 * result + this.x;
            result = 31 * result + (this.direction != null ? this.direction.hashCode() : 0);
            return result;
        }
    }

    private static class SimulationResult {
        private final BeamPoint startingBeamPoint;
        private final int tilesEnergized;

        public SimulationResult(BeamPoint startingBeamPoint, int tilesEnergized) {
            this.startingBeamPoint = startingBeamPoint;
            this.tilesEnergized = tilesEnergized;
        }

        public BeamPoint getStartingBeamPoint() {
            return this.startingBeamPoint;
        }

        public int getTilesEnergized() {
            return this.tilesEnergized;
        }
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        char[][] grid = LineReader.charArraysArray(inputCharacters, true);
        int height = grid.length;
        int width = grid[0].length;
        return new BasicPuzzleResults<>(
            simulate(grid, height, width, new BeamPoint(0, 0, Direction.EAST)).getTilesEnergized(),
            simulateBest(grid, height, width)
        );
    }

    private static int simulateBest(char[][] grid, int height, int width) {
        Collection<SimulationResult> simulationResults = new ArrayList<>();
        simulateRange(simulationResults, grid, height, width, 0, height, 0, 0, width, 1, Direction.SOUTH);
        simulateRange(simulationResults, grid, height, width, height-1, height, 0, 0, width, 1, Direction.NORTH);
        simulateRange(simulationResults, grid, height, width, 0, height, 1, 0, width, 0, Direction.EAST);
        simulateRange(simulationResults, grid, height, width, 0, height, 1, width-1, width, 0, Direction.WEST);
        return simulationResults.stream().mapToInt(SimulationResult::getTilesEnergized).max().getAsInt();
    }

    private static void simulateRange(Collection<SimulationResult> simulationResults, char[][] grid, int height, int width, int yStart, int yStop, int yDelta, int xStart, int xStop, int xDelta, Direction direction) {
        int y = yStart;
        int x = xStart;
        do {
            simulationResults.add(simulate(grid, height, width, new BeamPoint(y, x, direction)));
            y += yDelta;
            x += xDelta;
        } while (y != yStop && x != xStop);
    }

    private static SimulationResult simulate(char[][] grid, int height, int width, BeamPoint startingBeamPoint) {
        Set<BeamPoint> seenBeamPoints = new HashSet<>();
        Deque<BeamPoint> pendingBeamPoints = new LinkedList<>();
        pendingBeamPoints.add(startingBeamPoint);
        boolean[][] cellsEnergized = new boolean[height][width];
        while (true) {
            BeamPoint beamPoint = pendingBeamPoints.pollFirst();
            if (beamPoint == null) {
                break;
            }
            if (!seenBeamPoints.add(beamPoint)) {
                continue;
            }
            int y = beamPoint.getY();
            if (y < 0 || y >= height) {
                continue;
            }
            int x = beamPoint.getX();
            if (x < 0 || x >= width) {
                continue;
            }
            cellsEnergized[y][x] = true;
            Direction direction = beamPoint.getDirection();
            switch (grid[y][x]) {
                case '.':
                    pendingBeamPoints.add(moveAndCreateBeamPoint(y, x, direction));
                    break;
                case '\\':
                    switch (direction) {
                        case NORTH:
                            pendingBeamPoints.add(moveAndCreateBeamPoint(y, x, Direction.WEST));
                            break;
                        case EAST:
                            pendingBeamPoints.add(moveAndCreateBeamPoint(y, x, Direction.SOUTH));
                            break;
                        case SOUTH:
                            pendingBeamPoints.add(moveAndCreateBeamPoint(y, x, Direction.EAST));
                            break;
                        case WEST:
                            pendingBeamPoints.add(moveAndCreateBeamPoint(y, x, Direction.NORTH));
                            break;
                    }
                    break;
                case '/':
                    switch (direction) {
                        case NORTH:
                            pendingBeamPoints.add(moveAndCreateBeamPoint(y, x, Direction.EAST));
                            break;
                        case EAST:
                            pendingBeamPoints.add(moveAndCreateBeamPoint(y, x, Direction.NORTH));
                            break;
                        case SOUTH:
                            pendingBeamPoints.add(moveAndCreateBeamPoint(y, x, Direction.WEST));
                            break;
                        case WEST:
                            pendingBeamPoints.add(moveAndCreateBeamPoint(y, x, Direction.SOUTH));
                            break;
                    }
                    break;
                case '|':
                    switch (direction) {
                        case NORTH:
                        case SOUTH:
                            pendingBeamPoints.add(moveAndCreateBeamPoint(y, x, direction));
                            break;
                        case EAST:
                        case WEST:
                            pendingBeamPoints.add(moveAndCreateBeamPoint(y, x, Direction.NORTH));
                            pendingBeamPoints.add(moveAndCreateBeamPoint(y, x, Direction.SOUTH));
                            break;
                    }
                    break;
                case '-':
                    switch (direction) {
                        case NORTH:
                        case SOUTH:
                            pendingBeamPoints.add(moveAndCreateBeamPoint(y, x, Direction.EAST));
                            pendingBeamPoints.add(moveAndCreateBeamPoint(y, x, Direction.WEST));
                            break;
                        case EAST:
                        case WEST:
                            pendingBeamPoints.add(moveAndCreateBeamPoint(y, x, direction));
                            break;
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected grid character");
            }
        }
        int cellsEnergizedCount = 0;
        for (int y=0; y<height; y++) {
            boolean[] cellsEnergizedRow = cellsEnergized[y];
            for (int x=0; x<width; x++) {
                if (cellsEnergizedRow[x]) {
                    cellsEnergizedCount++;
                }
            }
        }
        return new SimulationResult(startingBeamPoint,cellsEnergizedCount);
    }

    private static BeamPoint moveAndCreateBeamPoint(int currentY, int currentX, Direction newDirection) {
        return new BeamPoint(currentY + newDirection.getYDelta(), currentX + newDirection.getXDelta(), newDirection);
    }
}
