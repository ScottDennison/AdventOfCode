package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2019;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Day03 implements IPuzzle {
    private static class WireCell {
        private final int x;
        private final int y;
        private final Map<Integer, Integer> stepsPerWire;

        public WireCell(int x, int y) {
            this.x = x;
            this.y = y;
            this.stepsPerWire = new HashMap<>();
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        public void recordStepsForWireNumber(int wireNumber, int steps) {
            Integer currentSteps = stepsPerWire.get(wireNumber);
            if (currentSteps == null || currentSteps > steps) {
                stepsPerWire.put(wireNumber, steps);
            }
        }

        public int getStepsForWireNumber(int wireNumber) {
            Integer steps = stepsPerWire.get(wireNumber);
            if (steps == null) {
                throw new IllegalStateException("Wire " + wireNumber + " does not visit this cell.");
            }
            return steps;
        }

        public int getUniqueWireNumberCount() {
            return this.stepsPerWire.size();
        }
    }

    private interface WireCellVisitor {
        void visitCell(int x, int y);
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        String[] inputLines = LineReader.stringsArray(inputCharacters, true);
        int wireCount = inputLines.length;
        final Map<Integer,Map<Integer,WireCell>> wireCellsMap = new HashMap<>();
        Set<WireCell> intersectionWireCells = Collections.newSetFromMap(new IdentityHashMap<>());
        Set<Integer> wireNumbers = new HashSet<>();
        int wireNumber = 0;
        for (String inputLine : inputLines) {
            int finalWireNumber = ++wireNumber;
            wireNumbers.add(finalWireNumber);
            int startX = 0;
            int startY = 0;
            WireCellVisitor wireCellVisitor = new WireCellVisitor() {
                private int steps = 0;

                @Override
                public void visitCell(int x, int y) {
                    WireCell wireCell = wireCellsMap.computeIfAbsent(y,__ -> new HashMap<>()).computeIfAbsent(x,__ -> new WireCell(x, y));
                    wireCell.recordStepsForWireNumber(finalWireNumber, ++this.steps);
                    if (wireCell.getUniqueWireNumberCount() == wireCount) {
                        intersectionWireCells.add(wireCell);
                    }
                }
            };
            for (String inputInstructionString : inputLine.split(",")) {
                char inputInstructionType = inputInstructionString.charAt(0);
                int inputInstructionAmount = Integer.parseInt(inputInstructionString.substring(1));
                int finishX = startX;
                int finishY = startY;
                switch (inputInstructionType) {
                    case 'U':
                    case 'u':
                        finishY -= inputInstructionAmount;
                        for (int y=startY-1; y>=finishY; y--) {
                            wireCellVisitor.visitCell(finishX, y);
                        }
                        startY = finishY;
                        break;
                    case 'D':
                    case 'd':
                        finishY += inputInstructionAmount;
                        for (int y=startY+1; y<=finishY; y++) {
                            wireCellVisitor.visitCell(finishX, y);
                        }
                        startY = finishY;
                        break;
                    case 'L':
                    case 'l':
                        finishX -= inputInstructionAmount;
                        for (int x=startX-1; x>=finishX; x--) {
                            wireCellVisitor.visitCell(x, finishY);
                        }
                        startX = finishX;
                        break;
                    case 'R':
                    case 'r':
                        finishX += inputInstructionAmount;
                        for (int x=startX+1; x<=finishX; x++) {
                            wireCellVisitor.visitCell(x, finishY);
                        }
                        startX = finishX;
                        break;
                    default:
                        throw new IllegalStateException("Unexpected instruction character");
                }
            }
        }
        return new BasicPuzzleResults<>(
            intersectionWireCells.stream().mapToInt(wireCell -> Math.abs(wireCell.getX()) + Math.abs(wireCell.getY())).min().getAsInt(),
            intersectionWireCells.stream().mapToInt(wireCell -> wireNumbers.stream().mapToInt(wireCell::getStepsForWireNumber).sum()).min().getAsInt()
        );
    }
}
