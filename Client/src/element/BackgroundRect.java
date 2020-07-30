package element;

import ui.MyColors;

import java.awt.*;
import javax.swing.*;

/**
 * The {@code BackgroundRect} class draws a semi-translucent rounded rectangle
 * as background.
 * 
 * @author Weizhao Tang
 */
public class BackgroundRect extends JPanel {
    static final long serialVersionUID = 1L;

    /** opaque level of rectangle */
    private static final int alpha = 200;

    /** width of the rectangle */
    private final int w_;
    /** height of the rectangle */
    private final int h_;

    /** full-opaque color with no alpha dimension */
    private final Color textBGOpaque = MyColors.gray;
    /** transparent color */
    private Color textBG = new Color(textBGOpaque.getRed(), textBGOpaque.getGreen(), textBGOpaque.getBlue(), alpha);

    /**
     * Instantiate a {@code BackgroundRect} instance given width and height.
     * 
     * @param w width of rectangle
     * @param h height of rectangle
     */
    public BackgroundRect(final int w, final int h) {
        super();
        setOpaque(false);
        w_ = w;
        h_ = h;
    }

    /**
     * Instantiate a {@code BackgroundRect} instance given width, height and color.
     * 
     * @param w     width of rectangle
     * @param h     height of rectangle
     * @param color color of the rectangle
     */
    public BackgroundRect(final int w, final int h, Color color) {
        this(w, h);
        textBG = color;
        if (textBG.getAlpha() != alpha)
            textBG = new Color(textBG.getRed(), textBG.getGreen(), textBG.getBlue(), alpha);
    }

    /**
     * Set bounds of rectangle, omitting width and height.
     * 
     * @param x x-coordinate
     * @param y y-coordinate
     */
    public void setBounds(final int x, final int y) {
        setBounds(x, y, w_, h_);
    }

    /**
     * Paint a rounded rectangle upon instantiation.
     */
    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        final int roundScale = (int) (Math.min(w_, h_) * .08f);
        g.setColor(textBG);
        g.fillRoundRect(0, 0, w_, h_, roundScale, roundScale);
    }
}
