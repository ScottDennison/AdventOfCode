package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2020;

import uk.co.scottdennison.java.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleResults;
import uk.co.scottdennison.java.libs.text.input.LineReader;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Day03 implements IPuzzle {
	private static final char TREE_CHAR = '#';
	private static final char NO_TREE_CHAR = '.';

	private static final Slope[] PART_A_SLOPE_SET =
		{
			new Slope(3, 1)
		};

	private static final Slope[] PART_B_SLOPE_SET =
		{
			new Slope(1, 1),
			new Slope(3, 1),
			new Slope(5, 1),
			new Slope(7, 1),
			new Slope(1, 2)
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

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		List<boolean[]> trees = new ArrayList<>();
		List<String> inputLines = LineReader.stringsList(inputCharacters, true);
		int columnCount = inputLines.get(0).length();
		for (String inputLine : inputLines) {
			if (inputLine.length() != columnCount) {
				throw new IllegalStateException("mismatched line sizes");
			}
			boolean[] treesInRow = new boolean[columnCount];
			for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
				boolean isTree;
				switch (inputLine.charAt(columnIndex)) {
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
		boolean[][] treesArray = trees.toArray(new boolean[0][]);
		return new BasicPuzzleResults<>(
			countTreesUsingSlopeSet(treesArray, PART_A_SLOPE_SET),
			countTreesUsingSlopeSet(treesArray, PART_B_SLOPE_SET)
		);
	}

	private static long countTreesUsingSlopeSet(boolean[][] trees, Slope[] slopeSet) {
		int rowCount = trees.length;
		int columnCount = trees[0].length;
		long treesEncounteredProduct = 1;
		for (Slope slope : slopeSet) {
			int xStartPosition = slope.getXStartPosition();
			int yStartPosition = slope.getYStartPosition();
			int xIncrement = slope.getXIncrement();
			int yIncrement = slope.getYIncrement();
			int currentColumn = xStartPosition;
			int currentRow = yStartPosition;
			int treesEncountered = 0;
			while (currentRow >= 0 && currentRow < rowCount) {
				if (trees[currentRow][currentColumn]) {
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
		}
		return treesEncounteredProduct;
	}
}
