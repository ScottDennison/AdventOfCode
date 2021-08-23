package uk.co.scottdennison.java.soft.challenges.adventofcode.framework;

import java.io.PrintWriter;

public interface IPuzzle {
	IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter);
}
