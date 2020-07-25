package element;

import ui.MyColors;

import java.awt.*;
import javax.swing.*;

public class BackgroundRect extends JPanel {
    static final long serialVersionUID = 1L;

    private final int w_, h_;

    private final Color textBGOpaque = MyColors.gray;
    private final Color textBG = new Color(textBGOpaque.getRed(), textBGOpaque.getGreen(), textBGOpaque.getBlue(), 210);

    public BackgroundRect(final int w, final int h) {
        super();
        setOpaque(false);
        w_ = w;
        h_ = h;
    }

    public void setBounds(final int x, final int y) {
        setBounds(x, y, w_, h_);
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        final int roundScale = (int) (Math.min(w_, h_) * .125f);
        g.setColor(textBG);
        g.fillRoundRect(0, 0, w_, h_, roundScale, roundScale);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(w_, h_);
    }
}
