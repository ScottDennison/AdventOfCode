package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2020;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Day24 implements IPuzzle {
    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        Map<Integer, Set<Integer>> blackTiles = new HashMap<>();
        for (char[] line : LineReader.charArrays(inputCharacters)) {
            int lineLength = line.length;
            int lineIndex = 0;
            int x = 0;
            int y = 0;
            while (lineIndex < lineLength) {
                char firstChar = line[lineIndex++];
                switch (firstChar) {
                    case 'n':
                    case 's':
                        if (lineIndex >= lineLength) {
                            throw new IllegalStateException("Incomplete direction");
                        }
                        char secondChar = line[lineIndex++];
                        switch (secondChar) {
                            case 'e':
                                x += 1;
                                break;
                            case 'w':
                                x -= 1;
                                break;
                            default:
                                throw new IllegalStateException("Unexpected character");
                        }
                        switch (firstChar) {
                            case 'n':
                                y -= 1;
                                break;
                            case 's':
                                y += 1;
                                break;
                        }
                        break;
                    case 'e':
                        x += 2;
                        break;
                    case 'w':
                        x -= 2;
                        break;
                    default:
                        throw new IllegalStateException("Unexpected characte");
                }
            }
            flipTile(blackTiles, y,x);
        }
        long day0BlackTileCount = countBlackTiles(blackTiles);
        int days = Integer.parseInt(new String(configProvider.getPuzzleConfigChars("days")).trim());
        for (int day=1; day<=days; day++) {
            int minY = Integer.MAX_VALUE;
            int maxY = Integer.MIN_VALUE;
            int minX = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            for (Map.Entry<Integer,Set<Integer>> blackTilesRowEntry : blackTiles.entrySet()) {
                int y = blackTilesRowEntry.getKey();
                minY = Math.min(minY,y);
                maxY = Math.max(maxY,y);
                for (int x : blackTilesRowEntry.getValue()) {
                    minX = Math.min(minX,x);
                    maxX = Math.max(maxX,x);
                }
            }
            minY -= 1;
            maxY += 1;
            minX -= 2;
            maxX += 2;
            Map<Integer,Set<Integer>> newBlackTiles = new HashMap<>();
            for (int y=minY; y<=maxY; y++) {
                for (int x=minX+(((minX+y)&1)==0?0:1); x<=maxX; x+=2) {
                    int blackNeighbours =
                        isTileBlack10(blackTiles,y,x-2)
                        +
                        isTileBlack10(blackTiles,y-1,x-1)
                        +
                        isTileBlack10(blackTiles,y-1,x+1)
                        +
                        isTileBlack10(blackTiles,y,x+2)
                        +
                        isTileBlack10(blackTiles,y+1,x+1)
                        +
                        isTileBlack10(blackTiles,y+1,x-1);
                    boolean oldTileBlack = isTileBlack(blackTiles,y,x);
                    boolean newTileBlack;
                    if (oldTileBlack && (blackNeighbours == 0 | blackNeighbours > 2)) {
                        newTileBlack = false;
                    } else if (!oldTileBlack && blackNeighbours == 2) {
                        newTileBlack = true;
                    } else {
                        newTileBlack= oldTileBlack;
                    }
                    if (newTileBlack) {
                        flipTile(newBlackTiles,y,x);
                    }
                }
            }
            blackTiles = newBlackTiles;
        }
        long dayNBlackTileCount = countBlackTiles(blackTiles);
        return new BasicPuzzleResults<>(
            day0BlackTileCount,
            dayNBlackTileCount
        );
    }

    private static long countBlackTiles(Map<Integer,Set<Integer>> blackTiles) {
        return blackTiles.values().stream().mapToInt(Set::size).sum();
    }

    private static void flipTile(Map<Integer,Set<Integer>> blackTiles, int y, int x) {
        Set<Integer> blackTilesInRow = blackTiles.get(y);
        if (blackTilesInRow == null) {
            blackTilesInRow = new HashSet<>();
            blackTiles.put(y,blackTilesInRow);
            blackTilesInRow.add(x);
        } else if (blackTilesInRow.remove(x)) {
            if (blackTilesInRow.isEmpty()) {
                blackTiles.remove(y);
            }
        } else {
            blackTilesInRow.add(x);
        }
    }

    private static boolean isTileBlack(Map<Integer,Set<Integer>> blackTiles, int y, int x) {
        Set<Integer> blackTilesInRow = blackTiles.get(y);
        if (blackTilesInRow == null) {
            return false;
        }
        return blackTilesInRow.contains(x);
    }

    private static int isTileBlack10(Map<Integer,Set<Integer>> blackTiles, int y, int x) {
        return isTileBlack(blackTiles,y,x)?1:0;
    }
}
