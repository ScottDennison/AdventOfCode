package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2025;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;

public class Day04 implements IPuzzle {
    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        char[][] roomGrid = LineReader.charArraysArray(inputCharacters, true);
        int width = roomGrid[0].length;
        int height = roomGrid.length;
        int removeablePaperRollCountAllRounds = 0;
        int removeablePaperRollCountFirstRound = 0;
        int removeablePaperRollCountThisRound;
        boolean isFirstRound = true;
        do {
            removeablePaperRollCountThisRound = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (roomGrid[y][x] == '@') {
                        int neighbouringPaperRolls = 0;
                        for (int yDelta = -1; yDelta <= 1; yDelta++) {
                            int neighbourY = y + yDelta;
                            if (neighbourY >= 0 && neighbourY < height) {
                                for (int xDelta = -1; xDelta <= 1; xDelta++) {
                                    int neighbourX = x + xDelta;
                                    if (neighbourX >= 0 && neighbourX < width && !(yDelta == 0 && xDelta == 0)) {
                                        switch (roomGrid[neighbourY][neighbourX]) {
                                            case '@':
                                            case 'x':
                                                neighbouringPaperRolls++;
                                                break;
                                        }
                                    }
                                }
                            }
                        }
                        if (neighbouringPaperRolls < 4) {
                            roomGrid[y][x] = 'x';
                        }
                    }
                }
            }
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (roomGrid[y][x] == 'x') {
                        roomGrid[y][x] = '.';
                        removeablePaperRollCountThisRound++;
                    }
                }
            }
            removeablePaperRollCountAllRounds += removeablePaperRollCountThisRound;
            if (isFirstRound) {
                removeablePaperRollCountFirstRound = removeablePaperRollCountThisRound;
                isFirstRound = false;
            }
        } while (removeablePaperRollCountThisRound > 0);
        return new BasicPuzzleResults<>(
            removeablePaperRollCountFirstRound,
            removeablePaperRollCountAllRounds
        );
    }
}
