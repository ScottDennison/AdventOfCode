package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2016;

import uk.co.scottdennison.java.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleResults;
import uk.co.scottdennison.java.libs.text.input.LineReader;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.UnaryOperator;

public class Day06 implements IPuzzle {
	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		List<String> inputLines = LineReader.stringsList(inputCharacters, true);
		List<Map<Character, Integer>> frequencies = new ArrayList<>();
		int characterCount = inputLines.get(0).length();
		for (int characterIndex = 0; characterIndex < characterCount; characterIndex++) {
			frequencies.add(new HashMap<>());
		}
		for (String inputLine : inputLines) {
			if (inputLine.length() != characterCount) {
				throw new IllegalStateException("Incorrectly sized line.");
			}
			for (int characterIndex = 0; characterIndex < characterCount; characterIndex++) {
				char character = inputLine.charAt(characterIndex);
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
		return new BasicPuzzleResults<>(
			new String(message1),
			new String(message2)
		);
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
