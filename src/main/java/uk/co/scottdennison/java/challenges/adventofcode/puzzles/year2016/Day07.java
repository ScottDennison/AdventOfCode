package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2016;

import uk.co.scottdennison.java.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.utils.LineReader;

import java.io.PrintWriter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day07 implements IPuzzle {
	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		List<String> inputLines = LineReader.stringsList(inputCharacters, true);
		return new BasicPuzzleResults<>(
			countLinesMatchingPattern(inputLines, Pattern.compile("(?:(?!^.*\\[[a-z]*([a-z])((?!\\1)[a-z])\\2\\1[a-z]*].*$)^.*([a-z])((?!\\3)[a-z])\\4\\3.*$)")),
			countLinesMatchingPattern(inputLines, Pattern.compile("(?:^.*][a-z]*([a-z])((?!\\1)[a-z])\\1[a-z]*\\[(?:.*\\[)?[a-z]*\\2\\1\\2[a-z]*].*$)|(?:^.*\\[[a-z]*([a-z])((?!\\3)[a-z])\\3[a-z]*](?:.*])?[a-z]*\\4\\3\\4[a-z]*\\[.*$)"))
		);
	}

	private static long countLinesMatchingPattern(List<String> inputLines, Pattern pattern) {
		return inputLines.stream().map(String::trim).map(inputLine -> "]" + inputLine + "[").map(pattern::matcher).filter(Matcher::matches).count();
	}
}
