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

    private static final long[] POWERS_OF_10 = {
        1L,
        10L,
        100L,
        1000L,
        10000L,
        100000L,
        1000000L,
        10000000L,
        100000000L,
        1000000000L,
        10000000000L,
        100000000000L,
        1000000000000L,
        10000000000000L,
        100000000000000L,
        1000000000000000L,
        10000000000000000L,
        100000000000000000L,
        1000000000000000000L
    };

    private long[] memory;
    private final List<Long> inputs;
    private final List<Long> outputs;

    private State state;
    private boolean inputsLocked;
    private long instructionPointer;
    private int inputPointer;
    private long relativeBase;

    private long currentIntcode;

    public IntcodeComputer(long[] memory) {
        this.memory = Arrays.copyOf(memory, memory.length);
        this.inputs = new ArrayList<>();
        this.outputs = new ArrayList<>();
        this.instructionPointer = 0;
        this.inputPointer = 0;
        this.relativeBase = 0;
        this.state = State.NOT_STARTED;
    }

    private void ensureInputsUnlocked() {
        if (this.inputsLocked) {
            throw new IllegalStateException("This operation cannot be done while inputs are locked.");
        }
    }

    public void addInput(long input) {
        this.ensureInputsUnlocked();
        this.inputs.add(input);
    }

    public void addInputs(long... inputs) {
        this.ensureInputsUnlocked();
        Arrays.stream(inputs).boxed().forEach(this.inputs::add);
    }

    public void addInputs(Collection<Long> inputs) {
        this.ensureInputsUnlocked();
        this.inputs.addAll(inputs);
    }

    public void lockInputs() {
        this.inputsLocked = true;
    }

    public long getMemory(int index) {
        return this.readMemory(index, false);
    }

    public int getOutputCount() {
        return this.outputs.size();
    }

    public long getOutput(int index) {
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
        int opCode = (int)((this.currentIntcode = this.readMemory(this.instructionPointer)) % 100);
        switch (opCode) {
            case 1:
                this.writeParameter(3, this.readParameter(1) + this.readParameter(2));
                this.instructionPointer += 4;
                break;
            case 2:
                this.writeParameter(3, this.readParameter(1) * this.readParameter(2));
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
                this.writeParameter(1, this.inputs.get(this.inputPointer++));
                this.instructionPointer += 2;
                break;
            case 4:
                this.outputs.add(this.readParameter(1));
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
            case 9:
                this.relativeBase += this.readParameter(1);
                this.instructionPointer += 2;
                break;
            case 99:
                this.state = State.HALTED;
                break;
            default:
                throw new IllegalProgramException("Tried to run invalid opcode: " + opCode);
        }
    }

    private long readMemory(int address) {
        return this.readMemory(address, true);
    }

    private long readMemory(int address, boolean allowOutside) {
        if (address < 0) {
            throw new IllegalProgramException("Tried to access negative memory address.");
        }
        if (address >= this.memory.length) {
            if (allowOutside) {
                return 0;
            }
            else {
                throw new IllegalProgramException("Tried to access memory address that is too high.");
            }
        }
        return this.memory[address];
    }

    private long readMemory(long address) {
        return this.readMemory(address, true);
    }

    private long readMemory(long address, boolean allowOutside) {
        if (address < Integer.MIN_VALUE || address > Integer.MAX_VALUE) {
            throw new IllegalProgramException("Tried to read memory that is outside of 32-bit range.");
        }
        return this.readMemory((int)address, allowOutside);
    }

    private void writeMemory(int address, long value) {
        if (address < 0) {
            throw new IllegalProgramException("Tried to access negative memory address.");
        }
        if (address >= this.memory.length) {
            long[] newMemory = new long[(address*2)+1];
            System.arraycopy(this.memory,0,newMemory,0,this.memory.length);
            this.memory = newMemory;
        }
        this.memory[address] = value;
    }

    private void writeMemory(long address, long value) {
        if (address < Integer.MIN_VALUE || address > Integer.MAX_VALUE) {
            throw new IllegalProgramException("Tried to write memory that is outside of 32-bit range.");
        }
        this.writeMemory((int)address, value);
    }

    private long readParameter(int parameterNumber) {
        long parameterValue = this.readMemory(this.instructionPointer + parameterNumber);
        switch (this.getParameterMode(parameterNumber)) {
            case 0:
                return this.readMemory(parameterValue);
            case 1:
                return parameterValue;
            case 2:
                return this.readMemory(parameterValue + this.relativeBase);
            default:
                throw new IllegalProgramException("Unexpected parameter mode");
        }
    }

    private void writeParameter(int parameterNumber, long value) {
        long parameterValue =  this.readMemory(this.instructionPointer + parameterNumber);
        switch (this.getParameterMode(parameterNumber)) {
            case 0:
                this.writeMemory(parameterValue, value);
                break;
            case 1:
                throw new IllegalProgramException("Parameter mode 1 is invalid for writing");
            case 2:
                this.writeMemory(parameterValue + this.relativeBase, value);
                break;
            default:
                throw new IllegalProgramException("Unexpected parameter mode");
        }
    }

    private int getParameterMode(int parameterNumber) {
        return (int)((this.currentIntcode / IntcodeComputer.POWERS_OF_10[parameterNumber + 1]) % 10);
    }

    public static final long[] readProgram(char[] inputCharacters) {
        return Arrays.stream(new String(inputCharacters).trim().split(",")).mapToLong(Long::parseLong).toArray();
    }
}
