package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2016;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.common.AStarSolver;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Day24 implements IPuzzle {
	private static class Point {
		private final int x;
		private final int y;

		public Point(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}
	}

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		char[][] grid = LineReader.charArraysArray(inputCharacters, true);
		int maxY = grid.length-1;
		int maxX = grid[0].length-1;
		Map<Integer,Point> numberLocations = new HashMap<>();
		for (int y=0; y<=maxY; y++) {
			char[] gridRow = grid[y];
			for (int x=0; x<=maxX; x++) {
				char gridChar = gridRow[x];
				if (gridChar >= '0' && gridChar <= '9') {
					numberLocations.put(gridChar-'0',new Point(x, y));
				}
			}
		}
		int maxNumber = numberLocations.size()-1;
		for (int number=0; number<=maxNumber; number++) {
			if (!numberLocations.containsKey(number)) {
				throw new IllegalStateException("Non contiguous numbers.");
			}
		}
		Integer one = 1;
		int[][] distances = new int[maxNumber+1][maxNumber+1];
		AStarSolver.PointNodeAdapter<Integer> aStarPointNodeAdapter = new AStarSolver.PointNodeAdapter<>(
			(linkedFromPoint, linkedToPoint) -> grid[linkedToPoint.getY()][linkedToPoint.getX()] != '#',
			AStarSolver.PointNodeAdapter.UnchangingActualMoveCostAdapter.One.Of.INTEGER,
			AStarSolver.PointNodeAdapter.EstimatedMoveCostAdapter.CommonAlgorithms.Manhattan.Of.Integer.INSTANCE,
			0,
			maxY,
			0,
			maxX
		);
		AStarSolver.FullResultAdapter<AStarSolver.PointNodeAdapter.Point,Integer,Integer> aStarResultAdapter = new AStarSolver.ThrowingResultAdapter<>(new AStarSolver.CostOnlyResultAdapter<>());
		for (int numberFrom=0; numberFrom<=maxNumber; numberFrom++) {
			Point fromPoint = numberLocations.get(numberFrom);
			int fromY = fromPoint.getY();
			int fromX = fromPoint.getX();
			distances[numberFrom][numberFrom] = 0;
			for (int numberTo=numberFrom+1; numberTo<=maxNumber; numberTo++) {
				Point toPoint = numberLocations.get(numberTo);
				int toY = toPoint.getY();
				int toX = toPoint.getX();
				int stepCount = AStarSolver.run(
					aStarPointNodeAdapter,
					AStarSolver.CostAdapter.CommonTypes.Of.Integer.INSTANCE,
					aStarResultAdapter,
					new AStarSolver.PointNodeAdapter.Point(fromY, fromX),
					new AStarSolver.PointNodeAdapter.Point(toY, toX)
				);
				distances[numberFrom][numberTo] = stepCount;
				distances[numberTo][numberFrom] = stepCount;
			}
		}
		return new BasicPuzzleResults<>(
			recurse(distances,new boolean[maxNumber+1],0,Integer.MAX_VALUE,0,0,maxNumber, false),
			recurse(distances,new boolean[maxNumber+1],0,Integer.MAX_VALUE,0,0,maxNumber, true)
		);
	}

	private static int recurse(int[][] distances, boolean[] used, int currentDistance, int lowestDistance, int currentNumber, int recursionLevel, int maxNumber, boolean returnToZero) {
		if (recursionLevel >= maxNumber) {
			if (returnToZero) {
				currentDistance += distances[currentNumber][0];
			}
			if (currentDistance < lowestDistance) {
				return currentDistance;
			} else {
				return lowestDistance;
			}
		}
		used[currentNumber] = true;
		int[] distancesFromCurrent = distances[currentNumber];
		int newRecursionLevel = recursionLevel+1;
		for (int newNumber=0; newNumber<=maxNumber; newNumber++) {
			if (!used[newNumber]) {
				lowestDistance = recurse(distances, used, currentDistance + distancesFromCurrent[newNumber], lowestDistance, newNumber, newRecursionLevel, maxNumber, returnToZero);
			}
		}
		used[currentNumber] = false;
		return lowestDistance;
	}
}
