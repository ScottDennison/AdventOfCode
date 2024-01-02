package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2023;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

public class Day21PartialAttempt1 implements IPuzzle {
	private static enum Direction {
		RIGHT(0,1),
		DOWN(1, 0),
		LEFT(0,-1),
		UP(-1,0);

		private final int yDelta;
		private final int xDelta;

		Direction(int yDelta, int xDelta) {
			this.yDelta = yDelta;
			this.xDelta = xDelta;
		}

		public int getYDelta() {
			return yDelta;
		}

		public int getXDelta() {
			return xDelta;
		}
	}

	private static class Point {
		private final int y;
		private final int x;

		public Point(int y, int x) {
			this.y = y;
			this.x = x;
		}

		public int getY() {
			return y;
		}

		public int getX() {
			return x;
		}
	}

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		char[][] inputCharactersGrid = LineReader.charArraysArray(inputCharacters, true);
		int originalHeight = inputCharactersGrid.length;
		int originalWidth = inputCharactersGrid[0].length;
		int modifiedHeight = originalHeight+2;
		int modifiedWidth = originalHeight+2;
		Point[][] points = new Point[modifiedHeight][modifiedWidth];
		Point startPoint = null;
		for (int originalY=0; originalY<originalHeight; originalY++) {
			char[] inputCharactersGridRow = inputCharactersGrid[originalY];
			int modifiedY = originalY+1;
			Point[] pointsRow = points[modifiedY];
			for (int originalX=0; originalX<originalWidth; originalX++) {
				char inputGridCharacter = inputCharactersGridRow[originalX];
				if (inputGridCharacter != '#') {
					int modifiedX = originalX+1;
					pointsRow[modifiedX] = new Point(modifiedY, modifiedX);
					if (inputGridCharacter == 'S') {
						if (startPoint != null) {
							throw new IllegalStateException("Multiple start points");
						}
						startPoint = pointsRow[modifiedX];
					}
				}
			}
		}
		if (startPoint == null) {
			throw new IllegalStateException("No start point.");
		}
		Set<Point> lastStepPoints = new HashSet<>();
		lastStepPoints.add(startPoint);
		int steps = Integer.parseInt(new String(configProvider.getPuzzleConfigChars("part_a_steps")).trim());
		for (int step=1; step<=steps; step++) {
			Set<Point> thisStepPoints = new HashSet<>();
			for (Point lastStepPoint : lastStepPoints) {
				int y = lastStepPoint.getY();
				int x = lastStepPoint.getX();
				for (Direction direction : Direction.values()) {
					thisStepPoints.add(points[y+direction.getYDelta()][x+direction.getXDelta()]);
				}
			}
			thisStepPoints.remove(null);
			if (false) {
				char[][] displayGrid = new char[modifiedHeight][modifiedWidth];
				for (int y = 0; y < modifiedHeight; y++) {
					for (int x = 0; x < modifiedWidth; x++) {
						char character;
						if (y > 0 && y <= originalHeight && x > 0 && x <= originalWidth) {
							character = inputCharactersGrid[y - 1][x - 1];
						}
						else {
							character = '+';
						}
						displayGrid[y][x] = character;
					}
				}
				for (Point point : thisStepPoints) {
					displayGrid[point.getY()][point.getX()] = 'O';
				}
				for (Point point : lastStepPoints) {
					displayGrid[point.getY()][point.getX()] = 'O';
				}
				if (displayGrid[startPoint.getY()][startPoint.getX()] != 'O') {
					displayGrid[startPoint.getY()][startPoint.getX()] = 'S';
				}
				printWriter.println("Step " + step);
				for (int y = 0; y < modifiedHeight; y++) {
					printWriter.println(displayGrid[y]);
				}
				printWriter.println("Points: " + thisStepPoints.size());
				printWriter.println("------");
			}
			lastStepPoints = thisStepPoints;
		}
		return new BasicPuzzleResults<>(
			lastStepPoints.size(),
			null
		);
	}
}
