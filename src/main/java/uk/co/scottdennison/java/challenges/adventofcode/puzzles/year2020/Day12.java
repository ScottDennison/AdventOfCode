package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2020;

import uk.co.scottdennison.java.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleResults;
import uk.co.scottdennison.java.libs.text.input.LineReader;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Day12 implements IPuzzle {
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

		private final int adjustment;

		Rotation(int adjustment) {
			this.adjustment = adjustment;
		}

		public int getAdjustment() {
			return this.adjustment;
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

	private interface NavigationSystem {
		void moveForward(int count);

		void moveInDirection(Direction direction, int count);

		void rotate(int count);

		Position getShipPosition();
	}

	private static class DirectNavigationSystem implements NavigationSystem {
		private Position shipPosition;
		private Direction shipDirection;

		private static final Direction[] DIRECTIONS = Direction.values();
		private static final int DIRECTION_COUNT = DIRECTIONS.length;

		public DirectNavigationSystem(Position shipPosition, Direction shipDirection) {
			this.shipPosition = shipPosition;
			this.shipDirection = shipDirection;
		}

		@Override
		public void moveForward(int count) {
			this.shipPosition = this.shipDirection.calculateNewPosition(this.shipPosition, count);
		}

		@Override
		public void moveInDirection(Direction direction, int count) {
			this.shipPosition = direction.calculateNewPosition(this.shipPosition, count);
		}

		@Override
		public void rotate(int count) {
			int ordinal = this.shipDirection.ordinal() + count;
			while (ordinal < 0) {
				ordinal += DIRECTION_COUNT;
			}
			this.shipDirection = DIRECTIONS[ordinal % DIRECTION_COUNT];
		}

		@Override
		public Position getShipPosition() {
			return this.shipPosition;
		}
	}

	private static class WaypointNavigationSystem implements NavigationSystem {
		private Position shipPosition;
		private Position waypointRelativePosition;

		public WaypointNavigationSystem(Position shipPosition, Position waypointRelativePosition) {
			this.shipPosition = shipPosition;
			this.waypointRelativePosition = waypointRelativePosition;
		}

		@Override
		public void moveForward(int count) {
			this.shipPosition = new Position(this.shipPosition.getX() + (count * this.waypointRelativePosition.getX()), this.shipPosition.getY() + (count * this.waypointRelativePosition.getY()));
		}

		@Override
		public void moveInDirection(Direction direction, int count) {
			this.waypointRelativePosition = direction.calculateNewPosition(this.waypointRelativePosition, count);
		}

		@SuppressWarnings("SuspiciousNameCombination")
		@Override
		public void rotate(int count) {
			int oldX = this.waypointRelativePosition.getX();
			int oldY = this.waypointRelativePosition.getY();
			int newX;
			int newY;
			switch (count) {
				case 1:
				case -3:
					newX = -oldY;
					newY = oldX;
					break;
				case 2:
				case -2:
					newX = -oldX;
					newY = -oldY;
					break;
				case 3:
				case -1:
					newX = oldY;
					newY = -oldX;
					break;
				default:
					throw new IllegalStateException("Unexpected rotation.");
			}
			this.waypointRelativePosition = new Position(newX, newY);
		}

		@Override
		public Position getShipPosition() {
			return this.shipPosition;
		}
	}

	private static <T extends Enum<T>> Map<String, T> createLookup(Class<T> clazz) {
		return Arrays.stream(clazz.getEnumConstants()).collect(Collectors.toMap(direction -> Character.toString(direction.name().charAt(0)), Function.identity()));
	}

	private static final Pattern PATTERN = Pattern.compile("^(?<letter>[a-z])(?<number>[0-9]+)$", Pattern.CASE_INSENSITIVE);
	private static final Map<String, Direction> DIRECTION_LOOKUP = createLookup(Direction.class);
	private static final Map<String, Rotation> ROTATION_LOOKUP = createLookup(Rotation.class);

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		NavigationSystem partANavigationSystem = new DirectNavigationSystem(new Position(0, 0), Direction.EAST);
		NavigationSystem partBNavigationSystem = new WaypointNavigationSystem(new Position(0, 0), new Position(10, -1));
		NavigationSystem[] navigationSystems = {partANavigationSystem, partBNavigationSystem};
		for (String inputLine : LineReader.strings(inputCharacters)) {
			Matcher matcher = PATTERN.matcher(inputLine);
			if (!matcher.matches()) {
				throw new IllegalStateException("Unparsable line.");
			}
			String letter = matcher.group("letter");
			int number = Integer.parseInt(matcher.group("number"));
			Consumer<NavigationSystem> action;
			if ("F".equals(letter)) {
				action = navigationSystem -> navigationSystem.moveForward(number);
			}
			else {
				Direction lineDirection = DIRECTION_LOOKUP.get(letter);
				if (lineDirection == null) {
					Rotation lineRotation = ROTATION_LOOKUP.get(letter);
					if (lineRotation == null) {
						throw new IllegalStateException("Unrecognized character.");
					}
					else {
						if (number < 0 || number >= 360 || number % 90 != 0) {
							throw new IllegalStateException("Unexpected rotation amount.");
						}
						int count = (number / 90) * lineRotation.getAdjustment();
						action = navigationSystem -> navigationSystem.rotate(count);
					}
				}
				else {
					action = navigationSystem -> navigationSystem.moveInDirection(lineDirection, number);
				}
			}
			for (NavigationSystem navigationSystem : navigationSystems) {
				action.accept(navigationSystem);
			}
		}
		return new BasicPuzzleResults<>(
			calculateDistanceTravelled(partANavigationSystem),
			calculateDistanceTravelled(partBNavigationSystem)
		);
	}

	private static int calculateDistanceTravelled(NavigationSystem navigationSystem) {
		Position shipPosition = navigationSystem.getShipPosition();
		return Math.abs(shipPosition.getX()) + Math.abs(shipPosition.getY());
	}
}
