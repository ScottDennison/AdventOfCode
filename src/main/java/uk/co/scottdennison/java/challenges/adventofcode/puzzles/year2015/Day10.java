package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2015;

import uk.co.scottdennison.java.challenges.adventofcode.utils.InputFileUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class Day10 {
	private static final Pattern VERIFICATION_PATTERN = Pattern.compile("^[0-9]+$");
	private static final char END_OF_INPUT = '_';

	private static final int[] ITERATIONS = {40, 50};

	public static void main(String[] args) throws IOException {
		String inputString = new String(Files.readAllBytes(InputFileUtils.getInputPath()), StandardCharsets.UTF_8).trim();
		if (!VERIFICATION_PATTERN.matcher(inputString).matches()) {
			throw new IllegalStateException("Input string does not match.");
		}
		int maximumIterations = IntStream.of(ITERATIONS).max().getAsInt();
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
		for (int desiredIterations : ITERATIONS) {
			System.out.format("%d iterations results in a length of: %d%n", desiredIterations, sequenceLengths[desiredIterations]);
		}
	}
}
