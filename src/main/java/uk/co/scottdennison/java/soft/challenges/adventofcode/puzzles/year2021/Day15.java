package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2021;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.common.AStarSolver;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;

public class Day15 implements IPuzzle {
    private static final int TILE_MULTIPLES = 5;

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        char[][] inputCharacters2D = LineReader.charArraysArray(inputCharacters,true);
        int partAHeight = inputCharacters2D.length;
        int partAWidth = inputCharacters2D[0].length;
        int partBHeight = partAHeight*TILE_MULTIPLES;
        int partBWidth = partAWidth*TILE_MULTIPLES;
        Integer[][] partARisk = new Integer[partAHeight][partAWidth];
        Integer[][] partBRisk = new Integer[partBHeight][partBWidth];
        for (int y=0; y<partAHeight; y++) {
            char[] inputCharacters2DRow = inputCharacters2D[y];
            if (inputCharacters2DRow.length != partAWidth) {
                throw new IllegalStateException("Jagged input");
            }
            for (int x=0; x<partAWidth; x++) {
                char inputCharacter = inputCharacters2DRow[x];
                if (inputCharacter >= '0' && inputCharacter <= '9') {
                    int partAInputValue = inputCharacter-'0';
                    partARisk[y][x] = partAInputValue;
                    for (int tileY=0; tileY<TILE_MULTIPLES; tileY++) {
                        for (int tileX=0; tileX<TILE_MULTIPLES; tileX++) {
                            int partBInputValue = partAInputValue + tileY + tileX;
                            while (partBInputValue > 9) {
                                partBInputValue -= 9; // So it wraps around to 1 not 0
                            }
                            partBRisk[tileY*partAHeight+y][tileX*partAWidth+x] = partBInputValue;
                        }
                    }
                } else {
                    throw new IllegalStateException("Invalid input character");
                }
            }
        }
        return new BasicPuzzleResults<>(
            runAStar(partARisk,partAHeight,partAWidth),
            runAStar(partBRisk,partBHeight,partBWidth)
        );
    }

    private int runAStar(Integer[][] risk, int height, int width) {
        return AStarSolver.run(
            new AStarSolver.PointNodeAdapter<>(
                AStarSolver.PointNodeAdapter.AlwaysTrueCanMoveAdapter.INSTANCE,
                (linkedFromPoint, linkedToPoint) -> risk[linkedToPoint.getY()][linkedToPoint.getX()],
                AStarSolver.PointNodeAdapter.EstimatedMoveCostAdapter.CommonAlgorithms.Manhattan.Of.Integer.INSTANCE,
                0,
                height-1,
                0,
                width-1
            ),
            AStarSolver.CostAdapter.CommonTypes.Of.Integer.INSTANCE,
            new AStarSolver.PointNodeAdapter.Point(0,0),
            new AStarSolver.PointNodeAdapter.Point(height-1,width-1)
        ).orElseThrow(() -> new IllegalStateException("Unable to solve")).getCost();
    }
}
