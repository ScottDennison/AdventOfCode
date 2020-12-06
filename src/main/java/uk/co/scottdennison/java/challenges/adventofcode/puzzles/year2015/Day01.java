package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2015;

import uk.co.scottdennison.java.challenges.adventofcode.utils.InputFileUtils;

import java.io.IOException;
import java.nio.file.Files;

public class Day01 {
	public static void main(String[] args) throws IOException {
		int floor = 0;
		Integer basementEnteredPosition = null;
		byte[] fileBytes = Files.readAllBytes(InputFileUtils.getInputPath());
		int fileByteCount = fileBytes.length;
		for (int fileByteIndex = 0; fileByteIndex < fileByteCount; fileByteIndex++) {
			switch (fileBytes[fileByteIndex]) {
				case '(':
					floor++;
					break;
				case ')':
					floor--;
					break;
				default:
					throw new IllegalStateException("Unexpected character");
			}
			if (floor < 0 && basementEnteredPosition == null) {
				basementEnteredPosition = fileByteIndex + 1;
			}
		}
		if (basementEnteredPosition == null) {
			throw new IllegalStateException("The basement was never entered");
		}
		System.out.format("Target floor is: %d%n", floor);
		System.out.format("Basement entered at position: %d%n", basementEnteredPosition);
	}
}
