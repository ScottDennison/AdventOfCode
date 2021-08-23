package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2015;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;
import uk.co.scottdennison.java.libs.text.input.LineReader;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day13 implements IPuzzle {
	private static final Pattern PATTERN = Pattern.compile("^(?<name1>(.+)) would (?<happinessModifier>gain|lose) (?<happiness>([0-9]+)) happiness unit(?:s)? by sitting next to (?<name2>(.+))\\.$");

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		Set<String> namesSet = new HashSet<>();
		Map<String, Map<String, Integer>> happinesses = new HashMap<>();
		for (String inputLine : LineReader.strings(inputCharacters)) {
			Matcher matcher = PATTERN.matcher(inputLine);
			if (!matcher.matches()) {
				throw new IllegalStateException("Could not parse line.");
			}
			String name1 = matcher.group("name1");
			String name2 = matcher.group("name2");
			int happiness = ("gain".equalsIgnoreCase(matcher.group("happinessModifier")) ? 1 : -1) * Integer.parseInt(matcher.group("happiness"));
			addHappiness(namesSet, happinesses, name1, name2, happiness);
			addHappiness(namesSet, happinesses, name2, name1, happiness);
		}
		for (String name1 : namesSet) {
			Map<String, Integer> happinessesForName1 = happinesses.get(name1);
			if (happinessesForName1 == null) {
				throw new IllegalStateException("No happinesses for name " + name1);
			}
			for (String name2 : namesSet) {
				if (!name1.equals(name2)) {
					if (happinessesForName1.get(name2) == null) {
						throw new IllegalStateException("No happiness for " + name1 + " to " + name2);
					}
				}
			}
		}
		int happinessesExcludingMe = recurseHappinesses(namesSet, happinesses);
		StringBuilder myNameBuilder = new StringBuilder();
		String myName;
		do {
			myNameBuilder.append('_');
			myName = myNameBuilder.toString();
		} while (namesSet.contains(myName));
		for (String otherName : new HashSet<>(namesSet)) {
			addHappiness(namesSet, happinesses, myName, otherName, 0);
			addHappiness(namesSet, happinesses, otherName, myName, 0);
		}
		int happinessesIncludingMe = recurseHappinesses(namesSet, happinesses);
		return new BasicPuzzleResults<>(
			happinessesExcludingMe,
			happinessesIncludingMe
		);
	}

	private static int recurseHappinesses(Set<String> namesSet, Map<String, Map<String, Integer>> happinesses) {
		String[] namesArray = namesSet.toArray(new String[0]);
		boolean[] namesUsed = new boolean[namesArray.length];
		return recurseHappinesses(namesArray, happinesses, namesUsed, null, null, 0, 0);
	}

	private static int recurseHappinesses(String[] names, Map<String, Map<String, Integer>> happinesses, boolean[] namesUsed, String firstName, String previousName, int depth, int currentHappiness) {
		if (depth >= names.length) {
			return currentHappiness + happinesses.get(previousName).get(firstName);
		}
		int newDepth = depth + 1;
		int bestHappiness = Integer.MIN_VALUE;
		for (int index = 0; index < names.length; index++) {
			if (!namesUsed[index]) {
				String thisName = names[index];
				int newHappiness;
				String newFirstName;
				if (previousName == null) {
					newHappiness = currentHappiness;
				}
				else {
					newHappiness = currentHappiness + happinesses.get(previousName).get(thisName);
				}
				if (firstName == null) {
					newFirstName = thisName;
				}
				else {
					newFirstName = firstName;
				}
				namesUsed[index] = true;
				bestHappiness = Math.max(bestHappiness, recurseHappinesses(names, happinesses, namesUsed, newFirstName, thisName, newDepth, newHappiness));
				namesUsed[index] = false;
			}
		}
		return bestHappiness;
	}

	private static void addHappiness(Set<String> namesSet, Map<String, Map<String, Integer>> happinesses, String leftName, String rightName, int happiness) {
		namesSet.add(leftName);
		happinesses.computeIfAbsent(leftName, __ -> new HashMap<>()).merge(rightName, happiness, Math::addExact);
	}
}
