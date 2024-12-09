package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2020;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.LongBinaryOperator;

public class Day18 implements IPuzzle {
	private interface Operator {
		int getPrecedence();

		boolean isLeftAssociative();

		void run(Deque<Long> outputDeque);
	}

	private static class BasicLeftAssociativeBinaryOperator implements Operator {
		private final int precedence;
		private final LongBinaryOperator action;

		public BasicLeftAssociativeBinaryOperator(int precedence, LongBinaryOperator action) {
			this.precedence = precedence;
			this.action = action;
		}

		@Override
		public boolean isLeftAssociative() {
			return true;
		}

		@Override
		public int getPrecedence() {
			return this.precedence;
		}

		@Override
		public void run(Deque<Long> outputDeque) {
			long value2 = outputDeque.removeLast();
			long value1 = outputDeque.removeLast();
			outputDeque.addLast(this.action.applyAsLong(value1, value2));
		}
	}

	private static void runOperator(Map<Character, Operator> operators, Deque<Long> outputDeque, char operatorCharacter) {
		operators.get(operatorCharacter).run(outputDeque);
	}

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		List<String> inputLines = LineReader.stringsList(inputCharacters, true);
		runOutputtingSummary(printWriter, inputLines, "normal", 1, 2);
		long partAResult = runOutputtingSummary(printWriter, inputLines, "part A", 1, 1);
		long partBResult = runOutputtingSummary(printWriter, inputLines, "part B", 2, 1);
		return new BasicPuzzleResults<>(
			partAResult,
			partBResult
		);
	}

	private static long runOutputtingSummary(PrintWriter printWriter, List<String> inputLines, String precedenceDescription, int additionSubtractionPrecedence, int multiplicationDivisionPrecedence) {
		Map<Character, Operator> operators = new HashMap<>();
		//noinspection Convert2MethodRef
		operators.put('+', new BasicLeftAssociativeBinaryOperator(additionSubtractionPrecedence, (x, y) -> x + y));
		operators.put('-', new BasicLeftAssociativeBinaryOperator(additionSubtractionPrecedence, (x, y) -> x - y));
		operators.put('*', new BasicLeftAssociativeBinaryOperator(multiplicationDivisionPrecedence, (x, y) -> x * y));
		operators.put('/', new BasicLeftAssociativeBinaryOperator(multiplicationDivisionPrecedence, (x, y) -> x * y));
		long totalExpressions = totalExpressions(inputLines, operators);
		printWriter.format("Total when running expressions using %s precedence rules: %d%n", precedenceDescription, totalExpressions);
		printWriter.flush();
		return totalExpressions;
	}

	private static long totalExpressions(List<String> inputLines, Map<Character, Operator> operators) {
		long runningTotal = 0;
		for (String inputLine : inputLines) {
			int inputLineLength = inputLine.length();
			char[] inputLineCharacters = new char[inputLineLength + 2];
			inputLineCharacters[0] = '(';
			inputLine.getChars(0, inputLineLength, inputLineCharacters, 1);
			inputLineCharacters[inputLineLength + 1] = ')';
			Deque<Long> outputDeque = new ArrayDeque<>();
			Deque<Character> operatorStack = new ArrayDeque<>();
			boolean buildingNumber = false;
			for (char inputLineCharacter : inputLineCharacters) {
				if (!Character.isWhitespace(inputLineCharacter)) {
					if (inputLineCharacter >= '0' && inputLineCharacter <= '9') {
						long value = inputLineCharacter - '0';
						if (buildingNumber) {
							value = (outputDeque.removeLast() * 10) + value;
						}
						outputDeque.addLast(value);
						buildingNumber = true;
					}
					else {
						buildingNumber = false;
						Operator newOperator = operators.get(inputLineCharacter);
						if (newOperator != null) {
							int newOperatorPrecedence = newOperator.getPrecedence();
							Character stackOperatorCharacter;
							while ((stackOperatorCharacter = operatorStack.pollFirst()) != null) {
								boolean runOperator;
								if (stackOperatorCharacter == '(') {
									runOperator = false;
								}
								else {
									Operator stackOperator = operators.get(stackOperatorCharacter);
									int stackOperatorPrecedence = stackOperator.getPrecedence();
									runOperator = (stackOperatorPrecedence > newOperatorPrecedence || (stackOperatorPrecedence == newOperatorPrecedence && newOperator.isLeftAssociative()));
								}
								if (runOperator) {
									runOperator(operators, outputDeque, stackOperatorCharacter);
								}
								else {
									operatorStack.addFirst(stackOperatorCharacter);
									break;
								}
							}
							operatorStack.addFirst(inputLineCharacter);
						}
						else {
							switch (inputLineCharacter) {
								case '(':
									operatorStack.addFirst(inputLineCharacter);
									break;
								case ')':
									while (true) {
										Character operatorCharacter = operatorStack.pollFirst();
										if (operatorCharacter == null) {
											throw new IllegalStateException("Mismatched brackets");
										}
										else if (operatorCharacter == '(') {
											break;
										}
										else {
											runOperator(operators, outputDeque, operatorCharacter);
										}
									}
									break;
								default:
									throw new IllegalStateException("Unexpected character " + ((int) inputLineCharacter) + ".");
							}
						}
					}
				}
			}
			Character operator;
			while ((operator = operatorStack.pollFirst()) != null) {
				if (operator == '(') {
					throw new IllegalStateException("Mismatched brackets");
				}
				else {
					runOperator(operators, outputDeque, operator);
				}
			}
			runningTotal += outputDeque.removeLast();
			if (!outputDeque.isEmpty()) {
				throw new IllegalStateException("Expected only one value on the output deque");
			}
		}
		return runningTotal;
	}
}
