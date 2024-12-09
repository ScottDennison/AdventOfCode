package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2019;

import uk.co.scottdennison.java.soft.challenges.adventofcode.common.IntcodeComputer;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.function.LongSupplier;

public class Day07 implements IPuzzle {
    private static final int PART_A_MIN_PHASE_SETTING = 0;
    private static final int PART_A_MAX_PHASE_SETTING = 4;
    private static final int PART_A_THRUSTER_COUNT = 5;

    private static final int PART_B_MIN_PHASE_SETTING = 5;
    private static final int PART_B_MAX_PHASE_SETTING = 9;
    private static final int PART_B_THRUSTER_COUNT = 5;

    private static long recursePhasesPartA(long[] program, int depth, boolean[] usedPhaseSettings, long input) {
        if (depth >= PART_A_THRUSTER_COUNT) {
            return input;
        }
        else {
            int nextDepth = depth+1;
            long maxThrusterSignal = Long.MIN_VALUE;
            for (int phaseSetting=PART_A_MIN_PHASE_SETTING; phaseSetting<=PART_A_MAX_PHASE_SETTING; phaseSetting++) {
                if (!usedPhaseSettings[phaseSetting]) {
                    usedPhaseSettings[phaseSetting] = true;
                    IntcodeComputer intcodeComputer = new IntcodeComputer(program);
                    intcodeComputer.addInputs((long)phaseSetting, input);
                    intcodeComputer.runFully();
                    if (intcodeComputer.getOutputCount() != 1) {
                        throw new IllegalStateException("Unexpected amount of outputs");
                    }
                    maxThrusterSignal = Math.max(maxThrusterSignal, recursePhasesPartA(program, nextDepth, usedPhaseSettings, intcodeComputer.getOutput(0)));
                    usedPhaseSettings[phaseSetting] = false;
                }
            }
            return maxThrusterSignal;
        }
    }

    private static long recursePhasesPartB(long[] program, int depth, boolean[] usedPhaseSettings, int[] chosenPhaseSettings) {
        if (depth >= PART_B_THRUSTER_COUNT) {
            IntcodeComputer[] intcodeComputers = new IntcodeComputer[PART_B_THRUSTER_COUNT];
            for (int index=0; index<PART_B_THRUSTER_COUNT; index++) {
                IntcodeComputer intcodeComputer = new IntcodeComputer(program);
                intcodeComputer.addInput((long)chosenPhaseSettings[index]);
                intcodeComputers[index] = intcodeComputer;
            }
            long input = 0;
            int index = 0;
            boolean resultPending = true;
            while (true) {
                IntcodeComputer intcodeComputer = intcodeComputers[index];
                intcodeComputer.addInput(input);
                int oldOutputCount = intcodeComputer.getOutputCount();
                intcodeComputer.run();
                int newOutputCount = intcodeComputer.getOutputCount();
                if ((oldOutputCount + 1) != newOutputCount) {
                    throw new IllegalStateException("Expected a single new output.");
                }
                input = intcodeComputer.getOutput(oldOutputCount);
                if (++index == PART_B_THRUSTER_COUNT) {
                    index = 0;
                    if (intcodeComputer.getState() == IntcodeComputer.State.HALTED) {
                        break;
                    }
                }
            }
            return input;
        }
        else {
            int nextDepth = depth+1;
            long maxThrusterSignal = Long.MIN_VALUE;
            for (int phaseSetting=PART_B_MIN_PHASE_SETTING; phaseSetting<=PART_B_MAX_PHASE_SETTING; phaseSetting++) {
                if (!usedPhaseSettings[phaseSetting]) {
                    usedPhaseSettings[phaseSetting] = true;
                    chosenPhaseSettings[depth] = phaseSetting;
                    maxThrusterSignal = Math.max(maxThrusterSignal, recursePhasesPartB(program, nextDepth, usedPhaseSettings, chosenPhaseSettings));
                    usedPhaseSettings[phaseSetting] = false;
                }
            }
            return maxThrusterSignal;
        }
    }

    private static Long runPart(LongSupplier part) {
        try {
            return part.getAsLong();
        } catch (IntcodeComputer.InsufficentInputsException ex) {
            return null;
        }
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        long[] program = IntcodeComputer.readProgram(inputCharacters);
        return new BasicPuzzleResults<>(
            runPart(() -> recursePhasesPartA(program,0, new boolean[PART_A_MAX_PHASE_SETTING+1], 0)),
            runPart(() -> recursePhasesPartB(program, 0, new boolean[PART_B_MAX_PHASE_SETTING+1], new int[PART_B_THRUSTER_COUNT]))
        );
    }
}
