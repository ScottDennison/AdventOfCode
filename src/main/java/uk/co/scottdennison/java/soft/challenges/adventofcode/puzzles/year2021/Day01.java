package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2021;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;

public class Day01 implements IPuzzle {
	@Override
	public IPuzzleResults runPuzzle(
		char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter
	) {
		int[] inputNumbers = LineReader.stringsStream(inputCharacters).mapToInt(Integer::parseInt).toArray();
		return new BasicPuzzleResults<>(
			runWithWindowSize(inputNumbers,1),
			runWithWindowSize(inputNumbers,3)
		);
	}

	private int runWithWindowSize(int[] inputNumbers, int windowSize) {
		int inputNumberCount = inputNumbers.length;
		int value = 0;
		int increaseCount = 0;
		for (int inputNumberIndex=0; inputNumberIndex<windowSize; inputNumberIndex++) {
			value += inputNumbers[inputNumberIndex];
		}
		for (int inputNumberWindowNewIndex=windowSize, inputNumberWindowOldIndex=0; inputNumberWindowNewIndex<inputNumberCount; inputNumberWindowNewIndex++, inputNumberWindowOldIndex++) {
			int newValue = value + inputNumbers[inputNumberWindowNewIndex] - inputNumbers[inputNumberWindowOldIndex];
			if (newValue > value) {
				increaseCount++;
			}
			value = newValue;
		}
		return increaseCount;
	}
}
