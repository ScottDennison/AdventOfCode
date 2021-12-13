package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2021;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Day09 implements IPuzzle {
    private static final int BASIN_SIZES_TO_USE_IN_PART_B = 3;

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        char[][] heightmapChars = LineReader.charArraysArray(inputCharacters, true);
        int width = heightmapChars[0].length;
        int height = heightmapChars.length;
        int[][] heightmap = new int[height][width];
        for (int y=0; y<height; y++) {
            if (heightmapChars[y].length != width) {
                throw new IllegalStateException("Jagged widths");
            }
            for (int x=0; x<width; x++) {
                char heightmapChar = heightmapChars[y][x];
                if (heightmapChar < '0' || heightmapChar > '9') {
                    throw new IllegalStateException("Invalid character");
                }
                heightmap[y][x] = heightmapChar-'0';
            }
        }
        int maxXWithNeighbours = width-2;
        int maxYWithNeighbours = height-2;
        int riskLevelSum = 0;
        List<Integer> basinSizes = new ArrayList<>();
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                int heightmapValue = heightmap[y][x];
                if (
                    (y < 1 || heightmap[y-1][x] > heightmapValue)
                    &&
                    (x < 1 || heightmap[y][x-1] > heightmapValue)
                    &&
                    (y > maxYWithNeighbours || heightmap[y+1][x] > heightmapValue)
                    &&
                    (x > maxXWithNeighbours || heightmap[y][x+1] > heightmapValue)
                ) {
                    riskLevelSum += heightmapValue+1;
                    boolean[][] inBasin = new boolean[height][width];
                    markBasin(heightmap,inBasin,height,width,y,x,heightmapValue-1);
                    int basinSize = 0;
                    for (int basinY=0; basinY<height; basinY++) {
                        for (int basinX=0; basinX<width; basinX++) {
                            if (inBasin[basinY][basinX]) {
                                basinSize++;
                            }
                        }
                    }
                    basinSizes.add(basinSize);
                }
            }
        }
        return new BasicPuzzleResults<>(
            riskLevelSum,
            basinSizes.stream().sorted(Collections.reverseOrder()).limit(BASIN_SIZES_TO_USE_IN_PART_B).mapToInt(Integer::intValue).reduce(1,Math::multiplyExact)
        );
    }

    private void markBasin(int[][] heightmap, boolean[][] inBasin, int height, int width, int y, int x, int previousHeightmapValue) {
        if (y >= 0 && y < height && x >= 0 && x < width && !inBasin[y][x]) {
            int thisHeightmapValue = heightmap[y][x];
            if (thisHeightmapValue > previousHeightmapValue && thisHeightmapValue < 9) {
                inBasin[y][x] = true;
                markBasin(heightmap, inBasin, height, width, y - 1, x, thisHeightmapValue);
                markBasin(heightmap, inBasin, height, width, y, x - 1, thisHeightmapValue);
                markBasin(heightmap, inBasin, height, width, y + 1, x, thisHeightmapValue);
                markBasin(heightmap, inBasin, height, width, y, x + 1, thisHeightmapValue);
            }
        }
    }
}
