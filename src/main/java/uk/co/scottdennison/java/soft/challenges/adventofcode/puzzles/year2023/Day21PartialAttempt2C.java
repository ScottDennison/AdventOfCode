package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2023;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;

public class Day21PartialAttempt2C implements IPuzzle {
	private static class Coordinate {
		private final int y;
		private final int x;

		public Coordinate(int y, int x) {
			this.y = y;
			this.x = x;
		}

		public int getY() {
			return y;
		}

		public int getX() {
			return x;
		}

		@Override
		public boolean equals(Object otherObject) {
			if (this == otherObject) {
				return true;
			}
			if (otherObject == null || getClass() != otherObject.getClass()) {
				return false;
			}
			Coordinate otherCoordinate = (Coordinate) otherObject;
			return y == otherCoordinate.y && x == otherCoordinate.x;
		}

		@Override
		public int hashCode() {
			return ((y&0xFFFF)<<16)|(x&0xFFFF);
		}

		@Override
		public String toString() {
			return "Coordinate{" + "y=" + y + ", x=" + x + '}';
		}
	}

	private static class Runner {
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

		private static class EntryEvent {
			private final int relativeTime;
			private final Coordinate coordinate;

			public EntryEvent(int relativeTime, Coordinate coordinate) {
				this.relativeTime = relativeTime;
				this.coordinate = coordinate;
			}

			@Override
			public boolean equals(Object otherObject) {
				if (this == otherObject) {
					return true;
				}
				if (otherObject == null || getClass() != otherObject.getClass()) {
					return false;
				}
				EntryEvent otherEntryEvent = (EntryEvent) otherObject;
				return relativeTime == otherEntryEvent.relativeTime && coordinate.equals(otherEntryEvent.coordinate);
			}

			@Override
			public int hashCode() {
				return Objects.hash(relativeTime, coordinate);
			}

			@Override
			public String toString() {
				return "EntryEvent{" + "relativeTime=" + relativeTime + ", coordinate=" + coordinate + '}';
			}
		}

		private static class CompleteGridSignature {
			private final Set<EntryEvent> entryEvents;
			private final int stepsToComplete;
			private transient Integer hash;

			public CompleteGridSignature(Set<EntryEvent> entryEvents, int stepsToComplete) {
				this.entryEvents = entryEvents;
				this.stepsToComplete = stepsToComplete;
			}

			@Override
			public boolean equals(Object otherObject) {
				if (this == otherObject) {
					return true;
				}
				if (otherObject == null || getClass() != otherObject.getClass()) {
					return false;
				}
				CompleteGridSignature otherCompleteGridSignature = (CompleteGridSignature) otherObject;
				return stepsToComplete == otherCompleteGridSignature.stepsToComplete && entryEvents.equals(otherCompleteGridSignature.entryEvents);
			}

			@Override
			public int hashCode() {
				if (hash == null) {
					hash = Objects.hash(entryEvents, stepsToComplete);
				}
				return hash;
			}

			@Override
			public String toString() {
				return "CompleteGridSignature{" + "entryEvents=" + entryEvents + ", stepsToComplete=" + stepsToComplete + '}';
			}
		}

		private static interface OutsideCoordinateReciever {
			void acceptOutsideCoordinate(Coordinate repetitionCoordinate, Coordinate gridCoordinate);
		}

		private final class CompleteReachGridInfo {
			private final int startStepNumber;
			private final byte signatureNumber;

			public CompleteReachGridInfo(int startStepNumber, byte signatureNumber) {
				this.startStepNumber = startStepNumber;
				this.signatureNumber = signatureNumber;
			}

			public int getStartStepNumber() {
				return this.startStepNumber;
			}

			public byte getSignatureNumber() {
				return this.signatureNumber;
			}
		}

		private final class ReachGrid {
			private final Coordinate repetitionCoordinate;
			private final Set<EntryEvent> entryEvents;
			private final byte[][] visited;
			private int visitedCount;
			private Set<Coordinate> activeCoordinates;
			private final int startStepNumber;
			private int completionStepNumber;

			public ReachGrid(Coordinate repetitionCoordinate, int startStepNumber) {
				this.repetitionCoordinate = repetitionCoordinate;
				this.visited = new byte[gridHeight][gridWidth];
				this.entryEvents = new HashSet<>();
				this.visitedCount = 0;
				this.activeCoordinates = new HashSet<>();
				this.startStepNumber = startStepNumber;
			}

			public void advanceInside(OutsideCoordinateReciever outsideCoordinateReciever, int stepNumber) {
				Set<Coordinate> sourceActiveCoordinates = activeCoordinates;
				activeCoordinates = new HashSet<>();
				for (Coordinate sourceActiveCoordinate : sourceActiveCoordinates) {
					int currentY = sourceActiveCoordinate.getY();
					int currentX = sourceActiveCoordinate.getX();
					if (visited[currentY][currentX] != 2) {
						visited[currentY][currentX] = 2;
						visitedCount++;
						for (Direction direction : DIRECTIONS) {
							int newY = currentY + direction.getYDelta();
							int newX = currentX + direction.getXDelta();
							int repetitionCoordinateYDelta = 0;
							int repetitionCoordinateXDelta = 0;
							if (newY == -1) {
								repetitionCoordinateYDelta = -1;
								newY = gridHeight-1;
							}
							else if (newY == gridHeight) {
								repetitionCoordinateYDelta = 1;
								newY = 0;
							}
							if (newX == -1) {
								repetitionCoordinateXDelta = -1;
								newX = gridWidth-1;
							}
							else if (newX == gridWidth) {
								repetitionCoordinateXDelta = 1;
								newX = 0;
							}
							if (gardenPlots[newY][newX]) {
								if (repetitionCoordinateYDelta == 0 && repetitionCoordinateXDelta == 0) {
									if (visited[newY][newX] == 0) {
										visited[newY][newX] = 1;
										activeCoordinates.add(new Coordinate(newY, newX));
									}
								}
								else {
									outsideCoordinateReciever.acceptOutsideCoordinate(new Coordinate(repetitionCoordinate.getY() + repetitionCoordinateYDelta, repetitionCoordinate.getX() + repetitionCoordinateXDelta), new Coordinate(newY, newX));
								}
							}
						}
					}
				}
				if (isComplete()) {
					completionStepNumber = stepNumber;
				}
			}

			public void acceptFromOutside(Coordinate coordinate, int stepNumber) {
				int y = coordinate.getY();
				int x = coordinate.getX();
				if (gardenPlots[y][x]) {
					if (visited[y][x] == 0) {
						visited[y][x] = 1;
						activeCoordinates.add(coordinate);
						entryEvents.add(new EntryEvent(stepNumber-startStepNumber,coordinate));
					}
				}
				if (isComplete()) {
					completionStepNumber = stepNumber;
					return;
				}
			}

			public boolean isStalledWithoutOutsideInput() {
				return this.activeCoordinates.isEmpty();
			}

			public int getVisitedCount() {
				return visitedCount;
			}

			public boolean isComplete() {
				return visitedCount == completeGridGardenPlotCount;
			}

			public int getStartStepNumber() {
				return startStepNumber;
			}

			public Coordinate getRepetitionCoordinate() {
				return repetitionCoordinate;
			}

			public CompleteGridSignature getCompleteGridSignature() {
				if (!isComplete()) {
					throw new IllegalStateException("Cannot get a complete grid signature when the grid is not complete.");
				}
				return new CompleteGridSignature(entryEvents, completionStepNumber - startStepNumber +1);
			}
		}

		private static final Direction[] DIRECTIONS = Direction.values();

		private final boolean[][] gardenPlots;
		private final int gridHeight;
		private final int gridWidth;
		private final Coordinate startRepetitionCoordinate;
		private final Coordinate startGridCoordinate;
		private final int completeGridGardenPlotCount;

		public Runner(boolean[][] gardenPlots, int gridHeight, int gridWidth, Coordinate startRepetitionCoordinate, Coordinate startGridCoordinate) {
			this.gardenPlots = gardenPlots;
			this.gridHeight = gridHeight;
			this.gridWidth = gridWidth;
			this.startRepetitionCoordinate = startRepetitionCoordinate;
			this.startGridCoordinate = startGridCoordinate;
			int completeGridGardenPlotCount = 0;
			ReachGrid reachGrid = new ReachGrid(startRepetitionCoordinate, 0);
			reachGrid.acceptFromOutside(startGridCoordinate,0);
			Set<Coordinate> wrappingGridCoordinates = new HashSet<>();
			while (true) {
				reachGrid.advanceInside((repetitionCoordinate, gridCoordinate) -> wrappingGridCoordinates.add(gridCoordinate),0);
				for (Coordinate gridCoordinate : wrappingGridCoordinates) {
					reachGrid.acceptFromOutside(gridCoordinate, 0);
				}
				if (reachGrid.isStalledWithoutOutsideInput()) {
					break;
				}
				wrappingGridCoordinates.clear();
			}
			this.completeGridGardenPlotCount = reachGrid.getVisitedCount();
		}

		public Object run() {
			Map<Coordinate, ReachGrid> incompleteGridsByRepetitionCoordinate = new HashMap<>();
			Map<Coordinate,CompleteReachGridInfo> completeGridInfo = new HashMap<>();
			Map<Coordinate, Set<Coordinate>> pendingOutsideCoordinates = new HashMap<>();
			Map<CompleteGridSignature,Byte> completeGridSignatureNumberLookup = new HashMap<>();
			int[] signatureNumberCounts = new int[64];
			Map<Byte,Map<Byte,Set<Integer>>> stepIncreaseBetweenGridSignatures = new HashMap<>();
			Map<CompleteGridSignature, SortedMap<Integer, Integer>> completeGridSignatureToRepetitionCoordinateListMap = new LinkedHashMap<>();
			ReachGrid startingReachGrid = new ReachGrid(startRepetitionCoordinate,0);
			startingReachGrid.acceptFromOutside(startGridCoordinate, 0);
			incompleteGridsByRepetitionCoordinate.put(startingReachGrid.getRepetitionCoordinate(), startingReachGrid);
			int stepsToSimulate = gridWidth*111;//completeGridGardenPlotCount*11;
			stepLoop: for (int stepsTaken=1; stepsTaken<=stepsToSimulate; stepsTaken++) {
				Iterator<ReachGrid> gridIterator = incompleteGridsByRepetitionCoordinate.values().iterator();
				while (gridIterator.hasNext()) {
					ReachGrid reachGrid = gridIterator.next();
					if (reachGrid.isComplete()) {
						gridIterator.remove();
						Coordinate repetitionCoordinate = reachGrid.getRepetitionCoordinate();
						int repetitionCoordinateY = repetitionCoordinate.getY();
						int repetitionCoordinateX = repetitionCoordinate.getX();
						pendingOutsideCoordinates.remove(repetitionCoordinate);
						CompleteGridSignature completeGridSignature = reachGrid.getCompleteGridSignature();
						Byte signatureNumberBoxed = completeGridSignatureNumberLookup.get(completeGridSignature);
						if (signatureNumberBoxed == null) {
							signatureNumberBoxed = (byte)completeGridSignatureNumberLookup.size();
							if (signatureNumberBoxed >= 64) {
								throw new IllegalStateException("Too many grid signatures");
							}
							completeGridSignatureNumberLookup.put(completeGridSignature,signatureNumberBoxed);
						}
						byte signatureNumber = (byte)signatureNumberBoxed;
						int startStepNumber = reachGrid.getStartStepNumber();
						CompleteReachGridInfo gridInfo = new CompleteReachGridInfo(startStepNumber,signatureNumber);
						completeGridInfo.put(repetitionCoordinate,gridInfo);
						for (Direction direction : DIRECTIONS) {
							CompleteReachGridInfo neighbourGridInfo = completeGridInfo.get(new Coordinate(repetitionCoordinateY+direction.getYDelta(),repetitionCoordinateX+direction.getXDelta()));
							if (neighbourGridInfo != null) {
								byte neighbourSignatureNumber = neighbourGridInfo.getSignatureNumber();
								byte lowerSignatureNumber;
								byte higherSignatureNumber;
								int neighbourToThisStepIncrease = startStepNumber-neighbourGridInfo.getStartStepNumber();
								int positiveSignatureNumber;
								if (neighbourToThisStepIncrease > 0) {
									lowerSignatureNumber = neighbourSignatureNumber;
									higherSignatureNumber = signatureNumber;
									positiveSignatureNumber = neighbourToThisStepIncrease;
								} else {
									lowerSignatureNumber = signatureNumber;
									higherSignatureNumber = neighbourSignatureNumber;
									positiveSignatureNumber = -neighbourToThisStepIncrease;
								}
								stepIncreaseBetweenGridSignatures.computeIfAbsent(lowerSignatureNumber,__ -> new HashMap<>()).computeIfAbsent(higherSignatureNumber,__ -> new HashSet<>()).add(positiveSignatureNumber);
							}
						}
						if (++signatureNumberCounts[signatureNumberBoxed] > 100) {
							Set<Integer> interestingCounts = new HashSet<>();
							for (Map.Entry<Byte,Map<Byte,Set<Integer>>> stepIncreaseBetweenGridSignaturesEntry1 : stepIncreaseBetweenGridSignatures.entrySet()) {
								if (signatureNumberCounts[stepIncreaseBetweenGridSignaturesEntry1.getKey()] > 5) {
									for (Map.Entry<Byte,Set<Integer>> stepIncreaseBetweenGridSignaturesEntry2 : stepIncreaseBetweenGridSignaturesEntry1.getValue().entrySet()) {
										if (signatureNumberCounts[stepIncreaseBetweenGridSignaturesEntry2.getKey()] > 5) {
											interestingCounts.addAll(stepIncreaseBetweenGridSignaturesEntry2.getValue());
										}
									}
								}
							}
							Map<String,Object> res = new HashMap<>();
							res.put("counts", Arrays.toString(Arrays.copyOf(signatureNumberCounts,completeGridSignatureNumberLookup.size())));
							res.put("increases", stepIncreaseBetweenGridSignatures);
							res.put("interest", interestingCounts);
							return res;
							//break stepLoop;
						}
					}
					else {
						reachGrid.advanceInside((repetitionCoordinate, gridCoordinate) -> pendingOutsideCoordinates.computeIfAbsent(repetitionCoordinate, __ -> new HashSet<>()).add(gridCoordinate), stepsTaken);
					}
				}
				for (Map.Entry<Coordinate,Set<Coordinate>> pendingOutsideCoordinatesEntry : pendingOutsideCoordinates.entrySet()) {
					Coordinate repetitionCoordinate = pendingOutsideCoordinatesEntry.getKey();
					if (!completeGridInfo.containsKey(repetitionCoordinate)) {
						ReachGrid reachGrid = incompleteGridsByRepetitionCoordinate.get(repetitionCoordinate);
						if (reachGrid == null) {
							reachGrid = new ReachGrid(repetitionCoordinate, stepsTaken);
							incompleteGridsByRepetitionCoordinate.put(repetitionCoordinate, reachGrid);
						}
						Set<Coordinate> gridCoordinates = pendingOutsideCoordinatesEntry.getValue();
						for (Coordinate gridCoordinate : gridCoordinates) {
							reachGrid.acceptFromOutside(gridCoordinate, stepsTaken);
						}
						gridCoordinates.clear();
					}
				}
			}
			return -1;
		}
	}

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		char[][] inputCharactersGrid = LineReader.charArraysArray(inputCharacters, true);
		int gridHeight = inputCharactersGrid.length;
		int gridWidth = inputCharactersGrid[0].length;
		boolean[][] gardenPlots = new boolean[gridHeight][gridWidth];
		Coordinate startCoordinate = null;
		for (int y=0; y<gridHeight; y++) {
			char[] inputCharactersGridRow = inputCharactersGrid[y];
			boolean[] gardenPlotsRow = gardenPlots[y];
			for (int x=0; x<gridWidth; x++) {
				char inputGridCharacter = inputCharactersGridRow[x];
				gardenPlotsRow[x] = inputGridCharacter != '#';
				if (inputGridCharacter == 'S') {;
					if (startCoordinate != null) {
						throw new IllegalStateException("Multiple start points");
					}
					startCoordinate = new Coordinate(y, x);
				}
			}
		}
		if (startCoordinate == null) {
			throw new IllegalStateException("No start point.");
		}
		printWriter.println("Debug: " + new Runner(gardenPlots,gridHeight,gridWidth,new Coordinate(0,0),startCoordinate).run());
		return new BasicPuzzleResults<>(
			null,
			null
		);
	}
}
