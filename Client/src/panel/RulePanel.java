package panel;

import ui.*;
import rule.Card;
import element.BackgroundRect;
import layout.PokerGameLayout;
import main.ClientController;

import java.awt.*;
import java.util.*;

import javax.swing.*;

public class RulePanel extends JPanel {
    static final long serialVersionUID = 1L;

    private static final int w_ = PokerGameLayout.rulew;
    private static final int h_ = PokerGameLayout.ruleh;

    private static final int insetScale = 5;
    private static final int texth = (h_ - 2 * insetScale) / 6;
    private static final int textlw = w_ / 2 - insetScale + 6;
    private static final int textrw = w_ / 2 - insetScale - 6;
    private static final int textwOverflow = 10;
    private static final int textGap = 7;
    private static final int splitterYOffset = 2;

    private static final String oldEffPrefix = "<font color=\"rgb((145, 135, 145))\"><strike>";
    private static final String oldEffPostFix = "</strike></font> ";

    private static final Card trans = new Card(Card.TRANS);
    private static final Card sheep = new Card(Card.SHEEP);
    private static final Card pig = new Card(Card.PIG);
    private static final HashMap<String, Card> allHearts = new HashMap<>(Map.of("2~4", new Card("2H"), "5~T",
            new Card("5H"), "J", new Card("JH"), "Q", new Card("QH"), "K", new Card("KH"), "A", new Card(Card.ACEH)));

    private final BackgroundRect background;

    private final JLabel dTransLabel = new JLabel();
    private final JLabel transLabel = new JLabel();
    private final JLabel pigLabel = new JLabel();
    private final JLabel sheepLabel = new JLabel();
    private final JLabel jHeartsLabel = new JLabel();
    private final JLabel qHeartsLabel = new JLabel();
    private final JLabel kHeartsLabel = new JLabel();
    private final JLabel aHeartsLabel = new JLabel();
    private final JLabel heartsLabel1 = new JLabel();
    private final JLabel heartsLabel2 = new JLabel();

    public RulePanel() {
        setLayout(null);
        setOpaque(false);
        setPreferredSize(new Dimension(w_, h_));

        if (ClientController.TEST_MODE >= 2)
            setBorder(BorderFactory.createLineBorder(MyColors.green));

        background = new BackgroundRect(w_, h_, MyColors.lightYellow);
        background.setBounds(0, 0, w_, h_);
        add(background);

        JLabel title = new JLabel(MyText.getRuleTitle(), SwingConstants.CENTER);
        title.setForeground(MyColors.tableGreen);
        title.setBounds(insetScale, 0, textlw + textrw, texth);
        title.setVerticalAlignment(JLabel.TOP);
        add(title);

        int y = texth;
        dTransLabel.setForeground(MyColors.clubColor);
        setLeftAxis(dTransLabel, y);
        add(dTransLabel);

        transLabel.setForeground(MyColors.clubColor);
        setRightAxis(transLabel, y);
        add(transLabel);

        y += texth;
        jHeartsLabel.setForeground(MyColors.heartColor);
        setRightAxis(jHeartsLabel, y);
        add(jHeartsLabel);

        sheepLabel.setForeground(MyColors.diamondColor);
        setLeftAxis(sheepLabel, y);
        add(sheepLabel);

        y += texth;
        qHeartsLabel.setForeground(MyColors.heartColor);
        setRightAxis(qHeartsLabel, y);
        add(qHeartsLabel);

        pigLabel.setForeground(MyColors.spadeColor);
        setLeftAxis(pigLabel, y);
        add(pigLabel);

        y += texth;
        kHeartsLabel.setForeground(MyColors.heartColor);
        setRightAxis(kHeartsLabel, y);
        add(kHeartsLabel);

        heartsLabel1.setForeground(MyColors.heartColor);
        setLeftAxis(heartsLabel1, y);
        add(heartsLabel1);

        y += texth;
        aHeartsLabel.setForeground(MyColors.heartColor);
        setRightAxis(aHeartsLabel, y);
        add(aHeartsLabel);

        heartsLabel2.setForeground(MyColors.heartColor);
        setLeftAxis(heartsLabel2, y);
        add(heartsLabel2);

        JLabel splitLabel = new JLabel();
        splitLabel.setBounds(insetScale + textlw - 1, texth + splitterYOffset, 2, h_ - texth - splitterYOffset);
        splitLabel.setBorder(BorderFactory.createLineBorder(MyColors.tableGreen, 5));
        add(splitLabel);

        splitLabel = new JLabel();
        splitLabel.setBounds(0, texth + splitterYOffset, w_, 2);
        splitLabel.setBorder(BorderFactory.createLineBorder(MyColors.tableGreen, 5));
        add(splitLabel);

        for (final Component comp : getComponents()) {
            comp.setFont(MyFont.rule);
        }

        title.setFont(MyText.getRuleTitleFont());

        setEffectText();

        setComponentZOrder(background, getComponentCount() - 1);
        revalidate();
        repaint();
        setVisible(true);
    }

    public void reset() {
        trans.resetGrade();
        sheep.resetGrade();
        pig.resetGrade();
        allHearts.values().stream().forEach(c -> c.resetGrade());
        setEffectText();
    }

    private void setEffectText() {
        dTransLabel.setText(String.format("\u2663TT%4s\u00d7", "" + Card.getMultStr(4, trans, trans)));
        transLabel.setText(String.format("\u2663T%4s\u00d7", "" + Card.getMultStr(4, trans)));
        sheepLabel.setText(String.format("\u2666J%6s", "" + sheep.value()));
        pigLabel.setText(String.format("\u2660Q%6d", pig.value()));
        heartsLabel1.setText(String.format("\u26652~4%4d", allHearts.get("2~4").value()));
        heartsLabel2.setText(String.format("\u26655~T%4d", allHearts.get("5~T").value()));
        jHeartsLabel.setText(String.format("\u2665J%5d", allHearts.get("J").value()));
        qHeartsLabel.setText(String.format("\u2665Q%5d", allHearts.get("Q").value()));
        kHeartsLabel.setText(String.format("\u2665K%5d", allHearts.get("K").value()));
        aHeartsLabel.setText(String.format("\u2665A%5d", allHearts.get("A").value()));
    }

    public void updateEffects(String[] exposed) {
        for (String alias : exposed) {
            if (alias.startsWith(Card.TRANS)) {
                trans.upgrade(1);
            } else if (alias.startsWith(Card.SHEEP)) {
                sheep.upgrade(1);
            } else if (alias.startsWith(Card.PIG)) {
                pig.upgrade(1);
            } else if (alias.startsWith(Card.ACEH)) {
                allHearts.values().stream().forEach(c -> c.upgrade(1));
            }
        }
        setEffectText();
    }

    public static String getEffect(final String exposable) {
        if (Card.TRANS.equals(exposable)) {
            if (trans.fullyUpgraded())
                return "";

            Card newTrans = new Card(trans.fullAlias());
            newTrans.upgrade(1);
            return MyColors.getColoredText("\u2663T(" + oldEffPrefix + Card.getMult(trans) + "\u00d7" + oldEffPostFix
                    + Card.getMult(newTrans) + "\u00d7)", MyColors.clubColor);
        } else if (Card.SHEEP.equals(exposable)) {
            if (sheep.fullyUpgraded())
                return "";

            Card newSheep = new Card(sheep.fullAlias());
            newSheep.upgrade(1);
            return MyColors.getColoredText(
                    "\u2666J(" + oldEffPrefix + sheep.value() + oldEffPostFix + newSheep.value() + ")",
                    MyColors.diamondColor);
        } else if (Card.PIG.equals(exposable)) {
            if (pig.fullyUpgraded())
                return "";

            Card newPig = new Card(pig.fullAlias());
            newPig.upgrade(1);
            return MyColors.getColoredText(
                    "\u2660Q(" + oldEffPrefix + pig.value() + oldEffPostFix + newPig.value() + ")",
                    MyColors.spadeColor);
        } else if (Card.ACEH.equals(exposable)) {
            if (allHearts.get("A").fullyUpgraded())
                return "";

            int heartsMult = 1 << allHearts.get("A").exposed;
            return MyColors.getColoredText("\u2665A(" + oldEffPrefix + "\u2665\u00d7" + heartsMult + oldEffPostFix
                    + "\u2665\u00d7" + (heartsMult << 1) + ")", MyColors.heartColor);
        } else
            throw new IllegalArgumentException("Error: getEffect takes illegal argument \"" + exposable + "\"");
    }

    private void setLeftAxis(final JLabel item, int y) {
        item.setBounds(insetScale, y + insetScale, textlw + textwOverflow, texth);
    }

    private void setRightAxis(final JLabel item, int y) {
        item.setBounds(insetScale + textlw + textGap, y + insetScale, textrw - textGap + textwOverflow, texth);
    }
}