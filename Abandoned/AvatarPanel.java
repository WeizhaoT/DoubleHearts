import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Random;

public class AvatarPanel extends JPanel {
    static final long serialVersionUID = 1L;

    private static int numAvt = 20;
    private static int pad = 10;
    private static int imageScale = 150;

    private static int boxScale = 2 * pad + imageScale;
    public static Dimension avatarPanelDim = new Dimension(boxScale * 5, boxScale * 4);

    private int choice = -1;
    private MaskedAvatar[] avatars = new MaskedAvatar[numAvt];
    private JLabel indicator;

    private MouseAdapter mouseAdapter;

    public AvatarPanel() {
        setLayout(null);
        setPreferredSize(avatarPanelDim);
        setBackground(dims.darkBlue);

        setupAvatars();
        setupIndicator();
    }

    private void setupAvatars() {
        for (int i = 0; i < numAvt; i++) {
            avatars[i] = new MaskedAvatar(i);
            avatars[i].setName(String.valueOf(i));
            avatars[i].setBounds(boxScale * (i % 5) + pad, boxScale * (i / 5) + pad, imageScale, imageScale);
            avatars[i].maskOn();
            avatars[i].addMouseListener(mouseAdapter);
            add(avatars[i]);
        }
    }

    private void setupIndicator() {
        try {
            indicator = new JLabel(new ImageIcon(ImageIO.read(Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(String.format("Avatars/indicator.png")))));
            indicator.setName("-1");
        } catch (IOException e) {
            e.printStackTrace();
        }

        choice = (new Random()).nextInt(numAvt);
        avatars[choice].maskOff();

        setComponentZOrder(indicator, getComponentCount() - 1);
        indicator.setBounds(boxScale * (choice % 5), boxScale * (choice / 5), boxScale, boxScale);
        indicator.setVisible(true);
        add(indicator);
    }

    public int getChoice() {
        return choice;
    }

    public void showChanges() {
        revalidate();
        repaint();
        setVisible(true);
    }

}