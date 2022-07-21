import java.util.*;
import java.util.stream.Collectors;

/**
 * Card objects represent a standard playing card with a rank and a suit.
 *
 * @author Weizhao Tang
 */
public class Card {
    public static int baseScore = 10;

    public static final String OPENER = Rank.TWO.alias() + Suit.CLUBS.alias();
    public static final String TRANS = Rank.TEN.alias() + Suit.CLUBS.alias();
    public static final String SHEEP = Rank.JACK.alias() + Suit.DIAMONDS.alias();
    public static final String PIG = Rank.QUEEN.alias() + Suit.SPADES.alias();
    public static final String ACEH = Rank.ACE.alias() + Suit.HEARTS.alias();
    public static final String[] exposables = new String[] { TRANS, SHEEP, PIG, ACEH };

    public static double MULT_EXP = 0.4;
    public static double MULT_GET = 0.4;

    private final Rank rank; // rank of the card
    private final Suit suit; // suit of the card
    public int exposed = 0;

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

        Rank(int value) {
            this.value = value;
        }

        public static Rank fromString(String c) {
            if (c.length() != 1)
                throw new NumberFormatException("Illegal rank \"" + c + "\"");

            int n;
            try {
                n = Integer.valueOf(c);
            } catch (NumberFormatException e) {
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

        Suit(int value) {
            this.value = value;
        }

        public static Suit fromString(String c) {
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
    public Card(Rank rank_, Suit suit_) {
        rank = rank_;
        suit = suit_;
    }

    public Card(String alias) {
        if (alias.length() != 2 && alias.length() != 3 || (alias.length() == 3 && !"zx".contains(alias.substring(2))))
            throw new NumberFormatException("Illegal card alias \"" + alias + "\"");

        rank = Rank.fromString(alias.substring(0, 1));
        suit = Suit.fromString(alias.substring(1, 2));

        if (alias.length() == 3) {
            upgrade(alias.charAt(2) == 'x' ? 1 : 2);
        }
    }

    public boolean fullEquals(String literal) {
        return fullAlias().equals(literal);
    }

    public boolean weakEquals(String literal) {
        return alias().equals(literal.substring(0, 2));
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

    public boolean isScored() {
        return isTransformer() || isSheep() || isPig() || isHeart();
    }

    /**
     * Set {@code exposed} flag to {@code true} when the card is exposed.
     */
    public synchronized void upgrade(int numLevels) {
        if ((exposed += numLevels) > 2)
            throw new RuntimeException("Upgrade to " + exposed + " over maximum 2");
    }

    /**
     * Returns the rank of the card.
     *
     * @return the rank of the card
     */
    public Rank rank() {
        return rank;
    }

    public Suit suit() {
        return suit;
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

    private boolean isNegativeHeart() {
        return (rank.value > Rank.FOUR.value && suit == Suit.HEARTS);
    }

    public int weight() {
        return rank.value - 2 + suit.value * 13;
    }

    public String alias() {
        return rank.alias() + suit.alias();
    }

    /**
     * Get full alias of card (w/ exposure information).
     * 
     * @return Full alias of card
     */
    public String fullAlias() {
        return rank.alias() + suit.alias() + (exposed == 0 ? "" : exposed == 1 ? "x" : "z");
    }

    private static boolean isPair(Collection<Card> collection) {
        Card[] cards = collection.toArray(new Card[0]);
        return cards.length == 2 && cards[0].suit == cards[1].suit && cards[0].rank == cards[1].rank;
    }

    private static int maxRankValue(Collection<Card> collection) {
        Card[] cards = collection.toArray(new Card[0]);
        return cards[0].suit == cards[1].suit ? Math.max(cards[0].rank.value, cards[1].rank.value) : -1;
    }

    public static int roundResult(ArrayList<ArrayList<Card>> cardSets, int leader) {
        ArrayList<Card> leadCards = cardSets.get(0);
        Card leadCard = leadCards.get(0);

        int maxRank = leadCard.rank.value;

        int numPlayers = cardSets.size();
        int roundSize = leadCards.size();
        int winner = 0;

        if (roundSize == 1) {
            for (int i = 1; i < numPlayers; i++) {
                Card followedCard = cardSets.get(i).get(0);
                if (followedCard.suit != leadCard.suit)
                    continue;

                if (followedCard.rank.value > maxRank) {
                    winner = i;
                    maxRank = followedCard.rank.value;
                }
            }
        } else if (roundSize == 2) {
            assert isPair(leadCards);

            boolean maxIsPair = true;
            boolean isNotDiamond = leadCard.suit != Suit.DIAMONDS;

            for (int i = 1; i < numPlayers; i++) {
                ArrayList<Card> cards = cardSets.get(i);
                if (!cards.stream().allMatch(card -> card.suit == leadCard.suit))
                    continue;

                int topRank = maxRankValue(cards);
                boolean isPair = isPair(cards);

                if ((topRank > maxRank && !(maxIsPair ^ isPair)) || (isNotDiamond && maxIsPair && !isPair)) {
                    maxRank = topRank;
                    winner = i;
                    maxIsPair = isPair;
                }
            }
        }
        return (winner + leader) % numPlayers;
    }

    public static double getMult(final Collection<Card> trans) {
        if (trans == null || trans.isEmpty())
            return 1.0;

        final double maxMult = trans.stream().max(Comparator.comparing(c -> c.multiplier())).get().multiplier();
        return maxMult + trans.size() * MULT_GET;
    }

    public static String concatCards(String delim, Collection<Card> collection) {
        if (collection == null || collection.size() == 0)
            return "";

        return collection.stream().map(c -> c.fullAlias()).collect(Collectors.joining(delim));
    }
}