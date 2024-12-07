package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2024;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;

public class Day07 implements IPuzzle {
    private static boolean recurse(long firstOperand, long runnihgTotal, long[] operands, int operandIndex, boolean includeConcatenation) {
        if (operandIndex == 0) {
            if (runnihgTotal == firstOperand) {
                return true;
            }
            else {
                return false;
            }
        }
        long operand = operands[operandIndex];
        int nextOperandIndex = operandIndex - 1;
        long subtractTotal = runnihgTotal - operand;
        if (subtractTotal >= firstOperand) {
            if (recurse(firstOperand, subtractTotal, operands, nextOperandIndex, includeConcatenation)) {
                return true;
            }
        }
        long divideResult = runnihgTotal / operand;
        if (divideResult >= firstOperand && (divideResult * operand) == runnihgTotal) {
            if (recurse(firstOperand, divideResult, operands, nextOperandIndex, includeConcatenation)) {
                return true;
            }
        }
        if (includeConcatenation) {
            long concatDivisor = (long)Math.pow(10, Math.ceil(Math.log10(operand + 1)));
            if (runnihgTotal > concatDivisor) {
                long concatResult = runnihgTotal / concatDivisor;
                if (concatResult >= firstOperand && ((runnihgTotal - (concatDivisor * concatResult)) == operand)) {
                    if (recurse(firstOperand, concatResult, operands, nextOperandIndex, true)) {
                        return true;
                    }
                }
            }
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
            int lastOperandIndex = operandCount - 1;
            if (recurse(operands[0], target, operands, lastOperandIndex, false)) {
                partASum += target;
                partBSum += target;
            }
            else if (recurse(operands[0], target, operands, lastOperandIndex, true)) {
                partBSum += target;
            }
        }
        return new BasicPuzzleResults<>(
            partASum,
            partBSum
        );
    }
}
