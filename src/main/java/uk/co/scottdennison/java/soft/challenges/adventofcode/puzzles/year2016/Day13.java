package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2016;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day13 implements IPuzzle {
    private static class Cubical {
        private final int x;
        private final int y;
        private int steps;
        private boolean visited;

        private Cubical(int x, int y) {
            this.x = x;
            this.y = y;
            this.steps = Integer.MAX_VALUE;
            this.visited = false;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        public int getSteps() {
            return this.steps;
        }

        public void setSteps(int steps) {
            this.steps = steps;
        }

        public boolean isVisited() {
            return this.visited;
        }

        public void markVisited() {
            this.visited = true;
        }
    }

    private static final Pattern PATTERN_CONFIG_COORDINATES = Pattern.compile("^(?<startX>[0-9]+),(?<startY>[0-9]+) -> (?<targetX>[0-9]+),(?<targetY>[0-9]+)$");

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        int officeDesignersFavouriteNumber = Integer.parseInt(new String(inputCharacters).trim());
        Matcher configCoordinatesMatcher = PATTERN_CONFIG_COORDINATES.matcher(new String(configProvider.getPuzzleConfigChars("coordinates")).trim());
        if (!configCoordinatesMatcher.matches()) {
            throw new IllegalStateException("Could not parse config coordinates");
        }
        int startX = Integer.parseInt(configCoordinatesMatcher.group("startX"));
        int startY = Integer.parseInt(configCoordinatesMatcher.group("startY"));
        int targetX = Integer.parseInt(configCoordinatesMatcher.group("targetX"));
        int targetY = Integer.parseInt(configCoordinatesMatcher.group("targetY"));
        if (!isCoordinateACubical(startX,startY,officeDesignersFavouriteNumber)) {
            throw new IllegalStateException("Starting position is a wall");
        }
        Cubical startCubical = new Cubical(startX,startY);
        startCubical.setSteps(0);
        Map<Integer, Map<Integer,Cubical>> cubicals = new HashMap<>();
        Map<Integer,Cubical> cubicalsStartRow = new HashMap<>();
        cubicalsStartRow.put(startX,startCubical);
        cubicals.put(startY,cubicalsStartRow);
        Queue<Cubical> priorityQueue = new PriorityQueue<>(Comparator.comparing(Cubical::getSteps));
        priorityQueue.add(startCubical);
        Integer partASteps = null;
        Integer partBCount = null;
        while (true) {
            Cubical currentCubical = priorityQueue.remove();
            currentCubical.markVisited();
            int currentX = currentCubical.getX();
            int currentY = currentCubical.getY();
            int currentSteps = currentCubical.getSteps();
            if (partASteps == null && currentX == targetX && currentY == targetY) {
                partASteps = currentCubical.getSteps();
                if (partBCount != null) {
                    break;
                }
            }
            if (currentSteps >= 50) {
                int partBCountPrimitive = 0;
                for (Map<Integer,Cubical> cubicalRow : cubicals.values()) {
                    for (Cubical cubical : cubicalRow.values()) {
                        if (cubical.isVisited() && cubical.getSteps() <= 50) {
                            partBCountPrimitive++;
                        }
                    }
                }
                partBCount = partBCountPrimitive;
                if (partASteps != null) {
                    break;
                }
            }
            checkRoute(priorityQueue,cubicals,officeDesignersFavouriteNumber,currentSteps,currentY-1,currentX);
            checkRoute(priorityQueue,cubicals,officeDesignersFavouriteNumber,currentSteps,currentY+1,currentX);
            checkRoute(priorityQueue,cubicals,officeDesignersFavouriteNumber,currentSteps,currentY,currentX-1);
            checkRoute(priorityQueue,cubicals,officeDesignersFavouriteNumber,currentSteps,currentY,currentX+1);
        }
        return new BasicPuzzleResults<>(
            partASteps,
            partBCount
        );
    }

    private static void checkRoute(Queue<Cubical> priorityQueue, Map<Integer,Map<Integer,Cubical>> cubicals, int officeDesignersFavouriteNumber, int currentSteps, int neighbourY, int neighbourX) {
        if (neighbourX >= 0 && neighbourY >= 0) {
            Map<Integer,Cubical> cubicalsForRow = cubicals.get(neighbourY);
            Cubical neighbourCubical = cubicalsForRow==null?null:cubicalsForRow.get(neighbourX);
            boolean newlyAdded = false;
            if (neighbourCubical == null) {
                if (Integer.bitCount((neighbourX*neighbourX)+(3*neighbourX)+(2*neighbourX*neighbourY)+neighbourY+(neighbourY*neighbourY)+officeDesignersFavouriteNumber) % 2 == 0) {
                    if (cubicalsForRow == null) {
                        cubicalsForRow = new HashMap<>();
                        cubicals.put(neighbourY,cubicalsForRow);
                    }
                    neighbourCubical = new Cubical(neighbourX,neighbourY);
                    cubicalsForRow.put(neighbourX,neighbourCubical);
                    newlyAdded = true;
                }
            }
            if (neighbourCubical != null && !neighbourCubical.isVisited()) {
                int routeSteps = currentSteps + 1;
                if (routeSteps < neighbourCubical.getSteps()) {
                    neighbourCubical.setSteps(routeSteps);
                    if (!newlyAdded) {
                        priorityQueue.remove(neighbourCubical);
                    }
                    priorityQueue.add(neighbourCubical);
                }
            }
        }
    }

    private boolean isCoordinateACubical(int x, int y, int officeDesignersFavouriteNumber) {
        return Integer.bitCount((x*x)+(3*x)+(2*x*y)+y+(y*y)+officeDesignersFavouriteNumber) % 2 == 0;
    }
}
