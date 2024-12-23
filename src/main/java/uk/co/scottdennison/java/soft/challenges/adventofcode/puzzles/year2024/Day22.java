package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2024;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Day22 implements IPuzzle {
    private static final int _19_TO_POWER_OF_4 = 130321; // Where 19 is the number of possible values -9 to 9

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        long secretNumberTotal = 0;
        String[] inputLines = LineReader.stringsArray(inputCharacters, true);
        int sellerCount = inputLines.length;
        int[] priceDifferenceKeyToBananas = new int[_19_TO_POWER_OF_4];
        int[] lastSellerSeenWithPriceDifferenceKey = new int[_19_TO_POWER_OF_4];
        for (int sellerNumber=1; sellerNumber<=sellerCount; sellerNumber++) {
            long secretNumber = Long.parseLong(inputLines[sellerNumber - 1]);
            int lastPrice = (int)(secretNumber % 10);
            int priceDifferenceKey = 0;
            for (int iteration=1; iteration<=2000; iteration++) {
                secretNumber = (secretNumber ^ (secretNumber << 6)) & 0xFFFFFF;
                secretNumber = (secretNumber ^ (secretNumber >> 5)) & 0xFFFFFF;
                secretNumber = (secretNumber ^ (secretNumber << 11)) & 0xFFFFFF;
                int price = (int)(secretNumber % 10);
                priceDifferenceKey = ((priceDifferenceKey * 19) + ((price - lastPrice) + 9)) % _19_TO_POWER_OF_4;
                if (iteration >= 4 && lastSellerSeenWithPriceDifferenceKey[priceDifferenceKey] != sellerNumber) {
                    lastSellerSeenWithPriceDifferenceKey[priceDifferenceKey] = sellerNumber;
                    priceDifferenceKeyToBananas[priceDifferenceKey] += price;
                }
                lastPrice = price;
            }
            secretNumberTotal += secretNumber;
        }
        int maxBananas = 0;
        for (int priceDifferenceKey=0; priceDifferenceKey<_19_TO_POWER_OF_4; priceDifferenceKey++) {
            int bananas = priceDifferenceKeyToBananas[priceDifferenceKey];
            if (bananas > maxBananas) {
                maxBananas = bananas;
            }
        }
        return new BasicPuzzleResults<>(
            secretNumberTotal,
            maxBananas
        );
    }
}
