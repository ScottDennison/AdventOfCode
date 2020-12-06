package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2016;

import uk.co.scottdennison.java.challenges.adventofcode.utils.InputFileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Day01 {
	private static class Position {
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
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			Position position = (Position) o;

			if (x != position.x) {
				return false;
			}
			return y == position.y;
		}

		@Override
		public int hashCode() {
			int result = x;
			result = 31 * result + y;
			return result;
		}
	}

	private static class State {
		private Direction direction;
		private Position position;
		private int pendingSteps;
		private boolean separationRequired;
		private List<Position> visitedPositions;

		public State(Direction initialDirection, Position initialPosition, int initialPendingSteps, boolean initialSeparationRequired, List<Position> initialVisitedPositions) {
			this.direction = initialDirection;
			this.position = initialPosition;
			this.pendingSteps = initialPendingSteps;
			this.separationRequired = initialSeparationRequired;
			this.visitedPositions = new ArrayList<>(initialVisitedPositions);
		}

		public Direction getDirection() {
			return this.direction;
		}

		public void setDirection(Direction direction) {
			this.direction = direction;
		}

		public Position getPosition() {
			return this.position;
		}

		public void setPosition(Position position) {
			this.position = position;
		}

		public int getPendingSteps() {
			return this.pendingSteps;
		}

		public void setPendingSteps(int pendingSteps) {
			this.pendingSteps = pendingSteps;
		}

		public boolean isSeparationRequired() {
			return this.separationRequired;
		}

		public void setSeparationRequired(boolean separationRequired) {
			this.separationRequired = separationRequired;
		}

		public List<Position> getVisitedPositions() {
			return Collections.unmodifiableList(this.visitedPositions);
		}

		public void setVisitedPositions(List<Position> visitedPositions) {
			this.visitedPositions = new ArrayList<>(visitedPositions);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			State state = (State) o;

			if (pendingSteps != state.pendingSteps) {
				return false;
			}
			if (separationRequired != state.separationRequired) {
				return false;
			}
			if (direction != state.direction) {
				return false;
			}
			if (!Objects.equals(position, state.position)) {
				return false;
			}
			return Objects.equals(visitedPositions, state.visitedPositions);
		}

		@Override
		public int hashCode() {
			int result = direction != null ? direction.hashCode() : 0;
			result = 31 * result + (position != null ? position.hashCode() : 0);
			result = 31 * result + pendingSteps;
			result = 31 * result + (separationRequired ? 1 : 0);
			result = 31 * result + (visitedPositions != null ? visitedPositions.hashCode() : 0);
			return result;
		}
	}

	private static final class ExpectationResult {
		private final Expectation nextExpectation;
		private final boolean advanceToNextByte;

		public ExpectationResult(Expectation nextExpectation, boolean advanceToNextByte) {
			this.nextExpectation = nextExpectation;
			this.advanceToNextByte = advanceToNextByte;
		}

		public Expectation getNextExpectation() {
			return this.nextExpectation;
		}

		public boolean isAdvanceToNextByte() {
			return this.advanceToNextByte;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			ExpectationResult that = (ExpectationResult) o;

			if (advanceToNextByte != that.advanceToNextByte) {
				return false;
			}
			return nextExpectation == that.nextExpectation;
		}

		@Override
		public int hashCode() {
			int result = nextExpectation != null ? nextExpectation.hashCode() : 0;
			result = 31 * result + (advanceToNextByte ? 1 : 0);
			return result;
		}
	}

	private enum Expectation {
		SPACING {
			@Override
			public ExpectationResult accept(byte inputByte, State state) {
				switch (inputByte) {
					case ' ':
					case '\t':
					case '\r':
					case '\n':
						state.setSeparationRequired(false);
						return new ExpectationResult(Expectation.SPACING, true);
					default:
						if (state.isSeparationRequired()) {
							return null;
						}
						return new ExpectationResult(Expectation.DIRECTION, false);
				}
			}

			@Override
			public void finish(State state) {
				if (state.isSeparationRequired()) {
					throw new IllegalStateException("Unexpected a direction, recieved end of input");
				}
			}
		},
		DIRECTION {
			@Override
			public ExpectationResult accept(byte inputByte, State state) {
				Rotation rotation;
				switch (inputByte) {
					case 'L':
						rotation = Rotation.LEFT;
						break;
					case 'R':
						rotation = Rotation.RIGHT;
						break;
					default:
						return null;
				}
				state.setDirection(rotation.calculateNewDirection(state.getDirection()));
				return new ExpectationResult(Expectation.PENDING_STEPS_DIGIT, true);
			}

			@Override
			public void finish(State state) {
				throw new IllegalStateException("Unexpected a direction, recieved end of input");
			}
		},
		PENDING_STEPS_DIGIT {
			@Override
			public ExpectationResult accept(byte inputByte, State state) {
				if (inputByte >= '0' && inputByte <= '9') {
					state.setPendingSteps((state.getPendingSteps() * 10) + (inputByte - '0'));
					return new ExpectationResult(Expectation.PENDING_STEPS_DIGIT, true);
				}
				this.move(state);
				if (inputByte == ',') {
					state.setSeparationRequired(false);
					return new ExpectationResult(Expectation.SPACING, true);
				}
				else {
					state.setSeparationRequired(true);
					return new ExpectationResult(Expectation.SPACING, false);
				}
			}

			@Override
			public void finish(State state) {
				this.move(state);
			}

			private void move(State state) {
				Direction direction = state.getDirection();
				Position position = state.getPosition();
				int pendingSteps = state.getPendingSteps();
				List<Position> newVisitedPositions = new ArrayList<>(state.getVisitedPositions());
				for (int step = 1; step <= pendingSteps; step++) {
					position = direction.calculateNewPosition(position, 1);
					newVisitedPositions.add(position);
				}
				state.setPosition(position);
				state.setVisitedPositions(newVisitedPositions);
				state.setPendingSteps(0);
			}
		};

		public abstract ExpectationResult accept(byte fileByte, State state);

		public abstract void finish(State state);
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

	public static void main(String[] args) throws IOException {
		byte[] fileBytes = Files.readAllBytes(InputFileUtils.getInputPath());
		Expectation expectation = Expectation.SPACING;
		Position initialPosition = new Position(0, 0);
		State state = new State(Direction.NORTH, initialPosition, 0, false, Collections.emptyList());
		for (byte fileByte : fileBytes) {
			while (true) {
				ExpectationResult expectationResult = expectation.accept(fileByte, state);
				if (expectationResult == null) {
					throw new IllegalStateException("Unexpected byte: " + fileByte);
				}
				expectation = expectationResult.getNextExpectation();
				if (expectationResult.isAdvanceToNextByte()) {
					break;
				}
			}
		}
		expectation.finish(state);
		Position easterBunnyHQPosition1 = state.getPosition();
		Set<Position> encounteredPositions = new HashSet<>();
		Position easterBunnyHQPosition2 = null;
		for (Position position : state.getVisitedPositions()) {
			if (!encounteredPositions.add(position)) {
				easterBunnyHQPosition2 = position;
				break;
			}
		}
		if (easterBunnyHQPosition2 == null) {
			throw new IllegalStateException("No duplicate positions visited.");
		}
		System.out.format("Easter bunny HQ #1 is %d block(s) away%n", calculateBlocksAway(initialPosition, easterBunnyHQPosition1));
		System.out.format("Easter bunny HQ #2 is %d block(s) away%n", calculateBlocksAway(initialPosition, easterBunnyHQPosition2));
	}

	private static int calculateBlocksAway(Position initialPosition, Position currentPosition) {
		return Math.abs(initialPosition.getX() - currentPosition.getX()) + Math.abs(initialPosition.getY() - currentPosition.getY());
	}
}
