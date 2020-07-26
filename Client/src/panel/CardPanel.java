package panel;

import ui.*;
import rule.Card;

import java.awt.*;
import javax.swing.*;

public class CardPanel extends JPanel {
    static final long serialVersionUID = 1L;

    private boolean illuminated = false;

    private final JLabel card;
    private final JLabel mask;

    public static final int w_ = 95;
    public static final int h_ = 130;

    private static final Color disabled = new Color(0, 0, 0, 75);
    private static final Color dummy = MyColors.yellow;
    private static final Color doubled = new Color(dummy.getRed(), dummy.getGreen(), dummy.getBlue(), 65);

    public CardPanel(final String alias) {
        setLayout(null);
        setName(alias);
        setOpaque(false);
        setPreferredSize(new Dimension(w_, h_));

        card = Card.createCard(alias);
        card.setOpaque(false);
        card.setBounds(0, 0, w_, h_);

        mask = new JLabel();
        mask.setOpaque(true);
        mask.setBackground(disabled);
        mask.setBounds(0, 0, w_, h_);
        mask.setVisible(false);

        add(card);
        add(mask, 0);

        if (alias.length() == 3 && alias.charAt(2) == 'x')
            setIlluminate(true);
    }

    public void maskOn() {
        mask.setVisible(true);
        mask.setOpaque(true);
        mask.setBackground(disabled);
        showChange();
    }

    public void maskOff() {
        if (illuminated) {
            mask.setVisible(true);
            mask.setBackground(doubled);
        } else {
            mask.setVisible(false);
            mask.setOpaque(false);
        }
        showChange();
    }

    public boolean maskIsOn() {
        return mask.isVisible() && mask.getBackground().equals(disabled);
    }

    public void setIlluminate(final boolean illuminate) {
        illuminated = illuminate;
        if (maskIsOn())
            return;

        maskOff();
    }

    public void showChange() {
        revalidate();
        repaint();
        setVisible(true);
    }
}