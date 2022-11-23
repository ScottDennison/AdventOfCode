package uk.co.scottdennison.java.soft.challenges.adventofcode.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class IntcodeComputer {
    public static enum State {
        NOT_STARTED,
        RUNNING,
        AWAITING_INPUT,
        HALTED
    }

    public static class InvalidIntcodeComputerStateException extends IllegalStateException {
        private InvalidIntcodeComputerStateException(String message) {
            super(message);
        }
    }

    public static class InsufficentInputsException extends IllegalStateException {
        private InsufficentInputsException(String message) {
            super(message);
        }
    }

    public static class IllegalProgramException extends IllegalStateException {
        private IllegalProgramException(String message) {
            super(message);
        }
    }

    private static final int[] POWERS_OF_10 = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000};

    private final int[] memory;
    private final List<Integer> inputs;
    private final List<Integer> outputs;

    private State state;
    private boolean inputsLocked;
    private int instructionPointer;
    private int inputPointer;

    private int currentIntcode;

    public IntcodeComputer(int[] memory) {
        this.memory = Arrays.copyOf(memory, memory.length);
        this.inputs = new ArrayList<>();
        this.outputs = new ArrayList<>();
        this.instructionPointer = 0;
        this.inputPointer = 0;
        this.state = State.NOT_STARTED;
    }

    private void ensureInputsUnlocked() {
        if (this.inputsLocked) {
            throw new IllegalStateException("This operation cannot be done while inputs are locked.");
        }
    }

    public void addInput(int input) {
        ensureInputsUnlocked();
        this.inputs.add(input);
    }

    public void addInputs(int... inputs) {
        ensureInputsUnlocked();
        Arrays.stream(inputs).boxed().forEach(this.inputs::add);
    }

    public void addInputs(Collection<Integer> inputs) {
        ensureInputsUnlocked();
        this.inputs.addAll(inputs);
    }

    public void lockInputs() {
        this.inputsLocked = true;
    }

    public int getMemory(int index) {
        return this.memory[index];
    }

    public int getOutputCount() {
        return this.outputs.size();
    }

    public int getOutput(int index) {
        return this.outputs.get(index);
    }

    public State getState() {
        return this.state;
    }

    public void runFully() {
        if (this.state != State.NOT_STARTED) {
            throw new InvalidIntcodeComputerStateException("The program was already partially run.");
        }
        this.lockInputs();
        this.run();
        if (this.state != State.HALTED) {
            throw new InvalidIntcodeComputerStateException("The program did not halt.");
        }
    }

    public void run() {
        do {
            this.step();
        } while (this.state == State.RUNNING);
    }

    public void step() {
        switch (this.state) {
            case HALTED:
                throw new InvalidIntcodeComputerStateException("The program is halted.");
            case AWAITING_INPUT:
                if (this.inputPointer >= this.inputs.size()) {
                    throw new InsufficentInputsException("Attempting to resume running when awaiting input but no new inputs added.");
                }
                break;
        }
        this.state = State.RUNNING;
        int opCode = (this.currentIntcode = this.memory[this.instructionPointer]) % 100;
        switch (opCode) {
            case 1:
                writeParameter(3, readParameter(1) + readParameter(2));
                this.instructionPointer += 4;
                break;
            case 2:
                writeParameter(3, readParameter(1) * readParameter(2));
                this.instructionPointer += 4;
                break;
            case 3:
                if (this.inputPointer >= this.inputs.size()) {
                    if (this.inputsLocked) {
                        throw new InsufficentInputsException("Not enough inputs, and inputs are locked.");
                    }
                    else {
                        this.state = State.AWAITING_INPUT;
                        break;
                    }
                }
                writeParameter(1, this.inputs.get(this.inputPointer++));
                this.instructionPointer += 2;
                break;
            case 4:
                outputs.add(this.readParameter(1));
                this.instructionPointer += 2;
                break;
            case 5:
                if (this.readParameter(1) != 0) {
                    this.instructionPointer = this.readParameter(2);
                } else {
                    this.instructionPointer += 3;
                }
                break;
            case 6:
                if (this.readParameter(1) == 0) {
                    this.instructionPointer = this.readParameter(2);
                } else {
                    this.instructionPointer += 3;
                }
                break;
            case 7:
                this.writeParameter(3, this.readParameter(1) < this.readParameter(2) ? 1 : 0);
                this.instructionPointer += 4;
                break;
            case 8:
                this.writeParameter(3, this.readParameter(1) == this.readParameter(2) ? 1 : 0);
                this.instructionPointer += 4;
                break;
            case 99:
                this.state = State.HALTED;
                break;
            default:
                throw new IllegalProgramException("Tried to run invalid opcode: " + opCode);
        }
    }

    private int readParameter(int parameterNumber) {
        int value = memory[instructionPointer + parameterNumber];
        switch (getParameterMode(parameterNumber)) {
            case 0:
                return memory[value];
            case 1:
                return value;
            default:
                throw new IllegalProgramException("Unexpected parameter mode");
        }
    }

    private void writeParameter(int parameterNumber, int value) {
        if (getParameterMode(parameterNumber) != 0) {
            throw new IllegalProgramException("Invalid parameter mode");
        }
        memory[memory[instructionPointer + parameterNumber]] = value;
    }

    private int getParameterMode(int parameterNumber) {
        return (currentIntcode / POWERS_OF_10[parameterNumber + 1]) % 10;
    }

    public static final int[] readProgram(char[] inputCharacters) {
        return Arrays.stream(new String(inputCharacters).trim().split(",")).mapToInt(Integer::parseInt).toArray();
    }
}
