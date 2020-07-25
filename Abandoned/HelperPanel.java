import javax.swing.*;

import java.awt.*;
import java.util.HashSet;

public class HelperPanel extends JPanel {
    static final long serialVersionUID = 1L;

    public final int w_ = 680;
    public final int h_ = 500;

    private final int errw = 420;
    private final int errh = 120;
    private final int scorew = 680;
    private final int scoreh = 300;
    private final int namew = 420;
    private final int gap = 0;
    private final int inset = 10;
    private final int divh = scoreh / 5;

    private final Color scoreFG = MyColors.tableGreen;

    private static final float errFontSize = 24.0f;
    private static final float scoreBoardFontSize = 24.0f;

    private JLabel helperErrLabel;
    private BackgroundRect errLabelBG;
    private JPanel scoreBoard;
    private BackgroundRect scoreBoardBG;

    public HelperPanel() {
        setLayout(null);
        setPreferredSize(new Dimension(w_, h_));
        setBackground(MyColors.tableGreen);
        setOpaque(false);

        setBounds(dims.tablex + dims.centerLoc(2 * dims.tableSec.width, w_),
                dims.tabley + dims.centerLoc(3 * dims.tableSec.height, h_), w_, h_);

        helperErrLabel = new JLabel("");
        helperErrLabel.setOpaque(false);
        helperErrLabel.setForeground(Color.BLACK);
        helperErrLabel.setFont(helperErrLabel.getFont().deriveFont(errFontSize));
        helperErrLabel.setBounds(dims.centerLoc(w_, errw), dims.centerLoc(h_, errh), errw, errh);
        add(helperErrLabel);

        errLabelBG = new BackgroundRect(errw + 2 * inset, errh + 2 * inset);
        errLabelBG.setBounds(dims.centerLoc(w_, errw) - inset, dims.centerLoc(h_, errh) - inset);
        add(errLabelBG);

        scoreBoard = new JPanel(null);
        scoreBoard.setOpaque(false);
        scoreBoard.setBounds(dims.centerLoc(w_, scorew), dims.centerLoc(h_, scoreh), scorew, scoreh);
        add(scoreBoard);

        scoreBoardBG = new BackgroundRect(scorew, scoreh);
        scoreBoardBG.setBounds(scoreBoard.getBounds());
        add(scoreBoardBG);

        setComponentZOrder(helperErrLabel, 0);
        setComponentZOrder(scoreBoard, 1);
        setComponentZOrder(errLabelBG, 2);
        setComponentZOrder(scoreBoardBG, 3);

        reset();
    }

    public void setErrMsg(String errmsg) {
        if (errmsg == null || errmsg.isEmpty()) {
            helperErrLabel.setVisible(false);
            errLabelBG.setVisible(false);
            showChanges();
            return;
        }

        helperErrLabel.setText("<html><center>" + errmsg + "</center></html>");
        helperErrLabel.setVisible(true);
        errLabelBG.setVisible(true);
        showChanges();
    }

    public void setScoreBoard(ClientView view) {
        if (view == null) {
            scoreBoard.removeAll();
            scoreBoard.setVisible(false);
            scoreBoardBG.setVisible(false);
            showChanges();
            return;
        }

        int[] scores = view.getScores();
        String[] names = view.getNames();

        HashSet<Integer> frameWinners = new HashSet<>();
        HashSet<Integer> totalWinners = new HashSet<>();
        frameWinners.add(0);
        totalWinners.add(0);
        int maxScore = scores[0];
        int maxTotalScore = scores[4];

        for (int i = 1; i < 4; i++) {
            if (scores[i] >= maxScore) {
                if (scores[i] > maxScore)
                    frameWinners.clear();

                frameWinners.add(i);
                maxScore = scores[i];
            }

            if (scores[4 + i] >= maxTotalScore) {
                if (scores[4 + i] > maxTotalScore)
                    totalWinners.clear();

                totalWinners.add(i);
                maxTotalScore = scores[4 + i];
            }
        }

        JLabel title = new JLabel("Scoreboard", SwingConstants.CENTER);
        title.setForeground(scoreFG);
        title.setFont(title.getFont().deriveFont(scoreBoardFontSize));
        title.setBounds(0, 0, scorew, divh);
        scoreBoard.add(title);

        JLabel horizSplitter = new JLabel();
        horizSplitter.setBounds(gap, divh, scorew - 2 * gap, 5);
        horizSplitter.setBorder(BorderFactory.createLineBorder(MyColors.tableGreen, 5));
        scoreBoard.add(horizSplitter);

        JLabel vertSplitter = new JLabel();
        vertSplitter.setBounds(namew + inset, divh, 5, scoreh - divh - gap);
        vertSplitter.setBorder(BorderFactory.createLineBorder(MyColors.tableGreen, 5));
        scoreBoard.add(vertSplitter);

        int y = divh;
        for (int i = 0; i < 4; i++) {
            int x = inset;
            String scoreString = String.valueOf(scores[i]);
            String totalScoreString = String.valueOf(scores[4 + i]);
            if (scores[i] >= 0)
                scoreString = "+" + scoreString;

            scoreString = "( " + scoreString + " )";
            // if (frameWinners.contains(i))
            // scoreString = "<b>" + dims.getColoredText(scoreString, dims.heartColor) +
            // "</b>";

            // if (totalWinners.contains(i))
            // totalScoreString = "<b>" + dims.getColoredText(totalScoreString,
            // dims.heartColor) + "</b>";

            JLabel nameLabel = new JLabel(names[i], SwingConstants.CENTER);
            nameLabel.setForeground(scoreFG);
            nameLabel.setFont(nameLabel.getFont().deriveFont(scoreBoardFontSize));
            nameLabel.setBounds(x, y, namew, divh);
            scoreBoard.add(nameLabel);

            x += namew;
            JLabel totalScoreLabel = new JLabel(totalScoreString, SwingConstants.RIGHT);
            totalScoreLabel.setForeground(totalWinners.contains(i) ? dims.heartColor : scoreFG);
            totalScoreLabel.setFont(totalScoreLabel.getFont().deriveFont(scoreBoardFontSize));
            totalScoreLabel.setBounds(x, y, (scorew - namew) / 2 - inset, divh);
            scoreBoard.add(totalScoreLabel);

            x += (scorew - namew) / 2 - inset;
            JLabel scoreLabel = new JLabel(scoreString, SwingConstants.RIGHT);
            scoreLabel.setForeground(frameWinners.contains(i) ? dims.heartColor : scoreFG);
            scoreLabel.setFont(scoreLabel.getFont().deriveFont(scoreBoardFontSize));
            scoreLabel.setBounds(x, y, (scorew - namew) / 2 - inset, divh);
            scoreBoard.add(scoreLabel);

            y += divh;
        }
        scoreBoardBG.setVisible(true);
        scoreBoard.setVisible(true);
        showChanges();
    }

    public void reset() {
        helperErrLabel.setVisible(false);
        errLabelBG.setVisible(false);
        setScoreBoard(null);
        showChanges();
    }

    public void showChanges() {
        revalidate();
        repaint();
        setVisible(true);
    }

}