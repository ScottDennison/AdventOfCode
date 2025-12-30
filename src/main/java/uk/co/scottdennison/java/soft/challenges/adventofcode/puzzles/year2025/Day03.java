package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2025;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;

public class Day03 implements IPuzzle {
    private static long solve(int[][] inputLinesDigits, int outputDigitCount) {
        long totalOutputJoltage = 0;
        for (int[] inputLineDigits : inputLinesDigits) {
            int inputLineDigitCount = inputLineDigits.length;
            long bankJoltage = 0;
            int lastChosenInputLineDigitIndex = -1;
            for (int outputDigitIndex=0; outputDigitIndex<outputDigitCount; outputDigitIndex++) {
                int lastInputDigitIndexToConsider = inputLineDigitCount - (outputDigitCount - outputDigitIndex);
                int bestInputDigitIndex = -1;
                int bestInputDigit = -1;
                for (int inputDigitIndex=lastChosenInputLineDigitIndex+1; inputDigitIndex<=lastInputDigitIndexToConsider; inputDigitIndex++) {
                    int inputDigit = inputLineDigits[inputDigitIndex];
                    if (inputDigit > bestInputDigit) {
                        bestInputDigit = inputDigit;
                        bestInputDigitIndex = inputDigitIndex;
                    }
                }
                bankJoltage = Math.addExact(Math.multiplyExact(bankJoltage, 10), bestInputDigit);
                lastChosenInputLineDigitIndex = bestInputDigitIndex;
            }
            totalOutputJoltage = Math.addExact(totalOutputJoltage, bankJoltage);
        }
        return totalOutputJoltage;
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        char[][] inputLines = LineReader.charArraysArray(inputCharacters, true);
        int inputLineCount = inputLines.length;
        int[][] inputLinesDigits = new int[inputLineCount][];
        for (int inputLineIndex=0; inputLineIndex<inputLineCount; inputLineIndex++) {
            char[] inputLineCharacters = inputLines[inputLineIndex];
            int inputLineDigitCount = inputLineCharacters.length;
            int[] inputLineDigits = new int[inputLineDigitCount];
            for (int inputLineDigitIndex=0; inputLineDigitIndex<inputLineDigitCount; inputLineDigitIndex++) {
                int digit = Character.getNumericValue(inputLineCharacters[inputLineDigitIndex]);
                if (digit < 0 || digit > 9) {
                    throw new IllegalStateException("Invalid digit");
                }
                inputLineDigits[inputLineDigitIndex] = digit;
            }
            inputLinesDigits[inputLineIndex] = inputLineDigits;
        }
        return new BasicPuzzleResults<>(
            solve(inputLinesDigits, 2),
            solve(inputLinesDigits, 12)
        );
    }
}
