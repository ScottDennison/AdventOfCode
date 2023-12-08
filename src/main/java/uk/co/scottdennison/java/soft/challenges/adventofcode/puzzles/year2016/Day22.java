package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2016;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day22 implements IPuzzle {
    private static final boolean PRINT = false;

    private static final Pattern PATTERN_FULL_INPUT = Pattern.compile("^(?<user>[a-z0-9\\-]+)@(?<device>[a-z0-9\\-]+)# df -h\\nFilesystem +Size +Used +Avail +Use% *\\n(?<fileSystems>.+?)\\n*$",Pattern.DOTALL);
    private static final Pattern PATTERN_FILESYSTEM = Pattern.compile("^/dev/grid/node-x(?<x>[0-9]+)-y(?<y>[0-9]+) +(?<size>[0-9]+)T +(?<used>[0-9]+)T +(?<avail>[0-9]+)T +(?<usePercent>[0-9]+)% *$");

    private static class FileSystem {
        private final int x;
        private final int y;
        private final int size;
        private final int used;
        private final int avail;
        private final int usePercent;

        public FileSystem(int x, int y, int size, int used, int avail, int usePercent) {
            if (size != (used + avail)) {
                throw new IllegalArgumentException("Size doesn't marry up");
            }
            this.x = x;
            this.y = y;
            this.size = size;
            this.used = used;
            this.avail = avail;
            this.usePercent = usePercent;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        public int getSize() {
            return this.size;
        }

        public int getUsed() {
            return this.used;
        }

        public int getAvail() {
            return this.avail;
        }

        public int getUsePercent() {
            return this.usePercent;
        }
    }

    private static enum FileSystemClassification {
        EMPTY ('E'),
        STANDARD ('.'),
        REQUIRED_DATA ('!'),
        TOO_FULL ('F');

        private final char character;

        FileSystemClassification(char character) {
            this.character = character;
        }

        public char getCharacter() {
            return this.character;
        }
    };

    private static class AStarPoint {
        private final int x;
        private final int y;
        private AStarPoint cameFrom = null;
        private int fScore = Integer.MAX_VALUE;
        private int gScore = Integer.MAX_VALUE;

        private AStarPoint(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        public AStarPoint getCameFrom() {
            return this.cameFrom;
        }

        public void setCameFrom(AStarPoint cameFrom) {
            this.cameFrom = cameFrom;
        }

        public int getFScore() {
            return this.fScore;
        }

        public void setFScore(int hScore) {
            this.fScore = hScore;
        }

        public int getGScore() {
            return this.gScore;
        }

        public void setGScore(int gScore) {
            this.gScore = gScore;
        }

        @Override
        public boolean equals(Object otherObject) {
            if (this == otherObject) return true;
            if (otherObject == null || this.getClass() != otherObject.getClass()) return false;

            AStarPoint otherAStarPoint = (AStarPoint) otherObject;

            if (this.x != otherAStarPoint.x) return false;
            if (this.y != otherAStarPoint.y) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = this.x;
            result = 31 * result + this.y;
            return result;
        }
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        Matcher fullInputMatcher = PATTERN_FULL_INPUT.matcher(new String(inputCharacters));
        if (!fullInputMatcher.matches()) {
            throw new IllegalStateException("Could not parse full input");
        }
        String[] fileSystemLines = LineReader.stringsArray(fullInputMatcher.group("fileSystems").toCharArray(), true);
        int fileSystemsCount = fileSystemLines.length;
        FileSystem[] fileSystemArray = new FileSystem[fileSystemsCount];
        for (int fileSystemIndex=0; fileSystemIndex<fileSystemsCount; fileSystemIndex++) {
            Matcher fileSystemMatcher = PATTERN_FILESYSTEM.matcher(fileSystemLines[fileSystemIndex]);
            if (!fileSystemMatcher.matches()) {
                throw new IllegalStateException("Could not parse filesystem");
            }
            fileSystemArray[fileSystemIndex] = new FileSystem(
                Integer.parseInt(fileSystemMatcher.group("x")),
                Integer.parseInt(fileSystemMatcher.group("y")),
                Integer.parseInt(fileSystemMatcher.group("size")),
                Integer.parseInt(fileSystemMatcher.group("used")),
                Integer.parseInt(fileSystemMatcher.group("avail")),
                Integer.parseInt(fileSystemMatcher.group("usePercent"))
            );
        }
        int viablePairs = 0;
        for (int fileSystemIndexA=0; fileSystemIndexA<fileSystemsCount; fileSystemIndexA++) {
            FileSystem fileSystemA = fileSystemArray[fileSystemIndexA];
            int fileSystemAUsed = fileSystemA.getUsed();
            if (fileSystemAUsed > 0) {
                for (int fileSystemIndexB=0; fileSystemIndexB<fileSystemsCount; fileSystemIndexB++) {
                    if (fileSystemIndexA != fileSystemIndexB) {
                        FileSystem fileSystemB = fileSystemArray[fileSystemIndexB];
                        if (fileSystemB.getAvail() >= fileSystemAUsed) {
                            viablePairs++;
                        }
                    }
                }
            }
        }
        int maxX = Arrays.stream(fileSystemArray).mapToInt(FileSystem::getX).max().getAsInt();
        int maxY = Arrays.stream(fileSystemArray).mapToInt(FileSystem::getY).max().getAsInt();
        FileSystem emptyFileSystem = null;
        for (FileSystem fileSystem : fileSystemArray) {
            if (fileSystem.getUsed() == 0) {
                if (emptyFileSystem != null) {
                    throw new IllegalStateException("Multiple empty file system, this solution cannot handle this scenario.");
                }
                emptyFileSystem = fileSystem;
            }
        }
        if (emptyFileSystem == null) {
            throw new IllegalStateException("No empty file system, this solution cannot handle this scenario.");
        }
        int emptyFileSystemY = emptyFileSystem.getY();
        int emptyFileSystemX = emptyFileSystem.getX();
        int emptyFileSystemSize = emptyFileSystem.getSize();
        FileSystemClassification[][] fileSystemClassificationGrid = new FileSystemClassification[maxY+1][maxX+1];
        for (FileSystem fileSystem : fileSystemArray) {
            fileSystemClassificationGrid[fileSystem.getY()][fileSystem.getX()] = fileSystem.getUsed() > emptyFileSystemSize ? FileSystemClassification.TOO_FULL : FileSystemClassification.STANDARD;
        }
        FileSystemClassification[] fileSystemClassificationGridFirstRow = fileSystemClassificationGrid[0];
        for (int x=0; x<=maxX; x++) {
            if (fileSystemClassificationGridFirstRow[x] == FileSystemClassification.TOO_FULL) {
                throw new IllegalStateException("First row contains a too full node, this solution cannot handle this scenario.");
            }
        }
        fileSystemClassificationGrid[emptyFileSystemY][emptyFileSystemX] = FileSystemClassification.EMPTY;
        fileSystemClassificationGrid[0][maxX] = FileSystemClassification.REQUIRED_DATA;
        printGrid(fileSystemClassificationGrid, maxY, maxX, printWriter);
        int moveOperations = moveBetween(fileSystemClassificationGrid, emptyFileSystemY, emptyFileSystemX, 0, maxX-1, maxY, maxX, printWriter);
        for (int topSwapStep=maxX-1; topSwapStep>0; topSwapStep--) {
            moveOperations += swapBetween(fileSystemClassificationGrid, 0, topSwapStep, 0, topSwapStep+1, maxY, maxX, printWriter);
            moveOperations += moveBetween(fileSystemClassificationGrid, 0, topSwapStep+1, 0, topSwapStep-1, maxY, maxX, printWriter);
        }
        moveOperations += swapBetween(fileSystemClassificationGrid,0,1,0,0, maxY, maxX, printWriter);
        return new BasicPuzzleResults<>(
            viablePairs,
            moveOperations
        );
    }

    private static int moveBetweenAStarHeuristic(int fromY, int fromX, int toY, int toX) {
        return Math.abs(toY-fromY)+Math.abs(toX-fromX);
    }

    private static void moveBetweenCheckNeighbour(FileSystemClassification[][] fileSystemClassificationGrid, PriorityQueue<AStarPoint> openSet, AStarPoint[][] knownPoints, AStarPoint currentPoint, int neighbourY, int neighbourX, int targetY, int targetX) {
        if (fileSystemClassificationGrid[neighbourY][neighbourX] != FileSystemClassification.STANDARD) {
            return;
        }
        AStarPoint neighbourPoint = knownPoints[neighbourY][neighbourX];
        boolean newPoint = false;
        if (neighbourPoint == null) {
            newPoint = true;
            neighbourPoint = new AStarPoint(neighbourX,neighbourY);
            knownPoints[neighbourY][neighbourX] = neighbourPoint;
        }
        int potentialGScore = currentPoint.getGScore() + 1;
        if (potentialGScore < neighbourPoint.getGScore()) {
            if (!newPoint) {
                openSet.remove(neighbourPoint);
            }
            neighbourPoint.setCameFrom(currentPoint);
            neighbourPoint.setGScore(potentialGScore);
            neighbourPoint.setFScore(potentialGScore + moveBetweenAStarHeuristic(neighbourY,neighbourX,targetY,targetX));
            openSet.add(neighbourPoint);
        }
    }

    private static int moveBetween(FileSystemClassification[][] fileSystemClassificationGrid, int sourceY, int sourceX, int targetY, int targetX, int maxY, int maxX, PrintWriter printWriter) {
        PriorityQueue<AStarPoint> openSet = new PriorityQueue<>(Comparator.comparing(AStarPoint::getFScore));
        AStarPoint[][] knownPoints = new AStarPoint[maxY+1][maxX+1];
        AStarPoint startPoint = new AStarPoint(sourceX, sourceY);
        startPoint.setGScore(0);
        startPoint.setFScore(moveBetweenAStarHeuristic(sourceY,sourceX,targetY,targetX));
        knownPoints[sourceY][sourceX] = startPoint;
        openSet.add(startPoint);
        while (true) {
            AStarPoint aStarPoint = openSet.poll();
            if (aStarPoint == null) {
                throw new IllegalStateException("No route found.");
            }
            if (aStarPoint.getY() == targetY && aStarPoint.getX() == targetX) {
                Deque<AStarPoint> steps = new LinkedList<>();
                steps.addFirst(aStarPoint);
                while (true) {
                    aStarPoint = aStarPoint.getCameFrom();
                    if (aStarPoint == null) {
                        break;
                    }
                    steps.addFirst(aStarPoint);
                }
                int moveOperations = 0;
                AStarPoint moveFromPoint = steps.removeFirst();
                while (true) {
                    AStarPoint moveToPoint = steps.pollFirst();
                    if (moveToPoint == null) {
                        break;
                    }
                    moveOperations += swapBetween(fileSystemClassificationGrid, moveFromPoint.getY(), moveFromPoint.getX(), moveToPoint.getY(), moveToPoint.getX(), maxY, maxX, printWriter);
                    moveFromPoint = moveToPoint;
                }
                return moveOperations;
            }
            int y = aStarPoint.getY();
            int x = aStarPoint.getX();
            for (int yDelta=-1; yDelta<=1; yDelta+=2) {
                int newY = y+yDelta;
                if (newY >= 0 && newY <= maxY) {
                    moveBetweenCheckNeighbour(fileSystemClassificationGrid,openSet,knownPoints,aStarPoint,newY,x,targetY,targetX);
                }
            }
            for (int xDelta=-1; xDelta<=1; xDelta+=2) {
                int newX = x+xDelta;
                if (newX >= 0 && newX <= maxX) {
                    moveBetweenCheckNeighbour(fileSystemClassificationGrid,openSet,knownPoints,aStarPoint,y,newX,targetY,targetX);
                }
            }
        }
    }

    private static int swapBetween(FileSystemClassification[][] fileSystemClassificationGrid, int sourceY, int sourceX, int targetY, int targetX, int maxY, int maxX, PrintWriter printWriter) {
        FileSystemClassification temp = fileSystemClassificationGrid[sourceY][sourceX];
        fileSystemClassificationGrid[sourceY][sourceX] = fileSystemClassificationGrid[targetY][targetX];
        fileSystemClassificationGrid[targetY][targetX] = temp;
        printGrid(fileSystemClassificationGrid, maxY, maxX, printWriter);
        return 1;
    }

    private static void printGrid(FileSystemClassification[][] fileSystemClassificationGrid, int maxY, int maxX, PrintWriter printWriter) {
        if (PRINT) {
            for (int y=0; y<=maxY; y++) {
                FileSystemClassification[] fileSystemClassificationGridRow = fileSystemClassificationGrid[y];
                for (int x=0; x<=maxX; x++) {
                    printWriter.print(fileSystemClassificationGridRow[x].getCharacter());
                }
                printWriter.println();
            }
            printWriter.println();
            printWriter.flush();
        }
    }
}
