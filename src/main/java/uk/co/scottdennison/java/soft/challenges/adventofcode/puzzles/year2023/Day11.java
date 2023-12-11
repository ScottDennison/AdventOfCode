package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2023;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Day11 implements IPuzzle {
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
		char[][] inputGrid = LineReader.charArraysArray(inputCharacters,true);
		return new BasicPuzzleResults<>(
			solve(inputGrid, 1),
			solve(inputGrid, Integer.parseInt(new String(configProvider.getPuzzleConfigChars("part_b_gap")).trim())-1)
		);
	}

	private static long solve(char[][] inputGrid, int increase) {
		int height = inputGrid.length;
		int width = inputGrid[0].length;
		int[] yAdjustments = new int[height];
		int[] xAdjustments = new int[width];
		for (int y=0; y<height; y++) {
			boolean rowEmpty = true;
			for (int x=0; x<width; x++) {
				if (inputGrid[y][x] == '#') {
					rowEmpty = false;
					break;
				}
			}
			if (rowEmpty) {
				for (int y2=y+1; y2<height; y2++) {
					yAdjustments[y2] += increase;
				}
			}
		}
		for (int x=0; x<width; x++) {
			boolean columnEmpty = true;
			for (int y=0; y<height; y++) {
				if (inputGrid[y][x] == '#') {
					columnEmpty = false;
					break;
				}
			}
			if (columnEmpty) {
				for (int x2=x+1; x2<width; x2++) {
					xAdjustments[x2] += increase;
				}
			}
		}
		List<Point> pointsList = new ArrayList<>();
		for (int y=0; y<height; y++) {
			for (int x=0 ;x<width; x++) {
				if (inputGrid[y][x] == '#') {
					pointsList.add(new Point(y+yAdjustments[y],x+xAdjustments[x]));
				}
			}
		}
		Point[] pointsArray = pointsList.toArray(new Point[0]);
		int pointCount = pointsArray.length;
		long shortestPathSum = 0;
		for (int point1Index=0; point1Index<pointCount; point1Index++) {
			Point point1 = pointsArray[point1Index];
			int point1Y = point1.getY();
			int point1X = point1.getX();
			for (int point2Index=point1Index+1; point2Index<pointCount; point2Index++) {
				Point point2 = pointsArray[point2Index];
				int point2Y = point2.getY();
				int point2X = point2.getX();
				shortestPathSum += Math.abs(point2Y-point1Y)+Math.abs(point2X-point1X);
			}
		}
		return shortestPathSum;
	}
}
