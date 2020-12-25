package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2020;

import uk.co.scottdennison.java.challenges.adventofcode.utils.InputFileUtils;
import uk.co.scottdennison.java.libs.grammar.chomsky.algorithms.ChomskyReducedFormRuleCYKAlgorithm;
import uk.co.scottdennison.java.libs.grammar.chomsky.model.ChomskyReducedFormRules;
import uk.co.scottdennison.java.libs.grammar.chomsky.transformation.ChomskyReducedFormRuleTransformationHelper;
import uk.co.scottdennison.java.libs.grammar.contextfree.model.ContextFreeGrammarRule;
import uk.co.scottdennison.java.libs.grammar.contextfree.model.ContextFreeGrammarSubRule;
import uk.co.scottdennison.java.libs.grammar.contextfree.model.ContextFreeGrammarSymbol;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Day19UsingCYK {
	private static final String[] EXTRA_RULE_LINES = {
		"8: 42 | 42 8",
		"11: 42 31 | 42 11 31"
	};

	private static final Pattern PATTERN_RULE = Pattern.compile("^(?<ruleNumber>[0-9]+): (?<ruleComponents>.+)$");
	private static final Pattern PATTERN_RULE_COMPONENT_OR_PART_SPLIT = Pattern.compile(" *\\| *");
	private static final Pattern PATTERN_RULE_COMPONENT_SEQUENCE_SPLIT = Pattern.compile(" ");
	private static final Pattern PATTERN_RULE_COMPONENT_PART = Pattern.compile("(?<otherRuleNumber>[0-9]+)|(?:\"(?<literal>.+?)\")");

	private static final class NumberedGrammarRuleManager {
		private final Map<Integer, ContextFreeGrammarRule<String>> grammarRules = new HashMap<>();

		public void addRuleComponent(int ruleNumber, ContextFreeGrammarRule<String> grammarRule, boolean allowOverride) {
			if ((this.grammarRules.put(ruleNumber, grammarRule) != null) ^ allowOverride) {
				throw new IllegalStateException("Duplicate rule");
			}
		}

		public Collection<ContextFreeGrammarRule<String>> getGrammarRules() {
			return this.grammarRules.values();
		}
	}

	private static void parseRuleIntoManager(NumberedGrammarRuleManager numberedGrammarRuleManager, String line, boolean allowOverride) {
		Matcher ruleMatcher = PATTERN_RULE.matcher(line);
		if (!ruleMatcher.matches()) {
			throw new IllegalStateException("Could not parse rule");
		}
		List<ContextFreeGrammarSubRule<String>> subRules = new ArrayList<>();
		for (String ruleComponentOrPartString : PATTERN_RULE_COMPONENT_OR_PART_SPLIT.split(ruleMatcher.group("ruleComponents"))) {
			List<ContextFreeGrammarSymbol<String>> symbols = new ArrayList<>();
			for (String ruleComponentSequencePartString : PATTERN_RULE_COMPONENT_SEQUENCE_SPLIT.split(ruleComponentOrPartString)) {
				Matcher ruleComponentMatcher = PATTERN_RULE_COMPONENT_PART.matcher(ruleComponentSequencePartString);
				if (!ruleComponentMatcher.matches()) {
					throw new IllegalStateException("Could not parse rule component");
				}
				String symbolValue = ruleComponentMatcher.group("otherRuleNumber");
				boolean terminal;
				if (symbolValue != null) {
					terminal = false;
				}
				else {
					symbolValue = ruleComponentMatcher.group("literal");
					if (symbolValue != null) {
						terminal = true;
					}
					else {
						throw new IllegalStateException("Could not extract rule component data");
					}
				}
				symbols.add(new ContextFreeGrammarSymbol<>(terminal,symbolValue));
			}
			subRules.add(new ContextFreeGrammarSubRule<>(symbols));
		}
		String ruleNumberString = ruleMatcher.group("ruleNumber");
		numberedGrammarRuleManager.addRuleComponent(Integer.parseInt(ruleNumberString), new ContextFreeGrammarRule<>(ruleNumberString, subRules), allowOverride);
	}

	private static int matchLinesAgainstRule(NumberedGrammarRuleManager numberedGrammarRuleManager, String ruleNumber, List<String> inputLines) {
		System.out.println(System.currentTimeMillis());
		Collection<ContextFreeGrammarRule<String>> contextFreeGrammarRules = numberedGrammarRuleManager.getGrammarRules();
		System.out.println(System.currentTimeMillis());
		ChomskyReducedFormRules<Object, String> chomskyReducedFormRules = ChomskyReducedFormRuleTransformationHelper.transformContextFreeGrammarIntoChomskyReducedForm(contextFreeGrammarRules, Collections.singleton(ruleNumber), String.class);
		System.out.println(System.currentTimeMillis());
		int matchCount = 0;
		for (String inputLine : inputLines) {
			if (ChomskyReducedFormRuleCYKAlgorithm.runCYKAndCheckAStartRuleMatches(chomskyReducedFormRules, inputLine.chars().mapToObj(charValue -> Character.toString((char)charValue)).collect(Collectors.toList()))) {
				matchCount++;
			}
		}
		System.out.println(System.currentTimeMillis());
		return matchCount;
	}

	public static void main(String[] args) throws IOException {
		NumberedGrammarRuleManager numberedGrammarRuleManager = new NumberedGrammarRuleManager();
		List<String> inputLines = new ArrayList<>();
		boolean rulesFinished = false;
		for (String fileLine : Files.readAllLines(InputFileUtils.getInputPath(2020,19))) {
			if (rulesFinished) {
				if (!fileLine.isEmpty()) {
					inputLines.add(fileLine);
				}
			}
			else {
				if (fileLine.isEmpty()) {
					rulesFinished = true;
				}
				else {
					parseRuleIntoManager(numberedGrammarRuleManager, fileLine, false);
				}
			}
		}
		outputSummary(1, numberedGrammarRuleManager, 0, inputLines);
		for (String extraRuleLine : EXTRA_RULE_LINES) {
			parseRuleIntoManager(numberedGrammarRuleManager, extraRuleLine, true);
		}
		outputSummary(2, numberedGrammarRuleManager, 0, inputLines);
	}

	private static void outputSummary(int partNumber, NumberedGrammarRuleManager numberedGrammarRuleManager, @SuppressWarnings("SameParameterValue") int ruleNumber, List<String> inputLines) {
		System.out.format("Match count for part %d: %d%n", partNumber, matchLinesAgainstRule(numberedGrammarRuleManager, Integer.toString(ruleNumber), inputLines));
	}
}
