package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2024;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class Day20 implements IPuzzle {
    private static final boolean OUTPUT_DEBUG = false;

    private static enum Direction {
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
    }

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

    private static int[][] findNonCheatPicosecondNumbers(boolean[][] walls, int gridHeight, int gridWidth, int mazeStartY, int mazeStartX, int mazeEndY, int mazeEndX) {
        int[][] nonCheatPicosecondNumbers = new int[gridHeight][gridWidth];
        int picosecondNumber = 0;
        int currentY = mazeStartY;
        int currentX = mazeStartX;
        int previousY = currentY;
        int previousX = currentX;
        Direction[] directions = Direction.values();
        while (true) {
            nonCheatPicosecondNumbers[currentY][currentX] = ++picosecondNumber;
            if (currentY == mazeEndY && currentX == mazeEndX) {
                break;
            }
            int nextY = -1;
            int nextX = -1;
            boolean nextFound = false;
            for (Direction direction : directions) {
                int potentialNextY = currentY + direction.getYDelta();
                int potentialNextX = currentX + direction.getXDelta();
                if (potentialNextY < 0 || potentialNextY >= gridHeight || potentialNextX < 0 || potentialNextX >= gridWidth || walls[potentialNextY][potentialNextX] || (potentialNextY == previousY && potentialNextX == previousX)) {
                    continue;
                }
                if (nextFound) {
                    throw new IllegalStateException("Multiple paths found.");
                }
                nextFound = true;
                nextY = potentialNextY;
                nextX = potentialNextX;
            }
            previousY = currentY;
            previousX = currentX;
            currentY = nextY;
            currentX = nextX;
            if (!nextFound) {
                throw new IllegalStateException("No next paths foun.");
            }
        }
        return nonCheatPicosecondNumbers;
    }

    private static int findCheats(PrintWriter printWriter, IPuzzleConfigProvider configProvider, int[][] nonCheatPicosecondNumbers, int gridHeight, int gridWidth, char part, int cheatMaxSize) {
        int minimumPicosecondSaving = Integer.parseInt(new String(configProvider.getPuzzleConfigChars("part_" + Character.toLowerCase(part) + "_minimum_picosecond_saving")).trim());
        SortedMap<Integer,Integer> picosecondSavingCheatCounts = new TreeMap<>();
        for (int cheatStartY=0; cheatStartY<gridHeight; cheatStartY++) {
            for (int cheatStartX=0; cheatStartX<gridWidth; cheatStartX++) {
                int cheatStartNonCheatPicosecond = nonCheatPicosecondNumbers[cheatStartY][cheatStartX];
                if (cheatStartNonCheatPicosecond < 1) {
                    continue;
                }
                for (int deltaY=-cheatMaxSize; deltaY<=cheatMaxSize; deltaY++) {
                    int cheatEndY = cheatStartY + deltaY;
                    if (cheatEndY < 0 || cheatEndY >= gridHeight) {
                        continue;
                    }
                    int[] nonCheatPicosecondNumbersForCheatEndY = nonCheatPicosecondNumbers[cheatEndY];
                    int absDeltaY = Math.abs(deltaY);
                    int maxAbsDeltaX = cheatMaxSize - absDeltaY;
                    for (int deltaX=-maxAbsDeltaX; deltaX<=maxAbsDeltaX; deltaX++) {
                        int cheatEndX = cheatStartX + deltaX;
                        if (cheatEndX < 0 || cheatEndX >= gridWidth) {
                            continue;
                        }
                        int cheatEndNonCheatPicosecond = nonCheatPicosecondNumbersForCheatEndY[cheatEndX];
                        if (cheatEndNonCheatPicosecond < 1) {
                            continue;
                        }
                        int picosecondSaving = (cheatEndNonCheatPicosecond - cheatStartNonCheatPicosecond) - (absDeltaY + Math.abs(deltaX));
                        if (picosecondSaving < minimumPicosecondSaving) {
                            continue;
                        }
                        picosecondSavingCheatCounts.merge(picosecondSaving, 1, Integer::sum);
                    }
                }
            }
        }
        if (OUTPUT_DEBUG) {
            printWriter.println("Part " + Character.toUpperCase(part));
            }
        int totalMatchingCheatCount = 0;
        for (Map.Entry<Integer,Integer> entry : picosecondSavingCheatCounts.entrySet()) {
            int matchingCheatCount = entry.getValue();
            if (OUTPUT_DEBUG) {
                int picosecondSaving = entry.getKey();
                printWriter.print('\t');
                if (matchingCheatCount == 1) {
                    printWriter.print("There is one cheat that saves");
                }
                else {
                    printWriter.print("There are ");
                    printWriter.print(matchingCheatCount);
                    printWriter.print(" cheats that save");
                }
                printWriter.print(' ');
                printWriter.print(picosecondSaving);
                printWriter.print(" picosecond");
                if (picosecondSaving != 1) {
                    printWriter.print("s");
                }
                printWriter.println('.');
            }
            totalMatchingCheatCount += matchingCheatCount;
        }
        return totalMatchingCheatCount;
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        char[][] grid = LineReader.charArraysArray(inputCharacters, true);
        int gridHeight = grid.length;
        int gridWidth = grid[0].length;
        boolean[][] walls = new boolean[gridHeight][gridWidth];
        Coordinate startCoordinate = findAndReplaceCharacter(grid, gridHeight, gridWidth, 'S');
        Coordinate endCoordinate = findAndReplaceCharacter(grid, gridHeight, gridWidth, 'E');
        for (int y=0; y<gridHeight; y++) {
            for (int x=0; x<gridWidth; x++) {
                boolean wall;
                switch (grid[y][x]) {
                    case '#':
                        wall = true;
                        break;
                    case '.':
                        wall = false;
                        break;
                    default:
                        throw new IllegalStateException("Unexpected character");
                }
                walls[y][x] = wall;
            }
        }
        int[][] nonCheatPicosecondNumbers = findNonCheatPicosecondNumbers(walls, gridHeight, gridWidth, startCoordinate.getY(), startCoordinate.getX(), endCoordinate.getY(), endCoordinate.getX());
        return new BasicPuzzleResults<>(
            findCheats(printWriter, configProvider, nonCheatPicosecondNumbers, gridHeight, gridWidth, 'a', 2),
            findCheats(printWriter, configProvider, nonCheatPicosecondNumbers, gridHeight, gridWidth, 'b', 20)
        );
    }
}
