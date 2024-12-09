package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2023;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2023.day01.Day01AhoCorasick;

import java.io.PrintWriter;

public class Day01 implements IPuzzle {
    /*
        I have written multiple solutions for day 01. This class simply forwards the runPuzzle method to an instance of one of them.
        The choices are:
        * Day01IndexOf     - My first attempt.
                             It takes 4-7 milliseconds per input data set.
        * Day01AhoCorasick - My second attempt.
                             It takes 200-1000 microseconds per input data set.
    */
    private final IPuzzle instance = new Day01AhoCorasick();

    @Override
    public IPuzzleResults runPuzzle(final char[] inputCharacters, final IPuzzleConfigProvider configProvider, final boolean partBPotentiallyUnsolvable, final PrintWriter printWriter) {
        return instance.runPuzzle(inputCharacters,configProvider,partBPotentiallyUnsolvable,printWriter);
    }
}
