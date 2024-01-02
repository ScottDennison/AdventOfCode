package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2023;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Day21PartialAttempt2B implements IPuzzle {
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

	private static class CoordinatePair {
		private final Coordinate repetitionCoordinate;
		private final Coordinate gridCoordinate;

		public CoordinatePair(Coordinate repetitionCoordinate, Coordinate gridCoordinate) {
			this.repetitionCoordinate = repetitionCoordinate;
			this.gridCoordinate = gridCoordinate;
		}

		public Coordinate getRepetitionCoordinate() {
			return repetitionCoordinate;
		}

		public Coordinate getGridCoordinate() {
			return gridCoordinate;
		}

		@Override
		public boolean equals(Object otherObject) {
			if (this == otherObject) {
				return true;
			}
			if (otherObject == null || getClass() != otherObject.getClass()) {
				return false;
			}
			CoordinatePair otherCoordinatePair = (CoordinatePair) otherObject;
			return repetitionCoordinate.equals(otherCoordinatePair.repetitionCoordinate) && gridCoordinate.equals(otherCoordinatePair.gridCoordinate);
		}

		@Override
		public int hashCode() {
			return repetitionCoordinate.hashCode() ^ gridCoordinate.hashCode();
		}

		@Override
		public String toString() {
			return "CoordinatePair{" + "repetitionCoordinate=" + repetitionCoordinate + ", gridCoordinate=" + gridCoordinate + '}';
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

		private final class Grid {
			private final Coordinate repetitionCoordinate;
			private final Set<EntryEvent> entryEvents;
			private final byte[][] visited;
			private int visitedCount;
			private Set<Coordinate> activeCoordinates;
			private final int startTime;
			private int completionTime;

			public Grid(Coordinate repetitionCoordinate, int startTime) {
				this.repetitionCoordinate = repetitionCoordinate;
				this.visited = new byte[gridHeight][gridWidth];
				this.entryEvents = new HashSet<>();
				this.visitedCount = 0;
				this.activeCoordinates = new HashSet<>();
				this.startTime = startTime;
			}

			public void advanceInside(Set<CoordinatePair> outsideCoordinatesSink, int time) {
				Set<Coordinate> sourceActiveCoordinates = activeCoordinates;
				activeCoordinates = new HashSet<>();
				for (Coordinate sourceActiveCoordinate : sourceActiveCoordinates) {
					int currentY = sourceActiveCoordinate.getY();
					int currentX = sourceActiveCoordinate.getX();
					if (visited[currentY][currentX] != 2) {
						visited[currentY][currentX] = 2;
						visitedCount++;
						if (repetitionCoordinate.getX() == 0 && repetitionCoordinate.getY() == 0 && currentY == 0 && currentX >= 8) {
							System.out.println("hi1");
						}
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
									if ((repetitionCoordinate.getY() + repetitionCoordinateYDelta) == -1) {
										System.out.println("hi2");
									}
									outsideCoordinatesSink.add(new CoordinatePair(new Coordinate(repetitionCoordinate.getY() + repetitionCoordinateYDelta, repetitionCoordinate.getX() + repetitionCoordinateXDelta), new Coordinate(newY, newX)));
								}
							}
						}
					}
				}
				if (isComplete()) {
					completionTime = time;
				}
			}

			public void acceptFromOutside(Coordinate coordinate, int time) {
				int y = coordinate.getY();
				int x = coordinate.getX();
				if (gardenPlots[y][x]) {
					if (visited[y][x] == 0) {
						visited[y][x] = 1;
						activeCoordinates.add(coordinate);
						if (repetitionCoordinate.getY() == -1 && repetitionCoordinate.getX() == -2) {
							printWriter.println("Entry event at " + coordinate + " at " + time);
						}
						entryEvents.add(new EntryEvent(time-startTime,coordinate));
					}
				}
				if (isComplete()) {
					completionTime = time;
					return;
				}
			}

			public boolean isStalledWithoutOutputInput() {
				return this.activeCoordinates.isEmpty();
			}

			public int getVisitedCount() {
				return visitedCount;
			}

			public boolean isComplete() {
				return visitedCount == completeGridGardenPlotCount;
			}

			public int getStartTime() {
				return startTime;
			}

			public Coordinate getRepetitionCoordinate() {
				return repetitionCoordinate;
			}

			public CompleteGridSignature getCompleteGridSignature() {
				if (!isComplete()) {
					throw new IllegalStateException("Cannot get a complete grid signature when the grid is not complete.");
				}
				return new CompleteGridSignature(entryEvents,completionTime-startTime+1);
			}
		}

		private static final Direction[] DIRECTIONS = Direction.values();

		private final boolean[][] gardenPlots;
		private final int gridHeight;
		private final int gridWidth;
		private final CoordinatePair startCoordinatePair;
		private final int completeGridGardenPlotCount;

		private PrintWriter printWriter;

		public Runner(boolean[][] gardenPlots, int gridHeight, int gridWidth, CoordinatePair startCoordinatePair) {
			this.gardenPlots = gardenPlots;
			this.gridHeight = gridHeight;
			this.gridWidth = gridWidth;
			this.startCoordinatePair = startCoordinatePair;
			int completeGridGardenPlotCount = 0;
			Grid grid = new Grid(startCoordinatePair.getRepetitionCoordinate(), 0);
			grid.acceptFromOutside(startCoordinatePair.getGridCoordinate(),0);
			Set<CoordinatePair> outsideCoordinatePairsToIgnore = new HashSet<>();
			while (!grid.isStalledWithoutOutputInput()) {
				grid.advanceInside(outsideCoordinatePairsToIgnore,0);
			}
			this.completeGridGardenPlotCount = 63;//grid.getVisitedCount();
		}

		public void run(PrintWriter printWriter) {
			this.printWriter = printWriter;
			printWriter.println("CompleteGridGardenPlotCount: " + completeGridGardenPlotCount);
			if (completeGridGardenPlotCount != 63) {
				//return;
			}
			Map<Coordinate,Grid> incompleteGridsByRepetitionCoordinate = new HashMap<>();
			Set<Coordinate> completeGridRepetitionCoordinates = new HashSet<>();
			Set<CoordinatePair> pendingOutsideCoordinatesPairs = new HashSet<>();
			Map<Coordinate,Character> completeGridSignatureCharactersByRelativeCoordinate = new HashMap<>();
			Map<CompleteGridSignature,Character> completeGridSignatureCharacters = new HashMap<>();
			Map<CompleteGridSignature, SortedMap<Integer, Integer>> completeGridSignatureToRepetitionCoordinateListMap = new LinkedHashMap<>();
			Grid startingGrid = new Grid(startCoordinatePair.getRepetitionCoordinate(),0);
			startingGrid.acceptFromOutside(startCoordinatePair.getGridCoordinate(), 0);
			incompleteGridsByRepetitionCoordinate.put(startingGrid.getRepetitionCoordinate(),startingGrid);
			int stepsToSimulate = gridWidth*111;//completeGridGardenPlotCount*11;
			printWriter.println("Simulating " + stepsToSimulate + " steps");
			printWriter.flush();
			for (int stepsTaken=1; stepsTaken<=stepsToSimulate; stepsTaken++) {
				pendingOutsideCoordinatesPairs.clear();
				Iterator<Grid> gridIterator = incompleteGridsByRepetitionCoordinate.values().iterator();
				while (gridIterator.hasNext()) {
					Grid grid = gridIterator.next();
					if (grid.isComplete()) {
						gridIterator.remove();
						Coordinate repetitioncCoordinate = grid.getRepetitionCoordinate();
						completeGridRepetitionCoordinates.add(repetitioncCoordinate);
						CompleteGridSignature completeGridSignature = grid.getCompleteGridSignature();
						completeGridSignatureCharactersByRelativeCoordinate.put(repetitioncCoordinate,completeGridSignatureCharacters.computeIfAbsent(completeGridSignature,__ -> (char)(completeGridSignatureCharacters.size()+'A')));
						completeGridSignatureToRepetitionCoordinateListMap.computeIfAbsent(completeGridSignature,__ -> new TreeMap<>()).merge(grid.getStartTime(),1,Integer::sum);
						if (grid.getCompleteGridSignature().stepsToComplete == 19 && grid.getStartTime() == 22 && grid.getCompleteGridSignature().entryEvents.size() == 3) {
							printWriter.println("Matching Coordinate Pair: " + grid.getRepetitionCoordinate());
						}
					}
					else {
						grid.advanceInside(pendingOutsideCoordinatesPairs, stepsTaken);
					}
				}
				for (CoordinatePair pendingOutsideCoordinatesPair : pendingOutsideCoordinatesPairs) {
					Coordinate repetitionCoordinate = pendingOutsideCoordinatesPair.getRepetitionCoordinate();
					if (!completeGridRepetitionCoordinates.contains(repetitionCoordinate)) {
						Grid grid = incompleteGridsByRepetitionCoordinate.get(repetitionCoordinate);
						if (grid == null) {
							grid = new Grid(repetitionCoordinate, stepsTaken);
							incompleteGridsByRepetitionCoordinate.put(repetitionCoordinate, grid);
						}
						Coordinate gridCoordinate = pendingOutsideCoordinatesPair.getGridCoordinate();
						grid.acceptFromOutside(pendingOutsideCoordinatesPair.getGridCoordinate(), stepsTaken);
					}
				}
				if (stepsTaken <= stepsToSimulate) {
					IntSummaryStatistics repetitionYStats = incompleteGridsByRepetitionCoordinate.keySet().stream().mapToInt(Coordinate::getY).summaryStatistics();
					IntSummaryStatistics repetitionXStats = incompleteGridsByRepetitionCoordinate.keySet().stream().mapToInt(Coordinate::getX).summaryStatistics();
					int minRepetitionY = repetitionYStats.getMin();
					int maxRepetitionY = repetitionYStats.getMax();
					int minRepetitionX = repetitionXStats.getMin();
					int maxRepetitionX = repetitionXStats.getMax();
					printWriter.println("stepsTaken=" + stepsTaken + " / y=" + minRepetitionY + ",x=" + minRepetitionX + " to y=" + maxRepetitionY + ",x=" + maxRepetitionX);
					if (stepsTaken <= 150 || stepsTaken == stepsToSimulate) {
						char[][] displayGrid = new char[((maxRepetitionY-minRepetitionY)+1)*gridHeight][((maxRepetitionX-minRepetitionX)+1)*gridWidth];
						for (int repetitionY=minRepetitionY, displayRepetitionStartY=0; repetitionY<=maxRepetitionY; repetitionY++, displayRepetitionStartY+=gridHeight) {
							for (int repetitionX=minRepetitionX, displayRepetitionStartX=0; repetitionX<=maxRepetitionX; repetitionX++, displayRepetitionStartX+=gridHeight) {
								Coordinate repetitionCoordinate = new Coordinate(repetitionY, repetitionX);
								Grid grid = incompleteGridsByRepetitionCoordinate.get(repetitionCoordinate);
								char defaultGardenPlotChar;
								if (grid == null) {
									if (completeGridRepetitionCoordinates.contains(repetitionCoordinate)) {
										defaultGardenPlotChar='*';
									}
									else {
										defaultGardenPlotChar='.';
									}
								}
								else {
									defaultGardenPlotChar = '?';
								}
								for (int gridY=0, displayGridY=displayRepetitionStartY; gridY<gridHeight; gridY++, displayGridY++) {
									for (int gridX=0, displayGridX=displayRepetitionStartX; gridX<gridWidth; gridX++, displayGridX++) {
										char gridChar;
										if (gardenPlots[gridY][gridX]) {
											if (grid == null) {
												gridChar = defaultGardenPlotChar;
											}
											else if (grid.visited[gridY][gridX] == 2) {
												gridChar = '*';
											}
											else {
												gridChar = '.';
											}
										}
										else {
											gridChar = '#';
										}
										displayGrid[displayGridY][displayGridX] = gridChar;
									}
								}
								if (grid != null) {
									for (Coordinate activateCoordinate : grid.activeCoordinates) {
										displayGrid[displayRepetitionStartY+activateCoordinate.getY()][displayRepetitionStartX+activateCoordinate.getX()] = 'O';
									}
								}
							}
						}
						for (char[] displayGridLine : displayGrid) {
							printWriter.println(displayGridLine);
						}
						printWriter.println();
					}

					char[][] displayGrid = new char[maxRepetitionY-minRepetitionY+1][maxRepetitionX-minRepetitionX+1];
					for (int repetitionY=minRepetitionY, displayRepetitionY=0; repetitionY<=maxRepetitionY; repetitionY++, displayRepetitionY++) {
						for (int repetitionX=minRepetitionX, displayRepetitionX=0; repetitionX<=maxRepetitionX; repetitionX++, displayRepetitionX++) {
							Character character = completeGridSignatureCharactersByRelativeCoordinate.get(new Coordinate(repetitionY,repetitionX));
							if (character == null) {
								character = '.';
							}
							displayGrid[displayRepetitionY][displayRepetitionX] = character;
						}
					}
					for (char[] displayGridLine : displayGrid) {
						printWriter.println(displayGridLine);
					}
					printWriter.println();

					printWriter.println("----");
				}
			}
			if (completeGridSignatureToRepetitionCoordinateListMap.isEmpty()) {
				printWriter.println("No complete grid signatures");
			}
			else {
				for (Map.Entry<CompleteGridSignature,SortedMap<Integer, Integer>> completeGridSignatureToRepetitionCoordinateListMapEntry : completeGridSignatureToRepetitionCoordinateListMap.entrySet()) {
					printWriter.println("Signature: " + completeGridSignatureToRepetitionCoordinateListMapEntry.getKey());
					printWriter.println("Length: " + completeGridSignatureToRepetitionCoordinateListMapEntry.getKey().stepsToComplete);
					printWriter.println("Keys: " + completeGridSignatureToRepetitionCoordinateListMapEntry.getValue().keySet());
					printWriter.println("Values: " + completeGridSignatureToRepetitionCoordinateListMapEntry.getValue().values());
					printWriter.println();
				}
			}
			printWriter.flush();
		}
	}

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		char[][] inputCharactersGrid = LineReader.charArraysArray(inputCharacters, true);
		int gridHeight = inputCharactersGrid.length;
		int gridWidth = inputCharactersGrid[0].length;
		boolean[][] gardenPlots = new boolean[gridHeight][gridWidth];
		CoordinatePair startCoordinatePair = null;
		for (int y=0; y<gridHeight; y++) {
			char[] inputCharactersGridRow = inputCharactersGrid[y];
			boolean[] gardenPlotsRow = gardenPlots[y];
			for (int x=0; x<gridWidth; x++) {
				char inputGridCharacter = inputCharactersGridRow[x];
				gardenPlotsRow[x] = inputGridCharacter != '#';
				if (inputGridCharacter == 'S') {;
					if (startCoordinatePair != null) {
						throw new IllegalStateException("Multiple start points");
					}
					startCoordinatePair = new CoordinatePair(new Coordinate(0,0),new Coordinate(y, x));
				}
			}
		}
		if (startCoordinatePair == null) {
			throw new IllegalStateException("No start point.");
		}
		new Runner(gardenPlots,gridHeight,gridWidth,startCoordinatePair).run(printWriter);
		return new BasicPuzzleResults<>(
			null,
			null
		);
	}
}
