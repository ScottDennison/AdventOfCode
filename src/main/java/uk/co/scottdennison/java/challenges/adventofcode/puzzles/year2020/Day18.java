package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2020;

import uk.co.scottdennison.java.challenges.adventofcode.utils.InputFileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.LongBinaryOperator;

public class Day18 {
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

	public static void main(String[] args) throws IOException {
		List<String> fileLines = Files.readAllLines(InputFileUtils.getInputPath());
		outputSummary(fileLines, "normal", 1, 2);
		outputSummary(fileLines, "part 1", 1, 1);
		outputSummary(fileLines, "part 2", 2, 1);
	}

	private static void outputSummary(List<String> fileLines, String precedenceDescription, int additionSubtractionPrecedence, int multiplicationDivisionPrecedence) {
		Map<Character, Operator> operators = new HashMap<>();
		//noinspection Convert2MethodRef
		operators.put('+', new BasicLeftAssociativeBinaryOperator(additionSubtractionPrecedence, (x, y) -> x + y));
		operators.put('-', new BasicLeftAssociativeBinaryOperator(additionSubtractionPrecedence, (x, y) -> x - y));
		operators.put('*', new BasicLeftAssociativeBinaryOperator(multiplicationDivisionPrecedence, (x, y) -> x * y));
		operators.put('/', new BasicLeftAssociativeBinaryOperator(multiplicationDivisionPrecedence, (x, y) -> x * y));
		System.out.format("Total when running expressions using %s precedence rules: %d%n", precedenceDescription, totalExpressions(fileLines, operators));
	}

	private static long totalExpressions(List<String> fileLines, Map<Character, Operator> operators) {
		long runningTotal = 0;
		for (String fileLine : fileLines) {
			int fileLineLength = fileLine.length();
			char[] fileLineCharacters = new char[fileLineLength + 2];
			fileLineCharacters[0] = '(';
			fileLine.getChars(0, fileLineLength, fileLineCharacters, 1);
			fileLineCharacters[fileLineLength + 1] = ')';
			Deque<Long> outputDeque = new ArrayDeque<>();
			Deque<Character> operatorStack = new ArrayDeque<>();
			boolean buildingNumber = false;
			for (char fileLineCharacter : fileLineCharacters) {
				if (!Character.isWhitespace(fileLineCharacter)) {
					if (fileLineCharacter >= '0' && fileLineCharacter <= '9') {
						long value = fileLineCharacter - '0';
						if (buildingNumber) {
							value = (outputDeque.removeLast() * 10) + value;
						}
						outputDeque.addLast(value);
						buildingNumber = true;
					}
					else {
						buildingNumber = false;
						Operator newOperator = operators.get(fileLineCharacter);
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
							operatorStack.addFirst(fileLineCharacter);
						}
						else {
							switch (fileLineCharacter) {
								case '(':
									operatorStack.addFirst(fileLineCharacter);
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
									throw new IllegalStateException("Unexpected character " + ((int) fileLineCharacter) + ".");
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
