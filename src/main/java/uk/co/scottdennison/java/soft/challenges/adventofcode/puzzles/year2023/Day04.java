package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2023;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Day04 implements IPuzzle {
    private static final Pattern LINE_PATTERN = Pattern.compile("^Card +(?<cardNumber>[0-9]+): +(?<winningNumbers>[0-9 ]+) +\\| +(?<haveNumbers>[0-9 ]+) *$");
    private static final Pattern SPACES_PATTERN = Pattern.compile(" +");

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        String[] inputLines = LineReader.stringsArray(inputCharacters, true);
        int cardCount = inputLines.length;
        int[] winsPerCard = new int[cardCount];
        int[] copiesOfCard = new int[cardCount];
        for (String inputLine : LineReader.strings(inputCharacters)) {
            Matcher lineMatcher = LINE_PATTERN.matcher(inputLine);
            if (!lineMatcher.matches()) {
                throw new IllegalStateException("Unable to parse line");
            }
            int cardIndex = Integer.parseInt(lineMatcher.group("cardNumber"))-1;
            Set<Integer> winningNumbersSet = parseNumbers(lineMatcher.group("winningNumbers"));
            Set<Integer> haveNumbersSet = parseNumbers(lineMatcher.group("haveNumbers"));
            winsPerCard[cardIndex] = (int)winningNumbersSet.stream().filter(haveNumbersSet::contains).count();
            copiesOfCard[cardIndex] = 1;
        }
        int partAPoints = 0;
        int partBTotalCards = 0;
        for (int cardIndex=0; cardIndex<cardCount; cardIndex++) {
            int copiesOfThisCard = copiesOfCard[cardIndex];
            int winsForThisCard = winsPerCard[cardIndex];
            int cardCopiesStopIndex = cardIndex+winsForThisCard+1;
            if (cardCopiesStopIndex > cardCount) {
                cardCopiesStopIndex = cardCount;
            }
            for (int cardCopiesIndex=cardIndex+1; cardCopiesIndex<cardCopiesStopIndex; cardCopiesIndex++) {
                copiesOfCard[cardCopiesIndex] += copiesOfThisCard;
            }
            partAPoints += (1 << (winsForThisCard-1));
            partBTotalCards += copiesOfThisCard;
        }
        return new BasicPuzzleResults<>(
            partAPoints,
            partBTotalCards
        );
    }

    private static Set<Integer> parseNumbers(String numbersString) {
        return SPACES_PATTERN.splitAsStream(numbersString).map(Integer::parseInt).collect(Collectors.toSet());
    }
}
