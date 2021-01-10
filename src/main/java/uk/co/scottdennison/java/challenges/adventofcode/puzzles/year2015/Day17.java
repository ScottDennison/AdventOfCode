package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2015;

import uk.co.scottdennison.java.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.utils.LineReader;

import java.io.PrintWriter;

public class Day17 implements IPuzzle {
	private static final int TOTAL_LITERS = 150;

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		int[] containerSizes = LineReader.stringsStream(inputCharacters).mapToInt(Integer::parseInt).toArray();
		int minimumContainers = recurseContainersForMinimumContainers(containerSizes);
		return new BasicPuzzleResults<>(
			recurseContainersForWays(containerSizes, Integer.MAX_VALUE),
			recurseContainersForWays(containerSizes, minimumContainers)
		);
	}

	private static int recurseContainersForMinimumContainers(int[] containerSizes) {
		return recurseContainersForMinimumContainers(containerSizes, 0, 0, 0);
	}

	private static int recurseContainersForMinimumContainers(int[] containerSizes, int index, int currentLiters, int currentContainers) {
		if (index >= containerSizes.length) {
			return currentLiters == TOTAL_LITERS ? currentContainers : Integer.MAX_VALUE;
		}
		else {
			int nextIndex = index + 1;
			return Math.min(
				recurseContainersForMinimumContainers(containerSizes, nextIndex, currentLiters + containerSizes[index], currentContainers + 1),
				recurseContainersForMinimumContainers(containerSizes, nextIndex, currentLiters, currentContainers)
			);
		}
	}

	private static int recurseContainersForWays(int[] containerSizes, int maximumAllowableContainers) {
		return recurseContainersForWays(containerSizes, 0, 0, 0, maximumAllowableContainers);
	}

	private static int recurseContainersForWays(int[] containerSizes, int index, int currentLiters, int currentContainers, int maximumAllowableContainers) {
		if (currentContainers > maximumAllowableContainers) {
			return 0;
		}
		else if (index >= containerSizes.length) {
			return currentLiters == TOTAL_LITERS ? 1 : 0;
		}
		else {
			int nextIndex = index + 1;
			return
				recurseContainersForWays(containerSizes, nextIndex, currentLiters + containerSizes[index], currentContainers + 1, maximumAllowableContainers)
					+
					recurseContainersForWays(containerSizes, nextIndex, currentLiters, currentContainers, maximumAllowableContainers);
		}
	}
}
