package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2016;

import uk.co.scottdennison.java.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day01 implements IPuzzle {
	private static final Pattern PATTERN = Pattern.compile("(?<rotation>[LR])(?<steps>[0-9]+)(?:$|(?:,?[\\s]*))");

	private static final class Position {
		private final int x;
		private final int y;

		public Position(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public int getX() {
			return this.x;
		}

		public int getY() {
			return this.y;
		}

		@Override
		public boolean equals(Object otherObject) {
			if (this == otherObject) {
				return true;
			}
			if (!(otherObject instanceof Position)) {
				return false;
			}
			Position otherPosition = (Position) otherObject;
			return x == otherPosition.x && y == otherPosition.y;
		}

		@Override
		public int hashCode() {
			return (x << 16) | y;
		}
	}

	private enum Rotation {
		LEFT(-1),
		RIGHT(1);

		private static final Direction[] DIRECTIONS = Direction.values();
		private static final int DIRECTION_COUNT = DIRECTIONS.length;

		private final int enumOrdinalAdjustment;

		Rotation(int enumOrdinalAdjustment) {
			this.enumOrdinalAdjustment = enumOrdinalAdjustment;
		}

		public Direction calculateNewDirection(Direction oldDirection) {
			return DIRECTIONS[(oldDirection.ordinal() + DIRECTION_COUNT + this.enumOrdinalAdjustment) % DIRECTION_COUNT];
		}
	}

	private enum Direction {
		NORTH(0, -1),
		EAST(1, 0),
		SOUTH(0, 1),
		WEST(-1, 0);

		private final int xAdjustment;
		private final int yAdjustment;

		Direction(int xAdjustment, int yAdjustment) {
			this.xAdjustment = xAdjustment;
			this.yAdjustment = yAdjustment;
		}

		public Position calculateNewPosition(Position oldPosition, int amount) {
			return new Position(oldPosition.getX() + (amount * this.xAdjustment), oldPosition.getY() + (amount * this.yAdjustment));
		}
	}

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		String input = new String(inputCharacters).trim();
		int inputLength = input.length();
		Matcher matcher = PATTERN.matcher(input);
		Direction direction = Direction.NORTH;
		Position initialPosition = new Position(0, 0);
		Position currentPosition = initialPosition;
		Set<Position> visitedPositionsSet = new HashSet<>();
		Position firstDuplicatePosition = null;
		while (!matcher.hitEnd()) {
			if (matcher.lookingAt()) {
				Rotation rotation;
				switch (matcher.group("rotation")) {
					case "L":
					case "l":
						rotation = Rotation.LEFT;
						break;
					case "R":
					case "r":
						rotation = Rotation.RIGHT;
						break;
					default:
						throw new IllegalStateException("Unexpected rotation character.");
				}
				direction = rotation.calculateNewDirection(direction);
				int steps = Integer.parseInt(matcher.group("steps"));
				if (firstDuplicatePosition == null) {
					for (int step = 1; step <= steps; step++) {
						currentPosition = direction.calculateNewPosition(currentPosition, 1);
						if (!visitedPositionsSet.add(currentPosition)) {
							firstDuplicatePosition = currentPosition;
							int stepsRemaining = steps - step;
							if (stepsRemaining > 0) {
								currentPosition = direction.calculateNewPosition(currentPosition, stepsRemaining);
							}
							break;
						}
					}
				}
				else {
					currentPosition = direction.calculateNewPosition(currentPosition, steps);
				}
				matcher.region(matcher.end(), inputLength);
			}
			else {
				throw new IllegalStateException("Unable to parse instruction.");
			}
		}
		if (firstDuplicatePosition == null) {
			throw new IllegalStateException("No duplicate position.");
		}
		return new BasicPuzzleResults<>(
			calculateBlocksAway(initialPosition, currentPosition),
			calculateBlocksAway(initialPosition, firstDuplicatePosition)
		);
	}

	private static int calculateBlocksAway(Position initialPosition, Position position) {
		return Math.abs(initialPosition.getX() - position.getX()) + Math.abs(initialPosition.getY() - position.getY());
	}
}
