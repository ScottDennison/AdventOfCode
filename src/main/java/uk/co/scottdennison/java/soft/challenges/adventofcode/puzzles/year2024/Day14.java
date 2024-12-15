package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2024;

import uk.co.scottdennison.java.libs.math.ExtendedEuclideanAlgorithm;
import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day14 implements IPuzzle {
    private static final Pattern PATTERN_CONFIG_DIMENSIONS = Pattern.compile("\\Awidth: (?<width>[0-9]+)\r?\n?height: (?<height>[0-9]+)\\Z");
    private static final Pattern PATTERN_INPUT_LINE = Pattern.compile("^p=(?<positionX>-?[0-9]+),(?<positionY>-?[0-9]+) v=(?<velocityX>-?[0-9]+),(?<velocityY>-?[0-9]+)$");

    private static class Robot {
        private int x;
        private int y;
        private final int velocityX;
        private final int velocityY;

        public Robot(int x, int y, int velocityX, int velocityY) {
            this.x = x;
            this.y = y;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
        }

        public int getX() {
            return this.x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return this.y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getVelocityX() {
            return this.velocityX;
        }

        public int getVelocityY() {
            return this.velocityY;
        }

        public Robot copy() {
            return new Robot(x, y, velocityX, velocityY);
        }
    }

    private static int solvePartA(Robot[] robotDefinitions, int gridWidth, int gridHeight) {
        if (gridWidth % 2 != 1 || gridHeight % 2 != 1) {
            throw new IllegalStateException("Invalid grid dimensions");
        }
        int xSegmentDivider = gridWidth / 2;
        int ySegmentDivider = gridHeight / 2;
        int[] segmentRobotCounts = new int[4];
        for (Robot robot : robotDefinitions) {
            int x = Math.floorMod(robot.getX() + (100 * robot.getVelocityX()), gridWidth);
            int y = Math.floorMod(robot.getY() + (100 * robot.getVelocityY()), gridHeight);
            if (x == xSegmentDivider || y == ySegmentDivider) {
                continue;
            }
            segmentRobotCounts[((y < ySegmentDivider ? 0 : 1) << 1) | (x < xSegmentDivider ? 0 : 1)]++;
        }
        int result = 1;
        for (int segmentIndex=0; segmentIndex<4; segmentIndex++) {
            result *= segmentRobotCounts[segmentIndex];
        }
        return result;
    }

    private static int solvePartB(Robot[] robotDefinitions, int gridWidth, int gridHeight) {
        int robotCount = robotDefinitions.length;
        Robot[] robots = new Robot[robotCount];
        for (int robotIndex=0; robotIndex<robotCount; robotIndex++) {
            robots[robotIndex] = robotDefinitions[robotIndex].copy();
        }
        int possibleSteps = (gridWidth * gridHeight) / (int)ExtendedEuclideanAlgorithm.solveForGcdOnly(gridWidth, gridHeight);
        int[][] cellsOccupied = new int[gridHeight][gridWidth];
        for (Robot robot : robots) {
            cellsOccupied[robot.getY()][robot.getX()]++;
        }
        int gridHeightMinus2 = gridHeight-2;
        int gridWidthMinus2 = gridWidth-2;
        int maxCellsWithForNeighbours = Integer.MIN_VALUE;
        int stepWithMaxCellsWithForNeighbours = -1;
        for (int step=0; step<possibleSteps; step++) {
            int cellsWithFourNeighbours = 0;
            for (int y=1; y<=gridHeightMinus2; y++) {
                for (int x=1; x<gridWidthMinus2; x++) {
                    if (cellsOccupied[y-1][x] > 0 && cellsOccupied[y][x-1] > 0 && cellsOccupied[y][x+1] > 0 && cellsOccupied[y+1][x] > 0) {
                        cellsWithFourNeighbours++;
                    }
                }
            }
            if (cellsWithFourNeighbours > maxCellsWithForNeighbours) {
                maxCellsWithForNeighbours = cellsWithFourNeighbours;
                stepWithMaxCellsWithForNeighbours = step;
            }
            for (Robot robot : robots) {
                int x = robot.getX();
                int y = robot.getY();
                cellsOccupied[y][x]--;
                x = (x + robot.getVelocityX() + gridWidth) % gridWidth;
                y = (y + robot.getVelocityY() + gridHeight) % gridHeight;
                robot.setX(x);
                robot.setY(y);
                cellsOccupied[y][x]++;
            }
        }
        return stepWithMaxCellsWithForNeighbours;
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        Matcher configMatcher = PATTERN_CONFIG_DIMENSIONS.matcher(new String(configProvider.getPuzzleConfigChars("grid_dimensions")).trim());
        if (!configMatcher.matches()) {
            throw new IllegalStateException("Could not parse config");
        }
        int gridWidth = Integer.parseInt(configMatcher.group("width"));
        int gridHeight = Integer.parseInt(configMatcher.group("height"));
        String[] inputLines = LineReader.stringsArray(inputCharacters, true);
        int robotCount = inputLines.length;
        Robot[] robotDefinitions = new Robot[robotCount];
        for (int robotIndex=0; robotIndex<robotCount; robotIndex++) {
            Matcher inputLineMatcher = PATTERN_INPUT_LINE.matcher(inputLines[robotIndex]);
            if (!inputLineMatcher.matches()) {
                throw new IllegalStateException("Could not parse input line");
            }
            robotDefinitions[robotIndex] = new Robot(Integer.parseInt(inputLineMatcher.group("positionX")), Integer.parseInt(inputLineMatcher.group("positionY")), Integer.parseInt(inputLineMatcher.group("velocityX")), Integer.parseInt(inputLineMatcher.group("velocityY")));
        }
        return new BasicPuzzleResults<>(
            solvePartA(robotDefinitions, gridWidth, gridHeight),
            partBPotentiallyUnsolvable ? null : solvePartB(robotDefinitions, gridWidth, gridHeight)
        );
    }
}
