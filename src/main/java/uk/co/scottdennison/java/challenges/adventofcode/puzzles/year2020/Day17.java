package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2020;

import uk.co.scottdennison.java.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleResults;
import uk.co.scottdennison.java.libs.text.input.LineReader;

import java.io.PrintWriter;
import java.util.Arrays;

public class Day17 implements IPuzzle {
	private static class State {
		private final int dimensionCount;
		private final int[] dimensionSizes;
		private final int[] dimensionMultipliers;
		private final boolean[] flatCellStates;

		public State(int[] dimensionSizes) {
			this(dimensionSizes, null);
		}

		public State(int[] dimensionSizes, boolean[] flatCellStates) {
			if (dimensionSizes == null) {
				throw new IllegalArgumentException("Dimension sizes must not be NULL.");
			}

			int dimensionCount = dimensionSizes.length;
			this.dimensionCount = dimensionCount;
			this.dimensionSizes = Arrays.copyOf(dimensionSizes, dimensionCount);

			int[] dimensionMultipliers = new int[dimensionCount];
			int dimensionMultiplier = 1;
			for (int dimension = dimensionCount - 1; dimension >= 0; dimension--) {
				dimensionMultipliers[dimension] = dimensionMultiplier;
				int dimensionSize = this.dimensionSizes[dimension];
				if (dimensionSize < 1) {
					throw new IllegalStateException("Illegal dimension size.");
				}
				dimensionMultiplier *= dimensionSize;
			}
			this.dimensionMultipliers = dimensionMultipliers;

			if (flatCellStates == null) {
				this.flatCellStates = new boolean[dimensionMultiplier];
			}
			else {
				int flatStateSize = flatCellStates.length;

				if (flatStateSize != dimensionMultiplier) {
					throw new IllegalStateException("Invalid flat state size");
				}

				this.flatCellStates = Arrays.copyOf(flatCellStates, flatStateSize);
			}
		}

		public int getDimensionSize(int dimension) {
			if (dimension < 0 || dimension > dimensionCount) {
				throw new IllegalStateException("Invalid dimension");
			}
			return this.dimensionSizes[dimension];
		}

		public boolean getCellState(int[] dimensionIndices) {
			return this.flatCellStates[this.getFlatCellIndex(dimensionIndices)];
		}

		public void setCellState(int[] dimensionIndices, boolean cellState) {
			this.flatCellStates[this.getFlatCellIndex(dimensionIndices)] = cellState;
		}

		private int getFlatCellIndex(int[] dimensionIndices) {
			if (dimensionIndices == null) {
				throw new IllegalArgumentException("Dimension indices must not be NULL.");
			}
			int dimensionCount = dimensionIndices.length;
			if (dimensionCount != this.dimensionCount) {
				throw new IllegalStateException("Invalid dimension count.");
			}
			int flatCellIndex = 0;
			for (int dimension = dimensionCount - 1; dimension >= 0; dimension--) {
				int dimensionIndex = dimensionIndices[dimension];
				if (dimensionIndex < 0 || dimensionIndex > this.dimensionSizes[dimension]) {
					throw new IllegalStateException("Invalid dimension " + dimension);
				}
				flatCellIndex += dimensionIndex * this.dimensionMultipliers[dimension];
			}
			return flatCellIndex;
		}

		public int getCellsOnCount() {
			int cellsOnCount = 0;
			for (boolean flatCellState : this.flatCellStates) {
				cellsOnCount += flatCellState ? 1 : 0;
			}
			return cellsOnCount;
		}
	}

	private static final int CYCLES = 6;

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		char[][] inputLines = LineReader.charArraysArray(inputCharacters, true);
		int height = inputLines.length;
		if (height < 1) {
			throw new IllegalStateException("No height.");
		}
		int width = inputLines[0].length;
		if (width < 1) {
			throw new IllegalStateException("No width.");
		}
		boolean[][] initialState = new boolean[height][width];
		for (int y = 0; y < height; y++) {
			char[] inputLineCharacters = inputLines[y];
			if (inputLineCharacters.length != width) {
				throw new IllegalStateException("Unexpected width.");
			}
			for (int x = 0; x < width; x++) {
				boolean cellState;
				switch (inputLineCharacters[x]) {
					case '#':
						cellState = true;
						break;
					case '.':
						cellState = false;
						break;
					default:
						throw new IllegalStateException("Unexpected character");
				}
				initialState[y][x] = cellState;
			}
		}
		return new BasicPuzzleResults<>(
			simulateEnergySource(initialState, height, width, 3),
			simulateEnergySource(initialState, height, width, 4)
		);
	}

	private static int simulateEnergySource(boolean[][] initialState, int height, int width, int dimensionCount) {
		int[] dimensionSizes = new int[dimensionCount];
		Arrays.fill(dimensionSizes, 0, dimensionCount - 2, 1);
		dimensionSizes[dimensionCount - 2] = height;
		dimensionSizes[dimensionCount - 1] = width;
		boolean[] flatCellStates = new boolean[height * width];
		for (int y = 0; y < height; y++) {
			System.arraycopy(initialState[y], 0, flatCellStates, (y * height), width);
		}
		State state = new State(dimensionSizes, flatCellStates);
		for (int cycle = 1; cycle <= CYCLES; cycle++) {
			int[] newDimensionSizes = new int[dimensionCount];
			for (int dimension = 0; dimension < dimensionCount; dimension++) {
				newDimensionSizes[dimension] = state.getDimensionSize(dimension) + 2;
			}
			State newState = new State(newDimensionSizes);
			simulateEnergySourceRecurseNewDimensions(state, newState, new int[dimensionCount], 0);
			state = newState;
		}
		return state.getCellsOnCount();
	}

	private static void simulateEnergySourceRecurseNewDimensions(State oldState, State newState, int[] newDimensionIndices, int dimension) {
		int dimensionCount = newDimensionIndices.length;
		if (dimension >= dimensionCount) {
			int[] oldDimensionIndicesForCurrentState = new int[dimensionCount];
			boolean oldDimensionsValid = true;
			for (dimension = 0; dimension < dimensionCount; dimension++) {
				int oldDimensionIndex = newDimensionIndices[dimension] - 1;
				if (oldDimensionIndex < 0 || oldDimensionIndex >= oldState.getDimensionSize(dimension)) {
					oldDimensionsValid = false;
					break;
				}
				oldDimensionIndicesForCurrentState[dimension] = oldDimensionIndex;
			}
			boolean oldCellState = oldDimensionsValid && oldState.getCellState(oldDimensionIndicesForCurrentState);
			int neighbourCellsOnCount = simulateEnergySourceRecurseOldDimensionsForNeighbourCellsOnCount(oldState, new int[dimensionCount], newDimensionIndices, 0);
			boolean newCellState;
			if (oldCellState && (neighbourCellsOnCount < 2 || neighbourCellsOnCount > 3)) {
				newCellState = false;
			}
			else if (!oldCellState && neighbourCellsOnCount == 3) {
				newCellState = true;
			}
			else {
				newCellState = oldCellState;
			}
			newState.setCellState(newDimensionIndices, newCellState);
		}
		else {
			int newDimensionSize = newState.getDimensionSize(dimension);
			int nextDimension = dimension + 1;
			for (int newDimensionIndex = 0; newDimensionIndex < newDimensionSize; newDimensionIndex++) {
				newDimensionIndices[dimension] = newDimensionIndex;
				simulateEnergySourceRecurseNewDimensions(oldState, newState, newDimensionIndices, nextDimension);
			}
		}
	}

	private static int simulateEnergySourceRecurseOldDimensionsForNeighbourCellsOnCount(State oldState, int[] oldDimensionIndices, int[] newDimensionIndices, int dimension) {
		int dimensionCount = oldDimensionIndices.length;
		if (dimension >= dimensionCount) {
			boolean refersToSameCell = true;
			for (dimension = 0; dimension < dimensionCount; dimension++) {
				if ((oldDimensionIndices[dimension] + 1) != newDimensionIndices[dimension]) {
					refersToSameCell = false;
					break;
				}
			}
			if (refersToSameCell) {
				return 0;
			}
			else {
				return oldState.getCellState(oldDimensionIndices) ? 1 : 0;
			}
		}
		else {
			int baseOldDimensionIndex = newDimensionIndices[dimension] - 1;
			int oldDimensionSize = oldState.getDimensionSize(dimension);
			int nextDimension = dimension + 1;
			int neighbourCellsOnCount = 0;
			for (int dimensionIndexOffset = -1; dimensionIndexOffset <= 1; dimensionIndexOffset++) {
				int oldDimensionIndex = baseOldDimensionIndex + dimensionIndexOffset;
				if (oldDimensionIndex >= 0 && oldDimensionIndex < oldDimensionSize) {
					oldDimensionIndices[dimension] = oldDimensionIndex;
					neighbourCellsOnCount += simulateEnergySourceRecurseOldDimensionsForNeighbourCellsOnCount(oldState, oldDimensionIndices, newDimensionIndices, nextDimension);
				}
			}
			return neighbourCellsOnCount;
		}
	}
}
