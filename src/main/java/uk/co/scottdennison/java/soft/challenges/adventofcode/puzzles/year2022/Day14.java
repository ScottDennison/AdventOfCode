package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2022;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Day14 implements IPuzzle {
    private static final class Point {
        private final int x;
        private final int y;

        private Point(int x, int y) {
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

    private static final boolean SHOW_STATE = false;
    private static final Point START_POINT = new Point(500,0);
    private static final int FLOOR_GAP = 2;
    private static final int[] X_MOVEMENTS = {0,-1,1};
    private static final Pattern PATTERN_POINT_SPLIT = Pattern.compile("->");

    private static char[][] copyGrid(char[][] currentGrid, int boundsX, int boundsY, int additionalWidthPerSide) {
        int currentSizeX = boundsX+1;
        int newSizeX = currentSizeX+additionalWidthPerSide+additionalWidthPerSide;
        char[][] newGrid = new char[boundsY+1][newSizeX];
        for (int y=0; y<=boundsY; y++) {
            System.arraycopy(currentGrid[y],0,newGrid[y],additionalWidthPerSide,currentSizeX);
        }
        return newGrid;
    }

    private static int simulate(PrintWriter printWriter, String part, char[][] grid, int boundsX, int boundsY, int startX, int startY, boolean endlessVoid) {
        if (grid[startY][startX] != '+') {
            throw new IllegalStateException("Sand start position is blocked");
        }
        grid = copyGrid(grid, boundsX, boundsY, 0);
        int sandAtRest = 0;
        grainsLoop: while (true) {
            int x = startX;
            int y = startY;
            grainLoop: while (true) {
                int nextY = y+1;
                if (nextY > boundsY) {
                    if (endlessVoid) {
                        break grainsLoop;
                    }
                } else {
                    for (int movementX : X_MOVEMENTS) {
                        int nextX = x + movementX;
                        if (nextX < 0 || nextX > boundsX) {
                            grid = copyGrid(grid, boundsX, boundsY, boundsX);
                            x += boundsX;
                            nextX += boundsX;
                            startX += boundsX;
                            boundsX *= 3;
                        }
                        switch (grid[nextY][nextX]) {
                            case '#':
                            case 'o':
                                break;
                            default:
                                x = nextX;
                                y = nextY;
                                continue grainLoop;
                        }
                    }
                }
                grid[y][x] = 'o';
                sandAtRest++;
                break grainLoop;
            }
            if (grid[startY][startX] == 'o') {
                break grainsLoop;
            }
        }
        if (SHOW_STATE) {
            printWriter.println("Part " + part + " Final State");
            for (int y = 0; y <= boundsY; y++) {
                for (int x = 0; x <= boundsX; x++) {
                    char chr = grid[y][x];
                    if (chr == 0) {
                        chr = '.';
                    }
                    printWriter.print(chr);
                }
                printWriter.println();
            }
        }
        return sandAtRest;
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        List<List<Point>> pointLists = new ArrayList<>();
        int startX = START_POINT.getX();
        int startY = START_POINT.getY();
        int minX = startX;
        int minY = startY;
        int maxX = startX;
        int maxY = startY;
        for (String inputLine : LineReader.strings(inputCharacters)) {
            String[] inputPoints = PATTERN_POINT_SPLIT.split(inputLine);
            List<Point> pointList = new ArrayList<>();
            for (String inputPoint : inputPoints) {
                String[] inputPointParts = inputPoint.split(",");
                if (inputPointParts.length != 2) {
                    throw new IllegalStateException("Unexpected input.");
                }
                int x = Integer.parseInt(inputPointParts[0].trim());
                int y = Integer.parseInt(inputPointParts[1].trim());
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
                pointList.add(new Point(x,y));
            }
            pointLists.add(pointList);
        }
        maxY += FLOOR_GAP-1;
        startX -= minX;
        startY -= minY;
        int boundsX = maxX-minX;
        int boundsY = maxY-minY;
        char[][] grid = new char[boundsY+1][boundsX+1];
        for (List<Point> pointList : pointLists) {
            Point fromPoint = null;
            for (Point toPoint : pointList) {
                if (fromPoint != null) {
                    int fromX = fromPoint.getX() - minX;
                    int fromY = fromPoint.getY() - minY;
                    int toX = toPoint.getX() - minX;
                    int toY = toPoint.getY() - minY;
                    int distanceX = toX - fromX;
                    int distanceY = toY - fromY;
                    int steps = Math.max(Math.abs(distanceX),Math.abs(distanceY));
                    int stepX = distanceX/steps;
                    int stepY = distanceY/steps;
                    if ((fromX + (steps*stepX)) != toX || (fromY + (steps*stepY)) != toY) {
                        throw new IllegalStateException("Rock formation part is not an orthagonal direction");
                    }
                    for (int step=0, x=fromX, y=fromY; step<=steps; step++, x+=stepX, y+=stepY) {
                        grid[y][x] = '#';
                    }
                }
                fromPoint = toPoint;
            }
        }
        grid[startY][startX] = '+';
        return new BasicPuzzleResults<>(
            simulate(printWriter,"A",grid, boundsX, boundsY, startX, startY, true),
            simulate(printWriter,"B",grid, boundsX, boundsY, startX, startY, false)
        );
    }
}
