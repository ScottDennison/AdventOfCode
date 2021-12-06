package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2021;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.Arrays;

public class Day06 implements IPuzzle {
    private static final int SPAWN_INTERVAL = 8;
    private static final int NORMAL_INTERVAL = 6;

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        int[] inputValues = Arrays.stream(new String(inputCharacters).trim().split(",")).mapToInt(Integer::parseInt).toArray();
        if (Arrays.stream(inputValues).anyMatch(value -> value < 0 || value > NORMAL_INTERVAL)) {
            throw new IllegalStateException("Invalid input");
        }
        int countCount = Math.max(NORMAL_INTERVAL,SPAWN_INTERVAL)+1;
        BigInteger[] initialCounts = new BigInteger[countCount];
        Arrays.fill(initialCounts,BigInteger.ZERO);
        for (int inputValue : inputValues) {
            initialCounts[inputValue] = initialCounts[inputValue].add(BigInteger.ONE);
        }
        return new BasicPuzzleResults<>(
            run(initialCounts,configProvider,1),
            partBPotentiallyUnsolvable?null:run(initialCounts,configProvider,2)
        );
    }

    private BigInteger run(BigInteger[] initialCounts, IPuzzleConfigProvider configProvider, int part) {
        int days = Integer.parseInt(new String(configProvider.getPuzzleConfigChars("part" + part + "_days")));
        int countCount = initialCounts.length;
        int highestCount = countCount-1;
        BigInteger[] counts = Arrays.copyOf(initialCounts, countCount);
        for (int day=1; day<=days; day++) {
            BigInteger spawnCount = counts[0];
            for (int countIndex=1; countIndex<countCount; countIndex++) {
                counts[countIndex-1] = counts[countIndex];
            }
            counts[highestCount] = BigInteger.ZERO;
            counts[NORMAL_INTERVAL] = counts[NORMAL_INTERVAL].add(spawnCount);
            counts[SPAWN_INTERVAL] = counts[SPAWN_INTERVAL].add(spawnCount);
        }
        BigInteger total = BigInteger.ZERO;
        for (BigInteger count : counts) {
            total = total.add(count);
        }
        return total;
    }
}
