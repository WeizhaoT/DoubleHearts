package panel;

import main.ClientController;
import element.ImageLabel;
import ui.*;

import javax.swing.*;
import layout.PlayerPanelLayout;

public class PlayerPanel extends JPanel {
    static final long serialVersionUID = 1L;

    private int totalScore = 0;
    private JLabel scoreLabel;
    private JLabel nameLabel;
    private ImageLabel avatar;

    public PlayerPanel() {
        setOpaque(false);
        setLayout(new PlayerPanelLayout());

        if (ClientController.TEST_MODE)
            setBorder(BorderFactory.createLineBorder(MyColors.yellow));

        scoreLabel = new JLabel("<html>" + MyText.getTotalScoreText() + "<font face=\"Courier new\">0</font></html>",
                SwingConstants.CENTER);
        scoreLabel.setForeground(MyColors.yellow);
        scoreLabel.setFont(MyText.getScoreFont());

        avatar = new ImageLabel();

        nameLabel = new JLabel("", SwingConstants.CENTER);
        nameLabel.setForeground(MyColors.text);
        nameLabel.setFont(MyFont.playerName);

        add(avatar, PlayerPanelLayout.AVATAR);
        add(scoreLabel, PlayerPanelLayout.TSCORE);
        add(nameLabel, PlayerPanelLayout.NAME);
        showChanges();
    }

    public void setPlayer(int avtIndex, String name) {
        avatar.setAvatar(avtIndex);
        nameLabel.setText("<html><center>" + name + "</center></html>");
        setName(name);
        showChanges();
    }

    public void clear() {
        totalScore = 0;
        avatar.setEmptyAvatar();
        scoreLabel.setText("<html>" + MyText.getTotalScoreText() + "<font face=\"Courier new\">0</font></html>");
        nameLabel.setText("");
    }

    public void setTotalScore(int score) {
        totalScore = score;
        scoreLabel.setText(
                "<html>" + MyText.getTotalScoreText() + "<font face=\"Courier new\">" + totalScore + "</font></html>");
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void showChanges() {
        revalidate();
        repaint();
        setVisible(true);
    }
}