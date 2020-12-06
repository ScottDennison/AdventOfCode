package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2020;

import uk.co.scottdennison.java.challenges.adventofcode.utils.InputFileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day02 {
	private static final Pattern PATTERN = Pattern.compile("^([0-9]+)-([0-9]+) ([a-z]): ([a-z]+)$");

	public static void main(String[] args) throws IOException {
		int correctPasswordsForPolicy1 = 0;
		int correctPasswordsForPolicy2 = 0;
		for (String line : Files.readAllLines(InputFileUtils.getInputPath())) {
			Matcher matcher = PATTERN.matcher(line);
			if (!matcher.matches()) {
				throw new IllegalStateException("Unparsable line: " + line);
			}
			int value1 = Integer.parseInt(matcher.group(1));
			int value2 = Integer.parseInt(matcher.group(2));
			char charOfInterest = matcher.group(3).charAt(0);
			String password = matcher.group(4);
			int passwordLength = password.length();
			int charOfInterestCount = 0;
			for (int index = 0; index < passwordLength; index++) {
				if (password.charAt(index) == charOfInterest) {
					charOfInterestCount++;
				}
			}
			if (charOfInterestCount >= value1 && charOfInterestCount <= value2) {
				correctPasswordsForPolicy1++;
			}
			if ((password.charAt(value1 - 1) == charOfInterest) ^ (password.charAt(value2 - 1) == charOfInterest)) {
				correctPasswordsForPolicy2++;
			}
		}
		outputSummary(1, correctPasswordsForPolicy1);
		outputSummary(2, correctPasswordsForPolicy2);
	}

	private static void outputSummary(int policy, int correctPasswords) {
		System.out.format("Correct passwords for policy %d: %d%n", policy, correctPasswords);
	}
}
