package uk.co.scottdennison.java.soft.challenges.adventofcode.common;

public class AsciiArtProcessor {
    private interface FontDefinitionContainer {
        FontDefinition getFontDefinition();
    }

    private static class Margins {
        private final int leftMargin;
        private final int rightMargin;
        private final int topMargin;
        private final int bottomMargin;

        public Margins(int leftMargin, int rightMargin, int topMargin, int bottomMargin) {
            this.leftMargin = leftMargin;
            this.rightMargin = rightMargin;
            this.topMargin = topMargin;
            this.bottomMargin = bottomMargin;
        }

        public int getLeftMargin() {
            return this.leftMargin;
        }

        public int getRightMargin() {
            return this.rightMargin;
        }

        public int getTopMargin() {
            return this.topMargin;
        }

        public int getBottomMargin() {
            return this.bottomMargin;
        }

        private static interface GridLookup {
            boolean lookup(int c1, int c2);
        }

        private static int calculateMargin(int c1StartInclusive, int c1EndExclusive, int c1Delta, int c2Size, GridLookup gridLookup) {
            int margin = 0;
            for (int c1=c1StartInclusive; c1<c1EndExclusive; c1+=c1Delta) {
                for (int c2=0; c2<c2Size; c2++) {
                    if (gridLookup.lookup(c1, c2)) {
                        return margin;
                    }
                }
                margin++;
            }
            return margin;
        }

        public static Margins createFromGrid(boolean[][] grid, int gridHeight, int gridWidth) {
            return new Margins(
                calculateMargin(0, gridWidth, 1, gridHeight, (x, y) -> grid[y][x]),
                calculateMargin(gridWidth - 1, -1, -1, gridHeight, (x, y) -> grid[y][x]),
                calculateMargin(0, gridHeight, 1, gridWidth , (y, x) -> grid[y][x]),
                calculateMargin(gridHeight - 1, -1, -1, gridWidth , (y, x) -> grid[y][x])
            );
        }
    }

    public static class FontDefinition implements FontDefinitionContainer {
        public static class CharacterDefinition {
            private final char character;
            private final Margins margins;
            private final boolean[][] grid;

            private CharacterDefinition(char character, Margins margins, boolean[][] grid) {
                this.character = character;
                this.margins = margins;
                this.grid = grid;
            }
        }

        private final int characterWidth;
        private final int characterHeight;
        private final int spaceBetweenHorizontalCharacters;
        private final int spaceBetweenVerticalCharacters;
        private CharacterDefinition[] characterDefinitions;

        private FontDefinition(int characterWidth, int characterHeight, int spaceBetweenHorizontalCharacters, int spaceBetweenVerticalCharacters, CharacterDefinition[] characterDefinitions) {
            this.characterWidth = characterWidth;
            this.characterHeight = characterHeight;
            this.spaceBetweenHorizontalCharacters = spaceBetweenHorizontalCharacters;
            this.spaceBetweenVerticalCharacters = spaceBetweenVerticalCharacters;
            this.characterDefinitions = characterDefinitions;
        }

        @Override
        public FontDefinition getFontDefinition() {
            return this;
        }

        public static FontDefinition create(int characterWidth, int characterHeight, int spaceBetweenHorizontalCharacters, int spaceBetweenVerticalCharacters, String characterData) {
            int expectedCharacterDataEntryLength = (characterWidth * characterHeight) + 2;
            String[] characterDataEntries = characterData.split("\\|");
            int characterDataEntryCount = characterDataEntries.length;
            CharacterDefinition[] characterDefinitions = new CharacterDefinition[characterDataEntryCount+1];
            for (int characterDataEntryIndex = 0; characterDataEntryIndex < characterDataEntryCount; characterDataEntryIndex++) {
                char[] characterDataEntry = characterDataEntries[characterDataEntryIndex].toCharArray();
                boolean[][] grid = new boolean[characterHeight][characterWidth];
                if (characterDataEntry.length != expectedCharacterDataEntryLength || characterDataEntry[1] != '=') {
                    throw new IllegalStateException("Character data entry malformed");
                }
                for (int characterDataEntryCharIndex=2, gridY=0, gridX=0; characterDataEntryCharIndex<expectedCharacterDataEntryLength; characterDataEntryCharIndex++) {
                    boolean gridValue;
                    switch (characterDataEntry[characterDataEntryCharIndex]) {
                        case '#':
                            gridValue = true;
                            break;
                        case '.':
                            gridValue = false;
                            break;
                        default:
                            throw new IllegalStateException("Character data entry malformed");
                    }
                    grid[gridY][gridX] = gridValue;
                    if (++gridX == characterWidth) {
                        gridX = 0;
                        gridY++;
                    }
                }
                characterDefinitions[characterDataEntryIndex] = new CharacterDefinition(
                    characterDataEntry[0],
                    Margins.createFromGrid(grid, characterHeight, characterWidth),
                    grid
                );
            }
            characterDefinitions[characterDataEntryCount] = new CharacterDefinition(
                ' ',
                new Margins(0, 0, 0, 0),
                new boolean[characterHeight][characterWidth]
            );
            return new FontDefinition(
                characterWidth,
                characterHeight,
                spaceBetweenHorizontalCharacters,
                spaceBetweenVerticalCharacters,
                characterDefinitions
            );
        }
    }

    public static enum StandardFontDefinition implements FontDefinitionContainer {
        STANDARD_5_6 (
            FontDefinition.create(5, 6, 0, 0, "A=.##..#..#.#..#.####.#..#.#..#.|B=###..#..#.###..#..#.#..#.###..|C=.##..#..#.#....#....#..#..##..|D=###..#..#.#..#.#..#.#..#.###..|E=####.#....###..#....#....####.|F=####.#....###..#....#....#....|G=.##..#..#.#....#.##.#..#..###.|H=#..#.#..#.####.#..#.#..#.#..#.|I=.###...#....#....#....#...###.|J=..##....#....#....#.#..#..##..|K=#..#.#.#..##...#.#..#.#..#..#.|L=#....#....#....#....#....####.|M=#...###.###.#.##...##...##...#|N=#...###..##.#.##.#.##..###...#|O=.##..#..#.#..#.#..#.#..#..##..|P=###..#..#.#..#.###..#....#....|Q=.##..#..#.#..#.#.##.#..#..##.#|R=###..#..#.#..#.###..#.#..#..#.|S=.###.#....#.....##.....#.###..|T=#####..#....#....#....#....#..|U=#..#.#..#.#..#.#..#.#..#..##..|V=#...##...##...#.#.#..#.#...#..|W=#...##...##.#.##.#.##.#.#.#.#.|X=#...#.#.#...#...#.#..#.#.#...#|Y=#...##...#.#.#...#....#....#..|Z=####....#...#...#...#....####.")
        ),
        LARGE_5_8 (
            FontDefinition.create(5, 8, 1, 1, "H=#...##...##...#######...##...##...##...#|I=.###...#....#....#....#....#....#...###.")
        ),
        LARGE_6_10 (
            FontDefinition.create(5, 6, 2, 2, "A=..##...#..#.#....##....##....########....##....##....##....#|B=#####.#....##....##....######.#....##....##....##....######.|C=.####.#....##.....#.....#.....#.....#.....#.....#....#.####.|E=#######.....#.....#.....#####.#.....#.....#.....#.....######|F=#######.....#.....#.....#####.#.....#.....#.....#.....#.....|G=.####.#....##.....#.....#.....#..####....##....##...##.###.#|H=#....##....##....##....########....##....##....##....##....#|J=...###....#.....#.....#.....#.....#.....#.#...#.#...#..###..|K=#....##...#.#..#..#.#...##....##....#.#...#..#..#...#.#....#|L=#.....#.....#.....#.....#.....#.....#.....#.....#.....######|N=#....###...###...##.#..##.#..##..#.##..#.##...###...###....#|P=#####.#....##....##....######.#.....#.....#.....#.....#.....|R=#####.#....##....##....######.#..#..#...#.#...#.#....##....#|X=#....##....#.#..#..#..#...##....##...#..#..#..#.#....##....#|Z=######.....#.....#....#....#....#....#....#.....#.....######")
        );

        private FontDefinition fontDefinition;

        StandardFontDefinition(FontDefinition fontDefinition) {
            this.fontDefinition = fontDefinition;
        }

        @Override
        public FontDefinition getFontDefinition() {
            return this.fontDefinition;
        }
    }


    public static char[][] parse(boolean[][] grid, int gridHeight, int gridWidth, FontDefinitionContainer fontDefinitionContainer) {
        Margins margins = Margins.createFromGrid(grid, gridHeight, gridWidth);

        // WE hae trimmed the grid, we will ALWAYS be looking at margins
        /*

        for (int characterY=0, gridStartY=0; characterY<charactersTall; characterY++, gridStartY+=CHARACTER_HEIGHT) {
            for (int characterX=0, gridStartX=0; characterX<charactersWide; characterX++, gridStartX+=CHARACTER_WIDTH) {
                int bitfield = 0;
                for (int gridOffsetY=0, gridY=gridStartY; gridOffsetY<CHARACTER_HEIGHT; gridOffsetY++, gridY++) {
                    for (int gridOffsetX=0, gridX=gridStartX; gridOffsetX<CHARACTER_WIDTH; gridOffsetX++, gridX++) {
                        bitfield <<= 1;
                        if (grid[gridY][gridX]) {
                            bitfield |= 1;
                        }
                    }
                }
                Character character = BITFIELDS_TO_CHARACTERS.get(bitfield);
                if (character == null) {
                    throw new IllegalStateException("No matching character");
                }
                characters[characterY][characterX] = character;
            }
        }
        return characters;
         */
        return null;
    }

    public static String parse(boolean[][] grid, int gridHeight, int gridWidth, String lineSeparator, FontDefinitionContainer fontDefinitionContainer) {
        char[] lineSeparatorChars = lineSeparator.toCharArray();
        int lineSeparatorCharCount = lineSeparatorChars.length;;
        char[][] characters = parse(grid, gridHeight, gridWidth, fontDefinitionContainer);
        int height = characters.length;
        if (height == 1) {
            return new String(characters[0]);
        }
        int width = characters[0].length;
        char[] flatCharacters = new char[((width+lineSeparatorCharCount)*height)-lineSeparatorCharCount];
        int flatCharacterIndex = 0;
        for (int lineIndex=0; lineIndex<height; lineIndex++) {
            if (lineIndex != 0) {
                System.arraycopy(lineSeparatorChars,0,flatCharacters,flatCharacterIndex,lineSeparatorCharCount);
                flatCharacterIndex += lineSeparatorCharCount;
            }
            System.arraycopy(characters[lineIndex],0,flatCharacters,flatCharacterIndex,width);
            flatCharacterIndex += width;
        }
        return new String(flatCharacters);
    }
}
