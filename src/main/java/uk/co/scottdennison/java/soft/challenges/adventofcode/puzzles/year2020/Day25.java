package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2020;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;

public class Day25 implements IPuzzle {
    private static final long INITIAL_SUBJECT_NUMBER = 7;
    private static final long DIVISOR = 20201227L;

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        String[] lines = LineReader.stringsArray(inputCharacters,true);
        if (lines.length != 2) {
            throw new IllegalStateException("Expected only two input lines");
        }
        long publicKey1 = Long.parseLong(lines[0].trim());
        long publicKey2 = Long.parseLong(lines[1].trim());
        long actor1LoopSize = calculateLoopSize(publicKey1);
        long actor2LoopSize = calculateLoopSize(publicKey2);
        long encryptionKey1 = runLoop(publicKey2,actor1LoopSize);
        long encryptionKey2 = runLoop(publicKey1,actor2LoopSize);
        if (encryptionKey1 != encryptionKey2) {
            throw new IllegalStateException("Something went wrong, and the encryption keys differ.");
        }
        return new BasicPuzzleResults<>(
            encryptionKey1,
            "Pay The Deposit"
        );
    }

    private static long calculateLoopSize(long key) {
        long value = 1;
        long loopSize = 0;
        while (value != key) {
            value = (value * INITIAL_SUBJECT_NUMBER) % DIVISOR;
            loopSize++;
        }
        return loopSize;
    }

    private static long runLoop(long subjectNumber, long loopSize) {
        long value = 1;
        for (long loopNumber=1; loopNumber<=loopSize; loopNumber++) {
            value = (value * subjectNumber) % DIVISOR;
        }
        return value;
    }
}
