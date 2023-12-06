package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2023;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day06 implements IPuzzle {
    private static final Pattern PATTERN_ALL_INPUT = Pattern.compile("^Time: +(?<times>(?: *?[0-9]+)+) *\\nDistance: +(?<recordDistances>(?: *?[0-9]+)+) *\\n*$");
    private static final Pattern PATTERN_SPACE = Pattern.compile(" +");

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        Matcher matcher = PATTERN_ALL_INPUT.matcher(new String(inputCharacters));
        if (!matcher.matches()) {
            throw new IllegalStateException("Unable to parse input");
        }
        String timesInput = matcher.group("times");
        String recordDistancesInput = matcher.group("recordDistances");
        return new BasicPuzzleResults<>(
            solve(
                parsePartA(timesInput),
                parsePartA(recordDistancesInput)
            ),
            solve(
                parsePartB(timesInput),
                parsePartB(recordDistancesInput)
            )
        );
    }

    private static long solve(long[] times, long[] recordDistances) {
        int raceCount = times.length;
        if (recordDistances.length != raceCount) {
            throw new IllegalStateException("Input size mismatch");
        }
        long result = 1;
        for (int raceIndex=0;raceIndex<raceCount; raceIndex++) {
            long time = times[raceIndex];
            long recordDistance = recordDistances[raceIndex];
            long waysToBeatRecord = 0;
            for (long timeSpentPressingButton=0; timeSpentPressingButton<=time; timeSpentPressingButton++) {
                long distance = Math.multiplyExact(time-timeSpentPressingButton,timeSpentPressingButton);
                if (distance > recordDistance) {
                    waysToBeatRecord++;
                }
            }
            result = Math.multiplyExact(result, waysToBeatRecord);
        }
        return result;
    }

    private static long[] parsePartA(String input) {
        return PATTERN_SPACE.splitAsStream(input).mapToLong(Long::parseLong).toArray();
    }

    private static long[] parsePartB(String input) {
        return new long[]{Long.parseLong(PATTERN_SPACE.matcher(input).replaceAll(""))};
    }
}
