package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2021;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.common.CaptialLetterAsciiArtProcessor;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day13 implements IPuzzle {
    private static final class Point {
        private final int x;
        private final int y;

        private Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }
    }

    private static enum FoldType {
        ALONG_X,
        ALONG_Y
    }

    private static final class Fold {
        private final FoldType foldType;
        private final int coordinate;

        private Fold(FoldType foldType, int coordinate) {
            this.foldType = foldType;
            this.coordinate = coordinate;
        }

        public FoldType getFoldType() {
            return this.foldType;
        }

        public int getCoordinate() {
            return this.coordinate;
        }
    }

    private static final Pattern PATTERN_POINT = Pattern.compile("^(?<x>[0-9]+),(?<y>[0-9]+)$");
    private static final Pattern PATTERN_FOLD = Pattern.compile("^fold along (?<foldLine>[xy])=(?<coordinate>[0-9]+)$");

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        List<Point> pointsList = new ArrayList<>();
        List<Fold> foldsList = new ArrayList<>();
        boolean reachedSplit = false;
        int width = 0;
        int height = 0;
        for (String line : LineReader.strings(inputCharacters)) {
            if (reachedSplit) {
                Matcher matcher = PATTERN_FOLD.matcher(line);
                if (!matcher.matches()) {
                    throw new IllegalStateException("Could not parse fold");
                }
                String foldLine = matcher.group("foldLine");
                int coordinate = Integer.parseInt(matcher.group("coordinate"));
                int maxCoordinate = coordinate*2+1;
                FoldType foldType;
                switch (foldLine) {
                    case "x":
                        foldType = FoldType.ALONG_X;
                        width = Math.max(width,maxCoordinate);
                        break;
                    case "y":
                        foldType = FoldType.ALONG_Y;
                        height = Math.max(height,maxCoordinate);
                        break;
                    default:
                        throw new IllegalStateException("Unexpected fold line");
                }
                foldsList.add(new Fold(foldType,coordinate));
            } else if (line.isEmpty()) {
                reachedSplit = true;
            } else {
                Matcher matcher = PATTERN_POINT.matcher(line);
                if (!matcher.matches()) {
                    throw new IllegalStateException("Could not parse point");
                }
                int x = Integer.parseInt(matcher.group("x"));
                int y = Integer.parseInt(matcher.group("y"));
                width = Math.max(width,x+1);
                height = Math.max(height,y+1);
                pointsList.add(new Point(x,y));
            }
        }
        if (width < 1 || height < 1) {
            throw new IllegalStateException("Invalid size");
        }
        boolean[][] paper = new boolean[height][width];
        for (Point point : pointsList) {
            paper[point.getY()][point.getX()] = true;
        }
        int foldCount = foldsList.size();
        int firstFoldDotCount = 0;
        for (int foldIndex=0; foldIndex<foldCount; foldIndex++) {
            Fold fold = foldsList.get(foldIndex);
            FoldType foldType = fold.getFoldType();
            int coordinate = fold.getCoordinate();
            switch (foldType) {
                case ALONG_X:
                    if ((width/2) != coordinate || width % 2 != 1) {
                        throw new IllegalStateException("Even fold not requested, code needs rewriting");
                    }
                    for (int x1=coordinate-1, x2=coordinate+1; x1>=0; x1--, x2++) {
                        for (int y=0; y<height; y++) {
                            paper[y][x1] |= paper[y][x2];
                        }
                    }
                    width = coordinate;
                    break;
                case ALONG_Y:
                    if ((height/2) != coordinate || height % 2 != 1) {
                        throw new IllegalStateException("Even fold not requested, code needs rewriting");
                    }
                    for (int y1=coordinate-1, y2=coordinate+1; y1>=0; y1--, y2++) {
                        for (int x=0; x<width; x++) {
                            paper[y1][x] |= paper[y2][x];
                        }
                    }
                    height = coordinate;
                    break;
                default:
                    throw new IllegalStateException("Unexpected fold type");
            }
            if (foldIndex == 0) {
                firstFoldDotCount = countDots(paper,height,width);
            }
        }
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                printWriter.print(paper[y][x]?'X':' ');
            }
            printWriter.println();
        }
        return new BasicPuzzleResults<>(
            firstFoldDotCount,
            partBPotentiallyUnsolvable?null:CaptialLetterAsciiArtProcessor.parse(paper, height, width, "\n")
        );
    }

    private static int countDots(boolean[][] paper, int height, int width) {
        int dots = 0;
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                if (paper[y][x]) {
                    dots++;
                }
            }
        }
        return dots;
    }
}
