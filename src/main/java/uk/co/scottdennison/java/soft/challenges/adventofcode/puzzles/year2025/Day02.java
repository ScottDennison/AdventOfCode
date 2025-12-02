package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2025;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class Day02 implements IPuzzle {
    private static final int LONG_MAX_SIZE = (int)Math.log10(Long.MAX_VALUE);
    private static final boolean LOG = false;

    private static long sumSet(Set<Long> invalidIDs) {
        return invalidIDs.stream().mapToLong(Long::longValue).reduce(0L, Math::addExact);
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        Set<Long> partAInvalidIDs = new HashSet<>();
        Set<Long> partBInvalidIDs = new HashSet<>();
        for (String groupString : new String(inputCharacters).trim().split(",")) {
            int dividerIndex = groupString.indexOf('-');
            if (dividerIndex == -1) {
                throw new IllegalStateException("Could not parse group.");
            }
            long inputRangeStart = Long.parseLong(groupString.substring(0, dividerIndex));
            long inputRangeEnd = Long.parseLong(groupString.substring(dividerIndex + 1));
            if (LOG) printWriter.println("Input range: " + inputRangeStart + " - " + inputRangeEnd);
            int startSize = (int) Math.log10(inputRangeStart) + 1;
            int endSize = (int) Math.log10(inputRangeEnd) + 1;
            for (int size = startSize; size <= endSize; size++) {
                long rangeStart = Math.max(inputRangeStart, (long) Math.pow(10, size - 1));
                long rangeEnd = Math.min(inputRangeEnd, size == LONG_MAX_SIZE ? Long.MAX_VALUE : ((long) Math.pow(10, size) - 1));
                if (LOG) printWriter.println("\tSize: " + size + ", Range: " + rangeStart + " - " + rangeEnd);
                for (int repetitions = 2; repetitions <= size; repetitions++) {
                    int partSize = size / repetitions;
                    if ((partSize * repetitions) == size) {
                        long partDivisor = (long) Math.pow(10, size - partSize);
                        long partMultiplier = (long) Math.pow(10, partSize);
                        long partRangeStart = rangeStart / partDivisor;
                        long partRangeEnd = (rangeEnd / partDivisor) + 1;
                        if (((int) Math.log10(partRangeEnd)) == partSize) {
                            partRangeEnd--;
                        }
                        if (LOG) printWriter.println("\t\tRepetitions: " + repetitions + ", Part Size: " + partSize + ", Part range: " + partRangeStart + " - " + partRangeEnd);
                        for (long part = partRangeStart; part <= partRangeEnd; part++) {
                            long full = 0;
                            for (int reptition = 1; reptition <= repetitions; reptition++) {
                                full = Math.addExact(Math.multiplyExact(full, partMultiplier), part);
                            }
                            if (full >= rangeStart && full <= rangeEnd) {
                                if (repetitions == 2) {
                                    partAInvalidIDs.add(full);
                                }
                                if (partBInvalidIDs.add(full)) {
                                    if (LOG) printWriter.println("\t\t\tInvalid ID: " + full);
                                }
                                else {
                                    if (LOG) printWriter.println("\t\t\tDuplicate Invalid ID: " + full);
                                }
                            }
                        }
                    }
                }
            }
        }
        return new BasicPuzzleResults<>(sumSet(partAInvalidIDs), sumSet(partBInvalidIDs));
    }
}
