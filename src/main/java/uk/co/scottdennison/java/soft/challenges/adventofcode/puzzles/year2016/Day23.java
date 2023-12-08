package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2016;

import uk.co.scottdennison.java.soft.challenges.adventofcode.common.AssembunnyComputer;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;

public class Day23 implements IPuzzle {
	/**
	 * The problem text strongly hints that the problem can be optimized by replacing some instructions with a new multiply instruction
	 *
	 * Specifically:
	 *
	 * inc r1 or dec r2
	 * dec r2 or inc r1 (the unused instruction from above)
	 * jnz r2 -2
	 * dec r3
	 * jnz r3 -5
	 *
	 * Is the same as incrementing r1 by (r2 * r3), and in the process clearing r2 and r3.
	 *
	 * However, with the toggle mechanics, I feel uncomfortable creating an optimizer for such a case, so have decided to leave this solution taking 20+ seconds.
	 */

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
