package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2022;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Arrays;

public class Day03 implements IPuzzle {
    private static final int PART_A_RUCKSACK_COMPARTMENT_COUNT = 2;
    private static final int PART_B_ELF_RUCKSACK_COUNT = 3;

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        char[][] lines = LineReader.charArraysArray(inputCharacters, true);
        int lineCount = lines.length;
        if (lineCount % PART_B_ELF_RUCKSACK_COUNT != 0) {
            throw new IllegalStateException("Invalid amount of lines");
        }
        char[][] partARucksackCompartments = new char[PART_A_RUCKSACK_COMPARTMENT_COUNT][];
        char[][] partBElfRucksacks = new char[PART_B_ELF_RUCKSACK_COUNT][];
        int partASum = 0;
        int partBSum = 0;
        int partBElfRucksackIndexToCheckOn = PART_B_ELF_RUCKSACK_COUNT - 1;
        for (int lineIndex=0; lineIndex<lineCount; lineIndex++) {
            char[] line = lines[lineIndex];
            int lineLength = line.length;
            if (lineLength % PART_A_RUCKSACK_COMPARTMENT_COUNT != 0) {
                throw new IllegalStateException("Invalid line length");
            }
            int compartmentLength = lineLength / 2;
            int startIndex = 0;
            for (int compartmentIndex = 0; compartmentIndex< PART_A_RUCKSACK_COMPARTMENT_COUNT; compartmentIndex++) {
                int nextStartIndex = startIndex+compartmentLength;
                partARucksackCompartments[compartmentIndex] = Arrays.copyOfRange(line,startIndex,nextStartIndex);
                startIndex = nextStartIndex;
            }
            partASum += solve(partARucksackCompartments);
            int partBElfRucksackIndex = lineIndex %  PART_B_ELF_RUCKSACK_COUNT;
            partBElfRucksacks[partBElfRucksackIndex] = line;
            if (partBElfRucksackIndex == partBElfRucksackIndexToCheckOn) {
                partBSum += solve(partBElfRucksacks);
            }
        }
        return new BasicPuzzleResults<>(
            partASum,
            partBSum
        );
    }

    private static int solve(char[][] parts) {
        int partCount = parts.length;
        int sum = 0;
        boolean[][] partContents = new boolean[partCount][55];
        for (int partIndex=0; partIndex<partCount; partIndex++) {
            char[] part = parts[partIndex];
            boolean[] partContentsForPart = partContents[partIndex];
            int partLength = part.length;
            for (int charIndex=0; charIndex<partLength; charIndex++) {
                char charChar = part[charIndex];
                int charValue;
                if (charChar >= 'a' && charChar <= 'z') {
                    charValue = (charChar-'a')+1;
                }
                else if (charChar >= 'A' && charChar <= 'Z') {
                    charValue = (charChar-'A')+27;
                }
                else {
                    throw new IllegalStateException("Invalid character");
                }
                partContentsForPart[charValue] = true;
            }
        }
        boolean duplicateCharFound = false;
        int duplicateCharValue = 0;
        for (int charValue=1; charValue<=54; charValue++) {
            boolean allPartsContainCharacter = true;
            for (int partIndex=0; partIndex<partCount; partIndex++) {
                if (!partContents[partIndex][charValue]) {
                    allPartsContainCharacter = false;
                    break;
                }
            }
            if (allPartsContainCharacter) {
                if (duplicateCharFound) {
                    throw new IllegalStateException("Multiple duplicate characters");
                }
                else {
                    duplicateCharFound = true;
                    sum += charValue;
                }
            }
        }
        if (!duplicateCharFound) {
            throw new IllegalStateException("No duplicate character found");
        }
        return sum;
    }
}
