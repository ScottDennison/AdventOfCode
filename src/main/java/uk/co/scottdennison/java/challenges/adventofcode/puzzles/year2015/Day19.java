package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2015;

import uk.co.scottdennison.java.challenges.adventofcode.utils.InputFileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day19 {
	/*
	Unfortunately, my earlier forward and reverse breadth first search approaches failed, so I turned to reddit for this one
	As such, I turned to reddit for inspiration.

	The top answer involved hand analysis and the finding that all productions are in one of the forms ['X => XX','X => X Rn X Ar','X => X Rn X Y X Ar','X => X Rn X Y X Y X Ar'], which allows for massive simplification.
	However, not only is this something I would never have realised, it seems more like a quirk of the way the AOC creator generated the input files in the first place.

	A lot more of the solutions either
	* include random numbers.
	* are not deterministic, and can sometimes return the wrong answer.
	* or both of the above.

	Luckily, /u/anoi came to the rescue with the singular mention of the use of the CYK algorithm (https://www.reddit.com/r/adventofcode/comments/3xflz8/day_19_solutions/cy4ds8a).
	If I had known about it, I probably would have been able to think of using it relatively early on without ever looking at reddit. Unfortunately, I did not.
	Rather than rushing, I then spent a long time understanding Chomsky Normal Form (though I use it's reduced form cousin) and the CYK algorithm, resulting in this class.

	Also, note: technically 'e' is an electron and not a atom, but for the ease of implementation, we'll consider it an atom.
	*/

	private static final Pattern PATTERN_ATOM_TO_MOLECULE_REPLACEMENT = Pattern.compile("^(?<fromAtom>(?:(?:[A-Z][a-z]*)|e)) => (?<toMolecule>.+)$");
	private static final Pattern PATTERN_ATOM = Pattern.compile("[A-Z][a-z]*");

	private static final String INITIAL_MOLECULE = "e";

	private static final class Atom {
		private final String representation;

		private Atom(String representation) {
			this.representation = representation;
		}

		public static Atom create(String representation) {
			if (representation == null) {
				throw new IllegalStateException("Representation is NULL.");
			} else if (representation.isEmpty()) {
				throw new IllegalStateException("Representation is empty.");
			}
			return new Atom(representation);
		}

		public String getRepresentation() {
			return this.representation;
		}

		@Override
		public boolean equals(Object otherObject) {
			if (otherObject == this) {
				return true;
			}
			if (otherObject == null || otherObject.getClass() != Atom.class) {
				return false;
			}
			Atom otherAtom = (Atom) otherObject;
			return this.representation.equals(otherAtom.representation);
		}

		@Override
		public int hashCode() {
			return this.representation.hashCode();
		}

		@Override
		public String toString() {
			return this.representation;
		}
	}

	private static final class Molecule {
		private final List<Atom> atoms;
		private transient int hashCode = 0;

		private Molecule(List<Atom> atoms) {
			this.atoms = atoms;
		}

		private static void validateAtomsList(List<Atom> atoms) {
			if (atoms == null) {
				throw new IllegalStateException("Atoms list is NULL.");
			} else if (atoms.contains(null)) {
				throw new IllegalStateException("Atoms list contains a NULL atom.");
			} else if (atoms.isEmpty()) {
				throw new IllegalStateException("Atoms list is empty.");
			}
		}

		private void validateAtomIndex(int index) {
			if (index < 0 || index >= this.atoms.size()) {
				throw new IllegalArgumentException("Invalid atom index.");
			}
		}

		public static Molecule create(List<Atom> atoms) {
			validateAtomsList(atoms);
			return new Molecule(atoms);
		}

		public Atom getAtom(int atomIndex) {
			validateAtomIndex(atomIndex);
			return this.atoms.get(atomIndex);
		}

		public int getAtomCount() {
			return this.atoms.size();
		}

		public List<Atom> getAtomsView() {
			return Collections.unmodifiableList(this.atoms);
		}

		public Molecule replaceAtomWithAtoms(int atomIndex, List<Atom> replacementAtoms) {
			validateAtomIndex(atomIndex);
			validateAtomsList(replacementAtoms);
			List<Atom> newAtoms = new ArrayList<>(this.atoms);
			newAtoms.remove(atomIndex);
			newAtoms.addAll(atomIndex, replacementAtoms);
			return new Molecule(newAtoms);
		}

		@Override
		public boolean equals(Object otherObject) {
			if (otherObject == this) {
				return true;
			}
			if (otherObject == null || otherObject.getClass() != Molecule.class) {
				return false;
			}
			Molecule otherMolecule = (Molecule) otherObject;
			return this.atoms.equals(otherMolecule.atoms);
		}

		@Override
		public int hashCode() {
			int hashCode;
			if ((hashCode = this.hashCode) == 0) {
				hashCode = this.atoms.hashCode();
				this.hashCode = hashCode;
			}
			return hashCode;
		}

		@Override
		public String toString() {
			return this.atoms.toString();
		}
	}

	private static Atom createCanonicalizedAtom(Map<Atom,Atom> knownAtoms, String atomRepresentation) {
		Atom newAtom = Atom.create(atomRepresentation);
		Atom alreadyKnownAtom = knownAtoms.get(newAtom);
		if (alreadyKnownAtom == null) {
			knownAtoms.put(newAtom, newAtom);
			return newAtom;
		} else {
			return alreadyKnownAtom;
		}
	}

	private static Molecule parseMoleculeString(Map<Atom,Atom> knownAtoms, String moleculeString) {
		List<Atom> atoms = new ArrayList<>();
		Matcher atomMatcher = PATTERN_ATOM.matcher(moleculeString);
		while (atomMatcher.lookingAt()) {
			atoms.add(createCanonicalizedAtom(knownAtoms,atomMatcher.group()));
			atomMatcher.region(atomMatcher.end(),atomMatcher.regionEnd());
		}
		if (!atomMatcher.hitEnd()) {
			throw new IllegalStateException("Could not parse full molecule");
		}
		return Molecule.create(atoms);
	}

	public static void main(String[] args) throws IOException {
		Map<Atom,Atom> knownAtoms = new HashMap<>();
		Map<Atom, Set<Molecule>> possibleReplacementMoleculesForAtoms = new HashMap<>();
		Iterator<String> fileLineIterator = Files.lines(InputFileUtils.getInputPath()).iterator();
		while (fileLineIterator.hasNext()) {
			String fileLine = fileLineIterator.next();
			if (fileLine.isEmpty()) {
				break;
			}
			Matcher replacementMatcher = PATTERN_ATOM_TO_MOLECULE_REPLACEMENT.matcher(fileLine);
			if (!replacementMatcher.matches()) {
				throw new IllegalStateException("Could not parse molecule replacement.");
			}
			possibleReplacementMoleculesForAtoms
				.computeIfAbsent(createCanonicalizedAtom(
					knownAtoms,replacementMatcher.group("fromAtom")),
					__ -> new HashSet<>()
				)
				.add(parseMoleculeString(knownAtoms,replacementMatcher.group("toMolecule")));
		}
		Atom initialAtom = createCanonicalizedAtom(knownAtoms, INITIAL_MOLECULE);
		if (!possibleReplacementMoleculesForAtoms.containsKey(initialAtom)) {
			throw new IllegalStateException("Initial molecule has no possible replacements");
		}
		if (!fileLineIterator.hasNext()) {
			throw new IllegalStateException("Missing the input molecule");
		}
		Molecule inputMolecule = parseMoleculeString(knownAtoms,fileLineIterator.next());
		if (fileLineIterator.hasNext()) {
			throw new IllegalStateException("Expected EOF, but saw more input");
		}
		System.out.format("Number of possible molecules that can result by doing a single replacement to the input molecule: %d%n", countSingleReplacementPossibilities(inputMolecule, possibleReplacementMoleculesForAtoms));
		System.out.format("Number of steps to get from %s to the input molecule: %d", initialAtom.getRepresentation(), calculateStepsRequired(initialAtom, inputMolecule, possibleReplacementMoleculesForAtoms));
	}

	private static int countSingleReplacementPossibilities(Molecule inputMolecule, Map<Atom, Set<Molecule>> possibleReplacementMoleculesForAtoms) {
		Set<Molecule> possibleNewMolecules = new HashSet<>();
		int inputMoleculeAtomCount = inputMolecule.getAtomCount();
		for (int inputMoleculeAtomIndex=0; inputMoleculeAtomIndex<inputMoleculeAtomCount; inputMoleculeAtomIndex++) {
			Atom inputMoleculeAtom = inputMolecule.getAtom(inputMoleculeAtomIndex);
			Set<Molecule> possibleReplacementMoleculesForAtom = possibleReplacementMoleculesForAtoms.get(inputMoleculeAtom);
			if (possibleReplacementMoleculesForAtom != null) {
				for (Molecule possibleReplacementMolecule : possibleReplacementMoleculesForAtom) {
					possibleNewMolecules.add(inputMolecule.replaceAtomWithAtoms(inputMoleculeAtomIndex,possibleReplacementMolecule.getAtomsView()));
				}
			}
		}
		return possibleNewMolecules.size();
	}

	private static final class ChomskyReducedFormRules<K,RK extends K,V> {
		private final Class<K> keyClass;
		private final Set<K> validStartRuleKeys;
		private final Collection<ChomskyReducedFormTerminalRule<K,V>> terminalRules;
		private final Collection<ChomskyReducedFormNonTerminalRule<K>> nonTerminalRules;

		public ChomskyReducedFormRules(Class<K> keyClass, Set<K> validStartRuleKeys, Collection<ChomskyReducedFormTerminalRule<K,V>> terminalRules, Collection<ChomskyReducedFormNonTerminalRule<K>> nonTerminalRules) {
			this.keyClass = keyClass;
			this.validStartRuleKeys = new HashSet<>(validStartRuleKeys);
			this.terminalRules = new ArrayList<>(terminalRules);
			this.nonTerminalRules = new ArrayList<>(nonTerminalRules);
		}

		public Class<K> getKeyClass() {
			return this.keyClass;
		}

		public Set<K> getValidStartRuleKeys() {
			return this.validStartRuleKeys;
		}

		public Collection<ChomskyReducedFormTerminalRule<K, V>> getTerminalRulesView() {
			return Collections.unmodifiableCollection(this.terminalRules);
		}

		public Collection<ChomskyReducedFormNonTerminalRule<K>> getNonTerminalRulesView() {
			return Collections.unmodifiableCollection(this.nonTerminalRules);
		}
	}

	private static final class ChomskyReducedFormTerminalRule<K,V> extends AbstractChomskyReducedFormRule<K> {
		private final V output;

		protected ChomskyReducedFormTerminalRule(K ruleKey, boolean temporary, V output) {
			super(ruleKey, temporary);
			this.output = output;
		}

		public V getOutput() {
			return this.output;
		}
	}

	private static final class ChomskyReducedFormNonTerminalRule<K> extends AbstractChomskyReducedFormRule<K> {
		private final K leftChildRuleKey;
		private final K rightChildRuleKey;

		protected ChomskyReducedFormNonTerminalRule(K ruleKey, boolean temporary, K leftChildRuleKey, K rightChildRuleKey) {
			super(ruleKey, temporary);
			this.leftChildRuleKey = leftChildRuleKey;
			this.rightChildRuleKey = rightChildRuleKey;
		}

		public K getLeftChildRuleKey() {
			return this.leftChildRuleKey;
		}

		public K getRightChildRuleKey() {
			return this.rightChildRuleKey;
		}
	}

	private static class AbstractChomskyReducedFormRule<K> implements ChomskyReducedFormRule<K> {
		private final K ruleKey;
		private final boolean temporary;

		protected AbstractChomskyReducedFormRule(K ruleKey, boolean temporary) {
			this.ruleKey = ruleKey;
			this.temporary = temporary;
		}

		@Override
		public K getRuleKey() {
			return this.ruleKey;
		}

		@Override
		public boolean isTemporary() {
			return this.temporary;
		}
	}

	private interface ChomskyReducedFormRule<K> {
		K getRuleKey();
		boolean isTemporary();
	}

	private static final class ContextFreeGrammarSymbol<T> {
		private final boolean terminal;
		private final T symbol;

		public ContextFreeGrammarSymbol(boolean terminal, T symbol) {
			this.terminal = terminal;
			this.symbol = symbol;
		}

		public boolean isTerminal() {
			return this.terminal;
		}

		public T getSymbol() {
			return this.symbol;
		}
	}

	private static final class ContextFreeGrammarSubRule<T> {
		private final List<ContextFreeGrammarSymbol<T>> symbols;

		private ContextFreeGrammarSubRule(List<ContextFreeGrammarSymbol<T>> symbols) {
			this.symbols = new ArrayList<>(symbols);
		}

		public List<ContextFreeGrammarSymbol<T>> getSymbols() {
			return Collections.unmodifiableList(this.symbols);
		}
	}

	private static final class ContextFreeGrammarRule<T> {
		private final T leftSide;
		private final Collection<ContextFreeGrammarSubRule<T>> subRules;

		public ContextFreeGrammarRule(T leftSide, Collection<ContextFreeGrammarSubRule<T>> subRules) {
			this.leftSide = leftSide;
			this.subRules = new ArrayList<>(subRules);
		}

		public T getLeftSide() {
			return this.leftSide;
		}

		public Collection<ContextFreeGrammarSubRule<T>> getSubRules() {
			return Collections.unmodifiableCollection(this.subRules);
		}
	}

	private static final class ChomskyReducedFormBuildingRuleSymbol {
		private final boolean terminal;
		private final int value;

		public ChomskyReducedFormBuildingRuleSymbol(boolean terminal, int value) {
			this.terminal = terminal;
			this.value = value;
		}

		public boolean isTerminal() {
			return terminal;
		}

		public int getValue() {
			return value;
		}
	}

	private static final class ChomskyReducedFormBuildingRule {
		private final int leftSide;
		private final List<ChomskyReducedFormBuildingRuleSymbol> rightSide;
		private final boolean temporary;

		public ChomskyReducedFormBuildingRule(int leftSide, List<ChomskyReducedFormBuildingRuleSymbol> rightSide, boolean temporary) {
			this.leftSide = leftSide;
			this.rightSide = new ArrayList<>(rightSide);
			this.temporary = temporary;
		}

		public int getLeftSide() {
			return this.leftSide;
		}

		public List<ChomskyReducedFormBuildingRuleSymbol> getRightSide() {
			return Collections.unmodifiableList(this.rightSide);
		}

		public boolean isTemporary() {
			return this.temporary;
		}
	}

	private static final class ChomskyReducedFormBuildingRuleNewRuleHelper {
		private final Set<Integer> reservedRuleNumbers;
		private final Set<Integer> generatedRuleNumbers = new HashSet<>();
		private final List<ChomskyReducedFormBuildingRule> rules = new ArrayList<>();
		private final Map<Boolean,Map<Integer,ChomskyReducedFormBuildingRuleSymbol>> symbolCache = new HashMap<>();
		private int nextRuleNumber = 0;

		public ChomskyReducedFormBuildingRuleNewRuleHelper(Set<Integer> reservedRuleNumbers) {
			this.reservedRuleNumbers = new HashSet<>(reservedRuleNumbers);
		}

		public static ChomskyReducedFormBuildingRuleNewRuleHelper createWithReservedRuleNumbersFromExistingRules(Collection<ChomskyReducedFormBuildingRule> existingRules) {
			return new ChomskyReducedFormBuildingRuleNewRuleHelper(
				existingRules
					.stream()
					.map(ChomskyReducedFormBuildingRule::getLeftSide)
					.collect(Collectors.toSet())
			);
		}

		public void addRule(ChomskyReducedFormBuildingRule rule) {
			int leftSide = rule.getLeftSide();
			if (generatedRuleNumbers.contains(leftSide)) {
				throw new IllegalStateException("Rule left side was already used for a generated rule.");
			}
			this.reservedRuleNumbers.add(leftSide);
			this.rules.add(rule);
		}

		public void addRuleWithNewRightSide(ChomskyReducedFormBuildingRule existingRule, List<ChomskyReducedFormBuildingRuleSymbol> newRightSide) {
			this.addRule(
				new ChomskyReducedFormBuildingRule(
					existingRule.getLeftSide(),
					newRightSide,
					existingRule.isTemporary()
				)
			);
		}

		public ChomskyReducedFormBuildingRuleSymbol createTemporaryRuleAndReturnReferencingSymbol(List<ChomskyReducedFormBuildingRuleSymbol> rightSide) {
			int newRuleNumber = this.nextRuleNumber;
			while (this.reservedRuleNumbers.contains(newRuleNumber)) {
				newRuleNumber++;
			}
			this.nextRuleNumber = newRuleNumber+1;
			this.generatedRuleNumbers.add(newRuleNumber);
			this.rules.add(
				new ChomskyReducedFormBuildingRule(
					newRuleNumber,
					rightSide,
					true
				)
			);
			return createChomskyReducedFormBuildingRuleSymbol(newRuleNumber,false,this.symbolCache);
		}

		public List<ChomskyReducedFormBuildingRule> getRulesCopy() {
			return new ArrayList<>(this.rules);
		}
	}

	private static <T,U> U createSymbol(T item, boolean terminal, Map<Boolean,Map<T,U>> symbolsCache, BiFunction<Boolean,T,U> symbolConstructor) {
		return symbolsCache
			.computeIfAbsent(terminal, k -> new HashMap<>())
			.computeIfAbsent(item, k -> symbolConstructor.apply(terminal,k));
	}

	private static <T> ContextFreeGrammarSymbol<T> createContextFreeGrammarSymbol(T item, boolean terminal, Map<Boolean,Map<T,ContextFreeGrammarSymbol<T>>> symbolsCache) {
		return createSymbol(item,terminal,symbolsCache,ContextFreeGrammarSymbol::new);
	}

	private static ChomskyReducedFormBuildingRuleSymbol createChomskyReducedFormBuildingRuleSymbol(int item, boolean terminal, Map<Boolean,Map<Integer,ChomskyReducedFormBuildingRuleSymbol>> symbolsCache) {
		return createSymbol(item,terminal,symbolsCache,ChomskyReducedFormBuildingRuleSymbol::new);
	}

	private static ContextFreeGrammarRule<Atom> createContextFreeGrammarRuleForAtomReplacements(Atom atom, Set<Molecule> possibleReplacementMoleculesForAtom, Set<Atom> atomsWithRules, Map<Boolean,Map<Atom,ContextFreeGrammarSymbol<Atom>>> symbolsCache) {
		List<ContextFreeGrammarSubRule<Atom>> subRules = new ArrayList<>();
		if (possibleReplacementMoleculesForAtom != null) {
			possibleReplacementMoleculesForAtom
				.stream()
				.map(
					molecule -> new ContextFreeGrammarSubRule<>(
						molecule
							.getAtomsView()
							.stream()
							.map(moleculeAtom -> createContextFreeGrammarSymbol(moleculeAtom,!atomsWithRules.contains(moleculeAtom),symbolsCache))
							.collect(Collectors.toList())
					)
				)
				.forEach(subRules::add);
		}
		subRules.add(
			new ContextFreeGrammarSubRule<>(
				Collections.singletonList(
					createContextFreeGrammarSymbol(atom,true, symbolsCache)
				)
			)
		);
		return new ContextFreeGrammarRule<>(atom,subRules);
	}

	private static Collection<ContextFreeGrammarRule<Atom>> transformReplacementsIntoContextFreeGrammarRules(Map<Atom, Set<Molecule>> possibleReplacementMoleculesForAtoms) {
		Set<Atom> atomsWithRules = possibleReplacementMoleculesForAtoms.keySet();
		Map<Boolean,Map<Atom,ContextFreeGrammarSymbol<Atom>>> symbolsCache = new HashMap<>();
		return
			possibleReplacementMoleculesForAtoms
				.entrySet()
				.stream()
				.map(
					entry ->
						createContextFreeGrammarRuleForAtomReplacements(entry.getKey(),entry.getValue(),atomsWithRules,symbolsCache))
			.collect(Collectors.toList());
	}

	private static <T> Collection<ChomskyReducedFormBuildingRule> createChomskyReducedFormBuildingRulesFromContextFreeGrammar(Collection<ContextFreeGrammarRule<T>> contextFreeGrammarRules, Map<Integer,T> indexToValueLookupSink) {
		Set<T> allValues =
			Stream.concat(
				contextFreeGrammarRules
					.stream()
					.map(ContextFreeGrammarRule::getLeftSide),
				contextFreeGrammarRules
					.stream()
					.map(ContextFreeGrammarRule::getSubRules)
					.flatMap(Collection::stream)
					.map(ContextFreeGrammarSubRule::getSymbols)
					.flatMap(List::stream)
					.filter(ContextFreeGrammarSymbol::isTerminal)
					.map(ContextFreeGrammarSymbol::getSymbol)
			).collect(Collectors.toSet());
		Map<T,Integer> valueToIndexLookup = new HashMap<>();
		int index = 0;
		for (T value : allValues) {
			indexToValueLookupSink.put(index,value);
			valueToIndexLookup.put(value,index);
			index++;
		}
		Map<Boolean,Map<Integer,ChomskyReducedFormBuildingRuleSymbol>> symbolsCache = new HashMap<>();
		return
			contextFreeGrammarRules
				.stream()
				.flatMap(
					rule -> {
						int leftSide = valueToIndexLookup.get(rule.getLeftSide());
						return
							rule
								.getSubRules()
								.stream()
								.map(
									subRule -> new ChomskyReducedFormBuildingRule(
										leftSide,
										subRule
											.getSymbols()
											.stream()
											.map(
												symbol ->
													createChomskyReducedFormBuildingRuleSymbol(
														valueToIndexLookup.get(symbol.getSymbol()),
														symbol.isTerminal(),
														symbolsCache
													)
											)
											.collect(Collectors.toList()),
											false
									)
								);
					}
				).collect(Collectors.toList());
	}

	private static Collection<ChomskyReducedFormBuildingRule> runChomskyReducedFormTransformationTerm(Collection<ChomskyReducedFormBuildingRule> rules) {
		ChomskyReducedFormBuildingRuleNewRuleHelper newRuleHelper = ChomskyReducedFormBuildingRuleNewRuleHelper.createWithReservedRuleNumbersFromExistingRules(rules);
		Map<Integer,ChomskyReducedFormBuildingRuleSymbol> newRuleReferencesForTerminalValues = new HashMap<>();
		for (ChomskyReducedFormBuildingRule rule : rules) {
			List<ChomskyReducedFormBuildingRuleSymbol> rightSide = rule.getRightSide();
			if (rightSide.size() > 1 && rightSide.stream().anyMatch(ChomskyReducedFormBuildingRuleSymbol::isTerminal)) {
				List<ChomskyReducedFormBuildingRuleSymbol> newRightSide = new ArrayList<>();
				for (ChomskyReducedFormBuildingRuleSymbol rightSideSymbol : rightSide) {
					ChomskyReducedFormBuildingRuleSymbol newRightSideSymbol;
					if (rightSideSymbol.isTerminal()) {
						newRightSideSymbol =
							newRuleReferencesForTerminalValues.computeIfAbsent(
								rightSideSymbol.getValue(),
								value ->
									newRuleHelper.createTemporaryRuleAndReturnReferencingSymbol(
										Collections.singletonList(
											rightSideSymbol
										)
									)
							);
					} else {
						newRightSideSymbol = rightSideSymbol;
					}
					newRightSide.add(newRightSideSymbol);
				}
				newRuleHelper.addRuleWithNewRightSide(rule,newRightSide);
			} else {
				newRuleHelper.addRule(rule);
			}
		}
		return newRuleHelper.getRulesCopy();
	}

	private static Collection<ChomskyReducedFormBuildingRule> runChomskyReducedFormTransformationBin(Collection<ChomskyReducedFormBuildingRule> rules) {
		ChomskyReducedFormBuildingRuleNewRuleHelper newRuleHelper = ChomskyReducedFormBuildingRuleNewRuleHelper.createWithReservedRuleNumbersFromExistingRules(rules);
		for (ChomskyReducedFormBuildingRule rule : rules) {
			List<ChomskyReducedFormBuildingRuleSymbol> rightSide = rule.getRightSide();
			long nonTerminalCount = rightSide.stream().filter(symbol -> !symbol.isTerminal()).count();
			if (nonTerminalCount > 2) {
				LinkedList<ChomskyReducedFormBuildingRuleSymbol> newRightSide = new LinkedList<>(rightSide);
				while (nonTerminalCount > 2) {
					LinkedList<ChomskyReducedFormBuildingRuleSymbol> temporaryRightSide = new LinkedList<>();
					int movedNonTerminalCount = 0;
					while (movedNonTerminalCount < 2) {
						ChomskyReducedFormBuildingRuleSymbol symbol = newRightSide.removeLast();
						temporaryRightSide.addFirst(symbol);
						if (!symbol.isTerminal()) {
							movedNonTerminalCount++;
						}
					}
					newRightSide.addLast(newRuleHelper.createTemporaryRuleAndReturnReferencingSymbol(temporaryRightSide));
					nonTerminalCount--; // We removed two, but added a new one back.
				}
				newRuleHelper.addRuleWithNewRightSide(rule,newRightSide);
			} else {
				newRuleHelper.addRule(rule);
			}
		}
		return newRuleHelper.getRulesCopy();
	}

	private static Collection<ChomskyReducedFormBuildingRule> runChomskyReducedFormTransformationUnit(Collection<ChomskyReducedFormBuildingRule> rules) {
		boolean madeChanges;
		do {
			madeChanges = false;
			Set<Integer> rulesToLeaveAloneThisIteration = new HashSet<>();
			ChomskyReducedFormBuildingRuleNewRuleHelper newRuleHelper = ChomskyReducedFormBuildingRuleNewRuleHelper.createWithReservedRuleNumbersFromExistingRules(rules);
			for (ChomskyReducedFormBuildingRule rule : rules) {
				int leftSide = rule.getLeftSide();
				boolean addExistingRule = true;
				if (!rulesToLeaveAloneThisIteration.contains(leftSide)) {
					List<ChomskyReducedFormBuildingRuleSymbol> rightSide = rule.getRightSide();
					if (rightSide.size() == 1) {
						ChomskyReducedFormBuildingRuleSymbol rightSideSymbol = rightSide.get(0);
						if (!rightSideSymbol.isTerminal()) {
							int rightSideValue = rightSideSymbol.getValue();
							if (!rulesToLeaveAloneThisIteration.contains(rightSideValue)) {
								addExistingRule = false;
								madeChanges = true;
								rulesToLeaveAloneThisIteration.add(leftSide);
								rulesToLeaveAloneThisIteration.add(rightSideValue);
								for (ChomskyReducedFormBuildingRule innerRule : rules) {
									if (innerRule.getLeftSide() == rightSideValue) {
										newRuleHelper.addRule(
											new ChomskyReducedFormBuildingRule(
												leftSide,
												innerRule.getRightSide(),
												rule.isTemporary()
											)
										);
									}
								}
							}
						}
					}
				}
				if (addExistingRule) {
					newRuleHelper.addRule(rule);
				}
			}
			rules = newRuleHelper.getRulesCopy();
		} while (madeChanges);
		return rules;
	}

	private static Collection<ChomskyReducedFormBuildingRule> runChomskyReducedFormTransformationRemoveTemporaryDuplicates(Collection<ChomskyReducedFormBuildingRule> rules) {
		//TODO
		return rules;
	}

	private static Collection<ChomskyReducedFormBuildingRule> runChomskyReducedFormTransformationRemoveUnreachable(Collection<ChomskyReducedFormBuildingRule> rules) {
		//TODO
		return rules;
	}

	// A class needed so that temporary keys don't match any T
	private static class TemporaryRuleKey {
		private final int number;

		public TemporaryRuleKey(int number) {
			this.number = number;
		}

		@Override
		public String toString() {
			return "TemporaryRuleKey{" + this.number + "}";
		}
	}

	private static <T> ChomskyReducedFormRules<Object,T,T> createChomskyReducedFormRules(Collection<ChomskyReducedFormBuildingRule> buildingRules, T startingRule, Map<Integer,T> intValueToRealValueLookup) {
		Function<Integer,T> intValueToRealValueMapper = intValue -> {
			T value = intValueToRealValueLookup.get(intValue);
			if (value == null) {
				throw new IllegalStateException("Could not reverse integer mapping back to real key");
			}
			else {
				return value;
			}
		};
		Function<ChomskyReducedFormBuildingRule,Object> ruleToKeyMapper;
		if (startingRule instanceof TemporaryRuleKey) {
			throw new IllegalStateException("T must not be " + TemporaryRuleKey.class.getName());
		} else {
			ruleToKeyMapper = rule -> {
				int ruleLeftSide = rule.getLeftSide();
				if (rule.isTemporary()) {
					return new TemporaryRuleKey(ruleLeftSide);
				} else {
					return intValueToRealValueMapper.apply(ruleLeftSide);
				}
			};
		}
		Map<Integer,Object> ruleLeftSideToKeyMap =
			buildingRules
				.stream()
				.collect(
					Collectors.toMap(
						ChomskyReducedFormBuildingRule::getLeftSide,
						ruleToKeyMapper,
						(value1,value2) -> {
							if (Objects.equals(value1,value2)) {
								return value1;
							} else {
								throw new IllegalStateException("Differing keys for what appears to be the same rule. Something went wrong.");
							}
						} ,
						IdentityHashMap::new
					)
				);
		List<ChomskyReducedFormTerminalRule<Object,T>> terminalRules = new ArrayList<>();
		List<ChomskyReducedFormNonTerminalRule<Object>> nonTerminalRules = new ArrayList<>();
		for (ChomskyReducedFormBuildingRule buildingRule : buildingRules) {
			Object ruleKey = ruleLeftSideToKeyMap.get(buildingRule.getLeftSide());
			boolean temporary = buildingRule.isTemporary();
			List<ChomskyReducedFormBuildingRuleSymbol> rightSide = buildingRule.getRightSide();
			if (rightSide.size() == 1 && rightSide.stream().allMatch(ChomskyReducedFormBuildingRuleSymbol::isTerminal)) {
				terminalRules.add(
					new ChomskyReducedFormTerminalRule<>(
						ruleKey,
						temporary,
						intValueToRealValueMapper.apply(rightSide.get(0).getValue())
					)
				);
			} else if (rightSide.size() == 2 && rightSide.stream().noneMatch(ChomskyReducedFormBuildingRuleSymbol::isTerminal)) {
				nonTerminalRules.add(
					new ChomskyReducedFormNonTerminalRule<>(
						ruleKey,
						temporary,
						ruleLeftSideToKeyMap.get(rightSide.get(0).getValue()),
						ruleLeftSideToKeyMap.get(rightSide.get(1).getValue())
					)
				);
			} else {
				throw new IllegalStateException("Building rule is not a valid chomsky reduced form rule");
			}
		}
		return new ChomskyReducedFormRules<>(Object.class,Collections.singleton(startingRule),terminalRules,nonTerminalRules);
	}

	private static <T> ChomskyReducedFormRules<Object,T,T> transformContextFreeGrammarIntoChomskyReducedForm(Collection<ContextFreeGrammarRule<T>> contextFreeGrammarRules, T startingRule) {
		if (contextFreeGrammarRules.stream().noneMatch(contextFreeGrammarRule -> Objects.equals(contextFreeGrammarRule.getLeftSide(),startingRule))) {
			throw new IllegalStateException("Starting rule does not exist in rules collection");
		}
		if (contextFreeGrammarRules.stream().map(ContextFreeGrammarRule::getSubRules).flatMap(Collection::stream).map(ContextFreeGrammarSubRule::getSymbols).anyMatch(List::isEmpty)) {
			throw new IllegalStateException("Chomsky reduced form does not support empty sets.");
		}
		Map<Integer,T> intValueToRealValueLookup = new HashMap<>();
		Collection<ChomskyReducedFormBuildingRule> buildingRules;
		buildingRules = createChomskyReducedFormBuildingRulesFromContextFreeGrammar(contextFreeGrammarRules,intValueToRealValueLookup);
		// Transformation START is not needed for reduced form
		buildingRules = runChomskyReducedFormTransformationTerm(buildingRules);
		buildingRules = runChomskyReducedFormTransformationBin(buildingRules);
		// Transformation DEL is not needed for reduced form
		buildingRules = runChomskyReducedFormTransformationUnit(buildingRules);
		buildingRules = runChomskyReducedFormTransformationRemoveTemporaryDuplicates(buildingRules);
		buildingRules = runChomskyReducedFormTransformationRemoveUnreachable(buildingRules);
		// Final step is to convert this into proper chomsky classes.
		return createChomskyReducedFormRules(buildingRules, startingRule, intValueToRealValueLookup);
	}

	private static class ParseTree<K> {
		//private final K rule;
		//private final Set<Set<ParseTree<K>>> childrenOfPossibilities;
	}

	private static <K,RK extends K,V> Set<ParseTree<RK>> runCYKAndProduceParseTrees(ChomskyReducedFormRules<K,RK,V> chomskyReducedFormRules, List<V> input) {
		final class MatchCoordinate {
			private final int i;
			private final int j;
			private final K matchedChildRuleKey;

			public MatchCoordinate(int i, int j, K matchedChildRuleKey) {
				this.matchedChildRuleKey = matchedChildRuleKey;
				this.i = i;
				this.j = j;
			}
		}

		final class MatchCoordinatePair {
			private final MatchCoordinate leftCoordinate;
			private final MatchCoordinate rightCoordinate;

			public MatchCoordinatePair(MatchCoordinate leftCoordinate, MatchCoordinate rightCoordinate) {
				this.leftCoordinate = leftCoordinate;
				this.rightCoordinate = rightCoordinate;
			}
		}

		final class RuleMatch {
			private final K matchedRuleKey;
			private final List<MatchCoordinatePair> matchCoordinatePairs = new ArrayList<>();

			public RuleMatch(K matchedRuleKey) {
				this.matchedRuleKey = matchedRuleKey;
			}

			public void addMatchCoordinatePair(MatchCoordinatePair matchCoordinatePair) {
				this.matchCoordinatePairs.add(matchCoordinatePair);
			}

			public K getMatchedRuleKey() {
				return this.matchedRuleKey;
			}
		}

		Collection<ChomskyReducedFormTerminalRule<K,V>> terminalRules = chomskyReducedFormRules.getTerminalRulesView();
		Collection<ChomskyReducedFormNonTerminalRule<K>> nonTerminalRules = chomskyReducedFormRules.getNonTerminalRulesView();
		Map<V,Set<K>> ruleKeysThatProduceValues =
			terminalRules
				.stream()
				.collect(
					Collectors.groupingBy(
						ChomskyReducedFormTerminalRule::getOutput,
						Collectors.mapping(
							ChomskyReducedFormRule::getRuleKey,
							Collectors.toSet()
						)
					)
				);
		Map<K,Map<K,Set<K>>> parentRuleKeysThatMatchChildKeys =
			nonTerminalRules
				.stream()
				.collect(
					Collectors.groupingBy(
						ChomskyReducedFormNonTerminalRule::getLeftChildRuleKey,
						Collectors.groupingBy(
							ChomskyReducedFormNonTerminalRule::getRightChildRuleKey,
							Collectors.mapping(
								ChomskyReducedFormNonTerminalRule::getRuleKey,
								Collectors.toSet()
							)
						)
					)
				);
		int inputSize = input.size();

		RuleMatch[][][] rulesMatched = new RuleMatch[inputSize][][];
		RuleMatch[] emptyRuleMatchArray = new RuleMatch[0];
		System.out.println("hi");
		rulesMatched[0] = new RuleMatch[inputSize][];
		for (int inputIndex=0; inputIndex<inputSize; inputIndex++) {
			Set<K> ruleKeysThatProduceInputValue = ruleKeysThatProduceValues.get(input.get(inputIndex));
			RuleMatch[] rulesMatchedForInputValue;
			if (ruleKeysThatProduceInputValue != null) {
				int matchCount = ruleKeysThatProduceInputValue.size();
				rulesMatchedForInputValue = new RuleMatch[matchCount];
				int matchIndex = 0;
				for (K ruleKey : ruleKeysThatProduceInputValue) {
					rulesMatchedForInputValue[matchIndex] = new RuleMatch(ruleKey);
				}
			} else {
				rulesMatchedForInputValue = emptyRuleMatchArray;
			}
			rulesMatched[0][inputIndex] = rulesMatchedForInputValue;
		}
		for (int targetI=1; targetI<inputSize; targetI++) {
			rulesMatched[targetI] = new RuleMatch[inputSize-targetI][];
			for (int targetJ=0; targetJ<inputSize-targetI; targetJ++) {
				Map<K,RuleMatch> ruleMatchesForNewCell = new HashMap<>();
				for (
					int
					check1I=targetI-1,
					check2I=0,
					check2J=targetI+targetJ;
					check1I>=0;
					check1I--,
					check2I++,
					check2J--
				) {
					RuleMatch[] leftRuleMatches = rulesMatched[check1I][targetJ];
					RuleMatch[] rightRuleMatches = rulesMatched[check2I][check2J];
					for (RuleMatch leftRuleMatch : leftRuleMatches) {
						K leftRuleMatchedKey = leftRuleMatch.getMatchedRuleKey();
						MatchCoordinate leftMatchCoordinate = new MatchCoordinate(check1I,targetJ,leftRuleMatchedKey);
						Map<K,Set<K>> parentRuleKeysThatMatchLeftRuleKey = parentRuleKeysThatMatchChildKeys.get(leftRuleMatchedKey);
						if (parentRuleKeysThatMatchLeftRuleKey != null) {
							for (RuleMatch rightRuleMatch : rightRuleMatches) {
								K rightRuleMatchedKey = rightRuleMatch.getMatchedRuleKey();
								MatchCoordinate rightMatchCoordinate = new MatchCoordinate(check2I,check2J,rightRuleMatchedKey);
								Set<K> ruleKeysThatMatchBothKeys = parentRuleKeysThatMatchLeftRuleKey.get(rightRuleMatchedKey);
								if (ruleKeysThatMatchBothKeys != null) {
									MatchCoordinatePair matchCoordinatePair = new MatchCoordinatePair(leftMatchCoordinate,rightMatchCoordinate);
									for (K ruleKey : ruleKeysThatMatchBothKeys) {
										ruleMatchesForNewCell.computeIfAbsent(ruleKey, RuleMatch::new).addMatchCoordinatePair(matchCoordinatePair);
									}
								}
							}
						}
					}
				}
				rulesMatched[targetI][targetJ] = ruleMatchesForNewCell.values().toArray(emptyRuleMatchArray);
			}
		}
		System.out.println("bye");
		return null;
	}

	private static int calculateStepsRequired(Atom initialAtom, Molecule inputMolecule, Map<Atom, Set<Molecule>> possibleReplacementMoleculesForAtoms) {
		Collection<ContextFreeGrammarRule<Atom>> contextFreeGrammarRules = transformReplacementsIntoContextFreeGrammarRules(possibleReplacementMoleculesForAtoms);
		ChomskyReducedFormRules<Object,Atom,Atom> chomskyReducedFormRules = transformContextFreeGrammarIntoChomskyReducedForm(contextFreeGrammarRules, initialAtom);
		Set<ParseTree<Atom>> possibleParseTrees = runCYKAndProduceParseTrees(chomskyReducedFormRules, inputMolecule.getAtomsView()); // The ParseTrees should flatten temporary nodes into their parents
		//recurseParseTreesToCountWaysAndMaximumSize;
		throw new IllegalStateException("Not yet finished being implemented.");
	}
}
