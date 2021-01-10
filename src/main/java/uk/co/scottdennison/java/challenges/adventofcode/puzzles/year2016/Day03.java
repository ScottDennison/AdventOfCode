package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2016;

import uk.co.scottdennison.java.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.utils.LineReader;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Day03 implements IPuzzle {
	private static final Pattern SPACE_PATTERN = Pattern.compile("\\s+");

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		List<int[]> allSideIntsList = new ArrayList<>();
		for (String inputLine : LineReader.strings(inputCharacters)) {
			String[] sideStrings = SPACE_PATTERN.split(inputLine.trim());
			if (sideStrings.length != 3) {
				throw new IllegalStateException("Invalid amount of columns of data.");
			}
			int[] sideInts = new int[3];
			for (int side = 0; side < 3; side++) {
				sideInts[side] = Integer.parseInt(sideStrings[side]);
			}
			allSideIntsList.add(sideInts);
		}
		int[][] allSideInts = allSideIntsList.toArray(new int[0][3]);
		int rows = allSideInts.length;
		if (rows % 3 != 0) {
			throw new IllegalStateException("Invalid amount of rows of data.");
		}
		int validTriangleCount1 = 0;
		int validTriangleCount2 = 0;
		for (int y = 0; y < rows; y++) {
			validTriangleCount1 += isValidTriangleInt(allSideInts[y][0], allSideInts[y][1], allSideInts[y][2]);
			if (y % 3 == 0) {
				for (int x = 0; x < 3; x++) {
					validTriangleCount2 += isValidTriangleInt(allSideInts[y][x], allSideInts[y + 1][x], allSideInts[y + 2][x]);
				}
			}
		}
		return new BasicPuzzleResults<>(
			validTriangleCount1,
			validTriangleCount2
		);
	}

	private static int isValidTriangleInt(int side1, int side2, int side3) {
		return isValidTriangle(side1, side2, side3) ? 1 : 0;
	}

	private static boolean isValidTriangle(int side1, int side2, int side3) {
		return side1 + side2 > side3 && side1 + side3 > side2 && side2 + side3 > side1;
	}
}
