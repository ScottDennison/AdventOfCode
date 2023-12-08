package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2016;

import uk.co.scottdennison.java.soft.challenges.adventofcode.common.AssembunnyComputer;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;

public class Day23 implements IPuzzle {
	private static final int REGISTER_COUNT = 4;
	private static final int OUTPUT_REGISTER_INDEX = 0;
	private static final int INPUT_REGISTER_INDEX = 0;
	private static final long PART_A_INPUT_VALUE = 7;
	private static final long PART_B_INPUT_VALUE = 12;

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		AssembunnyComputer.InstructionPair[] instructionPairs = AssembunnyComputer.parseProgram(inputCharacters, REGISTER_COUNT);
		return new BasicPuzzleResults<>(
			runProgramUntilTermination(instructionPairs, PART_A_INPUT_VALUE),
			runProgramUntilTermination(instructionPairs, PART_B_INPUT_VALUE)
		);
	}

	private static long runProgramUntilTermination(AssembunnyComputer.InstructionPair[] instructionsPairs, long inputValue) {
		return AssembunnyComputer.runProgramUntilTerminationWithIORegisters(instructionsPairs, REGISTER_COUNT, INPUT_REGISTER_INDEX, OUTPUT_REGISTER_INDEX, inputValue);
	}
}
