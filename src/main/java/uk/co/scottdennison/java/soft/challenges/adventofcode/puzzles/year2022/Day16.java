package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2022;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2022.day16.Day16NonVariableActors;

import java.io.PrintWriter;

public class Day16 implements IPuzzle {
    /*
        I have written multiple solutions for day 16. This class simply forwards the runPuzzle method to an instance of one of them.
        The choices are:
        * Day16OriginalSlow                 - My first attempt.
                                              It takes 60-162 seconds per input data set.
        * Day16RecurseActorSelection        - My second attempt. It can support any number of elephants helping.
                                              It runs at an approximate maximum of 750ms per input data set.
        * Day16DynamicProgrammingWithMaps   - My third attempt. It also supports a variable number of elephants. This works best with 2+ elephants (so 3+ actors), and with larger number of valves.
                                              With 1 elephant, it runs at an approximate maximum of 750ms per input data set.
        * Day16DynamicProgrammingWithArrays - My fourth attempt, and is a tweak to my third attempt. This works best with 2+ elephants (so 3+ actors), and with smaller number of valves.
                                              With 1 elephant, it runs at an approximate maximum of 375ms per input data set.
        * Day16NonVariableActors            - My fifth attempt. It can only support either no elephants or one elephant, and as such there are optimizations that can be applied.
                                              It runs at an approximate maximum of 175ms per input data set.
    */
    private final IPuzzle instance = new Day16NonVariableActors();

    @Override
    public IPuzzleResults runPuzzle(final char[] inputCharacters, final IPuzzleConfigProvider configProvider, final boolean partBPotentiallyUnsolvable, final PrintWriter printWriter) {
        return instance.runPuzzle(inputCharacters,configProvider,partBPotentiallyUnsolvable,printWriter);
    }
}
