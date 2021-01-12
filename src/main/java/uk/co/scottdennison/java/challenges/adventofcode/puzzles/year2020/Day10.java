package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2020;

import uk.co.scottdennison.java.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleResults;
import uk.co.scottdennison.java.libs.text.input.LineReader;

import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

public class Day10 implements IPuzzle {
	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		List<Integer> adapterRatings = LineReader.stringsStream(inputCharacters).map(String::trim).map(Integer::parseInt).sorted().collect(Collectors.toList());
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
		return new BasicPuzzleResults<>(
			jump1Count * jump3Count,
			waysOfMakingRatings[targetAdapterRating]
		);
	}
}
