package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2024;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Day11 implements IPuzzle {
    private static long run(long[] input, int iterations) {
        Map<Long,Long> state = new HashMap<>();
        for (long inputEntry : input) {
            state.merge(inputEntry, 1L, Math::addExact);
        }
        for (int iteration=1; iteration<=iterations; iteration++) {
            Map<Long,Long> newState = new HashMap<>();
            for (Map.Entry<Long,Long> stateEntry : state.entrySet()) {
                long stateEntryNumber = stateEntry.getKey();
                long stateEntryCount = stateEntry.getValue();
                if (stateEntryNumber == 0) {
                    newState.merge(1L, stateEntryCount, Math::addExact);
                }
                else {
                    int length = (int)Math.ceil(Math.log10(stateEntryNumber + 1));
                    if ((length & 1) == 0) {
                        long divisor = (long)Math.pow(10, length>>1);
                        long quotient = stateEntryNumber / divisor;
                        newState.merge(quotient, stateEntryCount, Math::addExact);
                        newState.merge(stateEntryNumber - (divisor * quotient), stateEntryCount, Math::addExact);
                    }
                    else {
                        newState.merge(stateEntryNumber * 2024, stateEntryCount, Math::addExact);
                    }
                }
            }
            state = newState;
        }
        return state.values().stream().mapToLong(x -> x).reduce(Math::addExact).getAsLong();
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        long input[] = Arrays.stream(new String(inputCharacters).trim().split(" ")).mapToLong(Long::parseLong).toArray();
        return new BasicPuzzleResults<>(
            run(input, 25),
            run(input, 75)
        );
    }
}
