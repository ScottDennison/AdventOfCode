package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2020;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Day22 implements IPuzzle {
    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        List<Deque<Integer>> playerCards = new ArrayList<>();
        Deque<Integer> currentPlayerCards = null;
        Integer expectingPlayerLine = 1;
        for (String line : LineReader.strings(inputCharacters)) {
            if (expectingPlayerLine != null) {
                if (!line.equals("Player " + expectingPlayerLine + ":")) {
                    throw new IllegalStateException("Unexpected player line");
                }
                expectingPlayerLine = null;
                currentPlayerCards = new LinkedList<>();
                playerCards.add(currentPlayerCards);
            }
            else if (line.isEmpty()) {
                expectingPlayerLine = playerCards.size() + 1;
            }
            else {
                currentPlayerCards.addLast(Integer.parseInt(line));
            }
        }
        if (playerCards.size() != 2) {
            throw new IllegalStateException("Only expecting 2 players");
        }
        Deque<Integer> player1Cards = playerCards.get(0);
        Deque<Integer> player2Cards = playerCards.get(1);
        return new BasicPuzzleResults<>(
            runGameAndScore(player1Cards,player2Cards,false),
            runGameAndScore(player1Cards,player2Cards,true)
        );
    }

    private static int runGameAndScore(Deque<Integer> player1Cards, Deque<Integer> player2Cards, boolean allowRecursiveCombat) {
        Deque<Integer> modifiablePlayer1Cards = new LinkedList<>(player1Cards);
        Deque<Integer> modifiablePlayer2Cards = new LinkedList<>(player2Cards);
        Deque<Integer> winningPlayerCards;
        switch (runGame(modifiablePlayer1Cards,modifiablePlayer2Cards,allowRecursiveCombat)) {
            case 1:
                winningPlayerCards = modifiablePlayer1Cards;
                break;
            case 2:
                winningPlayerCards = modifiablePlayer2Cards;
                break;
            default:
                throw new IllegalStateException("Unexpected winner");
        }
        int multiplier = 1;
        int score = 0;
        Integer playerCard;
        while ((playerCard = winningPlayerCards.pollLast()) != null) {
            score += playerCard * (multiplier++);
        }
        return score;
    }

    private static int runGame(Deque<Integer> player1Cards, Deque<Integer> player2Cards, boolean allowRecursiveCombat) {
        Set<List<List<Integer>>> previousRounds = new HashSet<>();
        while (true) {
            List<List<Integer>> thisRound = Arrays.asList(new ArrayList<>(player1Cards), new ArrayList<>(player2Cards));
            if (!previousRounds.add(thisRound)) {
                return 1;
            }
            Integer player1PlayedCard;
            if ((player1PlayedCard = player1Cards.pollFirst()) == null) {
                return 2;
            }
            Integer player2PlayedCard;
            if ((player2PlayedCard = player2Cards.pollFirst()) == null) {
                player1Cards.addFirst(player1PlayedCard);
                return 1;
            }
            int winnerOfRound;
            if (allowRecursiveCombat && player1PlayedCard <= player1Cards.size() && player2PlayedCard <= player2Cards.size()) {
                winnerOfRound = runGame(createSubDeque(player1Cards,player1PlayedCard),createSubDeque(player2Cards,player2PlayedCard),allowRecursiveCombat);
            } else {
                int difference = player1PlayedCard - player2PlayedCard;
                if (difference > 0) {
                    winnerOfRound = 1;
                } else if (difference < 0) {
                    winnerOfRound = 2;
                } else {
                    throw new IllegalStateException("No instructions for handling tiebreaks");
                }
            }
            switch (winnerOfRound) {
                case 1:
                    player1Cards.addLast(player1PlayedCard);
                    player1Cards.addLast(player2PlayedCard);
                    break;
                case 2:
                    player2Cards.addLast(player2PlayedCard);
                    player2Cards.addLast(player1PlayedCard);
                    break;
                default:
                    throw new IllegalStateException("Unexpected winner of round");
            }
        }
    }

    private static Deque<Integer> createSubDeque(Deque<Integer> input, int count) {
        return new LinkedList<>(new ArrayList<>(input).subList(0,count));
    }
}
