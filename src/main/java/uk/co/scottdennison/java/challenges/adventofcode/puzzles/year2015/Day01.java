package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2015;

import uk.co.scottdennison.java.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;

public final class Day01 implements IPuzzle {
	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		int floor = 0;
		Integer basementEnteredPosition = null;
		int inputCharacterCount = inputCharacters.length;
		for (int inputCharacterIndex = 0; inputCharacterIndex < inputCharacterCount; inputCharacterIndex++) {
			switch (inputCharacters[inputCharacterIndex]) {
				case '(':
					floor++;
					break;
				case ')':
					floor--;
					break;
				default:
					throw new IllegalStateException("Unexpected character");
			}
			if (floor < 0 && basementEnteredPosition == null) {
				basementEnteredPosition = inputCharacterIndex + 1;
			}
		}
		if (basementEnteredPosition == null && !partBPotentiallyUnsolvable) {
			throw new IllegalStateException("The basement was never entered");
		}
		return new BasicPuzzleResults<>(floor, basementEnteredPosition);
	}
}
