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

    /** rank of the card */
    private final Rank rank;
    /** suit of the card */
    private final Suit suit;
    public boolean exposed = false;

    /**
     * The {@code CardComparator} class defines comparison between each pair of
     * card, so that they can be displayed neatly on hand panel.
     */
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

        /**
         * Translate an alias to a {@link Rank} enum instance.
         * 
         * @param c Alias of one letter
         * @return Corresponding {@link Rank} enum instance
         */
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
         * Get one-letter alias of the rank.
         * 
         * @return One-letter alias of the rank
         */
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

        /**
         * Translate an alias to a {@link Suit} enum instance.
         * 
         * @param c Alias of one letter
         * @return Corresponding {@link Suit} enum instance
         */
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
         * Get one-letter alias of the suit.
         * 
         * @return One-letter alias of the suit
         */
        public String alias() {
            return name().toUpperCase().substring(0, 1);
        }
    }

    /**
     * Instantiate a {@code Card} object given rank and suit.
     *
     * @param rank_ Rank of the card
     * @param suit_ Suit of the card
     */
    public Card(final Rank rank_, final Suit suit_) {
        rank = rank_;
        suit = suit_;
    }

    /**
     * Instantiate a {@code Card} object given its full alias.
     *
     * @param alias Full alias of new card
     */
    public Card(final String alias) {
        if (alias.length() != 2 && alias.length() != 3 || (alias.length() == 3 && alias.charAt(2) != 'x'))
            throw new NumberFormatException("Illegal card alias \"" + alias + "\"");

        final String sRank = alias.substring(0, 1);
        final String sSuit = alias.substring(1, 2);
        rank = Rank.fromString(sRank);
        suit = Suit.fromString(sSuit);

        if (alias.length() == 3)
            exposeCard();
    }

    /**
     * Tell if the card's full alias (including terminating "x" or not) is identical
     * to given literal.
     * 
     * @param literal Target full alias
     * @return {@code true} if they are exactly the same; {@code false} otherwise
     */
    public boolean fullEquals(final String literal) {
        return fullAlias().equals(literal);
    }

    /**
     * Tell if the card's partial alias (w/o terminating "x") is identical to given
     * literal.
     * 
     * @param literal Target alias
     * @return {@code true} if they are roughly the same; {@code false} otherwise
     */
    public boolean weakEquals(final String literal) {
        return alias().equals(literal.substring(0, 2));
    }

    /**
     * Set {@code exposed} flag to {@code true} when the card is exposed.
     */
    public void exposeCard() {
        exposed = true;
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
    }

    /**
     * Tell if a card is scored.
     * 
     * @return {@code true} if card is scored; {@code false} otherwise
     */
    public boolean isScored() {
        return isTransformer() || isSheep() || isPig() || isHeart();
    }

    /**
     * Tell if a card is forbidden to follow in the first round.
     * 
     * @return {@code true} if card is forbidden; {@code false} otherwise
     */
    public boolean forbiddenInFirstRound() {
        return isSheep() || isPig() || isNegativeHeart();
    }

    /**
     * Tell if a card can be exposed.
     * 
     * @return {@code true} if the card is allowed to be exposed; {@code false}
     *         otherwise
     */
    public boolean isExposable() {
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

    /**
     * Tell if a card is transformer (10 of clubs).
     * 
     * @return {@code true} if is transformer; {@code false} otherwise
     */
    public boolean isTransformer() {
        return (rank == Rank.TEN && suit == Suit.CLUBS);
    }

    /**
     * Tell if a card is pig (queen of spades).
     * 
     * @return {@code true} if is pig; {@code false} otherwise
     */
    public boolean isPig() {
        return (rank == Rank.QUEEN && suit == Suit.SPADES);
    }

    /**
     * Tell if a card is sheep (jack of diamonds).
     * 
     * @return {@code true} if is sheep; {@code false} otherwise
     */
    public boolean isSheep() {
        return (rank == Rank.JACK && suit == Suit.DIAMONDS);
    }

    /**
     * Tell if a card is in heart suit.
     * 
     * @return {@code true} if is in heart suit; {@code false} otherwise
     */
    public boolean isHeart() {
        return suit == Suit.HEARTS;
    }

    /**
     * Tell if a card is in heart suit and has negative score.
     * 
     * @return {@code true} if is in heart suit with negative score; {@code false}
     *         otherwise
     */
    public boolean isNegativeHeart() {
        return (rank.value > Rank.FOUR.value && suit == Suit.HEARTS);
    }

    /**
     * Get suit of this card.
     * 
     * @return {@code Suit} enum object of this card.
     */
    public Suit suit() {
        return suit;
    }

    /**
     * Assign an weight to card for card sorting.
     * 
     * @return Weight of card
     */
    private int weight() {
        return rank.value - 2 + suit.value * 13;
    }

    /**
     * Get short alias of card (w/o exposure information).
     * 
     * @return Short alias of card
     */
    public String alias() {
        return rank.alias() + suit.alias();
    }

    /**
     * Get full alias of card (w/ exposure information).
     * 
     * @return Full alias of card
     */
    public String fullAlias() {
        return rank.alias() + suit.alias() + (exposed ? "x" : "");
    }

    /**
     * Get set of recommended feasible cards.
     * 
     * @param cards      Cards in player's hand
     * @param leadSet    List of leading cards
     * @param selected   Cards already selected by player
     * @param firstRound {@code true} if this is first round; {@code false}
     *                   otherwise
     * @return A set of feasible cards recommended to player
     */
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

    /**
     * Get set of feasible cards which can be selected next by the rule.
     * 
     * @param cards      Cards in player's hand
     * @param leadSet    List of leading cards
     * @param selected   Cards already selected by player
     * @param firstRound {@code true} if this is first round; {@code false}
     *                   otherwise
     * @return A set of feasible cards that can be selected next
     */
    public static HashSet<Card> getFeasible(final ArrayList<Card> cards, final ArrayList<Card> leadSet,
            HashSet<Card> selected, final boolean firstRound) {
        if (selected == null)
            selected = new HashSet<>();

        final int numSelected = selected.size();
        if (numSelected >= 2)
            return new HashSet<>(); // If selected at least 2 cards, no card is feasible

        if (leadSet == null || leadSet.isEmpty()) { // Player is leader itself
            if (firstRound) {
                final HashSet<Card> twoOfClubs = new HashSet<>();
                for (final Card card : cards) {
                    if (card.weakEquals("2C") && !selected.contains(card))
                        twoOfClubs.add(card);
                }
                return twoOfClubs; // Only 2C is allowed to lead the first round
            } else if (numSelected == 0) {
                return new HashSet<>(cards); // Any card is allowed to lead in non-first rounds
            } else {
                String selectedAlias = selected.isEmpty() ? null : selected.iterator().next().alias();
                for (final Card card : cards) {
                    if (selected.contains(card)) {
                        continue;
                    } else if (card.weakEquals(selectedAlias)) {
                        return new HashSet<>(Arrays.asList(card)); // When a card is selected, the only feasible card is
                                                                   // its twin
                    }
                }
                return new HashSet<>(); // No twin exists; nothing is feasible
            }
        } else { // Player is following someone else's turn
            final Suit leadSuit = leadSet.get(0).suit;
            if (leadSet.size() == 1) { // Single leading card
                if (numSelected >= 1)
                    return new HashSet<>(); // Selected enough cards, so no card is feasible

                // Collect feasible cards separately from same suit and other suits
                final HashSet<Card> sameSuitFeasible = new HashSet<>();
                final HashSet<Card> otherSuitFeasible = new HashSet<>();
                for (final Card card : cards) {
                    if (card.suit == leadSuit) {
                        sameSuitFeasible.add(card);
                    } else if (!(firstRound && card.forbiddenInFirstRound())) {
                        otherSuitFeasible.add(card);
                    }
                }

                // First check samesuit, then check othersuit; if nothing is feasible, then
                // everything becomes feasible
                return sameSuitFeasible.isEmpty()
                        ? (otherSuitFeasible.isEmpty() ? new HashSet<>(cards) : otherSuitFeasible)
                        : sameSuitFeasible;
            } else { // Double leading card
                Card existing = null;
                String selectedAlias = selected.isEmpty() ? null : selected.iterator().next().alias();

                // Collect feasible cards in other suits, and try to find pairs in the leading
                // suit
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

                if (sameSuitMap.isEmpty()) { // First get feasible card in other suits; if nothing is feasible,
                                             // everthing becomes feasible
                    return otherSuitFeasible.isEmpty() ? new HashSet<>(cards) : otherSuitFeasible;
                } else if (selectedAlias == null || !sameSuitMap.containsKey(selectedAlias)) { // If nothing selected or
                                                                                               // selected card has no
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