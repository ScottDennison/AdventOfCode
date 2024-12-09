package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2016;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.math.BigInteger;

public class Day18 implements IPuzzle {
    private static final int PART_B_ROWS = 400000;

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        inputCharacters = new String(inputCharacters).trim().toCharArray();
        BigInteger traps = BigInteger.ZERO;
        BigInteger mask = BigInteger.ZERO;
        int columns = inputCharacters.length;
        int lastColumn = columns-1;
        for (int x=0; x<columns; x++) {
            traps = traps.shiftLeft(1);
            mask = mask.shiftLeft(1).or(BigInteger.ONE);
            switch (inputCharacters[x]) {
                case '^':
                    traps = traps.or(BigInteger.ONE);
                    break;
                case '.':
                    break;
                default:
                    throw new IllegalStateException("Unexpected character");
            }
        }
        return new BasicPuzzleResults<>(
            solve(traps, mask, columns, Integer.parseInt(new String(configProvider.getPuzzleConfigChars("part_a_rows")))),
            solve(traps, mask, columns, PART_B_ROWS)
        );
    }

    private static int solve(BigInteger traps, BigInteger mask, int columns, int rows) {
        int safe = 0;
        for (int row=0; row<rows; row++) {
            safe += columns-traps.bitCount();
            traps = traps.shiftLeft(1).xor(traps.shiftRight(1)).and(mask);
        }
        return safe;
    }
}
