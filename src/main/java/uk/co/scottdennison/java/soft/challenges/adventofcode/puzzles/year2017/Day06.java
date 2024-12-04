package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2017;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day06 implements IPuzzle {
    private static final Pattern PATTERN_SPACES = Pattern.compile("\\s+");

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        int[] banks = PATTERN_SPACES.splitAsStream(new String(inputCharacters)).mapToInt(Integer::parseInt).toArray();
        int bankCount = banks.length;
        Map<List<Integer>,Integer> seenBanks = new HashMap<>();
        int redistributionIterations = 0;
        while (true) {
            Integer alreadySeenRedistributionIteration = seenBanks.put(IntStream.of(banks).boxed().collect(Collectors.toList()), redistributionIterations);
            if (alreadySeenRedistributionIteration != null) {
                return new BasicPuzzleResults<>(
                    redistributionIterations,
                    redistributionIterations - alreadySeenRedistributionIteration
                );
            }
            int largestBankIndex = 0;
            int largestBank = -1;
            for (int bankIndex=0; bankIndex<banks.length; bankIndex++) {
                int bank = banks[bankIndex];
                if (bank > largestBank) {
                    largestBankIndex = bankIndex;
                    largestBank = bank;
                }
            }
            banks[largestBankIndex] = 0;
            int additionPerBank = largestBank / bankCount;
            int extraAdditions = largestBank - (additionPerBank * bankCount);
            int bankIterations = additionPerBank == 0 ? extraAdditions : bankCount;
            int bankIndex = largestBankIndex;
            for (int iteration=0; iteration<bankIterations; iteration++) {
                if (++bankIndex == bankCount) {
                    bankIndex = 0;
                }
                banks[bankIndex] += additionPerBank + (extraAdditions-- > 0 ? 1 : 0);
            }
            redistributionIterations++;
        }
    }
}
