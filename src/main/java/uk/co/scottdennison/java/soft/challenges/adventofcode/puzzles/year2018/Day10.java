package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2018;

import uk.co.scottdennison.java.libs.text.input.LineReader;
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
                char[][] pointGrid = new char[height][width];
                for (int y=0; y<height; y++) {
                    Arrays.fill(pointGrid[y], '.');
                }
                for (Point point : points) {
                    point.unstep();
                    pointGrid[point.getPositionY() - minY][point.getPositionX() - minX] = '#';
                }
                for (int y=0; y<height; y++) {
                    printWriter.println(pointGrid[y]);
                }
                printWriter.println("Found on step " + (stepCount - 1));
                break;
            }
            lastHeight = height;
            lastWidth = width;
        }
        return null;
    }
}
