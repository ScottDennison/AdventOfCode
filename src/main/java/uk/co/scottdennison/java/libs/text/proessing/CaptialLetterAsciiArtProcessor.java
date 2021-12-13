package uk.co.scottdennison.java.libs.text.proessing;

import java.util.HashMap;
import java.util.Map;

public class CaptialLetterAsciiArtProcessor {
    private static final int CHARACTER_WIDTH = 5;
    private static final int CHARACTER_HEIGHT = 6;

    private static final Map<Integer,Character> BITFIELDS_TO_CHARACTERS = new HashMap<>();
    static {
        BITFIELDS_TO_CHARACTERS.put(0B011001001010010111101001010010, 'A');
        BITFIELDS_TO_CHARACTERS.put(0B111001001011100100101001011100, 'B');
        BITFIELDS_TO_CHARACTERS.put(0B011001001010000100001001001100, 'C');
        BITFIELDS_TO_CHARACTERS.put(0B111001001010010100101001011100, 'D');
        BITFIELDS_TO_CHARACTERS.put(0B111101000011100100001000011110, 'E');
        BITFIELDS_TO_CHARACTERS.put(0B111101000011100100001000010000, 'F');
        BITFIELDS_TO_CHARACTERS.put(0B011001001010000101101001001110, 'G');
        BITFIELDS_TO_CHARACTERS.put(0B100101001011110100101001010010, 'H');
        BITFIELDS_TO_CHARACTERS.put(0B011100010000100001000010001110, 'I');
        BITFIELDS_TO_CHARACTERS.put(0B001100001000010000101001001100, 'J');
        BITFIELDS_TO_CHARACTERS.put(0B100101010011000101001010010010, 'K');
        BITFIELDS_TO_CHARACTERS.put(0B100001000010000100001000011110, 'L');
        BITFIELDS_TO_CHARACTERS.put(0B100011101110101100011000110001, 'M');
        BITFIELDS_TO_CHARACTERS.put(0B100011100110101101011001110001, 'N');
        BITFIELDS_TO_CHARACTERS.put(0B011001001010010100101001001100, 'O');
        BITFIELDS_TO_CHARACTERS.put(0B111001001010010111001000010000, 'P');
        BITFIELDS_TO_CHARACTERS.put(0B011001001010010101101001001101, 'Q');
        BITFIELDS_TO_CHARACTERS.put(0B111001001010010111001010010010, 'R');
        BITFIELDS_TO_CHARACTERS.put(0B011101000010000011000001011100, 'S');
        BITFIELDS_TO_CHARACTERS.put(0B111110010000100001000010000100, 'T');
        BITFIELDS_TO_CHARACTERS.put(0B100101001010010100101001001100, 'U');
        BITFIELDS_TO_CHARACTERS.put(0B100011000110001010100101000100, 'V');
        BITFIELDS_TO_CHARACTERS.put(0B100011000110101101011010101010, 'W');
        BITFIELDS_TO_CHARACTERS.put(0B100010101000100010100101010001, 'X');
        BITFIELDS_TO_CHARACTERS.put(0B100011000101010001000010000100, 'Y');
        BITFIELDS_TO_CHARACTERS.put(0B111100001000100010001000011110, 'Z');
        BITFIELDS_TO_CHARACTERS.put(0B000000000000000000000000000000, ' ');
    }

    public static char[][] parse(boolean[][] grid, int gridHeight, int gridWidth) {
        if (gridHeight % CHARACTER_HEIGHT != 0 || gridWidth % CHARACTER_WIDTH != 0) {
            throw new IllegalStateException("Grid does not evenly contain characters");
        }
        int charactersWide = gridWidth/CHARACTER_WIDTH;
        int charactersTall = gridHeight/CHARACTER_HEIGHT;
        if (charactersWide < 1 || charactersTall < 1) {
            throw new IllegalStateException("No characters");
        }
        char[][] characters = new char[charactersTall][charactersWide];
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
    }

    public static String parse(boolean[][] grid, int gridHeight, int gridWidth, String lineSeparator) {
        char[] lineSeparatorChars = lineSeparator.toCharArray();
        int lineSeparatorCharCount = lineSeparatorChars.length;;
        char[][] characters = parse(grid, gridHeight, gridWidth);
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
