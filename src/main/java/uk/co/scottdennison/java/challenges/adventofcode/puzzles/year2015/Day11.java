package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2015;

import uk.co.scottdennison.java.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.regex.Pattern;

public class Day11 implements IPuzzle {
	private static final Pattern PASSWORD_VALIDATION_PATTERN = Pattern.compile("^[a-z]+$");

	private static final Pattern[] PATTERNS = {
		Pattern.compile("^[a-z]*(?:abc|bcd|cde|def|efg|fgh|ghi|hij|ijk|jkl|klm|lmn|mno|nop|opq|pqr|qrs|rst|stu|tuv|uvw|vwx|wxy|xyz)[a-z]*$"),
		Pattern.compile("^[abcdefghjkmnpqrstuvwxyz]+$"),
		Pattern.compile("^[a-z]*([a-z])\\1[a-z]*((?!\\1)[a-z])\\2[a-z]*$")
	};

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		String currentPasswordString = new String(inputCharacters).trim();
		if (!PASSWORD_VALIDATION_PATTERN.matcher(currentPasswordString).matches()) {
			throw new IllegalStateException("Current password is not as expected.");
		}
		char[] password = currentPasswordString.toCharArray();
		int passwordEndIndex = password.length - 1;
		char continueChar = 'z' + 1;
		String[] newPasswords = new String[2];
		int newPasswordCount = newPasswords.length;
		for (int newPasswordIndex = 0; newPasswordIndex <= newPasswordCount; newPasswordIndex++) {
			while (true) {
				for (int passwordCharacterIndex = passwordEndIndex; passwordCharacterIndex >= -1; passwordCharacterIndex--) {
					// We'll get an ArrayIndexOutOfBoundsException if this is unsolvable.
					if (++password[passwordCharacterIndex] == continueChar) {
						password[passwordCharacterIndex] = 'a';
					}
					else {
						break;
					}
				}
				String passwordString = new String(password);
				boolean isValid = true;
				for (Pattern pattern : PATTERNS) {
					if (!pattern.matcher(passwordString).matches()) {
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
