package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2016;

import uk.co.scottdennison.java.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.utils.LineReader;

import java.io.PrintWriter;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Day04 implements IPuzzle {
	private static final Pattern INPUT_PATTERN = Pattern.compile("^(?<encryptedName>(?:(?:[a-z]+)-)+)(?<sectorID>[0-9]+)\\[(?<checksum>[a-z]{5})]$");

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		int sumOfRealRoomSectorIds = 0;
		Integer targetSectorId = null;
		Pattern namePattern = Pattern.compile(new String(configProvider.getPuzzleConfigChars("target_room_regex")), Pattern.CASE_INSENSITIVE);
		for (String inputLine : LineReader.strings(inputCharacters)) {
			Matcher matcher = INPUT_PATTERN.matcher(inputLine);
			if (!matcher.matches()) {
				throw new IllegalStateException("Pattern not matched.");
			}
			String encryptedName = matcher.group("encryptedName");
			encryptedName = encryptedName.substring(0, encryptedName.length() - 1);
			String actualChecksum =
				encryptedName
					.chars()
					.filter(v -> v != '-')
					.boxed()
					.collect(
						Collectors.groupingBy(
							Function.identity(),
							Collectors.counting()
						)
					)
					.entrySet()
					.stream()
					.sorted(
						Comparator
							.comparingLong((ToLongFunction<Entry<Integer, Long>>) Entry::getValue).reversed()
							.thenComparingInt(Entry::getKey)
					)
					.map(Map.Entry::getKey)
					.limit(5)
					.map(v -> Character.toString((char) v.intValue()))
					.collect(Collectors.joining());
			if (actualChecksum.equals(matcher.group("checksum"))) {
				int sectorId = Integer.parseInt(matcher.group("sectorID"));
				char[] encryptedNameChars = encryptedName.toCharArray();
				int nameCharCount = encryptedNameChars.length;
				char[] decryptedNameChars = new char[nameCharCount];
				for (int nameCharIndex = 0; nameCharIndex < nameCharCount; nameCharIndex++) {
					char encryptedNameChar = encryptedNameChars[nameCharIndex];
					char decryptedNameChar;
					if (encryptedNameChar == '-') {
						decryptedNameChar = encryptedNameChar;
					}
					else {
						decryptedNameChar = (char) ((((encryptedNameChars[nameCharIndex] - 'a') + sectorId) % 26) + 'a');
					}
					decryptedNameChars[nameCharIndex] = decryptedNameChar;
				}
				String decryptedName = new String(decryptedNameChars);
				if (namePattern.matcher(decryptedName).matches()) {
					if (targetSectorId != null) {
						throw new IllegalStateException("Multiple target sectors.");
					}
					targetSectorId = sectorId;
				}
				sumOfRealRoomSectorIds += sectorId;
			}
		}
		if (targetSectorId == null) {
			throw new IllegalStateException("No target sector.");
		}
		return new BasicPuzzleResults<>(
			sumOfRealRoomSectorIds,
			targetSectorId
		);
	}
}
