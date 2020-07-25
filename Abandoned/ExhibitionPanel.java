import javax.swing.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class ExhibitionPanel extends JPanel {
    static final long serialVersionUID = 1L;

    private ArrayList<Card> cards;
    private HashMap<Card, CardPanel> cardObjMap;

    public static final int w_ = 230;
    public static final int h_ = 190;
    public static final int hintw = w_;
    public static final int hinth = 50;
    private static final int y_ = dims.centerLoc(h_, dims.card.height);

    private static final float hintFontSize = 24.0f;

    private JLabel hintLabel;

    /**
     * Constructor for HandPanel object.
     *
     * @param controller Client GUI controller
     */

    public ExhibitionPanel() {
        cards = new ArrayList<>();
        cardObjMap = new HashMap<>();

        setLayout(null);
        setOpaque(false);
        setBackground(dims.tableGreen);
        setPreferredSize(new Dimension(w_, h_));

        hintLabel = new JLabel("No Card Shown", SwingConstants.CENTER);
        hintLabel.setOpaque(true);
        hintLabel.setForeground(dims.text);
        hintLabel.setBackground(dims.tableGreen);
        hintLabel.setFont(hintLabel.getFont().deriveFont(hintFontSize));
        hintLabel.setBounds(dims.centerLoc(w_, hintw), dims.centerLoc(h_, hinth), hintw, hinth);
        add(hintLabel);

        reset();
    }

    public void reset() {
        for (CardPanel cardObj : cardObjMap.values())
            remove(cardObj);

        cards.clear();
        cardObjMap.clear();
        hintLabel.setVisible(false);
        showChanges();
    }

    public void setAxis(int x, int y) {
        setBounds(x, y, w_, h_);
        showChanges();
    }

    public void addCards(String[] cardAliases) {
        for (String cardAlias : cardAliases) {
            Card newCard = new Card(cardAlias);
            CardPanel cardObj = new CardPanel(cardAlias);
            add(cardObj);
            cards.add(newCard);
            cardObjMap.put(newCard, cardObj);
        }

        cards.sort(new Card.CardComparator());

        repositionCards();
        showChanges();
    }

    public void showHint() {
        hintLabel.setVisible(true);
        showChanges();
    }

    private void repositionCards() {
        int numCards = cards.size();
        int x = ((1 - numCards) * dims.cardGap + w_ - dims.card.width) / 2;
        for (int i = 0; i < numCards; i++) {
            Card card = cards.get(numCards - 1 - i);
            CardPanel cardObj = cardObjMap.get(card);
            setComponentZOrder(cardObj, numCards - 1 - i);
            cardObj.setBounds(x, y_, dims.card.width, dims.card.height);
            x += dims.cardGap;
        }
    }

    /**
     * Shows changes made to the panel.
     */

    private void showChanges() {
        revalidate();
        repaint();
        setVisible(true);
    }

}