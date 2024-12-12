package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2017;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzlePartResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.MultiPartPuzzleResults;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.stream.IntStream;

public class Day10 implements IPuzzle {
    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    private static int[] runKnotHash(int[] lengths, int listSize, int iterations) {
        int[] list = new int[listSize];
        for (int listIndex=0; listIndex<listSize; listIndex++) {
            list[listIndex] = listIndex;
        }
        int currentIndex = 0;
        int skipSize = -1;
        for (int iteration=1; iteration<=iterations; iteration++) {
            for (int length : lengths) {
                for (int reverseIndex1=currentIndex, reverseIndex2=currentIndex+length-1; reverseIndex1 < reverseIndex2; reverseIndex1++, reverseIndex2--) {
                    int reverseIndex1Mod = reverseIndex1 % listSize;
                    int reverseIndex2Mod = reverseIndex2 % listSize;
                    int temp = list[reverseIndex1Mod];
                    list[reverseIndex1Mod] = list[reverseIndex2Mod];
                    list[reverseIndex2Mod] = temp;
                }
                currentIndex = (currentIndex + length + (skipSize = (skipSize + 1) % listSize)) % listSize;
            }
        }
        return list;
    }

    private static Integer solvePartA(String inputString, IPuzzleConfigProvider configProvider) {
        int[] inputLengths;
        try {
            inputLengths = Arrays.stream(inputString.split(",")).map(String::trim).mapToInt(Integer::parseInt).toArray();
        } catch (NumberFormatException ex) {
            return null;
        }
        int[] list = runKnotHash(
            inputLengths,
            Integer.parseInt(new String(configProvider.getPuzzleConfigChars("part_a_list_size")).trim()),
            1
        );
        return list[0] * list[1];
    }

    private static String solvePartB(String inputString) {
        int[] list = runKnotHash(
            IntStream.concat(
                inputString.chars(),
                IntStream.of(17, 31, 73, 47, 23)
            ).toArray(),
            256,
            64
        );
        if (list.length != 256) {
            throw new IllegalStateException("Cannot process list");
        }
        char[] output = new char[32];
        int outputIndex = 0;
        int listIndex = 0;
        for (int i=0; i<16; i++) {
            int xorResult = 0;
            for (int j=0; j<16; j++) {
                xorResult ^= list[listIndex++];
            }
            output[outputIndex++] = HEX_CHARS[xorResult >> 4];
            output[outputIndex++] = HEX_CHARS[xorResult & 15];
        }
        return new String(output);
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        String inputString = new String(inputCharacters).trim();
        return new MultiPartPuzzleResults<>(
            new BasicPuzzlePartResults<>(
                solvePartA(inputString, configProvider)
            ),
            new BasicPuzzlePartResults<>(
                solvePartB(inputString)
            )
        );
    }
}
