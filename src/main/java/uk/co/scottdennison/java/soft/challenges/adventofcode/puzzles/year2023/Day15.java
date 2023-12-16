package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2023;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day15 implements IPuzzle {
    private static final int BOX_COUNT = 256;

    private static final Pattern PATTERN_PART = Pattern.compile("^(?<label>[a-z]+)(?:(?:=(?<lensNumber>[0-9]+))|-)$");

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        int hashSum = 0;
        Map<Integer, LinkedHashMap<String,Integer>> boxes = new HashMap<>();
        Map<String,Integer> hashCache = new HashMap<>();
        for (String part : new String(inputCharacters).trim().split(",")) {
            hashSum += hash(hashCache,part);

            Matcher partMatcher = PATTERN_PART.matcher(part);
            if (!partMatcher.matches()) {
                throw new IllegalStateException("Could not parse part");
            }
            String label = partMatcher.group("label");
            String lensNumberString = partMatcher.group("lensNumber");
            int boxNumber = hash(hashCache,label);
            LinkedHashMap<String,Integer> box = boxes.computeIfAbsent(boxNumber,__->new LinkedHashMap<>());
            if (lensNumberString == null) {
                box.remove(label);
                if (box.isEmpty()) {
                    boxes.remove(label);
                }
            }
            else {
                box.put(label,Integer.parseInt(lensNumberString));
            }
        }
        int totalFocusingPower = 0;
        for (Map.Entry<Integer,LinkedHashMap<String,Integer>> boxEntry : boxes.entrySet()) {
            int boxNumberPlusOne = boxEntry.getKey() + 1;
            int lensPosition = 0;
            for (Integer lensNumber : boxEntry.getValue().values()) {
                totalFocusingPower += boxNumberPlusOne*(++lensPosition)*lensNumber;
            }
        }
        return new BasicPuzzleResults<>(
            hashSum,
            totalFocusingPower
        );
    }

    private static int hash(String input) {
        int currentValue = 0;
        for (char character : input.toCharArray()) {
            currentValue = ((currentValue + ((int)character)) * 17) % BOX_COUNT;
        }
        return currentValue;
    }

    private static int hash(Map<String,Integer> hashCache, String input) {
        return hashCache.computeIfAbsent(input, Day15::hash);
    }
}
