import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

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
    private static final int buttonh = 30;
    private static final int midButtonx = (w_ - buttonw) / 2;

    private static final float buttonFontSize = 20.0f;

    private ClientController controller; // client GUI controller
    private ClientView view;

    private JPanel cardsPanel;
    private JButton midButton;
    private JButton seeLastRoundButton;

    private MouseAdapter mouseAdapter;

    private final ConcurrentHashMap<CardPanel, Card> cardMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Card, CardPanel> cardObjMap = new ConcurrentHashMap<>();

    private final ArrayList<Card> cards = new ArrayList<>();
    private final ConcurrentHashMap<Card, Boolean> selectedCards = new ConcurrentHashMap<>();

    private ArrayList<Card> leadSet = null;

    private boolean mouseControl;
    private boolean firstRound;
    private MaskMode maskMode;
    private ButtonMode buttonMode;

    private enum MaskMode {
        NORMAL, ALL, SHOWABLE, TRADE,
    }

    private enum ButtonMode {
        READY, TRADE, SHOW, PLAY,
    }

    /**
     * Constructor for HandPanel object.
     *
     * @param controller Client GUI controller
     */

    public HandPanel(final ClientController controller, final ClientView view) {
        this.controller = controller;
        this.view = view;
        mouseControl = false;
        firstRound = false;

        mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                if (!mouseControl)
                    return;

                if (!SwingUtilities.isLeftMouseButton(e)) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        if (buttonMode != ButtonMode.READY && midButton.isEnabled() && midButton.isVisible()) {
                            midButton.doClick();
                        }
                    }
                    return;
                }

                synchronized (selectedCards) {
                    final CardPanel cardObj = (CardPanel) e.getComponent();
                    if (cardObj != null) {
                        final int cardy = cardObj.getY();
                        final Card card = cardMap.get(cardObj);

                        if (cardy == cardUp) {
                            drawCard(card);
                            if (selectedCards.size() == 1) {
                                if (buttonMode == ButtonMode.PLAY) {
                                    midButton.setEnabled(true);
                                } else if (buttonMode == ButtonMode.SHOW) {
                                    midButton.setText("Show");
                                }
                            }
                        } else {
                            putCard(card);
                            if (selectedCards.size() == 0) {
                                if (buttonMode == ButtonMode.PLAY) {
                                    midButton.setEnabled(false);
                                } else if (buttonMode == ButtonMode.SHOW) {
                                    midButton.setText("Pass");
                                }
                            }
                        }

                        if (buttonMode == ButtonMode.TRADE) {
                            midButton.setEnabled(selectedCards.size() == nTrade);
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

        final JPanel userButtonsPanel = new JPanel(null);
        userButtonsPanel.setOpaque(false);

        midButton = new JButton("");
        midButton.setFont(midButton.getFont().deriveFont(buttonFontSize));
        midButton.setBounds(midButtonx, 0, buttonw, buttonh);

        // playButton = new JButton("Play");
        // playButton.setFont(playButton.getFont().deriveFont(buttonFontSize));
        // playButton.setBounds(midButtonx, 0, buttonw, buttonh);
        // enablePlayButton(false);

        // readyButton = new JButton("Ready");
        // readyButton.setFont(readyButton.getFont().deriveFont(buttonFontSize));
        // readyButton.setBounds(midButtonx, 0, buttonw, buttonh);
        // enableReadyButton(true);

        // tradeButton = new JButton("Trade");
        // tradeButton.setFont(tradeButton.getFont().deriveFont(buttonFontSize));
        // tradeButton.setBounds(midButtonx, 0, buttonw, buttonh);
        // enableTradeButton(false);

        // showButton = new JButton("Pass");
        // showButton.setFont(showButton.getFont().deriveFont(buttonFontSize));
        // showButton.setBounds(midButtonx, 0, buttonw, buttonh);
        // enableShowButton(false);

        seeLastRoundButton = new JButton("Last Round");
        seeLastRoundButton.setFont(midButton.getFont().deriveFont(buttonFontSize));
        seeLastRoundButton.setBounds(w_ - buttonw, 0, buttonw, buttonh);
        showSeeLastRoundButton(false);
        enableSeeLastRoundButton(false);

        userButtonsPanel.add(midButton);
        // userButtonsPanel.add(readyButton);
        // userButtonsPanel.add(showButton);
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
        // playButton.addActionListener(this);
        // readyButton.addActionListener(this);
        // tradeButton.addActionListener(this);
        // showButton.addActionListener(this);
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

    public boolean addCard(final String cardAlias) {
        synchronized (cards) {
            final Card newCard = new Card(cardAlias);
            final CardPanel cardObj = new CardPanel(cardAlias);
            cardObj.addMouseListener(mouseAdapter);
            // cardObj.setBounds(0, 0, dims.card.width, dims.card.height);
            cardsPanel.add(cardObj);

            cardMap.put(cardObj, newCard);
            cardObjMap.put(newCard, cardObj);

            cards.add(newCard);
            cards.sort(new Card.CardComparator());

            repositionCards();
        }

        showChanges();
        // view.showChanges();

        if (ClientController.TEST_MODE)
            return cards.size() == ClientController.numCards;
        else
            return cards.size() == ClientController.numDecks * 13;
    }

    public void tradeIn(final String[] cardAliases) {
        synchronized (cards) {
            int i = 0;
            final CardPanel[] newCardPanels = new CardPanel[cardAliases.length];
            for (final String alias : cardAliases) {
                final Card newCard = new Card(alias);
                final CardPanel cardObj = new CardPanel(alias);
                cardObj.addMouseListener(mouseAdapter);
                cardsPanel.add(cardObj);

                cardMap.put(cardObj, newCard);
                cardObjMap.put(newCard, cardObj);

                newCardPanels[i++] = cardObj;

                selectedCards.put(newCard, true);
                cards.add(newCard);
            }
            cards.sort(new Card.CardComparator());
            repositionCards();

            putAllCards();
            for (final CardPanel cardObj : newCardPanels) {
                drawCard(cardObj);
            }
            enableMouseControl(true);
        }
        showChanges();
    }

    public void removeSelected() {
        synchronized (cards) {
            cards.removeAll(selectedCards.keySet());

            for (final Card card : selectedCards.keySet()) {
                final CardPanel cardObj = cardObjMap.remove(card);
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

    private HashSet<String> getFeasible(ConcurrentHashMap<Card, Boolean> selected) {
        final HashSet<String> feasible = new HashSet<>();
        if (selected == null)
            selected = new ConcurrentHashMap<>();

        final int numSelected = selected.size();

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
                    final Card card = selected.keySet().toArray(new Card[0])[0];
                    feasible.add(card.alias());
                }
            }
        } else {
            final Card leadCard = leadSet.get(0);
            boolean hasSuit = false;
            if (leadSet.size() == 1) {
                if (numSelected >= 1)
                    return feasible;

                for (final Card card : cards) {
                    if (card.suit() == leadCard.suit()) {
                        feasible.add(card.alias());
                        hasSuit = true;
                    }
                }

                if (!hasSuit)
                    feasible.add("ALL");
            } else {
                final HashSet<String> sameSuit = new HashSet<>();
                final HashSet<String> sameSuitPairs = new HashSet<>();

                for (final Card card : cards) {
                    if (card.suit() != leadCard.suit() || selected.contains(card))
                        continue;

                    final String alias = card.alias();
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
                    final Card selectedCard = selected.keySet().toArray(new Card[0])[0];
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
        final int numSelected = selectedCards.size();

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
                for (final Card card : selectedCards.keySet()) {
                    if (!card.weakEquals("2C")) {
                        view.setPlayErrMsg("You must lead the first round with only "
                                + MyColors.getColoredText("\u2663 2", MyColors.clubColor) + "(s)");
                        return false;
                    }
                }
            } else {
                if (numSelected == 2 && !Card.isPair(selectedCards.keySet())) {
                    view.setPlayErrMsg("You must lead two cards only if they are a pair");
                    return false;
                }
            }
        } else {
            final HashSet<Card.Rank> suitRanks = new HashSet<>();
            final Card.Suit leadingSuit = leadSet.get(0).suit();
            boolean discard = false;

            for (final Card card : selectedCards.keySet()) {
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

            if (!discard && (numSelected == 1 || (numSelected == 2 && Card.isPair(selectedCards.keySet())))) {
                view.setPlayErrMsg("");
                return true;
            }

            for (final Card card : cards) {
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
        for (final Card card : selectedCards.keySet()) {
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

    public void setMaskMode(final String mode) {
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

        final HashSet<String> feasible = getFeasible();

        if (feasible.contains("ALL")) {
            for (final CardPanel cardObj : cardMap.keySet()) {
                final Card card = cardMap.get(cardObj);
                if (selectedCards.contains(card))
                    cardObj.maskOff();
                else if (feasible.contains(card.alias()) || (firstRound && card.forbiddenInFirstRound()))
                    cardObj.maskOn();
                else
                    cardObj.maskOff();
            }
        } else {
            for (final CardPanel cardObj : cardMap.keySet()) {
                final Card card = cardMap.get(cardObj);

                if (selectedCards.contains(card))
                    cardObj.maskOff();
                else if (firstRound && card.forbiddenInFirstRound() || !feasible.contains(card.alias()))
                    cardObj.maskOn();
                else
                    cardObj.maskOff();
            }
        }
        System.out.println("Some cards are set feasible");
        showChanges();
    }

    private void setAllCardsFeasible() {
        for (final CardPanel cardObj : cardMap.keySet()) {
            cardObj.maskOff();
        }
        showChanges();
    }

    private void setShowablesFeasible() {
        for (final Card card : cardObjMap.keySet()) {
            final CardPanel cardObj = cardObjMap.get(card);
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
            for (final Card card : cardObjMap.keySet()) {
                final CardPanel cardObj = cardObjMap.get(card);
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
        midButton.setEnabled(true);
        midButton.setVisible(true);
        showChanges();
    }

    public void enableShowButton() {
        buttonMode = ButtonMode.SHOW;
        midButton.setText(selectedCards.isEmpty() ? "Pass" : "Show");
        midButton.setEnabled(true);
        midButton.setVisible(true);
        showChanges();
    }

    public void enableTradeButton() {
        buttonMode = ButtonMode.TRADE;
        midButton.setText("TRADE");
        midButton.setEnabled(true);
        midButton.setVisible(true);
        showChanges();
    }

    public void disableMidButton() {
        midButton.setEnabled(false);
        midButton.setVisible(false);
    }

    public void enableSeeLastRoundButton(final Boolean b) {
        seeLastRoundButton.setEnabled(b);
        showChanges();
    }

    public void showSeeLastRoundButton(final Boolean b) {
        seeLastRoundButton.setVisible(b);
        if (!b)
            seeLastRoundButton.setEnabled(b);

        showChanges();
    }

    public void enableMouseControl(final boolean b) {
        mouseControl = b;
    }

    public void setFirstRound(final boolean b) {
        firstRound = b;
    }

    public void setLeadCards(final String[] cardAliases) {
        if (cardAliases == null || cardAliases.length == 0) {
            leadSet = null;
        } else {
            leadSet = new ArrayList<>();
            for (final String alias : cardAliases) {
                leadSet.add(new Card(alias));
            }
        }
    }

    private void drawCard(final Card card) {
        final CardPanel cardObj = cardObjMap.get(card);
        cardObj.setLocation(cardObj.getX(), handGap);
        selectedCards.put(card, true);
    }

    private void drawCard(final CardPanel cardObj) {
        final Card card = cardMap.get(cardObj);
        cardObj.setLocation(cardObj.getX(), handGap);
        selectedCards.put(card, true);
    }

    private void putCard(final Card card) {
        final CardPanel cardObj = cardObjMap.get(card);
        cardObj.setLocation(cardObj.getX(), cardUp);
        selectedCards.remove(card);
    }

    private void putAllCards() {
        for (final Card card : selectedCards.keySet()) {
            putCard(card);
        }
    }

    private void repositionCards() {
        final int numCards = cards.size();
        int x = ((1 - numCards) * cardGap + w_ - CardPanel.w_) / 2;
        for (int i = 0; i < numCards; i++) {
            final Card card = cards.get(numCards - 1 - i);
            final CardPanel cardObj = cardObjMap.get(card);
            cardsPanel.setComponentZOrder(cardObj, numCards - 1 - i);
            cardObj.setBounds(x, cardUp, CardPanel.w_, CardPanel.h_);
            x += cardGap;
        }
    }

    public void setExhibitedCards() {
        for (final Card card : selectedCards.keySet()) {
            final CardPanel cardObj = cardObjMap.get(card);
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
            selectedCards.put(cards.get(0), true);
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
            for (Card card : cards)
                selectedCards.put(card, true);

            midButton.doClick();
            return true;
        }
        return false;
    }

    public void autoChooseOnlyOption() {
        if (leadSet == null)
            return;

        final HashSet<String> feasibleAliases = getFeasible(null);
        final HashSet<Card> feasibleCards = new HashSet<>();

        for (final Card card : cards) {
            if (feasibleAliases.contains(card.alias()))
                feasibleCards.add(card);
        }

        if (leadSet.size() >= feasibleCards.size()) {
            putAllCards();
            for (final Card card : feasibleCards) {
                drawCard(card);
            }
        }
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public void reset() {
        enableMouseControl(false);

        for (final Card card : cards) {
            final CardPanel cardObj = cardObjMap.get(card);
            remove(cardObj);
        }

        cardMap.clear();
        cardObjMap.clear();
        cards.clear();
        selectedCards.clear();
        leadSet = null;

        disableMidButton();
        // enableReadyButton(false);
        // enablePlayButton(false);
        // enableShowButton(false);
        // enableTradeButton(false);
        enableSeeLastRoundButton(false);
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e Event generated by component action
     */

    @Override
    public void actionPerformed(final ActionEvent e) {
        final Object target = e.getSource();

        if (target == seeLastRoundButton) {
            view.showAllHistory();
        } else if (target == midButton) {
            switch (buttonMode) {
                case PLAY:
                    if (checkPlayRule()) {
                        disableMidButton();
                        removeSelected();
                        setMaskMode("ALL");
                        controller.sendToServer("PLAY",
                                Card.concatCards(ClientController.SEND_DELIM, selectedCards.keySet()));
                        selectedCards.clear();
                    }
                    break;
                case READY:
                    controller.sendToServer("READY");
                    break;
                case TRADE:
                    if (checkTradeRule()) {
                        removeSelected();
                        enableMouseControl(false);
                        controller.sendToServer("TRADE",
                                Card.concatCards(ClientController.SEND_DELIM, selectedCards.keySet()));
                        selectedCards.clear();
                    }
                    break;
                case SHOW:
                    if (selectedCards.isEmpty())
                        controller.sendToServer("SHOW");
                    else if (checkShowRule()) {
                        controller.sendToServer("SHOW",
                                Card.concatCards(ClientController.SEND_DELIM, selectedCards.keySet()));
                        enableMouseControl(false);
                    }
                    break;
                default:
            }
        }
    }
}