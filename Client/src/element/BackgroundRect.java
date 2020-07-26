package element;

import ui.MyColors;

import java.awt.*;
import javax.swing.*;

public class BackgroundRect extends JPanel {
    static final long serialVersionUID = 1L;

    private static final int alhpa = 210;

    private final int w_, h_;

    private final Color textBGOpaque = MyColors.gray;
    private Color textBG = new Color(textBGOpaque.getRed(), textBGOpaque.getGreen(), textBGOpaque.getBlue(), alhpa);

    public BackgroundRect(final int w, final int h) {
        super();
        setOpaque(false);
        w_ = w;
        h_ = h;
    }

    public BackgroundRect(final int w, final int h, Color color) {
        super();
        setOpaque(false);
        w_ = w;
        h_ = h;
        textBG = color;
        if (textBG.getAlpha() != alhpa)
            textBG = new Color(textBG.getRed(), textBG.getGreen(), textBG.getBlue(), alhpa);
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
