package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2025;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Arrays;

public class Day07 implements IPuzzle {
    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        char[][] grid = LineReader.charArraysArray(inputCharacters, true);
        int height = grid.length;
        int width = grid[0].length;
        long[][] timelineCounts = new long[height][width];
        char[] currentGridRow = grid[0];
        char[] previousGridRow;
        long[] currentTimelineCountsRow = timelineCounts[0];
        long[] previousTimelineCountsRow;
        int splitCount = 0;
        for (int y=1; y<height; y++) {
            previousGridRow = currentGridRow;
            currentGridRow = grid[y];
            previousTimelineCountsRow = currentTimelineCountsRow;
            currentTimelineCountsRow = timelineCounts[y];
            for (int x=0; x<width; x++) {
                switch (previousGridRow[x]) {
                    case 'S':
                        previousTimelineCountsRow[x] = 1;
                    case '|':
                        long previousTimelineCount = previousTimelineCountsRow[x];
                        if (currentGridRow[x] == '^') {
                            splitCount++;
                            currentGridRow[x - 1] = '|';
                            currentGridRow[x + 1] = '|';
                            currentTimelineCountsRow[x - 1] = Math.addExact(currentTimelineCountsRow[x - 1], previousTimelineCount);
                            currentTimelineCountsRow[x + 1] = Math.addExact(currentTimelineCountsRow[x + 1], previousTimelineCount);
                        } else {
                            currentGridRow[x] = '|';
                            currentTimelineCountsRow[x] = Math.addExact(currentTimelineCountsRow[x], previousTimelineCount);
                        }
                }
            }
        }
        return new BasicPuzzleResults<>(
            splitCount,
            Arrays.stream(currentTimelineCountsRow).reduce(0L, Math::addExact)
        );
    }
}
