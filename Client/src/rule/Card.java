package rule;

import main.ClientController;

import java.io.IOException;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * Card objects represent a standard playing card with a rank and a suit.
 *
 * @author Jordan Segalman
 */

public class Card {
    private final Rank rank; // rank of the card
    private final Suit suit; // suit of the card
    public boolean bid = false;

    public static class CardComparator implements Comparator<Card> {
        public int compare(final Card c1, final Card c2) {
            if (c1.weight() < c2.weight())
                return 1;
            else
                return -1;
        }
    }

    public static int roundResult(final ArrayList<ArrayList<Card>> cardSets, final int leader) {
        final ArrayList<Card> leadCards = cardSets.get(0);
        final Card leadCard = leadCards.get(0);

        int maxRank = leadCard.rank.value;

        final int numPlayers = cardSets.size();
        final int roundSize = leadCards.size();
        int winner = 0;

        if (roundSize == 1) {
            for (int i = 1; i < numPlayers; i++) {
                final Card followedCard = cardSets.get(i).get(0);
                if (followedCard.suit != leadCard.suit)
                    continue;

                if (followedCard.rank.value > maxRank) {
                    winner = i;
                    maxRank = followedCard.rank.value;
                }
            }
        } else if (roundSize == 2) {
            assert isPair(leadCards);

            boolean isPair = true;

            for (int i = 1; i < numPlayers; i++) {
                boolean sameSuit = true;
                final ArrayList<Card> cards = cardSets.get(i);

                for (final Card card : cards) {
                    if (card.suit != leadCard.suit) {
                        sameSuit = false;
                        break;
                    }
                }

                if (!sameSuit)
                    continue;

                final Rank followedRank1 = cards.get(0).rank;
                final Rank followedRank2 = cards.get(1).rank;

                final boolean followedPair = followedRank1 == followedRank2;

                if (isPair) {
                    if (followedPair) {
                        if (followedRank1.value > maxRank) {
                            winner = i;
                            maxRank = followedRank1.value;
                        }
                    } else {
                        isPair = false;
                        winner = i;
                        maxRank = Integer.max(followedRank1.value, followedRank2.value);
                    }
                } else if (!followedPair) {
                    final int topRank = Integer.max(followedRank1.value, followedRank2.value);

                    if (topRank > maxRank) {
                        maxRank = topRank;
                        winner = i;
                    }
                }
            }
        }
        return (winner + leader) % numPlayers;
    }

    /**
     * Ranks that cards can have.
     */

    public enum Rank {
        TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8), NINE(9), TEN(10), JACK(11), QUEEN(12), KING(13),
        ACE(14);

        private int value; // value of the rank

        /**
         * Constructor for Rank object.
         *
         * @param value Value of the rank
         */

        Rank(final int value) {
            this.value = value;
        }

        public static Rank fromString(final String c) {
            if (c.length() != 1)
                throw new NumberFormatException("Illegal rank \"" + c + "\"");

            int n;
            try {
                n = Integer.valueOf(c);
            } catch (final NumberFormatException e) {
                if (c.equals("T"))
                    n = 10;
                else if (c.equals("J"))
                    n = 11;
                else if (c.equals("Q"))
                    n = 12;
                else if (c.equals("K"))
                    n = 13;
                else if (c.equals("A"))
                    n = 14;
                else
                    throw new NumberFormatException("Illegal rank \"" + c + "\"");
            }

            return Rank.values()[n - 2];
        }

        /**
         * Returns a string representation of the rank.
         *
         * @return the string representation of the rank
         */

        @Override
        public String toString() {
            return name().toLowerCase();
        }

        public String alias() {
            if (this.value < 10)
                return String.valueOf(this.value);
            else
                return name().toUpperCase().substring(0, 1);
        }
    }

    /**
     * Suits that cards can have.
     */

    public enum Suit {
        CLUBS(0), DIAMONDS(1), HEARTS(3), SPADES(2);

        private int value;

        Suit(final int value) {
            this.value = value;
        }

        public static Suit fromString(final String c) {
            if (c.equals("C"))
                return CLUBS;
            if (c.equals("D"))
                return DIAMONDS;
            if (c.equals("H"))
                return HEARTS;
            if (c.equals("S"))
                return SPADES;

            throw new NumberFormatException();
        }

        /**
         * Returns a string representation of the suit.
         *
         * @return the string representation of the suit
         */

        @Override
        public String toString() {
            return name().toLowerCase();
        }

        public String alias() {
            return name().toUpperCase().substring(0, 1);
        }
    }

    /**
     * Constructor for Card object.
     *
     * @param rank_ Rank of the card
     * @param suit_ Suit of the card
     */

    public Card(final Rank rank_, final Suit suit_) {
        rank = rank_;
        suit = suit_;
    }

    public Card(final String alias) {
        if (alias.length() != 2 && alias.length() != 3 || (alias.length() == 3 && alias.charAt(2) != 'x'))
            throw new NumberFormatException("Illegal card alias \"" + alias + "\"");

        final String sRank = alias.substring(0, 1);
        final String sSuit = alias.substring(1, 2);
        rank = Rank.fromString(sRank);
        suit = Suit.fromString(sSuit);

        if (alias.length() == 3)
            bidCard();
    }

    public boolean fullEquals(final String literal) {
        return fullAlias().equals(literal);
    }

    public boolean weakEquals(final String literal) {
        return alias().equals(literal.substring(0, 2));
    }

    public void bidCard() {
        bid = true;
    }

    /**
     * Returns the value of the card.
     *
     * @return the value of the card
     */

    public int value() {
        if (rank == Rank.JACK && suit == Suit.DIAMONDS)
            return 100;
        if (rank == Rank.QUEEN && suit == Suit.SPADES)
            return -100;
        if (suit == Suit.HEARTS && rank.value >= Rank.FIVE.value) {
            int baseValue = -10;
            if (rank.value >= Rank.JACK.value)
                baseValue += (-10) * (rank.value - Rank.TEN.value);

            return baseValue;
        }

        return 0;
        // return rank.value;
    }

    public boolean isValuable() {
        return isTransformer() || isSheep() || isPig() || isHeart();
    }

    public boolean forbiddenInFirstRound() {
        if (ClientController.TEST_MODE)
            return false;

        return isSheep() || isPig() || isScoredHeart();
    }

    public boolean isShowable() {
        return isTransformer() || isSheep() || isPig();
    }

    /**
     * Returns the rank of the card.
     *
     * @return the rank of the card
     */

    public Rank rank() {
        return rank;
    }

    public boolean isTransformer() {
        return (rank == Rank.TEN && suit == Suit.CLUBS);
    }

    public boolean isPig() {
        return (rank == Rank.QUEEN && suit == Suit.SPADES);
    }

    public boolean isSheep() {
        return (rank == Rank.JACK && suit == Suit.DIAMONDS);
    }

    public boolean isHeart() {
        return suit == Suit.HEARTS;
    }

    public boolean isScoredHeart() {
        return (rank.value > Rank.FOUR.value && suit == Suit.HEARTS);
    }

    public Suit suit() {
        return suit;
    }

    public int weight() {
        return rank.value - 2 + suit.value * 13;
    }

    public String alias() {
        return rank.alias() + suit.alias();
    }

    public String fullAlias() {
        return rank.alias() + suit.alias() + (bid ? "x" : "");
    }

    /**
     * Returns a JLabel containing an image of the card with the given name.
     *
     * @param cardName Name of card to add to JLabel
     * @return the JLabel containing an image of the card
     */

    public static JLabel createCard(final String cardName) {
        JLabel cardLabel = null; // label containing image of card
        try {
            String alias = new String(cardName);
            if (cardName.length() == 3)
                alias = alias.substring(0, 2);

            cardLabel = new JLabel(new ImageIcon(ImageIO.read(Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("CardImages/" + alias + ".png"))));
            cardLabel.setName(cardName);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return cardLabel;
    }

    public static boolean isPair(final Collection<Card> collection) {
        final Card[] cards = collection.toArray(new Card[0]);
        return cards.length == 2 && cards[0].suit == cards[1].suit && cards[0].rank == cards[1].rank;
    }

    public static String concatCards(final String delim, final Collection<Card> collection) {
        final ArrayList<String> aliasList = new ArrayList<>();

        for (final Card card : collection) {
            aliasList.add(card.fullAlias());
        }

        return String.join(delim, aliasList);
    }

}