package element;

import ui.*;

import java.io.IOException;
import java.awt.*;
import javax.imageio.ImageIO;
import javax.swing.*;

public class ImageLabel extends JLabel {
    static final long serialVersionUID = 1L;

    private boolean cleared;
    protected ImageIcon icon;
    public static Dimension defaultSize = new Dimension(100, 100);

    public ImageLabel() {
        super();
        setOpaque(true);
        setBackground(MyColors.gray);
        cleared = true;
        setSize(defaultSize);
    }

    public ImageLabel(int w, int h) {
        this();
        setSize(new Dimension(w, h));
    }

    public ImageLabel(String filename) {
        this();
        try {
            icon = new ImageIcon(
                    ImageIO.read(Thread.currentThread().getContextClassLoader().getResourceAsStream(filename)));
            setIcon(icon);
        } catch (IOException e) {
            e.printStackTrace();
        }
        cleared = false;
        setName(filename);
        setSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
    }

    public ImageLabel(String filename, int imageScale) {
        this(filename);
        rescale(imageScale);
        setPreferredSize(new Dimension(imageScale, imageScale));
    }

    public void showChange() {
        revalidate();
        repaint();
        setVisible(true);
    }

    public void rescale(int scale) {
        rescale(scale, scale);
    }

    public void rescale(int w, int h) {
        if (icon != null && !cleared) {
            setIcon(new ImageIcon(icon.getImage().getScaledInstance(w, h, 0)));
        }
        setSize(new Dimension(w, h));
    }

    public void setEmptyAvatar() {
        cleared = true;
        setIcon(null);
        setOpaque(true);
    }

    public void setEmptyAvatar(int avatarScale) {
        setEmptyAvatar();
        rescale(avatarScale, avatarScale);
    }

    public void setAvatar(int index) {
        setAvatar(index, getWidth());
    }

    public void setAvatar(int index, int avatarScale) {
        try {
            icon = new ImageIcon(ImageIO.read(Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(String.format("Avatars/avatar%02d.jpg", index))));
            cleared = false;
            setOpaque(false);
            rescale(avatarScale);
            setName(String.valueOf(index));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ImageLabel createAvatar(int index, int avatarScale) {
        ImageLabel label = new ImageLabel(String.format("Avatars/avatar%02d.jpg", index), avatarScale);
        if (label != null)
            label.setName(String.valueOf(index));

        return label;
    }

    public static ImageLabel getIndicator() {
        ImageLabel indicator = new ImageLabel("Avatars/indicator.png");
        if (indicator != null)
            indicator.setName("on10");

        return indicator;
    }

    public static ImageLabel getTable() {
        return new ImageLabel("card_table.png");
    }
}