package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2022;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;

public class Day17 implements IPuzzle {
    private static class Rock {
        //private final int width;
        //private final int height;
        //private final boolean[][] pattern;

        public Rock(String... patternLineStrings) {

        }
    }

    private static final Rock[] ROCKS = {
        new Rock(
            "####"
        ),
        new Rock(
            ".#.",
            "###",
            ".#."
        ),
        new Rock(
            "..#",
            "..#",
            "###"
        ),
        new Rock(
            "#",
            "#",
            "#",
            "#"
        ),
        new Rock(
            "##",
            "##"
        )
    };

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        return new BasicPuzzleResults<>(
            null,
            null
        );
    }
}
