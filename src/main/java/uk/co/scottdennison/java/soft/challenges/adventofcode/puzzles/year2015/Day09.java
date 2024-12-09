package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2015;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.IntBinaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day09 implements IPuzzle {
	private static final Pattern PATTERN = Pattern.compile("^(?<city1>.+) to (?<city2>.+) = (?<distance>[0-9]+)$");

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		Set<String> citiesSet = new HashSet<>();
		Map<String, Map<String, Integer>> distances = new HashMap<>();
		for (String inputLine : LineReader.strings(inputCharacters)) {
			Matcher matcher = PATTERN.matcher(inputLine);
			if (!matcher.matches()) {
				throw new IllegalStateException("Could not parse line.");
			}
			String city1 = matcher.group("city1");
			String city2 = matcher.group("city2");
			int distance = Integer.parseInt(matcher.group("distance"));
			addCity(citiesSet, distances, city1, city2, distance);
			addCity(citiesSet, distances, city2, city1, distance);
		}
		for (String city1 : citiesSet) {
			Map<String, Integer> distancesForCity1 = distances.get(city1);
			if (distancesForCity1 == null) {
				throw new IllegalStateException("No distances for city " + city1);
			}
			for (String city2 : citiesSet) {
				if (!city1.equals(city2)) {
					if (distancesForCity1.get(city2) == null) {
						throw new IllegalStateException("No distance for " + city1 + " to " + city2);
					}
				}
			}
		}
		String[] citiesArray = citiesSet.toArray(new String[0]);
		return new BasicPuzzleResults<>(
			recurseDistance(citiesArray, distances, Integer.MAX_VALUE, Math::min),
			recurseDistance(citiesArray, distances, Integer.MIN_VALUE, Math::max)
		);
	}

	private static long recurseDistance(String[] citiesArray, Map<String, Map<String, Integer>> distances, int bestDistance, IntBinaryOperator comparisonOperator) {
		boolean[] citiesVisited = new boolean[citiesArray.length];
		return recurseDistances(citiesArray, distances, citiesVisited, null, 0, 0, bestDistance, comparisonOperator);
	}

	//There are better algorithms for the travelling salesman problem, but we'll just bruteforce it here.
	private static int recurseDistances(String[] cities, Map<String, Map<String, Integer>> distances, boolean[] citiesVisited, String previousCity, int depth, int currentDistance, int bestDistance, IntBinaryOperator comparisonOperator) {
		if (depth >= cities.length) {
			return comparisonOperator.applyAsInt(currentDistance, bestDistance);
		}
		int newDepth = depth + 1;
		for (int index = 0; index < cities.length; index++) {
			if (!citiesVisited[index]) {
				String thisCity = cities[index];
				int newDistance;
				if (previousCity == null) {
					newDistance = currentDistance;
				}
				else {
					newDistance = currentDistance + distances.get(previousCity).get(thisCity);
				}
				citiesVisited[index] = true;
				bestDistance = recurseDistances(cities, distances, citiesVisited, thisCity, newDepth, newDistance, bestDistance, comparisonOperator);
				citiesVisited[index] = false;
			}
		}
		return bestDistance;
	}

	private static void addCity(Set<String> citiesSet, Map<String, Map<String, Integer>> distances, String primaryCity, String secondaryCity, int distance) {
		citiesSet.add(primaryCity);
		if (distances.computeIfAbsent(primaryCity, __ -> new HashMap<>()).put(secondaryCity, distance) != null) {
			throw new IllegalStateException("Duplicate distance");
		}
	}
}
