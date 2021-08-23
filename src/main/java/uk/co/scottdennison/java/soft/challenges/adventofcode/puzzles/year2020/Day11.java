package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2020;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzlePartResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzlePartResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.MultiPartPuzzleResults;
import uk.co.scottdennison.java.libs.text.input.LineReader;

import java.io.PrintWriter;

public class Day11 implements IPuzzle {
	public enum SeatState {
		FILLED,
		EMPTY,
		NO_SEAT
	}

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		char[][] inputLines = LineReader.charArraysArray(inputCharacters, true);
		int rowCount = inputLines.length;
		int columnCount = inputLines[0].length;
		SeatState[][] initialSeatStateArray = new SeatState[rowCount][columnCount];
		for (int y = 0; y < rowCount; y++) {
			char[] inputLineCharacters = inputLines[y];
			if (inputLineCharacters.length != columnCount) {
				throw new IllegalStateException("Incorrect input line length.");
			}
			SeatState[] initialSeatStateRow = initialSeatStateArray[y];
			for (int x = 0; x < columnCount; x++) {
				SeatState seatState;
				switch (inputLineCharacters[x]) {
					case '#':
						seatState = SeatState.FILLED;
						break;
					case 'L':
						seatState = SeatState.EMPTY;
						break;
					case '.':
						seatState = SeatState.NO_SEAT;
						break;
					default:
						throw new IllegalStateException("Unexpected character.");
				}
				initialSeatStateRow[x] = seatState;
			}
		}
		return new MultiPartPuzzleResults<>(
			runSimulation(initialSeatStateArray, rowCount, columnCount, printWriter, 4, false),
			runSimulation(initialSeatStateArray, rowCount, columnCount, printWriter, 5, true)
		);
	}

	private static IPuzzlePartResults runSimulation(SeatState[][] initialSeatStateArray, int rowCount, int columnCount, PrintWriter printWriter, int maximumNeighboursFilledCount, boolean skipOverMissingSeats) {
		SeatState[][][] seatStateArrays = new SeatState[2][rowCount][columnCount];
		int currentSeatStateArrayIndex = 0;
		SeatState[][] currentSeatStateArray = seatStateArrays[currentSeatStateArrayIndex];
		for (int y = 0; y < rowCount; y++) {
			SeatState[] initialSeatStateRow = initialSeatStateArray[y];
			SeatState[] currentSeatStateRow = currentSeatStateArray[y];
			System.arraycopy(initialSeatStateRow, 0, currentSeatStateRow, 0, columnCount);
		}
		SeatState[][] newSeatStateArray;
		int seatsFilled;
		int iterationCount = 0;
		while (true) {
			iterationCount++;
			seatsFilled = 0;
			int newSeatStateArrayIndex = ((currentSeatStateArrayIndex + 1) % 2);
			currentSeatStateArray = seatStateArrays[currentSeatStateArrayIndex];
			newSeatStateArray = seatStateArrays[newSeatStateArrayIndex];
			for (int y = 0; y < rowCount; y++) {
				SeatState[] newSeatStateRow = newSeatStateArray[y];
				for (int x = 0; x < columnCount; x++) {
					SeatState currentSeatState = currentSeatStateArray[y][x];
					SeatState newSeatState;
					if (currentSeatState == SeatState.NO_SEAT) {
						newSeatState = SeatState.NO_SEAT;
					}
					else {
						int neighboursFilledCount = 0;
						for (int yd = -1; yd <= 1; yd++) {
							for (int xd = -1; xd <= 1; xd++) {
								if (!(yd == 0 && xd == 0)) {
									int yr = y;
									int xr = x;
									while (true) {
										yr += yd;
										xr += xd;
										if (yr < 0 || yr >= rowCount || xr < 0 || xr >= columnCount) {
											break;
										}
										SeatState thisSeatState = currentSeatStateArray[yr][xr];
										if (thisSeatState == SeatState.FILLED) {
											neighboursFilledCount++;
											break;
										}
										else if (!(thisSeatState == SeatState.NO_SEAT && skipOverMissingSeats)) {
											break;
										}
									}
								}
							}
						}
						if (currentSeatState == SeatState.EMPTY && neighboursFilledCount == 0) {
							newSeatState = SeatState.FILLED;
						}
						else if (currentSeatState == SeatState.FILLED && neighboursFilledCount >= maximumNeighboursFilledCount) {
							newSeatState = SeatState.EMPTY;
						}
						else {
							newSeatState = currentSeatState;
						}
					}
					newSeatStateRow[x] = newSeatState;
				}
			}
			boolean noChange = true;
			for (int y = 0; y < rowCount; y++) {
				SeatState[] currentSeatStateRow = currentSeatStateArray[y];
				SeatState[] newSeatStateRow = newSeatStateArray[y];
				for (int x = 0; x < columnCount; x++) {
					if (newSeatStateRow[x] == SeatState.FILLED) {
						seatsFilled++;
					}
					if (noChange && currentSeatStateRow[x] != newSeatStateRow[x]) {
						noChange = false;
					}
				}
			}
			currentSeatStateArrayIndex = newSeatStateArrayIndex;
			if (noChange) {
				break;
			}
		}
		printWriter.format("Seats filled once stable (after %d iterations) with maximumNeighboursFilledCount=%d and skipOverMissingSeats=%b: %d%n", iterationCount, maximumNeighboursFilledCount, skipOverMissingSeats, seatsFilled);
		printWriter.flush();
		return new BasicPuzzlePartResults<>(seatsFilled);
	}
}
