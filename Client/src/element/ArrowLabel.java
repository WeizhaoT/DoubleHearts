package element;

import java.io.IOException;

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
        super("Arrows/" + gap + sender + "a.png");
        setOpaque(false);
        setName(String.valueOf(gap) + sender + "arrow");

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

        try {
            final String side = inactive ? "a" : "b";
            icon = new ImageIcon(ImageIO.read(Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("Arrows/" + tradeGap + senderIndex + side + ".png")));
            this.undecided = inactive;
            setIcon(icon);
            showChanges();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}