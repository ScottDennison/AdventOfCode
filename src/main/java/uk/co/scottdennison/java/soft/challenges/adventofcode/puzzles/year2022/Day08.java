package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2022;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;

public class Day08 implements IPuzzle {
    private static enum Direction {
        UP (0,-1),
        LEFT (-1, 0),
        DOWN (0, 1),
        RIGHT (1, 0);

        private final int xDelta;
        private final int yDelta;

        Direction(int xDelta, int yDelta) {
            this.xDelta = xDelta;
            this.yDelta = yDelta;
        }

        public int getXDelta() {
            return this.xDelta;
        }

        public int getYDelta() {
            return this.yDelta;
        }
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        char[][] inputCharactersGrid = LineReader.charArraysArray(inputCharacters,true);
        int height = inputCharactersGrid.length;
        int width = inputCharactersGrid[0].length;
        for (int y=1; y<height; y++) {
            if (inputCharactersGrid[y].length != width) {
                throw new IllegalStateException("Grid is not square");
            }
        }
        int visibleTreesFromOutsideCount = 0;
        int bestTreeScore = Integer.MIN_VALUE;
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                int thisTreeScore = 1;
                boolean treeVisibleFromOutside = false;
                for (Direction direction : Direction.values()) {
                    char startHeight = inputCharactersGrid[y][x];
                    int checkX = x;
                    int checkY = y;
                    int xDelta = direction.getXDelta();
                    int yDelta = direction.getYDelta();
                    int thisDirectionScore = 0;
                    while (true) {
                        checkX += xDelta;
                        checkY += yDelta;
                        if (checkX < 0 || checkY < 0 || checkX >= width || checkY >= height) {
                            treeVisibleFromOutside = true;
                            break;
                        }
                        thisDirectionScore++;
                        if (inputCharactersGrid[checkY][checkX] >= startHeight) {
                            break;
                        }
                    }
                    thisTreeScore *= thisDirectionScore;
                }
                if (treeVisibleFromOutside) {
                    visibleTreesFromOutsideCount++;
                }
                bestTreeScore = Math.max(bestTreeScore, thisTreeScore);
            }
        }
        return new BasicPuzzleResults<>(
            visibleTreesFromOutsideCount,
            bestTreeScore
        );
    }
}
