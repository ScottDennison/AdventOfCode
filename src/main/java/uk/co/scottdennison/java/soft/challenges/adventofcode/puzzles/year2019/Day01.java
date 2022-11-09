package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2019;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;

public class Day01 implements IPuzzle {
    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        int[] moduleMasses = LineReader.stringsStream(inputCharacters).mapToInt(Integer::parseInt).toArray();
        return new BasicPuzzleResults<>(
            calculateFuel(moduleMasses, true),
            calculateFuel(moduleMasses, false)
        );
    }

    private int calculateFuel(int[] moduleMasses, boolean dontRecurse) {
        int fuelNeeded = 0;
        for (int moduleMass : moduleMasses) {
            int pendingMass = moduleMass;
            while (true) {
                int additionalFuelNeeded = (pendingMass / 3) - 2;
                if (additionalFuelNeeded < 0) {
                    additionalFuelNeeded = 0;
                }
                fuelNeeded += additionalFuelNeeded;
                if (dontRecurse || additionalFuelNeeded < 1) {
                    break;
                }
                pendingMass = additionalFuelNeeded;
            }
        }
        return fuelNeeded;
    }
}
