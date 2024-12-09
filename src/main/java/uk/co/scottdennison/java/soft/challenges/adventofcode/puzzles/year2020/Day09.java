package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2020;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Day09 implements IPuzzle {
	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		List<Long> numbers = new ArrayList<>();
		Long weaknessNumber = null;
		int previousNumbers = Integer.parseInt(new String(configProvider.getPuzzleConfigChars("previous_numbers")));
		for (String inputLine : LineReader.strings(inputCharacters)) {
			long number = Long.parseLong(inputLine);
			//noinspection ConstantConditions (IntellIJ is wrong, given the program would fail with a later IllegalStateException if so, and it doesn't)
			if (weaknessNumber == null) {
				int numberCount = numbers.size();
				if (numberCount > previousNumbers) {
					boolean matchFound = false;
					for (int index1 = numberCount - previousNumbers; index1 < numberCount && !matchFound; index1++) {
						for (int index2 = index1; index2 < numberCount; index2++) {
							if ((numbers.get(index1) + numbers.get(index2)) == number) {
								matchFound = true;
								break;
							}
						}
					}
					if (!matchFound) {
						weaknessNumber = number;
						break;
					}
				}
			}
			numbers.add(number);
		}
		if (weaknessNumber == null) {
			throw new IllegalStateException("No weakness number found.");
		}
		int numberCount = numbers.size();
		Long encryptionWeakness = null;
		for (int index1 = 0; index1 < numberCount; index1++) {
			long total = 0;
			long minimum = Long.MAX_VALUE;
			long maximum = Long.MIN_VALUE;
			for (int index2 = index1; index2 < numberCount; index2++) {
				long number = numbers.get(index2);
				total += number;
				minimum = Math.min(minimum, number);
				maximum = Math.max(maximum, number);
				if (total == weaknessNumber) {
					if (encryptionWeakness == null) {
						encryptionWeakness = maximum + minimum;
					}
					else {
						throw new IllegalStateException("Multiple encryption weaknesses.");
					}
				}
			}
		}
		if (encryptionWeakness == null) {
			throw new IllegalStateException("No encryption weakness.");
		}
		return new BasicPuzzleResults<>(
			weaknessNumber,
			encryptionWeakness
		);
	}
}
