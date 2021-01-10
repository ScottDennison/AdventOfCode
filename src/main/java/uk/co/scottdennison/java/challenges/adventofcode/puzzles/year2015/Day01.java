package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2015;

import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.framework.ResultGetter;

import java.io.PrintWriter;

public final class Day01 implements IPuzzle {
	public static class Results implements IPuzzleResults {
		private final int targetFloor;
		private final Integer basementEnteredPosition;

		public Results(int targetFloor, Integer basementEnteredPosition) {
			this.targetFloor = targetFloor;
			this.basementEnteredPosition = basementEnteredPosition;
		}

		@ResultGetter
		public int getTargetFloor() {
			return this.targetFloor;
		}

		@ResultGetter
		public int getBasementEnteredPosition() {
			return this.basementEnteredPosition;
		}

		@Override
		public String getPartAAnswerString() {
			return Integer.toString(this.targetFloor);
		}

		@Override
		public String getPartBAnswerString() {
			if (this.basementEnteredPosition == null) {
				return "-1";
			} else {
				return Integer.toString(this.basementEnteredPosition);
			}
		}

		@Override
		public String getPartASummary() {
			return String.format("Target floor is: %d", this.targetFloor);
		}

		@Override
		public String getPartBSummary() {
			if (this.basementEnteredPosition == null) {
				return "The basement is never entered.";
			} else {
				return String.format("Basement entered at position: %d", this.basementEnteredPosition);
			}
		}
	}

	@Override
	public Day01.Results runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter progressWriter) {
		int floor = 0;
		Integer basementEnteredPosition = null;
		int inputCharacterCount = inputCharacters.length;
		for (int inputCharacterIndex = 0; inputCharacterIndex < inputCharacterCount; inputCharacterIndex++) {
			switch (inputCharacters[inputCharacterIndex]) {
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
				basementEnteredPosition = inputCharacterIndex + 1;
			}
		}
		if (basementEnteredPosition == null && !partBPotentiallyUnsolvable) {
			throw new IllegalStateException("The basement was never entered");
		}
		return new Day01.Results(floor, basementEnteredPosition);
	}
}
