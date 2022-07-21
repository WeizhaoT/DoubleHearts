package element;

import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * The {@code ArrowLabel} class is for the indicator arrows between each pair of
 * sender and recipient during card trading.
 * 
 * @author Weizhao Tang
 */
public class ArrowLabel extends ImageLabel {
    static final long serialVersionUID = 1L;

    private static final HashMap<String, ImageIcon> arrowIcons = new HashMap<>();

    /** gap between sender and recipient in counter-clockwise direction */
    private final int tradeGap;
    /** relative position of sender */
    private final int senderIndex;
    /** flag indicating if the sender has decided which cards to trade out */
    private boolean undecided;

    /**
     * Instantiate the {@code ArrowLabel} class. Generate the corresponding arrow
     * given sender and distance to recipient.
     * 
     * @param gap    Gap between sender and recipient in counter-clockwise direction
     * @param sender Relative location of sender
     */
    public ArrowLabel(final int gap, final int sender) {
        super(arrowIcons.get(String.format("%d%da", gap, sender)));
        setOpaque(false);
        setName(String.format("arrow%d%d", gap, sender));

        tradeGap = gap;
        senderIndex = sender;
        undecided = true;
    }

    /**
     * Flip arrow color to show the sender has (not) decided.
     * 
     * @param inactive {@code true} if not decided yet; {@code false} if decided
     */
    public void flip(final boolean inactive) {
        if (inactive == this.undecided)
            return;

        final String iconName = String.format("%d%d%s", tradeGap, senderIndex, inactive ? "a" : "b");
        icon = arrowIcons.get(iconName);
        this.undecided = inactive;
        setIcon(icon);
        showChanges();
    }

    public static void loadAllArrows() {
        ImageIcon arrowIcon;
        for (int i = 1; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                for (String side : new String[] { "a", "b" }) {
                    String iconName = String.format("%d%d%s", i, j, side);
                    try {
                        arrowIcon = new ImageIcon(ImageIO.read(Thread.currentThread().getContextClassLoader()
                                .getResourceAsStream("Arrows/" + iconName + ".png")));
                        arrowIcons.put(iconName, arrowIcon);
                    } catch (final IOException | IllegalArgumentException e) {
                        System.err.println("Error: failed to load arrow " + iconName);
                        System.exit(1);
                    }
                }
            }
        }
    }
}