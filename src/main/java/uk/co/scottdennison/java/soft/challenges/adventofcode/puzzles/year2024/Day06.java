package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2024;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class Day06 implements IPuzzle {
    private static enum Direction {
        UP (-1, 0),
        RIGHT (0, 1),
        DOWN (1, 0),
        LEFT (0, -1);

        private final int yDelta;
        private final int xDelta;

        Direction(int yDelta, int xDelta) {
            this.yDelta = yDelta;
            this.xDelta = xDelta;
        }

        public int getYDelta() {
            return yDelta;
        }

        public int getXDelta() {
            return xDelta;
        }
    }

    private static final class Position {
        private final int y;
        private final int x;

        public Position(int y, int x) {
            this.y = y;
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public int getX() {
            return x;
        }

        @Override
        public final boolean equals(Object otherObject) {
            if (this == otherObject) return true;
            if (!(otherObject instanceof Position)) return false;
            Position otherPosition = (Position) otherObject;
            return y == otherPosition.y && x == otherPosition.x;
        }

        @Override
        public int hashCode() {
            int result = y;
            result = 31 * result + x;
            return result;
        }
    }

    private static class FirstVisitInfo {
        private final int stepNumber;
        private final PositionAndDirection previousPositionAndDirection;

        private FirstVisitInfo(int stepNumber, PositionAndDirection previousPositionAndDirection) {
            this.stepNumber = stepNumber;
            this.previousPositionAndDirection = previousPositionAndDirection;
        }

        public int getStepNumber() {
            return stepNumber;
        }

        public PositionAndDirection getPreviousPositionAndDirection() {
            return previousPositionAndDirection;
        }

        // Never used as a key, no equals() or hashCode() needed
    }

    private static final class PositionAndDirection {
        // Using x/y again instead of a Position field saves on average about 30ms.
        private final int y;
        private final int x;
        private final Direction direction;

        public PositionAndDirection(int y, int x, Direction direction) {
            this.y = y;
            this.x = x;
            this.direction = direction;
        }

        public int getY() {
            return y;
        }

        public int getX() {
            return x;
        }

        public Direction getDirection() {
            return direction;
        }

        @Override
        public final boolean equals(Object otherObject) {
            if (this == otherObject) return true;
            if (!(otherObject instanceof PositionAndDirection)) return false;
            PositionAndDirection otherPositionAndDirection = (PositionAndDirection) otherObject;
            return y == otherPositionAndDirection.y && x == otherPositionAndDirection.x && direction == otherPositionAndDirection.direction;
        }

        @Override
        public int hashCode() {
            int result = y;
            result = 31 * result + x;
            result = 31 * result + direction.hashCode();
            return result;
        }
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        char[][] inputGridCharacters = LineReader.charArraysArray(inputCharacters, true);
        int height = inputGridCharacters.length;
        int width = inputGridCharacters[0].length;
        boolean[][] inputGrid = new boolean[height][width];
        int guardY = 0;
        int guardX = 0;
        Direction guardDirection = null;
        for (int y=0; y<height; y++) {
            for (int x = 0; x < width; x++) {
                char inputCharacter = inputGridCharacters[y][x];
                Direction possibleGuardDirection;
                boolean positionBlocked = false;
                switch (inputCharacter) {
                    case '^':
                        possibleGuardDirection = Direction.UP;
                        break;
                    case '>':
                        possibleGuardDirection = Direction.RIGHT;
                        break;
                    case 'v':
                        possibleGuardDirection = Direction.DOWN;
                        break;
                    case '<':
                        possibleGuardDirection = Direction.LEFT;
                        break;
                    case '#':
                        positionBlocked = true;
                        possibleGuardDirection = null;
                        break;
                    default:
                        possibleGuardDirection = null;
                        break;
                }
                if (possibleGuardDirection != null) {
                    if (guardDirection != null) {
                        throw new IllegalStateException("Multiple guard locations found");
                    }
                    guardY = y;
                    guardX = x;
                    guardDirection = possibleGuardDirection;
                }
                inputGrid[y][x] = positionBlocked;
            }
        }
        Direction[] possibleDirections = Direction.values();
        int possibleDirectionCount = possibleDirections.length;

        // --- Part A ---

        Map<Position,FirstVisitInfo> partAVisitedCells = new LinkedHashMap<>();
        Map<PositionAndDirection,Integer> partAVisitedCellsWithDirections = new HashMap<>();
        int stepNumber = 0;
        PositionAndDirection previousPositionAndDirection = null;
        while (true) {
            partAVisitedCells.putIfAbsent(new Position(guardY, guardX), new FirstVisitInfo(stepNumber, previousPositionAndDirection));
            PositionAndDirection positionAndDirection = new PositionAndDirection(guardY, guardX, guardDirection);
            partAVisitedCellsWithDirections.put(positionAndDirection, stepNumber);
            previousPositionAndDirection = positionAndDirection;
            stepNumber++;
            int potentialNewGuardY = guardY + guardDirection.getYDelta();
            int potentialNewGuardX = guardX + guardDirection.getXDelta();
            if (potentialNewGuardY < 0 || potentialNewGuardY >= height || potentialNewGuardX < 0 || potentialNewGuardX >= width) {
                break;
            }
            if (inputGrid[potentialNewGuardY][potentialNewGuardX]) {
                guardDirection = possibleDirections[(guardDirection.ordinal() + 1) % possibleDirectionCount];
            }
            else {
                guardY = potentialNewGuardY;
                guardX = potentialNewGuardX;
            }
        }

        /// --- Part B ---

        int partBObstacleCandidatesFound = 0;
        Iterator<Map.Entry<Position,FirstVisitInfo>> partAVisitedCellEntryIterator = partAVisitedCells.entrySet().iterator();
        partAVisitedCellEntryIterator.next(); // Skip the first entry, we can't put an entry on top of the guard!
        while (partAVisitedCellEntryIterator.hasNext()) {
            Map.Entry<Position,FirstVisitInfo> partAVisitedCellEntry = partAVisitedCellEntryIterator.next();
            Position obstaclePosition = partAVisitedCellEntry.getKey();
            int obstacleY = obstaclePosition.getY();
            int obstacleX = obstaclePosition.getX();
            FirstVisitInfo firstVisitInfo = partAVisitedCellEntry.getValue();
            int startingStepNumber = firstVisitInfo.getStepNumber();
            PositionAndDirection guardStartPosition = firstVisitInfo.getPreviousPositionAndDirection();
            guardY = guardStartPosition.getY();
            guardX = guardStartPosition.getX();
            guardDirection = possibleDirections[(guardStartPosition.getDirection().ordinal() + 1) % possibleDirectionCount]; // We know the first thing we are going to need to do is rotate.
            inputGrid[obstacleY][obstacleX] = true;
            Set<PositionAndDirection> partBVisitedCells = new HashSet<>();
            boolean loopDetected = false;
            while (true) {
                PositionAndDirection positionAndDirection = new PositionAndDirection(guardY, guardX, guardDirection);
                Integer partAVisitStep = partAVisitedCellsWithDirections.get(positionAndDirection);
                if (
                    (partAVisitStep != null && partAVisitStep < startingStepNumber) // Did we visit this position in part A, BEFORE we diverged?
                    ||
                    !partBVisitedCells.add(positionAndDirection) // Did we visit this position AFTER we diverged.
                ) {
                    loopDetected = true;
                    break;
                }
                int potentialNewGuardY = guardY + guardDirection.getYDelta();
                int potentialNewGuardX = guardX + guardDirection.getXDelta();
                if (potentialNewGuardY < 0 || potentialNewGuardY >= height || potentialNewGuardX < 0 || potentialNewGuardX >= width) {
                    break;
                }
                if (inputGrid[potentialNewGuardY][potentialNewGuardX]) {
                    guardDirection = possibleDirections[(guardDirection.ordinal() + 1) % possibleDirectionCount];
                }
                else {
                    guardY = potentialNewGuardY;
                    guardX = potentialNewGuardX;
                }
            }
            inputGrid[obstacleY][obstacleX] = false;
            if (loopDetected) {
                partBObstacleCandidatesFound++;
            }
        }

        // --- Result ---

        return new BasicPuzzleResults<>(
            partAVisitedCells.size(),
            partBObstacleCandidatesFound
        );
    }
}
