package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2020;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;
import uk.co.scottdennison.java.libs.text.input.LineReader;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day01 implements IPuzzle {
	private static final int TARGET = 2020;
	private static final int PART_A_INTERESTED_TIER = 2;
	private static final int PART_B_INTERESTED_TIER = 3;

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		int[] targets = {TARGET};
		int[] interestedTiers = {PART_A_INTERESTED_TIER, PART_B_INTERESTED_TIER};
		Set<Integer> values = LineReader.stringsStream(inputCharacters).map(Integer::parseInt).collect(Collectors.toSet());
		Map<Integer, Set<Set<Integer>>> tierResults = new HashMap<>();
		for (int value : values) {
			tierResults.put(value, Collections.singleton(Collections.singleton(value)));
		}
		Set<Integer> interestedTiersSet = IntStream.of(interestedTiers).boxed().collect(Collectors.toSet());
		int maxTier = IntStream.of(interestedTiers).max().orElseThrow(() -> new IllegalStateException("No interested tiers"));
		int maxTarget = IntStream.of(targets).max().orElseThrow(() -> new IllegalStateException("No targets"));
		Map<Integer, Map<Integer, Set<Set<Integer>>>> interestedTierResults = new TreeMap<>();
		if (interestedTiersSet.contains(1)) {
			interestedTierResults.put(1, tierResults);
		}
		for (int tier = 2; tier <= maxTier; tier++) {
			Map<Integer, Set<Set<Integer>>> newTierResults = new HashMap<>();
			for (Map.Entry<Integer, Set<Set<Integer>>> oldTierResultsEntry : tierResults.entrySet()) {
				int oldSum = oldTierResultsEntry.getKey();
				for (Set<Integer> oldCombination : oldTierResultsEntry.getValue()) {
					for (int value : values) {
						if (!oldCombination.contains(value)) {
							int newSum = oldSum + value;
							if (newSum <= maxTarget) {
								Set<Integer> newCombination = new HashSet<>(oldCombination);
								newCombination.add(value);
								newTierResults.computeIfAbsent(newSum, __ -> new HashSet<>()).add(newCombination);
							}
						}
					}
				}
			}
			if (interestedTiersSet.contains(tier)) {
				interestedTierResults.put(tier, newTierResults);
			}
			tierResults = newTierResults;
		}
		return new BasicPuzzleResults<>(
			calculateResultForTier(interestedTierResults, PART_A_INTERESTED_TIER, TARGET),
			calculateResultForTier(interestedTierResults, PART_B_INTERESTED_TIER, TARGET)
		);
	}

	private static int calculateResultForTier(Map<Integer, Map<Integer, Set<Set<Integer>>>> interestedTierResults, int tier, @SuppressWarnings("SameParameterValue") int target) {
		Map<Integer, Set<Set<Integer>>> interestedTierResultsForTier = interestedTierResults.get(tier);
		Set<Set<Integer>> waysOfMakingTarget = interestedTierResultsForTier.get(target);
		if (waysOfMakingTarget == null) {
			throw new IllegalStateException("No ways of making" + target + " with tier " + tier);
		}
		else {
			Iterator<Set<Integer>> waysOfMakingTargetIterator = waysOfMakingTarget.iterator();
			Set<Integer> wayOfMakingTarget = waysOfMakingTargetIterator.next();
			if (waysOfMakingTargetIterator.hasNext()) {
				throw new IllegalStateException("Too many ways of making" + target + " with tier " + tier);
			}
			else {
				return wayOfMakingTarget.stream().mapToInt(x -> x).reduce(1, (x, y) -> x * y);
			}
		}
	}
}
