package uk.co.scottdennison.java.soft.challenges.adventofcode.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IntcodeComputer {
    private static final int[] POWERS_OF_10 = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000};

    private final int[] memory;
    private final int[] inputs;
    private final List<Integer> outputs;

    private boolean running;
    private int instructionPointer;
    private int inputPointer;

    private int currentIntcode;

    public IntcodeComputer(int[] memory, int[] inputs) {
        this.memory = Arrays.copyOf(memory, memory.length);
        this.inputs = Arrays.copyOf(inputs, inputs.length);
        this.outputs = new ArrayList<>();
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

    public void run() {
        this.instructionPointer = 0;
        this.inputPointer = 0;
        this.outputs.clear();
        this.running = true;
        while (this.running) {
            this.step();
        }
    }

    public void step() {
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
                if (this.inputPointer >= this.inputs.length) {
                    throw new IllegalStateException("Not enough inputs");
                }
                writeParameter(1, this.inputs[this.inputPointer++]);
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
                this.running = false;
                break;
            default:
                throw new IllegalStateException("Tried to run invalid opcode: " + opCode);
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
                throw new IllegalStateException("Unexpected parameter mode");
        }
    }

    private void writeParameter(int parameterNumber, int value) {
        if (getParameterMode(parameterNumber) != 0) {
            throw new IllegalStateException("Invalid parameter mode");
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
