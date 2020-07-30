package element;

import ui.*;

import java.io.IOException;
import java.awt.*;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * The {@code ImageLabel} class loads full-sized images and enables flexible
 * rescaling with minimal quality loss.
 */
public class ImageLabel extends JLabel {
    static final long serialVersionUID = 1L;

    /** flag indicating if the image is cleared */
    private boolean cleared = true;
    /** {@link ImageIcon} object storing full-sized image */
    protected ImageIcon icon;
    /** Default size of the object */
    public static final Dimension defaultSize = new Dimension(100, 100);

    /**
     * Instantiate an {@code ImageLabel} object without loading image and showing
     * background color.
     */
    public ImageLabel() {
        super();
        setOpaque(true);
        setSize(defaultSize);
        setBackground(MyColors.gray);
    }

    /**
     * Instantiate an {@code ImageLabel} object, load an image and scale to its
     * original size.
     * 
     * @param filename Image name
     */
    public ImageLabel(final String filename) {
        this();
        try {
            setIcon(icon = new ImageIcon(
                    ImageIO.read(Thread.currentThread().getContextClassLoader().getResourceAsStream(filename))));
            setOpaque(false);
            cleared = false;
            setName(filename);
            setSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Instantiate an {@code ImageLabel} object, load an image and rescale it to the
     * given size.
     * 
     * @param filename   Image name
     * @param imageScale Wanted scale of image (both width and height)
     */
    public ImageLabel(final String filename, final int imageScale) {
        this(filename);
        rescale(imageScale);
        setPreferredSize(new Dimension(imageScale, imageScale));
    }

    /**
     * Rescale imgae to a square of given scale.
     * 
     * @param scale Scale of square
     */
    public void rescale(final int scale) {
        rescale(scale, scale);
    }

    /**
     * Rescale imgae to a rectangle of given scale.
     * 
     * @param w width of object
     * @param h height of object
     */
    public void rescale(final int w, final int h) {
        if (icon != null && !cleared) {
            setIcon(new ImageIcon(icon.getImage().getScaledInstance(w, h, 0)));
        }
        setSize(new Dimension(w, h));
    }

    /**
     * Clear the image and show background color.
     */
    public void setEmptyAvatar() {
        cleared = true;
        setIcon(null);
        setOpaque(true);
    }

    /**
     * Clear the image, show background color, and rescale to given size.
     * 
     * @param avatarScale Given size to rescale
     */
    public void setEmptyAvatar(final int avatarScale) {
        setEmptyAvatar();
        rescale(avatarScale, avatarScale);
    }

    /**
     * Set image to an avatar given its index and preserve current size.
     * 
     * @param index Index of avatar
     */
    public void setAvatar(final int index) {
        setAvatar(index, getWidth());
    }

    /**
     * Set image to an avatar given its index and desired size.
     * 
     * @param index       Index of avatar
     * @param avatarScale Scale (both width and height) of avatar
     */
    public void setAvatar(final int index, final int avatarScale) {
        try {
            icon = new ImageIcon(ImageIO.read(Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(String.format("Avatars/avatar%02d.png", index))));
            cleared = false;
            setOpaque(false);
            rescale(avatarScale);
            setName(String.valueOf(index));
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Refresh the elements.
     */
    protected void showChanges() {
        revalidate();
        repaint();
        setVisible(true);
    }

    /**
     * Create an avatar and set its name.
     * 
     * @param index       Index of avatar
     * @param avatarScale Size (both width and height) of avatar
     * @return An {@link ImageLabel} instance of the avatar
     */
    public static ImageLabel createAvatar(final int index, final int avatarScale) {
        final ImageLabel label = new ImageLabel(String.format("Avatars/avatar%02d.png", index), avatarScale);
        if (label != null)
            label.setName(String.valueOf(index));

        return label;
    }

    /**
     * Load indicator image.
     * 
     * @return An {@link ImageLabel} instance of the avatar indicator
     */
    public static ImageLabel getIndicator() {
        final ImageLabel indicator = new ImageLabel("Avatars/indicator.png");
        if (indicator != null)
            indicator.setName("on10");

        return indicator;
    }

    /**
     * Load card table image.
     * 
     * @return An {@link ImageLabel} instance of the card table
     */
    public static ImageLabel getTable() {
        return new ImageLabel("card_table.png");
    }
}