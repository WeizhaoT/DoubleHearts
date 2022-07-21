package panel;

import ui.*;
import main.*;
import element.*;
import layout.PokerTableLayout;

import java.awt.*;
import javax.swing.*;

public class CenterPanel extends JPanel {
    static final long serialVersionUID = 1L;

    private final ClientView view;
    private int frame = 0;

    private final TableSectionPanel[] sectionPanels = new TableSectionPanel[4];
    private final DigitalClock[] sectionClocks = new DigitalClock[4];
    private final DigitalClock cornerClock;

    private final LabelWithBG helperLabel;
    private final LabelWithBG bigHelperLabel;
    // private final BackgroundRect helperLabelBG;
    private final Scoreboard scoreboard;
    private final LabelWithBG passLabel;

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

        helperLabel = new LabelWithBG();
        helperLabel.setLabelFont(MyText.getErrMsgFont());
        add(helperLabel, PokerTableLayout.HELPER);

        bigHelperLabel = new LabelWithBG();
        bigHelperLabel.setLabelFont(MyText.getExposeFont());
        add(bigHelperLabel, PokerTableLayout.BHELPER);

        scoreboard = new Scoreboard();
        add(scoreboard, PokerTableLayout.SBOARD);

        passLabel = new LabelWithBG();
        passLabel.setLabelFont(MyText.getPassFont());
        add(passLabel, PokerTableLayout.PLABEL);

        cornerClock = new DigitalClock(-1, view, this, true);
        add(cornerClock, PokerTableLayout.CCLOCK);

        for (int j = 1; j < 4; j++) {
            for (int i = 0; i < 4; i++) {
                arrows[j][i] = new ArrowLabel(j, i);
                add(arrows[j][i], PokerTableLayout.ARROW + j + i);
            }
        }

        if (ClientController.TEST_MODE >= 2) {
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

    public void allApplyExposure(String[] aliases) {
        for (final TableSectionPanel tableSectionPanel : sectionPanels) {
            tableSectionPanel.applyExposure(aliases);
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

    public void setErrMsg(final int errCode, final int... flags) {
        if (errCode == MyText.NORMAL) {
            helperLabel.setVisible(false);
            bigHelperLabel.setVisible(false);
        } else if (errCode == MyText.HINT_SHOWING || errCode == MyText.ILLEGAL_SHOWING) {
            setBigHelper(errCode, flags);
            helperLabel.setVisible(false);
        } else {
            setHelper(MyText.getErrMsg(errCode), true);
            bigHelperLabel.setVisible(false);
        }
    }

    public void setConnErrMsg(final String name) {
        setHelper(MyText.getConnErrMsg(name), true);
    }

    private void setHelper(final String errmsg, final boolean html) {
        if (html)
            helperLabel.setLabelText("<html><center>" + errmsg + "</center></html>");
        else
            helperLabel.setLabelText(errmsg);

        helperLabel.setVisible(true);
        showChanges();
    }

    private void setBigHelper(final int errCode, final int... flags) {
        bigHelperLabel.setLabelText("<html><center>" + MyText.getErrMsg(errCode, flags) + "</center></html>");
    }

    public void enablePassingHints(final boolean enable) {
        if (enable) {
            passLabel.setLabelText("<html><center>" + MyText.getPassingHint() + "</center></html>");
        } else {
            passLabel.setVisible(false);
        }
        showChanges();
    }

    public void setScoreBoard() {
        scoreboard.showScores(view.getScores(), view.getNames());
    }

    public void reset() {
        helperLabel.setVisible(false);
        scoreboard.setVisible(false);
        passLabel.setVisible(false);

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