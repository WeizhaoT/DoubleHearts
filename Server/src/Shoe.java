import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        // for (int i = 0; i < numDecks; i++) {
        for (int i = 0; i < 2; i++) {
            addDeck(new Deck());
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

        System.err.println("Remaining: " + remainingCards());
    }

    /**
     * Adds a deck to the shoe.
     *
     * @param deck Deck to add to the shoe
     */

    private void addDeck(final Deck deck) {
        for (int i = 0; i < deck.size; i++) {
            shoe.add(deck.dealCard());
        }
        numCards += deck.size;
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

        final Card card = shoe.get(shoe.size() - 1); // last card in the shoe
        shoe.remove(card);
        numCards -= 1;
        return card;
    }

    /**
     * Returns the number of cards in the shoe.
     *
     * @return the number of cards in the shoe
     */

    public int remainingCards() {
        return numCards;
    }

    public static void main(final String[] args) {
        Server.TEST_MODE = true;
        Server.numCards = 4;
        final Shoe shoe = new Shoe(2);

        shoe.shuffle();

        while (shoe.remainingCards() > 0) {
            System.out.print(shoe.dealCard().fullAlias() + ", ");
        }

        System.out.println();
    }
}