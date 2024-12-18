package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2024;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.common.AStarSolver;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day18 implements IPuzzle {
    private static final class Coordinate {
        private final int x;
        private final int y;

        public Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }
    }

    private static final Pattern PATTERN_LINE = Pattern.compile("^(?<x>[0-9]+),(?<y>[0-9]+)$");

    private static <ResultType> ResultType runAStar(boolean[][] corruptedSquares, int maxCoordinateNumber, AStarSolver.FullResultAdapter<AStarSolver.PointNodeAdapter.Point,Integer,ResultType> resultAdapter) {
        return AStarSolver.run(
            new AStarSolver.PointNodeAdapter<Integer>(
                new AStarSolver.PointNodeAdapter.CanMoveAdapter() {
                    @Override
                    public boolean canMoveBetweenLinkedPoints(AStarSolver.PointNodeAdapter.Point linkedFromPoint, AStarSolver.PointNodeAdapter.Point linkedToPoint) {
                        return !corruptedSquares[linkedToPoint.getY()][linkedToPoint.getX()];
                    }
                },
                AStarSolver.PointNodeAdapter.UnchangingActualMoveCostAdapter.One.Of.INTEGER,
                AStarSolver.PointNodeAdapter.EstimatedMoveCostAdapter.CommonAlgorithms.Manhattan.Of.Integer.INSTANCE,
                0,
                maxCoordinateNumber,
                0,
                maxCoordinateNumber
            ),
            AStarSolver.CostAdapter.CommonTypes.Of.Integer.INSTANCE,
            resultAdapter,
            new AStarSolver.PointNodeAdapter.Point(0,0),
            new AStarSolver.PointNodeAdapter.Point(maxCoordinateNumber,maxCoordinateNumber)
        );
    }

    private static int runPartA(Coordinate[] fallingByteCoordinates, int gridSize, int partABytesFallen) {
        boolean[][] corruptedSquares = new boolean[gridSize][gridSize];
        int maxCoordinateNumber = gridSize - 1;
        AStarSolver.FullResultAdapter<AStarSolver.PointNodeAdapter.Point,Integer,Integer> resultAdapter = new AStarSolver.ThrowingResultAdapter<>(new AStarSolver.CostOnlyResultAdapter<>());
        for (int fallingByteIndex=0; fallingByteIndex<partABytesFallen; fallingByteIndex++) {
            Coordinate fallingByteCoordinate = fallingByteCoordinates[fallingByteIndex];
            corruptedSquares[fallingByteCoordinate.getY()][fallingByteCoordinate.getX()] = true;
        }
        return runAStar(corruptedSquares, maxCoordinateNumber, resultAdapter);
    }

    private static String runPartB(Coordinate[] fallingByteCoordinates, int gridSize) {
        boolean[][] corruptedSquares = new boolean[gridSize][gridSize];
        int maxCoordinateNumber = gridSize - 1;
        AStarSolver.FullResultAdapter<AStarSolver.PointNodeAdapter.Point,Integer,Optional<AStarSolver.PointNodeAdapter.Point[]>> resultAdapter = new AStarSolver.OptionalResultAdapter<>(new AStarSolver.SingleRouteResultAdapter<>(AStarSolver.PointNodeAdapter.Point.class, AStarSolver.SingleRouteResultAdapter.MultiplePossibleRoutesBehaviour.PICK_ARTIBTARILY));
        Set<AStarSolver.PointNodeAdapter.Point> currentRoutePoints = null;
        for (Coordinate fallingByteCoordinate : fallingByteCoordinates) {
            int fallingByteCoordinateX = fallingByteCoordinate.getX();
            int fallingByteCoordinateY = fallingByteCoordinate.getY();
            corruptedSquares[fallingByteCoordinateY][fallingByteCoordinateX] = true;
            if (currentRoutePoints == null || currentRoutePoints.contains(new AStarSolver.PointNodeAdapter.Point(fallingByteCoordinateY, fallingByteCoordinateX))) {
                Optional<AStarSolver.PointNodeAdapter.Point[]> aStarResult = runAStar(corruptedSquares, maxCoordinateNumber, resultAdapter);
                if (!aStarResult.isPresent()) {
                    return fallingByteCoordinateX + "," + fallingByteCoordinateY;
                }
                currentRoutePoints = new HashSet<>(Arrays.asList(aStarResult.get()));
            }
        }
        throw new IllegalStateException("The end coordinate is still reachable from the start coordinate after all falling bytes have fallen.");
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        int gridSize = Integer.parseInt(new String(configProvider.getPuzzleConfigChars("grid_size")).trim());
        int partABytesFallen = Integer.parseInt(new String(configProvider.getPuzzleConfigChars("part_a_bytes_fallen")).trim());
        String[] inputLines = LineReader.stringsArray(inputCharacters, true);
        int fallingByteCount = inputLines.length;
        if (fallingByteCount < partABytesFallen) {
            throw new IllegalStateException("Not enough input lines");
        }
        Coordinate[] fallingByteCoordinates = new Coordinate[fallingByteCount];
        for (int fallingByteIndex=0; fallingByteIndex<fallingByteCount; fallingByteIndex++) {
            Matcher matcher = PATTERN_LINE.matcher(inputLines[fallingByteIndex]);
            if (!matcher.matches()) {
                throw new IllegalStateException("Unable to parse line");
            }
            fallingByteCoordinates[fallingByteIndex] = new Coordinate(Integer.parseInt(matcher.group("x")), Integer.parseInt(matcher.group("y")));
        }
        return new BasicPuzzleResults<>(
            runPartA(fallingByteCoordinates,gridSize,partABytesFallen),
            runPartB(fallingByteCoordinates,gridSize)
        );
    }
}
