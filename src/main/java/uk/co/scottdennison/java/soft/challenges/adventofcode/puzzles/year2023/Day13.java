package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2023;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Day13 implements IPuzzle {
    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        List<List<char[]>> inputs = new ArrayList<>();
        List<char[]> currentInput = new ArrayList<>();
        inputs.add(currentInput);
        for (char[] inputLine : LineReader.charArrays(inputCharacters)) {
            if (inputLine.length == 0) {
                currentInput = new ArrayList<>();
                inputs.add(currentInput);
            } else {
                currentInput.add(inputLine);
            }
        }
        long partASummary = 0;
        long partBSummary = 0;
        int inputCount = inputs.size();
        for (int inputIndex=0; inputIndex<inputCount; inputIndex++) {
            List<char[]> lines = inputs.get(inputIndex);
            int height = lines.size();
            int width = lines.get(0).length;
            boolean[][] grid = new boolean[height][width];
            for (int y=0; y<height; y++) {
                char[] chars = lines.get(y);
                boolean[] gridRow = grid[y];
                if (chars.length != width) {
                    throw new IllegalStateException("Jagged grid");
                }
                for (int x=0; x<width; x++) {
                    boolean value;
                    switch (chars[x]) {
                        case '#':
                            value = true;
                            break;
                        case '.':
                            value = false;
                            break;
                        default:
                            throw new IllegalStateException("Unexpected character");
                    }
                    gridRow[x] = value;
                }
            }
            Integer partAInputSummary = findMirror(grid,height,width,null);
            if (partAInputSummary == null) {
                throw new IllegalStateException("Unable to find mirror for part A for grid " + (inputIndex+1));
            }
            Integer partBInputSummary = null;
            for (int smudgeY=0; smudgeY<height; smudgeY++) {
                boolean[] gridRow = grid[smudgeY];
                for (int smudgeX=0; smudgeX<width; smudgeX++) {
                    gridRow[smudgeX] = !gridRow[smudgeX];
                    Integer partBInputSummarySmudged = findMirror(grid, height, width, partAInputSummary);
                    if (partBInputSummarySmudged != null) {
                        if (partBInputSummary != null && !Objects.equals(partBInputSummarySmudged,partBInputSummary)) {
                            throw new IllegalStateException("Multiple smudges result in a new summary for grid " + (inputIndex+1) + ", new summary=" + partBInputSummarySmudged + ", old summary=" + partBInputSummary + ", part A summary=" + partAInputSummary);
                        }
                        partBInputSummary = partBInputSummarySmudged;
                    }
                    gridRow[smudgeX] = !gridRow[smudgeX];
                }
            }
            if (partBInputSummary == null) {
                throw new IllegalStateException("Unable to find smudge location for part B for grid " + (inputIndex+1));
            }
            partASummary += partAInputSummary;
            partBSummary += partBInputSummary;
        }
        return new BasicPuzzleResults<>(
            partASummary,
            partBSummary
        );
    }

    private static Integer findMirror(boolean[][] grid, int height, int width, Integer resultToSkip) {
        Set<Integer> possibleResults = new HashSet<>();
        for (int mirrorY=1; mirrorY<height; mirrorY++) {
            boolean mirrored = true;
            for (int y1=mirrorY-1, y2=mirrorY; mirrored && y1>=0 && y2<height; y1--, y2++) {
                for (int x=0; x<width; x++) {
                    if (grid[y1][x] != grid[y2][x]) {
                        mirrored = false;
                        break;
                    }
                }
            }
            if (mirrored) {
                possibleResults.add(mirrorY * 100);
            }
        }
        for (int mirrorX=1; mirrorX<width; mirrorX++) {
            boolean mirrored = true;
            for (int x1=mirrorX-1, x2=mirrorX; mirrored && x1>=0 && x2<width; x1--, x2++) {
                for (int y=0; y<height; y++) {
                    if (grid[y][x1] != grid[y][x2]) {
                        mirrored = false;
                        break;
                    }
                }
            }
            if (mirrored) {
                possibleResults.add(mirrorX);
            }
        }
        possibleResults.remove(resultToSkip);
        if (possibleResults.size() == 1) {
            return possibleResults.iterator().next();
        }
        else {
            return null;
        }
    }
}
