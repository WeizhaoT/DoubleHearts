package element;

import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;

public class ArrowLabel extends ImageLabel {
    static final long serialVersionUID = 1L;

    private final int frameIndex;
    private final int playerIndex;
    private boolean dim;

    public ArrowLabel(final int frame, final int player) {
        super("Arrows/" + frame + player + "a.png");
        setOpaque(false);
        setName(String.valueOf(frame) + player + "arrow");

        frameIndex = frame;
        playerIndex = player;
        dim = true;
    }

    public void flip(final boolean dim) {
        if (dim == this.dim)
            return;

        try {
            final String side = dim ? "a" : "b";
            icon = new ImageIcon(ImageIO.read(Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("Arrows/" + frameIndex + playerIndex + side + ".png")));
            this.dim = dim;
            setIcon(icon);
            showChange();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}