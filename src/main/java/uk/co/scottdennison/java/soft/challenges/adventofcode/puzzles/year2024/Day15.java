package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2024;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Day15 implements IPuzzle {
    private static class Box {
        private final int x;
        private final int y;
        private final int width;
        private final int height;

        public Box(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        public int getWidth() {
            return this.width;
        }

        public int getHeight() {
            return this.height;
        }
    }

    private static enum Direction {
        NORTH (-1, 0),
        EAST (0, 1),
        SOUTH (1, 0),
        WEST (0, -1);

        private final int yDelta;
        private final int xDelta;

        Direction(int yDelta, int xDelta) {
            this.yDelta = yDelta;
            this.xDelta = xDelta;
        }

        public int getYDelta() {
            return this.yDelta;
        }

        public int getXDelta() {
            return this.xDelta;
        }
    }

    private static void addBox(Box[][] boxGrid, Box box) {
        int boxHeight = box.getHeight();
        int boxWidth = box.getWidth();
        for (int gridYOffset=0, gridY=box.getY(); gridYOffset<boxHeight; gridYOffset++, gridY++) {
            for (int gridXOffset=0, gridX=box.getX(); gridXOffset<boxWidth; gridXOffset++, gridX++) {
                if (boxGrid[gridY][gridX] != null) {
                    throw new IllegalStateException("Box grid already occupied.");
                }
                boxGrid[gridY][gridX] = box;
            }
        }
    }

    private static void removeBox(Box[][] boxGrid, Box box) {
        int boxHeight = box.getHeight();
        int boxWidth = box.getWidth();
        for (int gridYOffset=0, gridY=box.getY(); gridYOffset<boxHeight; gridYOffset++, gridY++) {
            for (int gridXOffset=0, gridX=box.getX(); gridXOffset<boxWidth; gridXOffset++, gridX++) {
                if (boxGrid[gridY][gridX] != box) {
                    throw new IllegalStateException("Box grid and box do not match.");
                }
                boxGrid[gridY][gridX] = null;
            }
        }
    }

    private static int simulate(PrintWriter printWriter, boolean[][] wallGrid, Box[][] boxGrid, int gridHeight, int gridWidth, Direction[] directions, int robotY, int robotX) {
        for (Direction direction : directions) {
            int yDelta = direction.getYDelta();
            int xDelta = direction.getXDelta();
            int newRobotY = robotY + yDelta;
            int newRobotX = robotX + xDelta;
            if (wallGrid[newRobotY][newRobotX]) {
                continue;
            }
            Box rootBox = boxGrid[newRobotY][newRobotX];
            Set<Box> boxesToRemove = Collections.newSetFromMap(new IdentityHashMap<>());
            Set<Box> boxesToAdd = Collections.newSetFromMap(new IdentityHashMap<>());
            boolean movementValid = true;
            if (rootBox != null) {
                Deque<Box> boxesNeedingEffecting = new LinkedList<>();
                boxesNeedingEffecting.addLast(rootBox);
                Box boxNeedingEffecting;
                boxesNeedingEffectingLoop:
                while ((boxNeedingEffecting = boxesNeedingEffecting.pollFirst()) != null) {
                    if (!boxesToRemove.add(boxNeedingEffecting)) {
                        // We have already processed this box.
                        continue;
                    }
                    int newBoxX = boxNeedingEffecting.getX() + xDelta;
                    int newBoxY = boxNeedingEffecting.getY() + yDelta;
                    int boxWidth = boxNeedingEffecting.getWidth();
                    int boxHeight = boxNeedingEffecting.getHeight();
                    boxesToAdd.add(new Box(newBoxX, newBoxY, boxWidth, boxHeight));
                    for (int gridYOffset=0, gridY=newBoxY; gridYOffset<boxHeight; gridYOffset++, gridY++) {
                        for (int gridXOffset=0, gridX=newBoxX; gridXOffset<boxWidth; gridXOffset++, gridX++) {
                            if (wallGrid[gridY][gridX]) {
                                movementValid = false;
                                break boxesNeedingEffectingLoop;
                            }
                            else {
                                Box existingBox = boxGrid[gridY][gridX];
                                if (existingBox != null && existingBox != boxNeedingEffecting) {
                                    boxesNeedingEffecting.addLast(existingBox);
                                }
                            }
                        }
                    }
                }
            }
            if (movementValid) {
                robotY = newRobotY;
                robotX = newRobotX;
                for (Box box : boxesToRemove) {
                    removeBox(boxGrid, box);
                }
                for (Box box : boxesToAdd) {
                    addBox(boxGrid, box);
                }
            }
        }
        Set<Box> allBoxes = Collections.newSetFromMap(new IdentityHashMap<>());
        for (int y=0; y<gridHeight; y++) {
            for (int x=0; x<gridWidth; x++) {
                Box box = boxGrid[y][x];
                if (box != null) {
                    allBoxes.add(box);
                }
            }
        }
        int gpsTotal = 0;
        for (Box box : allBoxes) {
            gpsTotal += (box.getY() * 100) + box.getX();
        }
        return gpsTotal;
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        String[] inputLines = LineReader.stringsArray(inputCharacters, true);
        int inputLineCount = inputLines.length;
        int blankLineIndex = 0;
        for (int inputLineIndex=0; inputLineIndex<inputLineCount; inputLineIndex++) {
            if (inputLines[inputLineIndex].trim().isEmpty()) {
                blankLineIndex = inputLineIndex;
                break;
            }
        }
        int gridHeight = blankLineIndex;
        int partAGridWidth = inputLines[0].length();
        int partBGridWidth = partAGridWidth * 2;
        boolean[][] partAWallGrid = new boolean[gridHeight][partAGridWidth];
        Box[][] partABoxGrid = new Box[gridHeight][partAGridWidth];
        boolean[][] partBWallGrid = new boolean[gridHeight][partBGridWidth];
        Box[][] partBBoxGrid = new Box[gridHeight][partBGridWidth];
        int robotY = -1;
        int partARobotX = -1;
        int partBRobotX = -1;
        boolean robotFound = false;
        for (int y=0; y<gridHeight; y++) {
            char[] inputLine = inputLines[y].toCharArray();
            for (int x=0; x<partAGridWidth; x++) {
                switch (inputLine[x]) {
                    case '#':
                        partAWallGrid[y][x] = true;
                        partBWallGrid[y][x*2] = true;
                        partBWallGrid[y][x*2+1] = true;
                        break;
                    case 'O':
                        addBox(partABoxGrid, new Box(x, y, 1, 1));
                        addBox(partBBoxGrid, new Box(x*2, y, 2, 1));
                        break;
                    case '@':
                        if (robotFound) {
                            throw new IllegalStateException("Multiple robots found.");
                        }
                        robotFound = true;
                        robotY = y;
                        partARobotX = x;
                        partBRobotX = x*2;
                        break;
                    case '.':
                        break;
                    default:
                        throw new IllegalStateException("Unexpected character");
                }
            }
        }
        List<Direction> directionList = new ArrayList<>();
        for (int inputLineIndex=blankLineIndex+1; inputLineIndex<inputLineCount; inputLineIndex++) {
            for (char inputLineCharacter : inputLines[inputLineIndex].toCharArray()) {
                Direction direction;
                switch (inputLineCharacter) {
                    case '^':
                        direction = Direction.NORTH;
                        break;
                    case '>':
                        direction = Direction.EAST;
                        break;
                    case 'v':
                        direction = Direction.SOUTH;
                        break;
                    case '<':
                        direction = Direction.WEST;
                        break;
                    default:
                        throw new IllegalStateException("Unexpected character");
                }
                directionList.add(direction);
            }
        }
        Direction[] directionArray = directionList.toArray(new Direction[0]);
        return new BasicPuzzleResults<>(
            simulate(printWriter, partAWallGrid, partABoxGrid, gridHeight, partAGridWidth, directionArray, robotY, partARobotX),
            simulate(printWriter, partBWallGrid, partBBoxGrid, gridHeight, partBGridWidth, directionArray, robotY, partBRobotX)
        );
    }
}
