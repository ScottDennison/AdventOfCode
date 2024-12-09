package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2021;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day02 implements IPuzzle {
	private static final Pattern PATTERN = Pattern.compile("^(?<direction>[a-z]+) (?<value>[0-9]+)$");

	@Override
	public IPuzzleResults runPuzzle(
		char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter
	) {
		int horizonalPositionA = 0;
		int depthA = 0;
		int horizonalPositionB = 0;
		int depthB = 0;
		int aimB = 0;
		for (String line : LineReader.strings(inputCharacters)) {
			Matcher matcher = PATTERN.matcher(line);
			if (!matcher.matches()) {
				throw new IllegalStateException("Unable to match pattern");
			}
			int value = Integer.parseInt(matcher.group("value"));
			switch(matcher.group("direction")) {
				case "down":
					depthA += value;
					aimB += value;
					break;
				case "up":
					depthA -= value;
					aimB -= value;
					break;
				case "forward":
					horizonalPositionA += value;
					horizonalPositionB += value;
					depthB += aimB*value;
					break;
				default:
					throw new IllegalStateException("Unexpected direction");
			}
		}
		return new BasicPuzzleResults<>(
			horizonalPositionA*depthA,
			horizonalPositionB*depthB
		);
	}
}
