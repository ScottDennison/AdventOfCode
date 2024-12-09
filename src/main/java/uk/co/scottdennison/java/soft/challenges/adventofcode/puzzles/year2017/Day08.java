package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2017;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day08 implements IPuzzle {
    private static final Pattern PATTERN_LINE = Pattern.compile("^(?<modificationRegister>[a-z]+) (?<modificationOperation>inc|dec) (?<modificationValue>-?[0-9]+) if (?<testRegister>[a-z]+) (?<testOperation><|>|<=|>=|==|!=) (?<testValue>-?[0-9]+)$");

    private static int getRegisterValue(Map<String,Integer> registers, String register) {
        Integer value = registers.get(register);
        if (value == null) {
            registers.put(register, 0);
            return 0;
        }
        return value;
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        Map<String,Integer> registers = new HashMap<>();
        int highestIntermediateValue = Integer.MIN_VALUE;
        for (String inputLine : LineReader.strings(inputCharacters)) {
            Matcher matcher = PATTERN_LINE.matcher(inputLine);
            if (!matcher.matches()) {
                throw new IllegalStateException("Could not parse line");
            }
            int testRegisterValue = getRegisterValue(registers, matcher.group("testRegister"));
            int testValue = Integer.parseInt(matcher.group("testValue"));
            boolean testPasses;
            switch (matcher.group("testOperation")) {
                case "<":
                    testPasses = testRegisterValue < testValue;
                    break;
                case ">":
                    testPasses = testRegisterValue > testValue;
                    break;
                case "<=":
                    testPasses = testRegisterValue <= testValue;
                    break;
                case ">=":
                    testPasses = testRegisterValue >= testValue;
                    break;
                case "==":
                    testPasses = testRegisterValue == testValue;
                    break;
                case "!=":
                    testPasses = testRegisterValue != testValue;
                    break;
                default:
                    throw new IllegalStateException("Unknown operation.");
            }
            if (testPasses) {
                String modificationRegister = matcher.group("modificationRegister");
                int modificationRegisterValue = getRegisterValue(registers, modificationRegister);
                int modificationValue = Integer.parseInt(matcher.group("modificationValue"));
                switch (matcher.group("modificationOperation")) {
                    case "inc":
                        modificationRegisterValue += modificationValue;
                        break;
                    case "dec":
                        modificationRegisterValue -= modificationValue;
                        break;
                    default:
                        throw new IllegalStateException("Unknown operation.");
                }
                registers.put(modificationRegister, modificationRegisterValue);
                highestIntermediateValue = Math.max(highestIntermediateValue, modificationRegisterValue);
            }
        }
        int highestFinalValue = registers.values().stream().mapToInt(x -> x).max().orElseThrow(() -> new IllegalStateException("No register values."));
        return new BasicPuzzleResults<>(
            highestFinalValue,
            highestIntermediateValue
        );
    }
}
