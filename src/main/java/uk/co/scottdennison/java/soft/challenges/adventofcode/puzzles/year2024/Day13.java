package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2024;

import uk.co.scottdennison.java.libs.math.ExtendedEuclideanAlgorithm;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.OptionalLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day13 implements IPuzzle {
    private static final long BUTTON_A_TOKEN_COST = 3;
    private static final long BUTTON_B_TOKEN_COST = 1;
    private static final long PART_B_PRIZE_COORDINATE_OFFSET = 10000000000000L;

    private static final Pattern INPUT_PATTERN = Pattern.compile("\\GButton A: X\\+(?<buttonAXDelta>[0-9]+), Y\\+(?<buttonAYDelta>[0-9]+)(?:\\r\\n|\\r|\\n)Button B: X\\+(?<buttonBXDelta>[0-9]+), Y\\+(?<buttonBYDelta>[0-9]+)(?:\\r\\n|\\r|\\n)Prize: X=(?<prizeX>[0-9]+), Y=(?<prizeY>[0-9]+)(?:\\r\\n|\\r|\\n)*");

    private static OptionalLong calculateTokenCost(long buttonAXDelta, long buttonAYDelta, long buttonBXDelta, long buttonBYDelta, long prizeX, long prizeY) {
        // (aPresses * buttonAXDelta) + (bPresses * buttonBXDelta) = prizeX
        // (aPresses * buttonAYDelta) + (bPresses * buttonYXDelta) = prizeY
        // turn this into
        // (aPresses * buttonAXDelta) + (bPresses * buttonBXDelta) - prizeX = (aPresses * buttonAYDelta) + (bPresses * buttonYXDelta) - prizeY ( = 0)
        // which in turn can become
        // (aPresses * buttonAXDelta) + (bPresses * buttonBXDelta) - prizeX - ((aPresses * buttonAYDelta) + (bPresses * buttonYXDelta) - prizeY) = 0
        // which results in
        // (aPresses * (buttonAXDelta - buttonAYDelta)) + (bPresses * (buttonBXDelta - buttonAYDelta) - (prizeX - prizeY) = 0
        // and therefore
        // (aPresses * (buttonAXDelta - buttonAYDelta)) + (bPresses * (buttonBXDelta - buttonAYDelta) = (prizeX - prizeY)
        // This is then a linear diophantine equation of form ax + by = c, and can be solved accordingly
        // I used https://math.stackexchange.com/questions/20717/how-to-find-solutions-of-linear-diophantine-ax-by-c as a reference to solve this
        // For some reason I spent hours having this not work, only to fix the problem within 5 minutes of coming back the next day. *groan*
        long a = buttonAXDelta - buttonAYDelta;
        long b = buttonBXDelta - buttonBYDelta;
        long c = prizeX - prizeY;
        ExtendedEuclideanAlgorithm.Result eeaResult = ExtendedEuclideanAlgorithm.solve(a,b);
        long gcd = eeaResult.getGcd();
        if (c % gcd != 0) {
            return OptionalLong.empty();
        }
        long k = c / gcd;
        long x1 = eeaResult.getBezoutCoefficientS() * k;
        long y1 = eeaResult.getBezoutCoefficientT() * k;
        long x1r = -(b / gcd);
        long y1r = a / gcd;
        long n = (prizeX - ((buttonAXDelta * x1) + (buttonBXDelta * y1))) / ((buttonAXDelta * x1r) + (buttonBXDelta * y1r));
        long aPresses = (x1r*n)+x1;
        long bPresses = (y1r*n)+y1;
        long clawX = (aPresses * buttonAXDelta) + (bPresses * buttonBXDelta);
        long clawY = (aPresses * buttonAYDelta) + (bPresses * buttonBYDelta);
        if (clawX != prizeX || clawY != prizeY) {
            return OptionalLong.empty();
        }
        return OptionalLong.of((aPresses * BUTTON_A_TOKEN_COST) + (bPresses * BUTTON_B_TOKEN_COST));
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        Matcher matcher = INPUT_PATTERN.matcher(new String(inputCharacters).trim());
        long partACheapestTokenCost = 0;
        long partBCheapestTokenCost = 0;
        do {
            if (!matcher.find()) {
                throw new IllegalStateException("Could not parse full input.");
            }
            long buttonAXDelta = Long.parseLong(matcher.group("buttonAXDelta"));
            long buttonAYDelta = Long.parseLong(matcher.group("buttonAYDelta"));
            long buttonBXDelta = Long.parseLong(matcher.group("buttonBXDelta"));
            long buttonBYDelta = Long.parseLong(matcher.group("buttonBYDelta"));
            long prizeX = Long.parseLong(matcher.group("prizeX"));
            long prizeY = Long.parseLong(matcher.group("prizeY"));
            partACheapestTokenCost += calculateTokenCost(buttonAXDelta, buttonAYDelta, buttonBXDelta, buttonBYDelta, prizeX, prizeY).orElse(0L);
            partBCheapestTokenCost += calculateTokenCost(buttonAXDelta, buttonAYDelta, buttonBXDelta, buttonBYDelta, prizeX + PART_B_PRIZE_COORDINATE_OFFSET, prizeY + PART_B_PRIZE_COORDINATE_OFFSET).orElse(0L);
        } while (!matcher.hitEnd());
        return new BasicPuzzleResults<>(
            partACheapestTokenCost,
            partBCheapestTokenCost
        );
    }
}
