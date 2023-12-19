package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2023;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class Day17 implements IPuzzle {
    private static enum Direction {
        NORTH(-1,0),
        EAST(0,1),
        SOUTH(1, 0),
        WEST(0,-1);

        private Direction oppositeDirection;

        static {
            NORTH.oppositeDirection = SOUTH;
            SOUTH.oppositeDirection = NORTH;
            EAST.oppositeDirection = WEST;
            WEST.oppositeDirection = EAST;
        }

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

        public Direction getOppositeDirection() {
            return this.oppositeDirection;
        }
    }

    private static class NodeKey {
        private final int y;
        private final int x;
        private final Direction direction;
        private final int straightLineDistance;

        public NodeKey(int y, int x, Direction direction, int straightLineDistance) {
            this.y = y;
            this.x = x;
            this.direction = direction;
            this.straightLineDistance = straightLineDistance;
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

        public int getStraightLineDistance() {
            return this.straightLineDistance;
        }

        @Override
        public boolean equals(Object otherObject) {
            if (this == otherObject) return true;
            if (otherObject == null || this.getClass() != otherObject.getClass()) return false;

            NodeKey otherNodeKey = (NodeKey) otherObject;

            if (this.y != otherNodeKey.y) return false;
            if (this.x != otherNodeKey.x) return false;
            if (this.straightLineDistance != otherNodeKey.straightLineDistance) return false;
            if (this.direction != otherNodeKey.direction) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = this.y;
            result = 31 * result + this.x;
            result = 31 * result + (this.direction != null ? this.direction.hashCode() : 0);
            result = 31 * result + this.straightLineDistance;
            return result;
        }

        @Override
        public String toString() {
            return "NodeKey{" +
                "y=" + this.y + ", " +
                "x=" + this.x + ", " +
                "direction=" + this.direction + ", " +
                "straightLineDistance=" + this.straightLineDistance + "}";
        }
    }

    public static class Node {
        private final NodeKey nodeKey;
        private Node cameFrom;
        private int fScore = Integer.MAX_VALUE;
        private int gScore = Integer.MAX_VALUE;

        private Node(NodeKey nodeKey) {
            this.nodeKey = nodeKey;
        }

        public NodeKey getNodeKey() {
            return this.nodeKey;
        }

        private Node getCameFrom() {
            return this.cameFrom;
        }

        private void setCameFrom(Node cameFrom) {
            this.cameFrom = cameFrom;
        }

        private int getFScore() {
            return this.fScore;
        }

        private void setFScore(int hScore) {
            this.fScore = hScore;
        }

        private int getGScore() {
            return this.gScore;
        }

        private void setGScore(int gScore) {
            this.gScore = gScore;
        }

        @Override
        public boolean equals(Object otherObject) {
            if (this == otherObject) return true;
            if (otherObject == null || this.getClass() != otherObject.getClass()) return false;

            Node otherNode = (Node) otherObject;

            if (this.nodeKey != null ? !this.nodeKey.equals(otherNode.nodeKey) : otherNode.nodeKey != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            return this.nodeKey != null ? this.nodeKey.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "Node{" +
                "nodeKey=" + this.nodeKey + ", " +
                "cameFrom=" + this.cameFrom + ", " +
                "fScore=" + this.fScore + ", " +
                "gScore=" + this.gScore + "}";
        }
    }

    private static int heuristic(int fromY, int fromX, int toY, int toX) {
        return Math.abs(toY-fromY)+Math.abs(toX-fromX);
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        char[][] costCharGrid = LineReader.charArraysArray(inputCharacters, true);
        int height = costCharGrid.length;
        int width = costCharGrid[0].length;
        int[][] costsGrid = new int[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                costsGrid[y][x] += costCharGrid[y][x] - '0';
            }
        }
        return new BasicPuzzleResults<>(
            solve(costsGrid, height, width, 1, 3),
            solve(costsGrid, height, width, 4, 10)
        );
    }

    private static int solve(int[][] costsGrid, int height, int width, int minimumTurnOrStopStraightLineDistance, int maximumStraightLineDistance) {
        int targetY = height - 1;
        int targetX = width - 1;
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparing(Node::getFScore));
        NodeKey startNodeKey = new NodeKey(0, 0, null, 0);
        Node startNode = new Node(startNodeKey);
        startNode.setGScore(0);
        startNode.setFScore(heuristic(0, 0, targetY, targetX));
        Map<NodeKey, Node> knownNodesByKey = new HashMap<>();
        knownNodesByKey.put(startNodeKey, startNode);
        openSet.add(startNode);
        while (true) {
            Node currentNode = openSet.poll();
            if (currentNode == null) {
                throw new IllegalStateException("No path found");
            }
            NodeKey currentNodeKey = currentNode.getNodeKey();
            int currentY = currentNodeKey.getY();
            int currentX = currentNodeKey.getX();
            int currentStraightLineDistance = currentNodeKey.getStraightLineDistance();
            int currentGScore = currentNode.getGScore();
            if (currentY == targetY && currentX == targetX && currentStraightLineDistance >= minimumTurnOrStopStraightLineDistance) {
                return currentGScore;
            }
            Direction currentDirection = currentNodeKey.getDirection();
            Direction oppositeDirection = currentDirection == null ? null : currentDirection.getOppositeDirection();
            for (Direction travelDirection : Direction.values()) {
                if (travelDirection == oppositeDirection) {
                    continue;
                }
                int neighbourY = currentY + travelDirection.getYDelta();
                if (neighbourY < 0 || neighbourY >= height) {
                    continue;
                }
                int neighbourX = currentX + travelDirection.getXDelta();
                if (neighbourX < 0 || neighbourX >= width) {
                    continue;
                }
                int neighbourStraightLineDistance;
                if (travelDirection == currentDirection) {
                    neighbourStraightLineDistance = currentStraightLineDistance+1;
                    if (neighbourStraightLineDistance > maximumStraightLineDistance) {
                        continue;
                    }
                }
                else {
                    if (currentDirection != null && currentStraightLineDistance < minimumTurnOrStopStraightLineDistance) {
                        continue;
                    }
                    neighbourStraightLineDistance = 1;
                }
                NodeKey neighbourNodeKey = new NodeKey(neighbourY, neighbourX, travelDirection, neighbourStraightLineDistance);
                Node neighbourNode = knownNodesByKey.get(neighbourNodeKey);
                boolean newPoint = false;
                if (neighbourNode == null) {
                    newPoint = true;
                    neighbourNode = new Node(neighbourNodeKey);
                    knownNodesByKey.put(neighbourNodeKey, neighbourNode);
                }
                int potentialGScore = currentGScore + costsGrid[neighbourY][neighbourX];
                if (potentialGScore < neighbourNode.getGScore()) {
                    if (!newPoint) {
                        openSet.remove(neighbourNode);
                    }
                    neighbourNode.setCameFrom(currentNode);
                    neighbourNode.setGScore(potentialGScore);
                    neighbourNode.setFScore(potentialGScore + heuristic(neighbourY, neighbourX, targetY, targetX));
                    openSet.add(neighbourNode);
                }
            }
        }
    }
}
