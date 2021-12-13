package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2021;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Day10 implements IPuzzle {
    private static final Map<Character,Integer> SYNTAX_CHECKER_SCORES = new HashMap<>();
    static {
        SYNTAX_CHECKER_SCORES.put(')',3);
        SYNTAX_CHECKER_SCORES.put(']',57);
        SYNTAX_CHECKER_SCORES.put('}',1197);
        SYNTAX_CHECKER_SCORES.put('>',25137);
    }
    private static final Map<Character,Long> AUTOCOMPLETE_SCORES = new HashMap<>();
    static {
        AUTOCOMPLETE_SCORES.put(')',1L);
        AUTOCOMPLETE_SCORES.put(']',2L);
        AUTOCOMPLETE_SCORES.put('}',3L);
        AUTOCOMPLETE_SCORES.put('>',4L);
    }
    private static final Map<Character,Character> CLOSING_CHARACTERS_FOR_OPENING_CHARACTERS = new HashMap<>();
    static {
        CLOSING_CHARACTERS_FOR_OPENING_CHARACTERS.put('(',')');
        CLOSING_CHARACTERS_FOR_OPENING_CHARACTERS.put('[',']');
        CLOSING_CHARACTERS_FOR_OPENING_CHARACTERS.put('{','}');
        CLOSING_CHARACTERS_FOR_OPENING_CHARACTERS.put('<','>');
    }
    private static final long AUTOCOMPLETE_MULTIPLIER = 5;

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        char[][] lines = LineReader.charArraysArray(inputCharacters,true);
        int lineCount = lines.length;
        int longestLineLength = 0;
        for (char[] line : lines) {
            int lineLength = line.length;
            if (lineLength > longestLineLength) {
                longestLineLength = lineLength;
            }
        }
        char[] expectedClosingStack = new char[longestLineLength];
        int totalSyntaxCheckerScore = 0;
        long[] autocompleteScores = new long[lineCount];
        int autocompleteScoreCount = 0;
        for (char[] line : lines) {
            int lastStackPosition = -1;
            boolean incomplete = true;
            for (char lineChar : line) {
                Character associatedClosingCharacter = CLOSING_CHARACTERS_FOR_OPENING_CHARACTERS.get(lineChar);
                if (associatedClosingCharacter == null) {
                    if (lastStackPosition >= 0 && expectedClosingStack[lastStackPosition] == lineChar) {
                        lastStackPosition--;
                    } else {
                        Integer lineScore = SYNTAX_CHECKER_SCORES.get(lineChar);
                        if (lineScore == null) {
                            throw new IllegalStateException("Invalid character");
                        }
                        incomplete = false;
                        totalSyntaxCheckerScore += lineScore;
                        break;
                    }
                }
                else {
                    expectedClosingStack[++lastStackPosition] = associatedClosingCharacter;
                }
            }
            if (incomplete) {
                long lineAutocompleteScore = 0;
                for (int stackPosition=lastStackPosition; stackPosition>=0; stackPosition--) {
                    Long autocompleteCharacterScore = AUTOCOMPLETE_SCORES.get(expectedClosingStack[stackPosition]);
                    if (autocompleteCharacterScore == null) {
                        throw new IllegalStateException("Invalid character");
                    }
                    lineAutocompleteScore = Math.addExact(Math.multiplyExact(lineAutocompleteScore,AUTOCOMPLETE_MULTIPLIER),autocompleteCharacterScore);
                }
                autocompleteScores[autocompleteScoreCount++] = lineAutocompleteScore;
            }
        }
        if (autocompleteScoreCount % 2 != 1) {
            throw new IllegalStateException("Expected an odd number of incomplete lines");
        }
        Arrays.sort(autocompleteScores,0,autocompleteScoreCount);
        return new BasicPuzzleResults<>(
            totalSyntaxCheckerScore,
            autocompleteScores[autocompleteScoreCount/2]
        );
    }
}
