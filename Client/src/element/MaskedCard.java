package element;

import ui.*;
import rule.Card;

import java.awt.*;
import javax.swing.*;

/**
 * {@code MaskedCard} objects are the basic units of visualized cards. A mask is
 * put on top of a card to either blacken it, or illuminate it.
 * 
 * @author Weizhao Tang
 */
public class MaskedCard extends JPanel {
    static final long serialVersionUID = 1L;

    /** flag showing if card is blackened */
    private boolean blackened = false;
    /** flag showing if card is illuminated */
    private boolean illuminated = false;

    /** card image */
    private final JLabel card;
    /** colored mask */
    private final JLabel mask;

    /** fixed card width */
    public static final int w_ = 95;
    /** fixed card height */
    public static final int h_ = 130;

    /** color of blackening mask */
    private static final Color dark = new Color(0, 0, 0, 75);
    private static final Color dummy = MyColors.yellow;
    /** color of highlighting mask */
    private static final Color highlight = new Color(dummy.getRed(), dummy.getGreen(), dummy.getBlue(), 65);

    /**
     * Instantiate the {@code MaskedCard} class.
     * 
     * @param alias Full alias of the card to generate
     */
    public MaskedCard(final String alias) {
        setLayout(null);
        setName(alias);
        setOpaque(false);
        setPreferredSize(new Dimension(w_, h_));

        card = Card.createCard(alias);
        card.setOpaque(false);
        card.setBounds(0, 0, w_, h_);

        mask = new JLabel();
        mask.setOpaque(true);
        mask.setBackground(dark);
        mask.setBounds(0, 0, w_, h_);
        mask.setVisible(false);

        add(card);
        add(mask, 0);

        // Illuminate card with doubled effect, whose full alias ends with 'x'
        if (alias.length() == 3 && alias.charAt(2) == 'x')
            setIlluminate(true);

        revalidate();
        repaint();
    }

    /**
     * Set the card lightened or darkened.
     * 
     * @param enable {@code true} to lighten the card; {@code false} to darken it.
     */
    public void setLightened(boolean enable) {
        if (blackened = !enable) {
            mask.setVisible(true);
            mask.setBackground(dark);
        } else if (illuminated) {
            mask.setVisible(true);
            mask.setBackground(highlight);
        } else {
            mask.setVisible(false);
        }
    }

    /**
     * Illuminate the card and update the changes when not darkened.
     * 
     * @param illuminate {@code true} to illuminate the card; {@code false} to
     *                   disilluminate it
     */
    public void setIlluminate(final boolean illuminate) {
        illuminated = illuminate;
        if (!blackened) {
            setLightened(true);
        }
    }
}