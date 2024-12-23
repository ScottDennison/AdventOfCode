package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2024;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Day19 implements IPuzzle {
    private static long recurse(Map<String,Long> triedStrings, String[] towelTypes, String inputString) {
        Long triedStringsResult = triedStrings.get(inputString);
        if (triedStringsResult != null) {
            return triedStringsResult;
        }
        else {
            long result = 0;
            for (String towelType : towelTypes) {
                if (inputString.startsWith(towelType)) {
                    result = Math.addExact(result, recurse(triedStrings, towelTypes, inputString.substring(towelType.length())));
                }
            }
            triedStrings.put(inputString, result);
            return result;
        }
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        String[] inputLines = LineReader.stringsArray(inputCharacters, true);
        int inputLineCount = inputLines.length;
        if (inputLineCount < 3) {
            throw new IllegalStateException("Not enough input lines");
        }
        if (!inputLines[1].trim().isEmpty()) {
            throw new IllegalStateException("Expected input line 2 to be empty");
        }
        String[] towelTypes = Arrays.stream(inputLines[0].split(",")).map(String::trim).filter(inputEntry -> !inputEntry.isEmpty()).toArray(String[]::new);
        Map<String,Long> triedStrings = new HashMap<>();
        triedStrings.put("",1L);
        int partAResult = 0;
        long partBResult = 0;
        for (int inputLineIndex=2; inputLineIndex<inputLineCount; inputLineIndex++) {
            long result = recurse(triedStrings, towelTypes,inputLines[inputLineIndex].trim());
            partBResult = Math.addExact(partBResult, result);
            if (result > 0) {
                partAResult++;
            }
        }
        return new BasicPuzzleResults<>(
            partAResult,
            partBResult
        );
    }
}
