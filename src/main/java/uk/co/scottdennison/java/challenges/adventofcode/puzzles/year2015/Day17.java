package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2015;

import uk.co.scottdennison.java.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.utils.LineReader;

import java.io.PrintWriter;

public class Day17 implements IPuzzle {
	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		int[] containerSizes = LineReader.stringsStream(inputCharacters).mapToInt(Integer::parseInt).toArray();
		int totalLiters = Integer.parseInt(new String(configProvider.getPuzzleConfigChars("total_liters")));
		int minimumContainers = recurseContainersForMinimumContainers(containerSizes, totalLiters);
		return new BasicPuzzleResults<>(
			recurseContainersForWays(containerSizes, totalLiters, Integer.MAX_VALUE),
			recurseContainersForWays(containerSizes, totalLiters, minimumContainers)
		);
	}

	private static int recurseContainersForMinimumContainers(int[] containerSizes, int totalLiters) {
		return recurseContainersForMinimumContainers(containerSizes, totalLiters, 0, 0, 0);
	}

	private static int recurseContainersForMinimumContainers(int[] containerSizes, int totalLiters, int index, int currentLiters, int currentContainers) {
		if (index >= containerSizes.length) {
			return currentLiters == totalLiters ? currentContainers : Integer.MAX_VALUE;
		}
		else {
			int nextIndex = index + 1;
			return Math.min(
				recurseContainersForMinimumContainers(containerSizes, totalLiters, nextIndex, currentLiters + containerSizes[index], currentContainers + 1),
				recurseContainersForMinimumContainers(containerSizes, totalLiters, nextIndex, currentLiters, currentContainers)
			);
		}
	}

	private static int recurseContainersForWays(int[] containerSizes, int totalLiters, int maximumAllowableContainers) {
		return recurseContainersForWays(containerSizes, totalLiters, 0, 0, 0, maximumAllowableContainers);
	}

	private static int recurseContainersForWays(int[] containerSizes, int totalLiters, int index, int currentLiters, int currentContainers, int maximumAllowableContainers) {
		if (currentContainers > maximumAllowableContainers) {
			return 0;
		}
		else if (index >= containerSizes.length) {
			return currentLiters == totalLiters ? 1 : 0;
		}
		else {
			int nextIndex = index + 1;
			return
				recurseContainersForWays(containerSizes, totalLiters, nextIndex, currentLiters + containerSizes[index], currentContainers + 1, maximumAllowableContainers)
					+
					recurseContainersForWays(containerSizes, totalLiters, nextIndex, currentLiters, currentContainers, maximumAllowableContainers);
		}
	}
}
