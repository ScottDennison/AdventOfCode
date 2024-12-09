package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2015;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.LongUnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day23 implements IPuzzle {
    private static final Pattern PATTERN_INSTRUCTION = Pattern.compile("^(?<instruction>[a-z]+) (?<register>[a-z])?(, )?(?<offset>[+\\-][0-9]+)?$");

    private static final int REGISTER_COUNT = 2;
    private static final int INPUT_REGISTER_INDEX = 0;
    private static final int RESULT_REGISTER_INDEX = 1;
    private static final long PART_A_INPUT_VALUE = 0;
    private static final long PART_B_INPUT_VALUE = 1;

    private static interface Instruction {
        void run(State state);
    }

    private static class HalfInstruction implements Instruction {
        private final int register;

        public HalfInstruction(int register) {
            this.register = register;
        }

        @Override
        public void run(State state) {
            state.mutateRegisterValue(register, x->x/2);
            state.adjustPC(1);
        }
    }

    private static class TripleInstruction implements Instruction {
        private final int register;

        public TripleInstruction(int register) {
            this.register = register;
        }

        @Override
        public void run(State state) {
            state.mutateRegisterValue(register, x->x*3);
            state.adjustPC(1);
        }
    }

    private static class IncrementInstruction implements Instruction {
        private final int register;

        public IncrementInstruction(int register) {
            this.register = register;
        }

        @Override
        public void run(State state) {
            state.mutateRegisterValue(register, x->x+1);
            state.adjustPC(1);
        }
    }

    private static class JumpInstruction implements Instruction {
        private final int offset;

        public JumpInstruction(int offset) {
            this.offset = offset;
        }

        @Override
        public void run(State state) {
            state.adjustPC(offset);
        }
    }

    private static abstract class AbstractJumpIfInstruction implements Instruction {
        private final int register;
        private final int offset;

        public AbstractJumpIfInstruction(int register, int offset) {
            this.register = register;
            this.offset = offset;
        }

        protected abstract boolean shouldJump(long value);

        @Override
        public void run(State state) {
            state.adjustPC(shouldJump(state.getRegisterValue(register))?offset:1);
        }
    }

    private static class JumpIfEvenInstruction extends AbstractJumpIfInstruction {
        public JumpIfEvenInstruction(int register, int offset) {
            super(register, offset);
        }

        @Override
        protected boolean shouldJump(long value) {
            return value % 2 == 0;
        }
    }

    private static class JumpIfOneInstruction extends AbstractJumpIfInstruction {
        public JumpIfOneInstruction(int register, int offset) {
            super(register, offset);
        }

        @Override
        protected boolean shouldJump(long value) {
            return value == 1;
        }
    }

    private static class State {
        private final long[] registers;
        private int pc;

        public State(int registerCount) {
            this.registers = new long[registerCount];
        }

        public long getRegisterValue(int index) {
            return registers[index];
        }

        public void setRegisterValue(int index, long value) {
            registers[index] = value;
        }

        public void mutateRegisterValue(int index, LongUnaryOperator operation) {
            registers[index] = operation.applyAsLong(registers[index]);
        }

        public void adjustPC(int offset) {
            pc += offset;
        }

        public int getPC() {
            return pc;
        }
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        List<Instruction> instructionList = new ArrayList<>();
        for (String line : LineReader.strings(inputCharacters)) {
            Matcher matcher = PATTERN_INSTRUCTION.matcher(line);
            if (!matcher.matches()) {
                throw new IllegalStateException("Could not parse pattern");
            }
            String registerString = matcher.group("register");
            int register;
            if (registerString == null) {
                register = -1;
            } else {
                register = registerString.charAt(0)-'a';
            }
            String offsetString = matcher.group("offset");
            int offset;
            if (offsetString == null) {
                offset = 0;
            } else {
                offset = Integer.parseInt(offsetString);
            }
            String instructionString = matcher.group("instruction");
            Instruction instruction;
            switch (instructionString) {
                case "hlf":
                    instruction = new HalfInstruction(register);
                    break;
                case "tpl":
                    instruction = new TripleInstruction(register);
                    break;
                case "inc":
                    instruction = new IncrementInstruction(register);
                    break;
                case "jmp":
                    instruction = new JumpInstruction(offset);
                    break;
                case "jie":
                    instruction = new JumpIfEvenInstruction(register, offset);
                    break;
                case "jio":
                    instruction = new JumpIfOneInstruction(register, offset);
                    break;
                default:
                    throw new IllegalStateException("Unrecognized instruction: " + instructionString);
            }
            instructionList.add(instruction);
        }
        Instruction[] instructionArray = instructionList.toArray(new Instruction[0]);
        return new BasicPuzzleResults<>(
            runProgram(instructionArray, PART_A_INPUT_VALUE),
            runProgram(instructionArray, PART_B_INPUT_VALUE)
        );
    }

    private long runProgram(Instruction[] instructionArray, long inputValue) {
        int instructionCount = instructionArray.length;
        State state = new State(REGISTER_COUNT);
        state.setRegisterValue(INPUT_REGISTER_INDEX, inputValue);
        while (true) {
            int pc = state.getPC();
            if (pc < 0 || pc >= instructionCount) {
                break;
            }
            instructionArray[pc].run(state);
        }
        return state.getRegisterValue(RESULT_REGISTER_INDEX);
    }
}
