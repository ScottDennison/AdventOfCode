package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2020;

import uk.co.scottdennison.java.challenges.adventofcode.utils.InputFileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

public class Day10 {
	public static void main(String[] args) throws IOException {
		List<Integer> adapterRatings = Files.lines(InputFileUtils.getInputPath()).map(String::trim).map(Integer::parseInt).sorted().collect(Collectors.toList());
		int jump1Count = 0;
		int jump3Count = 1;
		int previousAdapterRating = 0;
		for (int adapterRating : adapterRatings) {
			int adapterRatingJump = adapterRating - previousAdapterRating;
			switch (adapterRatingJump) {
				case 1:
					jump1Count++;
					break;
				case 2:
					break;
				case 3:
					jump3Count++;
					break;
				default:
					throw new IllegalStateException("Unexpected adapter rating jump of " + adapterRatingJump);
			}
			previousAdapterRating = adapterRating;
		}
		int targetAdapterRating = previousAdapterRating + 3;
		adapterRatings.add(targetAdapterRating);
		long[] waysOfMakingRatings = new long[targetAdapterRating + 1];
		waysOfMakingRatings[0] = 1;
		for (int adapterRating = 1; adapterRating <= targetAdapterRating; adapterRating++) {
			waysOfMakingRatings[adapterRating] = 0;
		}
		for (int adapterRating : adapterRatings) {
			for (int offset = 1; offset <= 3; offset++) {
				previousAdapterRating = adapterRating - offset;
				if (previousAdapterRating >= 0) {
					waysOfMakingRatings[adapterRating] += waysOfMakingRatings[previousAdapterRating];
				}
			}
		}
		System.out.format("Jolt differences product is %d%n", (jump1Count * jump3Count));
		System.out.format("Ways of combining adapters is %d%n", waysOfMakingRatings[targetAdapterRating]);
	}
}
