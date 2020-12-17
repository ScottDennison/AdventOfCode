package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2015;

import uk.co.scottdennison.java.challenges.adventofcode.utils.InputFileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day16 {
	private static final Pattern PATTERN_ATTRIBUTE_SPLIT = Pattern.compile("\\n|(?: *, *)");
	private static final Pattern PATTERN_ATTRIBUTE = Pattern.compile("^(?<attributeName>[a-z]+) *: *(?<amount>[0-9]+)$");
	private static final Pattern PATTERN_FILE_LINE = Pattern.compile("^Sue (?<sueNumber>[0-9]+) *: *(?<attributes>.+)$");

	private static final String MFCSAM_TICKER_TAPE = "children: 3\n" +
		"cats: 7\n" +
		"samoyeds: 2\n" +
		"pomeranians: 3\n" +
		"akitas: 0\n" +
		"vizslas: 0\n" +
		"goldfish: 5\n" +
		"trees: 3\n" +
		"cars: 2\n" +
		"perfumes: 1";

	private static final String[] GREATER_THAN_ATTRIBUTES = {"cats", "trees"};
	private static final String[] LESS_THAN_ATTRIBUTES = {"pomeranians", "goldfish"};

	public static void main(String[] args) throws IOException {
		Map<String, Integer> mfcsamTickerTapeEntries = new HashMap<>();
		for (String mfcsamTickerTapeEntryString : PATTERN_ATTRIBUTE_SPLIT.split(MFCSAM_TICKER_TAPE)) {
			Matcher attributeMatcher = PATTERN_ATTRIBUTE.matcher(mfcsamTickerTapeEntryString);
			if (!attributeMatcher.matches()) {
				throw new IllegalStateException("Unparseable attribute");
			}
			String attributeName = attributeMatcher.group("attributeName");
			if (mfcsamTickerTapeEntries.put(attributeName, Integer.parseInt(attributeMatcher.group("amount"))) != null) {
				throw new IllegalStateException("Duplicate attribute.");
			}
		}
		List<String> fileLines = Files.readAllLines(InputFileUtils.getInputPath());
		System.out.format("The correct Sue number is %d%n", findCorrectSue(fileLines, mfcsamTickerTapeEntries, new String[0], new String[0]));
		System.out.format("The correct Sue number taking into account the outdated retroencabulator is %d%n", findCorrectSue(fileLines, mfcsamTickerTapeEntries, GREATER_THAN_ATTRIBUTES, LESS_THAN_ATTRIBUTES));
	}

	private static int findCorrectSue(List<String> fileLines, Map<String, Integer> mfcsamTickerTapeEntries, String[] greaterThanAttributeNames, String[] lessThanAttributeNames) {
		Set<String> greaterThanAttributeNamesSet = new HashSet<>(Arrays.asList(greaterThanAttributeNames));
		Set<String> lessThanAttributeNamesSet = new HashSet<>(Arrays.asList(lessThanAttributeNames));
		Integer correctSueNumber = null;
		for (String fileLine : fileLines) {
			Matcher fileLineMatcher = PATTERN_FILE_LINE.matcher(fileLine);
			if (!fileLineMatcher.matches()) {
				throw new IllegalStateException("Unparseable file line.");
			}
			boolean isRightSue = true;
			for (String fileLineAttributeString : PATTERN_ATTRIBUTE_SPLIT.split(fileLineMatcher.group("attributes"))) {
				Matcher attributeMatcher = PATTERN_ATTRIBUTE.matcher(fileLineAttributeString);
				if (!attributeMatcher.matches()) {
					throw new IllegalStateException("Unparseable attribute");
				}
				String attributeName = attributeMatcher.group("attributeName");
				Integer requiredAmount = mfcsamTickerTapeEntries.get(attributeName);
				if (requiredAmount == null) {
					throw new IllegalStateException("Attribute has name " + attributeName + " which not not an attribute supported by the MFCSAM machine.");
				}
				int requiredAmountPrimitive = requiredAmount;
				int actualAmount = Integer.parseInt(attributeMatcher.group("amount"));
				boolean isValid;
				if (greaterThanAttributeNamesSet.contains(attributeName)) {
					isValid = actualAmount > requiredAmountPrimitive;
				}
				else if (lessThanAttributeNamesSet.contains(attributeName)) {
					isValid = actualAmount < requiredAmountPrimitive;
				}
				else {
					isValid = actualAmount == requiredAmountPrimitive;
				}
				if (!isValid) {
					isRightSue = false;
					break;
				}
			}
			if (isRightSue) {
				if (correctSueNumber == null) {
					correctSueNumber = Integer.parseInt(fileLineMatcher.group("sueNumber"));
				}
				else {
					throw new IllegalStateException("Multiple matching Sues.");
				}
			}
		}
		if (correctSueNumber == null) {
			throw new IllegalStateException("No matching Sues.");
		}
		return correctSueNumber;
	}
}
