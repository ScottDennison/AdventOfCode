package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2021;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day17 implements IPuzzle {
    private static final Pattern PATTERN = Pattern.compile("^target area: x=(?<x1>-?[0-9]+)\\.\\.(?<x2>-?[0-9]+), y=(?<y1>-?[0-9]+)\\.\\.(?<y2>-?[0-9]+)$");

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        Matcher matcher = PATTERN.matcher(new String(inputCharacters).trim());
        if (!matcher.matches()) {
            throw new IllegalStateException("Could not parse puzzle input");
        }
        int x1 = Integer.parseInt(matcher.group("x1"));
        int x2 = Integer.parseInt(matcher.group("x2"));
        int y1 = Integer.parseInt(matcher.group("y1"));
        int y2 = Integer.parseInt(matcher.group("y2"));
        int minX = Math.min(x1,x2);
        int maxX = Math.max(x1,x2);
        int minY = Math.min(y1,y2);
        int maxY = Math.max(y1,y2);
        if (minY >= 0) {
            throw new IllegalStateException("This solution only works when the lowest Y co-ordinate of the target position is negative");
        }
        int partATriangleNumberNeeded = -minY-1;
        int partAHighestY = (partATriangleNumberNeeded*(partATriangleNumberNeeded+1))/2;
        int highestPotentialStartXVelocity = maxX;
        int highestPotentialStartYVelocity = -minY-1;
        int lowestPotentialStartXVelocity = 0;
        int lowestPotentialStartYVelocity = minY;
        int position=0;
        while (true) {
            position += lowestPotentialStartXVelocity;
            if (position > maxX) {
                throw new IllegalStateException("Cannot land in the target position area");
            }
            if (position >= minX) {
                break;
            }
            lowestPotentialStartXVelocity++;
        }
        int partBCandidates = 0;
        for (int startYVelocity=lowestPotentialStartYVelocity; startYVelocity<=highestPotentialStartYVelocity; startYVelocity++) {
            for (int startXVelocity=lowestPotentialStartXVelocity; startXVelocity<=highestPotentialStartXVelocity; startXVelocity++) {
                int yVelocity = startYVelocity;
                int xVelocity = startXVelocity;
                int y=0;
                int x=0;
                boolean enteredTargetZone = false;
                while (y >= minY && x <= maxX) {
                    if (y <= maxY && x >= minX) {
                        enteredTargetZone = true;
                        break;
                    }
                    y += yVelocity;
                    x += xVelocity;
                    yVelocity--;
                    if (xVelocity > 0) {
                        xVelocity--;
                    } else if (xVelocity < 0) {
                        xVelocity++;
                    }
                }
                if (enteredTargetZone) {
                    partBCandidates++;
                }
            }
        }
        return new BasicPuzzleResults<>(
            partAHighestY,
            partBCandidates
        );
    }
}
