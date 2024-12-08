package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2018;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.PrimitiveIterator;

public class Day08 implements IPuzzle {
    private static class IntArrayIterator implements PrimitiveIterator.OfInt {
        private final int[] intArrayReference;
        private int nextIndex;

        private IntArrayIterator(int[] intArrayReference) {
            this.intArrayReference = intArrayReference;
            this.nextIndex = 0;
        }

        @Override
        public int nextInt() {
            return intArrayReference[nextIndex++];
        }

        @Override
        public boolean hasNext() {
            return nextIndex < intArrayReference.length;
        }
    }

    private static int parseNodesPartA(PrimitiveIterator.OfInt licenseFileIterator) {
        int childNodeCount = licenseFileIterator.nextInt();
        int metadataEntryCount = licenseFileIterator.nextInt();
        int total = 0;
        for (int childNodeIndex=0; childNodeIndex<childNodeCount; childNodeIndex++) {
            total += parseNodesPartA(licenseFileIterator);
        }
        for (int metadataEntryIndex=0; metadataEntryIndex<metadataEntryCount; metadataEntryIndex++) {
            total += licenseFileIterator.nextInt();
        }
        return total;
    }

    private static int parseNodesPartB(PrimitiveIterator.OfInt licenseFileIterator) {
        int childNodeCount = licenseFileIterator.nextInt();
        int metadataEntryCount = licenseFileIterator.nextInt();
        int total = 0;
        if (childNodeCount > 0) {
            int[] childNodeTotals = new int[childNodeCount];
            for (int childNodeIndex = 0; childNodeIndex < childNodeCount; childNodeIndex++) {
                childNodeTotals[childNodeIndex] += parseNodesPartB(licenseFileIterator);
            }
            for (int metadataEntryIndex=0; metadataEntryIndex<metadataEntryCount; metadataEntryIndex++) {
                int metadataEntry = licenseFileIterator.nextInt();
                if (metadataEntry >= 1 && metadataEntry <= childNodeCount) {
                    total += childNodeTotals[metadataEntry - 1];
                }
            }
        }
        else {
            for (int metadataEntryIndex=0; metadataEntryIndex<metadataEntryCount; metadataEntryIndex++) {
                total += licenseFileIterator.nextInt();
            }
        }
        return total;
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        String[] inputStrings = new String(inputCharacters).trim().split(" ");
        int inputCount = inputStrings.length;
        int[] inputInts = new int[inputCount];
        for (int inputIndex=0; inputIndex<inputCount; inputIndex++) {
            inputInts[inputIndex] = Integer.parseInt(inputStrings[inputIndex]);
        }
        return new BasicPuzzleResults<>(
            parseNodesPartA(new IntArrayIterator(inputInts)),
            parseNodesPartB(new IntArrayIterator(inputInts))
        );
    }
}
