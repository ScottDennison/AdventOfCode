package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2024;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Day17 implements IPuzzle {
    private static final Pattern PATTERN_INPUT = Pattern.compile("\\ARegister A: (?<registerA>[0-9]+)\\nRegister B: (?<registerB>[0-9]+)\\nRegister C: (?<registerC>[0-9]+)\\n\\nProgram: (?<program>[0-7](,[0-7])*)\\z");

    private static int runProgram(long registerA, long registerB, long registerC, int[] program, long[] output) {
        int outputIndex = 0;
        int programIndex = 0;
        int programLength = program.length;
        while (programIndex < programLength) {
            int opcode = program[programIndex++];
            int literalOperand = program[programIndex++];
            switch (opcode) {
                case 1:
                    registerB ^= literalOperand;
                    break;
                case 3:
                    if (registerA != 0) {
                        programIndex = (int)literalOperand;
                    }
                    break;
                default:
                    long comboOperand;
                    switch (literalOperand) {
                        case 4:
                            comboOperand = registerA;
                            break;
                        case 5:
                            comboOperand = registerB;
                            break;
                        case 6:
                            comboOperand = registerC;
                            break;
                        case 7:
                            throw new IllegalStateException("7 is not a valid combo operand.");
                        default:
                            comboOperand = literalOperand;
                    }
                    switch (opcode) {
                        case 0:
                            registerA /= 1 << comboOperand;
                            break;
                        case 2:
                            registerB = comboOperand % 8;
                            break;
                        case 4:
                            registerB ^= registerC;
                            break;
                        case 5:
                            output[outputIndex++] = comboOperand % 8;
                            break;
                        case 6:
                            registerB = registerA / (1 << comboOperand);
                            break;
                        case 7:
                            registerC = registerA / (1 << comboOperand);
                            break;
                        default:
                            throw new IllegalStateException("Unexpected opcode");
                    }
            }
        }
        return outputIndex;
    }

    private static long recurseFindAForReplication(long initialRegisterB, long initialRegisterC, int[] program, long[] output, int registerABitReduction, int recursionLevel, long currentA, long bestValidA) {
        int programLength = program.length;
        if (recursionLevel > programLength) {
            if (currentA < bestValidA) {
                return currentA;
            }
            else {
                return bestValidA;
            }
        }
        else {
            long newBaseA = currentA << registerABitReduction;
            int possibilites = 1 << registerABitReduction;
            int nextRecursionLevel = recursionLevel + 1;
            possibilityLoop:
            for (int possibility=0; possibility<possibilites; possibility++) {
                long newCurrentA = newBaseA | possibility;
                if (runProgram(newCurrentA, initialRegisterB, initialRegisterC, program, output) != recursionLevel) {
                    continue;
                }
                for (int ouputIndex=0, programIndex=programLength - recursionLevel; ouputIndex<recursionLevel; ouputIndex++, programIndex++) {
                    if (output[ouputIndex] != program[programIndex]) {
                        continue possibilityLoop;
                    }
                }
                bestValidA = recurseFindAForReplication(initialRegisterB, initialRegisterC, program, output, registerABitReduction, nextRecursionLevel, newCurrentA, bestValidA);
            }
            return bestValidA;
        }
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        Matcher matcher = PATTERN_INPUT.matcher(new String(inputCharacters).trim());
        if (!matcher.matches()) {
            throw new IllegalStateException("Unable to parse input");
        }
        long initialRegisterA = Long.parseLong(matcher.group("registerA"));
        long initialRegisterB = Long.parseLong(matcher.group("registerB"));
        long initialRegisterC = Long.parseLong(matcher.group("registerC"));
        int[] program = Arrays.stream(matcher.group("program").split(",")).mapToInt(Integer::parseInt).toArray();
        int programLength = program.length;
        if (programLength % 2 != 0) {
            throw new IllegalStateException("Program does not have enough values");
        }
        if (program[programLength - 2] != 3 && program[programLength - 1] != 0) {
            throw new IllegalStateException("The assumptions in this solution assume that the last instruction is JNZ 0.");
        }
        int programIndex = 0;
        int programLengthMinus2 = programLength - 2;
        boolean hadAdvInstruction = false;
        int registerABitReduction = -1;
        boolean hadFirstBModifyingOperation = false;
        boolean hadFirstCModifyingOperation = false;
        Set<Integer> registersInitiallyDependentOnAEachLoop = new HashSet<>();
        registersInitiallyDependentOnAEachLoop.add(4);
        boolean firstBModifyingOperationDependsEntirelyOnAViolated = false;
        boolean firstCModifyingOperationDependsEntirelyOnAViolated = false;
        while (programIndex < programLengthMinus2) {
            int opcode = program[programIndex++];
            int literalOperand = program[programIndex++];
            switch (opcode) {
                case 0:
                    if (hadAdvInstruction) {
                        throw new IllegalStateException("The assumptions in this solution assume only one ADV instruction appears in the program.");
                    }
                    switch (literalOperand) {
                        case 0:
                        case 4:
                        case 5:
                        case 6:
                            throw new IllegalStateException("The assumptions in this solution assume that ADV instruction has a non-zero literal value as it's combo operand.");
                        default:
                            registerABitReduction = literalOperand;
                    }
                    hadAdvInstruction = true;
                    break;
                case 1:
                case 4:
                    if (!hadFirstBModifyingOperation) {
                        firstBModifyingOperationDependsEntirelyOnAViolated = true;
                    }
                    break;
                case 2:
                case 6:
                    if (!hadFirstBModifyingOperation) {
                        hadFirstBModifyingOperation = true;
                        if (registersInitiallyDependentOnAEachLoop.contains(literalOperand)) {
                            registersInitiallyDependentOnAEachLoop.add(5);
                        }
                        else {
                            firstBModifyingOperationDependsEntirelyOnAViolated = true;
                        }
                    }
                    break;
                case 7:
                    if (!hadFirstCModifyingOperation) {
                        hadFirstCModifyingOperation = true;
                        if (registersInitiallyDependentOnAEachLoop.contains(literalOperand)) {
                            registersInitiallyDependentOnAEachLoop.add(6);
                        }
                        else {
                            firstCModifyingOperationDependsEntirelyOnAViolated = true;
                        }
                    }
                    break;
                case 3:
                    throw new IllegalStateException("The assumptions in this solution assume only one JNZ instruction appears in the program.");
            }
        }
        if (!hadAdvInstruction) {
            throw new IllegalStateException("The assumptions in this solution assume that an ADV instruction appears in the program.");
        }
        if (hadFirstBModifyingOperation && firstBModifyingOperationDependsEntirelyOnAViolated) {
            throw new IllegalStateException("The first B modifying operation did not depend entirely on A.");
        }
        if (hadFirstCModifyingOperation && firstCModifyingOperationDependsEntirelyOnAViolated) {
            throw new IllegalStateException("The first C modifying operation did not depend entirely on A.");
        }
        return new BasicPuzzleResults<>(
            runPartA(initialRegisterA, initialRegisterB, initialRegisterC, program, registerABitReduction),
            runPartB(                  initialRegisterB, initialRegisterC, program, registerABitReduction)
        );
    }

    private static String runPartA(long initialRegisterA, long initialRegisterB, long initialRegisterC, int[] program, int registerABitReduction) {
        long[] output = new long[(int)Math.ceil((Long.SIZE - Long.numberOfLeadingZeros(initialRegisterA)) / (double)registerABitReduction)];
        runProgram(initialRegisterA, initialRegisterB, initialRegisterC, program, output);
        return Arrays.stream(output).mapToObj(Long::toString).collect(Collectors.joining(","));
    }

    private static Long runPartB(long initialRegisterB, long initialRegisterC, int[] program, int registerABitReduction) {
        long result = recurseFindAForReplication(initialRegisterB, initialRegisterC, program, new long[program.length], registerABitReduction, 1, 0, Long.MAX_VALUE);
        if (result == Long.MAX_VALUE) {
            return null;
        }
        return result;
    }
}
