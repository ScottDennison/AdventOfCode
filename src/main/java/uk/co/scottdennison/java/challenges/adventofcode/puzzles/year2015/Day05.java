package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2015;

import uk.co.scottdennison.java.challenges.adventofcode.utils.InputFileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class Day05 {
	public static void main(String[] args) throws IOException {
		int niceStringCountForRuleset1 = 0;
		int niceStringCountForRuleset2 = 0;
		for (String fileLine : Files.readAllLines(InputFileUtils.getInputPath())) {
			char[] characters = fileLine.toCharArray();
			int charactersCount = characters.length;
			int vowelCount = 0;
			boolean hasDisallowedSubstring = false;
			boolean hasRepeatedLetter = false;
			boolean hasRepeatedLetterWithGap = false;
			boolean hasRepeatedSubstring = false;
			char lastCharacter1 = 0;
			char lastCharacter2 = 0;
			Map<String, Integer> encouteredSubStrings = new HashMap<>();
			char[] thisSubstringChars = new char[2];
			for (int characterIndex = 0; characterIndex < charactersCount; characterIndex++) {
				char character = characters[characterIndex];
				if (!hasRepeatedSubstring) {
					thisSubstringChars[0] = lastCharacter1;
					thisSubstringChars[1] = character;
					String thisSubstring = new String(thisSubstringChars);
					Integer existingSubstringStartCharacterIndex = encouteredSubStrings.putIfAbsent(thisSubstring, characterIndex);
					if (existingSubstringStartCharacterIndex != null && (characterIndex - existingSubstringStartCharacterIndex) > 1) {
						hasRepeatedSubstring = true;
					}
				}
				if (character == lastCharacter1) {
					hasRepeatedLetter = true;
				}
				if (character == lastCharacter2) {
					hasRepeatedLetterWithGap = true;
				}
				switch (character) {
					case 'a':
					case 'e':
					case 'i':
					case 'o':
					case 'u':
						vowelCount++;
						break;
					case 'b':
						if (lastCharacter1 == 'a') {
							hasDisallowedSubstring = true;
						}
						break;
					case 'd':
						if (lastCharacter1 == 'c') {
							hasDisallowedSubstring = true;
						}
						break;
					case 'q':
						if (lastCharacter1 == 'p') {
							hasDisallowedSubstring = true;
						}
						break;
					case 'y':
						if (lastCharacter1 == 'x') {
							hasDisallowedSubstring = true;
						}
						break;
				}
				lastCharacter2 = lastCharacter1;
				lastCharacter1 = character;
			}
			if (vowelCount >= 3 && hasRepeatedLetter && !hasDisallowedSubstring) {
				niceStringCountForRuleset1++;
			}
			if (hasRepeatedSubstring && hasRepeatedLetterWithGap) {
				niceStringCountForRuleset2++;
			}
		}
		System.out.format("Nice string count for ruleset 1: %d%n", niceStringCountForRuleset1);
		System.out.format("Nice string count for ruleset 2: %d%n", niceStringCountForRuleset2);
	}
}
