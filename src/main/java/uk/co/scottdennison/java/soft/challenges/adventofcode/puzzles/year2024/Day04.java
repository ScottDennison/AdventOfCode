package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2024;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;

public class Day04 implements IPuzzle {
    private static final char[] XMAS_CHARACTERS = {'X','M','A','S'};

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        char[][] inputGrid = LineReader.charArraysArray(inputCharacters, true);
        int height = inputGrid.length;
        int width = inputGrid[0].length;
        int partAMatchCount = 0;
        for (int startY=0; startY<height; startY++) {
            for (int startX=0; startX<width; startX++) {
                for (int yDelta=-1; yDelta<=1; yDelta++) {
                    for (int xDelta=-1; xDelta<=1; xDelta++) {
                        if (!(yDelta == 0 && xDelta == 0)) {
                            boolean matches = true;
                            int y = startY;
                            int x = startX;
                            for (int xmasCharacterIndex=0; xmasCharacterIndex<4; xmasCharacterIndex++, x+=xDelta, y+=yDelta) {
                                if (x < 0 || x >= width || y < 0 || y >= height || inputGrid[y][x] != XMAS_CHARACTERS[xmasCharacterIndex]) {
                                    matches = false;
                                    break;
                                }
                            }
                            if (matches) {
                                partAMatchCount++;
                            }
                        }
                    }
                }
            }
        }
        int widthMinusOne = width-1;
        int heightMinusOne = height-1;
        int partBMatchCount = 0;
        for (int centerY=1; centerY<heightMinusOne; centerY++) {
            for (int centerX=1; centerX<widthMinusOne; centerX++) {
                if (inputGrid[centerY][centerX] == 'A') {
                    char topLeft = inputGrid[centerY-1][centerX-1];
                    char topRight = inputGrid[centerY-1][centerX+1];
                    char bottomLeft = inputGrid[centerY+1][centerX-1];
                    char bottomRight = inputGrid[centerY+1][centerX+1];
                    if (((topLeft == 'M' && bottomRight == 'S') || (topLeft == 'S' && bottomRight == 'M')) && ((topRight == 'M' && bottomLeft == 'S') || (topRight == 'S' && bottomLeft == 'M'))) {
                        partBMatchCount++;
                    }
                }
            }
        }
        return new BasicPuzzleResults<>(
            partAMatchCount,
            partBMatchCount
        );
    }
}
