package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2023;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Objects;

public class Day21PartialAttempt2A implements IPuzzle {
	private static class RepetitionCoordinate {
		private final int repetitionY;
		private final int repetitionX;

		public RepetitionCoordinate(int repetitionY, int repetitionX) {
			this.repetitionY = repetitionY;
			this.repetitionX = repetitionX;
		}

		public int getRepetitionY() {
			return repetitionY;
		}

		public int getRepetitionX() {
			return repetitionX;
		}

		@Override
		public boolean equals(Object otherObject) {
			if (this == otherObject) {
				return true;
			}
			if (otherObject == null || getClass() != otherObject.getClass()) {
				return false;
			}
			RepetitionCoordinate otherRepetitionCoordinate = (RepetitionCoordinate) otherObject;
			return repetitionY == otherRepetitionCoordinate.repetitionY && repetitionX == otherRepetitionCoordinate.repetitionX;
		}

		@Override
		public int hashCode() {
			return Objects.hash(repetitionY, repetitionX);
		}
	}

	private static class GridCoordinate {
		private final RepetitionCoordinate repetitionCoordinate;
		private final int gridY;
		private final int gridX;

		public GridCoordinate(RepetitionCoordinate repetitionCoordinate, int gridY, int gridX) {
			this.repetitionCoordinate = repetitionCoordinate;
			this.gridY = gridY;
			this.gridX = gridX;
		}

		public RepetitionCoordinate getRepetitionCoordinate() {
			return repetitionCoordinate;
		}

		public int getGridY() {
			return gridY;
		}

		public int getGridX() {
			return gridX;
		}

		@Override
		public boolean equals(Object otherObject) {
			if (this == otherObject) {
				return true;
			}
			if (otherObject == null || getClass() != otherObject.getClass()) {
				return false;
			}
			GridCoordinate otherGridCoordinate = (GridCoordinate) otherObject;
			return gridY == otherGridCoordinate.gridY && gridX == otherGridCoordinate.gridX && repetitionCoordinate.equals(otherGridCoordinate.repetitionCoordinate);
		}

		@Override
		public int hashCode() {
			return Objects.hash(repetitionCoordinate, gridY, gridX);
		}
	}

	private static enum Direction {
		UP {
			@Override
			public GridCoordinate getNextPoint(GridCoordinate currentGridCoordinate, int gridHeight, int gridWidth) {
				RepetitionCoordinate currentRepetitionCoordinate = currentGridCoordinate.getRepetitionCoordinate();
				int currentGridY = currentGridCoordinate.getGridY();
				if (currentGridY == 0) {
					return new GridCoordinate(
						new RepetitionCoordinate(
							currentRepetitionCoordinate.getRepetitionY()-1,
							currentRepetitionCoordinate.getRepetitionX()
						),
						gridHeight-1,
						currentGridCoordinate.getGridX()
					);
				}
				else {
					return new GridCoordinate(
						currentRepetitionCoordinate,
						currentGridY-1,
						currentGridCoordinate.getGridX()
					);
				}
			}
		},
		LEFT {
			@Override
			public GridCoordinate getNextPoint(GridCoordinate currentGridCoordinate, int gridHeight, int gridWidth) {
				RepetitionCoordinate currentRepetitionCoordinate = currentGridCoordinate.getRepetitionCoordinate();
				int currentGridX = currentGridCoordinate.getGridX();
				if (currentGridX == 0) {
					return new GridCoordinate(
						new RepetitionCoordinate(
							currentRepetitionCoordinate.getRepetitionY(),
							currentRepetitionCoordinate.getRepetitionX()-1
						),
						currentGridCoordinate.getGridY(),
						gridWidth-1
					);
				}
				else {
					return new GridCoordinate(
						currentRepetitionCoordinate,
						currentGridCoordinate.getGridY(),
						currentGridX-1
					);
				}
			}
		},
		DOWN {
			@Override
			public GridCoordinate getNextPoint(GridCoordinate currentGridCoordinate, int gridHeight, int gridWidth) {
				RepetitionCoordinate currentRepetitionCoordinate = currentGridCoordinate.getRepetitionCoordinate();
				int newGridY = currentGridCoordinate.getGridY()+1;
				if (newGridY == gridHeight) {
					return new GridCoordinate(
						new RepetitionCoordinate(
							currentRepetitionCoordinate.getRepetitionY()+1,
							currentRepetitionCoordinate.getRepetitionX()
						),
						0,
						currentGridCoordinate.getGridX()
					);
				}
				else {
					return new GridCoordinate(
						currentRepetitionCoordinate,
						newGridY,
						currentGridCoordinate.getGridX()
					);
				}
			}
		},
		RIGHT {
			@Override
			public GridCoordinate getNextPoint(GridCoordinate currentGridCoordinate, int gridHeight, int gridWidth) {
				RepetitionCoordinate currentRepetitionCoordinate = currentGridCoordinate.getRepetitionCoordinate();
				int newGridX = currentGridCoordinate.getGridX()+1;
				if (newGridX == gridWidth) {
					return new GridCoordinate(
						new RepetitionCoordinate(
							currentRepetitionCoordinate.getRepetitionY(),
							currentRepetitionCoordinate.getRepetitionX()+1
						),
						currentGridCoordinate.getGridY(),
						0
					);
				}
				else {
					return new GridCoordinate(
						currentRepetitionCoordinate,
						currentGridCoordinate.getGridY(),
						newGridX
					);
				}
			}
		};

		public abstract GridCoordinate getNextPoint(GridCoordinate currentGridCoordinate, int gridHeight, int gridWidth);
	}

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		char[][] inputCharactersGrid = LineReader.charArraysArray(inputCharacters, true);
		int gridHeight = inputCharactersGrid.length;
		int gridWidth = inputCharactersGrid[0].length;
		boolean[][] gardenPlots = new boolean[gridHeight][gridWidth];
		GridCoordinate startGridCoordinate = null;
		for (int y=0; y<gridHeight; y++) {
			char[] inputCharactersGridRow = inputCharactersGrid[y];
			boolean[] gardenPlotsRow = gardenPlots[y];
			for (int x=0; x<gridWidth; x++) {
				char inputGridCharacter = inputCharactersGridRow[x];
				gardenPlotsRow[x] = inputGridCharacter != '.';
				if (inputGridCharacter == 'S') {;
					if (startGridCoordinate != null) {
						throw new IllegalStateException("Multiple start points");
					}
					startGridCoordinate = new GridCoordinate(new RepetitionCoordinate(0, 0), y, x);
				}
			}
		}
		if (startGridCoordinate == null) {
			throw new IllegalStateException("No start point.");
		}
		//Map<RepetitionCoordinate,Grid> gridMap = new HashMap<>();
		return null;
	}
}
