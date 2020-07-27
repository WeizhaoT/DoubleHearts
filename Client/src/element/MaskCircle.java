package element;

import java.awt.*;
import javax.swing.*;

public class MaskCircle extends JPanel {
    static final long serialVersionUID = 1L;

    private static final int alhpa = 100;

    private int w_;
    private int h_;

    private final Color maskOpaque = Color.BLACK;
    private Color maskBlack = new Color(maskOpaque.getRed(), maskOpaque.getGreen(), maskOpaque.getBlue(), alhpa);

    public MaskCircle(final int w, final int h) {
        super();
        setOpaque(false);
        w_ = w;
        h_ = h;
    }

    public void setBoundsAndRepaint(final int x, final int y, final int w, final int h) {
        setBounds(x, y, w_ = w, h_ = h);
        repaint();
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        g.setColor(maskBlack);
        g.fillOval(0, 0, w_, h_);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(w_, h_);
    }
}
