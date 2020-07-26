
import java.util.LinkedList;

/**
 * {@code Asset} class maintains all asset cards of a player.
 * 
 * @author Weizhao Tang
 */

public class Asset {
    public LinkedList<Card> cards = new LinkedList<>();

    public void addAsset(final Card newCard) {
        cards.add(newCard);
        cards.sort(new Card.CardComparator());
    }

    public int getScore(final int numDecks) {
        int heartScore = 0;
        int pigScore = 0;

        int numTrans = 0, numTransx = 0;
        int numSheep = 0, numSheepx = 0;
        int numPig = 0, numPigx = 0;
        int numHearts = 0;

        final int propertySize = cards.size();

        for (final Card card : cards) {
            if (card.isHeart()) {
                numHearts += 1;
                heartScore += card.value();
            } else if (card.isPig()) {
                if (card.bid)
                    numPigx += 1;
                else
                    numPig += 1;
            } else if (card.isSheep()) {
                if (card.bid)
                    numSheepx += 1;
                else
                    numSheep += 1;
            } else if (card.isTransformer()) {
                if (card.bid)
                    numTransx += 1;
                else
                    numTrans += 1;
            }
        }

        if (propertySize == 0)
            return 0;

        if (propertySize == numTrans + numTransx)
            return numTrans * 50 + numTransx * 100;

        pigScore = -numPig * 100 - numPigx * 200;

        if (numHearts == 13 * numDecks) {
            heartScore = -heartScore;
            heartScore += propertySize == 16 * numDecks ? -pigScore : pigScore;
        }

        heartScore += pigScore + numSheep * 100 + numSheepx * 200;
        heartScore <<= (numTrans + numTransx * 2);
        return heartScore;
    }

    public void clear() {
        cards.clear();
    }
}