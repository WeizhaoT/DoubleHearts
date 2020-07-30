package element;

import java.awt.*;
import javax.swing.*;

/**
 * The {@code MaskedAvatar} class describes a avatar with adjustable mask.
 * 
 * @author Weizhao Tang
 */
public class MaskedAvatar extends JPanel {
    static final long serialVersionUID = 1L;

    /** avatar */
    private ImageLabel avt;
    /** dark mask that makes the avatar less concentrating */
    private MaskCircle mask;

    /**
     * Instantiate a {@code MaskedAvatar} object given the avatar index and the
     * scale.
     * 
     * @param index      Index of avatar
     * @param imageScale Size (both width and height) of the avatar
     */
    public MaskedAvatar(int index, int imageScale) {
        setLayout(null);
        setOpaque(false);
        setName(String.valueOf(index));

        avt = ImageLabel.createAvatar(index, imageScale);
        avt.setBounds(0, 0, imageScale, imageScale);
        mask = new MaskCircle(imageScale, imageScale);
        mask.setBoundsAndRepaint(0, 0, imageScale, imageScale);
        mask.setVisible(false);
        setPreferredSize(avt.getPreferredSize());
        add(avt);
        add(mask, 0); // put mask on top of the avatar
        showChanges();
    }

    /**
     * Enable or disable the mask.
     * 
     * @param b {@code true} to enable the mask, {@code false} to disable it.
     */
    public void enableMask(boolean b) {
        mask.setVisible(b);
        showChanges();
    }

    /**
     * Rescale the avatar and the mask.
     * 
     * @param scale New size of the object
     */
    public void rescale(int scale) {
        avt.rescale(scale);
        avt.setBounds(0, 0, scale, scale);
        mask.setBoundsAndRepaint(0, 0, scale, scale);
        setPreferredSize(new Dimension(scale, scale));
    }

    /**
     * Refresh the whole object.
     */
    private void showChanges() {
        revalidate();
        repaint();
        setVisible(true);
    }
}
