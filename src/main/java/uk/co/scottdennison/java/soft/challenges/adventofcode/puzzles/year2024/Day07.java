package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2024;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;

public class Day07 implements IPuzzle {
    private static boolean recurse(long target, long runnihgTotal, long[] operands, int operandIndex, int operandCount, boolean includeConcatenation) {
        if (runnihgTotal > target) {
            return false;
        }
        if (operandIndex >= operandCount) {
            if (runnihgTotal == target) {
                return true;
            }
            else {
                return false;
            }
        }
        long operand = operands[operandIndex];
        int nextOperandIndex = operandIndex + 1;
        if (recurse(target, runnihgTotal + operand, operands, nextOperandIndex, operandCount, includeConcatenation)) {
            return true;
        }
        if (recurse(target, runnihgTotal * operand, operands, nextOperandIndex, operandCount, includeConcatenation)) {
            return true;
        }
        if (includeConcatenation && recurse(target, (long)((runnihgTotal * Math.pow(10, Math.ceil(Math.log10(operand + 1)))) + operand), operands, nextOperandIndex, operandCount, includeConcatenation)) {
            return true;
        }
        return false;
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        long partASum = 0;
        long partBSum = 0;
        for (String inputLine : LineReader.strings(inputCharacters)) {
            String[] lineParts = inputLine.split(":");
            if (lineParts.length != 2) {
                throw new IllegalStateException("Could not parse input line");
            }
            long target = Long.parseLong(lineParts[0]);
            String[] operandStrings = lineParts[1].trim().split(" ");
            int operandCount = operandStrings.length;
            long[] operands = new long[operandCount];
            for (int operandIndex = 0; operandIndex < operandCount; operandIndex++) {
                operands[operandIndex] = Long.parseLong(operandStrings[operandIndex].trim());
            }
            if (recurse(target, operands[0], operands, 1, operandCount, false)) {
                partASum += target;
                partBSum += target;
            }
            else if (recurse(target, operands[0], operands, 1, operandCount, true)) {
                partBSum += target;
            }
        }
        return new BasicPuzzleResults<>(
            partASum,
            partBSum
        );
    }
}
