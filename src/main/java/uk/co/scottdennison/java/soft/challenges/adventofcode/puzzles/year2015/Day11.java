package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2015;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.regex.Pattern;

public class Day11 implements IPuzzle {
	private static final Pattern PASSWORD_VALIDATION_PATTERN = Pattern.compile("^[a-z]+$");

	private static final Pattern[] VALIDATION_PATTERNS = {
		Pattern.compile("^[a-z]*(?:abc|bcd|cde|def|efg|fgh|ghi|hij|ijk|jkl|klm|lmn|mno|nop|opq|pqr|qrs|rst|stu|tuv|uvw|vwx|wxy|xyz)[a-z]*$"),
		Pattern.compile("^[a-z]*([a-z])\\1[a-z]*((?!\\1)[a-z])\\2[a-z]*$")
	};

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		// Note code will not work if two characters next to each other are not allowed. Luckily, only i, o, and l are disallowed.
		String currentPasswordString = new String(inputCharacters).trim();
		if (!PASSWORD_VALIDATION_PATTERN.matcher(currentPasswordString).matches()) {
			throw new IllegalStateException("Current password is not as expected.");
		}
		char[] password = currentPasswordString.toCharArray();
		int passwordEndIndex = password.length - 1;
		String[] newPasswords = new String[2];
		int newPasswordCount = newPasswords.length;
		boolean hitNeededReplacement = false;
		for (int passwordCharacterIndex = 0; passwordCharacterIndex <= passwordEndIndex; passwordCharacterIndex++) {
			if (hitNeededReplacement) {
				password[passwordCharacterIndex] = 'a';
			}
			else {
				switch (password[passwordCharacterIndex]) {
					case 'i':
					case 'o':
					case 'l':
						password[passwordCharacterIndex]++;
						hitNeededReplacement = true;
						break;
				}
			}
		}
		for (int newPasswordIndex = 0; newPasswordIndex < newPasswordCount; newPasswordIndex++) {
			while (true) {
				for (int passwordCharacterIndex = passwordEndIndex; passwordCharacterIndex >= -1; passwordCharacterIndex--) {
					// We'll get an ArrayIndexOutOfBoundsException if this is unsolvable.
					switch (++password[passwordCharacterIndex]) {
						case '{':
							password[passwordCharacterIndex] = 'a';
							break;
						case 'i':
						case 'o':
						case 'l':
							password[passwordCharacterIndex]++;
							while (++passwordCharacterIndex < passwordEndIndex) {
								password[passwordCharacterIndex] = 'a';
							}
							passwordCharacterIndex = -2;
							break;
						default:
							passwordCharacterIndex = -2;
							break;
					}
				}
				String passwordString = new String(password);
				boolean isValid = true;
				for (Pattern validationPattern : VALIDATION_PATTERNS) {
					if (!validationPattern.matcher(passwordString).matches()) {
						isValid = false;
					}
				}
				if (isValid) {
					newPasswords[newPasswordIndex] = passwordString;
					break;
				}
			}
		}
		return new BasicPuzzleResults<>(
			newPasswords[0],
			newPasswords[1]
		);
	}
}
