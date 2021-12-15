package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2021;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day14 implements IPuzzle {
    private static final Pattern PATTERN_REPLACEMENT = Pattern.compile("^(?<moleculeLeft>[A-Z])(?<moleculeRight>[A-Z]) -> (?<moleculeMiddle>[A-Z])$");

    private static final class Molecule {
        private final char character;

        private Molecule(char character) {
            this.character = character;
        }

        public char getCharacter() {
            return this.character;
        }

        @Override
        public boolean equals(Object otherObject) {
            if (otherObject == this) return true;
            if (!(otherObject instanceof Molecule)) return false;
            return this.character == ((Molecule)otherObject).character;
        }

        @Override
        public int hashCode() {
            return (int) this.character;
        }

        @Override
        public String toString() {
            return Character.toString(this.character);
        }
    }

    private static final class MoleculePair {
        private final Molecule leftMolecule;
        private final Molecule rightMolecule;
        private transient int hashCode = -1;

        public MoleculePair(Molecule leftMolecule, Molecule rightMolecule) {
            this.leftMolecule = leftMolecule;
            this.rightMolecule = rightMolecule;
        }

        public Molecule getLeftMolecule() {
            return this.leftMolecule;
        }

        public Molecule getRightMolecule() {
            return this.rightMolecule;
        }

        @Override
        public boolean equals(Object otherObject) {
            if (otherObject == this) return true;
            if (!(otherObject instanceof MoleculePair)) return false;

            MoleculePair otherMoleculePair = (MoleculePair)otherObject;

            if (this.hashCode() != otherMoleculePair.hashCode()) return false;
            return this.leftMolecule.equals(otherMoleculePair.leftMolecule) && this.rightMolecule.equals(otherMoleculePair.rightMolecule);
        }

        @Override
        public int hashCode() {
            if (hashCode == -1) {
                return (hashCode = 31 * this.leftMolecule.hashCode() + this.rightMolecule.hashCode());
            }
            return hashCode;
        }

        @Override
        public String toString() {
            return this.leftMolecule.toString() + this.rightMolecule.toString();
        }
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        String[] lines = LineReader.stringsArray(inputCharacters, true);
        int lineCount = lines.length;
        if (lineCount < 3) {
            throw new IllegalStateException("Too few lines");
        }
        if (!lines[1].isEmpty()) {
            throw new IllegalStateException("Expected an empty line on input line 2");
        }
        char[] polymerTemplateCharacters = lines[0].toCharArray();
        int polymerTemplateCharactersCount = polymerTemplateCharacters.length;
        Map<Character,Molecule> moleculeMap = new HashMap<>();
        Map<MoleculePair,Long> moleculePairCounts = new HashMap<>();
        Molecule leftMolecule = moleculeMap.computeIfAbsent(polymerTemplateCharacters[0],Molecule::new);
        for (int polymerTemplateCharacterIndex=1; polymerTemplateCharacterIndex<polymerTemplateCharactersCount; polymerTemplateCharacterIndex++) {
            Molecule rightMolecule = moleculeMap.computeIfAbsent(polymerTemplateCharacters[polymerTemplateCharacterIndex],Molecule::new);
            moleculePairCounts.merge(new MoleculePair(leftMolecule,rightMolecule),1L,Math::addExact);
            leftMolecule = rightMolecule;
        }
        Molecule lastMolecule = leftMolecule;
        Map<MoleculePair, List<MoleculePair>> replacements = new HashMap<>();
        for (int lineIndex=2; lineIndex<lineCount; lineIndex++) {
            Matcher matcher = PATTERN_REPLACEMENT.matcher(lines[lineIndex]);
            if (!matcher.matches()) {
                throw new IllegalStateException("Unparseable line");
            }
            Molecule moleculeLeft = moleculeMap.computeIfAbsent(matcher.group("moleculeLeft").charAt(0),Molecule::new);
            Molecule moleculeMiddle = moleculeMap.computeIfAbsent(matcher.group("moleculeMiddle").charAt(0),Molecule::new);
            Molecule moleculeRight = moleculeMap.computeIfAbsent(matcher.group("moleculeRight").charAt(0),Molecule::new);
            replacements.put(new MoleculePair(moleculeLeft,moleculeRight), Arrays.asList(new MoleculePair(moleculeLeft,moleculeMiddle),new MoleculePair(moleculeMiddle,moleculeRight)));
        }
        return new BasicPuzzleResults<>(
            run(moleculePairCounts,replacements,lastMolecule,10),
            run(moleculePairCounts,replacements,lastMolecule,40)
        );
    }

    private static long run(Map<MoleculePair, Long> moleculePairCounts, Map<MoleculePair, List<MoleculePair>> replacements, Molecule lastMolecule, int steps) {
        for (int step=1; step<=steps; step++) {
            Map<MoleculePair,Long> newMoleculePairCounts = new HashMap<>();
            for (Map.Entry<MoleculePair,Long> moleculePairCountsEntry : moleculePairCounts.entrySet()) {
                long count = moleculePairCountsEntry.getValue();
                for (MoleculePair replacement : replacements.get(moleculePairCountsEntry.getKey())) {
                    newMoleculePairCounts.merge(replacement,count,Math::addExact);
                }
            }
            moleculePairCounts = newMoleculePairCounts;
        }
        Map<Molecule,Long> moleculeCounts = new HashMap<>();
        moleculeCounts.put(lastMolecule,1L);
        for (Map.Entry<MoleculePair,Long> moleculePairCountsEntry : moleculePairCounts.entrySet()) {
            moleculeCounts.merge(moleculePairCountsEntry.getKey().getLeftMolecule(),moleculePairCountsEntry.getValue(),Math::addExact);
        }
        long minimumMoleculeCount = Long.MAX_VALUE;
        long maximumMoleculeCount = Long.MIN_VALUE;
        for (Long moleculeCount : moleculeCounts.values()) {
            minimumMoleculeCount = Math.min(minimumMoleculeCount,moleculeCount);
            maximumMoleculeCount = Math.max(maximumMoleculeCount,moleculeCount);
        }
        return maximumMoleculeCount-minimumMoleculeCount;
    }
}
