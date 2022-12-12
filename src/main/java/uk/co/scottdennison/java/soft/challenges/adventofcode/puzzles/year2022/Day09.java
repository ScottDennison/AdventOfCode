package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2022;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day09 implements IPuzzle {
	private static final boolean DEBUG_MASTER_TURN_OFF = true;

	private static enum Direction {
		UP (0,-1),
		LEFT (-1, 0),
		DOWN (0, 1),
		RIGHT (1, 0);

		private static final Map<Character,Direction> characterRepresentationToDirectionMap;
		static {
			characterRepresentationToDirectionMap = new HashMap<>();
			for (Direction direction : Direction.values()) {
				if (characterRepresentationToDirectionMap.put(direction.getCharacterRepresentation(), direction) != null) {
					throw new IllegalStateException("Duplicate character representation");
				}
			}
		}

		public static Direction getDirectionFromCharacterRepresentation(char characterRepresentation) {
			Direction direction = Direction.characterRepresentationToDirectionMap.get(characterRepresentation);
			if (direction == null) {
				throw new IllegalStateException("No such direction");
			}
			return direction;
		}

		private final int xDelta;
		private final int yDelta;

		Direction(int xDelta, int yDelta) {
			this.xDelta = xDelta;
			this.yDelta = yDelta;
		}

		public int getXDelta() {
			return this.xDelta;
		}

		public int getYDelta() {
			return this.yDelta;
		}

		public char getCharacterRepresentation() {
			return this.name().toUpperCase(Locale.ROOT).charAt(0);
		}
	}

	private static final class Instruction {
		private final Direction direction;
		private final int amount;

		public Instruction(Direction direction, int amount) {
			this.direction = direction;
			this.amount = amount;
		}

		public Direction getDirection() {
			return this.direction;
		}

		public int getAmount() {
			return this.amount;
		}
	}

	private static final class MutablePoint {
		private int x;
		private int y;
		private final char characterRepresentation;

		public MutablePoint(MutablePoint mutablePoint) {
			this(mutablePoint.x, mutablePoint.y, mutablePoint.characterRepresentation);
		}

		public MutablePoint(MutablePoint mutablePoint, char characterRepresentation) {
			this(mutablePoint.x, mutablePoint.y, characterRepresentation);
		}

		public MutablePoint(int x, int y, char characterRepresentation) {
			this.x = x;
			this.y = y;
			this.characterRepresentation = characterRepresentation;
		}

		public int getX() {
			return this.x;
		}

		public int getY() {
			return this.y;
		}

		public char getCharacterRepresentation() {
			return this.characterRepresentation;
		}

		public boolean is(int x, int y) {
			return this.x == x && this.y == y;
		}

		public void move(int xDelta, int yDelta) {
			this.x += xDelta;
			this.y += yDelta;
		}
	}

	private static enum DebugLevel {
		NONE (false, false, false, false),
		PARTIAL (true, false, true, true),
		FULL (true, true, true, true);

		private final boolean debugInitialState;
		private final boolean debugInstructionMidState;
		private final boolean debugInstructionEndState;
		private final boolean debugVisitedCoordinates;

		DebugLevel(boolean debugInitialState, boolean debugInstructionMidState, boolean debugInstructionEndState, boolean debugVisitedCoordinates) {
			this.debugInitialState = debugInitialState;
			this.debugInstructionMidState = debugInstructionMidState;
			this.debugInstructionEndState = debugInstructionEndState;
			this.debugVisitedCoordinates = debugVisitedCoordinates;
		}

		public boolean isDebugInitialState() {
			return this.debugInitialState;
		}

		public boolean isDebugInstructionMidState() {
			return this.debugInstructionMidState;
		}

		public boolean isDebugInstructionEndState() {
			return this.debugInstructionEndState;
		}

		public boolean isDebugVisitedCoordinates() {
			return this.debugVisitedCoordinates;
		}
	}

	private static final Pattern PATTERN = Pattern.compile("^(?<direction>[A-Z]) (?<amount>[0-9]+)$", Pattern.CASE_INSENSITIVE);

	private static void recordTailKnotVisitedPoint(Map<Integer, Set<Integer>> tailKnotVisitedCoordinates, MutablePoint tailKnotPoint) {
		tailKnotVisitedCoordinates.computeIfAbsent(tailKnotPoint.getY(),__ -> new HashSet<>()).add(tailKnotPoint.getX());
	}

	private static void printHeader(PrintWriter printWriter, int significance, String headerText) {
		for (int iteration=1; iteration<=significance; iteration++) {
			printWriter.print("==");
		}
		printWriter.print(' ');
		printWriter.print(headerText);
		printWriter.print(' ');
		for (int iteration=1; iteration<=significance; iteration++) {
			printWriter.print("==");
		}
		printWriter.println();
		printWriter.println();
	}

	private static void printState(PrintWriter printWriter, int minX, int minY, int maxX, int maxY, MutablePoint[] allPointsPrioritized) {
		final MutablePoint[] pointsAtThisCoordinate = new MutablePoint[allPointsPrioritized.length];
		for (int y=minY; y<=maxY; y++) {
			StringBuilder overlapsInformationBuilder = null;
			for (int x=minX; x<=maxX; x++) {
				int pointsAtThisCoordinateCount = 0;
				for (MutablePoint point : allPointsPrioritized) {
					if (point.is(x, y)) {
						pointsAtThisCoordinate[pointsAtThisCoordinateCount++] = point;
					}
				}
				final char character;
				if (pointsAtThisCoordinateCount > 0) {
					character = pointsAtThisCoordinate[0].getCharacterRepresentation();
					if (pointsAtThisCoordinateCount > 1) {
						if (overlapsInformationBuilder == null) {
							overlapsInformationBuilder = new StringBuilder(" (");
						}
						else {
							overlapsInformationBuilder.append("; ");
						}
						overlapsInformationBuilder
							.append(character)
							.append(" covers ")
							.append(pointsAtThisCoordinate[1].getCharacterRepresentation());
						for (int pointsAtThisCoordinateIndex=2; pointsAtThisCoordinateIndex<pointsAtThisCoordinateCount; pointsAtThisCoordinateIndex++) {
							overlapsInformationBuilder
								.append(", ")
								.append(pointsAtThisCoordinate[pointsAtThisCoordinateIndex].getCharacterRepresentation());
						}
					}
				}
				else {
					character = '.';
				}
				printWriter.print(character);
			}
			if (overlapsInformationBuilder != null) {
				printWriter.print(overlapsInformationBuilder.append(')').toString());
			}
			printWriter.println();
		}
		printWriter.println();
	}

	private static void printVisitedCoordinates(PrintWriter printWriter, int minX, int minY, int maxX, int maxY, MutablePoint startPoint, Map<Integer, Set<Integer>> tailVisitedCoordinates) {
		final char visitedCharacter = '#';
		final char unvisitedCharacter = '.';
		final char[] unvisitedRowCharacters = new char[maxX-minX+1];
		Arrays.fill(unvisitedRowCharacters,unvisitedCharacter);
		final int startPointX = startPoint.getX();
		final int startPointY = startPoint.getY();
		for (int y=minY; y<=maxY; y++) {
			Set<Integer> tailVisitedCoordinatesInRow = tailVisitedCoordinates.get(y);
			if (tailVisitedCoordinatesInRow == null) {
				printWriter.print(unvisitedRowCharacters);
			} else {
				final boolean rowContainsStartPoint = y == startPointY;
				for (int x=minX; x<=maxX; x++) {
					final char character;
					if (rowContainsStartPoint && x == startPointX) {
						character = startPoint.getCharacterRepresentation();
					} else if (tailVisitedCoordinatesInRow.contains(x)) {
						character = visitedCharacter;
					} else {
						character = unvisitedCharacter;
					}
					printWriter.print(character);
				}
			}
			printWriter.println();
		}
		printWriter.println();
	}

	private static int solve(PrintWriter printWriter, DebugLevel debugLevel, Instruction[] instructions, char... extraKnotCharacters) {
		final MutablePoint startPoint = new MutablePoint(0, 0, 's');
		final MutablePoint headKnotPoint = new MutablePoint(startPoint, 'H');
		final int extraKnotCount = extraKnotCharacters.length;
		final MutablePoint[] extraKnotPoints = new MutablePoint[extraKnotCount];
		for (int knotIndex=0; knotIndex<extraKnotCount; knotIndex++) {
			extraKnotPoints[knotIndex] = new MutablePoint(startPoint, extraKnotCharacters[knotIndex]);
		}
		final MutablePoint tailKnotPoint = extraKnotPoints[extraKnotPoints.length-1];
		final MutablePoint[] allPointsPrioritized;
		int minX = headKnotPoint.getX();
		int minY = headKnotPoint.getY();
		int maxX = minX;
		int maxY = minY;
		if (debugLevel.isDebugInitialState() || debugLevel.isDebugInstructionMidState() || debugLevel.isDebugInstructionEndState() || debugLevel.isDebugVisitedCoordinates()) {
			printHeader(printWriter,2,"Solving with " + (extraKnotCount+1) + " knots");
			final MutablePoint boundsHeadPoint = new MutablePoint(headKnotPoint);
			for (Instruction instruction : instructions) {
				Direction direction = instruction.getDirection();
				int amount = instruction.getAmount();
				boundsHeadPoint.move(direction.getXDelta()*amount, direction.getYDelta()*amount);
				int boundsHeadX = boundsHeadPoint.getX();
				int boundsHeadY = boundsHeadPoint.getY();
				minX = Math.min(minX, boundsHeadX);
				minY = Math.min(minY, boundsHeadY);
				maxX = Math.max(maxX, boundsHeadX);
				maxY = Math.max(maxY, boundsHeadY);
			}
 			allPointsPrioritized = new MutablePoint[extraKnotCount+2];
			allPointsPrioritized[0] = headKnotPoint;
			System.arraycopy(extraKnotPoints, 0, allPointsPrioritized, 1, extraKnotCount);
			allPointsPrioritized[extraKnotCount+1] = startPoint;
		} else {
			allPointsPrioritized = null;
		}
		final Map<Integer, Set<Integer>> tailKnotVisitedCoordinates = new HashMap<>();
		recordTailKnotVisitedPoint(tailKnotVisitedCoordinates, tailKnotPoint);
		if (debugLevel.isDebugInitialState()) {
			printHeader(printWriter, 1, "Initial State");
			printState(printWriter, minX, minY, maxX, maxY, allPointsPrioritized);
		}
		for (Instruction instruction : instructions) {
			final Direction direction = instruction.getDirection();
			final int amount = instruction.getAmount();
			if (debugLevel.isDebugInstructionMidState() || debugLevel.isDebugInstructionEndState()) {
				printHeader(printWriter,1, direction.getCharacterRepresentation()+" "+amount);
			}
			final int directionXDelta = direction.getXDelta();
			final int directionYDelta = direction.getYDelta();
			for (int iteration=1; iteration<=amount; iteration++) {
				headKnotPoint.move(directionXDelta, directionYDelta);
				MutablePoint lastKnotPoint = headKnotPoint;
				for (MutablePoint extraKnotPoint : extraKnotPoints) {
					final int xDifference = lastKnotPoint.getX()-extraKnotPoint.getX();
					final int yDifference = lastKnotPoint.getY()-extraKnotPoint.getY();
					if (Math.abs(xDifference) <= 1 && Math.abs(yDifference) <= 1) {
						break;
					}
					final int xDelta;
					final int yDelta;
					if (xDifference > 0) {
						xDelta = Math.min(1,xDifference);
					} else if (xDifference < 0) {
						xDelta = Math.max(-1,xDifference);
					} else {
						xDelta = 0;
					}
					if (yDifference > 0) {
						yDelta = Math.min(1,yDifference);
					} else if (yDifference < 0) {
						yDelta = Math.max(-1,yDifference);
					} else {
						yDelta = 0;
					}
					extraKnotPoint.move(xDelta,yDelta);
					lastKnotPoint = extraKnotPoint;
				}
				if (debugLevel.isDebugInstructionMidState() || (iteration == amount && debugLevel.isDebugInstructionEndState())) {
					printState(printWriter, minX, minY, maxX, maxY, allPointsPrioritized);
				}
				recordTailKnotVisitedPoint(tailKnotVisitedCoordinates, tailKnotPoint);
			}
		}
		if (debugLevel.isDebugVisitedCoordinates()) {
			printHeader(printWriter, 1, "Visited Coordinates");
			printVisitedCoordinates(printWriter, minX, minY, maxX, maxY, startPoint, tailKnotVisitedCoordinates);
		}
		return tailKnotVisitedCoordinates.values().stream().mapToInt(Set::size).sum();
	}

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		final String[] lines = LineReader.stringsArray(inputCharacters,true);
		final Instruction[] instructions = new Instruction[lines.length];
		for (int index=0; index<lines.length; index++) {
			Matcher matcher = PATTERN.matcher(lines[index]);
			if (!matcher.matches()) {
				throw new IllegalStateException("Could not parse line");
			}
			instructions[index] = new Instruction(
				Direction.getDirectionFromCharacterRepresentation(matcher.group("direction").charAt(0)),
				Integer.parseInt(matcher.group("amount"))
			);
		}
		final DebugLevel debugLevel;
		if (DEBUG_MASTER_TURN_OFF) {
			debugLevel = DebugLevel.NONE;
		} else {
			debugLevel = DebugLevel.valueOf(new String(configProvider.getPuzzleConfigChars("debug_level")));
		}
		return new BasicPuzzleResults<>(
			solve(printWriter,debugLevel,instructions,'T'),
			solve(printWriter,debugLevel,instructions,'1','2','3','4','5','6','7','8','9')
		);
	}
}
