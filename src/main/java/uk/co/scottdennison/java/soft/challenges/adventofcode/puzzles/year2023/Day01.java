package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2023;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.common.IntBinaryPredicate;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.ToIntBiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Day01 implements IPuzzle {
	private static final class TextualDigit {
		private final String digitSpeltOutString;
		private final String digitRawString;
		private final int value;

		public TextualDigit(String digitSpeltOutString, String digitRawString, int value) {
			this.digitSpeltOutString = digitSpeltOutString;
			this.digitRawString = digitRawString;
			this.value = value;
		}

		public String getDigitSpeltOutString() {
			return digitSpeltOutString;
		}

		public String getDigitRawString() {
			return digitRawString;
		}

		public int getValue() {
			return value;
		}

		public static TextualDigit create(String digitSpeltOut, int value) {
			return new TextualDigit(digitSpeltOut, Integer.toString(value), value);
		}
	}

	private static final class DigitResult {
		private final int index;
		private final int value;

		public DigitResult(int index, int value) {
			this.index = index;
			this.value = value;
		}

		public int getIndex() {
			return index;
		}

		public int getValue() {
			return value;
		}
	}

	private static final TextualDigit[] TEXTUAL_DIGITS = {
		TextualDigit.create("one", 1),
		TextualDigit.create("two", 2),
		TextualDigit.create("three", 3),
		TextualDigit.create("four", 4),
		TextualDigit.create("five", 5),
		TextualDigit.create("six", 6),
		TextualDigit.create("seven", 7),
		TextualDigit.create("eight", 8),
		TextualDigit.create("nine", 9)
	};

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		return new BasicPuzzleResults<>(
			solve(inputCharacters, false),
			solve(inputCharacters, true)
		);
	}

	private static int solve(char[] inputCharacters, boolean useSpeltOutDigits) {
		return LineReader
			.stringsStream(inputCharacters)
			.map(inputLine -> solve(inputLine, useSpeltOutDigits))
			.filter(OptionalInt::isPresent)
			.mapToInt(OptionalInt::getAsInt)
			.sum();
	}

	private static OptionalInt solve(String inputLine, boolean useSpeltOutDigits) {
		OptionalInt firstDigit = findDigit(inputLine, String::indexOf, Integer.MAX_VALUE, (a, b) -> a < b, useSpeltOutDigits);
		if (!firstDigit.isPresent()) {
			return OptionalInt.empty();
		}
		OptionalInt lastDigit = findDigit(inputLine, String::lastIndexOf, Integer.MIN_VALUE, (a, b) -> a > b, useSpeltOutDigits);
		if (!lastDigit.isPresent()) {
			throw new IllegalStateException("Somehow found first digit but didn't find last digit. This should be impossible.");
		}
		return OptionalInt.of((firstDigit.getAsInt() * 10) + lastDigit.getAsInt());
	}

	private static OptionalInt findDigit(String inputLine, ToIntBiFunction<String, String> indexOfFunction, int startPosition, IntBinaryPredicate isBestPredicate, boolean useSpeltOutDigits) {
		List<DigitResult> digitResults = new ArrayList<>();
		findDigit(digitResults, inputLine, indexOfFunction, startPosition, isBestPredicate, TextualDigit::getDigitRawString);
		if (useSpeltOutDigits) {
			findDigit(digitResults, inputLine, indexOfFunction, startPosition, isBestPredicate, TextualDigit::getDigitSpeltOutString);
		}
		Optional<DigitResult> bestDigitResult = digitResults.stream().reduce((left,right) -> isBestPredicate.test(left.getIndex(),right.getIndex())?left:right);
		if (bestDigitResult.isPresent()) {
			return OptionalInt.of(bestDigitResult.get().getValue());
		} else {
			return OptionalInt.empty();
		}
	}

	private static void findDigit(List<DigitResult> digitResultsSink, String inputLine, ToIntBiFunction<String, String> indexOfFunction, int startPosition, IntBinaryPredicate isBestPredicate, Function<TextualDigit,String> textualDigitToStringFunction) {
		for (TextualDigit textualDigit : TEXTUAL_DIGITS) {
			int index = indexOfFunction.applyAsInt(inputLine, textualDigitToStringFunction.apply(textualDigit));
			if (index >= 0) {
				digitResultsSink.add(new DigitResult(index, textualDigit.getValue()));
			}
		}
	}
}
