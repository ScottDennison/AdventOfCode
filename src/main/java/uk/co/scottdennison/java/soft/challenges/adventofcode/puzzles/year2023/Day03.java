package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2023;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Day03 implements IPuzzle {
	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		char[][] inputGrid = LineReader.charArraysArray(inputCharacters, true);
		int width = inputGrid[0].length;
		int height = inputGrid.length;
		Map<Integer,Integer> partNumbersPerId = new HashMap<>();
		int[][] partIdsByGridPosition = new int[height][width];
		int numberLength = 0;
		boolean inNumber = false;
		int numberStartXInclusive = 0;
		int numberEndXExclusive = 0;
		for (int y=0; y<height; y++) {
			char[] inputGridRow = inputGrid[y];
			for (int x=0; x<width; x++) {
				char inputGridChar = inputGridRow[x];
				if (inputGridChar >= '0' && inputGridChar <= '9') {
					if (!inNumber) {
						inNumber = true;
						numberStartXInclusive = x;
					}
				} else {
					if (inNumber) {
						inNumber = false;
						numberEndXExclusive = x;
						checkForPartNumber(inputGrid, partIdsByGridPosition, partNumbersPerId, width, height, y, numberStartXInclusive, numberEndXExclusive);
					}
				}
			}
			if (inNumber) {
				inNumber = false;
				numberEndXExclusive = width;
				checkForPartNumber(inputGrid, partIdsByGridPosition, partNumbersPerId, width, height, y, numberStartXInclusive, numberEndXExclusive);
			}
		}
		int partNumbersTotal = 0;
		for (int partNumber : partNumbersPerId.values()) {
			partNumbersTotal += partNumber;
		}
		int totalGearRatio = 0;
		Set<Integer> partIdsAdjacent = new HashSet<>();
		for (int y=0; y<height; y++) {
			char[] inputGridRow = inputGrid[y];
			for (int x=0; x<width; x++) {
				if (inputGridRow[x] == '*') {
					partIdsAdjacent.clear();
					for (int yDelta=-1; yDelta<=1; yDelta++) {
						int ySearch = y+yDelta;
						int[] partIdsByGridPositionRow = partIdsByGridPosition[ySearch];
						if (ySearch >= 0 && ySearch < height) {
							for (int xDelta=-1; xDelta<=1; xDelta++) {
								int xSearch = x+xDelta;
								if (xSearch >= 0 && xSearch < width) {
									int potentialPartId = partIdsByGridPositionRow[xSearch];
									if (potentialPartId != 0) {
										partIdsAdjacent.add(potentialPartId);
									}
								}
							}
						}
					}
					if (partIdsAdjacent.size() == 2) {
						int gearRatio = 1;
						for (int partId : partIdsAdjacent) {
							gearRatio *= partNumbersPerId.get(partId);
						}
						totalGearRatio += gearRatio;
					}
				}
			}
		}
		return new BasicPuzzleResults<>(
			partNumbersTotal,
			totalGearRatio
		);
	}

	private static void checkForPartNumber(char[][] inputGrid, int[][] partIdsByGridPosition, Map<Integer,Integer> partNumbersPerId, int width, int height, int numberY, int numberStartXInclusive, int numberEndXExclusive) {
		for (int y=numberY-1; y<=numberY+1; y++) {
			if (y >= 0 && y < height) {
				char[] inputGridRow = inputGrid[y];
				for (int x = numberStartXInclusive - 1; x <= numberEndXExclusive; x++) {
					if (x >= 0 && x < width) {
						char inputGridChar = inputGridRow[x];
						if (!(inputGridChar >= '0' && inputGridChar <= '9') && inputGridChar != '.') {
							int partNumber = Integer.parseInt(new String(inputGrid[numberY],numberStartXInclusive,numberEndXExclusive-numberStartXInclusive));
							int partId = partNumbersPerId.size()+1;
							partNumbersPerId.put(partId,partNumber);
							int[] partIdsByGridPositionRow = partIdsByGridPosition[numberY];
							for (int partX=numberStartXInclusive; partX<numberEndXExclusive; partX++) {
								partIdsByGridPositionRow[partX] = partId;
							}
							return;
						}
					}
				}
			}
		}
	}
}
