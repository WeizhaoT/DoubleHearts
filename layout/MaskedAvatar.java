import java.awt.*;
import javax.swing.*;

public class MaskedAvatar extends JPanel {
    static final long serialVersionUID = 1L;

    private ImageLabel avt;
    private JLabel mask;

    public MaskedAvatar(int i, int imageScale) {
        setLayout(null);

        avt = ImageLabel.createAvatar(i, imageScale);
        avt.setBounds(0, 0, imageScale, imageScale);
        mask = new JLabel();
        mask.setOpaque(true);
        mask.setBackground(new Color(0, 0, 0, 150));
        mask.setBounds(0, 0, imageScale, imageScale);
        setPreferredSize(avt.getPreferredSize());
        add(mask);
        add(avt, 0);
    }

    public void maskOn() {
        setComponentZOrder(mask, 0);
        showChange();
    }

    public void maskOff() {
        setComponentZOrder(avt, 0);
        showChange();
    }

    public void showChange() {
        revalidate();
        repaint();
        setVisible(true);
    }

    public void rescale(int scale) {
        avt.rescale(scale);
        avt.setBounds(0, 0, scale, scale);
        mask.setBounds(0, 0, scale, scale);
        setPreferredSize(new Dimension(scale, scale));
    }
}
