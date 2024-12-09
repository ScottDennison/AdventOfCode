package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2016;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.common.CaptialLetterAsciiArtProcessor;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day08 implements IPuzzle {
	private static final Pattern PATTERN = Pattern.compile("^(?:(?:rect (?<rectWidth>[0-9]+)x(?<rectHeight>[0-9]+))|(?:(?:(?:rotate row y=(?<rotateY>[0-9]+))|(?:rotate column x=(?<rotateX>[0-9]+))) by (?<rotateAmount>[0-9]+)))$");

	private static final int SCREEN_WIDTH = 50;
	private static final int SCREEN_HEIGHT = 6;

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		int currentScreenIndex = 0;
		boolean[][][] screens = new boolean[2][SCREEN_HEIGHT][SCREEN_WIDTH];
		for (String inputLine : LineReader.strings(inputCharacters)) {
			Matcher matcher = PATTERN.matcher(inputLine);
			if (!matcher.matches()) {
				throw new IllegalStateException("Could not parse line.");
			}
			boolean[][] currentScreen = screens[currentScreenIndex];
			String temporaryGroupValue;
			if ((temporaryGroupValue = matcher.group("rotateAmount")) != null) {
				int newScreenIndex = (currentScreenIndex + 1) % 2;
				boolean[][] newScreen = screens[newScreenIndex];
				int rotateAmount = Integer.parseInt(temporaryGroupValue);
				int rotateY;
				int rotateX;
				if ((temporaryGroupValue = matcher.group("rotateY")) != null) {
					rotateY = Integer.parseInt(temporaryGroupValue);
					rotateX = -1;

				}
				else {
					rotateX = Integer.parseInt(matcher.group("rotateX"));
					rotateY = -1;
				}
				for (int y = 0; y < SCREEN_HEIGHT; y++) {
					boolean[] currentScreenRow = currentScreen[y];
					for (int x = 0; x < SCREEN_WIDTH; x++) {
						newScreen[(x == rotateX ? ((y + rotateAmount) % SCREEN_HEIGHT) : y)][(y == rotateY ? ((x + rotateAmount) % SCREEN_WIDTH) : x)] = currentScreenRow[x];
					}
				}
				currentScreenIndex = newScreenIndex;
			}
			else {
				int rectWidth = Integer.parseInt(matcher.group("rectWidth"));
				int rectHeight = Integer.parseInt(matcher.group("rectHeight"));
				for (int y = 0; y < rectHeight; y++) {
					boolean[] currentScreenRow = currentScreen[y];
					for (int x = 0; x < rectWidth; x++) {
						currentScreenRow[x] = true;
					}
				}
			}
		}
		boolean[][] finalScreen = screens[currentScreenIndex];
		int lightsOn = 0;
		for (int y=0; y<SCREEN_HEIGHT; y++) {
			for (int x=0; x<SCREEN_WIDTH; x++) {
				if (finalScreen[y][x]) {
					lightsOn++;
					printWriter.print('X');
				} else {
					printWriter.print(' ');
				}
			}
			printWriter.println();
		}
		return new BasicPuzzleResults<>(
			lightsOn,
			CaptialLetterAsciiArtProcessor.parse(finalScreen, SCREEN_HEIGHT, SCREEN_WIDTH, "\n")
		);
	}
}
