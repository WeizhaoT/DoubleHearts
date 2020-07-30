package panel;

import ui.*;
import main.*;
import element.*;
import layout.PokerTableLayout;

import java.awt.*;
import javax.swing.*;

public class CenterPanel extends JPanel {
    static final long serialVersionUID = 1L;

    private static final int errw = PokerTableLayout.helperWidth;
    private static final int errh = PokerTableLayout.helperHeight;
    private static final int passw = PokerTableLayout.passWidth;
    private static final int passh = PokerTableLayout.passHeight;

    private final ClientView view;
    private int frame = 0;

    private final TableSectionPanel[] sectionPanels = new TableSectionPanel[4];
    private final DigitalClock[] sectionClocks = new DigitalClock[4];
    private final DigitalClock cornerClock;

    private final JLabel helperLabel;
    private final BackgroundRect helperLabelBG;
    // private final JPanel scoreboard;
    private final Scoreboard scoreboard;
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
            sectionClocks[i] = new DigitalClock(-1, view, this, i == 0);

            add(sectionPanels[i], PokerTableLayout.ALLSECS[i]);
            add(sectionClocks[i], PokerTableLayout.ALLCLOCKS[i]);
        }

        helperLabel = new JLabel("", SwingConstants.CENTER);
        helperLabel.setOpaque(false);
        helperLabel.setForeground(MyColors.tableGreen);
        helperLabel.setFont(MyText.getErrMsgFont());
        add(helperLabel, PokerTableLayout.HLABEL);

        helperLabelBG = new BackgroundRect(errw, errh);
        add(helperLabelBG, PokerTableLayout.HBG);

        scoreboard = new Scoreboard();
        add(scoreboard, PokerTableLayout.SBOARD);

        passLabel = new JLabel("", SwingConstants.CENTER);
        passLabel.setOpaque(false);
        passLabel.setForeground(MyColors.tableGreen);
        passLabel.setFont(MyText.getPassFont());
        add(passLabel, PokerTableLayout.PLABEL);

        passLabelBG = new BackgroundRect(passw, passh);
        add(passLabelBG, PokerTableLayout.PBG);

        cornerClock = new DigitalClock(-1, view, this, true);
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

    public void setCurrentClockEnforcer(boolean b) {
        cornerClock.setEnforcement(b);
        sectionClocks[0].setEnforcement(b);
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

    public void endCornerTiming() {
        cornerClock.endTiming();
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

    public void setTradeGap(final int index) {
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

    public void setErrMsg(final int errCode) {
        setErrMsg(MyText.getErrMsg(errCode), true);
    }

    public void setConnErrMsg(final String name) {
        setErrMsg(MyText.getConnErrMsg(name), true);
    }

    private void setErrMsg(final String errmsg, final boolean html) {
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
            passLabel.setText("<html><center>" + MyText.getPassingHint() + "</center></html>");
            passLabel.setVisible(true);
            passLabelBG.setVisible(true);
        } else {
            passLabel.setVisible(false);
            passLabelBG.setVisible(false);
        }
        showChanges();
    }

    public void setScoreBoard() {
        scoreboard.showScores(view.getScores(), view.getNames());
    }

    public void reset() {
        helperLabel.setVisible(false);
        helperLabelBG.setVisible(false);
        scoreboard.setVisible(false);
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