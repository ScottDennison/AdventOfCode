package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2024;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

public class Day10 implements IPuzzle {
    private static final class Position {
        private final int y;
        private final int x;

        public Position(int y, int x) {
            this.y = y;
            this.x = x;
        }

        public int getY() {
            return this.y;
        }

        public int getX() {
            return this.x;
        }

        @Override
        public boolean equals(Object otherObject) {
            if (this == otherObject) return true;
            if (!(otherObject instanceof Position)) return false;

            Position otherPosition = (Position) otherObject;

            if (this.y != otherPosition.y) return false;
            if (this.x != otherPosition.x) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = this.y;
            result = 31 * result + this.x;
            return result;
        }
    }

    private static int recurse(char[][] inputGrid, int height, int width, int y, int x, int currentTarget, Set<Position> reachable9Positions) {
        if (y < 0 || y >= height || x < 0 || x >= width || inputGrid[y][x] != currentTarget) {
            return 0;
        }
        if (currentTarget == '9') {
            reachable9Positions.add(new Position(y, x));
            return 1;
        }
        char nextTarget = (char)(currentTarget + 1);
        return
            recurse(inputGrid, height, width, y - 1, x, nextTarget, reachable9Positions)
            +
            recurse(inputGrid, height, width, y + 1, x, nextTarget, reachable9Positions)
            +
            recurse(inputGrid, height, width, y, x - 1, nextTarget, reachable9Positions)
            +
            recurse(inputGrid, height, width, y, x + 1, nextTarget, reachable9Positions);
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        char[][] inputGrid = LineReader.charArraysArray(inputCharacters, true);
        int height = inputGrid.length;
        int width = inputGrid[0].length;
        int partASum = 0;
        int partBSum = 0;
        Set<Position> partAReachable9Positionss = new HashSet<>();
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                partBSum += recurse(inputGrid, height, width, y, x, '0', partAReachable9Positionss);
                partASum += partAReachable9Positionss.size();
                partAReachable9Positionss.clear();
            }
        }
        return new BasicPuzzleResults<>(
            partASum,
            partBSum
        );
    }
}
