package panel;

import ui.*;
import main.*;
import rule.Card;
import layout.PokerGameLayout;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/**
 * HandPanel objects are panels that contain the cards, hand value, bet amount,
 * and hit, stand, split pairs, and double down buttons for a Blackjack hand.
 *
 * @author Jordan Segalman
 */

public class HandPanel extends JPanel implements ActionListener {
    static final long serialVersionUID = 1L;

    private static final int w_ = PokerGameLayout.handw;
    private static final int h_ = PokerGameLayout.handh;

    private static final int nTrade = 3;

    public static final int cardGap = 24;

    private static final int cardUp = 30;
    private static final int handGap = 10;
    private static final int buttonw = 140;
    private static final int wideButtonw = 160;
    private static final int buttonh = 30;
    private static final int midButtonx = (w_ - buttonw) / 2;

    private static final float buttonFontSize = 20.0f;

    private ClientController controller; // client GUI controller
    private ClientView view;

    private JPanel cardsPanel;
    private JButton midButton = new JButton("");
    private JButton seeLastRoundButton = new JButton("Last Round");

    private MouseAdapter mouseAdapter;

    private HashMap<CardPanel, Card> cardMap;
    private HashMap<Card, CardPanel> cardObjMap;

    private ArrayList<Card> cards;
    private HashSet<Card> selectedCards;
    private ArrayList<Card> leadSet = null;

    private boolean mouseControl;
    private boolean firstRound;
    private MaskMode maskMode;
    private ButtonMode buttonMode = ButtonMode.NONE;

    private enum MaskMode {
        NORMAL, ALL, SHOWABLE, TRADE,
    }

    private enum ButtonMode {
        NONE, READY, TRADE, SHOW, PLAY,
    }

    /**
     * Constructor for HandPanel object.
     *
     * @param controller Client GUI controller
     */

    public HandPanel(ClientController controller, ClientView view) {
        this.controller = controller;
        this.view = view;
        cardMap = new HashMap<>();
        cardObjMap = new HashMap<>();
        cards = new ArrayList<>();
        selectedCards = new HashSet<>();
        mouseControl = false;
        firstRound = false;

        if (ClientController.TEST_MODE)
            setBorder(BorderFactory.createLineBorder(Color.PINK));

        mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!mouseControl) {
                    return;
                } else if (!SwingUtilities.isLeftMouseButton(e)) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        clickMidButton();
                    }
                    return;
                }

                synchronized (selectedCards) {
                    CardPanel cardObj = (CardPanel) e.getComponent();
                    if (cardObj != null) {
                        int cardy = cardObj.getY();
                        Card card = cardMap.get(cardObj);

                        if (cardy == cardUp) {
                            drawCard(card);
                        } else {
                            putCard(card);
                        }
                        updateFeasibleCard();
                    }
                }
            }
        };

        setupHandPanel();
        setupActionListeners();
    }

    private void setupHandPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(w_, h_));
        setLayout(null);

        JPanel userButtonsPanel = new JPanel(null);
        userButtonsPanel.setOpaque(false);

        midButton.setFont(midButton.getFont().deriveFont(buttonFontSize));
        midButton.setBounds(midButtonx, 0, buttonw, buttonh);

        seeLastRoundButton.setFont(midButton.getFont().deriveFont(buttonFontSize));
        seeLastRoundButton.setBounds(w_ - wideButtonw, 0, wideButtonw, buttonh);
        showSeeLastRoundButton(false);
        enableSeeLastRoundButton(false);

        userButtonsPanel.add(midButton);
        userButtonsPanel.add(seeLastRoundButton);
        userButtonsPanel.setBounds(0, 0, w_, buttonh);

        add(userButtonsPanel);

        cardsPanel = new JPanel();
        cardsPanel.setLayout(null);
        cardsPanel.setOpaque(false);
        cardsPanel.setBounds(0, buttonh, w_, h_ - buttonh);
        add(cardsPanel);
    }

    /**
     * Sets up the action listeners.
     */

    private void setupActionListeners() {
        midButton.addActionListener(this);
        seeLastRoundButton.addActionListener(this);
    }

    /**
     * Shows changes made to the panel.
     */

    private void showChanges() {
        revalidate();
        repaint();
        setVisible(true);
    }

    public boolean addCard(String cardAlias) {
        synchronized (cards) {
            Card newCard = new Card(cardAlias);
            CardPanel cardObj = new CardPanel(cardAlias);
            cardObj.addMouseListener(mouseAdapter);
            cardsPanel.add(cardObj);

            cardMap.put(cardObj, newCard);
            cardObjMap.put(newCard, cardObj);

            cards.add(newCard);
            cards.sort(new Card.CardComparator());

            repositionCards();
        }

        showChanges();

        if (ClientController.TEST_MODE)
            return cards.size() == ClientController.numCards;
        else
            return cards.size() == ClientController.numDecks * 13;
    }

    public void tradeIn(String[] cardAliases) {
        synchronized (cards) {
            int i = 0;
            CardPanel[] newCardPanels = new CardPanel[cardAliases.length];
            for (String alias : cardAliases) {
                Card newCard = new Card(alias);
                CardPanel cardObj = new CardPanel(alias);
                cardObj.addMouseListener(mouseAdapter);
                cardsPanel.add(cardObj);

                cardMap.put(cardObj, newCard);
                cardObjMap.put(newCard, cardObj);

                newCardPanels[i++] = cardObj;

                selectedCards.add(newCard);
                cards.add(newCard);
            }
            cards.sort(new Card.CardComparator());
            repositionCards();

            putAllCards();
            for (CardPanel cardObj : newCardPanels) {
                drawCard(cardObj);
            }
            enableMouseControl(true);
        }
        showChanges();
    }

    public void removeSelected() {
        synchronized (cards) {
            cards.removeAll(selectedCards);

            for (Card card : selectedCards) {
                CardPanel cardObj = cardObjMap.remove(card);
                cardMap.remove(cardObj);
                cardObj.removeMouseListener(mouseAdapter);
                cardsPanel.remove(cardObj);
            }

            cards.sort(new Card.CardComparator());
            repositionCards();
        }
        showChanges();
    }

    public HashSet<String> getFeasible() {
        return getFeasible(selectedCards);
    }

    private HashSet<String> getFeasible(HashSet<Card> selected) {
        HashSet<String> feasible = new HashSet<>();
        if (selected == null)
            selected = new HashSet<>();

        int numSelected = selected.size();

        if (numSelected >= 2)
            return feasible;

        if (leadSet == null) {
            if (firstRound) {
                if (numSelected == 0)
                    feasible.add("2C");
            } else {
                if (numSelected == 0)
                    feasible.add("ALL");
                else {
                    Card card = (Card) selected.toArray()[0];
                    feasible.add(card.alias());
                }
            }
        } else {
            Card leadCard = leadSet.get(0);
            boolean hasSuit = false;
            if (leadSet.size() == 1) {
                if (numSelected >= 1)
                    return feasible;

                for (Card card : cards) {
                    if (card.suit() == leadCard.suit()) {
                        feasible.add(card.alias());
                        hasSuit = true;
                    }
                }

                if (!hasSuit)
                    feasible.add("ALL");
            } else {
                HashSet<String> sameSuit = new HashSet<>();
                HashSet<String> sameSuitPairs = new HashSet<>();

                for (Card card : cards) {
                    if (card.suit() != leadCard.suit() || selected.contains(card))
                        continue;

                    String alias = card.alias();
                    if (sameSuit.contains(alias))
                        sameSuitPairs.add(card.alias());
                    else
                        sameSuit.add(card.alias());
                }

                if (sameSuit.isEmpty()) {
                    feasible.add("ALL");
                } else if (numSelected == 0) {
                    if (sameSuitPairs.isEmpty()) {
                        feasible.addAll(sameSuit);
                    } else
                        feasible.addAll(sameSuitPairs);
                } else {
                    Card selectedCard = (Card) selected.toArray()[0];
                    if (sameSuit.contains(selectedCard.alias()))
                        feasible.add(selectedCard.alias());
                    else
                        feasible.addAll(sameSuit);
                }
            }
        }
        return feasible;
    }

    public boolean checkPlayRule() {
        int numSelected = selectedCards.size();

        if (numSelected > 2) {
            view.setPlayErrMsg("You must choose at most 2 cards to play");
            return false;
        }

        if (leadSet != null && numSelected != leadSet.size()) {
            view.setPlayErrMsg("You must choose the same number of cards to follow");
            return false;
        }

        if (leadSet == null) {
            if (firstRound) {
                for (Card card : selectedCards) {
                    if (!card.weakEquals("2C")) {
                        view.setPlayErrMsg("You must lead the first round with only "
                                + MyColors.getColoredText("\u2663 2", MyColors.clubColor) + "(s)");
                        return false;
                    }
                }
            } else {
                if (numSelected == 2 && !Card.isPair(selectedCards)) {
                    view.setPlayErrMsg("You must lead two cards only if they are a pair");
                    return false;
                }
            }
        } else {
            HashSet<Card.Rank> suitRanks = new HashSet<>();
            Card.Suit leadingSuit = leadSet.get(0).suit();
            boolean discard = false;

            for (Card card : selectedCards) {
                if (firstRound && card.forbiddenInFirstRound()) {
                    view.setPlayErrMsg("You cannot follow " + MyColors.getColoredText("\u2666 J", MyColors.diamondColor)
                            + ", " + MyColors.getColoredText("\u2660 Q", MyColors.spadeColor) + ", or "
                            + MyColors.getColoredText("\u2665 5~A", MyColors.heartColor) + " in the first round");
                    return false;
                }
                if (card.suit() != leadingSuit) {
                    discard = true;
                    break;
                }
            }

            if (!discard && (numSelected == 1 || (numSelected == 2 && Card.isPair(selectedCards)))) {
                view.setPlayErrMsg("");
                return true;
            }

            for (Card card : cards) {
                if (card.suit() != leadingSuit)
                    continue;

                if (discard && !selectedCards.contains(card)) {
                    view.setPlayErrMsg("You must not discard when the leading suit is not empty");
                    view.showChanges();
                    return false;
                }

                if (numSelected == 2) {
                    if (suitRanks.contains(card.rank())) {
                        view.setPlayErrMsg("You must follow a pair when you have one in the leading suit");
                        view.showChanges();
                        return false;
                    }
                    suitRanks.add(card.rank());
                }
            }
        }
        view.setPlayErrMsg("");
        return true;
    }

    public boolean checkShowRule() {
        for (Card card : selectedCards) {
            if (!card.isShowable()) {
                view.setPlayErrMsg(
                        "You cannot show cards other than " + MyColors.getColoredText("\u2663 T", MyColors.clubColor)
                                + ", " + MyColors.getColoredText("\u2666 J", MyColors.diamondColor) + ", or "
                                + MyColors.getColoredText("\u2660 Q", MyColors.spadeColor));
                return false;
            }
        }
        view.setPlayErrMsg("");
        return true;
    }

    public boolean checkTradeRule() {
        if (selectedCards.size() != nTrade) {
            view.setPlayErrMsg("You must trade out exactly " + nTrade + " cards");
            return false;
        }
        view.setPlayErrMsg("");
        return true;
    }

    public void setMaskMode(String mode) {
        maskMode = MaskMode.valueOf(mode);
        updateFeasibleCard();
    }

    public void updateFeasibleCard() {
        if (maskMode == MaskMode.ALL) {
            setAllCardsFeasible();
            return;
        } else if (maskMode == MaskMode.SHOWABLE) {
            setShowablesFeasible();
            return;
        } else if (maskMode == MaskMode.TRADE) {
            setMaxNCardsFeasible();
            return;
        }

        HashSet<String> feasible = getFeasible();

        if (feasible.contains("ALL")) {
            for (CardPanel cardObj : cardMap.keySet()) {
                Card card = cardMap.get(cardObj);
                if (selectedCards.contains(card))
                    cardObj.maskOff();
                else if (feasible.contains(card.alias()) || (firstRound && card.forbiddenInFirstRound()))
                    cardObj.maskOn();
                else
                    cardObj.maskOff();
            }
        } else {
            for (CardPanel cardObj : cardMap.keySet()) {
                Card card = cardMap.get(cardObj);

                if (selectedCards.contains(card))
                    cardObj.maskOff();
                else if (firstRound && card.forbiddenInFirstRound() || !feasible.contains(card.alias()))
                    cardObj.maskOn();
                else
                    cardObj.maskOff();
            }
        }
        showChanges();
    }

    private void setAllCardsFeasible() {
        for (CardPanel cardObj : cardMap.keySet()) {
            cardObj.maskOff();
        }
        showChanges();
    }

    private void setShowablesFeasible() {
        for (Card card : cardObjMap.keySet()) {
            CardPanel cardObj = cardObjMap.get(card);
            if (card.isShowable()) {
                cardObj.maskOff();
            } else {
                cardObj.maskOn();
            }
        }

        if (!selectedCards.isEmpty() && buttonMode == ButtonMode.SHOW)
            midButton.setText("Show");

        showChanges();
    }

    private void setMaxNCardsFeasible() {
        if (selectedCards.size() < nTrade) {
            setAllCardsFeasible();
        } else {
            for (Card card : cardObjMap.keySet()) {
                CardPanel cardObj = cardObjMap.get(card);
                if (selectedCards.contains(card)) {
                    cardObj.maskOff();
                } else {
                    cardObj.maskOn();
                }
            }
            showChanges();
        }
    }

    public void enableReadyButton() {
        buttonMode = ButtonMode.READY;
        midButton.setText("Ready");
        midButton.setEnabled(true);
        midButton.setVisible(true);
        showChanges();
    }

    public void enablePlayButton() {
        buttonMode = ButtonMode.PLAY;
        midButton.setText("Play");
        midButton.setVisible(true);
        updateButton();
        showChanges();
        System.err.println("play enabled");
    }

    public void enableShowButton() {
        buttonMode = ButtonMode.SHOW;
        midButton.setEnabled(true);
        midButton.setVisible(true);
        updateButton();
        showChanges();
    }

    public void enableTradeButton() {
        buttonMode = ButtonMode.TRADE;
        midButton.setText("TRADE");
        midButton.setVisible(true);
        updateButton();
        showChanges();
    }

    public void disableMidButton() {
        midButton.setEnabled(false);
        midButton.setVisible(false);
    }

    public void enableSeeLastRoundButton(Boolean b) {
        seeLastRoundButton.setEnabled(b);
        showChanges();
    }

    public void showSeeLastRoundButton(Boolean b) {
        seeLastRoundButton.setVisible(b);
        if (!b)
            seeLastRoundButton.setEnabled(b);

        showChanges();
    }

    public void enableMouseControl(boolean b) {
        mouseControl = b;
    }

    public void setFirstRound(boolean b) {
        firstRound = b;
    }

    public void setLeadCards(String[] cardAliases) {
        if (cardAliases == null || cardAliases.length == 0) {
            leadSet = null;
        } else {
            leadSet = new ArrayList<>();
            for (String alias : cardAliases) {
                leadSet.add(new Card(alias));
            }
        }
    }

    private void drawCard(Card card) {
        CardPanel cardObj = cardObjMap.get(card);
        cardObj.setLocation(cardObj.getX(), handGap);
        selectedCards.add(card);

        updateButton();
    }

    private void drawCard(CardPanel cardObj) {
        Card card = cardMap.get(cardObj);
        drawCard(card);
    }

    private void putCard(Card card) {
        CardPanel cardObj = cardObjMap.get(card);
        cardObj.setLocation(cardObj.getX(), cardUp);
        selectedCards.remove(card);

        updateButton();
    }

    private void updateButton() {
        if (buttonMode == ButtonMode.PLAY) {
            midButton.setEnabled(selectedCards.size() > 0);
        } else if (buttonMode == ButtonMode.SHOW) {
            midButton.setText(selectedCards.size() > 0 ? "Show" : "Pass");
        } else if (buttonMode == ButtonMode.TRADE) {
            midButton.setEnabled(selectedCards.size() == nTrade);
        }
    }

    private void drawAllCards() {
        for (Card card : cards) {
            drawCard(card);
        }
    }

    private void putAllCards() {
        Card[] cardArr = selectedCards.toArray(new Card[0]);
        for (Card card : cardArr) {
            putCard(card);
        }
    }

    private void repositionCards() {
        int numCards = cards.size();
        int x = ((1 - numCards) * cardGap + w_ - CardPanel.w_) / 2;
        for (int i = 0; i < numCards; i++) {
            Card card = cards.get(numCards - 1 - i);
            CardPanel cardObj = cardObjMap.get(card);
            cardsPanel.setComponentZOrder(cardObj, numCards - 1 - i);
            cardObj.setBounds(x, cardUp, CardPanel.w_, CardPanel.h_);
            x += cardGap;
        }
    }

    public void setExhibitedCards() {
        for (Card card : selectedCards) {
            CardPanel cardObj = cardObjMap.get(card);
            card.bidCard();
            cardObj.setIlluminate(true);
        }
        selectedCards.clear();
        repositionCards();
        showChanges();
    }

    /**
     * Try to play last card in hand
     * 
     * @return {@code true} if executed; {@code false} if skipped
     */
    public boolean autoPlayLastCard() {
        if (cards.size() == 1 && buttonMode == ButtonMode.PLAY) {
            drawCard(cards.get(0));
            midButton.doClick();
            return true;
        }
        return false;
    }

    /**
     * Try to follow with all remaining cards in hand
     * 
     * @return {@code true} if executed; {@code false} if skipped
     */
    public boolean autoFollowLastRound() {
        if (leadSet != null && cards.size() == leadSet.size() && buttonMode == ButtonMode.PLAY) {
            drawAllCards();
            midButton.doClick();
            return true;
        }
        return false;
    }

    public void autoChooseOnlyOption() {
        if (leadSet == null)
            return;

        HashSet<String> feasibleAliases = getFeasible(null);
        HashSet<Card> feasibleCards = new HashSet<>();

        for (Card card : cards) {
            if (feasibleAliases.contains(card.alias()))
                feasibleCards.add(card);
        }

        if (leadSet.size() >= feasibleCards.size()) {
            putAllCards();
            for (Card card : feasibleCards) {
                drawCard(card);
            }
        }
    }

    public void clickMidButton() {
        if (mouseControl && buttonMode != ButtonMode.READY && midButton.isEnabled() && midButton.isVisible()) {
            midButton.doClick();
        }
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public void reset() {
        enableMouseControl(false);

        for (CardPanel cardObj : cardMap.keySet()) {
            cardsPanel.remove(cardObj);
        }

        cardMap.clear();
        cardObjMap.clear();
        cards.clear();
        selectedCards.clear();
        leadSet = null;

        disableMidButton();
        enableSeeLastRoundButton(false);
        showChanges();
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e Event generated by component action
     */

    @Override
    public void actionPerformed(ActionEvent e) {
        Object target = e.getSource();

        if (target == seeLastRoundButton) {
            view.showAllHistory();
        } else if (target == midButton) {
            switch (buttonMode) {
                case PLAY:
                    if (checkPlayRule()) {
                        disableMidButton();
                        removeSelected();
                        setMaskMode("ALL");
                        controller.sendToServer("PLAY", Card.concatCards(ClientController.SEND_DELIM, selectedCards));
                        selectedCards.clear();
                    } else {
                        putAllCards();
                    }
                    break;
                case READY:
                    controller.sendToServer("READY");
                    break;
                case TRADE:
                    if (checkTradeRule()) {
                        removeSelected();
                        enableMouseControl(false);
                        controller.sendToServer("TRADE", Card.concatCards(ClientController.SEND_DELIM, selectedCards));
                        selectedCards.clear();
                    } else {
                        putAllCards();
                    }
                    break;
                case SHOW:
                    if (selectedCards.isEmpty())
                        controller.sendToServer("SHOW");
                    else if (checkShowRule()) {
                        controller.sendToServer("SHOW", Card.concatCards(ClientController.SEND_DELIM, selectedCards));
                        enableMouseControl(false);
                    } else {
                        putAllCards();
                    }
                    break;
                default:
                    System.err.println("Warning: pressing button at NONE mode");
            }
        }
    }
}