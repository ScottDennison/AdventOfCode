package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2023;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.regex.Pattern;

public class Day09 implements IPuzzle {
	private static final Pattern PATTERN_SPACE = Pattern.compile(" +");

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		int totalOfLine0NewValueFowards = 0;
		int totalOfLine0NewValueBackwards = 0;
		for (String inputLine : LineReader.strings(inputCharacters)) {
			int[] topLine = PATTERN_SPACE.splitAsStream(inputLine).mapToInt(Integer::parseInt).toArray();
			int lineCount = topLine.length+1;
			int[][] lines = new int[lineCount][];
			lines[0] = topLine;
			for (int lineIndex=1; lineIndex<lineCount; lineIndex++) {
				int[] previousLine = lines[lineIndex-1];
				int entriesInThisLine = previousLine.length-1;
				int[] thisLine = new int[entriesInThisLine];
				boolean allZeros = true;
				for (int entryIndex=0; entryIndex<entriesInThisLine; entryIndex++) {
					int entry = previousLine[entryIndex+1]-previousLine[entryIndex];
					thisLine[entryIndex] = entry;
					allZeros &= entry == 0;
				}
				lines[lineIndex] = thisLine;
				if (allZeros) {
					int newValueForward = 0;
					int newValueBackward = 0;
					for (int backwardsLineIndex=lineIndex-1; backwardsLineIndex>=0; backwardsLineIndex--) {
						int[] thatLine = lines[backwardsLineIndex];
						newValueForward = thatLine[thatLine.length-1]+newValueForward;
						newValueBackward = thatLine[0]-newValueBackward;
					}
					totalOfLine0NewValueFowards += newValueForward;
					totalOfLine0NewValueBackwards += newValueBackward;
					break;
				}
			}
		}
		return new BasicPuzzleResults<>(
			totalOfLine0NewValueFowards,
			totalOfLine0NewValueBackwards
		);
	}
}
