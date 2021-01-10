package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2015;

import uk.co.scottdennison.java.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.utils.LineReader;

import java.io.PrintWriter;

public class Day08 implements IPuzzle {
	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		int totalStringDecodeExtraLength = 0;
		int totalStringEncodeExtraLength = 0;
		for (String inputLine : LineReader.strings(inputCharacters)) {
			inputLine = inputLine.trim();
			int inputLineLength = inputLine.length();
			if (inputLine.charAt(0) != '\"' || inputLine.charAt(inputLineLength - 1) != '\"' || (inputLineLength >= 3 && inputLine.charAt(inputLineLength - 2) == '\\' && !(inputLineLength >= 4 && inputLine.charAt(inputLineLength - 3) == '\\'))) {
				throw new IllegalStateException("Improper string: " + inputLine);
			}
			int stringDecodeExtraLength = 2; // Current surrounding quotes.
			int stringEncodeExtraLength = 4; // New surrounding quotes and the escapes for the existing quotes.
			int escapeCharacterIndex = -1;
			while ((escapeCharacterIndex = inputLine.indexOf('\\', escapeCharacterIndex + 1)) != -1) {
				int escapedCharacterIndex = escapeCharacterIndex + 1;
				char escapedCharacter = inputLine.charAt(escapeCharacterIndex + 1);
				stringEncodeExtraLength++;
				switch (escapedCharacter) {
					case '\\':
						escapeCharacterIndex++; // Skip the escaped character to stop it triggering this block again.
						stringEncodeExtraLength++; // Given we will be skipping the escaped backslash, add the backslash needed for THAT to the count.
						stringDecodeExtraLength++;
						break;
					case '\"':
						stringEncodeExtraLength++; // The quote will need an extra escape too.
						stringDecodeExtraLength++;
						break;
					case 'x':
						stringDecodeExtraLength += 3;
						break;
					default:
						throw new IllegalStateException("Unexpected escape (" + ((int) escapedCharacter) + ") in string at index " + escapedCharacterIndex + ": " + inputLine);
				}
			}
			totalStringDecodeExtraLength += stringDecodeExtraLength;
			totalStringEncodeExtraLength += stringEncodeExtraLength;
		}
		return new BasicPuzzleResults<>(
			totalStringDecodeExtraLength,
			totalStringEncodeExtraLength
		);
	}
}
