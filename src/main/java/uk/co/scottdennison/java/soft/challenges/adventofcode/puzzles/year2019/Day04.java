package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2019;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day04 implements IPuzzle {
    private static final Pattern INPUT_PATTERN = Pattern.compile("^(?<min>[0-9]+)-(?<max>[0-9]+)$");

    private static class IncrementableInt {
        private int value = 0;

        public void increment() {
            value++;
        }

        public int getValue() {
            return value;
        }
    }

    private static void recurse(
        int index,
        int maxIndex,
        int[] min,
        int[] max,
        boolean edgeMin,
        boolean edgeMax,
        boolean criteriaAMatches,
        boolean criteriaBMatches,
        int lastDigit3,
        int lastDigit2,
        int lastDigit1,
        IncrementableInt criteriaACount,
        IncrementableInt criteriaBCount
    ) {
        if (index == maxIndex) {
            if (criteriaAMatches) {
                criteriaACount.increment();
            }
            if (criteriaBMatches || (lastDigit3 != lastDigit2 && lastDigit2 == lastDigit1)) {
                criteriaBCount.increment();
            }
        }
        else {
            int thisMin = min[index];
            int thisMax = max[index];
            int nextIndex = index + 1;
            int start = edgeMin ? Math.max(lastDigit1, thisMin) : lastDigit1;
            int end = edgeMax ? thisMax : 9;
            for (int digit0=start; digit0<=end; digit0++) {
                recurse(
                    nextIndex,
                    maxIndex,
                    min,
                    max,
                    edgeMin && digit0 == thisMin,
                    edgeMax && digit0 == thisMax,
                    criteriaAMatches || digit0 == lastDigit1,
                    criteriaBMatches || (lastDigit3 != lastDigit2 && lastDigit2 == lastDigit1 && lastDigit1 != digit0),
                    lastDigit2,
                    lastDigit1,
                    digit0,
                    criteriaACount,
                    criteriaBCount
                );
            }
        }
    }

    private int[] parseInputPart(String inputPart) {
        char[] characters = inputPart.toCharArray();
        int length = characters.length;
        int[] ints = new int[length];
        for (int index=0; index<length; index++) {
            ints[index] = characters[index] - '0';
        }
        return ints;
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        Matcher inputMatcher = INPUT_PATTERN.matcher(new String(inputCharacters).trim());
        if (!inputMatcher.matches()) {
            throw new IllegalStateException("Invalid input");
        }
        String minString = inputMatcher.group("min");
        String maxString = inputMatcher.group("max");
        int length;
        if ((length = minString.length()) != maxString.length()) {
            throw new IllegalStateException("Mismatched lengths");
        }
        IncrementableInt criteriaACount = new IncrementableInt();
        IncrementableInt criteriaBCount = new IncrementableInt();
        recurse(
            0,
            length,
            parseInputPart(minString),
            parseInputPart(maxString),
            true,
            true,
            false,
            false,
            -3,
            -2,
            -1,
            criteriaACount,
            criteriaBCount
        );
        return new BasicPuzzleResults<>(
            criteriaACount.getValue(),
            criteriaBCount.getValue()
        );
    }

    public static void main1(String[] args) {
        long startTime = System.nanoTime();
        for (int a=1; a<=9; a++) {
            if (a < 2 || a > 7) {
                continue;
            }
            boolean edgeMinA = a == 2;
            boolean edgeMaxA = a == 7;
            for (int b=a; b<=9; b++) {
                if ((edgeMinA && b < 7) || (edgeMaxA && b > 8)) {
                    continue;
                }
                boolean edgeMinB = edgeMinA && b == 7;
                boolean edgeMaxB = edgeMaxA && b == 8;
                boolean hadDoubleB = a == b;
                for (int c=b; c<=9; c++) {
                    if ((edgeMinB && c < 1) || (edgeMaxB && c > 5)) {
                        continue;
                    }
                    boolean edgeMinC = edgeMinB && c == 1;
                    boolean edgeMaxC = edgeMaxB && c == 5;
                    boolean hadDoubleC = hadDoubleB || b == c;
                    boolean blahC = a == b && b != c;
                    for (int d=c; d<=9; d++) {
                        if ((edgeMinC && d < 9) || (edgeMaxC && d > 9)) {
                            continue;
                        }
                        boolean edgeMinD = edgeMinC && d == 9;
                        boolean edgeMaxD = edgeMaxC && d == 9;
                        boolean hadDoubleD = hadDoubleC || c == d;
                        boolean blahD = blahC || (a != b && b == c && c != d);
                        for (int e=d; e<=9; e++) {
                            if ((edgeMinD && e < 7) || (edgeMaxD && e > 6)) {
                                continue;
                            }
                            boolean edgeMinE = edgeMinD && e == 7;
                            boolean edgeMaxE = edgeMaxD && e == 6;
                            boolean hadDoubleE = hadDoubleD || d == e;
                            boolean blahE = blahD || (b != c && c == d && d != e);
                            for (int f=e; f<=9; f++) {
                                if ((edgeMinE && f < 3) || (edgeMaxB && f > 1)) {
                                    continue;
                                }
                                if (hadDoubleE || e == f) {
                                    //System.out.println(a + "" + b + "" + c + "" + d + "" + e + "" + f);
                                }
                                if (blahE || (c != d && d == e && e != f) || (d != e && e == f)) {
                                    System.out.println(a + "" + b + "" + c + "" + d + "" + e + "" + f);
                                }
                            }
                        }
                    }
                }
            }
        }
        long endTime = System.nanoTime();
        System.out.println(endTime-startTime);
    }
}
