package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2024;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Day01 implements IPuzzle {
    private static final Pattern LINE_PATTERN = Pattern.compile("^([0-9]+)[ \\t]+([0-9]+)$");

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        String[] inputLines = LineReader.stringsArray(inputCharacters, true);
        int inputLineCount = inputLines.length;
        int[] leftArray = new int[inputLineCount];
        int[] rightArray = new int[inputLineCount];
        for (int inputLineIndex=0; inputLineIndex<inputLineCount; inputLineIndex++) {
            Matcher matcher = LINE_PATTERN.matcher(inputLines[inputLineIndex]);
            if (!matcher.matches()) {
                throw new IllegalStateException("Could not parse input line");
            }
            leftArray[inputLineIndex] = Integer.parseInt(matcher.group(1));
            rightArray[inputLineIndex] = Integer.parseInt(matcher.group(2));
        }
        Arrays.sort(leftArray);
        Arrays.sort(rightArray);
        int partAResult = 0;
        int partBResult = 0;
        Map<Integer,Long> rightCounts = Arrays.stream(rightArray).boxed().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        for (int inputLineIndex=0; inputLineIndex<inputLineCount; inputLineIndex++) {
            int leftEntry = leftArray[inputLineIndex];
            partAResult += Math.abs(leftEntry - rightArray[inputLineIndex]);
            partBResult += leftEntry * rightCounts.getOrDefault(leftEntry, 0L).intValue();
        }
        return new BasicPuzzleResults<>(
            partAResult,
            partBResult
        );
    }
}
