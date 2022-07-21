
import java.util.*;

/**
 * {@code Asset} class maintains all asset cards of a player.
 * 
 * @author Weizhao Tang
 */

public class Asset {
    public LinkedList<Card> cards = new LinkedList<>();

    public void addAsset(final Card newCard) {
        cards.add(newCard);
    }

    public void clear() {
        cards.clear();
    }

    public int getScore(final int numDecks) {
        int total = 0;
        int heartScore = 0, pigScore = 0, sheepScore = 0, transScore = 0;

        int numHearts = 0;
        int numTrans = 0;

        final int assetSize = cards.size();
        final ArrayList<Card> transformers = new ArrayList<>();

        for (final Card card : cards) {
            if (card.isHeart()) {
                numHearts++;
                heartScore += card.value();
            } else if (card.isPig()) {
                pigScore += card.value();
            } else if (card.isSheep()) {
                sheepScore += card.value();
            } else if (card.isTransformer()) {
                transScore += card.value();
                transformers.add(card);
                numTrans++;
            }
        }

        if (assetSize == 0)
            return 0;
        if (assetSize == numTrans)
            return transScore;

        if (numHearts == 13 * numDecks) {
            heartScore = -heartScore;
            pigScore = assetSize == 16 * numDecks ? -pigScore : pigScore;
        }

        total = (int) Math.round((heartScore + pigScore + sheepScore) * Card.getMult(transformers));
        return total;
    }
}