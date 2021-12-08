package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2015;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;

public class Day20 implements IPuzzle {
    /*
    In happy coincidence, when trying to come up with a non-brute-force solution, I actually found a no-code solution to part 1
    Look up http://oeis.org/A034885/b034885.txt, find N where vthe alue is >= puzzle input, then use http://oeis.org/A002093/b002093.txt to look up that Nth term
    */

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        int target = Integer.parseInt(new String(inputCharacters).trim());
        return new BasicPuzzleResults<>(
            run(target, 10, elf -> target / elf),
            run(target, 11, elf -> 50)
        );
    }

    private int run(int target, int presentsMultiplierPerVisit, IntUnaryOperator visitsFunction) {
        int maxIterations = target/presentsMultiplierPerVisit;
        int[] housePresents = new int[maxIterations+1];
        for (int elf=1; elf<=maxIterations; elf++) {
            int visits = visitsFunction.applyAsInt(elf);
            int presentsToGive = elf*presentsMultiplierPerVisit;
            for (int visit=1, house=elf; visit<=visits && house<=maxIterations; visit++, house+=elf) {
                housePresents[house] += presentsToGive;
            }
            if (housePresents[elf] >= target) {
                return elf;
            }
        }
        throw new IllegalStateException("Not solvable.");
    }

/*
        int x=3400000;
        int m=10;
        int v[] = new int[x+1];
        for (int i=1; i<=x; i++) {
            int z=x/i;
            for (int j=1; j<=z; j++) {
                int k=i*j;
                if (k>x) {
                    break;
                }
                v[k] += i*m;
            }
            if (v[i] >= 34000000) {
                System.out.println(i);
                return;
            }
        }
 */
}
