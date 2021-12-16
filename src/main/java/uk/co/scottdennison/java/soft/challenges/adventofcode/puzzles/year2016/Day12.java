package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2016;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2015.Day23;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.LongUnaryOperator;
import java.util.regex.Matcher;

public class Day12 implements IPuzzle {
    private static final int REGISTER_COUNT = 4;
    private static final int RESULT_REGISTER_INDEX = 0;
    private static final int INPUT_REGISTER_INDEX = 2;
    private static final long PART_A_INPUT_VALUE = 0;
    private static final long PART_B_INPUT_VALUE = 1;

    private static enum OperandType {
        REGISTER,
        VALUE
    }

    public interface Operand {
        OperandType getOperandType();
    }

    public static final class RegisterOperand implements Operand {
        private final int register;

        public RegisterOperand(int register) {
            this.register = register;
        }

        public int getRegister() {
            return this.register;
        }

        @Override
        public OperandType getOperandType() {
            return OperandType.REGISTER;
        }
    }

    public static final class ValueOperand implements Operand {
        private final long value;

        public ValueOperand(long value) {
            this.value = value;
        }

        public long getValue() {
            return this.value;
        }

        @Override
        public OperandType getOperandType() {
            return OperandType.VALUE;
        }
    }

    private static interface InstructionFactory {
        Instruction createInstruction(Operand[] operands);
    }

    private static interface Instruction {
        void run(State state);
    }

    private static abstract class AbstractOneRegisterOperandInstructionFactory implements InstructionFactory {
        @Override
        public Instruction createInstruction(Operand[] operands) {
            if (operands.length != 1) {
                throw new IllegalStateException("1 operand expected");
            }
            Operand operand = operands[0];
            if (operand.getOperandType() != OperandType.REGISTER) {
                throw new IllegalStateException("The operand should be a register");
            }
            return createInstruction(((RegisterOperand)operand).getRegister());
        }

        protected abstract Instruction createInstruction(int register);
    }

    private static final class CopyRegisterToRegisterInstruction implements Instruction {
        private final int sourceRegister;
        private final int destinationRegister;

        public CopyRegisterToRegisterInstruction(int sourceRegister, int destinationRegister) {
            this.sourceRegister = sourceRegister;
            this.destinationRegister = destinationRegister;
        }

        @Override
        public void run(State state) {
            state.setRegisterValue(destinationRegister, state.getRegisterValue(sourceRegister));
            state.adjustPC(1);
        }
    }

    private static final class SetRegisterValueInstruction implements Instruction {
        private final long sourceValue;
        private final int destinationRegister;

        public SetRegisterValueInstruction(long sourceValue, int destinationRegister) {
            this.sourceValue = sourceValue;
            this.destinationRegister = destinationRegister;
        }

        @Override
        public void run(State state) {
            state.setRegisterValue(destinationRegister, sourceValue);
            state.adjustPC(1);
        }
    }

    private static final class CopyInstructionFactory implements InstructionFactory {
        public static final CopyInstructionFactory INSTANCE = new CopyInstructionFactory();

        private CopyInstructionFactory() {}

        @Override
        public Instruction createInstruction(Operand[] operands) {
            if (operands.length != 2) {
                throw new IllegalStateException("2 operands expected");
            }
            Operand secondOperand = operands[1];
            if (secondOperand.getOperandType() != OperandType.REGISTER) {
                throw new IllegalStateException("Second operand should be a register");
            }
            int destinationRegister = ((RegisterOperand)secondOperand).getRegister();
            Operand firstOperand = operands[0];
            switch (firstOperand.getOperandType()) {
                case REGISTER:
                    return new CopyRegisterToRegisterInstruction(((RegisterOperand)firstOperand).getRegister(),destinationRegister);
                case VALUE:
                    return new SetRegisterValueInstruction(((ValueOperand)firstOperand).getValue(),destinationRegister);
                default:
                    throw new IllegalStateException("Unexpected first operand type");
            }
        }
    }

    private static final class IncrementRegisterInstruction implements Instruction {
        private final int register;

        public IncrementRegisterInstruction(int register) {
            this.register = register;
        }

        @Override
        public void run(State state) {
            state.mutateRegisterValue(register, 1);
            state.adjustPC(1);
        }
    }

    public static final class IncrementRegisterInstructionFactory extends AbstractOneRegisterOperandInstructionFactory {
        public static final IncrementRegisterInstructionFactory INSTANCE = new IncrementRegisterInstructionFactory();

        private IncrementRegisterInstructionFactory() {}

        @Override
        protected Instruction createInstruction(int register) {
            return new IncrementRegisterInstruction(register);
        }
    }

    private static final class DecrementRegisterInstruction implements Instruction {
        private final int register;

        public DecrementRegisterInstruction(int register) {
            this.register = register;
        }

        @Override
        public void run(State state) {
            state.mutateRegisterValue(register, -1);
            state.adjustPC(1);
        }
    }

    public static final class DecrementRegisterInstructionFactory extends AbstractOneRegisterOperandInstructionFactory {
        public static final DecrementRegisterInstructionFactory INSTANCE = new DecrementRegisterInstructionFactory();

        private DecrementRegisterInstructionFactory() {}

        @Override
        protected Instruction createInstruction(int register) {
            return new DecrementRegisterInstruction(register);
        }
    }

    private static final class JumpInstruction implements Instruction {
        private final int offset;

        public JumpInstruction(int offset) {
            this.offset = offset;
        }

        @Override
        public void run(State state) {
            state.adjustPC(offset);
        }
    }

    private static final class JumpIfRegisterNotZeroInstruction implements Instruction {
        private final int register;
        private final int offset;

        public JumpIfRegisterNotZeroInstruction(int register, int offset) {
            this.register = register;
            this.offset = offset;
        }

        @Override
        public void run(State state) {
            state.adjustPC(state.getRegisterValue(register)==0?1:offset);
        }
    }

    private static final class NoopInstruction implements Instruction {
        private static final NoopInstruction INSTANCE = new NoopInstruction();

        private NoopInstruction() {}

        @Override
        public void run(State state) {
            state.adjustPC(1);
        }
    }

    private static final class JumpIfNotZeroInstructionFactory implements InstructionFactory {
        public static final JumpIfNotZeroInstructionFactory INSTANCE = new JumpIfNotZeroInstructionFactory();

        private JumpIfNotZeroInstructionFactory() {}

        @Override
        public Instruction createInstruction(Operand[] operands) {
            if (operands.length != 2) {
                throw new IllegalStateException("2 operands expected");
            }
            Operand secondOperand = operands[1];
            if (secondOperand.getOperandType() != OperandType.VALUE) {
                throw new IllegalStateException("Second operand should be a value");
            }
            long offsetLong = ((ValueOperand)secondOperand).getValue();
            if (offsetLong < Integer.MIN_VALUE || offsetLong > Integer.MAX_VALUE) {
                throw new IllegalStateException("Offset out of bounds");
            }
            int offset = (int)offsetLong;
            Operand firstOperand = operands[0];
            switch (firstOperand.getOperandType()) {
                case REGISTER:
                    return new JumpIfRegisterNotZeroInstruction(((RegisterOperand)firstOperand).getRegister(),offset);
                case VALUE:
                    long value = ((ValueOperand)firstOperand).getValue();
                    if (value == 0) {
                        return NoopInstruction.INSTANCE;
                    } else {
                        return new JumpInstruction(offset);
                    }
                default:
                    throw new IllegalStateException("Unexpected first operand type");
            }
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

        public void mutateRegisterValue(int index, int addition) {
            registers[index] = registers[index] + addition;
        }

        public void adjustPC(int offset) {
            pc += offset;
        }

        public int getPC() {
            return pc;
        }
    }

    private static final Map<String,InstructionFactory> INSTRUCTION_FACTORY_MAP = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    static {
        INSTRUCTION_FACTORY_MAP.put("cpy",CopyInstructionFactory.INSTANCE);
        INSTRUCTION_FACTORY_MAP.put("inc",IncrementRegisterInstructionFactory.INSTANCE);
        INSTRUCTION_FACTORY_MAP.put("dec",DecrementRegisterInstructionFactory.INSTANCE);
        INSTRUCTION_FACTORY_MAP.put("jnz",JumpIfNotZeroInstructionFactory.INSTANCE);
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        String[] instructionStringsArray = LineReader.stringsArray(inputCharacters,true);
        int instructionCount = instructionStringsArray.length;
        Instruction[] instructionsArray = new Instruction[instructionCount];
        for (int instructionIndex=0; instructionIndex<instructionCount; instructionIndex++) {
            String[] instructionStringParts = instructionStringsArray[instructionIndex].split(" ");
            int instructionStringPartCount = instructionStringParts.length;
            InstructionFactory instructionFactory = INSTRUCTION_FACTORY_MAP.get(instructionStringParts[0]);
            if (instructionFactory == null) {
                throw new IllegalStateException("No matching instruction found");
            }
            Operand[] operands = new Operand[instructionStringPartCount-1];
            for (int instructionStringPartIndex=1; instructionStringPartIndex<instructionStringPartCount; instructionStringPartIndex++) {
                String instructionStringPart = instructionStringParts[instructionStringPartIndex];
                Operand operand = null;
                if (instructionStringPart.length() == 1) {
                    char instructionChar = instructionStringPart.charAt(0);
                    Integer register = null;
                    if (instructionChar >= 'a' && instructionChar <= 'z') {
                        register = instructionChar-'a';
                    }
                    else if (instructionChar >= 'A' && instructionChar <= 'Z') {
                        register = instructionChar-'A';
                    }
                    if (register != null) {
                        if (register >= 0 && register < REGISTER_COUNT) {
                            operand = new RegisterOperand(register);
                        } else {
                            throw new IllegalStateException("Unexpected register");
                        }
                    }
                }
                if (operand == null) {
                    operand = new ValueOperand(Long.parseLong(instructionStringPart));
                }
                operands[instructionStringPartIndex-1] = operand;
            }
            Instruction instruction = instructionFactory.createInstruction(operands);
            instructionsArray[instructionIndex] = instruction;
        }
        return new BasicPuzzleResults<>(
            runProgram(instructionsArray, PART_A_INPUT_VALUE),
            runProgram(instructionsArray, PART_B_INPUT_VALUE)
        );
    }

    private long runProgram(Instruction[] instructionsArray, long inputValue) {
        int instructionCount = instructionsArray.length;
        State state = new State(REGISTER_COUNT);
        state.setRegisterValue(INPUT_REGISTER_INDEX, inputValue);
        while (true) {
            int pc = state.getPC();
            if (pc < 0 || pc >= instructionCount) {
                break;
            }
            instructionsArray[pc].run(state);
        }
        return state.getRegisterValue(RESULT_REGISTER_INDEX);
    }
}
