package uk.co.scottdennison.java.challenges.adventofcode.framework;

import java.io.PrintWriter;

public interface IPuzzle {
	IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable,PrintWriter progressWriter);
}
