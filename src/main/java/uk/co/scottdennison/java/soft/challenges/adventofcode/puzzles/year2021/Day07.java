package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2021;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Arrays;

public class Day07 implements IPuzzle {
    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        int[] crabPositions = Arrays.stream(new String(inputCharacters).trim().split(",")).mapToInt(Integer::parseInt).toArray();
        int minimumCrabPosition = Integer.MAX_VALUE;
        int maximumCrabPosition = Integer.MIN_VALUE;
        for (int crabPosition : crabPositions) {
            if (crabPosition < minimumCrabPosition) {
                minimumCrabPosition = crabPosition;
            }
            if (crabPosition > maximumCrabPosition) {
                maximumCrabPosition = crabPosition;
            }
        }
        int partAMinimumFuelNeeded = Integer.MAX_VALUE;
        int partBMinimumFuelNeeded = Integer.MAX_VALUE;
        for (int targetPosition=minimumCrabPosition; targetPosition<=maximumCrabPosition; targetPosition++) {
            int partAFuelNeeded = 0;
            int partBFuelNeeded = 0;
            for (int crabPosition : crabPositions) {
                int distanceNeeded = Math.abs(targetPosition-crabPosition);
                partAFuelNeeded += distanceNeeded;
                partBFuelNeeded += ((distanceNeeded+1)*distanceNeeded)/2;
            }
            if (partAFuelNeeded < partAMinimumFuelNeeded) {
                partAMinimumFuelNeeded = partAFuelNeeded;
            }
            if (partBFuelNeeded < partBMinimumFuelNeeded) {
                partBMinimumFuelNeeded = partBFuelNeeded;
            }
        }
        return new BasicPuzzleResults<>(
            partAMinimumFuelNeeded,
            partBMinimumFuelNeeded
        );
    }
}
