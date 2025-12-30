package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2025;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.function.LongBinaryOperator;
import java.util.regex.Pattern;

public class Day06 implements IPuzzle {
    private static final Pattern PATTERN_SPACES = Pattern.compile(" +");

    @FunctionalInterface
    private static interface SumDigitLookup {
        char getDigit(int operandIndex, int operandDigitIndex);
    }

    private static long calculateSum(LongBinaryOperator sumOperator, long total, int operandCount, int maxOperandDigitCount, SumDigitLookup sumDigitLookup) {
        char[] operand = new char[maxOperandDigitCount];
        for (int operandIndex = 0; operandIndex < operandCount; operandIndex++) {
            for (int operandDigitIndex = 0; operandDigitIndex < maxOperandDigitCount; operandDigitIndex++) {
                operand[operandDigitIndex] = sumDigitLookup.getDigit(operandIndex, operandDigitIndex);
            }
            String operandString = new String(operand).trim();
            if (!operandString.isEmpty()) {
                total = sumOperator.applyAsLong(total, Long.parseLong(operandString));
            }
        }
        return total;
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        char[][] inputGrid = LineReader.charArraysArray(inputCharacters, true);
        int numberLinesCount = inputGrid.length - 1;
        char[] operatorsLine = inputGrid[numberLinesCount];
        int inputWidth = operatorsLine.length;
        /*
        String[] operatorStrings = PATTERN_SPACES.split(inputLines[numberLinesCount]);
        int operatorCount = operatorStrings.length;
        LongBinaryOperator[] operators = new LongBinaryOperator[operatorCount];
        long[] totals = new long[operatorCount];
        for (int sumIndex = 0; sumIndex < operatorCount; sumIndex++) {
            String operatorString = operatorStrings[sumIndex].trim();
            if (operatorString.length() != 1) {
                throw new IllegalArgumentException("Expected a single character");
            }
            LongBinaryOperator operator;
            long total;
            switch (operatorString.charAt(0)) {
                case '*':
                    operator = Math::multiplyExact;
                    total = 1;
                    break;
                case '+':
                    operator = Math::addExact;
                    total = 0;
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected operator");
            }
            operators[sumIndex] = operator;
            totals[sumIndex] = total;
        }
        for (int numberLineIndex=0; numberLineIndex < numberLinesCount; numberLineIndex++) {
            long[] operands = PATTERN_SPACES.splitAsStream(inputLines[numberLineIndex].trim()).mapToLong(Long::parseLong).toArray();
            if (operands.length != operatorCount) {
                throw new IllegalStateException("Expected " + operatorCount + " operators, but got " + operands.length);
            }
            for (int sumIndex=0; sumIndex < operatorCount; sumIndex++) {
                totals[sumIndex] = operators[sumIndex].applyAsLong(totals[sumIndex],operands[sumIndex]);
            }
        }
        */
        int nextSumStartIndex = 0;
        long partATotal = 0;
        long partBTotal = 0;
        while (nextSumStartIndex < inputWidth) {
            int sumStartIndex = nextSumStartIndex;
            long sumStartTotal;
            LongBinaryOperator sumOperator;
            switch (operatorsLine[sumStartIndex]) {
                case '*':
                    sumOperator = Math::multiplyExact;
                    sumStartTotal = 1;
                    break;
                case '+':
                    sumOperator = Math::addExact;
                    sumStartTotal = 0;
                    break;
                case ' ':
                    throw new IllegalArgumentException("Expected an operator where there was not one.");
                default:
                    throw new IllegalArgumentException("Unexpected operator");
            }
            nextSumStartIndex = sumStartIndex + 1;
            while (nextSumStartIndex < inputWidth && operatorsLine[nextSumStartIndex] == ' ') {
                nextSumStartIndex++;
            }
            int rawSumWidth = nextSumStartIndex - sumStartIndex;
            partATotal = Math.addExact(partATotal, calculateSum(sumOperator, sumStartTotal, numberLinesCount, rawSumWidth, (operandIndex, operandDigitIndex) -> inputGrid[operandIndex][operandDigitIndex + sumStartIndex]));
            partBTotal = Math.addExact(partBTotal, calculateSum(sumOperator, sumStartTotal, rawSumWidth, numberLinesCount, (operandIndex, operandDigitIndex) -> inputGrid[operandDigitIndex][operandIndex + sumStartIndex]));
        }
        return new BasicPuzzleResults<>(
            partATotal,
            partBTotal
        );
    }
}
