package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2024;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.ListIterator;

public class Day09 implements IPuzzle {
    private static class SpaceInfo {
        private final int size;
        private final int startPosition;

        public SpaceInfo(int size, int startPosition) {
            this.size = size;
            this.startPosition = startPosition;
        }

        public int getSize() {
            return size;
        }

        public int getStartPosition() {
            return startPosition;
        }
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        // Parsing
        char[] trimmedInputCharacters = new String(inputCharacters).trim().toCharArray();
        int inputSize = inputCharacters.length;
        int[] input = new int[inputSize + 1];
        for (int inputCharacterIndex=0; inputCharacterIndex<inputSize; inputCharacterIndex++) {
            char inputCharacter = inputCharacters[inputCharacterIndex];
            if (inputCharacter < '0' || inputCharacter > '9') {
                throw new IllegalStateException("Unexpected input character.");
            }
            input[inputCharacterIndex] = inputCharacter - '0';
        }
        if (inputSize % 2 == 1) {
            input[inputSize++] = 0;
        }

        // Pre-processing
        int[] partADiskLayout = new int[inputSize * 10];
        int partAStartPointer = -1;
        int partAEndPointer = 0;
        SpaceInfo[] partBFileInfoEntries = new SpaceInfo[inputSize/2];
        LinkedList<SpaceInfo> partBFreeSpaceEntries = new LinkedList<>();
        int inputIndex = 0;
        int fileID = 0;
        while (inputIndex < inputSize) {
            int fileSize = input[inputIndex++];
            int freeSpaceSize = input[inputIndex++];
            partBFileInfoEntries[fileID] = new SpaceInfo(fileSize, partAEndPointer);
            for (int fileByteIndex=0; fileByteIndex<fileSize; fileByteIndex++) {
                partADiskLayout[partAEndPointer++] = fileID;
            }
            if (freeSpaceSize > 0) {
                partBFreeSpaceEntries.add(new SpaceInfo(freeSpaceSize, partAEndPointer));
            }
            for (int freeSpaceByteIndex=0; freeSpaceByteIndex<freeSpaceSize; freeSpaceByteIndex++) {
                partADiskLayout[partAEndPointer++] = -1;
            }
            fileID++;
        }

        // Part A
        long partAChecksum = 0;
        while (++partAStartPointer < partAEndPointer) {
            int diskLayoutValue = partADiskLayout[partAStartPointer];
            while (diskLayoutValue == -1) {
                diskLayoutValue = partADiskLayout[partAStartPointer] = partADiskLayout[--partAEndPointer];
            }
            if (partAStartPointer < partAEndPointer) {
                partAChecksum += diskLayoutValue * partAStartPointer;
            }
        }

        // Part B
        long partBChecksum = 0;
        for (fileID--; fileID>=0; fileID--) {
            SpaceInfo fileInfo = partBFileInfoEntries[fileID];
            int fileSize = fileInfo.getSize();
            int fileStartPosition = fileInfo.getStartPosition();
            ListIterator<SpaceInfo> freeSpaceEntriesIterator = partBFreeSpaceEntries.listIterator();
            while (freeSpaceEntriesIterator.hasNext()) {
                SpaceInfo freeSpaceInfo = freeSpaceEntriesIterator.next();
                int newFreeSpaceSize = freeSpaceInfo.getSize() - fileSize;
                if (newFreeSpaceSize < 0) {
                    continue;
                }
                int freeSpaceStartPosition = freeSpaceInfo.getStartPosition();
                if (freeSpaceStartPosition > fileStartPosition) {
                    break;
                }
                fileStartPosition = freeSpaceStartPosition;
                if (newFreeSpaceSize > 0) {
                    freeSpaceEntriesIterator.set(new SpaceInfo(newFreeSpaceSize, freeSpaceStartPosition + fileSize));
                }
                else {
                    freeSpaceEntriesIterator.remove();
                }
                break;
            }
            int fileEndPosition = fileStartPosition + fileSize;
            for (int byteIndex=fileStartPosition; byteIndex<fileEndPosition; byteIndex++) {
                partBChecksum += byteIndex * fileID;
            }
        }

        // Result
        return new BasicPuzzleResults<>(
            partAChecksum,
            partBChecksum
        );
    }
}
