package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2024;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.common.AStarSolver;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day18 implements IPuzzle {
    private static final Pattern PATTERN_LINE = Pattern.compile("^(?<x>[0-9]+),(?<y>[0-9]+)$");

    private static Optional<Integer> runAStar(boolean[][] corruptedSquares, int maxCoordinate) {
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
                maxCoordinate,
                0,
                maxCoordinate
            ),
            AStarSolver.CostAdapter.CommonTypes.Of.Integer.INSTANCE,
            new AStarSolver.OptionalResultAdapter<>(new AStarSolver.CostOnlyResultAdapter<>()),
            new AStarSolver.PointNodeAdapter.Point(0,0),
            new AStarSolver.PointNodeAdapter.Point(maxCoordinate,maxCoordinate)
        );
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        int gridSize = Integer.parseInt(new String(configProvider.getPuzzleConfigChars("grid_size")).trim());
        int partABytesFallen = Integer.parseInt(new String(configProvider.getPuzzleConfigChars("part_a_bytes_fallen")).trim());
        int maxCoordinate = gridSize - 1;
        String[] inputLines = LineReader.stringsArray(inputCharacters, true);
        int inputLineCount = inputLines.length;
        if (inputLineCount < partABytesFallen) {
            throw new IllegalStateException("Not enough input lines");
        }
        boolean[][] corruptedSquares = new boolean[gridSize][gridSize];
        Integer partASolution = null;
        String partBSolution = null;
        for (int fallenByteIndex=0; !(partASolution != null && partBSolution != null); fallenByteIndex++) {
            if (fallenByteIndex == partABytesFallen) {
                partASolution = runAStar(corruptedSquares, maxCoordinate).orElseThrow(() -> new IllegalStateException("It is not possible to reach the end after " + partABytesFallen + " bytes have fallen, therefore part A is unsolveable."));
            }
            if (fallenByteIndex == inputLineCount) {
                break;
            }
            Matcher matcher = PATTERN_LINE.matcher(inputLines[fallenByteIndex]);
            if (!matcher.matches()) {
                throw new IllegalStateException("Unable to parse line");
            }
            int x = Integer.parseInt(matcher.group("x"));
            int y = Integer.parseInt(matcher.group("y"));
            corruptedSquares[y][x] = true;
            if (partBSolution == null && !runAStar(corruptedSquares, maxCoordinate).isPresent()) {
                partBSolution = x + "," + y;
            }
        }
        if (partBSolution == null && !partBPotentiallyUnsolvable) {
            throw new IllegalStateException("No solution for part B.");
        }
        return new BasicPuzzleResults<>(
            partASolution,
            partBSolution
        );
    }
}
