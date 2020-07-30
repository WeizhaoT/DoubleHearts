package panel;

import element.ImageLabel;
import layout.WelcomeLayout;
import main.ClientView;
import ui.*;

import java.awt.*;
import javax.swing.*;

public class SeatPanel extends JPanel {
    static final long serialVersionUID = 1L;

    private final ImageLabel avatar;
    private final JLabel playerName;
    private final JButton sitButton;

    private static final int w_ = WelcomeLayout.seatw;
    private static final int h_ = WelcomeLayout.seath;
    private static final int buttonheight = 30;
    private static final int avtScale = h_ - buttonheight;

    public SeatPanel(final int index, final ClientView view) {
        setLayout(null);
        setName("seat_" + index);
        setPreferredSize(new Dimension(w_, h_));
        setBackground(MyColors.darkBlue);

        avatar = ImageLabel.createAvatar(index, avtScale);
        avatar.setOpaque(true);
        avatar.setBackground(MyColors.gray);
        avatar.setBounds((w_ - avtScale) / 2, 0, avtScale, avtScale);

        playerName = new JLabel("", SwingConstants.CENTER);
        playerName.setForeground(MyColors.text);
        playerName.setFont(MyFont.seatName);
        playerName.setBounds(0, avtScale, w_, buttonheight);

        sitButton = new JButton(MyText.getSitButtonLabel());
        sitButton.setBounds((w_ - avtScale) / 2, avtScale, avtScale, buttonheight);
        sitButton.setFont(MyText.getSitButtonFont());
        sitButton.setName("sitButton_" + index);
        sitButton.addActionListener(view);

        add(avatar);
        add(playerName);
        add(sitButton);
        reset();
    }

    public void reset() {
        avatar.setIcon(null);
        avatar.setOpaque(true);
        avatar.setBorder(BorderFactory.createLineBorder(MyColors.darkGray, 2));

        playerName.setText("");
        playerName.setVisible(false);

        sitButton.setVisible(true);
        sitButton.setEnabled(true);
        showChanges();
    }

    public void setPlayer(final int avtIndex, final String name) {
        avatar.setAvatar(avtIndex, avtScale);
        avatar.setBorder(BorderFactory.createEmptyBorder());

        sitButton.setVisible(false);
        sitButton.setEnabled(false);

        playerName.setText(name);
        playerName.setVisible(true);
        showChanges();
    }

    public void enableSitButton(final boolean enable) {
        sitButton.setEnabled(enable);
    }

    public void showChanges() {
        revalidate();
        repaint();
        setVisible(true);
    }
}