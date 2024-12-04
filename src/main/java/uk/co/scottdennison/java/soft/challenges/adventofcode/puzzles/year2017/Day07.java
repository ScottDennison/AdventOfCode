package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2017;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
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

public class Day07 implements IPuzzle {
    private static class Tower {
        private final String name;
        private final int ownWeight;

        private Tower parentTower = null;
        private Set<Tower> childTowers = new HashSet<>();

        private Integer combinedWeight = null;
        private Boolean balanced = null;

        public Tower(String name, int ownWeight) {
            this.name = name;
            this.ownWeight = ownWeight;
        }

        public String getName() {
            return name;
        }

        public int getOwnWeight() {
            return ownWeight;
        }

        public Tower getParentTower() {
            return this.parentTower;
        }

        public void setParentTower(Tower parentTower) {
            this.parentTower = parentTower;
        }

        public void addChildTower(Tower childTower) {
            if (this.childTowers.add(childTower)) {
                this.combinedWeight = null;
                this.balanced = null;
            }
        }

        public Iterator<Tower> iterateChildTowers() {
            return Collections.unmodifiableSet(this.childTowers).iterator();
        }

        public int getCombinedWeight() {
            if (this.combinedWeight == null) {
                this.combinedWeight = this.ownWeight + this.childTowers.stream().mapToInt(Tower::getCombinedWeight).sum();
            }
            return this.combinedWeight;
        }

        public boolean isBalanced() {
            if (this.balanced == null) {
                this.balanced = true;
                if (!childTowers.isEmpty()) {
                    Iterator<Tower> childTowersIterator = childTowers.iterator();
                    int firstChildCombinedWeight = childTowersIterator.next().getCombinedWeight();
                    while (childTowersIterator.hasNext()) {
                        if (childTowersIterator.next().getCombinedWeight() != firstChildCombinedWeight) {
                            this.balanced = false;
                            break;
                        }
                    }
                }
            }
            return this.balanced;
        }
    }

    private static final Pattern PATTERN = Pattern.compile("^(?<towerName>[a-z]+) \\((?<towerWeight>[0-9]+)\\)(?: -> (?<childTowerNames>[a-z]+(?:, [a-z]+)+))?$");

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        Map<String, Tower> towers = new HashMap<>();
        Map<String, Set<String>> pendingRelationshipAssignments = new HashMap<>();
        for (String inputLine : LineReader.strings(inputCharacters)) {
            Matcher matcher = PATTERN.matcher(inputLine);
            if (!matcher.matches()) {
                throw new IllegalStateException("Unable to parse line");
            }
            String towerName = matcher.group("towerName");
            if (towers.put(towerName, new Tower(towerName, Integer.parseInt(matcher.group("towerWeight")))) != null) {
                throw new IllegalStateException("Duplicate tower name: " + towerName);
            }
            String childTowerNamesString = matcher.group("childTowerNames");
            if (childTowerNamesString != null) {
                pendingRelationshipAssignments.put(towerName, Arrays.stream(childTowerNamesString.split(",")).map(String::trim).collect(Collectors.toCollection(HashSet::new)));
            }
        }
        for (Map.Entry<String, Set<String>> pendingRelationshipAssignmentsEntry : pendingRelationshipAssignments.entrySet()) {
            Tower parentTower = towers.get(pendingRelationshipAssignmentsEntry.getKey());
            for (String childTowerName : pendingRelationshipAssignmentsEntry.getValue()) {
                Tower childTower = towers.get(childTowerName);
                if (childTower == null) {
                    throw new IllegalStateException("No such tower: " + childTowerName);
                }
                if (childTower.getParentTower() != null) {
                    throw new IllegalStateException("Assigning parent of tower " + childTowerName + " which already has a parent.");
                }
                childTower.setParentTower(parentTower);
                parentTower.addChildTower(childTower);
            }
        }
        Set<String> parentlessTowerNames = towers.values().stream().filter(tower -> tower.getParentTower() == null).map(Tower::getName).collect(Collectors.toSet());
        if (parentlessTowerNames.size() != 1) {
            throw new IllegalStateException("Expeced only one parentless tower");
        }
        Integer adjustedTowerWeight = null;
        for (Tower tower : towers.values()) {
            if (!tower.isBalanced()) {
                Iterator<Tower> childTowersIterator = tower.iterateChildTowers();
                boolean isRootCause = true;
                while (childTowersIterator.hasNext()) {
                    Tower childTower = childTowersIterator.next();
                    if (!childTower.isBalanced()) {
                        isRootCause = false;
                    }
                }
                if (isRootCause) {
                    if (adjustedTowerWeight != null) {
                        throw new IllegalStateException("Already have an adjusted tower weight, two twoers can't be the root cause.");
                    }
                    childTowersIterator = tower.iterateChildTowers();
                    Map<Integer,Set<Tower>> childTowerCombinedWeights = new HashMap<>();
                    while (childTowersIterator.hasNext()) {
                        Tower childTower = childTowersIterator.next();
                        childTowerCombinedWeights.computeIfAbsent(childTower.getCombinedWeight(), __ -> new HashSet<>()).add(childTower);
                    }
                    if (childTowerCombinedWeights.size() != 2) {
                        throw new IllegalStateException("Expected two sets of towers when split by combined weight");
                    }
                    Iterator<Map.Entry<Integer,Set<Tower>>> childTowerCombinedWeightsIterator = childTowerCombinedWeights.entrySet().iterator();
                    Map.Entry<Integer,Set<Tower>> childTowerCombinedWeightsEntry1 = childTowerCombinedWeightsIterator.next();
                    Map.Entry<Integer,Set<Tower>> childTowerCombinedWeightsEntry2 = childTowerCombinedWeightsIterator.next();
                    Map.Entry<Integer,Set<Tower>> childTowerCombinedWeightsEntrySingle;
                    Map.Entry<Integer,Set<Tower>> childTowerCombinedWeightsEntryMultiple;
                    if (childTowerCombinedWeightsEntry1.getValue().size() == 1) {
                        childTowerCombinedWeightsEntrySingle = childTowerCombinedWeightsEntry1;
                        childTowerCombinedWeightsEntryMultiple = childTowerCombinedWeightsEntry2;
                        if (childTowerCombinedWeightsEntryMultiple.getValue().size() == 1) {
                            throw new IllegalStateException("Expected one tower to be different from multiple others.");
                        }
                    }
                    else if (childTowerCombinedWeightsEntry2.getValue().size() == 1) {
                        childTowerCombinedWeightsEntrySingle = childTowerCombinedWeightsEntry2;
                        childTowerCombinedWeightsEntryMultiple = childTowerCombinedWeightsEntry1;
                    }
                    else {
                        throw new IllegalStateException("Expected only one child tower to differ");
                    }
                    adjustedTowerWeight = childTowerCombinedWeightsEntrySingle.getValue().iterator().next().getOwnWeight() + (childTowerCombinedWeightsEntryMultiple.getKey() - childTowerCombinedWeightsEntrySingle.getKey());
                }
            }
        }
        if (adjustedTowerWeight == null) {
            throw new IllegalStateException("Could not identify tower that needs adjustment");
        }
        return new BasicPuzzleResults<>(
            parentlessTowerNames.iterator().next(),
            adjustedTowerWeight
        );
    }
}
