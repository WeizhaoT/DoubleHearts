package rule;

import java.io.IOException;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * Card objects represent a standard playing card with a rank and a suit.
 *
 * @author Weizhao Tang
 */

public class Card {
    public static final int NONE = 0;

    private final Rank rank; // rank of the card
    private final Suit suit; // suit of the card
    public boolean bid = false;

    public static class CardComparator implements Comparator<Card> {
        public int compare(final Card c1, final Card c2) {
            return c1.weight() == c2.weight() ? 0 : (c1.weight() < c2.weight() ? 1 : -1);
        }
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

    public static HashSet<Card> getHintFeasible(final ArrayList<Card> cards, final ArrayList<Card> leadSet,
            HashSet<Card> selected, final boolean firstRound) {

        HashSet<Card> emptyFeasible = getFeasible(cards, leadSet, null, firstRound);
        if (leadSet == null || emptyFeasible.size() >= leadSet.size())
            return emptyFeasible;

        assert (emptyFeasible.size() == 1 && leadSet.size() == 2);
        if (selected == null || !selected.contains(emptyFeasible.iterator().next())) {
            return emptyFeasible;
        } else {
            final HashSet<Card> feasible = getFeasible(cards, leadSet, emptyFeasible, firstRound);
            feasible.addAll(emptyFeasible);
            return feasible;
        }
    }

    public static HashSet<Card> getFeasible(final ArrayList<Card> cards, final ArrayList<Card> leadSet,
            HashSet<Card> selected, final boolean firstRound) {
        if (selected == null)
            selected = new HashSet<>();

        final int numSelected = selected.size();
        if (numSelected >= 2)
            return new HashSet<>();

        if (leadSet == null || leadSet.isEmpty()) {
            if (firstRound) {
                final HashSet<Card> twoOfClubs = new HashSet<>();
                for (final Card card : cards) {
                    if (card.weakEquals("2C") && !selected.contains(card))
                        twoOfClubs.add(card);
                }
                return twoOfClubs;
            } else if (numSelected == 0) {
                return new HashSet<>(cards);
            } else {
                String selectedAlias = selected.isEmpty() ? null : selected.iterator().next().alias();

                for (final Card card : cards) {
                    if (selected.contains(card)) {
                        continue;
                    } else if (card.weakEquals(selectedAlias)) {
                        return new HashSet<>(Arrays.asList(card));
                    }
                }
                return new HashSet<>();
            }
        } else {
            final Suit leadSuit = leadSet.get(0).suit;
            if (leadSet.size() == 1) {
                if (numSelected >= 1)
                    return new HashSet<>();

                final HashSet<Card> sameSuitFeasible = new HashSet<>();
                final HashSet<Card> otherSuitFeasible = new HashSet<>();
                for (final Card card : cards) {
                    if (card.suit == leadSuit) {
                        sameSuitFeasible.add(card);
                    } else if (!(firstRound && card.forbiddenInFirstRound())) {
                        otherSuitFeasible.add(card);
                    }
                }

                return sameSuitFeasible.isEmpty()
                        ? (otherSuitFeasible.isEmpty() ? new HashSet<>(cards) : otherSuitFeasible)
                        : sameSuitFeasible;
            } else {
                Card existing = null;
                String selectedAlias = selected.isEmpty() ? null : selected.iterator().next().alias();
                final HashSet<Card> otherSuitFeasible = new HashSet<>();
                final HashSet<Card> sameSuitPairs = new HashSet<>();
                final HashMap<String, Card> sameSuitMap = new HashMap<>();

                for (final Card card : cards) {
                    if (selected.contains(card) || card.suit != leadSuit) {
                        if (!selected.contains(card) && !(firstRound && card.forbiddenInFirstRound())) {
                            otherSuitFeasible.add(card);
                        }
                        continue;
                    }

                    if ((existing = sameSuitMap.get(card.alias())) != null) {
                        sameSuitPairs.addAll(Arrays.asList(card, existing));
                    } else {
                        sameSuitMap.put(card.alias(), card);
                    }
                }

                if (sameSuitMap.isEmpty()) {
                    return otherSuitFeasible.isEmpty() ? new HashSet<>(cards) : otherSuitFeasible;
                } else if (selectedAlias == null || !sameSuitMap.containsKey(selectedAlias)) {
                    return new HashSet<>(sameSuitPairs.isEmpty() ? sameSuitMap.values() : sameSuitPairs);
                } else {
                    existing = sameSuitMap.get(selectedAlias);
                    return new HashSet<>(existing == null ? sameSuitMap.values() : Arrays.asList(existing));
                }
            }
        }
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