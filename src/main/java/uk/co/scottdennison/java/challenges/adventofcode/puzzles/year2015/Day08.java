package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2015;

import uk.co.scottdennison.java.challenges.adventofcode.utils.InputFileUtils;

import java.io.IOException;
import java.nio.file.Files;

public class Day08 {
	public static void main(String[] args) throws IOException {
		int totalStringDecodeExtraLength = 0;
		int totalStringEncodeExtraLength = 0;
		for (String fileLine : Files.readAllLines(InputFileUtils.getInputPath())) {
			fileLine = fileLine.trim();
			int fileLineLength = fileLine.length();
			if (fileLine.charAt(0) != '\"' || fileLine.charAt(fileLineLength - 1) != '\"' || (fileLineLength >= 3 && fileLine.charAt(fileLineLength - 2) == '\\' && !(fileLineLength >= 4 && fileLine.charAt(fileLineLength - 3) == '\\'))) {
				throw new IllegalStateException("Improper string: " + fileLine);
			}
			int stringDecodeExtraLength = 2; // Current surrounding quotes.
			int stringEncodeExtraLength = 4; // New surrounding quotes and the escapes for the existing quotes.
			int escapeCharacterIndex = -1;
			while ((escapeCharacterIndex = fileLine.indexOf('\\', escapeCharacterIndex + 1)) != -1) {
				int escapedCharacterIndex = escapeCharacterIndex + 1;
				char escapedCharacter = fileLine.charAt(escapeCharacterIndex + 1);
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
						throw new IllegalStateException("Unexpected escape (" + ((int) escapedCharacter) + ") in string at index " + escapedCharacterIndex + ": " + fileLine);
				}
			}
			totalStringDecodeExtraLength += stringDecodeExtraLength;
			totalStringEncodeExtraLength += stringEncodeExtraLength;
		}
		System.out.format("Total wasted characters when decoding string is %d%n", totalStringDecodeExtraLength);
		System.out.format("Total new characters when encoding string is %d%n", totalStringEncodeExtraLength);
	}
}
