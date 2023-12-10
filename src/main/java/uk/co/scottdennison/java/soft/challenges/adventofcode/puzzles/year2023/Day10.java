package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2023;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.ToIntFunction;

public class Day10 implements IPuzzle {
    private static class TileLinkDelta {
        private final int yDelta;
        private final int xDelta;

        public TileLinkDelta(int yDelta, int xDelta) {
            this.yDelta = yDelta;
            this.xDelta = xDelta;
        }

        public int getYDelta() {
            return this.yDelta;
        }

        public int getXDelta() {
            return this.xDelta;
        }
    }

    private static enum TileType implements Iterable<TileLinkDelta> {
        VERTICAL   ('|', new TileLinkDelta(-1,  0), new TileLinkDelta( 1,  0)),
        HORIZONTAL ('-', new TileLinkDelta( 0, -1), new TileLinkDelta( 0,  1)),
        BEND_NE    ('L', new TileLinkDelta(-1,  0), new TileLinkDelta( 0,  1)),
        BEND_NW    ('J', new TileLinkDelta(-1,  0), new TileLinkDelta( 0, -1)),
        BEND_SW    ('7', new TileLinkDelta( 1,  0), new TileLinkDelta( 0, -1)),
        BEND_SE    ('F', new TileLinkDelta( 1,  0), new TileLinkDelta( 0,  1)),
        GROUND     ('.', new TileLinkDelta( 0,  0), new TileLinkDelta( 0,  0));

        private static final Map<Character,TileType> CHARACTER_TO_TILE_TYPE_MAP = new HashMap<>();
        static {
            for (TileType tileType : TileType.values()) {
                if (CHARACTER_TO_TILE_TYPE_MAP.put(tileType.character, tileType) != null) {
                    throw new IllegalStateException("Duplicate character");
                }
            }
        }

        public static TileType getTileTypeForCharacter(char character) {
            TileType tileType = CHARACTER_TO_TILE_TYPE_MAP.get(character);
            if (tileType == null) {
                throw new IllegalStateException("No such character is known.");
            }
            return tileType;
        }

        private final char character;
        private final TileLinkDelta tileLinkDelta1;
        private final TileLinkDelta tileLinkDelta2;

        TileType(char character, TileLinkDelta tileLinkDelta1, TileLinkDelta tileLinkDelta2) {
            this.character = character;
            this.tileLinkDelta1 = tileLinkDelta1;
            this.tileLinkDelta2 = tileLinkDelta2;
        }


        @Override
        public Iterator<TileLinkDelta> iterator() {
            return new Iterator<TileLinkDelta>() {
                private int state = 0;

                @Override
                public boolean hasNext() {
                    return state == 0 || state == 1;
                }

                @Override
                public TileLinkDelta next() {
                    switch (state++) {
                        case 0:
                            return TileType.this.tileLinkDelta1;
                        case 1:
                            return TileType.this.tileLinkDelta2;
                        default:
                            throw new IllegalStateException("No more entries");
                    }
                }
            };
        }
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        char[][] charGrid = LineReader.charArraysArray(inputCharacters, true);
        int height = charGrid.length;
        int width = charGrid[0].length;
        TileType[][] tileTypeGrid = new TileType[height][width];
        int startY = 0;
        int startX = 0;
        boolean startFound = false;
        for (int y=0; y<height; y++) {
            char[] charGridRow = charGrid[y];
            TileType[] tileTypeGridRow = tileTypeGrid[y];
            for (int x=0; x<width; x++) {
                char character = charGridRow[x];
                if (character == 'S') {
                    if (startFound) {
                        throw new IllegalStateException("Multiple starts found");
                    }
                    startFound = true;
                    startY = y;
                    startX = x;
                } else {
                    tileTypeGridRow[x] = TileType.getTileTypeForCharacter(character);
                }
            }
        }
        if (!startFound) {
            throw new IllegalStateException("No start found.");
        }
        TileType startTileType = null;
        for (TileType potentialStartTileType : TileType.values()) {
            int linkBacks = 0;
            for (TileLinkDelta startTileLinkDelta : potentialStartTileType) {
                int linkedTileY = startY+startTileLinkDelta.getYDelta();
                int linkedTileX = startX+startTileLinkDelta.getXDelta();
                if ((linkedTileY == startY && linkedTileX == startX) || linkedTileY < 0 || linkedTileY >= height || linkedTileX < 0 || linkedTileX >= width) {
                    continue;
                }
                TileType linkedTile = tileTypeGrid[linkedTileY][linkedTileX];
                for (TileLinkDelta linkedTileLinkDelta : linkedTile) {
                    int linkedLinkedTileY = linkedTileY+linkedTileLinkDelta.getYDelta();
                    int linkedLinkedTileX = linkedTileX+linkedTileLinkDelta.getXDelta();
                    if (linkedLinkedTileY == startY && linkedLinkedTileX == startX) {
                        linkBacks++;
                        break;
                    }
                }
            }
            if (linkBacks == 2) {
                if (startTileType != null) {
                    throw new IllegalStateException("Multiple potential start tile types found.");
                }
                startTileType = potentialStartTileType;
            }
        }
        if (startTileType == null) {
            throw new IllegalStateException("No potential start tile types found.");
        }
        tileTypeGrid[startY][startX] = startTileType;
        int currentY = startY;
        int currentX = startX;
        TileLinkDelta oneOfStartTileLinkDeltas = startTileType.iterator().next();
        int previousY = currentY+oneOfStartTileLinkDeltas.getYDelta();
        int previousX = currentX+oneOfStartTileLinkDeltas.getXDelta();
        int pathLength = 0;
        do {
            for (TileLinkDelta tileLinkDelta : tileTypeGrid[currentY][currentX]) {
                int newY = currentY+tileLinkDelta.getYDelta();
                int newX = currentX+tileLinkDelta.getXDelta();
                if (!(previousY == newY && previousX == newX)) {
                    previousY = currentY;
                    previousX = currentX;
                    currentY = newY;
                    currentX = newX;
                    pathLength++;
                    break;
                }
            }
        } while (!(currentY == startY && currentX == startX));
        return new BasicPuzzleResults<>(
            pathLength/2,
            null
        );
    }
}
