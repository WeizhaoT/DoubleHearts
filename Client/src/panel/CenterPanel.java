package panel;

import ui.*;
import main.*;
import element.*;
import layout.PokerTableLayout;

import java.awt.*;
import java.util.HashSet;
import javax.swing.*;

public class CenterPanel extends JPanel {
    static final long serialVersionUID = 1L;

    private static final int errw = PokerTableLayout.helperWidth;
    private static final int errh = PokerTableLayout.helperHeight;
    private static final int scorew = PokerTableLayout.scoreWidth;
    private static final int scoreh = PokerTableLayout.scoreHeight;
    private static final int passw = PokerTableLayout.passWidth;
    private static final int passh = PokerTableLayout.passHeight;
    private static final int namew = 360;
    private static final int gap = 0;
    private static final int inset = 10;
    private static final int divh = scoreh / 5;

    private static final Color scoreFG = MyColors.tableGreen;

    private final ClientView view;
    private int frame = 0;

    private final TableSectionPanel[] sectionPanels = new TableSectionPanel[4];
    private final DigitalClock[] sectionClocks = new DigitalClock[4];
    private final DigitalClock cornerClock;

    private final JLabel helperLabel;
    private final BackgroundRect helperLabelBG;
    private final JPanel scoreBoard;
    private final BackgroundRect scoreBoardBG;
    private final JLabel passLabel;
    private final BackgroundRect passLabelBG;

    private final ArrowLabel[][] arrows = new ArrowLabel[4][4];

    public CenterPanel(ClientView v) {
        super();
        view = v;
        setLayout(new PokerTableLayout());
        setOpaque(false);

        for (int i = 0; i < 4; i++) {
            sectionPanels[i] = new TableSectionPanel(i);
            sectionClocks[i] = new DigitalClock(-1, i == 0 ? view : null);

            add(sectionPanels[i], PokerTableLayout.ALLSECS[i]);
            add(sectionClocks[i], PokerTableLayout.ALLCLOCKS[i]);
        }

        helperLabel = new JLabel("", SwingConstants.CENTER);
        helperLabel.setOpaque(false);
        helperLabel.setForeground(MyColors.tableGreen);
        helperLabel.setFont(MyFont.errMsg);
        add(helperLabel, PokerTableLayout.HLABEL);

        helperLabelBG = new BackgroundRect(errw, errh);
        add(helperLabelBG, PokerTableLayout.HBG);

        scoreBoard = new JPanel(null);
        scoreBoard.setOpaque(false);
        add(scoreBoard, PokerTableLayout.SLABEL);

        scoreBoardBG = new BackgroundRect(scorew, scoreh);
        add(scoreBoardBG, PokerTableLayout.SBG);

        passLabel = new JLabel("", SwingConstants.CENTER);
        passLabel.setOpaque(false);
        passLabel.setForeground(MyColors.tableGreen);
        passLabel.setFont(MyFont.pass);
        add(passLabel, PokerTableLayout.PLABEL);

        passLabelBG = new BackgroundRect(passw, passh);
        add(passLabelBG, PokerTableLayout.PBG);

        cornerClock = new DigitalClock(-1, view);
        add(cornerClock, PokerTableLayout.CCLOCK);

        for (int j = 1; j < 4; j++) {
            for (int i = 0; i < 4; i++) {
                arrows[j][i] = new ArrowLabel(j, i);
                add(arrows[j][i], PokerTableLayout.ARROW + j + i);
            }
        }

        if (ClientController.TEST_MODE) {
            setBorder(BorderFactory.createLineBorder(Color.WHITE));
            for (final Component comp : getComponents()) {
                ((JComponent) comp).setBorder(BorderFactory.createLineBorder(MyColors.randomColor()));
            }
        }

        reset();
    }

    public void clearSection(final int i) {
        sectionPanels[i].clear();
    }

    public void allSectionsClear() {
        for (final TableSectionPanel panel : sectionPanels)
            panel.clear();
    }

    public void allSaveHistory() {
        for (final TableSectionPanel tableSectionPanel : sectionPanels) {
            tableSectionPanel.saveHistory();
        }
    }

    public void allShowHistory() {
        for (final TableSectionPanel tableSectionPanel : sectionPanels) {
            tableSectionPanel.showHistory();
        }
    }

    public void allHideHistory() {
        for (final TableSectionPanel tableSectionPanel : sectionPanels) {
            tableSectionPanel.hideHistory();
        }
    }

    public void setCornerTimer(final int timeLimit) {
        cornerClock.restart(timeLimit);
    }

    public void showCards(final int i, final String[] aliases) {
        sectionPanels[i].showCards(aliases);
    }

    public void endTiming(final int i) {
        sectionClocks[i].endTiming();
    }

    public void showWaiting(final int timeLimit, final int i) {
        sectionClocks[i].restart(timeLimit);
    }

    public void showReady(final int i) {
        sectionPanels[i].showReady();
    }

    public void showPass(final int i) {
        sectionPanels[i].showPass();
    }

    public void setFrameIndex(final int index) {
        frame = index;
    }

    public void showAllArrows() {
        if (frame != 0) {
            for (int i = 0; i < 4; i++) {
                arrows[frame][i].flip(true);
                arrows[frame][i].setVisible(true);
            }
        }
    }

    public void flipArrow(final int i) {
        if (frame != 0) {
            arrows[frame][i].flip(false);
            showChanges();
        }
    }

    public void setErrMsg(final String errmsg) {
        setErrMsg(errmsg, true);
    }

    public void setErrMsg(final String errmsg, final boolean html) {
        if (errmsg == null || errmsg.isEmpty()) {
            helperLabel.setVisible(false);
            helperLabelBG.setVisible(false);
            showChanges();
            return;
        }

        if (html)
            helperLabel.setText("<html><center>" + errmsg + "</center></html>");
        else
            helperLabel.setText(errmsg);
        helperLabel.setVisible(true);
        helperLabelBG.setVisible(true);
        showChanges();
    }

    public void enablePassingHints(final boolean enable) {
        if (enable) {
            passLabel.setText("<html><center>Pass 3 cards along the arrows</center></html>");
            passLabel.setVisible(true);
            passLabelBG.setVisible(true);
        } else {
            passLabel.setVisible(false);
            passLabelBG.setVisible(false);
        }
        showChanges();
    }

    public void setScoreBoard(final ClientView view) {
        if (view == null) {
            scoreBoard.removeAll();
            scoreBoard.setVisible(false);
            scoreBoardBG.setVisible(false);
            showChanges();
            return;
        }

        final int[] scores = view.getScores();
        final String[] names = view.getNames();

        final HashSet<Integer> frameWinners = new HashSet<>();
        final HashSet<Integer> totalWinners = new HashSet<>();
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

        final JLabel title = new JLabel("Scoreboard", SwingConstants.CENTER);
        title.setForeground(scoreFG);
        title.setFont(title.getFont().deriveFont(MyFont.Size.score));
        title.setBounds(0, 0, scorew, divh);
        scoreBoard.add(title);

        final JLabel horizSplitter = new JLabel();
        horizSplitter.setBounds(gap, divh, scorew - 2 * gap, 5);
        horizSplitter.setBorder(BorderFactory.createLineBorder(MyColors.tableGreen, 5));
        scoreBoard.add(horizSplitter);

        final JLabel vertSplitter = new JLabel();
        vertSplitter.setBounds(namew + inset, divh, 5, scoreh - divh - gap);
        vertSplitter.setBorder(BorderFactory.createLineBorder(MyColors.tableGreen, 5));
        scoreBoard.add(vertSplitter);

        int y = divh;
        for (int i = 0; i < 4; i++) {
            int x = inset;
            String scoreString = String.valueOf(scores[i]);
            final String totalScoreString = String.valueOf(scores[4 + i]);
            if (scores[i] >= 0)
                scoreString = "+" + scoreString;

            scoreString = "(" + scoreString + ")";

            final JLabel nameLabel = new JLabel(names[i], SwingConstants.CENTER);
            nameLabel.setForeground(scoreFG);
            nameLabel.setFont(nameLabel.getFont().deriveFont(MyFont.Size.userNameLarge));
            nameLabel.setBounds(x, y, namew, divh);
            scoreBoard.add(nameLabel);

            x += namew;
            final JLabel totalScoreLabel = new JLabel(totalScoreString, SwingConstants.RIGHT);
            totalScoreLabel.setForeground(totalWinners.contains(i) ? MyColors.heartColor : scoreFG);
            totalScoreLabel.setFont(MyFont.smallScore);
            totalScoreLabel.setBounds(x, y, (scorew - namew) / 2 - inset, divh);
            scoreBoard.add(totalScoreLabel);

            x += (scorew - namew) / 2 - inset;
            final JLabel scoreLabel = new JLabel(scoreString, SwingConstants.RIGHT);
            scoreLabel.setForeground(frameWinners.contains(i) ? MyColors.heartColor : scoreFG);
            scoreLabel.setFont(MyFont.smallScore);
            scoreLabel.setBounds(x, y, (scorew - namew) / 2 - inset, divh);
            scoreBoard.add(scoreLabel);

            y += divh;
        }
        scoreBoardBG.setVisible(true);
        scoreBoard.setVisible(true);
        showChanges();
    }

    public void reset() {
        helperLabel.setVisible(false);
        helperLabelBG.setVisible(false);
        setScoreBoard(null);
        scoreBoard.setVisible(false);
        scoreBoardBG.setVisible(false);
        passLabel.setVisible(false);
        passLabelBG.setVisible(false);

        for (int j = 1; j < 4; j++) {
            for (int i = 0; i < 4; i++) {
                arrows[j][i].flip(true);
                arrows[j][i].setVisible(false);
            }
        }

        for (final TableSectionPanel sectionPanel : sectionPanels) {
            sectionPanel.clear();
        }
        for (final DigitalClock clock : sectionClocks) {
            clock.endTiming();
        }
        cornerClock.endTiming();

        showChanges();
    }

    public void showChanges() {
        revalidate();
        repaint();
        setVisible(true);
    }
}