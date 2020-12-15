package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2020;

import uk.co.scottdennison.java.challenges.adventofcode.utils.InputFileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Day15 {
	private static final int[] TARGET_MOVE_COUNTS = {2020, 30000000};

	public static void main(String[] args) throws IOException {
		int[] targetMoveCountsOrdered = Arrays.stream(TARGET_MOVE_COUNTS).distinct().sorted().toArray();
		int[] inputNumbers = Arrays.stream(Pattern.compile(",").split(new String(Files.readAllBytes(InputFileUtils.getInputPath())).trim())).mapToInt(Integer::parseInt).toArray();
		if (inputNumbers.length > targetMoveCountsOrdered[0]) {
			throw new IllegalStateException("Target move count is more than the amount of input numbers!");
		}
		Map<Integer, Integer> movesCalledWhen = new HashMap<>();
		int seedNumberCount = inputNumbers.length - 1;
		for (int inputNumberIndex = 0; inputNumberIndex < seedNumberCount; inputNumberIndex++) {
			if (movesCalledWhen.put(inputNumbers[inputNumberIndex], inputNumberIndex + 1) != null) {
				throw new IllegalStateException("Duplicate input number");
			}
		}
		int lastNumber = inputNumbers[seedNumberCount];
		int numberSpokenCount = inputNumbers.length;
		for (int targetMoveCount : targetMoveCountsOrdered) {
			while (numberSpokenCount < targetMoveCount) {
				Integer lastNumberCalledWhen = movesCalledWhen.get(lastNumber);
				movesCalledWhen.put(lastNumber, numberSpokenCount);
				if (lastNumberCalledWhen == null) {
					lastNumber = 0;
				}
				else {
					lastNumber = numberSpokenCount - lastNumberCalledWhen;
				}
				numberSpokenCount++;
			}
			System.out.format("Spoken number %d: %d%n", targetMoveCount, lastNumber);
		}
	}
}
