package uk.co.scottdennison.java.soft.challenges.adventofcode.common;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

public class AsciiArtProcessor {
    private interface FontDefinitionContainer {
        FontDefinition getFontDefinition();
    }

    public static class FontDefinition implements FontDefinitionContainer {
        public static class CharacterDefinition {
            private final char character;
            private final boolean[][] grid;

            private CharacterDefinition(char character, boolean[][] grid) {
                this.character = character;
                this.grid = grid;
            }

            private char getCharacter() {
                return this.character;
            }

            private boolean isSetAt(int y, int x) {
                return this.grid[y][x];
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

        private int getCharacterWidth() {
            return this.characterWidth;
        }

        private int getCharacterHeight() {
            return this.characterHeight;
        }

        private int getSpaceBetweenHorizontalCharacters() {
            return this.spaceBetweenHorizontalCharacters;
        }

        private int getSpaceBetweenVerticalCharacters() {
            return this.spaceBetweenVerticalCharacters;
        }

        private CharacterDefinition[] getCharacterDefinitions() {
            return this.characterDefinitions;
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
                    grid
                );
            }
            characterDefinitions[characterDataEntryCount] = new CharacterDefinition(
                ' ',
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
            FontDefinition.create(6, 10, 2, 2, "A=..##...#..#.#....##....##....########....##....##....##....#|B=#####.#....##....##....######.#....##....##....##....######.|C=.####.#....##.....#.....#.....#.....#.....#.....#....#.####.|E=#######.....#.....#.....#####.#.....#.....#.....#.....######|F=#######.....#.....#.....#####.#.....#.....#.....#.....#.....|G=.####.#....##.....#.....#.....#..####....##....##...##.###.#|H=#....##....##....##....########....##....##....##....##....#|J=...###....#.....#.....#.....#.....#.....#.#...#.#...#..###..|K=#....##...#.#..#..#.#...##....##....#.#...#..#..#...#.#....#|L=#.....#.....#.....#.....#.....#.....#.....#.....#.....######|N=#....###...###...##.#..##.#..##..#.##..#.##...###...###....#|P=#####.#....##....##....######.#.....#.....#.....#.....#.....|R=#####.#....##....##....######.#..#..#...#.#...#.#....##....#|X=#....##....#.#..#..#..#...##....##...#..#..#..#.#....##....#|Z=######.....#.....#....#....#....#....#....#.....#.....######")
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

    private static interface CalculateMarginGridLookup {
        boolean lookup(int c1, int c2);
    }

    private static int calculateMargin(int c1StartInclusive, int c1EndExclusive, int c1Delta, int c2Size, CalculateMarginGridLookup calculateMarginGridLookup) {
        int margin = 0;
        for (int c1=c1StartInclusive; c1<c1EndExclusive; c1+=c1Delta) {
            for (int c2=0; c2<c2Size; c2++) {
                if (calculateMarginGridLookup.lookup(c1, c2)) {
                    return margin;
                }
            }
            margin++;
        }
        return margin;
    }

    public static char[][] parse(boolean[][] grid, int gridHeight, int gridWidth, FontDefinitionContainer fontDefinitionContainer) {
        FontDefinition fontDefinition = fontDefinitionContainer.getFontDefinition();
        int shrunkGridMinY = calculateMargin(0, gridHeight, 1, gridWidth , (y, x) -> grid[y][x]);
        int shrunkGridMinX = calculateMargin(0, gridWidth, 1, gridHeight, (x, y) -> grid[y][x]);
        int shrunkGridMaxY = gridHeight - calculateMargin(gridHeight - 1, -1, -1, gridWidth , (y, x) -> grid[y][x])- 1;
        int shrunkGridMaxX = gridWidth - calculateMargin(gridWidth - 1, -1, -1, gridHeight, (x, y) -> grid[y][x]) - 1;
        int shrunkGridHeight = shrunkGridMaxY - shrunkGridMinY + 1;
        int shrunkGridWidth = shrunkGridMaxX - shrunkGridMinX + 1;
        int fontSpaceBetweenVerticalCharacters = fontDefinition.getSpaceBetweenVerticalCharacters();
        int fontSpaceBetweenHorizontalCharacters = fontDefinition.getSpaceBetweenVerticalCharacters();
        int characterHeight = fontDefinition.getCharacterHeight();
        int characterWidth = fontDefinition.getCharacterWidth();
        int characterHeightIncludingVerticalSpace = fontSpaceBetweenVerticalCharacters + characterHeight;
        int characterWidthIncludingHorizontalSpace = fontSpaceBetweenHorizontalCharacters + characterWidth;
        LinkedList<FontDefinition.CharacterDefinition> allCharacterDefinitions = new LinkedList<>(Arrays.asList(fontDefinition.getCharacterDefinitions()));
        char[][] validCharacters = null;
        topCharacterExtraSpaceLoop:
        for (int topCharacterExtraSpace=0; topCharacterExtraSpace<characterHeight; topCharacterExtraSpace++) {
            int virtualGridHeight = shrunkGridHeight + topCharacterExtraSpace;
            int virtualGridMinY = shrunkGridMinY - topCharacterExtraSpace;
            int virtualGridHeightIncludingAdditionalVerticalSpace = virtualGridHeight + fontSpaceBetweenVerticalCharacters;
            int charactersTall = virtualGridHeightIncludingAdditionalVerticalSpace / characterHeightIncludingVerticalSpace;
            if ((charactersTall * characterHeightIncludingVerticalSpace) < virtualGridHeightIncludingAdditionalVerticalSpace) {
                charactersTall++;
            }
            leftCharacterExtraSpaceLoop:
            for (int leftCharacterExtraSpace=0; leftCharacterExtraSpace<characterWidth; leftCharacterExtraSpace++) {
                int virtualGridWidth = shrunkGridWidth + leftCharacterExtraSpace;
                int virtualGridStartX = shrunkGridMinX - leftCharacterExtraSpace;
                int virtualGridWidthIncludingAdditionalHorizontalSpace = virtualGridWidth + fontSpaceBetweenHorizontalCharacters;
                int charactersWide = virtualGridWidthIncludingAdditionalHorizontalSpace / characterWidthIncludingHorizontalSpace;
                if ((charactersWide * characterWidthIncludingHorizontalSpace) < virtualGridWidthIncludingAdditionalHorizontalSpace) {
                    charactersWide++;
                }
                char[][] characters = new char[charactersTall][charactersWide];
                for (int characterY=0, virtualGridCharacterStartY=virtualGridMinY; characterY<charactersTall; characterY++, virtualGridCharacterStartY += characterHeightIncludingVerticalSpace) {
                    for (int characterX=0, virtualGridCharacterStartX=virtualGridStartX; characterX<charactersWide; characterX++, virtualGridCharacterStartX += characterWidthIncludingHorizontalSpace) {
                        LinkedList<FontDefinition.CharacterDefinition> possibleCharacterDefinitions = new LinkedList<>(allCharacterDefinitions);
                        for (int characterGridY=0, virtualGridY=virtualGridCharacterStartY; characterGridY<characterHeight; characterGridY++, virtualGridY++) {
                            for (int characterGridX=0, virtualGridX=virtualGridCharacterStartX; characterGridX<characterWidth; characterGridX++, virtualGridX++) {
                                boolean gridSet;
                                if (virtualGridY < shrunkGridMinY || virtualGridY > shrunkGridMaxY || virtualGridX < shrunkGridMinX || virtualGridX > shrunkGridMaxX) {
                                    gridSet = false;
                                }
                                else {
                                    gridSet = grid[virtualGridY][virtualGridX];
                                }
                                Iterator<FontDefinition.CharacterDefinition> characterDefinitionIterator = possibleCharacterDefinitions.iterator();
                                if (!characterDefinitionIterator.hasNext()) {
                                    continue leftCharacterExtraSpaceLoop;
                                }
                                while (characterDefinitionIterator.hasNext()) {
                                    FontDefinition.CharacterDefinition characterDefinition = characterDefinitionIterator.next();
                                    if (characterDefinition.isSetAt(characterGridY, characterGridX) != gridSet) {
                                        characterDefinitionIterator.remove();
                                    }
                                }
                            }
                        }
                        Iterator<FontDefinition.CharacterDefinition> characterDefinitionIterator = possibleCharacterDefinitions.iterator();
                        if (!characterDefinitionIterator.hasNext()) {
                            continue leftCharacterExtraSpaceLoop;
                        }
                        char character = characterDefinitionIterator.next().getCharacter();
                        if (characterDefinitionIterator.hasNext()) {
                            continue leftCharacterExtraSpaceLoop;
                        }
                        characters[characterY][characterX] = character;
                    }
                }
                if (validCharacters != null) {
                    throw new IllegalStateException("Found multiple possible character grids.");
                }
                validCharacters = characters;
            }
        }
        if (validCharacters == null) {
            throw new IllegalStateException("Found no possible character grids.");
        }
        return validCharacters;
    }

    public static String parse(boolean[][] grid, int gridHeight, int gridWidth, String lineSeparator, FontDefinitionContainer fontDefinitionContainer) {
        char[][] characters = parse(grid, gridHeight, gridWidth, fontDefinitionContainer);
        int height = characters.length;
        if (height == 1) {
            return new String(characters[0]);
        }
        int width = characters[0].length;
        char[] lineSeparatorChars = lineSeparator.toCharArray();
        int lineSeparatorCharCount = lineSeparatorChars.length;;
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

    public static char[] parseSingleLine(boolean[][] grid, int gridHeight, int gridWidth, FontDefinitionContainer fontDefinitionContainer) {
        char[][] characters = parse(grid, gridHeight, gridWidth, fontDefinitionContainer);
        if (characters.length != 1) {
            throw new IllegalStateException("Expected only a single line of characters");
        }
        return characters[0];
    }

    public static String parseSingleLineAsString(boolean[][] grid, int gridHeight, int gridWidth, FontDefinitionContainer fontDefinitionContainer) {
        return new String(parseSingleLine(grid, gridHeight, gridWidth, fontDefinitionContainer));
    }
}
