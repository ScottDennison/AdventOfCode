package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2016;

import uk.co.scottdennison.java.libs.math.ChineseNumberTheorem;
import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day15 implements IPuzzle {
    private static final class Disc {
        private final int positionCount;
        private final int positionAtTimeZero;

        public Disc(int positionCount, int positionAtTimeZero) {
            this.positionCount = positionCount;
            this.positionAtTimeZero = positionAtTimeZero;
        }

        public int getPositionCount() {
            return this.positionCount;
        }

        public int getPositionAtTimeZero() {
            return this.positionAtTimeZero;
        }
    }

    private static final Pattern PATTERN = Pattern.compile("^Disc #(?<discNumber>[0-9]+) has (?<positionCount>[0-9]+) positions; at time=(?<recordTime>[0-9]+), it is at position (?<recordPosition>[0-9]+)\\.$");
    private static final Disc PART_B_EXTRA_DISC = new Disc(11,0);

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        String[] lines = LineReader.stringsArray(inputCharacters,true);
        int partADiscCount = lines.length;
        Disc[] partADiscs = new Disc[partADiscCount];
        for (int lineIndex=0; lineIndex<partADiscCount; lineIndex++) {
            Matcher matcher = PATTERN.matcher(lines[lineIndex].trim());
            if (!matcher.matches()) {
                throw new IllegalStateException("Could not parse line.");
            }
            int discNumber = Integer.parseInt(matcher.group("discNumber"));
            int positionCount = Integer.parseInt(matcher.group("positionCount"));
            int recordTime = Integer.parseInt(matcher.group("recordTime"));
            int recordPosition = Integer.parseInt(matcher.group("recordPosition"));
            int positionAtTimeZero = (recordPosition-recordTime)%positionCount;
            if (positionAtTimeZero < 0) {
                positionAtTimeZero = positionCount-positionAtTimeZero;
            }
            int discIndex = discNumber-1;
            if (partADiscs[discIndex] != null) {
                throw new IllegalStateException("Disc already set");
            }
            partADiscs[discIndex] = new Disc(positionCount,positionAtTimeZero);
        }
        Disc[] partBDiscs = new Disc[partADiscCount+1];
        System.arraycopy(partADiscs,0,partBDiscs,0,partADiscCount);
        partBDiscs[partADiscCount] = PART_B_EXTRA_DISC;
        return new BasicPuzzleResults<>(
            run(partADiscs),
            run(partBDiscs)
        );
    }

    private long run(Disc[] discs) {
        int discCount = discs.length;
        ChineseNumberTheorem.Input[] chineseNumberTheoremInputs = new ChineseNumberTheorem.Input[discCount];
        for (int discIndex=0; discIndex<discCount; discIndex++) {
            Disc disc = discs[discIndex];
            int positionCount = disc.getPositionCount();
            int positionAtTimeZero = disc.getPositionAtTimeZero();
            chineseNumberTheoremInputs[discIndex] = new ChineseNumberTheorem.Input(positionCount, ((-positionAtTimeZero)-(discIndex+1))%positionCount);
        }
        return ChineseNumberTheorem.solve(chineseNumberTheoremInputs);
    }
}
