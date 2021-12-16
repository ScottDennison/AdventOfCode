package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2021;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

public class Day15 implements IPuzzle {
    private static class Point {
        private final int x;
        private final int y;
        private final int risk;
        private int score;
        private boolean visited;

        private Point(int x, int y, int risk) {
            this.x = x;
            this.y = y;
            this.risk = risk;
            this.score = Integer.MAX_VALUE;
            this.visited = false;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        public int getRisk() {
            return this.risk;
        }

        public int getScore() {
            return this.score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public boolean isVisited() {
            return this.visited;
        }

        public void markVisited() {
            this.visited = true;
        }
    }

    private static final int TILE_MULTIPLES = 5;

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        char[][] inputCharacters2D = LineReader.charArraysArray(inputCharacters,true);
        int partAHeight = inputCharacters2D.length;
        int partAWidth = inputCharacters2D[0].length;
        int partBHeight = partAHeight*TILE_MULTIPLES;
        int partBWidth = partAWidth*TILE_MULTIPLES;
        int[][] partARisk = new int[partAHeight][partAWidth];
        int[][] partBRisk = new int[partBHeight][partBWidth];
        for (int y=0; y<partAHeight; y++) {
            char[] inputCharacters2DRow = inputCharacters2D[y];
            if (inputCharacters2DRow.length != partAWidth) {
                throw new IllegalStateException("Jagged input");
            }
            for (int x=0; x<partAWidth; x++) {
                char inputCharacter = inputCharacters2DRow[x];
                if (inputCharacter >= '0' && inputCharacter <= '9') {
                    int partAInputValue = inputCharacter-'0';
                    partARisk[y][x] = partAInputValue;
                    for (int tileY=0; tileY<TILE_MULTIPLES; tileY++) {
                        for (int tileX=0; tileX<TILE_MULTIPLES; tileX++) {
                            int partBInputValue = partAInputValue + tileY + tileX;
                            while (partBInputValue > 9) {
                                partBInputValue -= 9; // So it wraps around to 1 not 0
                            }
                            partBRisk[tileY*partAHeight+y][tileX*partAWidth+x] = partBInputValue;
                        }
                    }
                } else {
                    throw new IllegalStateException("Invalid input character");
                }
            }
        }
        return new BasicPuzzleResults<>(
            runDikjstra(partARisk,partAHeight,partAWidth),
            runDikjstra(partBRisk,partBHeight,partBWidth)
        );
    }

    private int runDikjstra(int[][] risk, int height, int width) {
        Point[][] points = new Point[height][width];
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                points[y][x] = new Point(x,y,risk[y][x]);
            }
        }
        points[0][0].setScore(0);
        Queue<Point> priorityQueue = new PriorityQueue<>(Comparator.comparing(Point::getScore));
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                priorityQueue.add(points[y][x]);
            }
        }
        int targetX = width-1;
        int targetY = height-1;
        while (true) {
            Point currentPoint = priorityQueue.remove();
            currentPoint.markVisited();
            int currentX = currentPoint.getX();
            int currentY = currentPoint.getY();
            int currentScore = currentPoint.getScore();
            if (currentX == targetX && currentY == targetY) {
                return currentScore;
            }
            for (int yOffset=-1; yOffset<=1; yOffset+=2) {
                int neighbourY = currentY+yOffset;
                if (neighbourY >= 0 && neighbourY < height) {
                    checkRoute(priorityQueue,currentScore,points[neighbourY][currentX]);
                }
            }
            for (int xOffset=-1; xOffset<=1; xOffset+=2) {
                int neighbourX = currentX+xOffset;
                if (neighbourX >= 0 && neighbourX < width) {
                    checkRoute(priorityQueue,currentScore,points[currentY][neighbourX]);
                }
            }
        }
    }

    private void checkRoute(Queue<Point> priorityQueue, int currentScore, Point neighbourPoint){
        if (!neighbourPoint.isVisited()) {
            int routeScore = currentScore + neighbourPoint.getRisk();
            if (routeScore < neighbourPoint.getScore()) {
                neighbourPoint.setScore(routeScore);
                priorityQueue.remove(neighbourPoint);
                priorityQueue.add(neighbourPoint);
            }
        }
    }
}
