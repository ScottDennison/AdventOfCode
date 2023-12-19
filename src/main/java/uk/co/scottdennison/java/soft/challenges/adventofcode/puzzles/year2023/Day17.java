package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2023;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.common.AStar;
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
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.function.Consumer;

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
        Optional<AStar.ResultingRoute<NodeKey>> optionalResultingRoute = AStar.run(
            new AStar.NodeAdapter<NodeKey>() {
                @Override
                public void getLinkedNodeKeys(NodeKey fromNodeKey, Consumer<NodeKey> linkedNodeKeyConsumer) {
                    int fromY = fromNodeKey.getY();
                    int fromX = fromNodeKey.getX();
                    int fromStraightLineDistance = fromNodeKey.getStraightLineDistance();
                    Direction fromDirection = fromNodeKey.getDirection();
                    Direction oppositeDirection = fromDirection == null ? null : fromDirection.getOppositeDirection();
                    for (Direction travelDirection : Direction.values()) {
                        if (travelDirection == oppositeDirection) {
                            continue;
                        }
                        int neighbourY = fromY + travelDirection.getYDelta();
                        if (neighbourY < 0 || neighbourY >= height) {
                            continue;
                        }
                        int neighbourX = fromX + travelDirection.getXDelta();
                        if (neighbourX < 0 || neighbourX >= width) {
                            continue;
                        }
                        int neighbourStraightLineDistance;
                        if (travelDirection == fromDirection) {
                            neighbourStraightLineDistance = fromStraightLineDistance + 1;
                            if (neighbourStraightLineDistance > maximumStraightLineDistance) {
                                continue;
                            }
                        } else {
                            if (fromDirection != null && fromStraightLineDistance < minimumTurnOrStopStraightLineDistance) {
                                continue;
                            }
                            neighbourStraightLineDistance = 1;
                        }
                        linkedNodeKeyConsumer.accept(new NodeKey(neighbourY, neighbourX, travelDirection, neighbourStraightLineDistance));
                    }
                }

                @Override
                public int getCostOfMovingBetweenLinkedNodes(NodeKey linkedFromNodeKey, NodeKey linkedToNodeKey) {
                    return costsGrid[linkedToNodeKey.getY()][linkedToNodeKey.getX()];
                }

                @Override
                public int getCostEstimateOfMovingBetweenNodes(NodeKey fromNodeKey, NodeKey toNodeKey) {
                    return Math.abs(toNodeKey.getY()-fromNodeKey.getY())+Math.abs(toNodeKey.getX()-fromNodeKey.getX());
                }

                @Override
                public boolean isValidEndingNode(NodeKey nodeKey) {
                    return nodeKey.getStraightLineDistance() >= minimumTurnOrStopStraightLineDistance;
                }

                @Override
                public Class<NodeKey> getClazz() {
                    return NodeKey.class;
                }
            },
            new NodeKey(0, 0, null, 0),
            new NodeKey(height-1, width-1, null, 0)
        );
        if (!optionalResultingRoute.isPresent()) {
            throw new IllegalStateException("No route found");
        }
        return optionalResultingRoute.get().getCost();
    }
}
