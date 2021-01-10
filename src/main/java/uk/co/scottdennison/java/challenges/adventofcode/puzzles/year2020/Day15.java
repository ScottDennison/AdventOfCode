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
		Map<Integer, Integer> movesCalledWhen = new HashMap<>();
		int seedNumberCount = inputNumbers.length - 1;
		for (int inputNumberIndex = 0; inputNumberIndex < seedNumberCount; inputNumberIndex++) {
			if (movesCalledWhen.put(inputNumbers[inputNumberIndex], inputNumberIndex + 1) != null) {
				throw new IllegalStateException("Duplicate input number");
			}
		}
		int lastNumber = inputNumbers[seedNumberCount];
		int numberSpokenCount = inputNumbers.length;
		Map<Integer, Integer> results = new HashMap<>();
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
			results.put(targetMoveCount, lastNumber);
		}
		return new BasicPuzzleResults<>(
			results.get(PART_A_TARGET_MOVE_COUNT),
			results.get(PART_B_TARGET_MOVE_COUNT)
		);
	}
}
