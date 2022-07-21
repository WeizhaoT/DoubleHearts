package element;

// import ui.*;
import rule.Card;

import java.awt.*;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * {@code MaskedCard} objects are the basic units of visualized cards. A mask is
 * put on top of a card to either blacken it, or illuminate it.
 * 
 * @author Weizhao Tang
 */
public class MaskedCard extends JPanel {
    static final long serialVersionUID = 1L;

    private static final HashMap<String, ImageIcon> cardImages = new HashMap<>();
    private static final HashMap<String, ImageIcon> levelIcons = new HashMap<>();

    private static final String prefix = "card_";
    private static final int prefixLen = prefix.length();

    /** flag showing if card is blackened */
    private boolean blackened = false;

    /** flag showing if card is illuminated */
    private String levelID = "";
    private int effectLevel = 0;

    /** card image */
    private final JLabel cardLabel;
    private final JLabel effectLabel;
    /** colored mask */
    private final JLabel mask;

    /** fixed card width */
    public static final int w_ = 95;
    /** fixed card height */
    public static final int h_ = 130;

    private static final Rectangle effectIconBounds = new Rectangle(4, 90, 18, 34);

    /** color of blackening mask */
    private static final Color dark = new Color(0, 0, 0, 75);
    // private static final Color dummy = MyColors.yellow;
    /** color of highlighting mask */
    // private static final Color highlight = new Color(dummy.getRed(),
    // dummy.getGreen(), dummy.getBlue(), 65);

    /**
     * Instantiate the {@code MaskedCard} class.
     * 
     * @param fullAlias Full alias of the card to generate
     */
    public MaskedCard(final String fullAlias) {
        setLayout(null);
        setName(prefix + fullAlias);
        setOpaque(false);
        setPreferredSize(new Dimension(w_, h_));

        Card card = new Card(fullAlias);

        cardLabel = new JLabel();
        cardLabel.setIcon(cardImages.get(card.alias()));
        cardLabel.setOpaque(false);
        cardLabel.setBounds(0, 0, w_, h_);

        effectLabel = new JLabel();
        effectLabel.setOpaque(false);
        effectLabel.setBounds(effectIconBounds);

        mask = new JLabel();
        mask.setOpaque(true);
        mask.setBackground(dark);
        mask.setBounds(0, 0, w_, h_);
        mask.setVisible(false);

        add(cardLabel);
        add(effectLabel);
        add(mask);

        levelID = fullAlias.substring(1);
        // Illuminate card with doubled effect, whose full alias ends with 'x'
        if (fullAlias.length() == 3)
            setEffectLevel(effectLevel = (fullAlias.charAt(2) == 'x' ? 1 : (fullAlias.charAt(2) == 'z' ? 2 : 0)));

        if (card.isScored())
            effectLabel.setIcon(levelIcons.get(levelID));

        setComponentZOrder(cardLabel, 2);
        setComponentZOrder(mask, 0);

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
            // } else if (effectLevel > 0) {
            // mask.setVisible(true);
            // mask.setBackground(highlight);
        } else {
            mask.setVisible(false);
        }
    }

    private String exposerAlias() {
        Card card = new Card(getName().substring(prefixLen, prefixLen + 2));
        return card.exposerAlias();
    }

    /**
     * Illuminate the card and update the changes when not darkened.
     * 
     * @param effect {@code true} to illuminate the card; {@code false} to
     *               disilluminate it
     */
    private void setEffectLevel(final int effect) {
        effectLevel = effect;
        effectLabel.setIcon(
                levelIcons.get(levelID = levelID.substring(0, 1) + (effect == 0 ? "" : effect == 1 ? "x" : "z")));
        setName(getName().substring(0, 6) + levelID);
        if (!blackened) {
            setLightened(true);
        }
    }

    public synchronized void upgrade(int numLevels) {
        setEffectLevel(effectLevel += numLevels);
    }

    public static void upgradeEffects(String[] exposed, Collection<Card> cards, Collection<MaskedCard> maskedCards) {
        HashMap<String, Integer> exposedHist = new HashMap<>();
        for (String alias : exposed) {
            exposedHist.compute(alias.substring(0, 2), (k, v) -> v == null ? 1 : v + 1);
        }

        if (cards != null) {
            cards.stream().filter(c -> exposedHist.containsKey(c.exposerAlias()))
                    .forEach(c -> c.upgrade(exposedHist.get(c.exposerAlias())));
        }
        if (maskedCards != null) {
            maskedCards.stream().filter(c -> exposedHist.containsKey(c.exposerAlias()))
                    .forEach(c -> c.upgrade(exposedHist.get(c.exposerAlias())));
        }
    }

    public static void loadCardImages() {
        ImageIcon cardIcon = null; // label containing image of card

        for (Card.Rank rank : Card.Rank.values()) {
            for (Card.Suit suit : Card.Suit.values()) {
                String alias = rank.alias() + suit.alias();
                try {
                    cardIcon = new ImageIcon(ImageIO.read(Thread.currentThread().getContextClassLoader()
                            .getResourceAsStream("CardImages/" + alias + ".png")));
                    cardImages.put(alias, cardIcon);
                } catch (final IOException | IllegalArgumentException e) {
                    System.err.println("Error: failed to load card " + alias);
                    System.exit(1);
                }
            }
        }
    }

    public static void loadLevelIcons() {
        ImageIcon levelIcon = null; // label containing image of card
        for (String level : new String[] { "", "x", "z" }) {
            for (Card.Suit suit : Card.Suit.values()) {
                String alias = suit.alias() + level;
                try {
                    levelIcon = new ImageIcon(ImageIO.read(Thread.currentThread().getContextClassLoader()
                            .getResourceAsStream("CardImages/Ranks/" + alias + ".png")));
                    levelIcons.put(alias, levelIcon);
                } catch (final IOException | IllegalArgumentException e) {
                    System.err.println("Error: failed to load resource image " + alias);
                    System.exit(1);
                }
            }
        }
    }
}