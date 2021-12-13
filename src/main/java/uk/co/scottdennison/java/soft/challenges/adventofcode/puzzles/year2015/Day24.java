package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2015;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class Day24 implements IPuzzle {
    private static class Group {
        private final boolean[] usedWeightsReference;
        private final long quantumEntanglement;

        private Group(boolean[] usedWeightsReference, long quantumEntanglement) {
            this.usedWeightsReference = usedWeightsReference;
            this.quantumEntanglement = quantumEntanglement;
        }

        public boolean[] getUsedWeightsReference() {
            return this.usedWeightsReference;
        }

        public long getQuantumEntanglement() {
            return this.quantumEntanglement;
        }
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        int[] weights = LineReader.stringsStream(inputCharacters).map(Integer::parseInt).sorted(Collections.reverseOrder()).mapToInt(Integer::intValue).toArray();
        return new BasicPuzzleResults<>(
            run(weights, 3, printWriter),
            run(weights, 4, printWriter)
        );
    }

    private long run(int[] weights, int requiredGroups, PrintWriter printWriter) {
        if (requiredGroups < 2) {
            throw new IllegalStateException("There must be at least 2 groups.");
        }
        printWriter.println("Running to find " + requiredGroups + " required groups");
        int weightCount = weights.length;
        int totalWeight = Arrays.stream(weights).sum();
        int targetWeight = totalWeight/requiredGroups;
        if ((targetWeight*requiredGroups) != totalWeight) {
            throw new IllegalStateException("Unable to balance groups");
        }
        printWriter.println("Calculating all possible ways to create first group");
        SortedMap<Integer, List<boolean[]>> ways = findAllPrimeSumsForTarget(weights, targetWeight);
        for (Map.Entry<Integer,List<boolean[]>> waysEntry : ways.entrySet()) {
            boolean[][] waysWithLength = waysEntry.getValue().toArray(new boolean[0][]);
            int waysWithLengthCount = waysWithLength.length;
            printWriter.println("Checking the " + waysWithLengthCount + (waysWithLengthCount==1?" entry that has":" entries that have") + " a length of " + waysEntry.getKey());
            Group[] groups = new Group[waysWithLengthCount];
            for (int wayIndex=0; wayIndex<waysWithLengthCount; wayIndex++) {
                boolean[] way = waysWithLength[wayIndex];
                long quantumEntanglement = 1;
                for (int weightIndex=0; weightIndex<weightCount; weightIndex++) {
                    if (way[weightIndex]) {
                        quantumEntanglement = Math.multiplyExact(quantumEntanglement,weights[weightIndex]);
                    }
                }
                groups[wayIndex] = new Group(way, quantumEntanglement);
            }
            Arrays.sort(groups, Comparator.comparing(Group::getQuantumEntanglement));
            for (Group group : groups) {
                if (canMakePrimeSumGroups(weights, targetWeight, group.getUsedWeightsReference(), requiredGroups-1)) {
                    return group.getQuantumEntanglement();
                }
            }
        }
        throw new IllegalStateException("Unable to solve problem");
    }

    private static SortedMap<Integer,List<boolean[]>> findAllPrimeSumsForTarget(int[] weights, int targetWeight) {
        int weightCount = weights.length;
        int[] maxSums = new int[weightCount+1];
        int sum = 0;
        for (int index=weightCount-1; index>=0; index--) {
            maxSums[index] = (sum += weights[index]);
        }
        SortedMap<Integer,List<boolean[]>> ways = new TreeMap<>();
        findAllPrimeSumsForTarget(weights, maxSums, ways, new boolean[weightCount], 0, targetWeight, 0);
        return ways;
    }

    private static void findAllPrimeSumsForTarget(int[] weights, int[] maxSums, SortedMap<Integer,List<boolean[]>> ways, boolean[] currentUsedWeights, int currentUsedWeightCount, int remainingWeight, int weightIndex) {
        if (remainingWeight < 0) {
            // Not possible.
        }
        else if (remainingWeight == 0) {
            ways.computeIfAbsent(currentUsedWeightCount,__->new ArrayList<>()).add(Arrays.copyOf(currentUsedWeights,currentUsedWeights.length));
        }
        else if (remainingWeight > maxSums[weightIndex]) {
            // Not possible.
        }
        else {
            int nextWeightIndex = weightIndex+1;
            findAllPrimeSumsForTarget(weights, maxSums, ways, currentUsedWeights, currentUsedWeightCount, remainingWeight, nextWeightIndex);
            currentUsedWeights[weightIndex] = true;
            findAllPrimeSumsForTarget(weights, maxSums, ways, currentUsedWeights, currentUsedWeightCount + 1, remainingWeight - weights[weightIndex], nextWeightIndex);
            currentUsedWeights[weightIndex] = false;
        }
    }

    private static boolean canMakePrimeSumGroups(int[] weights, int targetWeight, boolean[] usedWeights, int groupsRequired) {
        if (groupsRequired < 1) {
            throw new IllegalStateException("Required to make at least one group");
        }
        return canMakePrimeSumGroups(weights, targetWeight, usedWeights, groupsRequired, 0, targetWeight);
    }

    private static boolean canMakePrimeSumGroups(int[] weights, int targetWeight, boolean[] usedWeights, int groupsRequired, int startWeightIndex, int remainingWeight) {
        int weightCount = weights.length;
        if (groupsRequired == 1) {
            for (int weightIndex=0; weightIndex<weightCount; weightIndex++) {
                if (!usedWeights[weightIndex]) {
                    remainingWeight -= weights[weightIndex];
                    if (remainingWeight < 0) {
                        break;
                    }
                }
            }
            return remainingWeight == 0;
        } else {
            for (int weightIndex=startWeightIndex; weightIndex<weightCount; weightIndex++) {
                if (!usedWeights[weightIndex]) {
                    int newRemainingWeight = remainingWeight-weights[weightIndex];
                    if (newRemainingWeight < 0) {
                        // We have gone over the target weight, this is not a valid path.
                    } else {
                        usedWeights[weightIndex] = true;
                        if (newRemainingWeight > 0) {
                            // We still need more weight
                            if (canMakePrimeSumGroups(weights, targetWeight, usedWeights, groupsRequired, weightIndex+1, newRemainingWeight)) {
                                return true;
                            }
                        } else {
                            // We've completed this group.
                            if (canMakePrimeSumGroups(weights, targetWeight, usedWeights, groupsRequired-1, 0, targetWeight)) {
                                return true;
                            }
                        }
                        usedWeights[weightIndex] = false;
                    }
                }
            }
            return false;
        }
    }
}
