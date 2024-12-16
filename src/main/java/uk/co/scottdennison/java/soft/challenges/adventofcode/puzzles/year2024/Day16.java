package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2024;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.common.AStarSolver;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    private static final class DijkstraNode {
        private final DirectionalCoordinate directionalCoordinate;
        private Set<DijkstraNode> cameFrom;
        private int score;

        private DijkstraNode(DirectionalCoordinate directionalCoordinate, DijkstraNode cameFrom, int score) {
            this.directionalCoordinate = directionalCoordinate;
            if (cameFrom == null) {
                this.cameFrom = new HashSet<>();
            }
            else {
                this.cameFrom = new HashSet<>(Collections.singleton(cameFrom));
            }
            this.score = score;
        }

        public DirectionalCoordinate getDirectionalCoordinate() {
            return this.directionalCoordinate;
        }

        public Set<DijkstraNode> getCameFrom() {
            return Collections.unmodifiableSet(this.cameFrom);
        }

        public int getScore() {
            return this.score;
        }

        public boolean update(DijkstraNode cameFrom, int score) {
            if (score < this.score) {
                this.cameFrom.clear();
                this.cameFrom.add(cameFrom);
                this.score = score;
                return true;
            }
            if (score == this.score) {
                this.cameFrom.add(cameFrom);
                return false;
            }
            return false;
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

    private static void handleNeighbour(Map<DirectionalCoordinate, DijkstraNode> knownNodes, PriorityQueue<DijkstraNode> openQueue, DijkstraNode currentNode, DirectionalCoordinate neighbouringDirectionalCoordinate, int score) {
        DijkstraNode neighbouringNode = knownNodes.get(neighbouringDirectionalCoordinate);
        if (neighbouringNode == null) {
            neighbouringNode = new DijkstraNode(neighbouringDirectionalCoordinate, currentNode, score);
            knownNodes.put(neighbouringDirectionalCoordinate, neighbouringNode);
            openQueue.add(neighbouringNode);
        }
        else if (neighbouringNode.update(currentNode, score)) {
            openQueue.remove(neighbouringNode);
            openQueue.add(neighbouringNode);
        }
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
        // Alas, we can't use AStarSolver (with a 0 heuristic) as we need to know all possible paths with the lowest score, not just the first.
        PriorityQueue<DijkstraNode> openQueue = new PriorityQueue<>(Comparator.comparing(DijkstraNode::getScore));
        DirectionalCoordinate fromNodeKey = new DirectionalCoordinate(start,Direction.EAST);
        DijkstraNode fromNode = new DijkstraNode(fromNodeKey,null,0);
        Map<DirectionalCoordinate, DijkstraNode> knownNodes = new HashMap<>();
        knownNodes.put(fromNodeKey, fromNode);
        openQueue.add(fromNode);
        DijkstraNode currentNode;
        while ((currentNode = openQueue.poll()) != null) {
            DirectionalCoordinate currentDirectionalCoordinate = currentNode.getDirectionalCoordinate();
            if (currentDirectionalCoordinate.getCoordinate().equals(end)) {
                break;
            }
            int currentScore = currentNode.getScore();
            Coordinate coordinate = currentDirectionalCoordinate.getCoordinate();
            Direction direction = currentDirectionalCoordinate.getDirection();
            int newCoordinateY = coordinate.getY() + direction.getYDelta();
            int newCoordinateX = coordinate.getX() + direction.getXDelta();
            if (newCoordinateY >= 0 && newCoordinateY < gridHeight && newCoordinateX >= 0 && newCoordinateX < gridWidth && grid[newCoordinateY][newCoordinateX] == '.') {
                handleNeighbour(knownNodes, openQueue, currentNode, new DirectionalCoordinate(new Coordinate(newCoordinateY, newCoordinateX), direction), currentScore + 1);
            }
            int directionOrdinal = direction.ordinal();
            handleNeighbour(knownNodes, openQueue, currentNode, new DirectionalCoordinate(coordinate, previousDirection[directionOrdinal]), currentScore + 1000);
            handleNeighbour(knownNodes, openQueue, currentNode, new DirectionalCoordinate(coordinate, nextDirection[directionOrdinal]), currentScore + 1000);
        }
        if (currentNode == null) {
            throw new IllegalStateException("Could not solve");
        }
        Deque<DijkstraNode> nodesToFollow = new LinkedList<>();
        nodesToFollow.addFirst(currentNode);
        int bestScore = currentNode.getScore();
        Map<Coordinate,Set<Coordinate>> linkedNodes = new HashMap<>();
        while ((currentNode = nodesToFollow.poll()) != null) {
            Coordinate currentCoordinate = currentNode.getDirectionalCoordinate().getCoordinate();
            Set<DijkstraNode> cameFrom = currentNode.getCameFrom();
            for (DijkstraNode cameFromNode : cameFrom) {
                Coordinate cameFromCoordinate = cameFromNode.getDirectionalCoordinate().getCoordinate();
                if (!currentCoordinate.equals(cameFromCoordinate)) {
                    linkedNodes.computeIfAbsent(currentCoordinate, __ -> new HashSet<>()).add(cameFromCoordinate);
                    linkedNodes.computeIfAbsent(cameFromCoordinate, __ -> new HashSet<>()).add(currentCoordinate);
                }
                nodesToFollow.addLast(cameFromNode);
            }
        }
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
            bestScore,
            linkedNodes.size()
        );
    }
}
