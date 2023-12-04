package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2016;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

public class Day18 implements IPuzzle {
    private static final int PART_B_ROWS = 400000;

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        inputCharacters = new String(inputCharacters).trim().toCharArray();
        int partARows = Integer.parseInt(new String(configProvider.getPuzzleConfigChars("part_a_rows")));
        int partBRows = PART_B_ROWS;
        if (partARows < 1 || partBRows < 1) {
            throw new IllegalStateException("Cannot calculate less than 1 row.");
        }
        int rows = Math.max(partARows,partBRows);
        int columns = inputCharacters.length;
        int lastColumn = columns-1;
        int partASafeTiles = 0;
        int partBSafeTiles = 0;
        boolean[] gridRow1 = new boolean[columns];
        boolean[] gridRow2 = new boolean[columns];
        boolean gridThisRowIsRow1 = true;
        boolean[] gridThisRow = gridRow1;
        boolean[] gridPreviousRow;
        for (int x=0; x<columns; x++) {
            switch (inputCharacters[x]) {
                case '^':
                    gridThisRow[x] = false;
                    break;
                case '.':
                    gridThisRow[x] = true;
                    partASafeTiles++;
                    partBSafeTiles++;
                    break;
                default:
                    throw new IllegalStateException("Unexpected character");
            }
        }
        for (int y=1; y<rows; y++) {
            if (gridThisRowIsRow1) {
                gridThisRowIsRow1 = false;
                gridPreviousRow = gridRow1;
                gridThisRow = gridRow2;
            }
            else {
                gridThisRowIsRow1 = true;
                gridPreviousRow = gridRow2;
                gridThisRow = gridRow1;
            }
            int safeTilesInThisRow = 0;
            for (int x=0; x<columns; x++) {
                boolean leftSafe = x>0?gridPreviousRow[x-1]:true;
                boolean centerSafe = gridPreviousRow[x];
                boolean rightSafe = x<lastColumn?gridPreviousRow[x+1]:true;
                boolean isSafe = !((!leftSafe && !centerSafe && rightSafe) || (leftSafe && !centerSafe && !rightSafe) || (!leftSafe && centerSafe && rightSafe) || (leftSafe && centerSafe && !rightSafe));
                gridThisRow[x] = isSafe;
                if (isSafe) {
                    safeTilesInThisRow++;
                }
            }
            if (y < partARows) {
                partASafeTiles += safeTilesInThisRow;
            }
            if (y < partBRows) {
                partBSafeTiles += safeTilesInThisRow;
            }
        }
        return new BasicPuzzleResults<>(
            partASafeTiles,
            partBSafeTiles
        );
    }
}
