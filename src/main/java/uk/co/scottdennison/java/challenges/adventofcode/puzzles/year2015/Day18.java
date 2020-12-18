package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2015;

import uk.co.scottdennison.java.challenges.adventofcode.utils.InputFileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class Day18 {
	public enum LightState {
		ON(true),
		OFF(false),
		FIXED_ON(true);

		private final boolean on;

		LightState(boolean on) {
			this.on = on;
		}

		public boolean isOn() {
			return this.on;
		}
	}

	private static final int ITERATIONS = 100;

	public static void main(String[] args) throws IOException {
		List<String> fileLines = Files.readAllLines(InputFileUtils.getInputPath());
		int rowCount = fileLines.size();
		int columnCount = fileLines.get(0).length();
		LightState[][] initialLightStateArray = new LightState[rowCount][columnCount];
		for (int y = 0; y < rowCount; y++) {
			char[] fileLineCharacters = fileLines.get(y).toCharArray();
			if (fileLineCharacters.length != columnCount) {
				throw new IllegalStateException("Incorrect file line length.");
			}
			LightState[] initialLightStateRow = initialLightStateArray[y];
			for (int x = 0; x < columnCount; x++) {
				LightState lightState;
				switch (fileLineCharacters[x]) {
					case '#':
						lightState = LightState.ON;
						break;
					case '.':
						lightState = LightState.OFF;
						break;
					default:
						throw new IllegalStateException("Unexpected character.");
				}
				initialLightStateRow[x] = lightState;
			}
		}
		outputSummary("", initialLightStateArray, rowCount, columnCount);
		initialLightStateArray[0][0] = LightState.FIXED_ON;
		initialLightStateArray[0][columnCount - 1] = LightState.FIXED_ON;
		initialLightStateArray[rowCount - 1][0] = LightState.FIXED_ON;
		initialLightStateArray[rowCount - 1][columnCount - 1] = LightState.FIXED_ON;
		outputSummary(" with the 4 corner lights fixed on", initialLightStateArray, rowCount, columnCount);
	}

	private static void outputSummary(String summaryAddition, LightState[][] initialLightStateArray, int rowCount, int columnCount) {
		System.out.format("There are %d lights on after %d iterations%s%n", runSimulation(initialLightStateArray, rowCount, columnCount), ITERATIONS, summaryAddition);
	}

	private static int runSimulation(LightState[][] initialLightStateArray, int rowCount, int columnCount) {
		LightState[][][] lightStateArrays = new LightState[2][rowCount][columnCount];
		int currentLightStateArrayIndex = 0;
		LightState[][] currentLightStateArray = lightStateArrays[currentLightStateArrayIndex];
		for (int y = 0; y < rowCount; y++) {
			LightState[] initialLightStateRow = initialLightStateArray[y];
			LightState[] currentLightStateRow = currentLightStateArray[y];
			System.arraycopy(initialLightStateRow, 0, currentLightStateRow, 0, columnCount);
		}
		LightState[][] newLightStateArray;
		int lightsOnCount;
		int iterationCount = 0;
		while (true) {
			iterationCount++;
			lightsOnCount = 0;
			int newLightStateArrayIndex = ((currentLightStateArrayIndex + 1) % 2);
			currentLightStateArray = lightStateArrays[currentLightStateArrayIndex];
			newLightStateArray = lightStateArrays[newLightStateArrayIndex];
			for (int y = 0; y < rowCount; y++) {
				LightState[] newLightStateRow = newLightStateArray[y];
				for (int x = 0; x < columnCount; x++) {
					LightState currentLightState = currentLightStateArray[y][x];
					LightState newLightState;
					if (currentLightState == LightState.FIXED_ON) {
						newLightState = LightState.FIXED_ON;
					}
					else {
						int neighboursOnCount = 0;
						for (int yd = -1; yd <= 1; yd++) {
							for (int xd = -1; xd <= 1; xd++) {
								if (!(yd == 0 && xd == 0)) {
									int yr = y + yd;
									int xr = x + xd;
									if (yr >= 0 && yr < rowCount && xr >= 0 && xr < columnCount) {
										if (currentLightStateArray[yr][xr].isOn()) {
											neighboursOnCount++;
										}
									}
								}
							}
						}
						if (currentLightState == LightState.ON && !(neighboursOnCount == 2 || neighboursOnCount == 3)) {
							newLightState = LightState.OFF;
						}
						else if (currentLightState == LightState.OFF && neighboursOnCount == 3) {
							newLightState = LightState.ON;
						}
						else {
							newLightState = currentLightState;
						}
					}
					newLightStateRow[x] = newLightState;
				}
			}
			boolean noChange = true;
			for (int y = 0; y < rowCount; y++) {
				LightState[] currentLightStateRow = currentLightStateArray[y];
				LightState[] newLightStateRow = newLightStateArray[y];
				for (int x = 0; x < columnCount; x++) {
					if (newLightStateRow[x].isOn()) {
						lightsOnCount++;
					}
					if (noChange && currentLightStateRow[x] != newLightStateRow[x]) {
						noChange = false;
					}
				}
			}
			currentLightStateArrayIndex = newLightStateArrayIndex;
			if (noChange || iterationCount >= 100) {
				break;
			}
		}
		return lightsOnCount;
	}
}
