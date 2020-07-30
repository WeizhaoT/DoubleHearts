package panel;

import ui.*;
import main.*;
import rule.Card;
import element.MaskedCard;
import layout.PokerGameLayout;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

/**
 * HandPanel objects are panels that contain the cards and interactive buttons
 * for a hand.
 *
 * @author Weizhao Tang
 */

public class HandPanel extends JPanel implements ActionListener {
    static final long serialVersionUID = 1L;

    public static final int nTrade = 3;
    public static final int cardGap = 24;

    private static final int w_ = PokerGameLayout.handw;
    private static final int h_ = PokerGameLayout.handh;

    private static final int cardUp = 30;
    private static final int handGap = 10;
    private static final int buttonw = 140;
    private static final int wideButtonw = 150;
    private static final int buttonh = 30;
    private static final int midButtonx = (w_ - buttonw) / 2;

    private ClientController controller; // client GUI controller
    private ClientView view;

    private JPanel cardsPanel;
    private final JButton midButton = new JButton("");
    private final JButton seeLastRoundButton = new JButton("");

    private MouseAdapter mouseAdapter;

    private HashMap<MaskedCard, Card> cardMap;
    private HashMap<Card, MaskedCard> cardObjMap;

    private ArrayList<Card> cards;
    private HashSet<Card> selectedCards;
    private ArrayList<Card> leadSet = null;

    private boolean mouseControl;
    private boolean firstRound;
    private int numDealingCards = -1;
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

    public HandPanel(final ClientController controller, final ClientView view) {
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
            public void mousePressed(final MouseEvent e) {
                if (!mouseControl) {
                    return;
                } else if (!SwingUtilities.isLeftMouseButton(e)) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        clickMidButton();
                    }
                    return;
                }

                synchronized (selectedCards) {
                    final MaskedCard cardObj = (MaskedCard) e.getComponent();
                    if (cardObj != null) {
                        final int cardy = cardObj.getY();
                        final Card card = cardMap.get(cardObj);

                        if (cardy == cardUp) {
                            drawCard(card);
                        } else {
                            putCard(card);
                        }
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

        midButton.setFont(MyText.getButtonFont());
        midButton.setBounds(midButtonx, 0, buttonw, buttonh);

        seeLastRoundButton.setText(MyText.getButtonLabel("LAST ROUND"));
        seeLastRoundButton.setFont(MyText.getButtonFont());
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

    public void reset() {
        enableMouseControl(false);

        for (final MaskedCard cardObj : cardMap.keySet()) {
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
     * Shows changes made to the panel.
     */

    private void showChanges() {
        revalidate();
        repaint();
        setVisible(true);
    }

    public void setNumDealingCards(int num) {
        numDealingCards = num;
    }

    public boolean addCard(final String cardAlias) {
        synchronized (cards) {
            final Card newCard = new Card(cardAlias);
            final MaskedCard cardObj = new MaskedCard(cardAlias);
            cardObj.addMouseListener(mouseAdapter);
            cardsPanel.add(cardObj);

            cardMap.put(cardObj, newCard);
            cardObjMap.put(newCard, cardObj);

            cards.add(newCard);
            cards.sort(new Card.CardComparator());

            repositionCards();
        }

        showChanges();

        return cards.size() == numDealingCards;
    }

    public void tradeIn(final String[] cardAliases) {
        synchronized (cards) {
            int i = 0;
            final MaskedCard[] newCardPanels = new MaskedCard[cardAliases.length];
            for (final String alias : cardAliases) {
                final Card newCard = new Card(alias);
                final MaskedCard cardObj = new MaskedCard(alias);
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
            for (final MaskedCard cardObj : newCardPanels) {
                drawCard(cardObj);
            }
            updateButton();
            updateFeasibleCard();
            enableMouseControl(true);
        }
        showChanges();
    }

    public void removeSelected() {
        synchronized (cards) {
            cards.removeAll(selectedCards);

            for (final Card card : selectedCards) {
                final MaskedCard cardObj = cardObjMap.remove(card);
                cardMap.remove(cardObj);
                cardObj.removeMouseListener(mouseAdapter);
                cardsPanel.remove(cardObj);
            }

            cards.sort(new Card.CardComparator());
            repositionCards();
        }
        showChanges();
    }

    private HashSet<Card> getHintFeasible() {
        return Card.getHintFeasible(cards, leadSet, selectedCards, firstRound);
    }

    private HashSet<Card> getNoSelectFeasible() {
        return Card.getFeasible(cards, leadSet, null, firstRound);
    }

    private HashSet<Card> getExclFeasible() {
        return Card.getFeasible(cards, leadSet, selectedCards, firstRound);
    }

    private int checkPlayRule() {
        final int numSelected = selectedCards.size();

        if (numSelected > 2) {
            return MyText.CARD_NUM_EXCESS;
        }

        if (leadSet != null && numSelected != leadSet.size()) {
            return MyText.CARD_NUM_MISMATCH;
        }

        if (leadSet == null) {
            if (firstRound) {
                for (final Card card : selectedCards) {
                    if (!card.weakEquals("2C")) {
                        return MyText.ILLEGAL_FIRST_LEAD;

                    }
                }
            } else if (numSelected == 2 && !Card.isPair(selectedCards)) {
                return MyText.ILLEGAL_DOUBLE_LEAD;

            }
        } else {
            final HashSet<Card.Rank> suitRanks = new HashSet<>();
            final Card.Suit leadingSuit = leadSet.get(0).suit();
            boolean discard = false;

            for (final Card card : selectedCards) {
                if (firstRound && card.forbiddenInFirstRound())
                    return MyText.BANNED_FIRST_ROUND_FOLLOW;

                if (card.suit() != leadingSuit) {
                    discard = true;
                    break;
                }
            }

            if (!discard && (numSelected == 1 || (numSelected == 2 && Card.isPair(selectedCards))))
                return MyText.NORMAL;

            for (final Card card : cards) {
                if (card.suit() != leadingSuit)
                    continue;

                if (discard && !selectedCards.contains(card))
                    return MyText.ILLEGAL_DISCARD;

                if (numSelected == 2) {
                    if (suitRanks.contains(card.rank()))
                        return MyText.ILLEGAL_PAIR_FOLLOW;

                    suitRanks.add(card.rank());
                }
            }
        }
        return MyText.NORMAL;
    }

    private int checkShowRule() {
        for (final Card card : selectedCards) {
            if (!card.isShowable()) {
                return MyText.ILLEGAL_SHOWING;
            }
        }
        return MyText.NORMAL;
    }

    private int checkTradeRule() {
        return selectedCards.size() == nTrade ? MyText.NORMAL : MyText.TRADE_NUM_MISMATCH;
    }

    public void setMaskMode(final String mode) {
        maskMode = MaskMode.valueOf(mode);
        updateFeasibleCard();
    }

    public void updateFeasibleCard() {
        if (maskMode == MaskMode.ALL) {
            setAllCardsFeasible();
        } else if (maskMode == MaskMode.SHOWABLE) {
            setShowablesFeasible();
        } else if (maskMode == MaskMode.TRADE) {
            setMaxNCardsFeasible();
        } else {
            final HashSet<Card> feasible = getHintFeasible();
            for (final MaskedCard cardObj : cardMap.keySet()) {
                cardObj.setLightened(feasible.contains(cardMap.get(cardObj)));
            }
        }
    }

    private void setAllCardsFeasible() {
        for (final MaskedCard cardObj : cardMap.keySet()) {
            cardObj.setLightened(true);
        }
    }

    private void setShowablesFeasible() {
        for (final Card card : cardObjMap.keySet()) {
            cardObjMap.get(card).setLightened(card.isShowable());
        }
    }

    private void setMaxNCardsFeasible() {
        if (selectedCards.size() < nTrade) {
            setAllCardsFeasible();
        } else {
            for (final Card card : cardObjMap.keySet()) {
                cardObjMap.get(card).setLightened(selectedCards.contains(card));
            }
        }
    }

    public void enableReadyButton() {
        buttonMode = ButtonMode.READY;
        midButton.setText(MyText.getButtonLabel("READY"));
        midButton.setEnabled(true);
        midButton.setVisible(true);
    }

    public void enablePlayButton() {
        buttonMode = ButtonMode.PLAY;
        midButton.setText(MyText.getButtonLabel("PLAY"));
        midButton.setVisible(true);
        updateButton();
    }

    public void enableShowButton() {
        buttonMode = ButtonMode.SHOW;
        midButton.setEnabled(true);
        midButton.setVisible(true);
        updateButton();
    }

    public void enableTradeButton() {
        buttonMode = ButtonMode.TRADE;
        midButton.setText(MyText.getButtonLabel("TRADE"));
        midButton.setVisible(true);
        updateButton();
    }

    public void disableMidButton() {
        buttonMode = ButtonMode.NONE;
        midButton.setEnabled(false);
        midButton.setVisible(false);
    }

    public void enableSeeLastRoundButton(final Boolean b) {
        seeLastRoundButton.setEnabled(b);
    }

    public void showSeeLastRoundButton(final Boolean b) {
        seeLastRoundButton.setVisible(b);
        if (!b)
            seeLastRoundButton.setEnabled(b);
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
        drawCard(card, true);
    }

    private void drawCard(final MaskedCard cardObj) {
        final Card card = cardMap.get(cardObj);
        drawCard(card, false);
    }

    private void drawCard(final Card card, boolean update) {
        final MaskedCard cardObj = cardObjMap.get(card);
        cardObj.setLocation(cardObj.getX(), handGap);
        selectedCards.add(card);

        if (update) {
            updateButton();
            updateFeasibleCard();
        }
    }

    private void putCard(final Card card) {
        putCard(card, true);
    }

    private void putCard(final Card card, boolean update) {
        final MaskedCard cardObj = cardObjMap.get(card);
        cardObj.setLocation(cardObj.getX(), cardUp);
        selectedCards.remove(card);

        if (update) {
            updateButton();
            updateFeasibleCard();
        }
    }

    private void drawAllCards() {
        for (final Card card : cards) {
            drawCard(card, false);
        }
        updateButton();
        updateFeasibleCard();
    }

    private void putAllCards() {
        final Card[] cardArr = selectedCards.toArray(new Card[0]);
        for (final Card card : cardArr) {
            putCard(card, false);
        }
        updateButton();
        updateFeasibleCard();
    }

    private void updateButton() {
        if (buttonMode == ButtonMode.PLAY) {
            midButton.setEnabled(selectedCards.size() > 0);
        } else if (buttonMode == ButtonMode.SHOW) {
            midButton.setText(MyText.getButtonLabel(selectedCards.size() > 0 ? "SHOW" : "PASS"));
        } else if (buttonMode == ButtonMode.TRADE) {
            midButton.setEnabled(selectedCards.size() == nTrade);
        }
    }

    private void repositionCards() {
        final int numCards = cards.size();
        int x = ((1 - numCards) * cardGap + w_ - MaskedCard.w_) / 2;
        for (int i = 0; i < numCards; i++) {
            final Card card = cards.get(numCards - 1 - i);
            final MaskedCard cardObj = cardObjMap.get(card);
            cardsPanel.setComponentZOrder(cardObj, numCards - 1 - i);
            cardObj.setBounds(x, cardUp, MaskedCard.w_, MaskedCard.h_);
            x += cardGap;
        }
    }

    public void confirmShownCards() {
        for (final Card card : selectedCards) {
            final MaskedCard cardObj = cardObjMap.get(card);
            card.bidCard();
            cardObj.setIlluminate(true);
        }
        putAllCards();
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
        int numLead = leadSet == null ? 1 : leadSet.size();

        final HashSet<Card> feasibleCards = getNoSelectFeasible();

        if (numLead >= feasibleCards.size()) {
            putAllCards();
            for (final Card card : feasibleCards) {
                drawCard(card);
            }
        }
    }

    public void performDefaultReaction() {
        if (buttonMode == ButtonMode.NONE) {
            System.err.println("Warning: enforced pressing button at NONE mode");
            return;
        }

        enableMouseControl(false);
        putAllCards();
        if (buttonMode == ButtonMode.TRADE) {
            final ArrayList<Card> cardsCopy = new ArrayList<>();
            cardsCopy.addAll(cards);
            Collections.shuffle(cardsCopy);
            for (int i = 0; i < nTrade; i++) {
                drawCard(cardsCopy.get(i));
            }
        } else if (buttonMode == ButtonMode.PLAY) {
            final int numPlay = leadSet == null ? 1 : leadSet.size();

            putAllCards();
            while (selectedCards.size() < numPlay) {
                final Card[] feasibles = getExclFeasible().toArray(new Card[0]);
                drawCard(feasibles[(new Random()).nextInt(feasibles.length)]);
            }
            executeMidButtonPressed();
            enableMouseControl(true);
            return;
        }
        executeMidButtonPressed();
    }

    public void clickMidButton() {
        if (mouseControl && buttonMode != ButtonMode.READY && midButton.isEnabled() && midButton.isVisible()) {
            midButton.doClick();
        }
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    private boolean executeMidButtonPressed() {
        int errCode = MyText.NORMAL;
        switch (buttonMode) {
            case PLAY:
                if ((errCode = checkPlayRule()) == MyText.NORMAL) {
                    disableMidButton();
                    removeSelected();
                    setMaskMode("ALL");
                    controller.sendToServer("PLAY", Card.concatCards(ClientController.SEND_DELIM, selectedCards));
                    selectedCards.clear();
                }
                break;
            case READY:
                controller.sendToServer("READY");
                break;
            case TRADE:
                if ((errCode = checkTradeRule()) == MyText.NORMAL) {
                    disableMidButton();
                    removeSelected();
                    enableMouseControl(false);
                    controller.sendToServer("TRADE", Card.concatCards(ClientController.SEND_DELIM, selectedCards));
                    selectedCards.clear();
                }
                break;
            case SHOW:
                if (selectedCards.isEmpty() || (errCode = checkShowRule()) == MyText.NORMAL) {
                    disableMidButton();
                    if (selectedCards.isEmpty())
                        controller.sendToServer("SHOW");
                    else {
                        controller.sendToServer("SHOW", Card.concatCards(ClientController.SEND_DELIM, selectedCards));
                        view.setPlayErrMsg(MyText.NORMAL);
                    }
                    enableMouseControl(false);
                }
                break;
            default:
                System.err.println("Warning: pressing button at NONE mode");
                errCode = -1;
        }
        view.setPlayErrMsg(errCode);
        if (errCode > 0)
            putAllCards();

        return errCode == MyText.NORMAL;
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
            view.acquireActionLock();
            if (executeMidButtonPressed())
                view.setClockEnforcer(false);

            view.releaseActionLock();
        }
    }
}