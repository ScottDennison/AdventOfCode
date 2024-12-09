package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2018;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day03 implements IPuzzle {
    private static class Segment {
        private final int elfNumber;
        private final int x;
        private final int y;
        private final int width;
        private final int height;

        public Segment(int elfNumber, int x, int y, int width, int height)
        {
            this.elfNumber = elfNumber;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public int getElfNumber()
        {
            return elfNumber;
        }

        public int getX()
        {
            return x;
        }

        public int getY()
        {
            return y;
        }

        public int getWidth()
        {
            return width;
        }

        public int getHeight()
        {
            return height;
        }
    }

    private static final Pattern LINE_PATTERN = Pattern.compile("^#(?<elfNumber>[0-9]+) @ (?<x>[0-9]+),(?<y>[0-9]+): (?<width>[0-9]+)x(?<height>[0-9]+)$");

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        List<Segment> segmentsList = new ArrayList<>();
        for (String inputLine : LineReader.strings(inputCharacters)) {
            Matcher matcher = LINE_PATTERN.matcher(inputLine.trim());
            if (!matcher.matches()) {
                throw new IllegalStateException("Unable to parse line");
            }
            segmentsList.add(
                new Segment(
                    Integer.parseInt(matcher.group("elfNumber")),
                    Integer.parseInt(matcher.group("x")),
                    Integer.parseInt(matcher.group("y")),
                    Integer.parseInt(matcher.group("width")),
                    Integer.parseInt(matcher.group("height"))
                )
            );
        }
        Segment[] segmentArray = segmentsList.toArray(new Segment[0]);
        int segmentCount = segmentArray.length;
        int width = 0;
        int height = 0;
        for (Segment segment : segmentArray) {
            width = Math.max(width, segment.getX() + segment.getWidth());
            height = Math.max(height, segment.getY() + segment.getHeight());
        }
        int[][] occuranceCountGrid = new int[height][width];
        int[][] segmentIndexGrid = new int[height][width];
        for (int segmentIndex=0; segmentIndex<segmentCount; segmentIndex++) {
            Segment segment = segmentArray[segmentIndex];
            int segmentWidth = segment.getWidth();
            int segmentHeight = segment.getHeight();
            int segmentY = segment.getY();
            int segmentX = segment.getX();
            for (int segmentRelativeY=0, gridY=segmentY; segmentRelativeY<segmentHeight; segmentRelativeY++, gridY++) {
                for (int segmentRelativeX=0, gridX=segmentX; segmentRelativeX<segmentWidth; segmentRelativeX++, gridX++) {
                    occuranceCountGrid[gridY][gridX]++;
                    segmentIndexGrid[gridY][gridX] = segmentIndex;
                }
            }
        }
        int overlapCount = 0;
        int[] segmentNoOverlapCounts = new int[segmentCount];
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                switch(occuranceCountGrid[y][x]) {
                    case 0:
                        break;
                    case 1:
                        segmentNoOverlapCounts[segmentIndexGrid[y][x]]++;
                        break;
                    default:
                        overlapCount++;
                }
            }
        }
        Integer nonOverlappingSegmentIndex = null;
        for (int segmentIndex=0; segmentIndex<segmentCount; segmentIndex++) {
            Segment segment = segmentArray[segmentIndex];
            if (segmentNoOverlapCounts[segmentIndex] == (segment.getWidth()*segment.getHeight())) {
                if (nonOverlappingSegmentIndex != null) {
                    throw new IllegalStateException("Mutliple non-overlapping segments");
                }
                nonOverlappingSegmentIndex = segmentIndex;
            }
        }
        if (nonOverlappingSegmentIndex == null) {
            throw new IllegalStateException("No non-overlapping segments");
        }
        return new BasicPuzzleResults<>(
            overlapCount,
            segmentArray[nonOverlappingSegmentIndex].getElfNumber()
        );
    }
}
