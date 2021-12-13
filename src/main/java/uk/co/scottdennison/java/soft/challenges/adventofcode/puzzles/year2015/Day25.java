package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2015;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day25 implements IPuzzle {
    private static final Pattern PATTERN = Pattern.compile("^To continue, please consult the code grid in the manual\\.\\s*Enter the code at row (?<row>[0-9]+), column (?<column>[0-9]+)\\.?$", Pattern.CASE_INSENSITIVE);

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        Matcher matcher = PATTERN.matcher(new String(inputCharacters).trim());
        if (!matcher.matches()) {
            throw new IllegalStateException("Cannot parse puzzle input");
        }
        int row = Integer.parseInt(matcher.group("row"));
        int column = Integer.parseInt(matcher.group("column"));
        int iterationsRequired = (((row+column-1)*(row+column))/2)-(row+column-1-column);
        long value = 20151125L;
        for (int iteration=0; iteration<iterationsRequired; iteration++) {
            value = (value * 252533L) % 33554393L;
        }
        return new BasicPuzzleResults<>(
            value,
            "Start the Weather Machine"
        );
    }
}
