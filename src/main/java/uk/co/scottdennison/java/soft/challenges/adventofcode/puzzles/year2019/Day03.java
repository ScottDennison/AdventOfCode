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
                throw new IllegalStateException("Wire " + wireNumber + " does not ");
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

    private interface WireSegment {
        void visitCells(WireCellVisitor wireCellVisitor);
    }

    private static abstract class VerticalWireSegment implements WireSegment {
        protected final int x;
        protected final int yStart;
        protected final int yFinish;

        protected VerticalWireSegment(int x, int yStart, int yFinish) {
            this.x = x;
            this.yStart = yStart;
            this.yFinish = yFinish;
        }
    }

    private static class UpWireSegment extends VerticalWireSegment {
        protected UpWireSegment(int x, int yStart, int yFinish) {
            super(x, yStart, yFinish);
            if (yStart <= yFinish) {
                throw new IllegalStateException("Invalid y movement");
            }
        }

        @Override
        public void visitCells(WireCellVisitor wireCellVisitor) {
            for (int y=this.yStart-1; y>=this.yFinish; y--) {
                wireCellVisitor.visitCell(this.x, y);
            }
        }
    }

    private static class DownWireSegment extends VerticalWireSegment {
        protected DownWireSegment(int x, int yStart, int yFinish) {
            super(x, yStart, yFinish);
            if (yStart >= yFinish) {
                throw new IllegalStateException("Invalid y movement");
            }
        }

        @Override
        public void visitCells(WireCellVisitor wireCellVisitor) {
            for (int y=this.yStart+1; y<=this.yFinish; y++) {
                wireCellVisitor.visitCell(this.x, y);
            }
        }
    }

    private static abstract class HorizonalWireSegment implements WireSegment {
        protected final int xStart;
        protected final int xFinish;
        protected final int y;

        public HorizonalWireSegment(int xStart, int xFinish, int y) {
            this.xStart = xStart;
            this.xFinish = xFinish;
            this.y = y;
        }
    }

    private static class LeftWireSegment extends HorizonalWireSegment {
        protected LeftWireSegment(int xStart, int xFinish, int y) {
            super(xStart, xFinish, y);
            if (xStart <= xFinish) {
                throw new IllegalStateException("Invalid x movement");
            }
        }

        @Override
        public void visitCells(WireCellVisitor wireCellVisitor) {
            for (int x=this.xStart-1; x>=this.xFinish; x--) {
                wireCellVisitor.visitCell(x, this.y);
            }
        }
    }

    private static class RightWireSegment extends HorizonalWireSegment {
        protected RightWireSegment(int xStart, int xFinish, int y) {
            super(xStart, xFinish, y);
            if (xStart >= xFinish) {
                throw new IllegalStateException("Invalid x movement");
            }
        }

        @Override
        public void visitCells(WireCellVisitor wireCellVisitor) {
            for (int x=this.xStart+1; x<=this.xFinish; x++) {
                wireCellVisitor.visitCell(x, this.y);
            }
        }
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        List<List<WireSegment>> wireSegmentsLists = new ArrayList<>();
        int minX = 0;
        int maxX = 0;
        int minY = 0;
        int maxY = 0;
        for (String inputLine : LineReader.stringsArray(inputCharacters, true)) {
            List<WireSegment> wireSegmentList = new ArrayList<>();
            int startX = 0;
            int startY = 0;
            int wireCells = 0;
            for (String inputInstructionString : inputLine.split(",")) {
                char inputInstructionType = inputInstructionString.charAt(0);
                int inputInstructionAmount = Integer.parseInt(inputInstructionString.substring(1));
                int finishX = startX;
                int finishY = startY;
                wireCells += inputInstructionAmount;
                WireSegment wireSegment;
                switch (inputInstructionType) {
                    case 'U':
                    case 'u':
                        finishY -= inputInstructionAmount;
                        minY = Math.min(minY, finishY);
                        wireSegment = new UpWireSegment(finishX, startY, finishY);
                        break;
                    case 'D':
                    case 'd':
                        finishY += inputInstructionAmount;
                        maxY = Math.max(maxY, finishY);
                        wireSegment = new DownWireSegment(finishX, startY, finishY);
                        break;
                    case 'L':
                    case 'l':
                        finishX -= inputInstructionAmount;
                        minX = Math.min(minX, finishX);
                        wireSegment = new LeftWireSegment(startX, finishX, finishY);
                        break;
                    case 'R':
                    case 'r':
                        finishX += inputInstructionAmount;
                        maxX = Math.max(maxX, finishX);
                        wireSegment = new RightWireSegment(startX, finishX, finishY);
                        break;
                    default:
                        throw new IllegalStateException("Unexpected instruction character");
                }
                startX = finishX;
                startY = finishY;
                wireSegmentList.add(wireSegment);
            }
            wireSegmentsLists.add(wireSegmentList);
        }
        final int finalMinX = minX;
        final int finalMaxX = maxX;
        final int finalMinY = minY;
        final int finalMaxY = maxY;
        final WireCell[][] wireCellsGrid = new WireCell[finalMaxY - finalMinY + 1][finalMaxX - finalMinX + 1];
        final Map<Integer,Map<Integer,WireCell>> wireCellsMap = new HashMap<>();
        int wireNumber = 0;
        final int wireCount = wireSegmentsLists.size();
        Set<WireCell> intersectionWireCells = Collections.newSetFromMap(new IdentityHashMap<>());
        Set<Integer> wireNumbers = new HashSet<>();
        for (List<WireSegment> wireSegmentList : wireSegmentsLists) {
            int finalWireNumber = ++wireNumber;
            wireNumbers.add(wireNumber);
            WireCellVisitor wireCellVisitor = new WireCellVisitor() {
                private int steps = 0;

                @Override
                public void visitCell(int x, int y) {
                    final int gridX = x - finalMinX;
                    final int gridY = y - finalMinY;
                    WireCell wireCell = wireCellsGrid[gridY][gridX];
                    if (wireCell == null) {
                        wireCell = new WireCell(x, y);
                        wireCellsGrid[gridY][gridX] = wireCell;
                    }
                    wireCell.recordStepsForWireNumber(finalWireNumber, ++this.steps);
                    if (wireCell.getUniqueWireNumberCount() == wireCount) {
                        intersectionWireCells.add(wireCell);
                    }
                }
            };
            for (WireSegment wireSegment : wireSegmentList) {
                wireSegment.visitCells(wireCellVisitor);
            }
        }
        return new BasicPuzzleResults<>(
            intersectionWireCells.stream().mapToInt(wireCell -> Math.abs(wireCell.getX()) + Math.abs(wireCell.getY())).min().getAsInt(),
            intersectionWireCells.stream().mapToInt(wireCell -> wireNumbers.stream().mapToInt(wireCell::getStepsForWireNumber).sum()).min().getAsInt()
        );
    }
}
