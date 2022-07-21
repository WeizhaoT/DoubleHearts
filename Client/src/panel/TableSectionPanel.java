package panel;

import ui.*;
import rule.Card;
import element.MaskedCard;
import layout.PokerTableLayout;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TableSectionPanel extends JPanel {
    static final long serialVersionUID = 1L;

    private static final Position[] POSITIONS = new Position[] { Position.BOTTOM, Position.RIGHT, Position.TOP,
            Position.LEFT };

    private enum Position {
        BOTTOM, RIGHT, TOP, LEFT;
    }

    private enum Status {
        IDLE, CARD, READY, WAIT, PASS;
    }

    private final Position position;
    private Status status;

    private boolean historyShown;
    private boolean textCovered;
    private final HashSet<Component> history;
    private final HashSet<Component> present;
    private javax.swing.Timer historyTimer;

    private final JLabel textLabel;

    private static final int w_ = PokerTableLayout.secWidth;
    private static final int h_ = PokerTableLayout.secHeight;
    private static final int cardGap = HandPanel.cardGap;
    private static final int maxRoundw = MaskedCard.w_ + 25 * cardGap;
    private static final int tableSecPad = (w_ - maxRoundw) / 2;
    private static final int tableSecTop = (h_ - MaskedCard.h_) / 2;

    private static final int textw = 300;
    private static final int texth = 40;

    public TableSectionPanel(final int i) {
        this(POSITIONS[i].toString());
    }

    public TableSectionPanel(final String position_) {
        position = Position.valueOf(position_);
        setLayout(null);
        setOpaque(false);
        setPreferredSize(new Dimension(w_, h_));

        textLabel = new JLabel();
        textLabel.setForeground(MyColors.yellow);
        textLabel.setFont(MyText.getSecTextFont());

        int x, y, alignment;
        if (position == Position.LEFT) {
            x = tableSecPad;
            y = (h_ - texth) / 2;
            alignment = SwingConstants.LEFT;
        } else if (position == Position.RIGHT) {
            x = w_ - textw - tableSecPad;
            y = (h_ - texth) / 2;
            alignment = SwingConstants.RIGHT;
        } else if (position == Position.TOP) {
            x = (w_ - textw) / 2;
            y = tableSecPad;
            alignment = SwingConstants.CENTER;
        } else {
            x = (w_ - textw) / 2;
            y = h_ - texth - tableSecPad;
            alignment = SwingConstants.CENTER;
        }

        textLabel.setHorizontalAlignment(alignment);
        textLabel.setBounds(x, y, textw, texth);
        textLabel.setVisible(false);
        textLabel.setName("textLabel");
        add(textLabel);

        history = new HashSet<>();
        present = new HashSet<>();
        historyShown = false;
        textCovered = false;

        status = Status.IDLE;
        showChanges();
    }

    public void saveHistory() {
        hideHistory();

        for (final Component comp : history) {
            remove(comp);
        }

        history.clear();
        for (final Component comp : getComponents()) {
            if (comp.getName().substring(0, 5).equals("card_")) {
                history.add(comp);
                comp.setVisible(false);
            }
        }
        textLabel.setVisible(false);
        status = Status.IDLE;
        showChanges();
    }

    public void showHistory() {
        present.clear();
        for (final Component comp : getComponents()) {
            if (!history.contains(comp)) {
                present.add(comp);
                comp.setVisible(false);
            } else {
                comp.setVisible(true);
            }
        }
        historyShown = true;
        if (historyTimer != null)
            historyTimer.stop();

        showChanges();
        historyTimer = new javax.swing.Timer(1500, new ActionListener() {
            public void actionPerformed(final ActionEvent evt) {
                hideHistory();
            }
        });
        historyTimer.setRepeats(false);
        historyTimer.start();
    }

    public void hideHistory() {
        if (!historyShown)
            return;

        historyTimer.stop();
        for (final Component comp : getComponents()) {
            if (history.contains(comp)) {
                comp.setVisible(false);
            } else {
                if (!textCovered ^ comp.getName().substring(0, 5).equals("card_"))
                    comp.setVisible(true);
            }
        }
        historyShown = false;
        showChanges();
    }

    public void showCards(final String[] aliases) {
        status = Status.CARD;

        textLabel.setVisible(false);
        if (aliases != null && aliases.length > 0) {
            final int numCards = aliases.length;
            int x;

            if (position == Position.LEFT)
                x = tableSecPad;
            else if (position == Position.RIGHT)
                x = w_ - tableSecPad - MaskedCard.w_ - cardGap * (numCards - 1);
            else
                x = (w_ - MaskedCard.w_ - cardGap * (numCards - 1)) / 2;

            final ArrayList<Card> cardList = new ArrayList<>();
            final HashMap<Card, MaskedCard> cardMap = new HashMap<>();

            for (int i = 0; i < aliases.length; i++) {
                final Card card = new Card(aliases[i]);
                final MaskedCard cardObj = new MaskedCard(aliases[i]);
                cardObj.setVisible(!historyShown);
                cardMap.put(card, cardObj);
                add(cardObj);
                cardList.add(card);
            }

            cardList.sort(new Card.CardComparator());

            for (int i = 0; i < aliases.length; i++) {
                final MaskedCard cardObj = cardMap.get(cardList.get(aliases.length - i - 1));
                setComponentZOrder(cardObj, aliases.length - i - 1);
                cardObj.setBounds(x, tableSecTop, MaskedCard.w_, MaskedCard.h_);
                x += cardGap;
            }
        }
        textCovered = true;
        showChanges();
    }

    public synchronized void applyExposure(String[] exposed) {
        MaskedCard.upgradeEffects(exposed, null, Stream.of(getComponents())
                .filter(c -> c.getClass() == MaskedCard.class).map(c -> (MaskedCard) c).collect(Collectors.toSet()));
        showChanges();
    }

    private void showText(final String text) {
        textLabel.setText("<html><b>" + text + "</b></html>");
        textLabel.setVisible(!historyShown);
        textCovered = false;
        showChanges();
    }

    public void showReady() {
        status = Status.READY;
        showText(MyText.getReadyText());
    }

    public void showPass() {
        status = Status.PASS;
        showText(MyText.getPassText());
    }

    public boolean isIdle() {
        return status == Status.IDLE;
    }

    public void clear() {
        history.clear();
        present.clear();
        status = Status.IDLE;
        textLabel.setVisible(false);

        for (final Component comp : getComponents()) {
            if (!comp.getName().equals("textLabel"))
                remove(comp);
        }

        showChanges();
    }

    private void showChanges() {
        revalidate();
        repaint();
        setVisible(true);
    }

}