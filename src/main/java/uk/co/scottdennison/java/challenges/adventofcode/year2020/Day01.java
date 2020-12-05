package uk.co.scottdennison.java.challenges.adventofcode.year2020;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day01 {
	private static final int[] TARGETS = {2020};
	private static final int[] INTERESTED_TIERS = {2, 3};

	public static void main(String[] args) throws IOException {
		Set<Integer> values = Files.lines(Paths.get("data/year2020/day01/input.txt")).map(Integer::parseInt).collect(Collectors.toSet());
		Map<Integer, Set<Set<Integer>>> tierResults = new HashMap<>();
		for (int value : values) {
			tierResults.put(value, Collections.singleton(Collections.singleton(value)));
		}
		Set<Integer> interestedTiersSet = IntStream.of(INTERESTED_TIERS).boxed().collect(Collectors.toSet());
		int[] sortedTargets = Arrays.copyOf(TARGETS, TARGETS.length);
		Arrays.sort(sortedTargets);
		int maxTier = IntStream.of(INTERESTED_TIERS).max().orElseThrow(() -> new IllegalStateException("No interested tiers"));
		int maxTarget = IntStream.of(TARGETS).max().orElseThrow(() -> new IllegalStateException("No targets"));
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
		for (Map.Entry<Integer, Map<Integer, Set<Set<Integer>>>> interestedTierResultsEntry : interestedTierResults.entrySet()) {
			int tier = interestedTierResultsEntry.getKey();
			Map<Integer, Set<Set<Integer>>> interestedTierResultsForTier = interestedTierResultsEntry.getValue();
			for (int target : sortedTargets) {
				System.out.format("Ways of making %d using %d tiers: ", target, tier);
				Set<Set<Integer>> waysOfMakingTarget = interestedTierResultsForTier.get(target);
				if (waysOfMakingTarget == null) {
					System.out.format("No Solution");
				}
				else {
					Iterator<Set<Integer>> waysOfMakingTargetIterator = waysOfMakingTarget.iterator();
					Set<Integer> wayOfMakingTarget = waysOfMakingTargetIterator.next();
					if (waysOfMakingTargetIterator.hasNext()) {
						System.out.format("Too many solutions");
					}
					else {
						System.out.format("Target made using %s with product of %s", wayOfMakingTarget, wayOfMakingTarget.stream().mapToInt(x -> x).reduce(1, (x, y) -> x * y));
					}
				}
				System.out.println();
			}
		}
	}
}
