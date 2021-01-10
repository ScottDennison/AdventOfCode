package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2015;

import uk.co.scottdennison.java.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.regex.Pattern;

public class Day10 implements IPuzzle {
	private static final Pattern VERIFICATION_PATTERN = Pattern.compile("^[0-9]+$");
	private static final char END_OF_INPUT = '_';

	private static final int PART_A_ITERATIONS = 40;
	private static final int PART_B_ITERATIONS = 50;

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		String inputString = new String(inputCharacters).trim();
		if (!VERIFICATION_PATTERN.matcher(inputString).matches()) {
			throw new IllegalStateException("Input string does not match.");
		}
		int maximumIterations = Math.max(PART_A_ITERATIONS, PART_B_ITERATIONS);
		char[] input = (inputString + END_OF_INPUT).toCharArray();
		int[] sequenceLengths = new int[maximumIterations + 1];
		sequenceLengths[0] = input.length;
		for (int iteration = 1; iteration <= maximumIterations; iteration++) {
			StringBuilder newInputBuilder = new StringBuilder();
			char lastCharacter = 0;
			int characterCount = 0;
			for (char currentCharacter : input) {
				if (currentCharacter == lastCharacter) {
					characterCount++;
				}
				else {
					if (characterCount > 0) {
						newInputBuilder.append(characterCount);
						newInputBuilder.append(lastCharacter);
					}
					lastCharacter = currentCharacter;
					characterCount = 1;
				}
			}
			newInputBuilder.append('_');
			int newInputLength = newInputBuilder.length();
			sequenceLengths[iteration] = newInputLength - 1;
			input = new char[newInputLength];
			newInputBuilder.getChars(0, newInputLength, input, 0);
		}
		return new BasicPuzzleResults<>(
			sequenceLengths[PART_A_ITERATIONS],
			sequenceLengths[PART_B_ITERATIONS]
		);
	}
}
