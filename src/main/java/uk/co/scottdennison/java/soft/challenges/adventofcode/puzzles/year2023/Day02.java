package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2023;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day02 implements IPuzzle {
	private final Pattern LINE_PATTERN = Pattern.compile("^Game (?<gameID>[0-9]+): (?<subGames>.+)$");
	private final Pattern WITHDRAWAL_PATTERN = Pattern.compile("^(?<amount>[0-9]+) (?<colour>[a-z]+)$");

	private static final Map<String, Integer> ALLOWED_AMOUNTS = new HashMap<>();
	static {
		ALLOWED_AMOUNTS.put("red", 12);
		ALLOWED_AMOUNTS.put("green", 13);
		ALLOWED_AMOUNTS.put("blue", 14);
	}

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		int partAIDSum = 0;
		int partBPowerSum = 0;
		for (String inputLine : LineReader.strings(inputCharacters)) {
			Matcher lineMatcher = LINE_PATTERN.matcher(inputLine);
			if (!lineMatcher.matches()) {
				throw new IllegalStateException("Unable to match line");
			}
			boolean gameValid = true;
			int gameID = Integer.parseInt(lineMatcher.group("gameID"));
			Map<String, Integer> minimumAmounts = new HashMap<>();
			String[] subGameStrings = lineMatcher.group("subGames").split(";");
			for (String subGameString : subGameStrings) {
				String[] withdrawalStrings = subGameString.split(",");
				for (String withdrawalString : withdrawalStrings) {
					Matcher withdrawalMatcher = WITHDRAWAL_PATTERN.matcher(withdrawalString.trim());
					if (!withdrawalMatcher.matches()) {
						throw new IllegalStateException("Unable to match withdrawal");
					}
					int amount = Integer.parseInt(withdrawalMatcher.group("amount"));
					String colour = withdrawalMatcher.group("colour");
					Integer allowedAmount = ALLOWED_AMOUNTS.get(colour);
					if (allowedAmount == null) {
						throw new IllegalStateException("Unexpected colour: " + colour);
					}
					if (amount > allowedAmount) {
						gameValid = false;
					}
					minimumAmounts.put(colour, Math.max(minimumAmounts.getOrDefault(colour, 0), amount));
				}
			}
			if (gameValid) {
				partAIDSum += gameID;
			}
			int power = 1;
			for (int amount : minimumAmounts.values()) {
				power *= amount;
			}
			partBPowerSum += power;
		}
		return new BasicPuzzleResults<>(
			partAIDSum,
			partBPowerSum
		);
	}
}
