package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2018;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

public class Day01 implements IPuzzle {
    private static final int CHECK_LIMIT_FOR_POTENTIALLY_UNSOLVABLE_PART_B = 200000;

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        int[] inputFrequencyChanges = LineReader.stringsStream(inputCharacters).mapToInt(Integer::parseInt).toArray();
        int inputFrequencyChangeCount = inputFrequencyChanges.length;
        int frequency = 0;
        for (int inputFrequencyChange : inputFrequencyChanges) {
            frequency += inputFrequencyChange;
        }
        Integer part1FrequencyResult = frequency;
        int index = 0;
        Integer part2FrequencyResult;
        frequency = 0;
        int checksMade = 1;
        int checkLimit = partBPotentiallyUnsolvable?CHECK_LIMIT_FOR_POTENTIALLY_UNSOLVABLE_PART_B:Integer.MAX_VALUE;
        Set<Integer> uniqueFrequencies = new HashSet<>();
        while (true) {
            checksMade++;
            if (!uniqueFrequencies.add(frequency)) {
                part2FrequencyResult = frequency;
                printWriter.println("Took " + checksMade + " checks");
                break;
            }
            frequency += inputFrequencyChanges[index];
            index = (index + 1) % inputFrequencyChangeCount;
            if (checksMade >= checkLimit) {
                printWriter.println("Reached " + checksMade + " checks made with no solution" + (partBPotentiallyUnsolvable?" and part B is marked as potentially unsolvable":"") + ". Aborting.");
                part2FrequencyResult = null;
                break;
            }
        }
        return new BasicPuzzleResults<>(
            part1FrequencyResult,
            part2FrequencyResult
        );
    }
}
