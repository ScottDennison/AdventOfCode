package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2017;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;

public class Day09 implements IPuzzle {
    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        int score = 0;
        int nesting = 0;
        int garbageCount = 0;
        boolean inGarbage = false;
        boolean nextCharacterCancelled = false;
        boolean newGroupRequired = false;
        for (char inputCharacter : new String(inputCharacters).trim().toCharArray()) {
            if (nextCharacterCancelled) {
                nextCharacterCancelled = false;
            }
            else if (inGarbage) {
                switch (inputCharacter) {
                    case '!':
                        nextCharacterCancelled = true;
                        break;
                    case '>':
                        inGarbage = false;
                        break;
                    default:
                        garbageCount++;
                        break;
                }
            }
            else {
                switch (inputCharacter) {
                    case '!':
                        nextCharacterCancelled = true;
                        break;
                    case '<':
                        newGroupRequired = false;
                        inGarbage = true;
                        break;
                    case '{':
                        newGroupRequired = false;
                        nesting++;
                        break;
                    case '}':
                        if (newGroupRequired) {
                            throw new IllegalStateException("Expected another group");
                        }
                        score += nesting--;
                        if (nesting < 0) {
                            throw new IllegalStateException("Negative nesting");
                        }
                        break;
                    case ',':
                        newGroupRequired = true;
                        break;
                    default:
                        throw new IllegalStateException("Unexpected character");
                }
            }
        }
        if (newGroupRequired || nesting != 0 || inGarbage || nextCharacterCancelled) {
            throw new IllegalStateException("Unexpected end state");
        }
        return new BasicPuzzleResults<>(
            score,
            garbageCount
        );
    }
}
