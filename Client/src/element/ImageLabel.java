package element;

import ui.*;

import java.io.IOException;
import java.util.HashMap;
import java.awt.*;
import javax.imageio.ImageIO;
import javax.swing.*;

import layout.WelcomeLayout;

/**
 * The {@code ImageLabel} class loads full-sized images and enables flexible
 * rescaling with minimal quality loss.
 */
public class ImageLabel extends JLabel {
    static final long serialVersionUID = 1L;

    private static final HashMap<Integer, ImageIcon> avatarIcons = new HashMap<>();
    private static ImageIcon tableIcon;
    private static ImageIcon indicatorIcon;

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
    public ImageLabel(final ImageIcon icon) {
        this();
        setIcon(this.icon = icon);
        setOpaque(false);
        cleared = false;
        setSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
    }

    /**
     * Instantiate an {@code ImageLabel} object, load an image and rescale it to the
     * given size.
     * 
     * @param filename   Image name
     * @param imageScale Wanted scale of image (both width and height)
     */
    public ImageLabel(final ImageIcon icon, final int imageScale) {
        this(icon);
        rescale(imageScale);
        setPreferredSize(new Dimension(imageScale, imageScale));
    }

    public ImageLabel(final int avatarIndex, final int imageScale) {
        this(avatarIcons.get(avatarIndex), imageScale);
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

    public static void loadAllAvatars() {
        ImageIcon avatarIcon;
        for (int i = 0; i < WelcomeLayout.numAvt; i++) {
            try {
                avatarIcon = new ImageIcon(ImageIO.read(Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream(String.format("Avatars/avatar%02d.png", i))));
                avatarIcons.put(i, avatarIcon);
            } catch (final IOException e) {
                System.err.println("Error: failed to load avatar No." + i);
                System.exit(1);
            }
        }
    }

    public static void loadMiscIcons() {
        try {
            tableIcon = new ImageIcon(
                    ImageIO.read(Thread.currentThread().getContextClassLoader().getResourceAsStream("card_table.png")));
        } catch (final IOException e) {
            System.err.println("Error: failed to load card table");
            System.exit(1);
        }
        try {
            indicatorIcon = new ImageIcon(ImageIO
                    .read(Thread.currentThread().getContextClassLoader().getResourceAsStream("Avatars/indicator.png")));
        } catch (final IOException e) {
            System.err.println("Error: failed to load avatar indicator");
            System.exit(1);
        }
    }

    /**
     * Create an avatar and set its name.
     * 
     * @param index       Index of avatar
     * @param avatarScale Size (both width and height) of avatar
     * @return An {@link ImageLabel} instance of the avatar
     */
    public static ImageLabel createAvatar(final int index, final int avatarScale) {
        final ImageLabel label = new ImageLabel(index, avatarScale);
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
        final ImageLabel indicator = new ImageLabel(indicatorIcon);
        if (indicator != null)
            indicator.setName("on00");

        return indicator;
    }

    /**
     * Load card table image.
     * 
     * @return An {@link ImageLabel} instance of the card table
     */
    public static ImageLabel getTable() {
        return new ImageLabel(tableIcon);
    }
}