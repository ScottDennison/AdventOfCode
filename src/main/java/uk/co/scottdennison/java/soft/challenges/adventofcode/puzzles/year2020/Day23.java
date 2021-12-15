package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2020;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Arrays;

public class Day23 implements IPuzzle {
    private static class Cup {
        private Cup nextCup;
        private final int label;

        private Cup(int label) {
            this.label = label;
        }

        public Cup getNextCup() {
            return this.nextCup;
        }

        public void setNextCup(Cup nextCup) {
            this.nextCup = nextCup;
        }

        public int getLabel() {
            return this.label;
        }
    }

    private static final int PART_B_CUP_COUNT = 1000000;
    private static final int CUPS_TO_MOVE_COUNT = 3;

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        int partAMoveCount = Integer.parseInt(new String(configProvider.getPuzzleConfigChars("part_a_move_count")).trim());
        char[] trimmedInputCharacters = new String(inputCharacters).trim().toCharArray();
        int partACupCount = trimmedInputCharacters.length;
        int[] partACupLabels = new int[partACupCount];
        for (int cupIndex=0; cupIndex<partACupCount; cupIndex++) {
            char cupLabelCharacter = trimmedInputCharacters[cupIndex];
            if (cupLabelCharacter >= '0' && cupLabelCharacter <= '9') {
                partACupLabels[cupIndex] = cupLabelCharacter-'0';
            } else {
                throw new IllegalStateException("Unexpected input character");
            }
        }
        int[] sortedPartACupLabels = Arrays.copyOf(partACupLabels,partACupCount);
        Arrays.sort(sortedPartACupLabels);
        for (int cupIndex=0; cupIndex<partACupCount; cupIndex++) {
            if (sortedPartACupLabels[cupIndex] != (cupIndex+1)) {
                throw new IllegalStateException("Expected contiguously numbered cup labels starting at 1");
            }
        }
        Cup partAOneCup = run(partACupLabels,partAMoveCount);
        Cup partAInterestCup = partAOneCup;
        int partAOutputCharacterCount = partACupCount-1;
        char[] partAOutputCharacters = new char[partAOutputCharacterCount];
        for (int partAOutputCharacterIndex=0; partAOutputCharacterIndex<partAOutputCharacterCount; partAOutputCharacterIndex++) {
            partAInterestCup = partAInterestCup.nextCup;
            partAOutputCharacters[partAOutputCharacterIndex] = (char)(partAInterestCup.getLabel()+'0');
        }
        String partAOutputString = new String(partAOutputCharacters);
        Long partBOutputValue;
        if (partBPotentiallyUnsolvable) {
            partBOutputValue = null;
        } else {
            int partBMoveCount = Integer.parseInt(new String(configProvider.getPuzzleConfigChars("part_b_move_count")).trim());
            int[] partBCupLabels = new int[PART_B_CUP_COUNT];
            System.arraycopy(partACupLabels,0,partBCupLabels,0,partACupCount);
            for (int cupIndex=partACupCount; cupIndex<PART_B_CUP_COUNT; cupIndex++) {
                partBCupLabels[cupIndex] = cupIndex+1;
            }
            Cup partBOneCup = run(partBCupLabels,partBMoveCount);
            Cup partBOneCupOffset1 = partBOneCup.getNextCup();
            Cup partBOneCupOffset2 = partBOneCupOffset1.getNextCup();
            partBOutputValue = Math.multiplyExact((long)partBOneCupOffset1.getLabel(),(long)partBOneCupOffset2.getLabel());
        }
        return new BasicPuzzleResults<>(
            partAOutputString,
            partBOutputValue
        );
    }

    private static Cup run(int[] cupLabels, int moveCount) {
        int cupCount = cupLabels.length;
        Cup[] cups = new Cup[cupCount+1];
        int startingCupLabel = cupLabels[0];
        Cup startingCup = new Cup(startingCupLabel);
        cups[startingCupLabel] = startingCup;
        Cup currentCup = startingCup;
        for (int cupIndex=1; cupIndex<cupCount; cupIndex++) {
            int cupLabel = cupLabels[cupIndex];
            Cup newCup = new Cup(cupLabel);
            cups[cupLabel] = newCup;
            currentCup.setNextCup(newCup);
            currentCup = newCup;
        }
        currentCup.setNextCup(startingCup);
        currentCup = startingCup;
        for (int move=1; move<=moveCount; move++) {
            Cup moveStartCup = currentCup.getNextCup();
            Cup moveEndCup = currentCup;
            for (int moveCupNumber=1; moveCupNumber<=CUPS_TO_MOVE_COUNT; moveCupNumber++) {
                moveEndCup = moveEndCup.getNextCup();
            }
            currentCup.setNextCup(moveEndCup.getNextCup());
            int destinationCupLabel = currentCup.getLabel()-1;
            while (true) {
                if (destinationCupLabel < 1) {
                    destinationCupLabel += cupCount;
                }
                boolean destinationCupLabelOK = true;
                Cup checkCup = moveStartCup;
                for (int cupOffset=1; cupOffset<=CUPS_TO_MOVE_COUNT; cupOffset++) {
                    if (checkCup.getLabel() == destinationCupLabel) {
                        destinationCupLabelOK = false;
                        break;
                    }
                    checkCup = checkCup.getNextCup();
                }
                if (destinationCupLabelOK) {
                    break;
                }
                destinationCupLabel--;
            }
            Cup destinationCup = cups[destinationCupLabel];
            moveEndCup.setNextCup(destinationCup.getNextCup());
            destinationCup.setNextCup(moveStartCup);
            currentCup = currentCup.getNextCup();
        }
        return cups[1];
    }
}
