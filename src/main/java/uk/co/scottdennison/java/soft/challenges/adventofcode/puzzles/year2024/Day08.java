package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2024;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Day08 implements IPuzzle {
    private static class Position {
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

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        char[][] inputGrid = LineReader.charArraysArray(inputCharacters, true);
        int height = inputGrid.length;
        int width = inputGrid[0].length;
        int largerDimension = Math.max(height, width);
        Map<Character, List<Position>> antennaPositions = new HashMap<>();
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                char character = inputGrid[y][x];
                if (character != '.') {
                    antennaPositions.computeIfAbsent(character, __ -> new ArrayList<>()).add(new Position(y, x));
                }
            }
        }
        Set<Position> partAAntinodePositions = new HashSet<>();
        Set<Position> partBAntinodePositions = new HashSet<>();
        for (List<Position> specificFrequencyAntennaPositions : antennaPositions.values()) {
            int positionCount = specificFrequencyAntennaPositions.size();
            for (int positionIndex1=0; positionIndex1<positionCount; positionIndex1++) {
                Position position1 = specificFrequencyAntennaPositions.get(positionIndex1);
                int position1Y = position1.getY();
                int position1X = position1.getX();
                for (int positionIndex2=positionIndex1+1; positionIndex2<positionCount; positionIndex2++) {
                    Position position2 = specificFrequencyAntennaPositions.get(positionIndex2);
                    int position2Y = position2.getY();
                    int position2X = position2.getX();
                    int differenceY = position2Y - position1Y;
                    int differenceX = position2X - position1X;
                    addAntinodes(partAAntinodePositions, false, 1, height, width, position1Y, position1X, -differenceY, -differenceX);
                    addAntinodes(partAAntinodePositions, false, 1, height, width, position2Y, position2X, differenceY, differenceX);
                    addAntinodes(partBAntinodePositions, true, largerDimension, height, width, position1Y, position1X, -differenceY, -differenceX);
                    addAntinodes(partBAntinodePositions, true, largerDimension, height, width, position2Y, position2X, differenceY, differenceX);
                }
            }
        }
        return new BasicPuzzleResults<>(
            partAAntinodePositions.size(),
            partBAntinodePositions.size()
        );
    }

    private void addAntinodes(Set<Position> antinodePositions, boolean includeOriginalPosition, int count, int height, int width, int startY, int startX, int differenceY, int differenceX) {
        int y = startY;
        int x = startX;
        if (includeOriginalPosition) {
            antinodePositions.add(new Position(y, x));
        }
        for (int index=0; index<count; index++) {
            y += differenceY;
            if (y < 0 || y >= height) {
                break;
            }
            x += differenceX;
            if (x < 0 || x >= width) {
                break;
            }
            antinodePositions.add(new Position(y, x));
        }
    }
}
