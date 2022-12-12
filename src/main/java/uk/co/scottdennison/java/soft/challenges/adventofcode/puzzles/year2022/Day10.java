package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2022;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.common.CaptialLetterAsciiArtProcessor;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;

public class Day10 implements IPuzzle {
    // So, I expected part B to be sum the signal strength after cycles (insert series of absurdley high numbers here), and thus designed the solution to not have to run the program many many times. Unfortunately, part 2 was ...not... that.

    private static final int INITIAL_X = 1;
    private static final int[] PART_A_CYCLE_NUMBERS = {20,60,100,140,180,220};
    private static final int PART_B_SPRITE_OFFSET = -1;
    private static final int PART_B_SPRITE_WIDTH = 3;
    private static final int PART_B_SCREEN_WIDTH = 40;
    private static final int PART_B_SCREEN_HEIGHT = 6;

    public enum OutputType {
        SCREEN {
            @Override
            public String produceOutput(boolean[][] crtOutput, int height, int width) {
                char[] visualCrtOutput = new char[height*(width+1)];
                int charIndex = 0;
                for (int y=0; y<PART_B_SCREEN_HEIGHT; y++) {
                    boolean[] crtOutputRow = crtOutput[y];
                    for (int x=0; x<PART_B_SCREEN_WIDTH; x++) {
                        visualCrtOutput[charIndex++] = crtOutputRow[x]?'#':'.';
                    }
                    visualCrtOutput[charIndex++] = '\n';
                }
                return new String(visualCrtOutput);
            }
        },
        TEXT {
            @Override
            public String produceOutput(boolean[][] crtOutput, int height, int width) {
                char[][] output = CaptialLetterAsciiArtProcessor.parse(crtOutput,height,width);
                if (output.length != 1) {
                    throw new IllegalStateException("Expected only one line of output.");
                }
                return new String(output[0]);
            }
        };

        public abstract String produceOutput(boolean[][] crtOutput, int height, int width);
    }

    private static int getXAfterCycle(int[] xIncreaseFromStartOfProgramToAfterCycleY, int cyclesPerProgram, int cycleNumber) {
        int cycleIndex = cycleNumber-1;
        if (cycleIndex < 0) {
            return INITIAL_X;
        } else {
            return ((((cycleIndex / cyclesPerProgram) * xIncreaseFromStartOfProgramToAfterCycleY[cyclesPerProgram - 1]) + xIncreaseFromStartOfProgramToAfterCycleY[cycleIndex % cyclesPerProgram]) + INITIAL_X);
        }
    }

    private static int getXDuringCycle(int[] xIncreaseFromStartOfProgramToAfterCycleY, int cyclesPerProgram, int cycleNumber) {
        return getXAfterCycle(xIncreaseFromStartOfProgramToAfterCycleY, cyclesPerProgram, cycleNumber-1);
    }

    private static int sumStrengthPerCycles(int[] xIncreaseFromStartOfProgramToAfterCycleY, int cyclesPerProgram, int... cycleNumbers) {
        int sum = 0;
        for (int cycleNumber : cycleNumbers) {
            sum += cycleNumber * getXDuringCycle(xIncreaseFromStartOfProgramToAfterCycleY, cyclesPerProgram, cycleNumber);
        }
        return sum;
    }

    private static boolean[][] drawCrtSprite(int[] xIncreaseFromStartOfProgramToAfterCycleY, int cyclesPerProgram) {
        boolean[][] crtOutput = new boolean[PART_B_SCREEN_HEIGHT][PART_B_SCREEN_WIDTH];
        for (int y=0, cycleNumber=1; y<PART_B_SCREEN_HEIGHT; y++) {
            boolean[] crtOutputRow = crtOutput[y];
            for (int x=0; x<PART_B_SCREEN_WIDTH; x++, cycleNumber++) {
                int xValue = getXDuringCycle(xIncreaseFromStartOfProgramToAfterCycleY, cyclesPerProgram, cycleNumber);
                int spriteStart = xValue+PART_B_SPRITE_OFFSET;
                int spriteEnd = spriteStart+PART_B_SPRITE_WIDTH-1;
                crtOutputRow[x] = x >= spriteStart && x <= spriteEnd;
            }
        }
        return crtOutput;
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        String[] instructionStrings = LineReader.stringsArray(inputCharacters,true);
        int instructionCount = instructionStrings.length;
        int[] xIncreaseFromStartOfProgramToAfterCycleY = new int[instructionCount*2];
        int cyclesPerProgram = 0;
        int xIncreaseFromStartOfProgramToAfterCurrentCycle = 0;
        for (int instructionIndex=0; instructionIndex<instructionCount; instructionIndex++) {
            String[] instructionParts = instructionStrings[instructionIndex].split(" ");
            int instructionPartCount = instructionParts.length;
            if (instructionParts[0].equals("noop") && instructionPartCount == 1) {
                xIncreaseFromStartOfProgramToAfterCycleY[cyclesPerProgram++] = xIncreaseFromStartOfProgramToAfterCurrentCycle;
            } else if (instructionParts[0].equals("addx") && instructionPartCount == 2) {
                xIncreaseFromStartOfProgramToAfterCycleY[cyclesPerProgram++] = xIncreaseFromStartOfProgramToAfterCurrentCycle;
                xIncreaseFromStartOfProgramToAfterCycleY[cyclesPerProgram++] = (xIncreaseFromStartOfProgramToAfterCurrentCycle += Integer.parseInt(instructionParts[1]));
            } else {
                throw new IllegalStateException("Could not parse instruction");
            }
        }
        return new BasicPuzzleResults<>(
            sumStrengthPerCycles(xIncreaseFromStartOfProgramToAfterCycleY,cyclesPerProgram,PART_A_CYCLE_NUMBERS),
            OutputType.valueOf(new String(configProvider.getPuzzleConfigChars("output_type"))).produceOutput(drawCrtSprite(xIncreaseFromStartOfProgramToAfterCycleY,cyclesPerProgram),PART_B_SCREEN_HEIGHT,PART_B_SCREEN_WIDTH)
        );
    }
}
