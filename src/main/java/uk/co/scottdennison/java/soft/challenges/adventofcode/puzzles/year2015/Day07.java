package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2015;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day07 implements IPuzzle {
	private static final int BITS = 16;
	private static final String VARIABLE_NAME_WANTED = "a";
	private static final String VARIABLE_NAME_TO_REPLACE = "b";

	private static final int MASK = ((1 << BITS) - 1);
	private static final Pattern PATTERN = Pattern.compile("^(?:(?<singularExpression>(?:(?<singularNot>NOT) )?(?:(?<singularNumber>[0-9]+)|(?<singularVariable>[a-z]+)))|(?<dualExpression>(?:(?<leftNumber>[0-9]+)|(?<leftVariable>[a-z]+)) (?<operator>[A-Z]+) (?:(?<rightNumber>[0-9]+)|(?<rightVariable>[a-z]+)))) -> (?<outputVariable>[a-z]+)$");

	private enum DualOperator {
		AND {
			@Override
			public long getValue(long leftValue, long rightValue) {
				return leftValue & rightValue;
			}
		},
		OR {
			@Override
			public long getValue(long leftValue, long rightValue) {
				return leftValue | rightValue;
			}
		},
		LSHIFT {
			@Override
			public long getValue(long leftValue, long rightValue) {
				return (leftValue << rightValue) & MASK;
			}
		},
		RSHIFT {
			@Override
			public long getValue(long leftValue, long rightValue) {
				return (leftValue >>> rightValue) & MASK;
			}
		};

		public abstract long getValue(long leftValue, long rightValue);
	}

	private static class DualExpression implements Expression {
		private final Value leftValue;
		private final DualOperator operator;
		private final Value rightValue;

		public DualExpression(Value leftValue, DualOperator operator, Value rightValue) {
			this.leftValue = leftValue;
			this.operator = operator;
			this.rightValue = rightValue;
		}

		@Override
		public long getValue(ExpressionManager expressionManager) {
			return this.operator.getValue(this.leftValue.getValue(expressionManager), this.rightValue.getValue(expressionManager));
		}
	}

	private static class SingularExpression implements Expression {
		private final boolean not;
		private final Value value;

		private SingularExpression(boolean not, Value value) {
			this.not = not;
			this.value = value;
		}

		@Override
		public long getValue(ExpressionManager expressionManager) {
			long longValue = this.value.getValue(expressionManager);
			if (this.not) {
				longValue = (~longValue) & MASK;
			}
			return longValue;
		}
	}

	private interface Expression {
		long getValue(ExpressionManager expressionManager);
	}

	private static class VariableValue implements Value {
		private final String variableName;

		private VariableValue(String variableName) {
			this.variableName = variableName;
		}

		@Override
		public long getValue(ExpressionManager expressionManager) {
			return expressionManager.getValue(this.variableName);
		}
	}

	private static class ConstantValue implements Value {
		private final long value;

		public ConstantValue(long value) {
			this.value = value & MASK;
		}

		@Override
		public long getValue(ExpressionManager expressionManager) {
			return this.value;
		}
	}

	private interface Value {
		long getValue(ExpressionManager expressionManager);
	}

	private static class ExpressionManager {
		private final Map<String, Expression> expressions;
		private final Map<String, Long> alreadyCalculatedValues;

		public ExpressionManager(Map<String, Expression> expressions) {
			this.expressions = new HashMap<>(expressions);
			this.alreadyCalculatedValues = new HashMap<>();
		}

		public long getValue(String name) {
			Long value = this.alreadyCalculatedValues.get(name);
			if (value == null) {
				Expression expression = this.expressions.get(name);
				if (expression == null) {
					throw new IllegalStateException("No expression with name " + name);
				}
				value = expression.getValue(this);
				this.alreadyCalculatedValues.put(name, value);
			}
			return value;
		}
	}

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		Map<String, Expression> expressions = new HashMap<>();
		for (String inputLine : LineReader.strings(inputCharacters)) {
			Matcher matcher = PATTERN.matcher(inputLine);
			if (!matcher.matches()) {
				throw new IllegalStateException("Unable to parse line.");
			}
			String outputVariableName = matcher.group("outputVariable");
			Expression expression;
			if (matcher.group("singularExpression") != null) {
				expression = new SingularExpression(matcher.group("singularNot") != null, createValue(matcher, "singular"));
			}
			else if (matcher.group("dualExpression") != null) {
				expression = new DualExpression(createValue(matcher, "left"), DualOperator.valueOf(matcher.group("operator")), createValue(matcher, "right"));
			}
			else {
				throw new IllegalStateException("Unexpected regular expression state");
			}
			if (expressions.put(outputVariableName, expression) != null) {
				throw new IllegalStateException("Duplicate expression name: " + outputVariableName);
			}
		}
		ExpressionManager expressionManager1 = new ExpressionManager(expressions);
		long originalWantedValue = expressionManager1.getValue(VARIABLE_NAME_WANTED);
		expressions.put(VARIABLE_NAME_TO_REPLACE, new SingularExpression(false, new ConstantValue(originalWantedValue)));
		ExpressionManager expressionManager2 = new ExpressionManager(expressions);
		long newWantedValue = expressionManager2.getValue(VARIABLE_NAME_WANTED);
		return new BasicPuzzleResults<>(
			originalWantedValue,
			newWantedValue
		);
	}

	private static Value createValue(Matcher matcher, String groupPrefix) {
		String numberGroupValue = matcher.group(groupPrefix + "Number");
		String variableGroupValue = matcher.group(groupPrefix + "Variable");
		if (numberGroupValue != null) {
			if (variableGroupValue != null) {
				throw new IllegalStateException("Unexpected regular expression state");
			}
			else {
				return new ConstantValue(Long.parseLong(numberGroupValue));
			}
		}
		else if (variableGroupValue != null) {
			return new VariableValue(variableGroupValue);
		}
		else {
			throw new IllegalStateException("Unexpected regular expression state");
		}
	}
}
