package rule;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Card objects represent a standard playing card with a rank and a suit.
 *
 * @author Weizhao Tang
 */
public class Card {
    private static int baseScore = 10;

    public static final String OPENER = Rank.TWO.alias() + Suit.CLUBS.alias();
    public static final String TRANS = Rank.TEN.alias() + Suit.CLUBS.alias();
    public static final String SHEEP = Rank.JACK.alias() + Suit.DIAMONDS.alias();
    public static final String PIG = Rank.QUEEN.alias() + Suit.SPADES.alias();
    public static final String ACEH = Rank.ACE.alias() + Suit.HEARTS.alias();

    public static double MULT_EXP = 0.1;
    public static double MULT_GET = 0.1;

    /** rank of the card */
    private final Rank rank;
    /** suit of the card */
    private final Suit suit;
    public int exposed = 0;

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
        if (alias.length() != 2 && alias.length() != 3 || (alias.length() == 3 && !"zx".contains(alias.substring(2))))
            throw new NumberFormatException("Illegal card alias \"" + alias + "\"");

        rank = Rank.fromString(alias.substring(0, 1));
        suit = Suit.fromString(alias.substring(1, 2));

        if (alias.length() == 3) {
            upgrade(alias.charAt(2) == 'x' ? 1 : 2);
        }
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
    public synchronized void upgrade(final int numLevels) {
        if ((exposed += numLevels) > 2)
            throw new RuntimeException("Upgrade to " + exposed + " over maximum 2");
    }

    public synchronized boolean fullyUpgraded() {
        return exposed >= 2;
    }

    /**
     * Set {@code exposed} flag to {@code true} when the card is exposed.
     */
    public synchronized void resetGrade() {
        exposed = 0;
    }

    /**
     * Returns the value of the card.
     *
     * @return the value of the card
     */
    public int value() {
        if (isTransformer())
            return (5 * baseScore) << exposed;
        if (isSheep())
            return (10 * baseScore) << exposed;
        if (isPig())
            return (-10 * baseScore) << exposed;
        if (isNegativeHeart()) {
            int baseValue = -baseScore;
            if (rank.value >= Rank.JACK.value)
                baseValue += (-baseScore) * (rank.value - Rank.TEN.value);

            return baseValue << exposed;
        }
        return 0;
    }

    public double multiplier() {
        if (isTransformer()) {
            return 1.0 + exposed * MULT_EXP;
        } else
            throw new RuntimeException("Calling multiplier on non-transformer \"" + fullAlias() + "\"");
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
    public boolean scoringInRound1() {
        return isSheep() || isPig() || isNegativeHeart();
    }

    /**
     * Tell if a card can be exposed.
     * 
     * @return {@code true} if the card is allowed to be exposed; {@code false}
     *         otherwise
     */
    public boolean isExposable() {
        return weakEquals(Card.ACEH) || isSheep() || isPig() || isTransformer();
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
        return alias() + (exposed == 0 ? "" : exposed == 1 ? "x" : "z");
    }

    public String exposerAlias() {
        return isScored() ? (isHeart() ? Card.ACEH : alias()) : "--";
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
            final HashSet<Card> selected, final boolean firstRound) {

        final HashSet<Card> emptyFeasible = getFeasible(cards, leadSet, null, firstRound);
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
            final HashSet<Card> selected_, final boolean firstRound) {
        final HashSet<Card> selected = selected_ == null ? new HashSet<>() : selected_;

        final int numSelected = selected.size();
        if (numSelected >= 2)
            return new HashSet<>(); // If selected at least 2 cards, no card is feasible

        if (leadSet == null || leadSet.isEmpty()) { // Player is leader itself
            if (firstRound) { // Only 2C is allowed to lead the first round
                return new HashSet<>(cards.stream().filter(c -> c.weakEquals(Card.OPENER) && !selected.contains(c))
                        .collect(Collectors.toSet()));
            } else if (numSelected == 0) {
                return new HashSet<>(cards); // Any card is allowed to lead in non-first rounds
            } else {
                final String selectedAlias = selected.isEmpty() ? null : selected.iterator().next().alias();
                // When a card is selected, the only feasible card is its twin
                return new HashSet<>(cards.stream().filter(c -> c.weakEquals(selectedAlias) && !selected.contains(c))
                        .collect(Collectors.toSet()));
            }
        } else { // Player is following someone else's turn
            final Suit leadSuit = leadSet.get(0).suit;
            if (leadSet.size() == 1) { // Single leading card
                if (numSelected >= 1)
                    return new HashSet<>(); // Selected enough cards, so no card is feasible

                // Collect feasible cards separately from same suit and other suits
                final HashSet<Card> sameSuitFeasible = new HashSet<>(
                        cards.stream().filter(c -> c.suit == leadSuit).collect(Collectors.toSet()));
                final HashSet<Card> allSuitFeasible = new HashSet<>(
                        cards.stream().filter(c -> !firstRound || !c.scoringInRound1()).collect(Collectors.toSet()));

                // First check samesuit, then check othersuit; if nothing is feasible, then
                // everything becomes feasible
                return sameSuitFeasible.isEmpty() ? (allSuitFeasible.isEmpty() ? new HashSet<>(cards) : allSuitFeasible)
                        : sameSuitFeasible;
            } else { // Double leading card
                Card existing = null;
                final String selectedAlias = selected.isEmpty() ? null : selected.iterator().next().alias();

                // Collect feasible cards in other suits, and try to find pairs in the leading
                // suit
                final HashSet<Card> otherSuitFeasible = new HashSet<>();
                final HashSet<Card> sameSuitPairs = new HashSet<>();
                final HashMap<String, Card> sameSuitMap = new HashMap<>();

                for (final Card card : cards) {
                    if (selected.contains(card) || card.suit != leadSuit) {
                        if (!selected.contains(card) && !(firstRound && card.scoringInRound1())) {
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

    public static double getMult(final Collection<Card> trans) {
        if (trans == null || trans.isEmpty())
            return 1.0;

        final double maxMult = trans.stream().max(Comparator.comparing(c -> c.multiplier())).get().multiplier();
        return maxMult + trans.size() * MULT_GET;
    }

    public static double getMult(Card... trans) {
        return getMult(Arrays.asList(trans));
    }

    public static String getNumerics(int len, double d) {
        if (Math.abs(d - Math.round(d)) < 0.05) {
            return String.format(len > 0 ? "%" + len + "d" : "%d", Math.round(d));
        } else
            return String.format(len > 0 ? "%" + len + ".1f" : "%.1f", d);
    }

    public static String getMultStr(int len, Card... trans) {
        return getNumerics(len, getMult(trans));
    }

    public static void setParams(final int score, final double mult_exp, final double mult_get) {
        baseScore = score;
        MULT_EXP = mult_exp;
        MULT_GET = mult_get;
    }

    public static boolean isPair(final String alias1, final String alias2) {
        return alias1.startsWith(alias2.substring(0, 2));
    }

    public static boolean isPair(final Collection<Card> collection) {
        final Card[] cards = collection.toArray(new Card[0]);
        return cards.length == 2 && cards[0].suit == cards[1].suit && cards[0].rank == cards[1].rank;
    }

    public static String concatCards(final String delim, final Collection<Card> collection) {
        return collection.stream().map(card -> card.fullAlias()).collect(Collectors.joining(delim));
    }
}