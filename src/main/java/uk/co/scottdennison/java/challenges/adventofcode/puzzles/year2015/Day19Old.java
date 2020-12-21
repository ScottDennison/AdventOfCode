package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2015;

import uk.co.scottdennison.java.challenges.adventofcode.utils.InputFileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Day19Old {
	private static final Pattern PATTERN_ATOM_REPLACEMENT = Pattern.compile("^(?<fromAtom>[A-Z]+) => (?<toMolecule>[A-Z]+)$", Pattern.CASE_INSENSITIVE);

	private static final String INITIAL_MOLECULE = "e";

	// This class stores direct references to the input and output arrays to prevent excessive copying.
	private static class Replacement {
		private final char[] input;
		private final char[][] outputs;

		public Replacement(char[] input, char[][] outputs) {
			this.input = input;
			this.outputs = outputs;
		}

		public char[] getInput() {
			return this.input;
		}

		public char[][] getOutputs() {
			return this.outputs;
		}
	}

	public static void main(String[] args) throws IOException {
		Map<String, Set<String>> possibleAtomReplacementsStringsMap = new HashMap<>();
		Iterator<String> iterator = Files.lines(InputFileUtils.getInputPath()).iterator();
		while (iterator.hasNext()) {
			String fileLine = iterator.next();
			if (fileLine.isEmpty()) {
				break;
			}
			Matcher matcher = PATTERN_ATOM_REPLACEMENT.matcher(fileLine);
			if (!matcher.matches()) {
				throw new IllegalStateException("Could not parse molecule replacement.");
			}
			possibleAtomReplacementsStringsMap.computeIfAbsent(matcher.group("fromAtom"), __ -> new HashSet<>()).add(matcher.group("toMolecule"));
		}
		if (!iterator.hasNext()) {
			throw new IllegalStateException("Missing the input molecule");
		}
		String inputMoleculeString = iterator.next();
		if (iterator.hasNext()) {
			throw new IllegalStateException("Expected EOF, but saw more input");
		}
		System.out.format("Number of possible molecules that can result by applying doing a single replacement to the input molecule: %d", countPossibleReplacementsCount(inputMoleculeString, possibleAtomReplacementsStringsMap));
		System.out.format("Number of steps to get from %s to the input molecule: %d", INITIAL_MOLECULE, calculateReverseStepsRequired(INITIAL_MOLECULE, inputMoleculeString, possibleAtomReplacementsStringsMap));
	}

	private static Replacement[] convertToReplacements(Map<String,Set<String>> inputStringsMap) {
		return
			inputStringsMap
				.entrySet()
				.stream()
				.map(
					entry -> new Replacement(
						entry.getKey().toCharArray(),
						entry.getValue().stream().map(String::toCharArray).toArray(char[][]::new)
					)
				).toArray(Replacement[]::new);
	}

	private static void runReplacements(String originalString, Collection<String> newStringsSet, Replacement[] replacements) {
		char[] originalCharacters = originalString.toCharArray();
		int originalCharacterCount = originalCharacters.length;
		for (int baseOriginalCharacterIndex=0; baseOriginalCharacterIndex<originalCharacterCount; baseOriginalCharacterIndex++) {
			for (Replacement replacement : replacements) {
				char[] replacementInputCharacters = replacement.getInput();
				int replacementInputCharacterCount = replacementInputCharacters.length;
				boolean isMatch = true;
				for (int replacementInputCharacterIndex = 0, originalCharacterIndex = baseOriginalCharacterIndex; replacementInputCharacterIndex < replacementInputCharacterCount; replacementInputCharacterIndex++, originalCharacterIndex++) {
					if (originalCharacterIndex >= originalCharacterCount || originalCharacters[originalCharacterIndex] != replacementInputCharacters[replacementInputCharacterIndex]) {
						isMatch = false;
						break;
					}
				}
				if (isMatch) {
					System.out.println("Found " + new String(replacementInputCharacters) + " at position " + baseOriginalCharacterIndex);
					for (char[] replacementOutputCharacters : replacement.getOutputs()) {
						System.out.println("\tReplacing with " + new String(replacementOutputCharacters));
						int replacementOutputCharacterCount = replacementOutputCharacters.length;
						char[] newCharacters = new char[originalCharacterCount - replacementInputCharacterCount + replacementOutputCharacterCount];
						System.arraycopy(originalCharacters, 0, newCharacters, 0, baseOriginalCharacterIndex);
						System.arraycopy(replacementOutputCharacters, 0, newCharacters, baseOriginalCharacterIndex, replacementOutputCharacterCount);
						System.arraycopy(originalCharacters, baseOriginalCharacterIndex + replacementInputCharacterCount, newCharacters, baseOriginalCharacterIndex + replacementOutputCharacterCount, originalCharacterCount - baseOriginalCharacterIndex - replacementInputCharacterCount);
						newStringsSet.add(new String(newCharacters));
					}
				}
			}
		}
	}

	private static int countPossibleReplacementsCount(String inputMoleculeString, Map<String, Set<String>> possibleAtomReplacementsStringsMap) {
		Replacement[] possibleAtomToMoleculeReplacements = convertToReplacements(possibleAtomReplacementsStringsMap);
		Set<String> possibleMoleculeReplacementMoleculeStrings = new HashSet<>();
		runReplacements(inputMoleculeString, possibleMoleculeReplacementMoleculeStrings, possibleAtomToMoleculeReplacements);
		return possibleMoleculeReplacementMoleculeStrings.size();
	}

	private static int calculateReverseStepsRequired(@SuppressWarnings("SameParameterValue") String initialMoleculeString, String inputMoleculeString, Map<String, Set<String>> possibleAtomReplacementsStringsMap) {
		class SingleAtomToMoleculeReplacement {
			private final String inputAtom;
			private final String outputMolecule;

			public SingleAtomToMoleculeReplacement(String inputAtom, String outputMolecule) {
				this.inputAtom = inputAtom;
				this.outputMolecule = outputMolecule;
			}

			public String getInputAtom() {
				return this.inputAtom;
			}

			public String getOutputMolecule() {
				return this.outputMolecule;
			}
		}

		Replacement[] possibleMoleculeToAtomReplacements =
			convertToReplacements(
				possibleAtomReplacementsStringsMap
					.entrySet()
					.stream()
					.flatMap(
						entry -> {
							String inputAtom = entry.getKey();
							return entry.getValue()
								.stream()
								.map(
									outputMolecule -> new SingleAtomToMoleculeReplacement(inputAtom,outputMolecule)
								);
						}
					)
					.collect(
						Collectors.groupingBy(
							SingleAtomToMoleculeReplacement::getOutputMolecule,
							Collectors.mapping(
								SingleAtomToMoleculeReplacement::getInputAtom,
								Collectors.toSet()
							)
						)
					)
			);

		Set<String> pendingMoleculeStrings = Collections.singleton(inputMoleculeString);
		boolean foundTargetMolecule = false;
		int stepsTaken = 0;
		while (!pendingMoleculeStrings.isEmpty()) {
			if (pendingMoleculeStrings.contains(initialMoleculeString)) {
				foundTargetMolecule = true;
				break;
			}
			stepsTaken++;
			List<String> newPendingMoleculeStrings = new ArrayList<>();
			for (String pendingMoleculeString : pendingMoleculeStrings) {
				runReplacements(pendingMoleculeString, newPendingMoleculeStrings, possibleMoleculeToAtomReplacements);
			}
			pendingMoleculeStrings = new HashSet<>(newPendingMoleculeStrings);
			System.out.println(newPendingMoleculeStrings);
			if (stepsTaken >= 1) {
				break;
			}
		}
		if (!foundTargetMolecule) {
			throw new IllegalStateException("The target molecule was not found.");
		}
		return stepsTaken;
	}
}