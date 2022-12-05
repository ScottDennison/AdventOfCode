package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2022;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day04 implements IPuzzle {
    private static final Pattern LINE_PATTERN = Pattern.compile("^(?<elf1Start>[0-9]+)-(?<elf1End>[0-9]+),(?<elf2Start>[0-9]+)-(?<elf2End>[0-9]+)$");

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        int containCount = 0;
        int overlapCount = 0;
        for (String line : LineReader.strings(inputCharacters)) {
            Matcher matcher = LINE_PATTERN.matcher(line);
            if (!matcher.matches()) {
                throw new IllegalStateException("Could not parse line");
            }
            int elf1Start = Integer.parseInt(matcher.group("elf1Start"));
            int elf1End = Integer.parseInt(matcher.group("elf1End"));
            int elf2Start = Integer.parseInt(matcher.group("elf2Start"));
            int elf2End = Integer.parseInt(matcher.group("elf2End"));
            if ((elf1Start <= elf2Start && elf1End >= elf2End) || (elf2Start <= elf1Start && elf2End >= elf1End)) {
                containCount++;
            }
            if (elf1Start <= elf2End && elf1End >= elf2Start) {
                overlapCount++;
            }
        }
        return new BasicPuzzleResults<>(
            containCount,
            overlapCount
        );
    }
}
