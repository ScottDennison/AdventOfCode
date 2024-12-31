package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2024;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Day25 implements IPuzzle {
    private static final int PIN_COUNT = 5;
    private static final int MAX_PIN_HEIGHT = 5;

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        int[] pinCounts = new int[PIN_COUNT];
        boolean[] pinsPreviouslySwitched = new boolean[PIN_COUNT];
        char topChar = '\0';
        char[][] inputLines = LineReader.charArraysArray(inputCharacters, true);
        int inputLineCount = inputLines.length;
        int inputLineIndex = 0;
        int keyLockRow = 0;
        List<int[]> keysList = new ArrayList<>();
        List<int[]> locksList = new ArrayList<>();
        int maxKeyLockRow = MAX_PIN_HEIGHT + 1;
        while (true) {
            boolean endOfInput = inputLineIndex == inputLineCount;
            char[] inputLine;
            if (inputLineIndex == inputLineCount) {
                inputLine = null;
            }
            else {
                inputLine = inputLines[inputLineIndex];
            }
            if (endOfInput || inputLine.length == 0) {
                for (int pin=0; pin<PIN_COUNT; pin++) {
                    if (!pinsPreviouslySwitched[pin]) {
                        throw new IllegalStateException("Pin " + (pin + 1) + " did not switch.");
                    }
                }
                int[] pinCountsForStorage;
                switch (topChar) {
                    case '#':
                        pinCountsForStorage = Arrays.copyOf(pinCounts, PIN_COUNT);
                        locksList.add(pinCountsForStorage);
                        break;
                    case '.':
                        pinCountsForStorage = new int[PIN_COUNT];
                        for (int pin=0; pin<PIN_COUNT; pin++) {
                            pinCountsForStorage[pin] = MAX_PIN_HEIGHT - pinCounts[pin];
                        }
                        keysList.add(pinCountsForStorage);
                        break;
                    default:
                        throw new IllegalStateException("Unexpected top char");
                }
                keyLockRow = -1;
                Arrays.fill(pinsPreviouslySwitched, false);
                Arrays.fill(pinCounts, 0);
            }
            else {
                if (keyLockRow == 0) {
                    topChar = inputLine[0];
                }
                else if (keyLockRow > maxKeyLockRow) {
                    throw new IllegalStateException("Too many rows.");
                }
                else {
                    for (int pin=0; pin<PIN_COUNT; pin++) {
                        char pinChar = inputLine[pin];
                        boolean pinSwitched = pinChar != topChar;
                        if (pinSwitched) {
                            pinsPreviouslySwitched[pin] = true;
                        }
                        else {
                            if (pinsPreviouslySwitched[pin]) {
                                throw new IllegalStateException("Pin switched then switched back.");
                            }
                            pinCounts[pin]++;
                        }
                    }
                }
            }
            keyLockRow++;
            if (endOfInput) {
                break;
            }
            inputLineIndex++;
        }
        int[][] keysArray = keysList.toArray(new int[0][]);
        int[][] locksArray = locksList.toArray(new int[0][]);
        int keysCount = keysArray.length;
        int locksCount = locksArray.length;
        int totalCombinations = 0;
        for (int keyIndex=0; keyIndex<keysCount; keyIndex++) {
            int[] key = keysArray[keyIndex];
            for (int lockIndex=0; lockIndex<locksCount; lockIndex++) {
                int[] lock = locksArray[lockIndex];
                boolean fitsTogether = true;
                for (int pin=0; pin<PIN_COUNT; pin++) {
                    if ((key[pin] + lock[pin]) > MAX_PIN_HEIGHT) {
                        fitsTogether = false;
                        break;
                    }
                }
                if (fitsTogether) {
                    totalCombinations++;
                }
            }
        }
        return new BasicPuzzleResults<>(
            totalCombinations,
            "Deliver The Chronicle"
        );
    }
}
