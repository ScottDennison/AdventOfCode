package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2016;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;

public class Day16 implements IPuzzle {
    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        inputCharacters = new String(inputCharacters).trim().toCharArray();
        int diskContentsLength = inputCharacters.length;
        boolean[] diskContents = new boolean[diskContentsLength];
        for (int index = 0; index < diskContentsLength; index++) {
            boolean diskContent;
            switch (inputCharacters[index]) {
                case '0':
                    diskContent = false;
                    break;
                case '1':
                    diskContent = true;
                    break;
                default:
                    throw new IllegalStateException("Unexpected character");
            }
            diskContents[index] = diskContent;
        }
        return new BasicPuzzleResults<>(
            run(configProvider, diskContents, "a"),
            run(configProvider, diskContents, "b")
        );
    }

    private String run(IPuzzleConfigProvider configProvider, boolean[] diskContents, String part) {
        int diskLength = Integer.parseInt(new String(configProvider.getPuzzleConfigChars("part_" + part + "_disk_length")).trim());
        if (diskLength <= 0 || diskLength % 2 != 0) {
            throw new IllegalStateException("Disk length must be an positive multiple of 2");
        }
        int diskContentsLength = diskContents.length;
        while (diskContentsLength < diskLength) {
            int newDiscContentsLength = diskContentsLength*2+1;
            boolean[] newDiskContents = new boolean[newDiscContentsLength];
            System.arraycopy(diskContents,0,newDiskContents,0,diskContentsLength);
            for (int sourceIndex=diskContentsLength-1, destinationIndex=diskContentsLength+1; sourceIndex>=0; sourceIndex--, destinationIndex++) {
                newDiskContents[destinationIndex] = !diskContents[sourceIndex];
            }
            diskContents = newDiskContents;
            diskContentsLength = newDiscContentsLength;
        }
        if (diskContentsLength > diskLength) {
            boolean[] newDiskContents = new boolean[diskLength];
            System.arraycopy(diskContents,0,newDiskContents,0,diskLength);
            diskContents = newDiskContents;
            diskContentsLength = diskLength;
        }
        int checksumLength = diskContentsLength;
        boolean checksum[] = new boolean[checksumLength];
        System.arraycopy(diskContents,0,checksum,0,checksumLength);
        do {
            int newChecksumLength = checksumLength/2;
            boolean[] newChecksum = new boolean[newChecksumLength];
            for (int checksumIndex=0, newChecksumIndex=0; checksumIndex<checksumLength; checksumIndex+=2, newChecksumIndex++) {
                if (checksum[checksumIndex] == checksum[checksumIndex+1]) {
                    newChecksum[newChecksumIndex] = true;
                }
            }
            checksum = newChecksum;
            checksumLength = newChecksumLength;
        } while (checksumLength % 2 == 0);
        char[] checksumCharecters = new char[checksumLength];
        for (int checksumIndex=0; checksumIndex<checksumLength; checksumIndex++) {
            checksumCharecters[checksumIndex] = checksum[checksumIndex]?'1':'0';
        }
        return new String(checksumCharecters);
    }
}
