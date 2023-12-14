package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2023;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class Day14 implements IPuzzle {
    private static final int TARGET = 1000000000;

    private static char[][] copyGrid(char[][] sourceGrid, int height, int width) {
        char[][] destGrid = new char[height][width];
        for (int y=0; y<height; y++) {
            char[] destGridRow = destGrid[y];
            char[] sourceGridRow = sourceGrid[y];
            for (int x=0; x<width; x++) {
                destGridRow[x] = sourceGridRow[x];
            }
        }
        return destGrid;
    }

    private static void runMovementNorth(char[][] grid, int height, int width) {
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                if (grid[y][x] == 'O') {
                    int stopY=0;
                    for (int y2=y-1; y2>=0; y2--) {
                        if (grid[y2][x] != '.') {
                            stopY = y2+1;
                            break;
                        }
                    }
                    if (stopY != y) {
                        grid[y][x] = '.';
                        grid[stopY][x] = 'O';
                    }
                }
            }
        }
    }

    private static void runMovementWest(char[][] grid, int height, int width) {
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                if (grid[y][x] == 'O') {
                    int stopX=0;
                    for (int x2=x-1; x2>=0; x2--) {
                        if (grid[y][x2] != '.') {
                            stopX = x2+1;
                            break;
                        }
                    }
                    if (stopX != x) {
                        grid[y][x] = '.';
                        grid[y][stopX] = 'O';
                    }
                }
            }
        }
    }

    private static void runMovementSouth(char[][] grid, int height, int width) {
        int defaultStopY=height-1;
        for (int y=height-1; y>=0; y--) {
            for (int x=0; x<width; x++) {
                if (grid[y][x] == 'O') {
                    int stopY=defaultStopY;
                    for (int y2=y+1; y2<height; y2++) {
                        if (grid[y2][x] != '.') {
                            stopY = y2-1;
                            break;
                        }
                    }
                    if (stopY != y) {
                        grid[y][x] = '.';
                        grid[stopY][x] = 'O';
                    }
                }
            }
        }
    }

    private static void runMovementEast(char[][] grid, int height, int width) {
        int defaultStopX=width-1;
        for (int x=width-1; x>=0; x--) {
            for (int y=0; y<height; y++) {
                if (grid[y][x] == 'O') {
                    int stopX=defaultStopX;
                    for (int x2=x+1; x2<width; x2++) {
                        if (grid[y][x2] != '.') {
                            stopX = x2-1;
                            break;
                        }
                    }
                    if (stopX != x) {
                        grid[y][x] = '.';
                        grid[y][stopX] = 'O';
                    }
                }
            }
        }
    }

    private static void runSpinCycle(char[][] grid, int height, int width) {
        runMovementNorth(grid, height, width);
        runMovementWest(grid, height, width);
        runMovementSouth(grid, height, width);
        runMovementEast(grid, height, width);
    }

    private static int calculateLoad(char[][] grid, int height, int width) {
        int total = 0;
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                if (grid[y][x] == 'O') {
                    total += (height-y);
                }
            }
        }
        return total;
    }

    private static boolean isSame(char[][] sourceGrid, char[][] destGrid, int height, int width) {
        for (int y=0; y<height; y++) {
            char[] destGridRow = destGrid[y];
            char[] sourceGridRow = sourceGrid[y];
            for (int x=0; x<width; x++) {
                if (destGridRow[x] != sourceGridRow[x]) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        char[][] inputGrid = LineReader.charArraysArray(inputCharacters, true);
        int height = inputGrid.length;
        int width = inputGrid[0].length;

        char[][] partAGrid = copyGrid(inputGrid,height,width);
        runMovementNorth(partAGrid,height,width);
        int partALoad = calculateLoad(partAGrid, height, width);

        Map<Integer, IdentityHashMap<char[][],Integer>> seenGridsByLoad = new HashMap<>();
        Map<Integer, char[][]> seenGridsBySpinCycleNumber = new HashMap<>();
        char[][] partBGrid = copyGrid(inputGrid,height,width);
        int spinCycleNumber = 0;
        int partBLoad;
        outerLoop: while (true) {
            spinCycleNumber++;
            runSpinCycle(partBGrid,height,width);
            int load = calculateLoad(partBGrid,height,width);
            if (spinCycleNumber == TARGET) {
                partBLoad = load;
                break;
            }
            IdentityHashMap<char[][],Integer> seenGridsForThisLoad = seenGridsByLoad.get(load);
            if (seenGridsForThisLoad == null) {
                seenGridsForThisLoad = new IdentityHashMap<>();
                seenGridsByLoad.put(load, seenGridsForThisLoad);
            }
            for (char[][] seenGridForThisLoad : seenGridsForThisLoad.keySet()) {
                if (isSame(partBGrid, seenGridForThisLoad, height, width)) {
                    int repetitionCycleLength = spinCycleNumber - seenGridsForThisLoad.get(seenGridForThisLoad);
                    int closeToTargetSpinCycleNumber = (((TARGET-spinCycleNumber)/repetitionCycleLength)*repetitionCycleLength)+spinCycleNumber;
                    if (closeToTargetSpinCycleNumber == TARGET) {
                        partBLoad = load;
                    }
                    else {
                        partBLoad = calculateLoad(seenGridsBySpinCycleNumber.get(spinCycleNumber-(repetitionCycleLength-(TARGET-closeToTargetSpinCycleNumber))),height,width);
                    }
                    break outerLoop;
                }
            }
            char[][] partBGridCopy = copyGrid(partBGrid, height, width);
            seenGridsForThisLoad.put(partBGridCopy, spinCycleNumber);
            seenGridsBySpinCycleNumber.put(spinCycleNumber, partBGridCopy);
        }
        return new BasicPuzzleResults<>(
            partALoad,
            partBLoad
        );
    }
}
