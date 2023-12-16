package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2017;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class Day05 implements IPuzzle {
    private static final int DECISION_POINT = 3;

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        int[] values = LineReader.stringsStream(inputCharacters).mapToInt(Integer::parseInt).toArray();
        return new BasicPuzzleResults<>(
            solve(values, false),
            solve(values, true)
        );
    }

    private static int solve(int[] values, final boolean decrementAfterDecisionPoint) {
        int index = 0;
        int instructionsExecuted = 0;
        int valuesCount = values.length;
        int[] valuesCopy = Arrays.copyOf(values,valuesCount);
        do {
            int offset = valuesCopy[index];
            if (decrementAfterDecisionPoint && offset >= DECISION_POINT) {
                valuesCopy[index]--;
            }
            else {
                valuesCopy[index]++;
            }
            index += offset;
            instructionsExecuted++;
        } while (index >= 0 && index < valuesCount);
        return instructionsExecuted;
    }
}
