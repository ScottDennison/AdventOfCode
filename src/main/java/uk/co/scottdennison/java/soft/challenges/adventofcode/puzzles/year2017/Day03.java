package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2017;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class Day03 implements IPuzzle {
	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		int inputNumber = Integer.parseInt(new String(inputCharacters).trim());
		return new BasicPuzzleResults<>(
			solvePartA(inputNumber),
			solvePartB(inputNumber)
		);
	}

	private static int solvePartA(int inputNumber) {
		int gridSize = (int)Math.ceil(Math.sqrt(inputNumber));
		if (gridSize % 2 == 0) {
			gridSize++;
		}
		int sideCoordinateChange = gridSize-1;
		int difference = (gridSize*gridSize)-inputNumber;
		int xOffset = (gridSize-1)/2;
		int yOffset = xOffset;
		if (difference > 0) {
			int moveBack = Math.min(difference, sideCoordinateChange);
			xOffset -= moveBack;
			difference -= moveBack;
		}
		if (difference > 0) {
			int moveBack = Math.min(difference, sideCoordinateChange);
			yOffset -= moveBack;
			difference -= moveBack;
		}
		if (difference > 0) {
			int moveBack = Math.min(difference, sideCoordinateChange);
			xOffset += moveBack;
			difference -= moveBack;
		}
		if (difference > 0) {
			yOffset += difference;
			difference -= difference;
		}
		return Math.abs(xOffset)+Math.abs(yOffset);
	}

	private static enum Direction {
		RIGHT (0,1),
		UP (-1,0),
		LEFT(0,-1),
		DOWN(1,0);

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

	private static int solvePartB(int inputNumber) {
		Map<Integer,Map<Integer,Integer>> map = new HashMap<>();
		Direction[] directions = Direction.values();
		int directionCount = directions.length;
		int lengthToHalf = 2;
		int y = 0;
		int x = 0;
		map.computeIfAbsent(y,__ -> new HashMap<>()).putIfAbsent(x,1);
		while (true) {
			for (Direction direction : directions) {
				int directionYDelta = direction.getYDelta();
				int directionXDelta = direction.getXDelta();
				int movements = (lengthToHalf++)/2;
				for (int movement=1; movement<=movements; movement++) {
					y += directionYDelta;
					x += directionXDelta;
					int sum = 0;
					for (int sumYDelta=-1; sumYDelta<=1; sumYDelta++) {
						for (int sumXDelta=-1; sumXDelta<=1; sumXDelta++) {
							sum += map.computeIfAbsent(y + sumYDelta,__ -> new HashMap<>()).getOrDefault(x + sumXDelta, 0);
						}
					}
					if (sum > inputNumber) {
						return sum;
					}
					map.get(y).put(x,sum);
				}
			}
		}
	}
}
