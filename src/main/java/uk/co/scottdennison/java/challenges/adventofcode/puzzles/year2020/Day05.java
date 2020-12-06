package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2020;

import uk.co.scottdennison.java.challenges.adventofcode.utils.InputFileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day05 {
	private static final int ROW_BSP_LENGTH = 7;
	private static final int COLUMN_BSP_LENGTH = 3;

	public static void main(String[] args) throws IOException {
		int minimumSeatId = Integer.MAX_VALUE;
		int maximumSeatId = Integer.MIN_VALUE;
		Integer mySeatId = null;
		Set<Integer> filledSeatIds = new HashSet<>();
		for (String line : Files.readAllLines(InputFileUtils.getInputPath())) {
			if (line.length() != 10) {
				throw new IllegalStateException("Unexpected line length");
			}
			int row = readBSP(line, 0, ROW_BSP_LENGTH, 'F', 'B');
			int column = readBSP(line, ROW_BSP_LENGTH, COLUMN_BSP_LENGTH, 'L', 'R');
			int seatId = (row << COLUMN_BSP_LENGTH) + column;
			filledSeatIds.add(seatId);
			if (seatId < minimumSeatId) {
				minimumSeatId = seatId;
			}
			if (seatId > maximumSeatId) {
				maximumSeatId = seatId;
			}
		}
		Set<Integer> unfilledSeatIds = IntStream.rangeClosed(minimumSeatId, maximumSeatId).boxed().filter(seatId -> !filledSeatIds.contains(seatId)).collect(Collectors.toSet());
		for (int unfilledSeatId : unfilledSeatIds) {
			if (filledSeatIds.contains(unfilledSeatId - 1) && filledSeatIds.contains(unfilledSeatId + 1)) {
				if (mySeatId == null) {
					mySeatId = unfilledSeatId;
				}
				else {
					throw new IllegalStateException("Duplicate seat ID found that could be my seat.");
				}
			}
		}
		if (mySeatId == null) {
			throw new IllegalStateException("No possible seat ID found for my seat.");
		}
		System.out.format("Largest seat id: %d%n", maximumSeatId);
		System.out.format("     My seat id: %d%n", mySeatId);
	}

	private static int readBSP(String line, int startCharIndex, int size, char lowerRangeCharacter, char upperRangeCharacter) {
		int bitFieldEntry = 1 << size;
		int bitField = 0;
		for (int count = 0; count < size; count++) {
			bitFieldEntry >>= 1;
			char character = line.charAt(startCharIndex + count);
			if (character == upperRangeCharacter) {
				bitField |= bitFieldEntry;
			}
			else if (character != lowerRangeCharacter) {
				throw new IllegalStateException("Unexpected character: " + character);
			}
		}
		return bitField;
	}
}
