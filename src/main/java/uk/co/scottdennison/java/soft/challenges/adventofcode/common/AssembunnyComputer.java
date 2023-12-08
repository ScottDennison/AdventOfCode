package uk.co.scottdennison.java.soft.challenges.adventofcode.common;

import uk.co.scottdennison.java.libs.text.input.LineReader;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

public class AssembunnyComputer {
	private static enum OperandType {
		REGISTER,
		VALUE
	}

	private interface Operand {
		OperandType getOperandType();
	}

	private static class InvalidInstructionOperandsException extends Exception {
		public InvalidInstructionOperandsException(String message) {
			super(message);
		}
	}

	private static final class RegisterOperand implements Operand {
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

	private static final class ValueOperand implements Operand {
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
		Instruction createInstruction(Operand[] operands) throws InvalidInstructionOperandsException;
		InstructionFactory getToggleInstructionFactory();
	}

	public enum InstructionType {
		OPTIMIZED_AWAY,
		INCREMENT_REGISTER_BY,
		INCREMENT_REGISTER_BY_MULTIPLICATION,
		COPY_INTO_REGISTER,
		INCREMENT_REGISTER,
		DECREMENT_REGISTER,
		JUMP_ALWAYS,
		JUMP_IF_REGISTER_NOT_ZERO,
		NOOP,
		TOGGLE;
	}

	public static interface Instruction {
		void run(State state);
		InstructionType getInstructionType();
	}

	private static class OptimizedAwayInstruction implements Instruction {
		public static final OptimizedAwayInstruction INSTANCE = new OptimizedAwayInstruction();

		private OptimizedAwayInstruction() {}

		@Override
		public void run(State state) {
			state.deoptimize();
		}

		@Override
		public InstructionType getInstructionType() {
			return InstructionType.OPTIMIZED_AWAY;
		}
	}

	private static class IncrementRegisterByInstruction implements Instruction {
		private int resultRegister;
		private int sourceRegister;

		public IncrementRegisterByInstruction(int resultRegister, int sourceRegister) {
			this.resultRegister = resultRegister;
			this.sourceRegister = sourceRegister;
		}

		public int getResultRegister() {
			return resultRegister;
		}

		public int getSourceRegister() {
			return sourceRegister;
		}

		@Override
		public void run(State state) {
			state.setRegisterValue(this.resultRegister, state.getRegisterValue(this.resultRegister) + state.getRegisterValue(this.sourceRegister));
			state.setRegisterValue(this.sourceRegister, 0);
			state.adjustPC(3);
		}

		@Override
		public InstructionType getInstructionType() {
			return InstructionType.INCREMENT_REGISTER_BY;
		}
	}

	private static class IncrementRegisterByMultiplicationInstruction implements Instruction {
		private int resultRegister;
		private int sourceRegister1;
		private int sourceRegister2;

		public IncrementRegisterByMultiplicationInstruction(int resultRegister, int sourceRegister1, int sourceRegister2) {
			this.resultRegister = resultRegister;
			this.sourceRegister1 = sourceRegister1;
			this.sourceRegister2 = sourceRegister2;
		}

		public int getResultRegister() {
			return resultRegister;
		}

		public int getSourceRegister1() {
			return sourceRegister1;
		}

		public int getSourceRegister2() {
			return sourceRegister2;
		}

		@Override
		public void run(State state) {
			state.setRegisterValue(this.resultRegister, state.getRegisterValue(this.resultRegister) + (state.getRegisterValue(this.sourceRegister1) * state.getRegisterValue(this.sourceRegister2)));
			state.setRegisterValue(this.sourceRegister1, 0);
			state.setRegisterValue(this.sourceRegister2, 0);
			state.adjustPC(5);
		}

		@Override
		public InstructionType getInstructionType() {
			return InstructionType.INCREMENT_REGISTER_BY_MULTIPLICATION;
		}
	}

	private static abstract class AbstractOneOperandInstructionFactory implements InstructionFactory {
		protected abstract Instruction createInstruction(Operand operand) throws InvalidInstructionOperandsException;

		@Override
		public final Instruction createInstruction(Operand[] operands) throws InvalidInstructionOperandsException {
			if (operands.length != 1) {
				throw new InvalidInstructionOperandsException("1 operand expected");
			}
			return createInstruction(operands[0]);
		}

		@Override
		public InstructionFactory getToggleInstructionFactory() {
			return IncrementRegisterInstructionFactory.INSTANCE;
		}
	}

	private static abstract class AbstractTwoOperandInstructionFactory implements InstructionFactory {
		protected abstract Instruction createInstruction(Operand firstOperand, Operand secondOperand) throws InvalidInstructionOperandsException;

		@Override
		public final Instruction createInstruction(Operand[] operands) throws InvalidInstructionOperandsException {
			if (operands.length != 2) {
				throw new InvalidInstructionOperandsException("2 operands expected");
			}
			return createInstruction(operands[0],operands[1]);
		}

		@Override
		public InstructionFactory getToggleInstructionFactory() {
			return JumpIfNotZeroInstructionFactory.INSTANCE;
		}
	}

	private static abstract class AbstractOneRegisterOperandInstructionFactory extends AbstractOneOperandInstructionFactory {
		@Override
		protected final Instruction createInstruction(Operand operand) throws InvalidInstructionOperandsException {
			if (operand.getOperandType() != OperandType.REGISTER) {
				throw new InvalidInstructionOperandsException("The operand should be a register");
			}
			return createInstruction(((RegisterOperand)operand).getRegister());
		}

		protected abstract Instruction createInstruction(int register);
	}

	private static final class CopyIntoRegisterInstruction implements Instruction {
		private final LongValueRetriever source;
		private final int destinationRegister;

		public CopyIntoRegisterInstruction(LongValueRetriever source, int destinationRegister) {
			this.source = source;
			this.destinationRegister = destinationRegister;
		}

		public LongValueRetriever getSource() {
			return source;
		}

		public int getDestinationRegister() {
			return destinationRegister;
		}

		@Override
		public void run(State state) {
			state.setRegisterValue(destinationRegister, source.getLongValue(state));
			state.adjustPC(1);
		}

		@Override
		public InstructionType getInstructionType() {
			return InstructionType.COPY_INTO_REGISTER;
		}
	}

	private static final class CopyIntoRegisterInstructionFactory extends AbstractTwoOperandInstructionFactory {
		public static final CopyIntoRegisterInstructionFactory INSTANCE = new CopyIntoRegisterInstructionFactory();

		private CopyIntoRegisterInstructionFactory() {}

		@Override
		protected Instruction createInstruction(Operand firstOperand, Operand secondOperand) throws InvalidInstructionOperandsException {
			if (secondOperand.getOperandType() != OperandType.REGISTER) {
				throw new InvalidInstructionOperandsException("Second operand should be a register");
			}
			return new CopyIntoRegisterInstruction(
				LongValueRetriever.createForOperand(firstOperand),
				((RegisterOperand)secondOperand).getRegister()
			);
		}
	}

	private static final class IncrementRegisterInstruction implements Instruction {
		private final int register;

		public IncrementRegisterInstruction(int register) {
			this.register = register;
		}

		public int getRegister() {
			return register;
		}

		@Override
		public void run(State state) {
			state.mutateRegisterValue(register, 1);
			state.adjustPC(1);
		}

		@Override
		public InstructionType getInstructionType() {
			return InstructionType.INCREMENT_REGISTER;
		}
	}

	private static final class IncrementRegisterInstructionFactory extends AbstractOneRegisterOperandInstructionFactory {
		public static final IncrementRegisterInstructionFactory INSTANCE = new IncrementRegisterInstructionFactory();

		private IncrementRegisterInstructionFactory() {}

		@Override
		protected Instruction createInstruction(int register) {
			return new IncrementRegisterInstruction(register);
		}

		@Override
		public InstructionFactory getToggleInstructionFactory() {
			return DecrementRegisterInstructionFactory.INSTANCE;
		}
	}

	private static final class DecrementRegisterInstruction implements Instruction {
		private final int register;

		public DecrementRegisterInstruction(int register) {
			this.register = register;
		}

		public int getRegister() {
			return register;
		}

		@Override
		public void run(State state) {
			state.mutateRegisterValue(register, -1);
			state.adjustPC(1);
		}

		@Override
		public InstructionType getInstructionType() {
			return InstructionType.DECREMENT_REGISTER;
		}
	}

	private static final class DecrementRegisterInstructionFactory extends AbstractOneRegisterOperandInstructionFactory {
		public static final DecrementRegisterInstructionFactory INSTANCE = new DecrementRegisterInstructionFactory();

		private DecrementRegisterInstructionFactory() {}

		@Override
		protected Instruction createInstruction(int register) {
			return new DecrementRegisterInstruction(register);
		}
	}

	private static enum ValueRetrieverType {
		STATIC,
		REGISTER
	}

	private static interface IntValueRetriever {
		int getIntValue(State state);
		ValueRetrieverType getType();

		public static IntValueRetriever createForOperand(Operand operand) throws InvalidInstructionOperandsException {
			switch (operand.getOperandType()) {
				case REGISTER:
					return new RegisterIntValueRetriever(((RegisterOperand)operand).getRegister());
				case VALUE:
					return StaticIntValueRetriever.createForLong(((ValueOperand)operand).getValue());
				default:
					throw new InvalidInstructionOperandsException("Unexpected operand type");
			}
		}
	}

	private static class StaticIntValueRetriever implements IntValueRetriever {
		private final int value;

		public StaticIntValueRetriever(int value) {
			this.value = value;
		}

		public static StaticIntValueRetriever createForLong(long value) throws InvalidInstructionOperandsException {
			if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
				throw new InvalidInstructionOperandsException("Value out of bounds");
			}
			return new StaticIntValueRetriever((int)value);
		}

		public int getIntValue() {
			return value;
		}

		@Override
		public int getIntValue(State state) {
			return this.value;
		}

		@Override
		public ValueRetrieverType getType() {
			return ValueRetrieverType.STATIC;
		}
	}

	private static class RegisterIntValueRetriever implements IntValueRetriever {
		private final int register;

		public RegisterIntValueRetriever(int register) {
			this.register = register;
		}

		public int getRegister() {
			return register;
		}

		@Override
		public int getIntValue(State state) {
			long registerValue = state.getRegisterValue(this.register);
			if (registerValue < Integer.MIN_VALUE || registerValue > Integer.MAX_VALUE) {
				throw new IllegalStateException("Value out of bounds");
			}
			return (int)registerValue;
		}

		@Override
		public ValueRetrieverType getType() {
			return ValueRetrieverType.REGISTER;
		}
	}

	private static interface LongValueRetriever {
		long getLongValue(State state);
		ValueRetrieverType getType();

		public static LongValueRetriever createForOperand(Operand operand) throws InvalidInstructionOperandsException {
			switch (operand.getOperandType()) {
				case REGISTER:
					return new RegisterLongValueRetriever(((RegisterOperand)operand).getRegister());
				case VALUE:
					return new StaticLongValueRetriever(((ValueOperand)operand).getValue());
				default:
					throw new InvalidInstructionOperandsException("Unexpected operand type");
			}
		}
	}

	private static class StaticLongValueRetriever implements LongValueRetriever {
		private final long value;

		public StaticLongValueRetriever(long value) {
			this.value = value;
		}

		@Override
		public long getLongValue(State state) {
			return this.value;
		}

		@Override
		public ValueRetrieverType getType() {
			return ValueRetrieverType.STATIC;
		}
	}

	private static class RegisterLongValueRetriever implements LongValueRetriever {
		private final int register;

		public RegisterLongValueRetriever(int register) {
			this.register = register;
		}

		@Override
		public long getLongValue(State state) {
			return state.getRegisterValue(this.register);
		}

		@Override
		public ValueRetrieverType getType() {
			return ValueRetrieverType.REGISTER;
		}
	}

	private static abstract class AbstractJumpInstruction implements Instruction {
		private final IntValueRetriever offsetRetriever;

		public AbstractJumpInstruction(IntValueRetriever offsetRetriever) {
			this.offsetRetriever = offsetRetriever;
		}

		public abstract boolean shouldJump(State state);

		public IntValueRetriever getOffsetRetriever() {
			return offsetRetriever;
		}

		@Override
		public final void run(State state) {
			state.adjustPC(shouldJump(state)?this.offsetRetriever.getIntValue(state):1);
		}
	}

	private static final class JumpAlwaysInstruction extends AbstractJumpInstruction {
		public JumpAlwaysInstruction(IntValueRetriever offsetRetriever) {
			super(offsetRetriever);
		}

		@Override
		public boolean shouldJump(State state) {
			return true;
		}

		@Override
		public InstructionType getInstructionType() {
			return InstructionType.JUMP_ALWAYS;
		}
	}

	private static final class JumpIfRegisterNotZeroInstruction extends AbstractJumpInstruction {
		private final int register;

		public JumpIfRegisterNotZeroInstruction(int register, IntValueRetriever offsetRetriever) {
			super(offsetRetriever);
			this.register = register;
		}

		public int getRegister() {
			return register;
		}

		@Override
		public boolean shouldJump(State state) {
			return state.getRegisterValue(this.register) != 0;
		}

		@Override
		public InstructionType getInstructionType() {
			return InstructionType.JUMP_IF_REGISTER_NOT_ZERO;
		}
	}

	private static final class NoopInstruction implements Instruction {
		private static final NoopInstruction INSTANCE = new NoopInstruction();

		private NoopInstruction() {}

		@Override
		public void run(State state) {
			state.adjustPC(1);
		}

		@Override
		public InstructionType getInstructionType() {
			return InstructionType.NOOP;
		}
	}

	private static final class JumpIfNotZeroInstructionFactory extends AbstractTwoOperandInstructionFactory {
		public static final JumpIfNotZeroInstructionFactory INSTANCE = new JumpIfNotZeroInstructionFactory();

		private JumpIfNotZeroInstructionFactory() {}

		@Override
		protected Instruction createInstruction(Operand firstOperand, Operand secondOperand) throws InvalidInstructionOperandsException {
			IntValueRetriever offsetRetriever = IntValueRetriever.createForOperand(secondOperand);
			switch (firstOperand.getOperandType()) {
				case REGISTER:
					return new JumpIfRegisterNotZeroInstruction(((RegisterOperand)firstOperand).getRegister(),offsetRetriever);
				case VALUE:
					long value = ((ValueOperand)firstOperand).getValue();
					if (value == 0) {
						return NoopInstruction.INSTANCE;
					} else {
						return new JumpAlwaysInstruction(offsetRetriever);
					}
				default:
					throw new InvalidInstructionOperandsException("Unexpected first operand type");
			}
		}

		@Override
		public InstructionFactory getToggleInstructionFactory() {
			return CopyIntoRegisterInstructionFactory.INSTANCE;
		}
	}

	private static class ToggleInstruction implements Instruction {
		private final IntValueRetriever offsetRetriever;

		public ToggleInstruction(IntValueRetriever offsetRetriever) {
			this.offsetRetriever = offsetRetriever;
		}

		public IntValueRetriever getOffsetRetriever() {
			return offsetRetriever;
		}

		@Override
		public final void run(State state) {
			int instructionIndex = state.getPC() + this.offsetRetriever.getIntValue(state);
			if (state.isValidInstructionIndex(instructionIndex)) {
				state.toggleInstruction(instructionIndex);
			}
			state.adjustPC(1);
		}

		@Override
		public InstructionType getInstructionType() {
			return InstructionType.TOGGLE;
		}
	}

	private static class ToggleInstructionFactory extends AbstractOneOperandInstructionFactory {
		public static final ToggleInstructionFactory INSTANCE = new ToggleInstructionFactory();

		private ToggleInstructionFactory() {}

		@Override
		protected Instruction createInstruction(Operand operand) throws InvalidInstructionOperandsException {
			return new ToggleInstruction(IntValueRetriever.createForOperand(operand));
		}
	}

	public static class InstructionPair {
		private final Instruction normalInstruction;
		private final Instruction toggledInstruction;

		public InstructionPair(InstructionPair instructionPair) {
			this(instructionPair.getNormalInstruction(), instructionPair.getToggledInstruction());
		}

		public InstructionPair(Instruction normalInstruction, Instruction toggledInstruction) {
			this.normalInstruction = normalInstruction;
			this.toggledInstruction = toggledInstruction;
		}

		public final Instruction getNormalInstruction() {
			return normalInstruction;
		}

		public final Instruction getToggledInstruction() {
			return toggledInstruction;
		}
	}

	public static class State {
		private final InstructionPair[] instructionsPairs;
		private final Instruction[] programWithToggles;
		private Instruction[] programWithTogglesAndOptimizations;
		private final boolean[] instructionsToggled;
		private final int instructionCount;
		private final long[] registers;
		private int pc;

		public State(InstructionPair[] instructionsPairs, int registerCount) {
			int instructionCount = instructionsPairs.length;
			this.instructionsPairs = Arrays.copyOf(instructionsPairs,instructionCount);
			this.programWithToggles = new Instruction[instructionCount];
			for (int instructionIndex=0; instructionIndex<instructionCount; instructionIndex++) {
				this.programWithToggles[instructionIndex] = instructionsPairs[instructionIndex].getNormalInstruction();
			}
			this.optimize();
			this.instructionsToggled = new boolean[instructionCount];
			this.instructionCount = instructionCount;
			this.registers = new long[registerCount];
		}

		private static boolean isInstructionJumpIfNotZeroOfRegisterAndOffset(Instruction instruction, int wantedRegister, int wantedOffset) {
			if (instruction.getInstructionType() != InstructionType.JUMP_IF_REGISTER_NOT_ZERO) {
				return false;
			}
			JumpIfRegisterNotZeroInstruction jumpIfRegisterNotZeroInstruction = (JumpIfRegisterNotZeroInstruction)instruction;
			if (jumpIfRegisterNotZeroInstruction.getRegister() != wantedRegister) {
				return false;
			}
			IntValueRetriever offsetRetriever = jumpIfRegisterNotZeroInstruction.getOffsetRetriever();
			if (offsetRetriever.getType() != ValueRetrieverType.STATIC) {
				return false;
			}
			StaticIntValueRetriever staticOffsetRetriever = (StaticIntValueRetriever)offsetRetriever;
			if (staticOffsetRetriever.getIntValue() != wantedOffset) {
				return false;
			}
			return true;
		}

		private void optimize() {
			this.programWithTogglesAndOptimizations = this.programWithToggles;
			boolean optimizationFound = false;
			int iterationsForIncrementBy = this.instructionCount-3;
			int iterationsForIncrementByMultiply = this.instructionCount-5;
			int instructionIndexIncrease = 1;
			for (int instructionIndex=0; instructionIndex<iterationsForIncrementBy; instructionIndex+=instructionIndexIncrease) {
				instructionIndexIncrease = 1;
				Instruction instruction1 = this.programWithTogglesAndOptimizations[instructionIndex];
				Instruction instruction2 = this.programWithTogglesAndOptimizations[instructionIndex+1];
				IncrementRegisterInstruction incrementRegisterInstruction;
				DecrementRegisterInstruction decrementRegisterInstruction;
				if (instruction1.getInstructionType() == InstructionType.INCREMENT_REGISTER && instruction2.getInstructionType() == InstructionType.DECREMENT_REGISTER) {
					incrementRegisterInstruction = (IncrementRegisterInstruction)instruction1;
					decrementRegisterInstruction = (DecrementRegisterInstruction)instruction2;
				}
				else if (instruction1.getInstructionType() == InstructionType.DECREMENT_REGISTER && instruction2.getInstructionType() == InstructionType.INCREMENT_REGISTER) {
					decrementRegisterInstruction = (DecrementRegisterInstruction)instruction1;
					incrementRegisterInstruction = (IncrementRegisterInstruction)instruction2;
				}
				else {
					continue;
				}
				int resultRegister = incrementRegisterInstruction.getRegister();
				int sourceRegister = decrementRegisterInstruction.getRegister();
				if (resultRegister == sourceRegister) {
					continue;
				}
				Instruction instruction3 = this.programWithToggles[instructionIndex+2];
				if (!isInstructionJumpIfNotZeroOfRegisterAndOffset(instruction3, sourceRegister, -2)) {
					continue;
				}
				if (!optimizationFound) {
					optimizationFound = true;
					this.programWithTogglesAndOptimizations = Arrays.copyOf(this.programWithToggles,this.instructionCount);
				}
				this.programWithTogglesAndOptimizations[instructionIndex+0] = new IncrementRegisterByInstruction(resultRegister,sourceRegister);
				this.programWithTogglesAndOptimizations[instructionIndex+1] = OptimizedAwayInstruction.INSTANCE;
				this.programWithTogglesAndOptimizations[instructionIndex+2] = OptimizedAwayInstruction.INSTANCE;
				instructionIndexIncrease = 3;
				if (instructionIndex < iterationsForIncrementByMultiply) {
					Instruction instruction4 = this.programWithTogglesAndOptimizations[instructionIndex+3];
					if (instruction4.getInstructionType() != InstructionType.DECREMENT_REGISTER) {
						continue;
					}
					DecrementRegisterInstruction decrementRegisterInstruction2 = (DecrementRegisterInstruction)instruction4;
					int sourceRegister2 = decrementRegisterInstruction2.getRegister();
					if (sourceRegister2 == sourceRegister || sourceRegister2 == resultRegister) {
						continue;
					}
					Instruction instruction5 = this.programWithToggles[instructionIndex+4];
					if (!isInstructionJumpIfNotZeroOfRegisterAndOffset(instruction5, sourceRegister2, -5)) {
						continue;
					}
					this.programWithTogglesAndOptimizations[instructionIndex+0] = new IncrementRegisterByMultiplicationInstruction(resultRegister, sourceRegister, sourceRegister2);
					this.programWithTogglesAndOptimizations[instructionIndex+3] = OptimizedAwayInstruction.INSTANCE;
					this.programWithTogglesAndOptimizations[instructionIndex+4] = OptimizedAwayInstruction.INSTANCE;
					instructionIndexIncrease = 5;
				}
			}
		}

		public void toggleInstruction(int index) {
			// Intentional = not ==
			InstructionPair instructionPair = this.instructionsPairs[index];
			Instruction instruction;
			if (this.instructionsToggled[index] = !this.instructionsToggled[index]) {
				instruction = instructionPair.getToggledInstruction();
			} else {
				instruction = instructionPair.getNormalInstruction();
			}
			this.programWithToggles[index] = instruction;
			this.optimize();
		}

		public void deoptimize() {
			this.programWithTogglesAndOptimizations = this.programWithToggles;
		}

		public boolean isValidInstructionIndex(int index) {
			return (index >= 0 && index < instructionCount);
		}

		public Instruction getInstruction(int index) {
			return programWithTogglesAndOptimizations[index];
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
		INSTRUCTION_FACTORY_MAP.put("cpy", CopyIntoRegisterInstructionFactory.INSTANCE);
		INSTRUCTION_FACTORY_MAP.put("inc",IncrementRegisterInstructionFactory.INSTANCE);
		INSTRUCTION_FACTORY_MAP.put("dec",DecrementRegisterInstructionFactory.INSTANCE);
		INSTRUCTION_FACTORY_MAP.put("jnz",JumpIfNotZeroInstructionFactory.INSTANCE);
		INSTRUCTION_FACTORY_MAP.put("tgl",ToggleInstructionFactory.INSTANCE);
	}

	private static InstructionPair parseInstructionStringIntoInstructionPair(String instructionString, int registerCount) {
		String[] instructionStringParts = instructionString.split(" ");
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
					if (register >= 0 && register < registerCount) {
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
		Instruction normalInstruction;
		Instruction toggledInstruction;
		try {
			normalInstruction = instructionFactory.createInstruction(operands);
		} catch (InvalidInstructionOperandsException ex) {
			throw new IllegalStateException("Cannot create normal instruction", ex);
		}
		try {
			toggledInstruction = instructionFactory.getToggleInstructionFactory().createInstruction(operands);
		} catch (InvalidInstructionOperandsException ex) {
			toggledInstruction = NoopInstruction.INSTANCE;
		}
		return new InstructionPair(normalInstruction, toggledInstruction);
	}

	public static InstructionPair[] parseProgram(char[] inputCharacters, int registerCount) {
		String[] instructionStringsArray = LineReader.stringsArray(inputCharacters, true);
		int instructionCount = instructionStringsArray.length;
		InstructionPair[] instructionPairsArray = new InstructionPair[instructionCount];
		for (int instructionIndex=0; instructionIndex<instructionCount; instructionIndex++) {
			instructionPairsArray[instructionIndex] = parseInstructionStringIntoInstructionPair(instructionStringsArray[instructionIndex], registerCount);
		}
		return instructionPairsArray;
	}

	public static long runProgramUntilTerminationWithIORegisters(InstructionPair[] instructionsPairs, int registerCount, int inputRegisterIndex, int outputRegisterIndex, long inputValue) {
		State state = new State(instructionsPairs, registerCount);
		state.setRegisterValue(inputRegisterIndex, inputValue);
		while (true) {
			int pc = state.getPC();
			if (!state.isValidInstructionIndex(pc)) {
				break;
			}
			state.getInstruction(pc).run(state);
		}
		return state.getRegisterValue(outputRegisterIndex);
	}
}
