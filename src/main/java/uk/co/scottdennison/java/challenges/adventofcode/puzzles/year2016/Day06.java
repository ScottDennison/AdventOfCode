package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2016;

import uk.co.scottdennison.java.challenges.adventofcode.utils.InputFileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.UnaryOperator;

public class Day06 {
	public static void main(String[] args) throws IOException {
		List<String> fileLines = Files.readAllLines(InputFileUtils.getInputPath());
		List<Map<Character, Integer>> frequencies = new ArrayList<>();
		int characterCount = fileLines.get(0).length();
		for (int characterIndex = 0; characterIndex < characterCount; characterIndex++) {
			frequencies.add(new HashMap<>());
		}
		for (String fileLine : fileLines) {
			if (fileLine.length() != characterCount) {
				throw new IllegalStateException("Incorrectly sized line.");
			}
			for (int characterIndex = 0; characterIndex < characterCount; characterIndex++) {
				char character = fileLine.charAt(characterIndex);
				Map<Character, Integer> frequenciesForIndex = frequencies.get(characterIndex);
				Integer count = frequenciesForIndex.get(character);
				if (count == null) {
					count = 1;
				}
				else {
					count++;
				}
				frequenciesForIndex.put(character, count);
			}
		}
		char[] message1 = new char[characterCount];
		char[] message2 = new char[characterCount];
		for (int characterIndex = 0; characterIndex < characterCount; characterIndex++) {
			message1[characterIndex] = findFrequent(frequencies.get(characterIndex), Comparator::reversed);
			message2[characterIndex] = findFrequent(frequencies.get(characterIndex), comparator -> comparator);
		}
		outputSummary(1, message1);
		outputSummary(2, message2);
	}

	private static void outputSummary(int messageNumber, char[] message) {
		System.out.format("Message %d is %s%n", messageNumber, new String(message));
	}

	private static char findFrequent(Map<Character, Integer> frequenciesForCharacter, UnaryOperator<Comparator<Entry<Character, Integer>>> comparatorModifier) {
		return frequenciesForCharacter
			.entrySet()
			.stream()
			.sorted(
				comparatorModifier.apply(
					Comparator.comparingInt(Map.Entry::getValue)
				)
			)
			.limit(1)
			.map(Map.Entry::getKey)
			.findFirst()
			.orElseThrow(() -> new IllegalStateException("No character found."));
	}
}
