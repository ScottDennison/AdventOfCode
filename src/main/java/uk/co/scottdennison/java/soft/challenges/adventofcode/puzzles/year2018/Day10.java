package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2018;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.common.AsciiArtProcessor;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day10 implements IPuzzle {
    private static final Pattern LINE_PATTERN = Pattern.compile("^position=< *(?<positionX>-?[0-9]+), *(?<positionY>-?[0-9]+)> +velocity=< *(?<velocityX>-?[0-9]+), *(?<velocityY>-?[0-9]+)>");

    private static class Point {
        private int positionX;
        private int positionY;
        private int velocityX;
        private int velocityY;

        public Point(int positionX, int positionY, int velocityX, int velocityY) {
            this.positionX = positionX;
            this.positionY = positionY;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
        }

        public void step() {
            positionX += velocityX;
            positionY += velocityY;
        }

        public void unstep() {
            positionX -= velocityX;
            positionY -= velocityY;
        }

        public int getPositionX() {
            return this.positionX;
        }

        public int getPositionY() {
            return this.positionY;
        }

        public int getVelocityX() {
            return this.velocityX;
        }

        public int getVelocityY() {
            return this.velocityY;
        }
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        AsciiArtProcessor.StandardFontDefinition fontDefinition = AsciiArtProcessor.StandardFontDefinition.valueOf(new String(configProvider.getPuzzleConfigChars("font")).trim());
        String[] inputLines = LineReader.stringsArray(inputCharacters, true);
        int pointCount = inputLines.length;
        Point[] points = new Point[pointCount];
        for (int pointIndex=0; pointIndex<pointCount; pointIndex++) {
            Matcher matcher = LINE_PATTERN.matcher(inputLines[pointIndex]);
            if (!matcher.matches()) {
                throw new IllegalStateException("Could not parse line");
            }
            points[pointIndex] = new Point(
                Integer.parseInt(matcher.group("positionX")),
                Integer.parseInt(matcher.group("positionY")),
                Integer.parseInt(matcher.group("velocityX")),
                Integer.parseInt(matcher.group("velocityY"))
            );
        }
        int lastMinY = Integer.MAX_VALUE;
        int lastMaxY = Integer.MIN_VALUE;
        int lastMinX = Integer.MAX_VALUE;
        int lastMaxX = Integer.MIN_VALUE;
        int lastHeight = Integer.MAX_VALUE;
        int lastWidth = Integer.MAX_VALUE;
        int stepCount = 0;
        while (true) {
            stepCount++;
            int minY = Integer.MAX_VALUE;
            int maxY = Integer.MIN_VALUE;
            int minX = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            for (Point point : points) {
                point.step();
                int y = point.getPositionY();
                int x = point.getPositionX();
                if (y < minY) {
                    minY = y;
                }
                if (y > maxY) {
                    maxY = y;
                }
                if (x < minX) {
                    minX = x;
                }
                if (x > maxX) {
                    maxX = x;
                }
            }
            int height = maxY - minY + 1;
            int width = maxX - minX + 1;
            if (height > lastHeight || width > lastWidth) {
                char[][] pointCharGrid = new char[lastHeight][lastWidth];
                boolean[][] pointBooleanGrid = new boolean[lastHeight][lastWidth];
                for (int y=0; y<lastHeight; y++) {
                    Arrays.fill(pointCharGrid[y], '.');
                    Arrays.fill(pointBooleanGrid[y], false);
                }
                for (Point point : points) {
                    point.unstep();
                    int gridY = point.getPositionY() - lastMinY;
                    int gridX = point.getPositionX() - lastMinX;
                    pointCharGrid[gridY][gridX] = '#';
                    pointBooleanGrid[gridY][gridX] = true;
                }
                for (int y=0; y<lastHeight; y++) {
                    printWriter.println(pointCharGrid[y]);
                }
                return new BasicPuzzleResults<>(
                    AsciiArtProcessor.parseSingleLineAsString(pointBooleanGrid, lastHeight, lastWidth, fontDefinition),
                    stepCount - 1
                );
            }
            lastMinY = minY;
            lastMaxY = maxY;
            lastMinX = minX;
            lastMaxX = maxX;
            lastHeight = height;
            lastWidth = width;
        }
    }
}
