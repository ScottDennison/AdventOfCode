package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2019;

import uk.co.scottdennison.java.soft.challenges.adventofcode.common.IntcodeComputer;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;

public class Day05 implements IPuzzle {
    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        long[] memory = IntcodeComputer.readProgram(inputCharacters);

        IntcodeComputer partAIntcodeComputer = new IntcodeComputer(memory);
        partAIntcodeComputer.addInput(1L);
        partAIntcodeComputer.runFully();
        int partAOutputCount = partAIntcodeComputer.getOutputCount();
        if (partAOutputCount < 2) {
            throw new IllegalStateException("Expected at least 2 outputs from part A");
        }
        for (int index=0; index<partAOutputCount-1; index++) {
            if (partAIntcodeComputer.getOutput(index) != 0L) {
                throw new IllegalStateException("Expected all outputs bar the last to be 0");
            }
        }
        long partAResult = partAIntcodeComputer.getOutput(partAOutputCount-1);

        IntcodeComputer partBIntcodeComputer = new IntcodeComputer(memory);
        partBIntcodeComputer.addInput(5L);
        partBIntcodeComputer.runFully();
        if (partBIntcodeComputer.getOutputCount() != 1) {
            throw new IllegalStateException("Expected only 1 output from part B");
        }
        long partBResult = partBIntcodeComputer.getOutput(0);

        return new BasicPuzzleResults<>(
            partAResult,
            partBResult
        );
    }
}
