package uk.co.scottdennison.java.challenges.adventofcode.year2020;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day02 {
	private static final Pattern PATTERN = Pattern.compile("^([0-9]+)-([0-9]+) ([a-z]): ([a-z]+)$");

	public static void main(String[] args) throws IOException {
		int correctPasswordsForPolicy1 = 0;
		int correctPasswordsForPolicy2 = 0;
		for (String line : Files.readAllLines(Paths.get("data/year2020/day02/input.txt"))) {
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
		System.out.format("Correct passwords for policy 1: %d%n", correctPasswordsForPolicy1);
		System.out.format("Correct passwords for policy 2: %d%n", correctPasswordsForPolicy2);
	}
}
