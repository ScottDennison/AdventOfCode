package uk.co.scottdennison.java.challenges.adventofcode.year2020;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Day03 {
	private static final Slope[][] SLOPE_SETS =
		{
			{
				new Slope(3, 1)
			},
			{
				new Slope(1, 1),
				new Slope(3, 1),
				new Slope(5, 1),
				new Slope(7, 1),
				new Slope(1, 2)
			}
		};

	private static class Slope {
		private final int xIncrement;
		private final int yIncrement;

		public Slope(int xIncrement, int yIncrement) {
			this.xIncrement = xIncrement;
			this.yIncrement = yIncrement;
		}

		public int getXIncrement() {
			return this.xIncrement;
		}

		public int getYIncrement() {
			return this.yIncrement;
		}
	}

	public static void main(String[] args) throws Exception {
		List<boolean[]> trees = new ArrayList<>();
		List<String> fileLines = Files.readAllLines(Paths.get("data/day03/input.txt"));
		int columnCount = fileLines.get(0).length();
		for (String fileLine : fileLines) {
			if (fileLine.length() != columnCount) {
				throw new IllegalStateException("mismatched line sizes");
			}
			boolean[] treesInRow = new boolean[columnCount];
			for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
				boolean isTree;
				switch (fileLine.charAt(columnIndex)) {
					case '.':
						isTree = false;
						break;
					case '#':
						isTree = true;
						break;
					default:
						throw new IllegalStateException("Unexpected character");
				}
				treesInRow[columnIndex] = isTree;
			}
			trees.add(treesInRow);
		}
		int rowCount = trees.size();
		for (Slope[] slopeSet : SLOPE_SETS) {
			int treesEncounteredProduct = 1;
			for (Slope slope : slopeSet) {
				int xIncrement = slope.getXIncrement();
				int yIncrement = slope.getYIncrement();
				int currentRow = 0;
				int currentColumn = 0;
				int treesEncountered = 0;
				while (currentRow >= 0 && currentRow < rowCount) {
					if (trees.get(currentRow)[currentColumn]) {
						treesEncountered++;
					}
					currentRow += yIncrement;
					currentColumn += xIncrement;
					while (currentColumn < 0) {
						currentColumn += columnCount;
					}
					currentColumn %= columnCount;
				}
				treesEncounteredProduct *= treesEncountered;
				System.out.format("Trees encountered for slope of right %d down %d is %d%n", xIncrement, yIncrement, treesEncountered);
			}
			System.out.format("Trees encountered product is %d%n", treesEncounteredProduct);
			System.out.println();
		}
	}
}
