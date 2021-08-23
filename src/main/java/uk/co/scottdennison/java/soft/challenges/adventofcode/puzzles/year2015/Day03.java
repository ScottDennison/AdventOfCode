package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2015;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzlePartResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzlePartResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.MultiPartPuzzleResults;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Day03 implements IPuzzle {
	private static final boolean LOG_PRESENTS = false;

	private static class House {
		private final int relativeX;
		private final int relativeY;
		private int presentCount;

		public House(int relativeX, int relativeY, int initialPresentCount) {
			this.relativeX = relativeX;
			this.relativeY = relativeY;
			this.presentCount = initialPresentCount;
		}

		public void givePresents(int presentCount) {
			this.presentCount += presentCount;
		}

		public int getRelativeX() {
			return this.relativeX;
		}

		public int getRelativeY() {
			return this.relativeY;
		}

		public int getPresentCount() {
			return this.presentCount;
		}
	}

	private static class GridManager {
		private final Map<Integer, Map<Integer, House>> houses = new HashMap<>();

		public House getHouse(int relativeX, int relativeY) {
			return this.houses.computeIfAbsent(relativeY, __ -> new HashMap<>()).computeIfAbsent(relativeX, __ -> new House(relativeX, relativeY, 0));
		}

		public Stream<House> getKnownHousesStream() {
			return this.houses.values().stream().map(Map::values).flatMap(Collection::stream);
		}
	}

	private static class SantaPositionTracker {
		private int relativeX;
		private int relativeY;

		public SantaPositionTracker() {
			this(0, 0);
		}

		public SantaPositionTracker(int initialRelativeX, int initialRelativeY) {
			this.relativeX = initialRelativeX;
			this.relativeY = initialRelativeY;
		}

		public void adjustPosition(int relativeXAddition, int relativeYAddition) {
			this.relativeX += relativeXAddition;
			this.relativeY += relativeYAddition;
		}

		public int getRelativeX() {
			return relativeX;
		}

		public int getRelativeY() {
			return relativeY;
		}
	}

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		SantaPositionTracker year1SantaPositionTracker = new SantaPositionTracker();
		SantaPositionTracker year2ActualSantaPositionTracker = new SantaPositionTracker();
		SantaPositionTracker year2RoboSantaPositionTracker = new SantaPositionTracker();
		GridManager year1GridManager = new GridManager();
		GridManager year2GridManager = new GridManager();
		handleVisit(year1GridManager, year1SantaPositionTracker);
		handleVisit(year2GridManager, year2ActualSantaPositionTracker);
		handleVisit(year2GridManager, year2RoboSantaPositionTracker);
		int inputCharacterCount = inputCharacters.length;
		for (int inputCharacterIndex = 0; inputCharacterIndex < inputCharacterCount; inputCharacterIndex++) {
			int relativeXAddition;
			int relativeYAddition;
			switch (inputCharacters[inputCharacterIndex]) {
				case '^':
					relativeXAddition = 0;
					relativeYAddition = -1;
					break;
				case 'v':
					relativeXAddition = 0;
					relativeYAddition = 1;
					break;
				case '<':
					relativeXAddition = -1;
					relativeYAddition = 0;
					break;
				case '>':
					relativeXAddition = 1;
					relativeYAddition = 0;
					break;
				default:
					throw new IllegalStateException("Unexpected character");
			}
			handleVisit(relativeXAddition, relativeYAddition, year1GridManager, year1SantaPositionTracker);
			handleVisit(relativeXAddition, relativeYAddition, year2GridManager, (inputCharacterIndex & 1) == 0 ? year2ActualSantaPositionTracker : year2RoboSantaPositionTracker);
		}
		return new MultiPartPuzzleResults<>(
			summarizePuzzlePart(1, year1GridManager, printWriter),
			summarizePuzzlePart(2, year2GridManager, printWriter)
		);
	}

	private static void handleVisit(GridManager gridManager, SantaPositionTracker santaPositionTracker) {
		gridManager.getHouse(santaPositionTracker.getRelativeX(), santaPositionTracker.getRelativeY()).givePresents(1);
	}

	private static void handleVisit(int relativeXAddition, int relativeYAddition, GridManager gridManager, SantaPositionTracker santaPositionTracker) {
		santaPositionTracker.adjustPosition(relativeXAddition, relativeYAddition);
		handleVisit(gridManager, santaPositionTracker);
	}

	public static IPuzzlePartResults summarizePuzzlePart(int year, GridManager gridManager, PrintWriter printWriter) {
		Set<House> visitedHousesWithPresents = gridManager.getKnownHousesStream().filter(house -> house.getPresentCount() > 0).collect(Collectors.toSet());
		if (LOG_PRESENTS) {
			printWriter.format("Year: %d%n", year);
			printWriter.format("\tHouses with at least one present: %d%n", visitedHousesWithPresents.size());
			SortedMap<Integer, List<House>> visitedHousesByPresentCount = visitedHousesWithPresents.stream()
				.sorted(
					Comparator
						.comparing(House::getRelativeY)
						.thenComparing(House::getRelativeX)
				)
				.collect(
					Collectors.groupingBy(
						House::getPresentCount,
						TreeMap::new,
						Collectors.toList()
					)
				);
			String coordinatePartFormat = String.format("%% %dd", (int) Math.ceil(Math.log10(IntStream.concat(visitedHousesWithPresents.stream().mapToInt(House::getRelativeX), visitedHousesWithPresents.stream().mapToInt(House::getRelativeY)).map(Math::abs).max().orElse(0) + 1)) + 1);
			String coordinateFormat = String.format("\t[ %s , %s ]%%n", coordinatePartFormat, coordinatePartFormat);
			for (Map.Entry<Integer, List<House>> visitedHousesByPresentCountEntry : visitedHousesByPresentCount.entrySet()) {
				printWriter.format("\tHouses with %d present(s):%n", visitedHousesByPresentCountEntry.getKey());
				for (House house : visitedHousesByPresentCountEntry.getValue()) {
					printWriter.format(coordinateFormat, house.getRelativeX(), house.getRelativeY());
				}
			}
		}
		return new BasicPuzzlePartResults<>(visitedHousesWithPresents.size());
	}
}
