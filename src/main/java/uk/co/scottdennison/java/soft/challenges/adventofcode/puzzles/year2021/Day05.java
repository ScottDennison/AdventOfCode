package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2021;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day05 implements IPuzzle {
	private static final Pattern PATTERN = Pattern.compile("^(?<x1>[0-9]+),(?<y1>[0-9]+) -> (?<x2>[0-9]+),(?<y2>[0-9]+)$");

	@Override
	public IPuzzleResults runPuzzle(
		char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter
	) {
		int width = 0;
		int height = 0;
		int[][] occuranceCountsA = new int[height][];
		int[][] occuranceCountsB = new int[height][];
		for (String line : LineReader.strings(inputCharacters)) {
			Matcher matcher = PATTERN.matcher(line);
			if (!matcher.matches()) {
				throw new IllegalStateException("Could not parse line");
			}
			int x1 = Integer.parseInt(matcher.group("x1"));
			int y1 = Integer.parseInt(matcher.group("y1"));
			int x2 = Integer.parseInt(matcher.group("x2"));
			int y2 = Integer.parseInt(matcher.group("y2"));
			int xMax = Math.max(x1,x2);
			int yMax = Math.max(y1,y2);
			int xMin = Math.min(x1,x2);
			int yMin = Math.min(y1,y2);
			if (yMax >= height) {
				int newHeight = yMax+1;
				int[][] newOccuranceCountsA = new int[newHeight][];
				int[][] newOccuranceCountsB = new int[newHeight][];
				System.arraycopy(occuranceCountsA,0,newOccuranceCountsA,0,height);
				System.arraycopy(occuranceCountsB,0,newOccuranceCountsB,0,height);
				for (int y=height; y<newHeight; y++) {
					newOccuranceCountsA[y] = new int[width];
					newOccuranceCountsB[y] = new int[width];
				}
				occuranceCountsA = newOccuranceCountsA;
				occuranceCountsB = newOccuranceCountsB;
				height = newHeight;
			}
			if (xMax >= width) {
				int newWidth = xMax+1;
				for (int y=0; y<height; y++) {
					int[] occuranceCountsARow = occuranceCountsA[y];
					int[] occuranceCountsBRow = occuranceCountsB[y];
					int[] newOccuranceCountsARow = new int[newWidth];
					int[] newOccuranceCountsBRow = new int[newWidth];
					System.arraycopy(occuranceCountsARow,0,newOccuranceCountsARow,0,width);
					System.arraycopy(occuranceCountsBRow,0,newOccuranceCountsBRow,0,width);
					occuranceCountsA[y] = newOccuranceCountsARow;
					occuranceCountsB[y] = newOccuranceCountsBRow;
				}
				width = newWidth;
			}
			if (x1 == x2) {
				for (int y=yMin; y<=yMax; y++) {
					occuranceCountsA[y][x1]++;
					occuranceCountsB[y][x1]++;
				}
			}
			else if (y1 == y2) {
				for (int x=xMin; x<=xMax; x++) {
					occuranceCountsA[y1][x]++;
					occuranceCountsB[y1][x]++;
				}
			}
			else {
				int distance;
				if ((distance=(xMax-xMin)) != (yMax-yMin)) {
					throw new IllegalStateException("Cannot plot diagonal lines that are not exactly 45 degrees");
				}
				else {
					int xDelta = x2>x1?1:-1;
					int yDelta = y2>y1?1:-1;
					for (int adjustment=0, x=x1, y=y1; adjustment<=distance; adjustment++, x+=xDelta, y+=yDelta) {
						occuranceCountsB[y][x]++;
					}
				}
			}
		}
		int multiPointCountA = 0;
		int multiPointCountB = 0;
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				if (occuranceCountsA[y][x] >= 2) {
					multiPointCountA++;
				}
				if (occuranceCountsB[y][x] >= 2) {
					multiPointCountB++;
				}
			}
		}
		return new BasicPuzzleResults<>(
			multiPointCountA,
			multiPointCountB
		);
	}
}
