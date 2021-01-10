package uk.co.scottdennison.java.libs.grammar.chomsky.algorithms;

import uk.co.scottdennison.java.libs.grammar.chomsky.model.ChomskyReducedFormNonTerminalRule;
import uk.co.scottdennison.java.libs.grammar.chomsky.model.ChomskyReducedFormRule;
import uk.co.scottdennison.java.libs.grammar.chomsky.model.ChomskyReducedFormRules;
import uk.co.scottdennison.java.libs.grammar.chomsky.model.ChomskyReducedFormTerminalRule;
import uk.co.scottdennison.java.libs.grammar.parseresults.model.ParseForest;
import uk.co.scottdennison.java.libs.grammar.parseresults.model.ParseTree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ChomskyReducedFormRuleCYKAlgorithm {
	private ChomskyReducedFormRuleCYKAlgorithm() {
	}

	public static <K, V extends K> Collection<ParseForest<K>> runCYKAndProduceParseTrees(ChomskyReducedFormRules<K, V> chomskyReducedFormRules, List<V> input) {
		final class RuleMatch {
			final class RuleMatchInstance {
				private final RuleMatch leftMatch;
				private final RuleMatch rightMatch;

				public RuleMatchInstance(RuleMatch leftMatch, RuleMatch rightMatch) {
					this.leftMatch = leftMatch;
					this.rightMatch = rightMatch;
				}

				public RuleMatch getLeftMatch() {
					return this.leftMatch;
				}

				public RuleMatch getRightMatch() {
					return this.rightMatch;
				}
			}

			private final K matchedRuleKey;
			private final List<RuleMatchInstance> ruleMatchInstances = new ArrayList<>();
			private transient ParseForest<K> parseForest;

			public RuleMatch(K matchedRuleKey) {
				this.matchedRuleKey = matchedRuleKey;
			}

			public void registerMatchSource(RuleMatch leftMatch, RuleMatch rightMatch) {
				this.ruleMatchInstances.add(new RuleMatchInstance(leftMatch, rightMatch));
			}

			public K getMatchedRuleKey() {
				return this.matchedRuleKey;
			}

			public ParseForest<K> getOrBuildParseForest() {
				ParseForest<K> parseForest;
				if ((parseForest = this.parseForest) == null) {
					parseForest = new ParseForest<>(
						this.matchedRuleKey,
						this.ruleMatchInstances
							.stream()
							.map(
								ruleMatchInstance -> new ParseTree<>(
									Arrays.asList(
										ruleMatchInstance.getLeftMatch().getOrBuildParseForest(),
										ruleMatchInstance.getRightMatch().getOrBuildParseForest()
									)
								)
							)
							.collect(Collectors.toList())
					);
					this.parseForest = parseForest;
				}
				return parseForest;
			}
		}

		final class RuleMatchHolder {
			private final Map<K, RuleMatch> matches = new HashMap<>();

			public RuleMatch getOrCreateRuleMatch(K rule) {
				return this.matches.computeIfAbsent(rule, RuleMatch::new);
			}

			public Collection<RuleMatch> getRuleMatches() {
				return this.matches.values();
			}
		}

		Collection<ChomskyReducedFormTerminalRule<K, V>> terminalRules = chomskyReducedFormRules.getTerminalRulesView();
		Collection<ChomskyReducedFormNonTerminalRule<K>> nonTerminalRules = chomskyReducedFormRules.getNonTerminalRulesView();
		Map<V, Set<K>> ruleKeysThatProduceValues =
			terminalRules
				.stream()
				.collect(
					Collectors.groupingBy(
						ChomskyReducedFormTerminalRule::getOutput,
						Collectors.mapping(
							ChomskyReducedFormRule::getRuleKey,
							Collectors.toSet()
						)
					)
				);
		Map<K, Map<K, Set<K>>> parentRuleKeysThatMatchChildKeys =
			nonTerminalRules
				.stream()
				.collect(
					Collectors.groupingBy(
						ChomskyReducedFormNonTerminalRule::getLeftChildRuleKey,
						Collectors.groupingBy(
							ChomskyReducedFormNonTerminalRule::getRightChildRuleKey,
							Collectors.mapping(
								ChomskyReducedFormNonTerminalRule::getRuleKey,
								Collectors.toSet()
							)
						)
					)
				);
		int inputSize = input.size();

		RuleMatchHolder[][] rulesMatched = new RuleMatchHolder[inputSize][];
		rulesMatched[0] = new RuleMatchHolder[inputSize];
		for (int inputIndex = 0; inputIndex < inputSize; inputIndex++) {
			Set<K> ruleKeysThatProduceInputValue = ruleKeysThatProduceValues.get(input.get(inputIndex));
			RuleMatchHolder ruleMatchHolder = new RuleMatchHolder();
			if (ruleKeysThatProduceInputValue != null) {
				for (K ruleKey : ruleKeysThatProduceInputValue) {
					ruleMatchHolder.getOrCreateRuleMatch(ruleKey);
				}
			}
			rulesMatched[0][inputIndex] = ruleMatchHolder;
		}
		for (int targetI = 1; targetI < inputSize; targetI++) {
			rulesMatched[targetI] = new RuleMatchHolder[inputSize - targetI];
			for (int targetJ = 0; targetJ < inputSize - targetI; targetJ++) {
				RuleMatchHolder ruleMatchesForNewCell = new RuleMatchHolder();
				for (
					int
					check1I = targetI - 1,
					check2I = 0,
					check2J = targetI + targetJ;
					check1I >= 0;
					check1I--,
						check2I++,
						check2J--
				) {
					RuleMatchHolder leftRuleMatches = rulesMatched[check1I][targetJ];
					RuleMatchHolder rightRuleMatches = rulesMatched[check2I][check2J];
					for (RuleMatch leftRuleMatch : leftRuleMatches.getRuleMatches()) {
						K leftRuleMatchedKey = leftRuleMatch.getMatchedRuleKey();
						Map<K, Set<K>> parentRuleKeysThatMatchLeftRuleKey = parentRuleKeysThatMatchChildKeys.get(leftRuleMatchedKey);
						if (parentRuleKeysThatMatchLeftRuleKey != null) {
							for (RuleMatch rightRuleMatch : rightRuleMatches.getRuleMatches()) {
								K rightRuleMatchedKey = rightRuleMatch.getMatchedRuleKey();
								Set<K> ruleKeysThatMatchBothKeys = parentRuleKeysThatMatchLeftRuleKey.get(rightRuleMatchedKey);
								if (ruleKeysThatMatchBothKeys != null) {
									for (K ruleKey : ruleKeysThatMatchBothKeys) {
										ruleMatchesForNewCell.getOrCreateRuleMatch(ruleKey).registerMatchSource(leftRuleMatch, rightRuleMatch);
									}
								}
							}
						}
					}
				}
				rulesMatched[targetI][targetJ] = ruleMatchesForNewCell;
			}
		}
		RuleMatchHolder rootRuleMatchHolder = rulesMatched[inputSize - 1][0];
		Set<K> validStartKeys = chomskyReducedFormRules.getValidStartRuleKeys();
		return
			rootRuleMatchHolder
				.getRuleMatches()
				.stream()
				.filter(ruleMatch -> validStartKeys.contains(ruleMatch.getMatchedRuleKey()))
				.map(RuleMatch::getOrBuildParseForest)
				.collect(Collectors.toList());
	}
}
