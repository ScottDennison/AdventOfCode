package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2023;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2023.day01.Day01AhoCorasick;

import java.io.PrintWriter;

public class Day21 implements IPuzzle {
    private final IPuzzle instance = new Day21PartialAttempt2B();

    @Override
    public IPuzzleResults runPuzzle(final char[] inputCharacters, final IPuzzleConfigProvider configProvider, final boolean partBPotentiallyUnsolvable, final PrintWriter printWriter) {
        return instance.runPuzzle(inputCharacters,configProvider,partBPotentiallyUnsolvable,printWriter);
    }
}
