package uk.co.scottdennison.java.soft.challenges.adventofcode.common;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
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

	public static interface Instruction {
		void run(State state);
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

	private static final class CopyInstructionFactory extends AbstractTwoOperandInstructionFactory {
		public static final CopyInstructionFactory INSTANCE = new CopyInstructionFactory();

		private CopyInstructionFactory() {}

		@Override
		protected Instruction createInstruction(Operand firstOperand, Operand secondOperand) throws InvalidInstructionOperandsException {
			if (secondOperand.getOperandType() != OperandType.REGISTER) {
				throw new InvalidInstructionOperandsException("Second operand should be a register");
			}
			int destinationRegister = ((RegisterOperand)secondOperand).getRegister();
			switch (firstOperand.getOperandType()) {
				case REGISTER:
					return new CopyRegisterToRegisterInstruction(((RegisterOperand)firstOperand).getRegister(),destinationRegister);
				case VALUE:
					return new SetRegisterValueInstruction(((ValueOperand)firstOperand).getValue(),destinationRegister);
				default:
					throw new InvalidInstructionOperandsException("Unexpected first operand type");
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

		@Override
		public void run(State state) {
			state.mutateRegisterValue(register, -1);
			state.adjustPC(1);
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

	private static interface IntValueRetriever {
		int getIntValue(State state);

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

		@Override
		public int getIntValue(State state) {
			return this.value;
		}
	}

	private static class RegisterIntValueRetriever implements IntValueRetriever {
		private final int register;

		public RegisterIntValueRetriever(int register) {
			this.register = register;
		}

		@Override
		public int getIntValue(State state) {
			long registerValue = state.getRegisterValue(this.register);
			if (registerValue < Integer.MIN_VALUE || registerValue > Integer.MAX_VALUE) {
				throw new IllegalStateException("Value out of bounds");
			}
			return (int)registerValue;
		}
	}

	private static abstract class AbstractJumpInstruction implements Instruction {
		private final IntValueRetriever offsetRetriever;

		public AbstractJumpInstruction(IntValueRetriever offsetRetriever) {
			this.offsetRetriever = offsetRetriever;
		}

		public abstract boolean shouldJump(State state);

		@Override
		public final void run(State state) {
			state.adjustPC(shouldJump(state)?this.offsetRetriever.getIntValue(state):1);
		}
	}

	private static final class JumpInstruction extends AbstractJumpInstruction {
		public JumpInstruction(IntValueRetriever offsetRetriever) {
			super(offsetRetriever);
		}

		@Override
		public boolean shouldJump(State state) {
			return true;
		}
	}

	private static final class JumpIfRegisterNotZeroInstruction extends AbstractJumpInstruction {
		private final int register;

		public JumpIfRegisterNotZeroInstruction(int register, IntValueRetriever offsetRetriever) {
			super(offsetRetriever);
			this.register = register;
		}

		@Override
		public boolean shouldJump(State state) {
			return state.getRegisterValue(this.register) != 0;
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
						return new JumpInstruction(offsetRetriever);
					}
				default:
					throw new InvalidInstructionOperandsException("Unexpected first operand type");
			}
		}

		@Override
		public InstructionFactory getToggleInstructionFactory() {
			return CopyInstructionFactory.INSTANCE;
		}
	}

	private static class ToggleInstruction implements Instruction {
		private final IntValueRetriever offsetRetriever;

		public ToggleInstruction(IntValueRetriever offsetRetriever) {
			this.offsetRetriever = offsetRetriever;
		}

		@Override
		public final void run(State state) {
			int instructionIndex = state.getPC() + this.offsetRetriever.getIntValue(state);
			if (state.isValidInstructionIndex(instructionIndex)) {
				state.getInstructionPair(instructionIndex).toggle();
			}
			state.adjustPC(1);
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

	public static class ToggleableInstructionPair extends InstructionPair {
		private Instruction currentInstruction;
		private boolean toggled;

		public ToggleableInstructionPair(InstructionPair instructionPair) {
			super(instructionPair);
			init();
		}

		public ToggleableInstructionPair(Instruction normalInstruction, Instruction toggledInstruction) {
			super(normalInstruction, toggledInstruction);
			init();
		}

		private void init() {
			this.currentInstruction = getNormalInstruction();
			this.toggled = false;
		}

		public Instruction getCurrentInstruction() {
			return this.currentInstruction;
		}

		public void toggle() {
			if (this.toggled) {
				this.toggled = false;
				this.currentInstruction = getNormalInstruction();
			} else {
				this.toggled = true;
				this.currentInstruction = getToggledInstruction();
			}
		}
	}

	public static class State {
		private final ToggleableInstructionPair[] instructionsPairs;
		private final int instructionCount;
		private final long[] registers;
		private int pc;

		public State(InstructionPair[] instructionsPairs, int registerCount) {
			int instructionCount = instructionsPairs.length;
			this.instructionCount = instructionCount;
			ToggleableInstructionPair[] toggleableInstructionPairs = new ToggleableInstructionPair[instructionCount];
			for (int instructionIndex=0; instructionIndex<instructionCount; instructionIndex++) {
				toggleableInstructionPairs[instructionIndex] = new ToggleableInstructionPair(instructionsPairs[instructionIndex]);
			}
			this.instructionsPairs = toggleableInstructionPairs;
			this.registers = new long[registerCount];
		}

		public boolean isValidInstructionIndex(int index) {
			return (index >= 0 && index < instructionCount);
		}

		public ToggleableInstructionPair getInstructionPair(int index) {
			return instructionsPairs[index];
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
			state.getInstructionPair(pc).getCurrentInstruction().run(state);
		}
		return state.getRegisterValue(outputRegisterIndex);
	}
}
