package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2020;

import uk.co.scottdennison.java.challenges.adventofcode.utils.InputFileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class Day11 {
	public static final RunInfo[] RUN_INFOS = {
		new RunInfo(4, false),
		new RunInfo(5, true)
	};

	private static final class RunInfo {
		private final int maximumNeighboursFilledCount;
		private final boolean skipOverMissingSeats;

		public RunInfo(int maximumNeighboursFilledCount, boolean skipOverMissingSeats) {
			this.maximumNeighboursFilledCount = maximumNeighboursFilledCount;
			this.skipOverMissingSeats = skipOverMissingSeats;
		}

		public int getMaximumNeighboursFilledCount() {
			return this.maximumNeighboursFilledCount;
		}

		public boolean isSkipOverMissingSeats() {
			return this.skipOverMissingSeats;
		}
	}

	public enum SeatState {
		FILLED,
		EMPTY,
		NO_SEAT
	}

	public static void main(String[] args) throws IOException {
		List<String> fileLines = Files.readAllLines(InputFileUtils.getInputPath());
		int rowCount = fileLines.size();
		int columnCount = fileLines.get(0).length();
		SeatState[][] initialSeatStateArray = new SeatState[rowCount][columnCount];
		for (int y = 0; y < rowCount; y++) {
			char[] fileLineCharacters = fileLines.get(y).toCharArray();
			if (fileLineCharacters.length != columnCount) {
				throw new IllegalStateException("Incorrect file line length.");
			}
			SeatState[] initialSeatStateRow = initialSeatStateArray[y];
			for (int x = 0; x < columnCount; x++) {
				SeatState seatState;
				switch (fileLineCharacters[x]) {
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
		SeatState[][][] seatStateArrays = new SeatState[2][rowCount][columnCount];
		for (RunInfo runInfo : RUN_INFOS) {
			int maximumNeighboursFilledCount = runInfo.getMaximumNeighboursFilledCount();
			boolean skipOverMissingSeats = runInfo.isSkipOverMissingSeats();
			int currentSeatStateArrayIndex = 0;
			SeatState[][] currentSeatStateArray = seatStateArrays[currentSeatStateArrayIndex];
			for (int y = 0; y < rowCount; y++) {
				SeatState[] initialSeatStateRow = initialSeatStateArray[y];
				SeatState[] currentSeatStateRow = currentSeatStateArray[y];
				System.arraycopy(initialSeatStateRow, 0, currentSeatStateRow, 0, columnCount);
			}
			SeatState[][] newSeatStateArray;
			int seatsFilled;
			while (true) {
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
			System.out.format("Seats filled once stable with maximumNeighboursFilledCount=%d and skipOverMissingSeats=%b: %d%n", maximumNeighboursFilledCount, skipOverMissingSeats, seatsFilled);
		}
	}
}
