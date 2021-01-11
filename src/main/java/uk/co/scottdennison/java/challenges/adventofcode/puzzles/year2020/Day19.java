package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2020;

import uk.co.scottdennison.java.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.utils.LineReader;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day19 implements IPuzzle {
	private static final String[] EXTRA_RULE_LINES = {
		"8: 42 | 42 8",
		"11: 42 31 | 42 11 31"
	};

	private static final Pattern PATTERN_RULE = Pattern.compile("^(?<ruleNumber>[0-9]+): (?<ruleComponents>.+)$");
	private static final Pattern PATTERN_RULE_COMPONENT_OR_PART_SPLIT = Pattern.compile(" *\\| *");
	private static final Pattern PATTERN_RULE_COMPONENT_SEQUENCE_SPLIT = Pattern.compile(" ");
	private static final Pattern PATTERN_RULE_COMPONENT_PART = Pattern.compile("(?<otherRuleNumber>[0-9]+)|(?:\"(?<literal>.+?)\")");

	private static final class EndRuleComponent implements RuleComponent {
		public static final EndRuleComponent INSTANCE = new EndRuleComponent();

		private EndRuleComponent() {
		}

		@Override
		public boolean test(char[] input, int index, RuleComponent nextRuleComponent, NumberedRuleComponentManager numberedRuleComponentManager) {
			if (nextRuleComponent != null) {
				throw new IllegalStateException("An end rule component cannot be followed by another rule");
			}
			return index == input.length;
		}
	}

	private static final class OrRuleComponent implements RuleComponent {
		private final List<RuleComponent> ruleComponents;

		private OrRuleComponent(List<RuleComponent> ruleComponents) {
			this.ruleComponents = new ArrayList<>(ruleComponents);
		}

		@Override
		public boolean test(char[] input, int index, RuleComponent nextRuleComponent, NumberedRuleComponentManager numberedRuleComponentManager) {
			for (RuleComponent ruleComponent : this.ruleComponents) {
				if (ruleComponent.test(input, index, nextRuleComponent, numberedRuleComponentManager)) {
					return true;
				}
			}
			return false;
		}
	}

	private static final class SequenceRuleComponent implements RuleComponent {
		private static final class TwoPartRuleComponent implements RuleComponent {
			private final RuleComponent leftRuleComponent;
			private final RuleComponent rightRuleComponent;

			public TwoPartRuleComponent(RuleComponent leftRuleComponent, RuleComponent rightRuleComponent) {
				this.leftRuleComponent = leftRuleComponent;
				this.rightRuleComponent = rightRuleComponent;
			}

			@Override
			public boolean test(char[] input, int index, RuleComponent nextRuleComponent, NumberedRuleComponentManager numberedRuleComponentManager) {
				if (nextRuleComponent == null) {
					nextRuleComponent = this.rightRuleComponent;
				}
				else {
					nextRuleComponent = new TwoPartRuleComponent(this.rightRuleComponent, nextRuleComponent);
				}
				return this.leftRuleComponent.test(input, index, nextRuleComponent, numberedRuleComponentManager);
			}
		}

		private final RuleComponent ruleComponent;

		public SequenceRuleComponent(List<RuleComponent> ruleComponents) {
			if (ruleComponents.isEmpty()) {
				throw new IllegalStateException("Rule components cannot be empty");
			}
			int index = ruleComponents.size() - 1;
			RuleComponent ruleComponent = ruleComponents.get(index);
			while (--index >= 0) {
				ruleComponent = new TwoPartRuleComponent(ruleComponents.get(index), ruleComponent);
			}
			this.ruleComponent = ruleComponent;
		}

		@Override
		public boolean test(char[] input, int index, RuleComponent nextRuleComponent, NumberedRuleComponentManager numberedRuleComponentManager) {
			return this.ruleComponent.test(input, index, nextRuleComponent, numberedRuleComponentManager);
		}
	}

	private static final class LiteralRuleComponent implements RuleComponent {
		private final char[] literalCharacters;

		private LiteralRuleComponent(String literalString) {
			this.literalCharacters = literalString.toCharArray();
		}

		@Override
		public boolean test(char[] input, int index, RuleComponent nextRuleComponent, NumberedRuleComponentManager numberedRuleComponentManager) {
			int literalCharacterCount = this.literalCharacters.length;
			int nextRuleIndex = index + literalCharacterCount;
			if (nextRuleIndex > input.length) {
				return false;
			}
			for (int literalCharacterIndex = 0, inputIndex = index; literalCharacterIndex < literalCharacterCount; literalCharacterIndex++, inputIndex++) {
				if (this.literalCharacters[literalCharacterIndex] != input[inputIndex]) {
					return false;
				}
			}
			return nextRuleComponent.test(input, nextRuleIndex, null, numberedRuleComponentManager);
		}
	}

	private static final class OtherRuleComponentRuleComponent implements RuleComponent {
		private final int ruleComponentNumber;

		public OtherRuleComponentRuleComponent(int ruleComponentNumber) {
			this.ruleComponentNumber = ruleComponentNumber;
		}

		@Override
		public boolean test(char[] input, int index, RuleComponent nextRuleComponent, NumberedRuleComponentManager numberedRuleComponentManager) {
			return numberedRuleComponentManager.getRuleComponent(this.ruleComponentNumber).test(input, index, nextRuleComponent, numberedRuleComponentManager);
		}
	}

	private interface RuleComponent {
		boolean test(char[] input, int index, RuleComponent nextRuleComponent, NumberedRuleComponentManager numberedRuleComponentManager);
	}

	private static final class NumberedRuleComponentManager {
		private final Map<Integer, RuleComponent> ruleComponents = new HashMap<>();

		public void addRuleComponent(int ruleNumber, RuleComponent ruleComponent, boolean allowOverride) {
			if ((this.ruleComponents.put(ruleNumber, ruleComponent) != null) && !allowOverride) {
				throw new IllegalStateException("Duplicate rule");
			}
		}

		public RuleComponent getRuleComponent(int ruleNumber) {
			RuleComponent ruleComponent = this.ruleComponents.get(ruleNumber);
			if (ruleComponent == null) {
				throw new IllegalStateException("No such rule component");
			}
			return ruleComponent;
		}
	}

	private static void parseRuleIntoManager(NumberedRuleComponentManager numberedRuleComponentManager, String line, boolean allowOverride) {
		Matcher ruleMatcher = PATTERN_RULE.matcher(line);
		if (!ruleMatcher.matches()) {
			throw new IllegalStateException("Could not parse rule");
		}
		List<RuleComponent> ruleComponents = new ArrayList<>();
		for (String ruleComponentOrPartString : PATTERN_RULE_COMPONENT_OR_PART_SPLIT.split(ruleMatcher.group("ruleComponents"))) {
			List<RuleComponent> ruleComponentsInOrPart = new ArrayList<>();
			for (String ruleComponentSequencePartString : PATTERN_RULE_COMPONENT_SEQUENCE_SPLIT.split(ruleComponentOrPartString)) {
				Matcher ruleComponentMatcher = PATTERN_RULE_COMPONENT_PART.matcher(ruleComponentSequencePartString);
				if (!ruleComponentMatcher.matches()) {
					throw new IllegalStateException("Could not parse rule component");
				}
				String otherRuleNumberString = ruleComponentMatcher.group("otherRuleNumber");
				RuleComponent ruleComponent;
				if (otherRuleNumberString != null) {
					ruleComponent = new OtherRuleComponentRuleComponent(Integer.parseInt(otherRuleNumberString));
				}
				else {
					String literalString = ruleComponentMatcher.group("literal");
					if (literalString != null) {
						ruleComponent = new LiteralRuleComponent(literalString);
					}
					else {
						throw new IllegalStateException("Could not extract rule component data");
					}
				}
				ruleComponentsInOrPart.add(ruleComponent);
			}
			RuleComponent ruleComponentOrPart;
			if (ruleComponentsInOrPart.size() == 1) {
				ruleComponentOrPart = ruleComponentsInOrPart.get(0);
			}
			else {
				ruleComponentOrPart = new SequenceRuleComponent(ruleComponentsInOrPart);
			}
			ruleComponents.add(ruleComponentOrPart);
		}
		RuleComponent ruleComponent;
		if (ruleComponents.size() == 1) {
			ruleComponent = ruleComponents.get(0);
		}
		else {
			ruleComponent = new OrRuleComponent(ruleComponents);
		}
		numberedRuleComponentManager.addRuleComponent(Integer.parseInt(ruleMatcher.group("ruleNumber")), ruleComponent, allowOverride);
	}

	private static int matchLinesAgainstRule(NumberedRuleComponentManager numberedRuleComponentManager, @SuppressWarnings("SameParameterValue") int ruleNumber, char[][] inputLines) {
		RuleComponent ruleComponent = numberedRuleComponentManager.getRuleComponent(ruleNumber);
		int matchCount = 0;
		for (char[] inputLine : inputLines) {
			if (ruleComponent.test(inputLine, 0, EndRuleComponent.INSTANCE, numberedRuleComponentManager)) {
				matchCount++;
			}
		}
		return matchCount;
	}

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		NumberedRuleComponentManager numberedRuleComponentManager = new NumberedRuleComponentManager();
		List<char[]> inputLines = new ArrayList<>();
		boolean rulesFinished = false;
		for (char[] inputLine : LineReader.charArrays(inputCharacters)) {
			if (rulesFinished) {
				if (inputLine.length != 0) {
					inputLines.add(inputLine);
				}
			}
			else {
				if (inputLine.length == 0) {
					rulesFinished = true;
				}
				else {
					parseRuleIntoManager(numberedRuleComponentManager, new String(inputLine), false);
				}
			}
		}
		char[][] inputLinesArray = inputLines.toArray(new char[0][]);
		int partAResult = matchLinesAgainstRule(numberedRuleComponentManager, 0, inputLinesArray);
		for (String extraRuleLine : EXTRA_RULE_LINES) {
			parseRuleIntoManager(numberedRuleComponentManager, extraRuleLine, true);
		}
		int partBResult = matchLinesAgainstRule(numberedRuleComponentManager, 0, inputLinesArray);
		return new BasicPuzzleResults<>(
			partAResult,
			partBResult
		);
	}
}
