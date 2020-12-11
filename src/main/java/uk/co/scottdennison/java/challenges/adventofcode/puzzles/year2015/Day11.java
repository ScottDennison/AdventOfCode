package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2015;

import uk.co.scottdennison.java.challenges.adventofcode.utils.InputFileUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Pattern;

public class Day11 {
	private static final Pattern FILE_VALIDATION_PATTERN = Pattern.compile("^[a-z]+$");

	private static final Pattern[] PATTERNS = {
		Pattern.compile("^[a-z]*(?:abc|bcd|cde|def|efg|fgh|ghi|hij|ijk|jkl|klm|lmn|mno|nop|opq|pqr|qrs|rst|stu|tuv|uvw|vwx|wxy|xyz)[a-z]*$"),
		Pattern.compile("^[abcdefghjkmnpqrstuvwxyz]+$"),
		Pattern.compile("^[a-z]*([a-z])\\1[a-z]*((?!\\1)[a-z])\\2[a-z]*$")
	};

	private static final int PASSWORDS_NEEDED = 2;

	public static void main(String[] args) throws IOException {
		String currentPasswordString = new String(Files.readAllBytes(InputFileUtils.getInputPath()), StandardCharsets.UTF_8).trim();
		if (!FILE_VALIDATION_PATTERN.matcher(currentPasswordString).matches()) {
			throw new IllegalStateException("Current password is not as expected.");
		}
		char[] password = currentPasswordString.toCharArray();
		int passwordEndIndex = password.length - 1;
		char continueChar = 'z' + 1;
		for (int passwordNumber = 1; passwordNumber <= PASSWORDS_NEEDED; passwordNumber++) {
			while (true) {
				for (int passwordIndex = passwordEndIndex; passwordIndex >= -1; passwordIndex--) {
					// We'll get an ArrayIndexOutOfBoundsException if this is unsolvable.
					if (++password[passwordIndex] == continueChar) {
						password[passwordIndex] = 'a';
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
					System.out.format("Santa's new password %d: %s%n", passwordNumber, passwordString);
					break;
				}
			}
		}
	}
}
