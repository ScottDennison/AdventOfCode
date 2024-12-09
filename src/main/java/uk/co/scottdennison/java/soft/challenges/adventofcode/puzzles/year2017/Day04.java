package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2017;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Day04 implements IPuzzle {
    private static final Pattern PATTERN_SPACE = Pattern.compile(" +");

    private static boolean areAllPartsUnique(Stream<String> parts) {
        return parts.allMatch(new HashSet<>()::add);
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        int partAValidPasswordsCount = 0;
        int partBValidPasswordsCount = 0;
        for (String inputLine : LineReader.strings(inputCharacters)) {
            String[] partALineParts = PATTERN_SPACE.split(inputLine);
            if (areAllPartsUnique(Arrays.stream(partALineParts))) {
                partAValidPasswordsCount++;
                if (areAllPartsUnique(Arrays.stream(partALineParts).map(String::toCharArray).sequential().peek(Arrays::sort).map(String::new))) {
                    partBValidPasswordsCount++;
                }
            }
        }
        return new BasicPuzzleResults<>(
            partAValidPasswordsCount,
            partBValidPasswordsCount
        );
    }
}
