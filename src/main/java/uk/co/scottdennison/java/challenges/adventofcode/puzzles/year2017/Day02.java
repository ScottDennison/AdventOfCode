package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2017;

import uk.co.scottdennison.java.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.utils.LineReader;

import java.io.PrintWriter;
import java.util.regex.Pattern;

public class Day02 implements IPuzzle {
	private static final Pattern SPACE_PATTERN = Pattern.compile("\\s+");

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		int checksum1 = 0;
		int checksum2 = 0;
		for (String inputLine : LineReader.strings(inputCharacters)) {
			int minimumValue = Integer.MAX_VALUE;
			int maximumValue = Integer.MIN_VALUE;
			String[] values = SPACE_PATTERN.split(inputLine);
			int valueCount = values.length;
			int[] intValues = new int[valueCount];
			int cleanDivisionResult = -1;
			for (int newIndex = 0; newIndex < valueCount; newIndex++) {
				int newIntValue = Integer.parseInt(values[newIndex]);
				intValues[newIndex] = newIntValue;
				for (int oldIndex = 0; oldIndex < newIndex; oldIndex++) {
					int oldIntValue = intValues[oldIndex];
					int smallerValue = Math.min(oldIntValue, newIntValue);
					int largerValue = Math.max(oldIntValue, newIntValue);
					int divisionResult = largerValue / smallerValue;
					if ((divisionResult * smallerValue) == largerValue) {
						if (cleanDivisionResult < 0) {
							cleanDivisionResult = divisionResult;
						}
						else {
							throw new IllegalStateException("Multiple values on this row divide cleanly.");
						}
					}
				}
				minimumValue = Math.min(minimumValue, newIntValue);
				maximumValue = Math.max(maximumValue, newIntValue);
			}
			if (cleanDivisionResult < 0) {
				throw new IllegalStateException("No values on this row divide cleanly.");
			}
			checksum1 += (maximumValue - minimumValue);
			checksum2 += cleanDivisionResult;
		}
		return new BasicPuzzleResults<>(
			checksum1,
			checksum2
		);
	}
}
