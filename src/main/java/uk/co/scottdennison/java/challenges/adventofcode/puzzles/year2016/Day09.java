package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2016;

import uk.co.scottdennison.java.challenges.adventofcode.utils.InputFileUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.PriorityQueue;

public class Day09 {
	private static class MultiplierRemoval implements Comparable<MultiplierRemoval> {
		private final long compressedLengthToRemoveAt;
		private final long multiplierDivision;

		public MultiplierRemoval(long compressedLengthToRemoveAt, long multiplierDivision) {
			this.compressedLengthToRemoveAt = compressedLengthToRemoveAt;
			this.multiplierDivision = multiplierDivision;
		}

		public long getCompressedLengthToRemoveAt() {
			return this.compressedLengthToRemoveAt;
		}

		public long getMultiplierDivision() {
			return this.multiplierDivision;
		}

		@Override
		public int compareTo(MultiplierRemoval otherMultiplierRemoval) {
			int comparison = Long.compare(this.compressedLengthToRemoveAt, otherMultiplierRemoval.compressedLengthToRemoveAt);
			if (comparison == 0) {
				comparison = Long.compare(this.multiplierDivision, otherMultiplierRemoval.multiplierDivision);
			}
			return comparison;
		}
	}

	private static class Model {
		private final boolean allowNestedInstructions;

		private final PriorityQueue<MultiplierRemoval> pendingMultiplierRemovals = new PriorityQueue<>();
		private final StringBuilder characterCountStringBuilder = new StringBuilder();
		private final StringBuilder repetitionCountStringBuilder = new StringBuilder();

		private State previousState = State.NORMAL_INPUT;
		private State nextState = State.NORMAL_INPUT;
		private long compressedLength = 0;
		private long decompressedLength = 0;
		private long multiplier = 1;
		private long forcedStandardCharacterCount = 0;

		public Model(boolean allowNestedInstructions) {
			this.allowNestedInstructions = allowNestedInstructions;
		}

		public boolean isAllowNestedInstructions() {
			return this.allowNestedInstructions;
		}

		public State getNextState() {
			return this.nextState;
		}

		public void setNextState(State nextState) {
			this.nextState = nextState;
		}

		public void addCharacterCountCharacter(char character) {
			this.characterCountStringBuilder.append(character);
		}

		public long getCharacterCount() {
			return Long.parseLong(this.characterCountStringBuilder.toString());
		}

		public void resetCharacterCount() {
			this.characterCountStringBuilder.setLength(0);
		}

		public void addRepetitionCountCharacter(char character) {
			this.repetitionCountStringBuilder.append(character);
		}

		public long getRepetitionCount() {
			return Long.parseLong(this.repetitionCountStringBuilder.toString());
		}

		public void resetRepetitionCount() {
			this.repetitionCountStringBuilder.setLength(0);
		}

		public void insertDecompressionLengthMultiplier(long multiplier, long forNCompressedCharacters) {
			this.pendingMultiplierRemovals.add(new MultiplierRemoval(this.compressedLength + forNCompressedCharacters, multiplier));
			this.multiplier *= multiplier;
		}

		public void setForceStandardCharacterCount(long count) {
			this.forcedStandardCharacterCount = count;
		}

		public void processStandardCharacter() {
			this.decompressedLength += this.multiplier;
			if (this.forcedStandardCharacterCount > 0) {
				this.forcedStandardCharacterCount--;
			}
		}

		@SuppressWarnings("BooleanMethodIsAlwaysInverted")
		public boolean isForceStandardCharacter() {
			return this.forcedStandardCharacterCount > 0;
		}

		public void postProcessCompressedCharacter() {
			this.compressedLength++;
			while (true) {
				MultiplierRemoval multiplierRemoval = this.pendingMultiplierRemovals.peek();
				if (multiplierRemoval == null || multiplierRemoval.getCompressedLengthToRemoveAt() >= this.compressedLength) {
					break;
				}
				if (!this.previousState.isMultiplierAllowedToDecreaseInThisState()) {
					throw new IllegalStateException("Attempting to decrease multiplier when disallowed");
				}
				this.pendingMultiplierRemovals.remove(multiplierRemoval);
				this.multiplier /= multiplierRemoval.getMultiplierDivision();
			}
			this.previousState = this.nextState;
		}

		public boolean isInputAllowedToEnd() {
			return this.nextState.isInputAllowedToEndInThisState() && !this.isForceStandardCharacter();
		}

		public long getDecompressedLength() {
			return this.decompressedLength;
		}
	}

	private enum State {
		NORMAL_INPUT(true, true) {
			@Override
			public void process(Model model, char character) {
				if (!model.isForceStandardCharacter() && character == '(') {
					model.resetCharacterCount();
					model.setNextState(CHARACTER_COUNT_INPUT);
				}
				else {
					model.processStandardCharacter();
				}
			}
		},
		CHARACTER_COUNT_INPUT(false, false) {
			@Override
			public void process(Model model, char character) {
				if (character >= '0' && character <= '9') {
					model.addCharacterCountCharacter(character);
				}
				else if (character == 'x' || character == 'X') {
					model.resetRepetitionCount();
					model.setNextState(REPETITION_COUNT_INPUT);
				}
				else {
					throw new IllegalStateException("Unexpected character");
				}
			}
		},
		REPETITION_COUNT_INPUT(false, false) {
			@Override
			public void process(Model model, char character) {
				if (character >= '0' && character <= '9') {
					model.addRepetitionCountCharacter(character);
				}
				else if (character == ')') {
					long characterCount = model.getCharacterCount();
					long repetitionCount = model.getRepetitionCount();
					model.resetCharacterCount();
					model.resetRepetitionCount();
					model.insertDecompressionLengthMultiplier(repetitionCount, characterCount + 1);
					if (!model.isAllowNestedInstructions()) {
						model.setForceStandardCharacterCount(characterCount);
					}
					model.setNextState(NORMAL_INPUT);
				}
				else {
					throw new IllegalStateException("Unexpected character");
				}
			}
		};

		private final boolean inputAllowedToEnd;
		private final boolean multiplierAllowedToDecrease;

		State(boolean inputAllowedToEnd, boolean multiplierAllowedToDecrease) {
			this.inputAllowedToEnd = inputAllowedToEnd;
			this.multiplierAllowedToDecrease = multiplierAllowedToDecrease;
		}

		public abstract void process(Model model, char character);

		public boolean isInputAllowedToEndInThisState() {
			return this.inputAllowedToEnd;
		}

		public boolean isMultiplierAllowedToDecreaseInThisState() {
			return this.multiplierAllowedToDecrease;
		}
	}

	public static void main(String[] args) throws IOException {
		char[] fileCharacters = new String(Files.readAllBytes(InputFileUtils.getInputPath()), StandardCharsets.UTF_8).trim().toCharArray();
		outputSummary(fileCharacters, "disallowed", false);
		outputSummary(fileCharacters, "allowed", true);
	}

	private static void outputSummary(char[] fileCharacters, String allowNestedInstructionsDescription, boolean allowNestedInstructions) {
		System.out.format("Decompressed length when nested instructions are %s: %d%n", allowNestedInstructionsDescription, runModel(fileCharacters, allowNestedInstructions));
	}

	private static long runModel(char[] fileCharacters, boolean allowNestedInstructions) {
		Model model = new Model(allowNestedInstructions);
		for (char fileCharacter : fileCharacters) {
			model.getNextState().process(model, fileCharacter);
			model.postProcessCompressedCharacter();
		}
		if (!model.isInputAllowedToEnd()) {
			throw new IllegalStateException("Unexpected EOF");
		}
		return model.getDecompressedLength();
	}
}
