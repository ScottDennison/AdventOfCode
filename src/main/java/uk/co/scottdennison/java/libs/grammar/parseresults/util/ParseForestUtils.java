package uk.co.scottdennison.java.libs.grammar.parseresults.util;

import uk.co.scottdennison.java.libs.grammar.chomsky.model.ChomskyReducedFormNonTerminalRule;
import uk.co.scottdennison.java.libs.grammar.chomsky.model.ChomskyReducedFormRule;
import uk.co.scottdennison.java.libs.grammar.chomsky.model.ChomskyReducedFormRules;
import uk.co.scottdennison.java.libs.grammar.chomsky.model.ChomskyReducedFormTerminalRule;
import uk.co.scottdennison.java.libs.grammar.parseresults.exceptions.UnflattenableParseForestsException;
import uk.co.scottdennison.java.libs.grammar.parseresults.model.ParseForest;
import uk.co.scottdennison.java.libs.grammar.parseresults.model.ParseForestStats;
import uk.co.scottdennison.java.libs.grammar.parseresults.model.ParseTree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ParseForestUtils {
	private ParseForestUtils() {
	}

	private static <K, T extends ChomskyReducedFormRule<K>, R> Map<K, R> computeChomskyReducedFormRuleKeyMapping(Collection<? extends Collection<? extends T>> ruleCollections, Function<T, R> ruleMapper, String reasoning) throws UnflattenableParseForestsException {
		Map<K, R> keyStates = new HashMap<>();
		Set<K> badKeys = new HashSet<>();
		for (Collection<? extends T> rulesCollection : ruleCollections) {
			for (T rule : rulesCollection) {
				K key = rule.getRuleKey();
				R newResult = ruleMapper.apply(rule);
				R existingResult = keyStates.put(key, newResult);
				if (existingResult != null && newResult != existingResult) {
					badKeys.add(key);
				}
			}
		}
		if (!badKeys.isEmpty()) {
			throw new UnflattenableParseForestsException("Flattening cannot occur as the following rule keys " + reasoning + ": " + badKeys);
		}
		return keyStates;
	}

	private static <V> List<ParseTree<V>> combinateParseTreeLists(List<List<List<ParseForest<V>>>> parseForestListsToCombinate, int index, List<List<ParseForest<V>>> currentCombinations) {
		if (index >= parseForestListsToCombinate.size()) {
			return
				currentCombinations
					.stream()
					.map(ParseTree::new)
					.collect(Collectors.toList());
		}
		else {
			int nextIndex = index + 1;
			List<List<ParseForest<V>>> newCombinations = new ArrayList<>();
			for (List<ParseForest<V>> newParseForests : parseForestListsToCombinate.get(index)) {
				for (List<ParseForest<V>> existingParseForests : currentCombinations) {
					List<ParseForest<V>> combinedParseForests = new ArrayList<>(existingParseForests);
					combinedParseForests.addAll(newParseForests);
					newCombinations.add(combinedParseForests);
				}
			}
			return combinateParseTreeLists(parseForestListsToCombinate, nextIndex, newCombinations);
		}
	}

	private static <K, V extends K> List<ParseTree<V>> flattenTemporariesOfParseTree(IdentityHashMap<ParseForest<K>, ParseForest<V>> alreadyFlattenedParseForests, IdentityHashMap<ParseTree<K>, List<ParseTree<V>>> alreadyFlattenedParseTrees, Map<K, Boolean> ruleKeysAreTemporaryMap, Map<K, ParseForest<V>> terminalRuleKeyParseForestsMap, Map<K, V> nonTerminalRuleKeyValuesMap, ParseTree<K> unflattenedParseTree) throws UnflattenableParseForestsException {
		List<ParseTree<V>> flattenedParseTrees = alreadyFlattenedParseTrees.get(unflattenedParseTree);
		if (flattenedParseTrees == null) {
			List<List<List<ParseForest<V>>>> parseForestListsToCombinate = new ArrayList<>();
			List<ParseForest<K>> unflattenedParseForests = unflattenedParseTree.getChildrenView();
			for (ParseForest<K> unflattenedParseForest : unflattenedParseForests) {
				Boolean isUnflattenedParseForestTemporary = ruleKeysAreTemporaryMap.get(unflattenedParseForest.getRule());
				List<List<ParseForest<V>>> parseForestListsToCombinateForThisUnflattenedParseForest;
				K key = unflattenedParseForest.getRule();
				Collection<ParseTree<K>> unflattenedParseForestPossibilities = unflattenedParseForest.getPossibilitiesView();
				if (unflattenedParseForestPossibilities.isEmpty()) {
					ParseForest<V> valueParseForest = terminalRuleKeyParseForestsMap.get(key);
					if (valueParseForest == null) {
						throw new IllegalStateException("Non-terminal leaf node.");
					}
					parseForestListsToCombinateForThisUnflattenedParseForest = Collections.singletonList(Collections.singletonList(valueParseForest));
				}
				else if (Boolean.TRUE.equals(isUnflattenedParseForestTemporary)) {
					parseForestListsToCombinateForThisUnflattenedParseForest = new ArrayList<>();
					for (ParseTree<K> childUnflattenedParseTree : unflattenedParseForestPossibilities) {
						parseForestListsToCombinateForThisUnflattenedParseForest.addAll(
							flattenTemporariesOfParseTree(alreadyFlattenedParseForests, alreadyFlattenedParseTrees, ruleKeysAreTemporaryMap, terminalRuleKeyParseForestsMap, nonTerminalRuleKeyValuesMap, childUnflattenedParseTree)
								.stream()
								.map(ParseTree::getChildrenView)
								.collect(Collectors.toList())
						);
					}
				}
				else if (Boolean.FALSE.equals(isUnflattenedParseForestTemporary)) {
					parseForestListsToCombinateForThisUnflattenedParseForest = Collections.singletonList(Collections.singletonList(flattenTemporariesOfNonTemporaryParseForest(alreadyFlattenedParseForests, alreadyFlattenedParseTrees, ruleKeysAreTemporaryMap, terminalRuleKeyParseForestsMap, nonTerminalRuleKeyValuesMap, unflattenedParseForest)));
				}
				else {
					throw new IllegalStateException("Unexpected temporariness.");
				}
				parseForestListsToCombinate.add(parseForestListsToCombinateForThisUnflattenedParseForest);
			}
			flattenedParseTrees = combinateParseTreeLists(parseForestListsToCombinate, 0, Collections.singletonList(Collections.emptyList()));
			alreadyFlattenedParseTrees.put(unflattenedParseTree, flattenedParseTrees);
		}
		return flattenedParseTrees;
	}

	private static <K, V extends K> ParseForest<V> flattenTemporariesOfNonTemporaryParseForest(IdentityHashMap<ParseForest<K>, ParseForest<V>> alreadyFlattenedParseForests, IdentityHashMap<ParseTree<K>, List<ParseTree<V>>> alreadyFlattenedParseTrees, Map<K, Boolean> ruleKeysAreTemporaryMap, Map<K, ParseForest<V>> terminalRuleKeyParseForestsMap, Map<K, V> nonTerminalRuleKeyValuesMap, ParseForest<K> unflattenedParseForest) throws UnflattenableParseForestsException {
		ParseForest<V> flattenedParseForest = alreadyFlattenedParseForests.get(unflattenedParseForest);
		if (flattenedParseForest == null) {
			K key = unflattenedParseForest.getRule();
			if (!Boolean.FALSE.equals(ruleKeysAreTemporaryMap.get(key))) {
				throw new UnflattenableParseForestsException("Only non-temporary parse forests can be flattened by this method.");
			}
			Collection<ParseTree<K>> unflattenedParseForestPossibilities = unflattenedParseForest.getPossibilitiesView();
			if (unflattenedParseForestPossibilities.isEmpty()) {
				ParseForest<V> valueParseForest = terminalRuleKeyParseForestsMap.get(key);
				if (valueParseForest == null) {
					throw new IllegalStateException("Non-terminal leaf node.");
				}
				flattenedParseForest = valueParseForest;
			}
			else {
				V value = nonTerminalRuleKeyValuesMap.get(key);
				if (value == null) {
					throw new IllegalStateException("Unmappable value.");
				}
				List<ParseTree<V>> flattenedParseTrees = new ArrayList<>();
				for (ParseTree<K> unflattenedParseTree : unflattenedParseForestPossibilities) {
					flattenedParseTrees.addAll(flattenTemporariesOfParseTree(alreadyFlattenedParseForests, alreadyFlattenedParseTrees, ruleKeysAreTemporaryMap, terminalRuleKeyParseForestsMap, nonTerminalRuleKeyValuesMap, unflattenedParseTree));
				}
				flattenedParseForest = new ParseForest<>(
					value,
					flattenedParseTrees
				);
			}
			alreadyFlattenedParseForests.put(unflattenedParseForest, flattenedParseForest);
		}
		return flattenedParseForest;
	}


	public static <K, V extends K> Collection<ParseForest<V>> flattenTemporariesOfParseForests(Collection<ParseForest<K>> parseForests, ChomskyReducedFormRules<K, V> chomskyReducedFormRules) throws UnflattenableParseForestsException {
		Collection<ChomskyReducedFormNonTerminalRule<K>> nonTerminalRules = chomskyReducedFormRules.getNonTerminalRulesView();
		Collection<ChomskyReducedFormTerminalRule<K, V>> terminalRules = chomskyReducedFormRules.getTerminalRulesView();
		Map<K, Boolean> ruleKeysAreTemporaryMap = computeChomskyReducedFormRuleKeyMapping(Arrays.asList(nonTerminalRules, terminalRules), ChomskyReducedFormRule::isTemporary, "have both associated temporary rules and associated non-temporary rules");
		List<ParseTree<V>> emptyParseForestTreeList = Collections.emptyList();
		Map<K, ParseForest<V>> terminalRuleKeyParseForestsMap = computeChomskyReducedFormRuleKeyMapping(Collections.singleton(terminalRules), terminalRule -> new ParseForest<>(terminalRule.getOutput(), emptyParseForestTreeList), "have multiple associated terminal rules which produce differing outputs");
		Map<K, V> nonTerminalRuleKeyValuesMap = new HashMap<>();
		Map<K, ClassCastException> failedCastExceptions = new HashMap<>();
		Class<V> valueClazz = chomskyReducedFormRules.getValueClass();
		for (ChomskyReducedFormNonTerminalRule<K> nonTerminalRule : nonTerminalRules) {
			if (!nonTerminalRule.isTemporary()) {
				K key = nonTerminalRule.getRuleKey();
				if (!nonTerminalRuleKeyValuesMap.containsKey(key) && !failedCastExceptions.containsKey(key)) {
					try {
						nonTerminalRuleKeyValuesMap.put(key, valueClazz.cast(key));
					} catch (ClassCastException ex) {
						failedCastExceptions.put(key, ex);
					}
				}
			}
		}
		if (!failedCastExceptions.isEmpty()) {
			UnflattenableParseForestsException rootException = new UnflattenableParseForestsException("Could not convert the following keys back into their values");
			for (Map.Entry<K, ClassCastException> failedCastExceptionEntry : failedCastExceptions.entrySet()) {
				rootException.addSuppressed(
					new IllegalStateException(
						"Unable to convert key " + failedCastExceptionEntry.getKey(),
						failedCastExceptionEntry.getValue()
					)
				);
			}
			throw rootException;
		}
		List<ParseForest<V>> flattenedParseForests = new ArrayList<>();
		IdentityHashMap<ParseForest<K>, ParseForest<V>> alreadyFlattenedParseForests = new IdentityHashMap<>();
		IdentityHashMap<ParseTree<K>, List<ParseTree<V>>> alreadyFlattenedParseTrees = new IdentityHashMap<>();
		for (ParseForest<K> rootParseForest : parseForests) {
			if (ruleKeysAreTemporaryMap.get(rootParseForest.getRule())) {
				throw new UnflattenableParseForestsException("Flattening cannot occur as one or more root rules is temporary.");
			}
			flattenedParseForests.add(flattenTemporariesOfNonTemporaryParseForest(alreadyFlattenedParseForests, alreadyFlattenedParseTrees, ruleKeysAreTemporaryMap, terminalRuleKeyParseForestsMap, nonTerminalRuleKeyValuesMap, rootParseForest));
		}
		return flattenedParseForests;
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	private static OptionalLong multiplyWays(OptionalLong left, OptionalLong right) {
		if (left.isPresent()) {
			if (right.isPresent()) {
				try {
					return OptionalLong.of(Math.multiplyExact(left.getAsLong(), right.getAsLong()));
				} catch (ArithmeticException ex) {
					return OptionalLong.empty();
				}
			}
			else {
				return right;
			}
		}
		else {
			return left;
		}
	}

	private static ParseForestStats combineParseForestStatsForForest(ParseForestStats leftParseForestStats, ParseForestStats rightParseForestStats) {
		return new ParseForestStats(
			Math.min(leftParseForestStats.getMinNodes(), rightParseForestStats.getMinNodes()),
			Math.max(leftParseForestStats.getMaxNodes(), rightParseForestStats.getMaxNodes()),
			Math.min(leftParseForestStats.getMinDepth(), rightParseForestStats.getMinDepth()),
			Math.max(leftParseForestStats.getMaxDepth(), rightParseForestStats.getMaxDepth()),
			multiplyWays(leftParseForestStats.getWays(), rightParseForestStats.getWays())
		);
	}

	private static ParseForestStats updateParseForestStatsForForests(ParseForestStats parseForestStats, int count) {
		return new ParseForestStats(
			parseForestStats.getMinNodes(),
			parseForestStats.getMaxNodes(),
			parseForestStats.getMinDepth(),
			parseForestStats.getMaxDepth(),
			multiplyWays(parseForestStats.getWays(), OptionalLong.of(count))
		);
	}

	private static ParseForestStats combineParseForestStatsForTree(ParseForestStats leftParseForestStats, ParseForestStats rightParseForestStats) {
		int maxDepth = Math.max(leftParseForestStats.getMaxDepth(), rightParseForestStats.getMaxDepth());
		return new ParseForestStats(
			leftParseForestStats.getMinNodes() + rightParseForestStats.getMinNodes(),
			leftParseForestStats.getMaxNodes() + rightParseForestStats.getMaxNodes(),
			maxDepth,
			maxDepth,
			multiplyWays(leftParseForestStats.getWays(), rightParseForestStats.getWays())
		);
	}

	private static ParseForestStats updateParseForestStatsForTrees(ParseForestStats parseForestStats) {
		return new ParseForestStats(
			parseForestStats.getMinNodes() + 1,
			parseForestStats.getMaxNodes() + 1,
			parseForestStats.getMinDepth(),
			parseForestStats.getMaxDepth(),
			parseForestStats.getWays()
		);
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	private static final OptionalLong OPTIONAL_LONG_ONE = OptionalLong.of(1L);

	private static <K> ParseForestStats computeParseForestStats(IdentityHashMap<ParseForest<K>, ParseForestStats> alreadyComputedParseForestStats, int depth, ParseForest<K> parseForest) {
		ParseForestStats parseForestStats = alreadyComputedParseForestStats.get(parseForest);
		if (parseForestStats == null) {
			Collection<ParseTree<K>> parseForestPossibilities = parseForest.getPossibilitiesView();
			if (parseForestPossibilities.isEmpty()) {
				parseForestStats = new ParseForestStats(0, 0, depth, depth, OPTIONAL_LONG_ONE);
			}
			else {
				int nextDepth = depth + 1;
				parseForestStats =
					parseForestPossibilities
						.stream()
						.map(
							parseTree ->
								parseTree
									.getChildrenView()
									.stream()
									.map(childParseForest -> computeParseForestStats(alreadyComputedParseForestStats, nextDepth, childParseForest))
									.reduce(ParseForestUtils::combineParseForestStatsForTree)
									.map(ParseForestUtils::updateParseForestStatsForTrees)
									.orElseThrow(() -> new IllegalStateException("No children"))
						)
						.reduce(ParseForestUtils::combineParseForestStatsForForest)
						.map(parseTreeStats -> updateParseForestStatsForForests(parseTreeStats, parseForestPossibilities.size()))
						.get();
			}
			alreadyComputedParseForestStats.put(parseForest, parseForestStats);
		}
		return parseForestStats;
	}

	public static <K> ParseForestStats computeParseForestStats(Collection<ParseForest<K>> parseForests) {
		IdentityHashMap<ParseForest<K>, ParseForestStats> alreadyComputedParseForestStats = new IdentityHashMap<>();
		return
			parseForests
				.stream()
				.map(parseForest -> computeParseForestStats(alreadyComputedParseForestStats, 0, parseForest))
				.reduce(ParseForestUtils::combineParseForestStatsForForest)
				.orElseThrow(() -> new IllegalStateException("No parse forests."));
	}
}
