package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2018;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day09 implements IPuzzle {
    private static final Pattern PATTERN_INPUT = Pattern.compile("^(?<playerCount>[1-9][0-9]*) players; last marble is worth (?<lastMarbleValue>[1-9][0-9]*) points$");

    private static class Node {
        int value;
        Node previousNode;
        Node nextNode;
    }

    private static long run(int playerCount, int lastMarbleValue) {
        long[] playerScores = new long[playerCount];
        Node currentNode = new Node();
        currentNode.value = 0;
        currentNode.nextNode = currentNode;
        currentNode.previousNode = currentNode;
        for (int marbleValue=1; marbleValue<=lastMarbleValue; marbleValue++) {
            if (marbleValue % 23 == 0) {
                Node nodeToRemove = currentNode.previousNode.previousNode.previousNode.previousNode.previousNode.previousNode.previousNode;
                Node nodeBeforeNodeToRemove = nodeToRemove.previousNode;
                currentNode = nodeToRemove.nextNode;
                nodeBeforeNodeToRemove.nextNode = currentNode;
                currentNode.previousNode = nodeBeforeNodeToRemove;
                // Technically the incorrect index (it should have a -1 in there), but it will always be the same incorrect.
                playerScores[marbleValue % playerCount] += marbleValue + nodeToRemove.value;
            }
            else {
                Node nodeAfterCurrent = currentNode.nextNode;
                Node nodeAfterNodeAfterCurrent = nodeAfterCurrent.nextNode;
                currentNode = new Node();
                currentNode.value = marbleValue;
                currentNode.previousNode = nodeAfterCurrent;
                currentNode.nextNode = nodeAfterNodeAfterCurrent;
                nodeAfterCurrent.nextNode = currentNode;
                nodeAfterNodeAfterCurrent.previousNode = currentNode;
            }
        }
        long highScore = Long.MIN_VALUE;
        for (int playerIndex=0; playerIndex<playerCount; playerIndex++) {
            long playerScore = playerScores[playerIndex];
            if (playerScore > highScore) {
                highScore = playerScore;
            }
        }
        return highScore;
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        Matcher matcher = PATTERN_INPUT.matcher(new String(inputCharacters).trim());
        if (!matcher.matches()) {
            throw new IllegalStateException("Could not parse input.");
        }
        int playerCount = Integer.parseInt(matcher.group("playerCount"));
        int lastMarbleValue = Integer.parseInt(matcher.group("lastMarbleValue"));
        return new BasicPuzzleResults<>(
            run(playerCount, lastMarbleValue),
            run(playerCount, lastMarbleValue*100) // This does rerun the game for part A again, but part A's runtime is insignifcant.
        );
    }
}
