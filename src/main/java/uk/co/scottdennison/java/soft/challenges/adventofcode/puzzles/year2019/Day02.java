package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2019;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Arrays;

public class Day02 implements IPuzzle {
    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        int[] memory = Arrays.stream(new String(inputCharacters).trim().split(",")).mapToInt(Integer::parseInt).toArray();
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
        int instructionPointer = 0;
        while (true) {
            int opcode = memory[instructionPointer];
            if (opcode == 99) {
                break;
            }
            int inputAddress1 = memory[instructionPointer+1];
            int inputAddress2 = memory[instructionPointer+2];
            int outputAddress = memory[instructionPointer+3];
            int inputValue1 = memory[inputAddress1];
            int inputValue2 = memory[inputAddress2];
            int ouputValue;
            switch (opcode) {
                case 1:
                    ouputValue = inputValue1+inputValue2;
                    break;
                case 2:
                    ouputValue = inputValue1*inputValue2;
                    break;
                default:
                    throw new IllegalStateException("Unexpected opcode");
            }
            memory[outputAddress] = ouputValue;
            instructionPointer += 4;
        }
        return memory[0];
    }
}
