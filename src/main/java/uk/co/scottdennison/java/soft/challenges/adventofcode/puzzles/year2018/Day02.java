package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2018;

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

public class Day02 implements IPuzzle {
    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        char[][] inputBoxIDs = LineReader.charArraysArray(inputCharacters, true);
        int twoCountCount = 0;
        int threeCountCount = 0;
        String commonPartialBoxID = null;
        Map<String,String> partialBoxIDs = new HashMap<>();
        for (char[] inputBoxID : inputBoxIDs) {
            int letterCounts[] = new int[26];
            for (char letterChar : inputBoxID) {
                if (letterChar < 'a' || letterChar > 'z') {
                    throw new IllegalStateException("Invalid input");
                }
                letterCounts[letterChar-'a']++;
            }
            boolean matchesTwoCountCriteria = false;
            boolean matchesThreeCountCriteria = false;
            for (int letterIndex=0; letterIndex<26; letterIndex++) {
                switch (letterCounts[letterIndex]) {
                    case 2:
                        matchesTwoCountCriteria = true;
                        break;
                    case 3:
                        matchesThreeCountCriteria = true;
                        break;
                }
            }
            if (matchesTwoCountCriteria) {
                twoCountCount++;
            }
            if (matchesThreeCountCriteria) {
                threeCountCount++;
            }
            String inputBoxIDString = new String(inputBoxID);
            int partialBoxIDLength = inputBoxID.length-1;
            char[] partialBoxID = new char[partialBoxIDLength];
            System.arraycopy(inputBoxID, 1, partialBoxID, 0, partialBoxIDLength);
            int index = 0;
            while (true) {
                String partialBoxIDString = new String(partialBoxID);
                String previousInputBoxIDString = partialBoxIDs.put(partialBoxIDString, inputBoxIDString);
                if (previousInputBoxIDString != null && !previousInputBoxIDString.equals(inputBoxIDString)) {
                    if (commonPartialBoxID != null) {
                        printWriter.println("Multiple common partial box ids. Discarting potential solution.");
                        commonPartialBoxID = null;
                        break;
                    }
                    commonPartialBoxID = partialBoxIDString;
                }
                if (index >= partialBoxIDLength) {
                    break;
                }
                partialBoxID[index] = inputBoxID[index];
                index++;
            }
        }
        return new BasicPuzzleResults<>(
            twoCountCount * threeCountCount,
            commonPartialBoxID
        );
    }
}
