package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2022;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Day01 implements IPuzzle {
	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		List<String> rawLines = LineReader.stringsList(inputCharacters, true);
		rawLines.add("");
		List<Integer> elfTotals = new ArrayList<>();
		int total=0;
		for (String rawLine : rawLines) {
			if (rawLine.trim().isEmpty()) {
				elfTotals.add(total);
				total = 0;
			} else {
				total += Integer.parseInt(rawLine);
			}
		}
		Collections.sort(elfTotals, Comparator.reverseOrder());
		return new BasicPuzzleResults<>(
			elfTotals.get(0),
			elfTotals.stream().limit(3).mapToInt(x -> x).sum()
		);
	}
}
