package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2015;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;
import uk.co.scottdennison.java.libs.text.input.LineReader;

import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day02 implements IPuzzle {
	private static final Pattern PATTERN = Pattern.compile("^([0-9]+)x([0-9]+)x([0-9]+)$");

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		int squareFeetOfPaper = 0;
		int feetOfRibbon = 0;
		for (String inputLine : LineReader.strings(inputCharacters)) {
			Matcher matcher = PATTERN.matcher(inputLine);
			if (!matcher.matches()) {
				throw new IllegalStateException("Unparseable line");
			}
			int length = Integer.parseInt(matcher.group(1));
			int width = Integer.parseInt(matcher.group(2));
			int height = Integer.parseInt(matcher.group(3));
			int area1 = length * width;
			int area2 = width * height;
			int area3 = height * length;
			squareFeetOfPaper += (2 * area1) + (2 * area2) + (2 * area3) + Math.min(Math.min(area1, area2), area3);
			feetOfRibbon += (length * width * height) + (2 * length) + (2 * width) + (2 * height) - (2 * Math.max(length, Math.max(width, height)));
		}
		return new BasicPuzzleResults<>(squareFeetOfPaper, feetOfRibbon);
	}
}
