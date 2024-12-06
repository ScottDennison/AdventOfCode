package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2024;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

public class Day05 implements IPuzzle {
    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        boolean readingRules = true;
        Map<Integer,Map<Integer,Integer>> sortRules = new HashMap<>();
        int partATotal = 0;
        int partBTotal = 0;
        for (String inputLine : LineReader.strings(inputCharacters)) {
            if (readingRules) {
                if (inputLine.isEmpty()) {
                    readingRules = false;
                    continue;
                }
                String[] parts = inputLine.split("\\|");
                if (parts.length != 2) {
                    throw new IllegalStateException("Expecting two parts");
                }
                int pageX = Integer.parseInt(parts[0]);
                int pageY = Integer.parseInt(parts[1]);
                sortRules.computeIfAbsent(pageX,__ -> new HashMap<>()).put(pageY,-1);
                sortRules.computeIfAbsent(pageY,__ -> new HashMap<>()).put(pageX,1);
            }
            else {
                String[] parts = inputLine.split(",");
                int pageCount = parts.length;
                Integer[] pageNumbers = new Integer[pageCount];
                for (int pageNumberIndex=0; pageNumberIndex<pageCount; pageNumberIndex++) {
                    pageNumbers[pageNumberIndex] = Integer.parseInt(parts[pageNumberIndex]);
                }
                Integer[] pageNumbersSorted = Arrays.copyOf(pageNumbers, pageCount);
                Arrays.sort(pageNumbersSorted, new Comparator<Integer>() {
                    @Override
                    public int compare(Integer pageNumberX, Integer pageNumberY) {
                        return sortRules.get(pageNumberX).get(pageNumberY);
                    }
                });
                if (Arrays.equals(pageNumbers,pageNumbersSorted)) {
                    partATotal += pageNumbers[pageCount/2];
                }
                else {
                    partBTotal += pageNumbersSorted[pageCount/2];
                }
            }
        }
        return new BasicPuzzleResults<>(
            partATotal,
            partBTotal
        );
    }
}
