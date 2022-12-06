package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2022;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;

public class Day06 implements IPuzzle {
    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        return new BasicPuzzleResults<>(
            solve(inputCharacters,4),
            solve(inputCharacters,14)
        );
    }

    private int solve(char[] inputCharacters, int uniqueDesiredCount) {
        int inputCharactersLength = inputCharacters.length;
        int[] counts = new int[Character.MAX_VALUE];
        char[] buffer = new char[uniqueDesiredCount];
        int unqiueCount = 0;
        int inputCharacterIndex = 0;
        int bufferIndex = 0;
        while (inputCharacterIndex < inputCharactersLength) {
            if (inputCharacterIndex >= uniqueDesiredCount && --counts[buffer[bufferIndex]] == 0) {
                unqiueCount--;
            }
            char inputCharacter = inputCharacters[inputCharacterIndex];
            if (counts[inputCharacter]++ == 0) {
                unqiueCount++;
                if (unqiueCount == uniqueDesiredCount) {
                    return inputCharacterIndex+1;
                }
            }
            buffer[bufferIndex++] = inputCharacter;
            if (bufferIndex == uniqueDesiredCount) {
                bufferIndex = 0;
            }
            inputCharacterIndex++;
        }
        throw new IllegalStateException("No solution found");
    }
}
