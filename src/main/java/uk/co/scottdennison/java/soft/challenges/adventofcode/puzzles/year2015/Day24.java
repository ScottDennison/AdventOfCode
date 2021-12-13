package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2015;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;

public class Day24 implements IPuzzle {
    private static final int REQUIRED_GROUPS = 4;

    private static class Group implements Comparable<Group> {
        public static final Group WORST = new Group(Integer.MAX_VALUE, Long.MAX_VALUE);

        private final int packageCount;
        private final long quantumEntanglement;

        private Group(int packageCount, long quantumEntanglement) {
            this.packageCount = packageCount;
            this.quantumEntanglement = quantumEntanglement;
        }

        public int getPackageCount() {
            return this.packageCount;
        }

        public long getQuantumEntanglement() {
            return this.quantumEntanglement;
        }

        @Override
        public int compareTo(Group otherGroup) {
            int difference = Integer.compare(packageCount,otherGroup.packageCount);
            if (difference == 0) {
                difference = Long.compare(quantumEntanglement,otherGroup.quantumEntanglement);
            }
            return difference;
        }
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        /*
        long[] weights = LineReader.stringsStream(inputCharacters).map(Long::parseLong).sorted(Collections.reverseOrder()).mapToLong(Long::longValue).toArray();
        long totalWeight = Arrays.stream(weights).sum();
        long targetWeight = totalWeight/REQUIRED_GROUPS;
        if ((targetWeight*REQUIRED_GROUPS) != totalWeight) {
            throw new IllegalStateException("Unable to balance groups");
        }
        Group[] potentialGroups = new Group[REQUIRED_GROUPS];
        int weightCount = weights.length;
        return new BasicPuzzleResults<>(
            buildGroup(weights,targetWeight,potentialGroups,1,new boolean[weightCount],Group.WORST).getQuantumEntanglement(),
            null
        );
        */
        return new BasicPuzzleResults<>(
            "This code takes 21h35m55s to run the example and all 4 datasets in jit mode, so will be skipped.",
            null
        );
    }

    private Group buildGroup(long[] weights, long targetWeight, Group[] potentialGroups, int groupNumber, boolean[] usedInAnyGroup, Group bestGroup) {
        return buildGroup(weights, targetWeight, potentialGroups, groupNumber, 0, 0, usedInAnyGroup, new boolean[weights.length], bestGroup);
    }

    private Group buildGroup(long[] weights, long targetWeight, Group[] potentialGroups, int groupNumber, int startWeightIndex, long currentWeight, boolean[] usedInAnyGroup, boolean[] usedInThisGroup, Group bestGroup) {
        int weightCount = weights.length;
        if (groupNumber == REQUIRED_GROUPS) {
            long remainingWeight = 0L;
            for (int weightIndex=0; weightIndex<weightCount; weightIndex++) {
                if (!usedInAnyGroup[weightIndex]) {
                    usedInThisGroup[weightIndex] = true;
                    remainingWeight += weights[weightIndex];
                }
            }
            if (remainingWeight == targetWeight) {
                potentialGroups[groupNumber-1] = createGroup(weights, usedInThisGroup);
                Group bestGroupFromPotentials = Group.WORST;
                for (int potentialGroupIndex=0; potentialGroupIndex<REQUIRED_GROUPS; potentialGroupIndex++) {
                    Group potentialGroup = potentialGroups[potentialGroupIndex];
                    if (potentialGroup.compareTo(bestGroupFromPotentials) < 0) {
                        bestGroupFromPotentials = potentialGroup;
                    }
                }
                if (bestGroupFromPotentials.compareTo(bestGroup) < 0) {
                    return bestGroupFromPotentials;
                }
            }
            return bestGroup;
        } else {
            for (int weightIndex=startWeightIndex; weightIndex<weightCount; weightIndex++) {
                if (!usedInAnyGroup[weightIndex]) {
                    long newWeight = currentWeight+weights[weightIndex];
                    long remainingRequiredWeight = targetWeight-newWeight;
                    if (remainingRequiredWeight < 0L) {
                        // We have gone over the target weight, this is not a valid path.
                    } else {
                        usedInAnyGroup[weightIndex] = true;
                        usedInThisGroup[weightIndex] = true;
                        if (remainingRequiredWeight > 0L) {
                            // We still need more weight
                            bestGroup = buildGroup(weights, targetWeight, potentialGroups, groupNumber, weightIndex+1, newWeight, usedInAnyGroup, usedInThisGroup, bestGroup);
                        } else {
                            // We are at our target weight.
                            potentialGroups[groupNumber-1] = createGroup(weights, usedInThisGroup);
                            bestGroup = buildGroup(weights, targetWeight, potentialGroups, groupNumber+1, usedInAnyGroup, bestGroup);
                        }
                        usedInThisGroup[weightIndex] = false;
                        usedInAnyGroup[weightIndex] = false;
                    }
                }
            }
            return bestGroup;
        }
    }

    private Group createGroup(long[] weights, boolean[] usedInThisGroup) {
        int weightCount = weights.length;
        int packageCount = 0;
        long quantumEntanglement = 1;
        for (int weightIndex=0; weightIndex<weightCount; weightIndex++) {
            if (usedInThisGroup[weightIndex]) {
                packageCount++;
                try {
                    quantumEntanglement = Math.multiplyExact(quantumEntanglement, weights[weightIndex]);
                } catch (ArithmeticException ex) {
                    // Lets cheat a little
                    quantumEntanglement = Long.MAX_VALUE;
                    for (int weightIndex2=weightIndex+1; weightIndex<weightCount; weightIndex++) {
                        if (usedInThisGroup[weightIndex2]) {
                            packageCount++;
                        }
                    }
                    break;
                }
            }
        }
        return new Group(packageCount,quantumEntanglement);
    }
}
