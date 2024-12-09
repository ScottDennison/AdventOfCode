package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2020;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Day21 implements IPuzzle {
    private static final Pattern PATTERN_INGREDIENTS_SPLIT = Pattern.compile(" ");
    private static final Pattern PATTERN_KNOWN_ALLERGENS_SPLIT = Pattern.compile(", ");
    private static final Pattern PATTERN_LINE = Pattern.compile("^(?<ingredients>[a-z]+(?:" + PATTERN_INGREDIENTS_SPLIT.pattern() + "[a-z]+)*) \\(contains (?<knownAllergens>[a-z]+(?:" + PATTERN_KNOWN_ALLERGENS_SPLIT.pattern() + "[a-z]+)*)\\)$");

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        Map<String,Integer> ingredientCounts = new HashMap<>();
        Map<String,Set<String>> possibleAllergenIngredients = new HashMap<>();
        for (String line : LineReader.strings(inputCharacters)) {
            Matcher matcher =  PATTERN_LINE.matcher(line);
            if (!matcher.matches()) {
                throw new IllegalStateException("Could not parse line");
            }
            String[] ingredients = PATTERN_INGREDIENTS_SPLIT.split(matcher.group("ingredients"));
            String[] knownAllergens = PATTERN_KNOWN_ALLERGENS_SPLIT.split(matcher.group("knownAllergens"));
            for (String ingredient : ingredients) {
                ingredientCounts.merge(ingredient, 1, Integer::sum);
            }
            for (String knownAllergen : knownAllergens) {
                Set<String> possibleThisAllergenIngredients = possibleAllergenIngredients.get(knownAllergen);
                Set<String> newPossibleThisAllergenIngredients;
                if (possibleThisAllergenIngredients == null) {
                    newPossibleThisAllergenIngredients = new HashSet<>(ingredients.length);
                    for (String ingredient : ingredients) {
                        newPossibleThisAllergenIngredients.add(ingredient);
                    }
                } else {
                    newPossibleThisAllergenIngredients = new HashSet<>(possibleThisAllergenIngredients.size());
                    for (String ingredient : ingredients) {
                        if (possibleThisAllergenIngredients.contains(ingredient)) {
                            newPossibleThisAllergenIngredients.add(ingredient);
                        }
                    }
                }
                possibleAllergenIngredients.put(knownAllergen,newPossibleThisAllergenIngredients);
            }
        }
        Map<String,String> allergenIngredients = new HashMap<>();
        while (allergenIngredients.size() < possibleAllergenIngredients.size()) {
            boolean modificationHappend = false;
            for (Map.Entry<String,Set<String>> possibleAllergenIngredientsEntry : possibleAllergenIngredients.entrySet()) {
                Set<String> possibleThisAllergenIngredients = possibleAllergenIngredientsEntry.getValue();
                if (possibleThisAllergenIngredients.size() == 1) {
                    String ingredient = possibleThisAllergenIngredients.iterator().next();
                    allergenIngredients.put(possibleAllergenIngredientsEntry.getKey(),ingredient);
                    for (Set<String> possibleOtherAllergenIngredients : possibleAllergenIngredients.values()) {
                        possibleOtherAllergenIngredients.remove(ingredient);
                    }
                    modificationHappend = true;
                }
            }
            if (!modificationHappend) {
                throw new IllegalStateException("Could not solve allergen<-->ingredient mapping");
            }
        }
        Set<String> ingredientsToAvoid = new HashSet<>(allergenIngredients.values());
        return new BasicPuzzleResults<>(
            ingredientCounts.entrySet().stream().filter(entry -> !ingredientsToAvoid.contains(entry.getKey())).mapToInt(Map.Entry::getValue).sum(),
            allergenIngredients.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey)).map(Map.Entry::getValue).collect(Collectors.joining(","))
        );
    }
}
