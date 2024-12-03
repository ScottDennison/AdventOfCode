package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2024;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day03 implements IPuzzle {
    private static final Pattern PATTERN = Pattern.compile("(?:(?<mulInstruction>mul)\\((?<mulArg1>[0-9]+),(?<mulArg2>[0-9]+)\\))|(?:(?<doInstruction>do)\\(\\))|(?:(?<dontInstruction>don't)\\(\\))");

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        int partATotal = 0;
        int partBTotal = 0;
        boolean enabled = true;
        Matcher matcher = PATTERN.matcher(new String(inputCharacters));
        while (matcher.find()) {
            if (matcher.group("mulInstruction") != null) {
                int product = Integer.parseInt(matcher.group("mulArg1"))*Integer.parseInt(matcher.group("mulArg2"));
                partATotal += product;
                if (enabled) {
                    partBTotal += product;
                }
            }
            else if (matcher.group("doInstruction") != null) {
                enabled = true;
            }
            else if (matcher.group("dontInstruction") != null) {
                enabled = false;
            }
            else {
                throw new IllegalStateException("Unexpected state");
            }
        }
        return new BasicPuzzleResults<>(
            partATotal,
            partBTotal
        );
    }
}
