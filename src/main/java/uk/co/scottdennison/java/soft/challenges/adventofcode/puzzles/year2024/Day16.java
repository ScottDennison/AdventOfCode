package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2024;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.common.AStarSolver;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class Day16 implements IPuzzle {
    private static class Coordinate {
        private final int y;
        private final int x;

        private Coordinate(int y, int x) {
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

        @Override
        public String toString() {
            return "Coordinate{" +
                "y=" + this.y + ", " +
                "x=" + this.x + "}";
        }
    }

    private enum Direction {
        NORTH (-1, 0),
        EAST (0, 1),
        SOUTH (1, 0),
        WEST (0, -1);

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
    };

    private static class DirectionalCoordinate {
        private final Coordinate coordinate;
        private final Direction direction;

        public DirectionalCoordinate(Coordinate coordinate, Direction direction) {
            this.coordinate = coordinate;
            this.direction = direction;
        }

        public Coordinate getCoordinate() {
            return this.coordinate;
        }

        public Direction getDirection() {
            return this.direction;
        }

        @Override
        public boolean equals(Object otherObject) {
            if (this == otherObject) return true;
            if (!(otherObject instanceof DirectionalCoordinate)) return false;

            DirectionalCoordinate otherDirectionalCoordinate = (DirectionalCoordinate) otherObject;

            if (!this.coordinate.equals(otherDirectionalCoordinate.coordinate)) return false;
            if (this.direction != otherDirectionalCoordinate.direction) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = this.coordinate.hashCode();
            result = 31 * result + (this.direction != null ? this.direction.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "DirectionalCoordinate{" +
                "coordinate=" + this.coordinate + ", " +
                "direction=" + this.direction + "}";
        }
    }

    private static Coordinate findAndReplaceCharacter(char[][] grid, int gridHeight, int gridWidth, char character) {
        Coordinate coordinate = null;
        for (int y=0; y<gridHeight; y++) {
            for (int x=0; x<gridWidth; x++) {
                if (grid[y][x] == character) {
                    if (coordinate != null) {
                        throw new IllegalStateException("Multiple of character " + character + " found.");
                    }
                    coordinate = new Coordinate(y, x);
                    grid[y][x] = '.';
                }
            }
        }
        if (coordinate == null) {
            throw new IllegalStateException("None of character " + character + " found.");
        }
        return coordinate;
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        char[][] grid = LineReader.charArraysArray(inputCharacters, true);
        int gridHeight = grid.length;
        int gridWidth = grid[0].length;
        Coordinate start = findAndReplaceCharacter(grid, gridHeight, gridWidth, 'S');
        Coordinate end = findAndReplaceCharacter(grid, gridHeight, gridWidth, 'E');
        Direction[] directions = Direction.values();
        int directionCount = directions.length;
        Direction[] nextDirection = new Direction[directionCount];
        Direction[] previousDirection = new Direction[directionCount];
        for (int directionOrdinal=0; directionOrdinal<directionCount; directionOrdinal++) {
            nextDirection[directionOrdinal] = directions[(directionOrdinal + 1) % directionCount];
            previousDirection[directionOrdinal] = directions[(directionOrdinal - 1 + directionCount) % directionCount];
        }
        AStarSolver.CostIncludingResultAdapter.CostAdaptedResult<Integer,Map<Coordinate,Set<Coordinate>>> result = AStarSolver.run(
            new AStarSolver.NodeAdapter<DirectionalCoordinate, Integer>() {
                @Override
                public void getLinkedNodeKeys(DirectionalCoordinate fromNodeKey, Consumer<DirectionalCoordinate> linkedNodeKeyConsumer) {
                    Coordinate coordinate = fromNodeKey.getCoordinate();
                    Direction direction = fromNodeKey.getDirection();
                    int newCoordinateY = coordinate.getY() + direction.getYDelta();
                    int newCoordinateX = coordinate.getX() + direction.getXDelta();
                    if (newCoordinateY >= 0 && newCoordinateY < gridHeight && newCoordinateX >= 0 && newCoordinateX < gridWidth && grid[newCoordinateY][newCoordinateX] == '.') {
                        linkedNodeKeyConsumer.accept(new DirectionalCoordinate(new Coordinate(newCoordinateY, newCoordinateX), direction));
                    }
                    int directionOrdinal = direction.ordinal();
                    linkedNodeKeyConsumer.accept(new DirectionalCoordinate(coordinate, previousDirection[directionOrdinal]));
                    linkedNodeKeyConsumer.accept(new DirectionalCoordinate(coordinate, nextDirection[directionOrdinal]));
                }

                @Override
                public Integer getCostOfMovingBetweenLinkedNodes(DirectionalCoordinate linkedFromNodeKey, DirectionalCoordinate linkedToNodeKey) {
                    return linkedFromNodeKey.getDirection() == linkedToNodeKey.getDirection() ? 1 : 1000;
                }

                @Override
                public Integer getCostEstimateOfMovingBetweenNodes(DirectionalCoordinate fromNodeKey, DirectionalCoordinate toNodeKey) {
                    return 0;
                }

                @Override
                public boolean isValidEndingNode(DirectionalCoordinate nodeKey) {
                    return nodeKey.getCoordinate().equals(end);
                }
            },
            AStarSolver.CostAdapter.CommonTypes.Of.Integer.INSTANCE,
            new AStarSolver.ThrowingResultAdapter<>(new AStarSolver.CostIncludingResultAdapter<>(new AStarSolver.LinkagesRouteAdapter<>(DirectionalCoordinate::getCoordinate))),
            new DirectionalCoordinate(start, Direction.EAST),
            new DirectionalCoordinate(end, null)
        );
        Map<Coordinate,Set<Coordinate>> linkedNodes = result.getResult();
        for (Map.Entry<Coordinate,Set<Coordinate>> linkedNodesEntry : linkedNodes.entrySet()) {
            char character;
            if (linkedNodesEntry.getValue().size() > 2) {
                character = 'O';
            }
            else {
                character = 'o';
            }
            Coordinate coordinate = linkedNodesEntry.getKey();
            grid[coordinate.getY()][coordinate.getX()] = character;
        }
        for (int y=0; y<gridHeight; y++) {
            printWriter.println(grid[y]);
        }
        return new BasicPuzzleResults<>(
            result.getCost(),
            linkedNodes.size()
        );
    }
}
