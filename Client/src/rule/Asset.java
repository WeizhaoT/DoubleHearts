package rule;

import java.util.LinkedList;

public class Asset {
    private final LinkedList<Card> property = new LinkedList<>();

    public void addAsset(final Card newCard) {
        property.add(newCard);
        property.sort(new Card.CardComparator());
    }

    public int computeScore(final int numDecks) {
        int heartScore = 0;
        int pigScore = 0;

        int numTrans = 0;
        int numTransx = 0;
        int numSheep = 0;
        int numSheepx = 0;
        int numPig = 0;
        int numPigx = 0;
        int numHearts = 0;

        final int propertySize = property.size();

        if (propertySize == 0)
            return 0;

        for (final Card card : property) {
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

        if (propertySize == numTrans + numTransx)
            return numTrans * 50 + numTransx * 100;

        pigScore = -numPig * 100 - numPigx * 200;

        if (numHearts == 13 * numDecks) {
            heartScore = -heartScore;

            if (propertySize == 16 * numDecks)
                pigScore = -pigScore;

            heartScore += pigScore + numSheep * 100 + numSheepx * 200;
        }

        for (int i = 0; i < numTrans; i++)
            heartScore *= 2;

        for (int i = 0; i < numTransx; i++)
            heartScore *= 4;

        return heartScore;
    }

    public void clear() {
        property.clear();
    }
}