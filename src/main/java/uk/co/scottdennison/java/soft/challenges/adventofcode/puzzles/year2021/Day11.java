package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2021;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;

public class Day11 implements IPuzzle {
    private static final int SIZE = 10;
    private static final int PART_A_STEPS = 100;
    private static final int FLASH_POINT = 10;

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        char[][] inputLines = LineReader.charArraysArray(inputCharacters, true);
        if (inputLines.length != SIZE) {
            throw new IllegalStateException("Unexpected line count");
        }
        int[][] energy = new int[SIZE][SIZE];
        for (int y=0; y<SIZE; y++) {
            if (inputLines[y].length != SIZE) {
                throw new IllegalStateException("Unexpcted line width");
            }
            for (int x=0; x<SIZE; x++) {
                char inputChar = inputLines[y][x];
                if (inputChar < '0' || inputChar > '9') {
                    throw new IllegalStateException("Invalid character");
                }
                energy[y][x] = inputChar-'0';
            }
        }
        int partAFlashCount = 0;
        int step = 0;
        int simultaneousFlashStep = -1;
        while (step <= PART_A_STEPS || simultaneousFlashStep < 1) {
            step++;
            boolean[][] flashed = new boolean[SIZE][SIZE];
            for (int y=0; y<SIZE; y++) {
                for (int x=0; x<SIZE; x++) {
                    energy[y][x]++;
                }
            }
            for (int y=0; y<SIZE; y++) {
                for (int x=0; x<SIZE; x++) {
                    handlePotentialFlash(energy,flashed,y,x);
                }
            }
            boolean simultaneousFlash = true;
            for (int y=0; y<SIZE; y++) {
                for (int x=0; x<SIZE; x++) {
                    if (flashed[y][x]) {
                        if (step <= PART_A_STEPS) {
                            partAFlashCount++;
                        }
                        energy[y][x] = 0;
                    } else {
                        simultaneousFlash = false;
                    }
                }
            }
            if (simultaneousFlash && simultaneousFlashStep < 1) {
                simultaneousFlashStep = step;
            }
        }
        return new BasicPuzzleResults<>(
            partAFlashCount,
            simultaneousFlashStep
        );
    }

    private void handlePotentialFlash(int[][] energy, boolean[][] flashed, int y, int x) {
        if (energy[y][x] >= FLASH_POINT) {
            if (!flashed[y][x]) {
                flashed[y][x] = true;
                for (int yDelta=-1; yDelta<=1; yDelta++) {
                    int yNeighbour = y+yDelta;
                    if (yNeighbour >= 0 && yNeighbour < SIZE) {
                        for (int xDelta=-1; xDelta<=1; xDelta++) {
                            if (!(yDelta == 0 && xDelta == 0)) {
                                int xNeighbour = x+xDelta;
                                if (xNeighbour >= 0 && xNeighbour < SIZE) {
                                    energy[yNeighbour][xNeighbour]++;
                                    handlePotentialFlash(energy,flashed,yNeighbour,xNeighbour);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
