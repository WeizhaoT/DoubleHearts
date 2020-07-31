import java.util.*;

/**
 * Shoe objects represent a shoe that holds decks of cards.
 *
 * @author Jordan Segalman
 */

public class Shoe {
    private ArrayList<Card> shoe = new ArrayList<>(); // holds the cards in the shoe
    private int numCards = 0;

    /**
     * Constructor for Shoe object.
     *
     * @param numDecks Number of decks in the shoe
     */

    public Shoe(final int numDecks) {
        for (final Card.Suit suit : Card.Suit.values()) {
            for (final Card.Rank rank : Card.Rank.values()) {
                for (int i = 0; i < numDecks; i++) {
                    shoe.add(new Card(rank, suit));
                    numCards++;
                }
            }
        }

        if (Server.TEST_MODE) {
            shuffle();
            numCards = 4 * Server.numCards;
            final List<Card> chosen = shoe.subList(0, numCards);
            shoe = new ArrayList<>(chosen);

            boolean hasTwoClubs = false;

            for (final Card card : shoe) {
                if (card.weakEquals("2C")) {
                    hasTwoClubs = true;
                    break;
                }
            }

            if (!hasTwoClubs) {
                shoe.remove(0);
                shoe.add(0, new Card("2C"));
                shuffle();
            }
        }
    }

    /**
     * Shuffles the shoe.
     */

    public void shuffle() {
        Collections.shuffle(shoe);
    }

    /**
     * Returns the last card in the shoe.
     *
     * @return the last card in the shoe
     */

    public Card dealCard() {
        if (numCards == 0)
            return null;

        return shoe.remove(--numCards);
    }

    /**
     * Returns the number of cards in the shoe.
     *
     * @return the number of cards in the shoe
     */

    public int remainingCards() {
        return numCards;
    }
}