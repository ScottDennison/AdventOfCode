package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2015;

import uk.co.scottdennison.java.challenges.adventofcode.utils.InputFileUtils;

import java.io.IOException;
import java.nio.file.Files;

public class Day17 {
	private static final int TOTAL_LITERS = 150;

	public static void main(String[] args) throws IOException {
		int[] containerSizes = Files.lines(InputFileUtils.getInputPath()).mapToInt(Integer::parseInt).toArray();
		int minimumContainers = recurseContainersForMinimumContainers(containerSizes, 0, 0, 0);
		System.out.format("Ways to make %d liters of eggnog: %d%n", TOTAL_LITERS, recurseContainersForWays(containerSizes, 0, 0, 0, Integer.MAX_VALUE));
		System.out.format("Ways to make %d liters of eggnog when limited to %d containers (the minimum amount possible): %d%n", TOTAL_LITERS, minimumContainers, recurseContainersForWays(containerSizes, 0, 0, 0, minimumContainers));
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
