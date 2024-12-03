package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2024;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Arrays;

public class Day02 implements IPuzzle {
     private static boolean checkReport(int[] report, int reportLength) {
        int firstLevel = report[0];
        boolean ascending = report[1] >= firstLevel;
        int previousLevel = firstLevel;
        for (int levelIndex=1; levelIndex<reportLength; levelIndex++) {
            int level = report[levelIndex];
            int gap = level - previousLevel;
            if ((ascending && (gap < 1 || gap > 3)) || (!ascending && (gap < -3 || gap > -1))) {
                return false;
            }
            previousLevel = level;
        }
        return true;
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        int partASafeCount = 0;
        int partBSafeCount = 0;
        for (String inputLine : LineReader.strings(inputCharacters)) {
            int[] report = Arrays.stream(inputLine.split(" ")).mapToInt(Integer::parseInt).toArray();
            int reportLength = report.length;
            if (checkReport(report, reportLength)) {
                partASafeCount++;
                partBSafeCount++;
            }
            else {
                int partialReportLength = reportLength - 1;
                int[] partialReport = Arrays.copyOf(report, partialReportLength);
                int index = partialReportLength;
                while (true) {
                    if (checkReport(partialReport, partialReportLength)) {
                        partBSafeCount++;
                        break;
                    }
                    if (--index < 0) {
                        break;
                    }
                    partialReport[index] = report[index + 1];
                }
            }
        }
        return new BasicPuzzleResults<>(
            partASafeCount,
            partBSafeCount
        );
    }
}
