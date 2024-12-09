package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2023;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day07 implements IPuzzle {
    private static final Pattern LINE_PATTERN = Pattern.compile("^(?<cards>[a-z0-9]{5}) (?<bid>[0-9]+)$", Pattern.CASE_INSENSITIVE);

    private static enum HandType {
        HIGH_CARD {
            @Override
            public boolean isApplicable(int[] cardCounts) {
                return cardCounts[1] == 5;
            }
        },
        ONE_PAIR {
            @Override
            public boolean isApplicable(int[] cardCounts) {
                return cardCounts[2] == 1 && cardCounts[1] == 3;
            }
        },
        TWO_PAIR {
            @Override
            public boolean isApplicable(int[] cardCounts) {
                return cardCounts[2] == 2 && cardCounts[1] == 1;
            }
        },
        THREE_OF_A_KIND {
            @Override
            public boolean isApplicable(int[] cardCounts) {
                return cardCounts[3] == 1 && cardCounts[1] == 2;
            }
        },
        FULL_HOUSE {
            @Override
            public boolean isApplicable(int[] cardCounts) {
                return cardCounts[3] == 1 && cardCounts[2] == 1;
            }
        },
        FOUR_OF_A_KIND {
            @Override
            public boolean isApplicable(int[] cardCounts) {
                return cardCounts[4] == 1 && cardCounts[1] == 1;
            }
        },
        FIVE_OF_A_KIND {
            @Override
            public boolean isApplicable(int[] cardCounts) {
                return cardCounts[5] == 1;
            }
        };

        public abstract boolean isApplicable(int[] cardCounts);
    }

    private static enum Card {
        JOKER  (null),
        TWO   ('2'),
        THREE ('3'),
        FOUR  ('4'),
        FIVE  ('5'),
        SIX   ('6'),
        SEVEN ('7'),
        EIGHT ('8'),
        NINE  ('9'),
        TEN   ('T'),
        JACK  (null),
        QUEEN ('Q'),
        KING  ('K'),
        ACE   ('A');

        private static final Map<Character,Card> CHARACTER_TO_CARD_MAP;
        static {
            CHARACTER_TO_CARD_MAP = new HashMap<>();
            for (Card card : Card.values()) {
                if (card.character != null) {
                    if (CHARACTER_TO_CARD_MAP.put(card.character, card) != null) {
                        throw new IllegalStateException("Duplicate character");
                    }
                }
            }
        }

        private final Character character;

        Card(Character character) {
            this.character = character;
        }

        public static Card getCardForCharacer(char character) {
            Card card = CHARACTER_TO_CARD_MAP.get(character);
            if (card == null) {
                throw new IllegalArgumentException("No such card");
            }
            return card;
        }
    }

    private static class Hand implements Comparable<Hand> {
        private final HandType handType;
        private final Card[] cards;
        private final int bid;

        public Hand(HandType handType, Card[] cards, int bid) {
            this.handType = handType;
            this.cards = Arrays.copyOf(cards, 5);
            this.bid = bid;
        }

        public int getBid() {
            return this.bid;
        }

        @Override
        public int compareTo(Hand otherHand) {
            int result = this.handType.compareTo(otherHand.handType);
            if (result == 0) {
                for (int cardIndex=0; cardIndex<5; cardIndex++) {
                    result = this.cards[cardIndex].compareTo(otherHand.cards[cardIndex]);
                    if (result != 0) {
                        break;
                    }
                }
            }
            return result;
        }
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        String[] inputLines = LineReader.stringsArray(inputCharacters, true);
        return new BasicPuzzleResults<>(
            solve(inputLines,Card.JACK),
            solve(inputLines,Card.JOKER)
        );
    }

    private static final int solve(String[] inputLines, Card jCard) {
        int handCount = inputLines.length;
        Hand[] hands = new Hand[handCount];
        for (int handIndex=0; handIndex<handCount; handIndex++) {
            Matcher matcher = LINE_PATTERN.matcher(inputLines[handIndex]);
            if (!matcher.matches()) {
                throw new IllegalStateException("Could not parse line");
            }
            String cardsString = matcher.group("cards");
            Card[] cards = new Card[5];
            Map<Card,Integer> cardToCountMap = new HashMap<>();
            for (int cardIndex=0; cardIndex<5; cardIndex++) {
                char character = cardsString.charAt(cardIndex);
                Card card;
                if (character == 'J') {
                    card = jCard;
                }
                else {
                    card = Card.getCardForCharacer(character);
                }
                cards[cardIndex] = card;
                cardToCountMap.put(card,cardToCountMap.getOrDefault(card,0)+1);
            }
            Integer jokerCount = cardToCountMap.remove(Card.JOKER);
            if (jokerCount != null) {
                if (jokerCount == 5) {
                    cardToCountMap.put(Card.JOKER,5);
                }
                else {
                    int highestCardCount = 0;
                    Card highestCardCountCardType = null;
                    for (Map.Entry<Card, Integer> cardToCountEntry : cardToCountMap.entrySet()) {
                        if (cardToCountEntry.getValue() > highestCardCount) {
                            highestCardCount = cardToCountEntry.getValue();
                            highestCardCountCardType = cardToCountEntry.getKey();
                        }
                    }
                    cardToCountMap.put(highestCardCountCardType, highestCardCount + jokerCount);
                }
            }
            int[] cardCounts = new int[6];
            for (Map.Entry<Card,Integer> cardToCountEntry : cardToCountMap.entrySet()) {
                cardCounts[cardToCountEntry.getValue()]++;
            }
            HandType handType = null;
            for (HandType possibleHandType : HandType.values()) {
                if (possibleHandType.isApplicable(cardCounts)) {
                    if (handType != null) {
                        throw new IllegalStateException("Multiple hand types applicable");
                    }
                    handType = possibleHandType;
                }
            }
            if (handType == null) {
                throw new IllegalStateException("No hand types applicable");
            }
            hands[handIndex] = new Hand(handType,cards,Integer.parseInt(matcher.group("bid")));
        }
        Arrays.sort(hands);
        int winnings = 0;
        for (int handIndex=0; handIndex<handCount; handIndex++) {
            winnings += hands[handIndex].getBid() * (handIndex+1);
        }
        return winnings;
    }
}
