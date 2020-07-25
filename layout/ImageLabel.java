import java.io.IOException;

import javax.imageio.ImageIO;
import java.awt.*;
import javax.swing.*;

public class ImageLabel extends JLabel {
    static final long serialVersionUID = 1L;

    private ImageIcon icon;

    public ImageLabel(String filename) {
        super();
        try {
            icon = new ImageIcon(
                    ImageIO.read(Thread.currentThread().getContextClassLoader().getResourceAsStream(filename)));
            setIcon(icon);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setName(filename);
        setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
        // System.out.println(getName() + " size: " + icon.getIconWidth() + ", " +
        // icon.getIconHeight());
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
        if (icon == null)
            return;

        // System.out.println(getName() + " size: " + icon.getIconWidth() + ", " +
        // icon.getIconHeight());

        setIcon(new ImageIcon(icon.getImage().getScaledInstance(w, h, 0)));
        setPreferredSize(new Dimension(w, h));
    }

    public void setAvatar(int index, int avatarScale) {
        try {
            icon = new ImageIcon(ImageIO.read(Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(String.format("Avatars/avatar%02d.jpg", index))));
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