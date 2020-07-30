package element;

import java.awt.*;
import javax.swing.*;

/**
 * The {@code MaskCircle} class describles a black mask in shape of circle.
 * 
 * @author Weizhao Tang
 */
public class MaskCircle extends JPanel {
    static final long serialVersionUID = 1L;

    /** opaqueness of circle */
    private static final int alpha = 100;

    /** width of circle */
    private int w_;
    /** height of circle */
    private int h_;

    /** original full-opaque color of mask */
    private final Color maskOpaque = Color.BLACK;
    /** semi-translucent color of mask */
    private Color darkMask = new Color(maskOpaque.getRed(), maskOpaque.getGreen(), maskOpaque.getBlue(), alpha);

    /**
     * Instantiate a {@code MaskCircle} object given its size.
     * 
     * @param w width of the circle (oval)
     * @param h height of the circle (oval)
     */
    public MaskCircle(final int w, final int h) {
        super();
        setOpaque(false);
        w_ = w;
        h_ = h;
    }

    /**
     * Repaint immediately after setting the dimensions of self.
     * 
     * @param x x-coordinate
     * @param y y-coordinate
     * @param w new width
     * @param h new height
     */
    public void setBoundsAndRepaint(final int x, final int y, final int w, final int h) {
        setBounds(x, y, w_ = w, h_ = h);
        repaint();
    }

    /**
     * Paint a semi-translucent oval.
     */
    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        g.setColor(darkMask);
        g.fillOval(0, 0, w_, h_);
    }
}
