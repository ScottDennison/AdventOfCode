package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2020;

import uk.co.scottdennison.java.challenges.adventofcode.utils.InputFileUtils;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Day03 {
	private static final char TREE_CHAR = '#';
	private static final char NO_TREE_CHAR = '.';

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
		private final int xStartPosition;
		private final int yStartPosition;
		private final int xIncrement;
		private final int yIncrement;

		public Slope(int xIncrement, int yIncrement) {
			this(0, 0, xIncrement, yIncrement);
		}

		public Slope(int xStartPosition, int yStartPosition, int xIncrement, int yIncrement) {
			this.xStartPosition = xStartPosition;
			this.yStartPosition = yStartPosition;
			this.xIncrement = xIncrement;
			this.yIncrement = yIncrement;
		}

		public int getXStartPosition() {
			return this.xStartPosition;
		}

		public int getYStartPosition() {
			return this.yStartPosition;
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
		List<String> fileLines = Files.readAllLines(InputFileUtils.getInputPath());
		int columnCount = fileLines.get(0).length();
		for (String fileLine : fileLines) {
			if (fileLine.length() != columnCount) {
				throw new IllegalStateException("mismatched line sizes");
			}
			boolean[] treesInRow = new boolean[columnCount];
			for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
				boolean isTree;
				switch (fileLine.charAt(columnIndex)) {
					case NO_TREE_CHAR:
						isTree = false;
						break;
					case TREE_CHAR:
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
				int xStartPosition = slope.getXStartPosition();
				int yStartPosition = slope.getYStartPosition();
				int xIncrement = slope.getXIncrement();
				int yIncrement = slope.getYIncrement();
				int currentColumn = xStartPosition;
				int currentRow = yStartPosition;
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
				System.out.format("Trees encountered for slope of right %d down %d, starting at position x=%d,y=%d is %d%n", xIncrement, yIncrement, xStartPosition, yStartPosition, treesEncountered);
			}
			System.out.format("Trees encountered product is %d%n", treesEncounteredProduct);
			System.out.println();
		}
	}
}
