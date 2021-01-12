package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2020;

import uk.co.scottdennison.java.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Day15 implements IPuzzle {
	private static final int PART_A_TARGET_MOVE_COUNT = 2020;
	private static final int PART_B_TARGET_MOVE_COUNT = 30000000;

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		int[] targetMoveCountsUnordered = {PART_A_TARGET_MOVE_COUNT, PART_B_TARGET_MOVE_COUNT};
		int[] targetMoveCountsOrdered = Arrays.stream(targetMoveCountsUnordered).distinct().sorted().toArray();
		int[] inputNumbers = Arrays.stream(Pattern.compile(",").split(new String(inputCharacters).trim())).mapToInt(Integer::parseInt).toArray();
		if (inputNumbers.length > targetMoveCountsOrdered[0]) {
			throw new IllegalStateException("Target move count is more than the amount of input numbers!");
		}
		int[] numbersCalledWhen = new int[targetMoveCountsOrdered[targetMoveCountsOrdered.length - 1]];
		int seedNumberCount = inputNumbers.length - 1;
		for (int inputNumberIndex = 0; inputNumberIndex < seedNumberCount; inputNumberIndex++) {
			int inputNumber = inputNumbers[inputNumberIndex];
			if (numbersCalledWhen[inputNumber] != 0) {
				throw new IllegalStateException("Duplicate input number");
			}
			numbersCalledWhen[inputNumber] = (inputNumberIndex + 1) + 1;
		}
		int lastNumber = inputNumbers[seedNumberCount];
		int numberSpokenCount = inputNumbers.length;
		Map<Integer, Integer> results = new HashMap<>();
		for (int targetMoveCount : targetMoveCountsOrdered) {
			while (numberSpokenCount < targetMoveCount) {
				int lastNumberCalledWhen = numbersCalledWhen[lastNumber] - 1;
				numbersCalledWhen[lastNumber] = numberSpokenCount + 1;
				if (lastNumberCalledWhen == -1) {
					lastNumber = 0;
				}
				else {
					lastNumber = numberSpokenCount - lastNumberCalledWhen;
				}
				numberSpokenCount++;
			}
			results.put(targetMoveCount, lastNumber);
		}
		return new BasicPuzzleResults<>(
			results.get(PART_A_TARGET_MOVE_COUNT),
			results.get(PART_B_TARGET_MOVE_COUNT)
		);
	}
}
