package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2025;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;

public class Day01 implements IPuzzle {
    private static final int DIAL_START_POSITION = 50;
    private static final int DIAL_SIZE = 100;

    private static enum Direction {
        LEFT {
            @Override
            public int adjust(int dialPosition, int amount) {
                return dialPosition - amount;
            }
        },
        RIGHT {
            @Override
            public int adjust(int dialPosition, int amount) {
                return dialPosition + amount;
            }
        };

        public abstract int adjust(int dialPosition, int amount);
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        int dialPosition = DIAL_START_POSITION;
        int part1Zeros = 0;
        int part2Zeros = 0;
        Direction direction;
        for (char[] inputLine : LineReader.charArrays(inputCharacters)) {
            char multiplierChar = inputLine[0];
            int multiplier;
            switch (multiplierChar) {
                case 'L':
                    direction = Direction.LEFT;
                    break;
                case 'R':
                    direction = Direction.RIGHT;
                    break;
                default:
                    throw new IllegalStateException("Unexpected direction: " + multiplierChar);
            }
            int amount = Integer.parseInt(new String(inputLine, 1, inputLine.length - 1));
            int fullRotations = amount / DIAL_SIZE;
            amount -= fullRotations * DIAL_SIZE;
            part2Zeros += fullRotations;
            if (amount > 0) {
                int dialPositionBeforeMovement = dialPosition;
                dialPosition = direction.adjust(dialPosition, amount);
                if ((dialPosition != (dialPosition = Math.floorMod(dialPosition, DIAL_SIZE)))) {
                    if (!(direction == Direction.LEFT && dialPositionBeforeMovement == 0)) {
                        part2Zeros++;
                    }
                }
            }
            if (dialPosition == 0) {
                part1Zeros++;
                if (amount > 0 && direction == Direction.LEFT) {
                    part2Zeros++;
                }
            }
        }
        return new BasicPuzzleResults<>(part1Zeros, part2Zeros);
    }
}
