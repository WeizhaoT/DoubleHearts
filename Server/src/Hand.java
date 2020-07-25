import java.util.ArrayList;

/**
 * Hand objects represent a hand of cards.
 *
 * @author Jordan Segalman
 */

public class Hand {
    public ArrayList<Card> hand = new ArrayList<>(); // holds the cards in the hand
    // public LinkedList<Card> hand = new LinkedList<>();

    /**
     * Adds a card to the hand.
     *
     * @param newCard Card to add to hand
     */

    public void addCard(Card newCard) {
        hand.add(newCard);
        hand.sort(new Card.CardComparator());
    }

    /**
     * Returns the value of the hand.
     *
     * @return the value of the hand
     */

    public int value() {
        int value = 0; // value of the hand
        for (Card card : hand) {
            value += card.value();
        }
        return value;
    }

    /**
     * Returns the number of cards in the hand.
     *
     * @return the number of cards in the hand
     */

    public int size() {
        return hand.size();
    }

    /**
     * Returns the card at the given index.
     *
     * @param index Index of the card to return
     * @return the card at the given index
     */

    public Card getCard(int index) {
        return hand.get(index);
    }

    public Card playCard(int index) {
        return hand.remove(index);
    }

    /**
     * Removes all of the cards from the hand.
     */

    public void clear() {
        hand.clear();
    }
}