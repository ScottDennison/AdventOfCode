package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2019;

import uk.co.scottdennison.java.libs.text.proessing.CaptialLetterAsciiArtProcessor;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Day08 implements IPuzzle {
	private static final int IMAGE_WIDTH = 25;
	private static final int IMAGE_HEIGHT = 6;

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		inputCharacters = new String(inputCharacters).trim().toCharArray();
		int imageSize = IMAGE_WIDTH * IMAGE_HEIGHT;
		if (inputCharacters.length % imageSize != 0) {
			throw new IllegalStateException("Incomplete image");
		}
		int gridCount = inputCharacters.length/imageSize;
		int[][][] grids = new int[gridCount][IMAGE_HEIGHT][IMAGE_WIDTH];
		int gridIndex = 0;
		int y = 0;
		int x = 0;
		for (char inputCharacter : inputCharacters) {
			int value;
			switch (inputCharacter) {
				case '0':
					value = 0;
					break;
				case '1':
					value = 1;
					break;
				case '2':
					value = 2;
					break;
				default:
					throw new IllegalStateException("Unexpected character");
			}
			grids[gridIndex][y][x] = value;
			if (++x == IMAGE_WIDTH) {
				x = 0;
				if (++y == IMAGE_HEIGHT) {
					y = 0;
					gridIndex++;
				}
			}
		}
		int[] digitCounts = new int[3];
		int minZeros = Integer.MAX_VALUE;
		int score = 0;
		boolean[][] finalImagePixels = new boolean[IMAGE_HEIGHT][IMAGE_WIDTH];
		boolean[][] finalImageLocked = new boolean[IMAGE_HEIGHT][IMAGE_WIDTH];
		for (gridIndex=0; gridIndex<gridCount; gridIndex++) {
			digitCounts[0] = digitCounts[1] = digitCounts[2] = 0;
			for (y=0; y<IMAGE_HEIGHT; y++) {
				for (x=0; x<IMAGE_WIDTH; x++) {
					int value = grids[gridIndex][y][x];
					digitCounts[value]++;
					if (!finalImageLocked[y][x]) {
						switch (value) {
							case 0:
								finalImagePixels[y][x] = false;
								finalImageLocked[y][x] = true;
								break;
							case 1:
								finalImagePixels[y][x] = true;
								finalImageLocked[y][x] = true;
								break;
							case 2:
								// Transparent
						}
					}
				}
			}
			if (digitCounts[0] < minZeros) {
				score = digitCounts[1] * digitCounts[2];
				minZeros = digitCounts[0];
			}
		}
		for (y=0; y<IMAGE_HEIGHT; y++) {
			for (x=0; x<IMAGE_WIDTH; x++) {
				printWriter.print(finalImagePixels[y][x]?'X':' ');
			}
			printWriter.println();
		}
		return new BasicPuzzleResults<>(
			score,
			new String(CaptialLetterAsciiArtProcessor.parse(finalImagePixels, IMAGE_HEIGHT, IMAGE_WIDTH)[0])
		);
	}
}

