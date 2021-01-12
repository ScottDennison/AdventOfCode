package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2020;

import uk.co.scottdennison.java.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleResults;
import uk.co.scottdennison.java.libs.text.input.LineReader;

import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day02 implements IPuzzle {
	private static final Pattern PATTERN = Pattern.compile("^([0-9]+)-([0-9]+) ([a-z]): ([a-z]+)$");

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		int correctPasswordsForPolicy1 = 0;
		int correctPasswordsForPolicy2 = 0;
		for (String line : LineReader.strings(inputCharacters)) {
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
		return new BasicPuzzleResults<>(
			correctPasswordsForPolicy1,
			correctPasswordsForPolicy2
		);
	}
}
