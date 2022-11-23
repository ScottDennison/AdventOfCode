package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2019;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.common.IntcodeComputer;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Arrays;

public class Day02 implements IPuzzle {
    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        int[] memory = IntcodeComputer.readProgram(inputCharacters);
        int part1Result = runProgram(memory,12,2);
        Integer part2Result = null;
        for (int noun=0; noun<=99; noun++) {
            for (int verb=0; verb<=99; verb++) {
                if (runProgram(memory,noun,verb) == 19690720) {
                    if (part2Result != null) {
                        throw new IllegalStateException("Multiple results");
                    }
                    part2Result = (100*noun)+verb;
                }
            }
        }
        return new BasicPuzzleResults<>(
            part1Result,
            part2Result
        );
    }

    private static int runProgram(int[] initialMemory, int noun, int verb) {
        int[] memory = Arrays.copyOf(initialMemory, initialMemory.length);
        memory[1] = noun;
        memory[2] = verb;
        IntcodeComputer intcodeComputer = new IntcodeComputer(memory);
        intcodeComputer.runFully();
        return intcodeComputer.getMemory(0);
    }
}
