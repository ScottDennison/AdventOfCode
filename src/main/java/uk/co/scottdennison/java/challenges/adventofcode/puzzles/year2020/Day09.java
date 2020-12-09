package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2020;

import uk.co.scottdennison.java.challenges.adventofcode.utils.InputFileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Day09 {
	private static final int N_NUMBERS = 25;

	public static void main(String[] args) throws IOException {
		List<Long> numbers = new ArrayList<>();
		Long weaknessNumber = null;
		for (String fileLine : Files.readAllLines(InputFileUtils.getInputPath())) {
			long number = Long.parseLong(fileLine);
			//noinspection ConstantConditions (IntellIJ is wrong, given the program would fail with a later IllegalStateException if so, and it doesn't)
			if (weaknessNumber == null) {
				int numberCount = numbers.size();
				if (numberCount > N_NUMBERS) {
					boolean matchFound = false;
					for (int index1 = numberCount - N_NUMBERS; index1 < numberCount && !matchFound; index1++) {
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
		System.out.format("Weakness number is %d%n", weaknessNumber);
		System.out.format("Encryption weakness is %d%n", encryptionWeakness);
	}
}
