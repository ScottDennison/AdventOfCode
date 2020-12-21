package uk.co.scottdennison.java.challenges.adventofcode.puzzles.year2015;

import uk.co.scottdennison.java.challenges.adventofcode.utils.InputFileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
		System.out.format("Number of possible molecules that can result by doing a single replacement to the input molecule: %d", countSingleReplacementsPossibilities(inputMolecule, possibleReplacementMoleculesForAtoms));
		System.out.format("Number of steps to get from %s to the input molecule: %d", initialAtom.getRepresentation(), calculateStepsRequired(initialAtom, inputMolecule, possibleReplacementMoleculesForAtoms));
	}

	private static int countSingleReplacementsPossibilities(Molecule inputMolecule, Map<Atom, Set<Molecule>> possibleReplacementMoleculesForAtoms) {
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

	private static final class ChomksyReducedFormRules<T> {
		private final int nonTerminalStartRuleIndex;
		private final List<ChomksyReducedFormTerminalRule<T>> terminalRules;
		private final List<ChomksyReducedFormNonTerminalRule<T>> nonTerminalRules;

		public ChomksyReducedFormRules(int nonTerminalStartRuleIndex, List<ChomksyReducedFormTerminalRule<T>> terminalRules, List<ChomksyReducedFormNonTerminalRule<T>> nonTerminalRules) {
			this.nonTerminalStartRuleIndex = nonTerminalStartRuleIndex;
			this.terminalRules = terminalRules;
			this.nonTerminalRules = nonTerminalRules;
		}
	}

	private static final class ChomksyReducedFormTerminalRule<T> extends AbstractChomksyReducedFormRule<T> {
		private final T output;

		protected ChomksyReducedFormTerminalRule(boolean temporary, T output) {
			super(temporary);
			this.output = output;
		}
	}

	private static final class ChomksyReducedFormSubRule<T> {
		private final ChomksyReducedFormRule<T> leftRule;
		private final ChomksyReducedFormRule<T> rightRule;

		public ChomksyReducedFormSubRule(ChomksyReducedFormRule<T> leftRule, ChomksyReducedFormRule<T> rightRule) {
			this.leftRule = leftRule;
			this.rightRule = rightRule;
		}
	}

	private static final class ChomksyReducedFormNonTerminalRule<T> extends AbstractChomksyReducedFormRule<T> {
		private List<ChomksyReducedFormSubRule<T>> subRules;

		protected ChomksyReducedFormNonTerminalRule(boolean temporary, List<ChomksyReducedFormSubRule<T>> subRules) {
			super(temporary);
			this.subRules = new ArrayList<>(subRules);
		}
	}

	private static class AbstractChomksyReducedFormRule<T> implements ChomksyReducedFormRule<T> {
		private final boolean temporary;

		protected AbstractChomksyReducedFormRule(boolean temporary) {
			this.temporary = temporary;
		}

		@Override
		public boolean isTemporary() {
			return this.temporary;
		}
	}

	private interface ChomksyReducedFormRule<T> {
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
	}

	private static class ContextFreeGrammarRule<T> {
		private final T leftSide;
		private final List<ContextFreeGrammarSubRule<T>> subRules;

		public ContextFreeGrammarRule(T leftSide, List<ContextFreeGrammarSubRule<T>> subRules) {
			this.leftSide = leftSide;
			this.subRules = subRules;
		}
	}

	private static ContextFreeGrammarRule<Atom> createContextFreeGrammarRuleForAtom(Atom atom, Map<Atom,ContextFreeGrammarSymbol<Atom>> nonTerminalSymbols, Set<Molecule> possibleReplacementMoleculesForAtom) {
		List<ContextFreeGrammarSubRule<Atom>> subRules = new ArrayList<>();
		if (possibleReplacementMoleculesForAtom != null) {
			possibleReplacementMoleculesForAtom
				.stream()
				.map(
					molecule -> new ContextFreeGrammarSubRule<>(
						molecule
							.getAtomsView()
							.stream()
							.map(nonTerminalSymbols::get)
							.collect(Collectors.toList())
					)
				)
				.forEach(subRules::add);
		}
		subRules.add(
			new ContextFreeGrammarSubRule<>(
				Collections.singletonList(
					new ContextFreeGrammarSymbol<>(
						true,
						atom
					)
				)
			)
		);
		return new ContextFreeGrammarRule<>(atom,subRules);
	}

	private static List<ContextFreeGrammarRule<Atom>> transformReplacementsIntoContextFreeGrammarRules(Map<Atom, Set<Molecule>> possibleReplacementMoleculesForAtoms) {
		Set<Atom> allPossibleAtoms =
			Stream.concat(
				possibleReplacementMoleculesForAtoms
					.keySet()
					.stream(),
				possibleReplacementMoleculesForAtoms
					.values()
					.stream()
					.flatMap(Set::stream)
					.map(Molecule::getAtomsView)
					.flatMap(List::stream)
			)
			.collect(Collectors.toSet());
		Map<Atom,ContextFreeGrammarSymbol<Atom>> nonTerminalSymbols =
			allPossibleAtoms
				.stream()
				.collect(
					Collectors.toMap(
						Function.identity(),
						atom -> new ContextFreeGrammarSymbol<>(false,atom)
					)
				);
		return
			allPossibleAtoms
				.stream()
				.map(atom -> createContextFreeGrammarRuleForAtom(atom, nonTerminalSymbols, possibleReplacementMoleculesForAtoms.get(atom)))
			.collect(Collectors.toList());
	}

	private static int calculateStepsRequired(Atom initialAtom, Molecule inputMolecule, Map<Atom, Set<Molecule>> possibleReplacementMoleculesForAtoms) {
		//ChomksyReducedFormRules<Atom> chomksyReducedFormRules = transformReplacementsIntoChomskyReducedForm(possibleReplacementMoleculesForAtoms);
		List<ContextFreeGrammarRule<Atom>> contextFreeGrammarRules = transformReplacementsIntoContextFreeGrammarRules(possibleReplacementMoleculesForAtoms);
		throw new IllegalStateException("Not yet finished being implemented.");
	}
}
