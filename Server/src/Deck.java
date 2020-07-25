import java.util.ArrayList;

/**
 * Deck objects represent a standard deck of playing cards.
 *
 * @author Jordan Segalman
 */

public class Deck {
    private final ArrayList<Card> deck = new ArrayList<>(); // holds the cards in the deck
    public int size = 0;

    /**
     * Constructor for Deck object.
     */

    public Deck() {
        for (final Card.Suit suit : Card.Suit.values()) {
            for (final Card.Rank rank : Card.Rank.values()) {
                deck.add(new Card(rank, suit));
            }
        }
        size = deck.size();
    }

    /**
     * Returns the last card in the deck.
     *
     * @return the last card in the deck
     */

    public Card dealCard() {
        if (deck.isEmpty())
            return null;

        final Card card = deck.get(deck.size() - 1); // last card in the deck
        deck.remove(card);
        return card;
    }

    public void showAllCards() {
        final ArrayList<String> arr = new ArrayList<>();

        for (final Card card : deck)
            arr.add(card.alias());

        System.out.println(String.join(", ", arr));
    }

    public static void main(final String[] args) {
        final Deck deck = new Deck();
        deck.showAllCards();
    }
}