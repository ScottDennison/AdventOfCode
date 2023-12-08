package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2016;

import uk.co.scottdennison.java.soft.challenges.adventofcode.common.AssembunnyComputer;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

public class Day25 implements IPuzzle {
	private static final int REGISTER_COUNT = 4;
	private static final int INPUT_REGISTER_INDEX = 0;
	private static final int CUTOFF_INSTRUCTION_COUNT = 2500000;
	private static final int TARGET_OUTPUT_COUNT = 128;

	private static class State {
		private boolean run;
		private int instructionsExecuted;
		private boolean firstOutput;
		private long expectedOutput;
		private int outputCount;
		private boolean success;

		public void reset() {
			run = true;
			instructionsExecuted = 0;
			firstOutput = true;
			outputCount = 0;
			success = true;
		}

		public boolean isRun() {
			return this.run;
		}

		public boolean isSuccess() {
			return this.success;
		}

		public void incrementInstructionCount() {
			if (++this.instructionsExecuted >= CUTOFF_INSTRUCTION_COUNT) {
				this.success = false;
				this.run = false;
			}
		}

		public void handleOutput(long output) {
			if (this.firstOutput) {
				if (output != 0 && output != 1) {
					this.success = false;
					this.run = false;
					return;
				}
				this.firstOutput = false;
			} else if (output != this.expectedOutput) {
				this.success = false;
				this.run = false;
				return;
			}
			this.expectedOutput = 1 - output;
			if (++this.outputCount >= TARGET_OUTPUT_COUNT) {
				this.run = false;
			}
		}
	}

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		AssembunnyComputer.InstructionPair[] instructionPairs = AssembunnyComputer.parseProgram(inputCharacters, REGISTER_COUNT);
		State problemState = new State();
		int inputValue = 0;
		while (true) {
			problemState.reset();
			AssembunnyComputer.State computerState = new AssembunnyComputer.State(instructionPairs, REGISTER_COUNT);
			computerState.setRegisterValue(INPUT_REGISTER_INDEX, inputValue);
			computerState.setOutputHandler(problemState::handleOutput);
			while (problemState.isRun()) {
				int pc = computerState.getPC();
				if (!computerState.isValidInstructionIndex(pc)) {
					break;
				}
				computerState.getInstruction(pc).run(computerState);
				problemState.incrementInstructionCount();
			}
			if (problemState.isSuccess()) {
				return new BasicPuzzleResults<>(
					inputValue,
					"Transmit the Signal"
				);
			}
			inputValue++;
		}
	}
}
