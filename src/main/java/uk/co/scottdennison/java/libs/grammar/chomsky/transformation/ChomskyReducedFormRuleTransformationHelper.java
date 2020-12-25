package uk.co.scottdennison.java.libs.grammar.chomsky.transformation;

import uk.co.scottdennison.java.libs.grammar.chomsky.model.ChomskyReducedFormNonTerminalRule;
import uk.co.scottdennison.java.libs.grammar.chomsky.model.ChomskyReducedFormRules;
import uk.co.scottdennison.java.libs.grammar.chomsky.model.ChomskyReducedFormTerminalRule;
import uk.co.scottdennison.java.libs.grammar.contextfree.model.ContextFreeGrammarRule;
import uk.co.scottdennison.java.libs.grammar.contextfree.model.ContextFreeGrammarSubRule;
import uk.co.scottdennison.java.libs.grammar.contextfree.model.ContextFreeGrammarSymbol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChomskyReducedFormRuleTransformationHelper {
	private static <T> Collection<ChomskyReducedFormBuildingRule> createChomskyReducedFormBuildingRulesFromContextFreeGrammar(Collection<ContextFreeGrammarRule<T>> contextFreeGrammarRules, Map<T, Integer> valueToIndexLookupSink, Map<Integer, T> indexToValueLookupSink) {
		Set<T> allValues =
			Stream.concat(
				contextFreeGrammarRules
					.stream()
					.map(ContextFreeGrammarRule::getLeftSide),
				contextFreeGrammarRules
					.stream()
					.map(ContextFreeGrammarRule::getSubRules)
					.flatMap(Collection::stream)
					.map(ContextFreeGrammarSubRule::getSymbols)
					.flatMap(List::stream)
					.filter(ContextFreeGrammarSymbol::isTerminal)
					.map(ContextFreeGrammarSymbol::getSymbol)
			).collect(Collectors.toSet());
		int index = 0;
		for (T value : allValues) {
			indexToValueLookupSink.put(index, value);
			valueToIndexLookupSink.put(value, index);
			index++;
		}
		Map<Boolean, Map<Integer, ChomskyReducedFormBuildingRuleSymbol>> symbolsCache = new HashMap<>();
		return
			contextFreeGrammarRules
				.stream()
				.flatMap(
					rule -> {
						int leftSide = valueToIndexLookupSink.get(rule.getLeftSide());
						return
							rule
								.getSubRules()
								.stream()
								.map(
									subRule -> new ChomskyReducedFormBuildingRule(
										leftSide,
										subRule
											.getSymbols()
											.stream()
											.map(
												symbol ->
													ChomskyReducedFormBuildingRuleSymbol.createUsingCache(
														valueToIndexLookupSink.get(symbol.getSymbol()),
														symbol.isTerminal(),
														symbolsCache
													)
											)
											.collect(Collectors.toList()),
										false
									)
								);
					}
				).collect(Collectors.toList());
	}

	private static Collection<ChomskyReducedFormBuildingRule> runChomskyReducedFormTransformationTerm(Collection<ChomskyReducedFormBuildingRule> rules) {
		ChomskyReducedFormBuildingRuleNewRuleHelper newRuleHelper = ChomskyReducedFormBuildingRuleNewRuleHelper.createWithReservedRuleNumbersFromExistingRules(rules);
		Map<Integer, ChomskyReducedFormBuildingRuleSymbol> newRuleReferencesForTerminalValues = new HashMap<>();
		for (ChomskyReducedFormBuildingRule rule : rules) {
			List<ChomskyReducedFormBuildingRuleSymbol> rightSide = rule.getRightSide();
			if (rightSide.size() > 1 && rightSide.stream().anyMatch(ChomskyReducedFormBuildingRuleSymbol::isTerminal)) {
				List<ChomskyReducedFormBuildingRuleSymbol> newRightSide = new ArrayList<>();
				for (ChomskyReducedFormBuildingRuleSymbol rightSideSymbol : rightSide) {
					ChomskyReducedFormBuildingRuleSymbol newRightSideSymbol;
					if (rightSideSymbol.isTerminal()) {
						newRightSideSymbol =
							newRuleReferencesForTerminalValues.computeIfAbsent(
								rightSideSymbol.getValue(),
								value ->
									newRuleHelper.createTemporaryRuleAndReturnReferencingSymbol(
										Collections.singletonList(
											rightSideSymbol
										)
									)
							);
					}
					else {
						newRightSideSymbol = rightSideSymbol;
					}
					newRightSide.add(newRightSideSymbol);
				}
				newRuleHelper.addRuleWithNewRightSide(rule, newRightSide);
			}
			else {
				newRuleHelper.addRule(rule);
			}
		}
		return newRuleHelper.getRulesCopy();
	}

	private static Collection<ChomskyReducedFormBuildingRule> runChomskyReducedFormTransformationBin(Collection<ChomskyReducedFormBuildingRule> rules) {
		ChomskyReducedFormBuildingRuleNewRuleHelper newRuleHelper = ChomskyReducedFormBuildingRuleNewRuleHelper.createWithReservedRuleNumbersFromExistingRules(rules);
		for (ChomskyReducedFormBuildingRule rule : rules) {
			List<ChomskyReducedFormBuildingRuleSymbol> rightSide = rule.getRightSide();
			long nonTerminalCount = rightSide.stream().filter(symbol -> !symbol.isTerminal()).count();
			if (nonTerminalCount > 2) {
				LinkedList<ChomskyReducedFormBuildingRuleSymbol> newRightSide = new LinkedList<>(rightSide);
				while (nonTerminalCount > 2) {
					LinkedList<ChomskyReducedFormBuildingRuleSymbol> temporaryRightSide = new LinkedList<>();
					int movedNonTerminalCount = 0;
					while (movedNonTerminalCount < 2) {
						ChomskyReducedFormBuildingRuleSymbol symbol = newRightSide.removeLast();
						temporaryRightSide.addFirst(symbol);
						if (!symbol.isTerminal()) {
							movedNonTerminalCount++;
						}
					}
					newRightSide.addLast(newRuleHelper.createTemporaryRuleAndReturnReferencingSymbol(temporaryRightSide));
					nonTerminalCount--; // We removed two, but added a new one back.
				}
				newRuleHelper.addRuleWithNewRightSide(rule, newRightSide);
			}
			else {
				newRuleHelper.addRule(rule);
			}
		}
		return newRuleHelper.getRulesCopy();
	}

	private static Collection<ChomskyReducedFormBuildingRule> runChomskyReducedFormTransformationUnit(Collection<ChomskyReducedFormBuildingRule> rules) {
		boolean madeChanges;
		do {
			madeChanges = false;
			Set<Integer> rulesToLeaveAloneThisIteration = new HashSet<>();
			ChomskyReducedFormBuildingRuleNewRuleHelper newRuleHelper = ChomskyReducedFormBuildingRuleNewRuleHelper.createWithReservedRuleNumbersFromExistingRules(rules);
			for (ChomskyReducedFormBuildingRule rule : rules) {
				int leftSide = rule.getLeftSide();
				boolean addExistingRule = true;
				if (!rulesToLeaveAloneThisIteration.contains(leftSide)) {
					List<ChomskyReducedFormBuildingRuleSymbol> rightSide = rule.getRightSide();
					if (rightSide.size() == 1) {
						ChomskyReducedFormBuildingRuleSymbol rightSideSymbol = rightSide.get(0);
						if (!rightSideSymbol.isTerminal()) {
							int rightSideValue = rightSideSymbol.getValue();
							if (!rulesToLeaveAloneThisIteration.contains(rightSideValue)) {
								addExistingRule = false;
								madeChanges = true;
								rulesToLeaveAloneThisIteration.add(leftSide);
								rulesToLeaveAloneThisIteration.add(rightSideValue);
								for (ChomskyReducedFormBuildingRule innerRule : rules) {
									if (innerRule.getLeftSide() == rightSideValue) {
										newRuleHelper.addRule(
											new ChomskyReducedFormBuildingRule(
												leftSide,
												innerRule.getRightSide(),
												rule.isTemporary()
											)
										);
									}
								}
							}
						}
					}
				}
				if (addExistingRule) {
					newRuleHelper.addRule(rule);
				}
			}
			rules = newRuleHelper.getRulesCopy();
		} while (madeChanges);
		return rules;
	}

	private static Collection<ChomskyReducedFormBuildingRule> runChomskyReducedFormTransformationRemoveTemporaryDuplicates(Collection<ChomskyReducedFormBuildingRule> rules) {
		Set<Integer> ruleLeftSidesThatAppearOnce =
			rules
				.stream()
				.collect(
					Collectors.groupingBy(
						ChomskyReducedFormBuildingRule::getLeftSide,
						Collectors.counting()
					)
				)
				.entrySet()
				.stream()
				.filter(entry -> entry.getValue() == 1L)
				.map(Map.Entry::getKey)
				.collect(Collectors.toSet());
		Collection<ChomskyReducedFormBuildingRule> rulesToInvestigate =
			rules
				.stream()
				.filter(rule -> ruleLeftSidesThatAppearOnce.contains(rule.getLeftSide()))
				.filter(ChomskyReducedFormBuildingRule::isTemporary)
				.filter(
					rule -> {
						int leftSide = rule.getLeftSide();
						return
							rule
								.getRightSide()
								.stream()
								.filter(symbol -> !symbol.isTerminal())
								.mapToInt(ChomskyReducedFormBuildingRuleSymbol::getValue)
								.noneMatch(value -> value == leftSide);
					}
				)
				.collect(Collectors.toSet());
		boolean candidateFound;
		while (true) {
			candidateFound = false;
			int leftSideToReplace = -1;
			int leftSideToReplaceWith = -1;
			Map<List<ChomskyReducedFormBuildingRuleSymbol>, Integer> seenRightSides = new HashMap<>();
			for (ChomskyReducedFormBuildingRule rule : rulesToInvestigate) {
				int leftSide = rule.getLeftSide();
				Integer previouslySeenLeftSideWithDuplicateRightSide = seenRightSides.put(rule.getRightSide(), leftSide);
				if (previouslySeenLeftSideWithDuplicateRightSide != null) {
					candidateFound = true;
					leftSideToReplace = leftSide;
					leftSideToReplaceWith = previouslySeenLeftSideWithDuplicateRightSide;
					rulesToInvestigate.remove(rule);
					break;
				}
			}
			if (candidateFound) {
				ChomskyReducedFormBuildingRuleSymbol symbolToReplaceWith = new ChomskyReducedFormBuildingRuleSymbol(
					false,
					leftSideToReplaceWith
				);
				Collection<ChomskyReducedFormBuildingRule> newRules = new ArrayList<>();
				for (ChomskyReducedFormBuildingRule rule : rules) {
					int leftSide = rule.getLeftSide();
					if (leftSide != leftSideToReplace) {
						boolean ruleChanged = false;
						List<ChomskyReducedFormBuildingRuleSymbol> rightSide = rule.getRightSide();
						List<ChomskyReducedFormBuildingRuleSymbol> newRightSide = new ArrayList<>();
						for (ChomskyReducedFormBuildingRuleSymbol rightSideSymbol : rightSide) {
							ChomskyReducedFormBuildingRuleSymbol newRightSideSymbol;
							if (!rightSideSymbol.isTerminal() && rightSideSymbol.getValue() == leftSideToReplace) {
								newRightSideSymbol = symbolToReplaceWith;
								ruleChanged = true;
							}
							else {
								newRightSideSymbol = rightSideSymbol;
							}
							newRightSide.add(newRightSideSymbol);
						}
						ChomskyReducedFormBuildingRule newRule;
						if (ruleChanged) {
							newRule = new ChomskyReducedFormBuildingRule(
								leftSide,
								newRightSide,
								rule.isTemporary()
							);
						}
						else {
							newRule = rule;
						}
						newRules.add(newRule);
					}
				}
				rules = newRules;
			}
			else {
				break;
			}
		}
		return rules;
	}

	private static Collection<ChomskyReducedFormBuildingRule> runChomskyReducedFormTransformationRemoveUnreachable(Collection<ChomskyReducedFormBuildingRule> rules, Set<Integer> startingRuleLeftSides) {
		Map<Integer, Set<ChomskyReducedFormBuildingRule>> rulesGroupedByLeftSide =
			rules
				.stream()
				.collect(
					Collectors.groupingBy(
						ChomskyReducedFormBuildingRule::getLeftSide,
						Collectors.toSet()
					)
				);
		Set<Integer> visitedLeftSides = new HashSet<>();
		Set<Integer> pendingLeftSides = startingRuleLeftSides;
		while (!pendingLeftSides.isEmpty()) {
			Set<Integer> newPendingLeftSides = new HashSet<>();
			for (int pendingLeftSide : pendingLeftSides) {
				if (visitedLeftSides.add(pendingLeftSide)) {
					Set<ChomskyReducedFormBuildingRule> rulesWithLeftSide = rulesGroupedByLeftSide.get(pendingLeftSide);
					if (rulesWithLeftSide != null) {
						for (ChomskyReducedFormBuildingRule rule : rulesWithLeftSide) {
							for (ChomskyReducedFormBuildingRuleSymbol symbol : rule.getRightSide()) {
								if (!symbol.isTerminal()) {
									newPendingLeftSides.add(symbol.getValue());
								}
							}
						}
					}
				}
			}
			pendingLeftSides = newPendingLeftSides;
		}
		List<ChomskyReducedFormBuildingRule> newRules = new ArrayList<>();
		for (ChomskyReducedFormBuildingRule rule : rules) {
			if (visitedLeftSides.contains(rule.getLeftSide())) {
				newRules.add(rule);
			}
		}
		return newRules;
	}

	private static <T> ChomskyReducedFormRules<Object, T> createChomskyReducedFormRules(Collection<ChomskyReducedFormBuildingRule> buildingRules, Set<T> startingRules, Map<Integer, T> intValueToRealValueLookup, Class<T> ruleClazz) {
		// A class needed so that temporary keys don't match any real key by accident.
		final class TemporaryRuleKey {
			private final int number;

			public TemporaryRuleKey(int number) {
				this.number = number;
			}

			@Override
			public boolean equals(Object otherObject) {
				if (otherObject == this) {
					return true;
				}
				if (otherObject == null || otherObject.getClass() != TemporaryRuleKey.class) {
					return false;
				}
				TemporaryRuleKey otherTemporaryRuleKey = (TemporaryRuleKey) otherObject;
				return this.number == otherTemporaryRuleKey.number;
			}

			@Override
			public int hashCode() {
				return this.number;
			}

			@Override
			public String toString() {
				return "TemporaryRuleKey{" + this.number + "}";
			}
		}

		Function<Integer, T> intValueToRealValueMapper = intValue -> {
			T value = intValueToRealValueLookup.get(intValue);
			if (value == null) {
				throw new IllegalStateException("Could not reverse integer mapping back to real key");
			}
			else {
				return value;
			}
		};
		for (T startingRule : startingRules) {
			if (startingRule instanceof TemporaryRuleKey) {
				throw new IllegalStateException("Starting rule must not be an instance of TemporaryRuleKey");
			}
		}
		Map<Integer, Object> ruleLeftSideToKeyMap =
			buildingRules
				.stream()
				.collect(
					Collectors.toMap(
						ChomskyReducedFormBuildingRule::getLeftSide,
						rule -> {
							int ruleLeftSide = rule.getLeftSide();
							if (rule.isTemporary()) {
								return new TemporaryRuleKey(ruleLeftSide);
							}
							else {
								return intValueToRealValueMapper.apply(ruleLeftSide);
							}
						},
						(value1, value2) -> {
							if (Objects.equals(value1, value2)) {
								return value1;
							}
							else {
								throw new IllegalStateException("Differing keys for what appears to be the same rule. Something went wrong.");
							}
						},
						HashMap::new
					)
				);
		List<ChomskyReducedFormTerminalRule<Object, T>> terminalRules = new ArrayList<>();
		List<ChomskyReducedFormNonTerminalRule<Object>> nonTerminalRules = new ArrayList<>();
		for (ChomskyReducedFormBuildingRule buildingRule : buildingRules) {
			Object ruleKey = ruleLeftSideToKeyMap.get(buildingRule.getLeftSide());
			boolean temporary = buildingRule.isTemporary();
			List<ChomskyReducedFormBuildingRuleSymbol> rightSide = buildingRule.getRightSide();
			if (rightSide.size() == 1 && rightSide.stream().allMatch(ChomskyReducedFormBuildingRuleSymbol::isTerminal)) {
				terminalRules.add(
					new ChomskyReducedFormTerminalRule<>(
						ruleKey,
						temporary,
						intValueToRealValueMapper.apply(rightSide.get(0).getValue())
					)
				);
			}
			else if (rightSide.size() == 2 && rightSide.stream().noneMatch(ChomskyReducedFormBuildingRuleSymbol::isTerminal)) {
				nonTerminalRules.add(
					new ChomskyReducedFormNonTerminalRule<>(
						ruleKey,
						temporary,
						ruleLeftSideToKeyMap.get(rightSide.get(0).getValue()),
						ruleLeftSideToKeyMap.get(rightSide.get(1).getValue())
					)
				);
			}
			else {
				throw new IllegalStateException("Building rule is not a valid chomsky reduced form rule");
			}
		}
		return new ChomskyReducedFormRules<>(ruleClazz, new HashSet<>(startingRules), terminalRules, nonTerminalRules);
	}

	public static <T> ChomskyReducedFormRules<Object, T> transformContextFreeGrammarIntoChomskyReducedForm(Collection<ContextFreeGrammarRule<T>> contextFreeGrammarRules, Set<T> startingRules, Class<T> ruleClazz) {
		if (contextFreeGrammarRules.stream().map(ContextFreeGrammarRule::getSubRules).flatMap(Collection::stream).map(ContextFreeGrammarSubRule::getSymbols).anyMatch(List::isEmpty)) {
			throw new IllegalStateException("Chomsky reduced form does not support empty sets.");
		}
		Map<T, Integer> realValueToIntValueLookup = new HashMap<>();
		Map<Integer, T> intValueToRealValueLookup = new HashMap<>();
		Collection<ChomskyReducedFormBuildingRule> buildingRules;
		buildingRules = createChomskyReducedFormBuildingRulesFromContextFreeGrammar(contextFreeGrammarRules, realValueToIntValueLookup, intValueToRealValueLookup);
		if (!startingRules.stream().allMatch(realValueToIntValueLookup::containsKey)) {
			throw new IllegalStateException("A starting rule does not exist in rules collection");
		}
		// Transformation START is not needed for reduced form
		buildingRules = runChomskyReducedFormTransformationTerm(buildingRules);
		buildingRules = runChomskyReducedFormTransformationBin(buildingRules);
		// Transformation DEL is not needed for reduced form
		buildingRules = runChomskyReducedFormTransformationUnit(buildingRules);
		buildingRules = runChomskyReducedFormTransformationRemoveTemporaryDuplicates(buildingRules);
		buildingRules = runChomskyReducedFormTransformationRemoveUnreachable(buildingRules, startingRules.stream().map(realValueToIntValueLookup::get).collect(Collectors.toSet()));
		if (buildingRules.isEmpty()) {
			throw new IllegalStateException("After transformation no rules remain");
		}
		// Final step is to convert this into proper chomsky classes.
		return createChomskyReducedFormRules(buildingRules, startingRules, intValueToRealValueLookup, ruleClazz);
	}
}
