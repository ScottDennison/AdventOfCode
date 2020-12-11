package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2020;

import uk.co.scottdennison.java.challenges.adventofcode.utils.InputFileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day08 {
	private static final Pattern PATTERN = Pattern.compile("^(?<operation>[a-z]+) (?<operand>[+\\-][0-9]+)$");

	private static class State {
		private final int instructionPointer;
		private final int accumulator;

		public State(int instructionPointer, int accumulator) {
			this.instructionPointer = instructionPointer;
			this.accumulator = accumulator;
		}

		public int getInstructionPointer() {
			return this.instructionPointer;
		}

		public int getAccumulator() {
			return this.accumulator;
		}
	}

	@SuppressWarnings("unused")
	private enum Operation {
		NOP {
			@Override
			public State execute(State inputState, int operand) {
				return new State(inputState.getInstructionPointer() + 1, inputState.getAccumulator());
			}
		},
		ACC {
			@Override
			public State execute(State inputState, int operand) {
				return new State(inputState.getInstructionPointer() + 1, inputState.getAccumulator() + operand);
			}
		},
		JMP {
			@Override
			public State execute(State inputState, int operand) {
				return new State(inputState.getInstructionPointer() + operand, inputState.getAccumulator());
			}
		};

		public abstract State execute(State inputState, int operand);
	}

	private static class Instruction {
		private final Operation operation;
		private final int operand;

		public Instruction(Operation operation, int operand) {
			this.operation = operation;
			this.operand = operand;
		}

		public Operation getOperation() {
			return this.operation;
		}

		public int getOperand() {
			return this.operand;
		}

		public State execute(State inputState) {
			return this.operation.execute(inputState, this.operand);
		}
	}

	private enum ProgramMode {
		RUNNING,
		INFINITE_LOOP_DETECTED,
		TERMINATED_NORMALLY,
		TERMINATED_ABNORMALLY
	}

	private static class ProgramResult {
		private final State state;
		private final ProgramMode programMode;

		public ProgramResult(State state, ProgramMode programMode) {
			this.state = state;
			this.programMode = programMode;
		}

		public State getState() {
			return this.state;
		}

		public ProgramMode getProgramMode() {
			return this.programMode;
		}
	}

	private static ProgramResult executeProgram(List<Instruction> instructions) {
		Set<Integer> instructionPointerVisited = new HashSet<>();
		State state = new State(0, 0);
		int instructionCount = instructions.size();
		ProgramMode programMode = ProgramMode.RUNNING;
		while (programMode == ProgramMode.RUNNING) {
			int instructionPointer = state.getInstructionPointer();
			if (instructionPointer == instructionCount) {
				programMode = ProgramMode.TERMINATED_NORMALLY;
			}
			else if (instructionPointer < 0 || instructionPointer > instructionCount) {
				programMode = ProgramMode.TERMINATED_ABNORMALLY;
			}
			else if (!instructionPointerVisited.add(state.getInstructionPointer())) {
				programMode = ProgramMode.INFINITE_LOOP_DETECTED;
			}
			else {
				state = instructions.get(state.getInstructionPointer()).execute(state);
			}
		}
		return new ProgramResult(state, programMode);
	}

	public static void main(String[] args) throws IOException {
		List<Instruction> instructions = new ArrayList<>();
		for (String fileLine : Files.readAllLines(InputFileUtils.getInputPath())) {
			Matcher matcher = PATTERN.matcher(fileLine);
			if (!matcher.matches()) {
				throw new IllegalStateException("Unable to parse line.");
			}
			instructions.add(new Instruction(Operation.valueOf(matcher.group("operation").toUpperCase(Locale.ENGLISH)), Integer.parseInt(matcher.group("operand"))));
		}
		ProgramResult standardProgramResult = executeProgram(instructions);
		ProgramMode standardProgramMode = standardProgramResult.getProgramMode();
		if (standardProgramMode != ProgramMode.INFINITE_LOOP_DETECTED) {
			throw new IllegalStateException("Did not encounter an infinite loop in the unmodified program. Mode was " + standardProgramMode);
		}
		int instructionCount = instructions.size();
		ProgramResult terminatingNormallyProgramResult = null;
		for (int instructionIndex = 0; instructionIndex < instructionCount; instructionIndex++) {
			Instruction oldInstruction = instructions.get(instructionIndex);
			Operation oldOperation = oldInstruction.getOperation();
			Operation newOperation;
			switch (oldOperation) {
				case JMP:
					newOperation = Operation.NOP;
					break;
				case NOP:
					newOperation = Operation.JMP;
					break;
				default:
					newOperation = oldOperation;
			}
			if (oldOperation != newOperation) {
				Instruction newInstruction = new Instruction(newOperation, oldInstruction.getOperand());
				instructions.set(instructionIndex, newInstruction);
				ProgramResult modifiedProgramResult = executeProgram(instructions);
				if (modifiedProgramResult.getProgramMode() == ProgramMode.TERMINATED_NORMALLY) {
					if (terminatingNormallyProgramResult == null) {
						terminatingNormallyProgramResult = modifiedProgramResult;
					}
					else {
						throw new IllegalStateException("Multiple modified programs terminate normally.");
					}
				}
				instructions.set(instructionIndex, oldInstruction);
			}
		}
		if (terminatingNormallyProgramResult == null) {
			throw new IllegalStateException("Did not find a program that terminated normally in all modified programs.");
		}
		System.out.format("Accumulator at %d for unmodified program when it reaches an infinite loop%n", standardProgramResult.getState().getAccumulator());
		System.out.format("Accumulator at %d for modified terminating-normally program%n", terminatingNormallyProgramResult.getState().getAccumulator());
	}
}