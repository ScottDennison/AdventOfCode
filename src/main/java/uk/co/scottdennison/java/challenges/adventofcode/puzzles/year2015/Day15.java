package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2015;

import uk.co.scottdennison.java.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleResults;
import uk.co.scottdennison.java.libs.text.input.LineReader;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Day15 implements IPuzzle {
	private static final Pattern PATTERN_LEVEL_1 = Pattern.compile("^(?<ingredient>[a-z]+): (?<properties>.+)$", Pattern.CASE_INSENSITIVE);
	private static final Pattern PATTERN_LEVEL_2_SPLIT = Pattern.compile(",");
	private static final Pattern PATTERN_LEVEL_2 = Pattern.compile("^ *(?<propertyName>[a-z]+) (?<propertyAmount>-?[0-9]+) *$");

	private static final String CALORIES_PROPERTY_NAME = "calories";

	private static final class RunInfo {
		private final int requiredTeaspoons;
		private final Integer requiredCalories;

		public RunInfo(int requiredTeaspoons, Integer requiredCalories) {
			this.requiredTeaspoons = requiredTeaspoons;
			this.requiredCalories = requiredCalories;
		}

		public int getRequiredTeaspoons() {
			return this.requiredTeaspoons;
		}

		public Integer getRequiredCalories() {
			return this.requiredCalories;
		}
	}

	private enum ContinueResult {
		STOP,
		SKIP,
		CONTINUE
	}

	private static final RunInfo RUN_INFO_PART_A = new RunInfo(100, null);
	private static final RunInfo RUN_INFO_PART_B = new RunInfo(100, 500);

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		Map<String, Integer> propertyNameIndices = null;
		Set<String> propertyNames = null;
		List<int[][]> ingredientsTeaspoonsIndexedPropertiesList = new ArrayList<>();
		int propertyCount = -1;
		int caloriesIndex = -1;
		int maximumRequiredTeaspoons = Stream.of(RUN_INFO_PART_A, RUN_INFO_PART_B).mapToInt(RunInfo::getRequiredTeaspoons).max().orElseThrow(() -> new IllegalStateException("No runs"));
		for (String inputLine : LineReader.strings(inputCharacters)) {
			Matcher matcherLevel1 = PATTERN_LEVEL_1.matcher(inputLine);
			if (!matcherLevel1.matches()) {
				throw new IllegalStateException("Unparsable line");
			}
			Map<String, Integer> propertiesForIngredient = new HashMap<>();
			for (String level2Part : PATTERN_LEVEL_2_SPLIT.split(matcherLevel1.group("properties"))) {
				Matcher matcherLevel2 = PATTERN_LEVEL_2.matcher(level2Part);
				if (!matcherLevel2.matches()) {
					throw new IllegalStateException("Unparsable line part");
				}
				if (propertiesForIngredient.put(matcherLevel2.group("propertyName"), Integer.parseInt(matcherLevel2.group("propertyAmount"))) != null) {
					throw new IllegalStateException("Duplicate property name");
				}
			}
			Set<String> propertyNamesForIngredient = propertiesForIngredient.keySet();
			int[] ingredientIndexedProperties;
			if (propertyNames == null) {
				propertyNames = new HashSet<>(propertyNamesForIngredient);
				propertyNameIndices = new HashMap<>();
				int index = 0;
				ingredientIndexedProperties = new int[propertiesForIngredient.size()];
				for (Map.Entry<String, Integer> propertyForIngredientEntry : propertiesForIngredient.entrySet()) {
					String propertyName = propertyForIngredientEntry.getKey();
					ingredientIndexedProperties[index] = propertyForIngredientEntry.getValue();
					propertyNameIndices.put(propertyName, index);
					index++;
				}
				Integer nullableCaloriesIndex = propertyNameIndices.get(CALORIES_PROPERTY_NAME);
				if (nullableCaloriesIndex == null) {
					throw new IllegalStateException("No property named \"" + CALORIES_PROPERTY_NAME + "\"");
				}
				caloriesIndex = nullableCaloriesIndex;
				propertyCount = ingredientIndexedProperties.length;
			}
			else if (propertyNames.equals(propertyNamesForIngredient)) {
				ingredientIndexedProperties = new int[propertyNameIndices.size()];
				for (Map.Entry<String, Integer> propertyForIngredientEntry : propertiesForIngredient.entrySet()) {
					ingredientIndexedProperties[propertyNameIndices.get(propertyForIngredientEntry.getKey())] = propertyForIngredientEntry.getValue();
				}
			}
			else {
				throw new IllegalStateException("Differing property names");
			}
			if (ingredientIndexedProperties[caloriesIndex] < 0) {
				throw new IllegalStateException("Calories must be positive.");
			}
			int[][] ingredientTeaspoonsIndexedProperties = new int[maximumRequiredTeaspoons + 1][];
			for (int teaspoons = 0; teaspoons <= maximumRequiredTeaspoons; teaspoons++) {
				int[] ingredientTeaspoonIndexedProperties = new int[propertyCount];
				for (int propertyIndex = 0; propertyIndex < propertyCount; propertyIndex++) {
					ingredientTeaspoonIndexedProperties[propertyIndex] = ingredientIndexedProperties[propertyIndex] * teaspoons;
				}
				ingredientTeaspoonsIndexedProperties[teaspoons] = ingredientTeaspoonIndexedProperties;
			}
			ingredientsTeaspoonsIndexedPropertiesList.add(ingredientTeaspoonsIndexedProperties);
		}
		if (propertyNameIndices == null) {
			throw new IllegalStateException("No input.");
		}
		int[][][] ingredientsTeaspoonsIndexedProperties = ingredientsTeaspoonsIndexedPropertiesList.toArray(new int[0][][]);
		return new BasicPuzzleResults<>(
			calculateBestScore(ingredientsTeaspoonsIndexedProperties, propertyCount, caloriesIndex, RUN_INFO_PART_A),
			calculateBestScore(ingredientsTeaspoonsIndexedProperties, propertyCount, caloriesIndex, RUN_INFO_PART_B)
		);
	}

	private static int calculateBestScore(int[][][] ingredientsTeaspoonsIndexedProperties, int propertyCount, int caloriesIndex, RunInfo runInfo) {
		return calculateBestScore(ingredientsTeaspoonsIndexedProperties, propertyCount, caloriesIndex, runInfo.getRequiredTeaspoons(), runInfo.getRequiredCalories());
	}

	private static int calculateBestScore(int[][][] ingredientsTeaspoonsIndexedProperties, int propertyCount, int caloriesIndex, int requiredTeaspoons, Integer requiredCalories) {
		if (requiredCalories == null) {
			return calculateBestScore(ingredientsTeaspoonsIndexedProperties, propertyCount, caloriesIndex, requiredTeaspoons, null, null);
		}
		else {
			int requiredCaloriesPrimitive = requiredCalories;
			return calculateBestScore(ingredientsTeaspoonsIndexedProperties, propertyCount, caloriesIndex, requiredTeaspoons, indexedPropertyAmounts -> indexedPropertyAmounts[caloriesIndex] <= requiredCaloriesPrimitive ? ContinueResult.CONTINUE : ContinueResult.STOP, indexedPropertyAmounts -> indexedPropertyAmounts[caloriesIndex] == requiredCaloriesPrimitive);
		}
	}

	private static int calculateBestScore(int[][][] ingredientsTeaspoonsIndexedProperties, int propertyCount, int caloriesIndex, int requiredTeaspoons, Function<int[], ContinueResult> ongoingRestriction, Predicate<int[]> finalRestriction) {
		return recurseChoices(ingredientsTeaspoonsIndexedProperties, caloriesIndex, requiredTeaspoons, 0, new int[propertyCount], ongoingRestriction, finalRestriction);
	}

	private static int recurseChoices(int[][][] ingredientsTeaspoonsIndexedProperties, int caloriesIndex, int teaspoonsRemaining, int ingredientIndex, int[] indexedPropertyAmounts, Function<int[], ContinueResult> ongoingRestriction, Predicate<int[]> finalRestriction) {
		int remainingIngredients = ingredientsTeaspoonsIndexedProperties.length - ingredientIndex;
		int propertyCount = indexedPropertyAmounts.length;
		if (remainingIngredients == 0) {
			if (finalRestriction != null && !finalRestriction.test(indexedPropertyAmounts)) {
				return 0;
			}
			else {
				int score = 1;
				for (int propertyIndex = 0; propertyIndex < propertyCount; propertyIndex++) {
					if (propertyIndex != caloriesIndex) {
						score *= Math.max(indexedPropertyAmounts[propertyIndex], 0);
					}
				}
				return score;
			}
		}
		else {
			int nextIngredientIndex = ingredientIndex + 1;
			int[][] ingredientTeaspoonsIndexedProperties = ingredientsTeaspoonsIndexedProperties[ingredientIndex];
			int minimumTeaspoons;
			if (remainingIngredients == 1) {
				minimumTeaspoons = teaspoonsRemaining;
			}
			else {
				minimumTeaspoons = 0;
			}
			int bestScore = 0;
			for (int teaspoons = minimumTeaspoons, newTeaspoonsRemaining = teaspoonsRemaining; teaspoons <= teaspoonsRemaining; teaspoons++, newTeaspoonsRemaining--) {
				int[] ingredientTeaspoonIndexedProperties = ingredientTeaspoonsIndexedProperties[teaspoons];
				for (int propertyIndex = 0; propertyIndex < propertyCount; propertyIndex++) {
					indexedPropertyAmounts[propertyIndex] += ingredientTeaspoonIndexedProperties[propertyIndex];
				}
				ContinueResult continueResult;
				if (ongoingRestriction != null) {
					continueResult = ongoingRestriction.apply(indexedPropertyAmounts);
				}
				else {
					continueResult = ContinueResult.CONTINUE;
				}
				if (continueResult == ContinueResult.CONTINUE) {
					bestScore = Math.max(bestScore, recurseChoices(ingredientsTeaspoonsIndexedProperties, caloriesIndex, newTeaspoonsRemaining, nextIngredientIndex, indexedPropertyAmounts, ongoingRestriction, finalRestriction));
				}
				for (int propertyIndex = 0; propertyIndex < propertyCount; propertyIndex++) {
					indexedPropertyAmounts[propertyIndex] -= ingredientTeaspoonIndexedProperties[propertyIndex];
				}
				if (continueResult == ContinueResult.STOP) {
					break;
				}
			}
			return bestScore;
		}
	}
}
