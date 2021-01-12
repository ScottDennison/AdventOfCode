package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2015;

import uk.co.scottdennison.java.challenges.adventofcode.framework.BasicPuzzlePartResults;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzlePartResults;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.framework.MultiPartPuzzleResults;
import uk.co.scottdennison.java.libs.text.input.LineReader;

import java.io.PrintWriter;
import java.util.regex.Pattern;

public class Day05 implements IPuzzle {
	private static final Pattern[] RULESET_A = {
		Pattern.compile("[aeiou].*[aeiou].*[aeiou]"),
		Pattern.compile("(.)\\1"),
		Pattern.compile("^((?!(ab|cd|pq|xy)).)*$")
	};

	private static final Pattern[] RULESET_B = {
		Pattern.compile("(..).*\\1"),
		Pattern.compile("(.).\\1")
	};

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		String[] inputLines = LineReader.stringsArray(inputCharacters, true);
		return new MultiPartPuzzleResults<>(
			runPuzzlePart(inputLines, RULESET_A),
			runPuzzlePart(inputLines, RULESET_B)
		);
	}

	private static IPuzzlePartResults runPuzzlePart(String[] inputLines, Pattern[] ruleSet) {
		int niceStringCount = 0;
		for (String inputLine : inputLines) {
			boolean isValid = true;
			for (Pattern rule : ruleSet) {
				if (!rule.matcher(inputLine).find()) {
					isValid = false;
					break;
				}
			}
			if (isValid) {
				niceStringCount++;
			}
		}
		return new BasicPuzzlePartResults<>(niceStringCount);
	}
}
