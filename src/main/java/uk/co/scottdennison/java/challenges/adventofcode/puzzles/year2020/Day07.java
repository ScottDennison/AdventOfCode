package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2020;

import uk.co.scottdennison.java.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.challenges.adventofcode.framework.IPuzzleResults;
import uk.co.scottdennison.java.libs.text.input.LineReader;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Day07 implements IPuzzle {
	private static final Pattern INNER_PATTERN = Pattern.compile("(?<innerBagCount>[0-9]+) (?<innerBagColour>[a-z ]+?) bag(?:s?)");
	private static final Pattern OUTER_PATTERN = Pattern.compile("^(?<outerBagColour>[a-z ]+?) bags contain ((?<innerBags>(?:[0-9]+ [a-z ]+? bag(?:s)?)(?:, (?:[0-9]+ [a-z ]+? bag(?:s)?))*)|(?:no other bags))\\.$");

	private static final String BAG_OF_INTEREST = "shiny gold";

	private static class BagInformation {
		private final String colour;
		private final int count;

		public BagInformation(String colour, int count) {
			this.colour = colour;
			this.count = count;
		}

		public String getColour() {
			return this.colour;
		}

		public int getCount() {
			return this.count;
		}
	}

	@Override
	public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
		Map<String, Set<BagInformation>> bagInformation = new HashMap<>();
		for (String inputLine : LineReader.strings(inputCharacters)) {
			Matcher outerMatcher = OUTER_PATTERN.matcher(inputLine);
			if (!outerMatcher.matches()) {
				throw new IllegalStateException("Could not match line against outer pattern: " + inputLine);
			}
			String innerBags = outerMatcher.group("innerBags");
			Set<BagInformation> bagInformationForOuterBag = new HashSet<>();
			if (innerBags != null) {
				Matcher innerMatcher = INNER_PATTERN.matcher(innerBags);
				Set<String> seenInnerBagColours = new HashSet<>();
				while (innerMatcher.find()) {
					String innerBagColour = innerMatcher.group("innerBagColour").intern();
					if (!seenInnerBagColours.add(innerBagColour)) {
						throw new IllegalStateException("Duplicate bag colour.");
					}
					int innerBagCount = Integer.parseInt(innerMatcher.group("innerBagCount"));
					if (innerBagCount < 1) {
						throw new IllegalStateException("Invalid inner bag count.");
					}
					bagInformationForOuterBag.add(new BagInformation(innerBagColour, innerBagCount));
				}
				if (!innerMatcher.hitEnd()) {
					throw new IllegalStateException("Not all bag information could be parsed for a line.");
				}
			}
			String outerBagColour = outerMatcher.group("outerBagColour").intern();
			if (bagInformation.put(outerBagColour, bagInformationForOuterBag) != null) {
				throw new IllegalStateException("Duplicate outer bag colour.");
			}
		}
		if (!bagInformation.containsKey(BAG_OF_INTEREST)) {
			throw new IllegalStateException("There is no bag with the colour " + BAG_OF_INTEREST + ".");
		}
		Map<String, Set<String>> bagColourContainers = new HashMap<>();
		for (Map.Entry<String, Set<BagInformation>> bagInformationEntry : bagInformation.entrySet()) {
			String outerBagColour = bagInformationEntry.getKey();
			for (BagInformation innerBagInformation : bagInformationEntry.getValue()) {
				bagColourContainers.computeIfAbsent(innerBagInformation.getColour(), __ -> new HashSet<>()).add(outerBagColour);
			}
		}
		Set<String> bagColoursEventuallyContainingBagOfInterest = new HashSet<>();
		Set<String> bagColoursToInvestigate = bagColourContainers.get(BAG_OF_INTEREST);
		if (bagColoursToInvestigate != null) {
			while (!bagColoursToInvestigate.isEmpty()) {
				bagColoursEventuallyContainingBagOfInterest.addAll(bagColoursToInvestigate);
				bagColoursToInvestigate =
					bagColoursToInvestigate
						.stream()
						.map(bagColourContainers::get)
						.filter(Objects::nonNull)
						.flatMap(Set::stream)
						.distinct()
						.filter(bagColour -> !bagColoursEventuallyContainingBagOfInterest.contains(bagColour))
						.collect(Collectors.toSet());
			}
		}
		int totalBagCount = 0;
		List<BagInformation> pendingBagInformation = Collections.singletonList(new BagInformation(BAG_OF_INTEREST, 1));
		while (!pendingBagInformation.isEmpty()) {
			List<BagInformation> newPendingBagInformation = new ArrayList<>();
			for (BagInformation pendingBagInformationEntry : pendingBagInformation) {
				int thisBagCount = pendingBagInformationEntry.getCount();
				totalBagCount += thisBagCount;
				for (BagInformation childBagInformation : bagInformation.get(pendingBagInformationEntry.getColour())) {
					newPendingBagInformation.add(new BagInformation(childBagInformation.getColour(), childBagInformation.getCount() * thisBagCount));
				}
			}
			pendingBagInformation = newPendingBagInformation;
		}
		totalBagCount -= 1; // We don't count the bag of interest itself.
		return new BasicPuzzleResults<>(
			bagColoursEventuallyContainingBagOfInterest.size(),
			totalBagCount
		);
	}
}
