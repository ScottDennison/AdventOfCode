package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2015;

import uk.co.scottdennison.java.challenges.adventofcode.utils.InputFileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day13 {
	private static final Pattern PATTERN = Pattern.compile("^(?<name1>(.+)) would (?<happinessModifier>gain|lose) (?<happiness>([0-9]+)) happiness unit(?:s)? by sitting next to (?<name2>(.+))\\.$");

	public static void main(String[] args) throws IOException {
		Set<String> namesSet = new HashSet<>();
		Map<String, Map<String, Integer>> happinesses = new HashMap<>();
		for (String fileLine : Files.readAllLines(InputFileUtils.getInputPath())) {
			Matcher matcher = PATTERN.matcher(fileLine);
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
		outputSummary("excluding", namesSet, happinesses);
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
		outputSummary("including", namesSet, happinesses);
	}

	private static void outputSummary(String myState, Set<String> namesSet, Map<String, Map<String, Integer>> happinesses) {
		String[] namesArray = namesSet.toArray(new String[0]);
		boolean[] namesUsed = new boolean[namesArray.length];
		System.out.format("Best happiness %s me: %d%n", myState, recurseHappinesses(namesArray, happinesses, namesUsed, null, null, 0, 0));
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
