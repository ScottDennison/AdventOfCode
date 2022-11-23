package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2019;

import uk.co.scottdennison.java.soft.challenges.adventofcode.common.IntcodeComputer;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;

public class Day09 implements IPuzzle {
	private static long run(long[] program, long input) {
		IntcodeComputer intcodeComputer = new IntcodeComputer(program);
		intcodeComputer.addInput(input);
		intcodeComputer.runFully();
		if (intcodeComputer.getOutputCount() != 1) {
			throw new IllegalStateException("Expected only one output");
		}
		return intcodeComputer.getOutput(0);
	}

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		long[] program = IntcodeComputer.readProgram(inputCharacters);
		return new BasicPuzzleResults<>(
			run(program, 1L),
			run(program, 2L)
		);
	}
}
