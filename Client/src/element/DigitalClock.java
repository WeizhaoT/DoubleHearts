package element;

import ui.MyFont;
import main.ClientView;
import panel.CenterPanel;
import layout.PokerTableLayout;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * The {@code DigitalClock} class represents a digical stop watch that counts
 * down from a given time limit.
 * 
 * @author Weizhao Tang
 */
public class DigitalClock extends JPanel {
    static final long serialVersionUID = 1L;

    /** fixed width of clock */
    public static final int w_ = PokerTableLayout.clockWidth;
    /** fixed height of clock */
    public static final int h_ = PokerTableLayout.clockHeight;

    /** width of digit label */
    private static final int textw = 70;
    /** height of digit label */
    private static final int texth = 70;
    /** X-offset of digit label from center */
    private static final int labelXOffset = -12;
    /** Y-offset of digit label from center */
    private static final int labelYOffset = -5;

    /** Frequency of count down (num per second) */
    private static final int freq = 2;
    /** Time threshold to enter emergency state */
    private static final int emergency = 10;

    /** classic light green color for digits */
    private static final Color digitGreen = new Color(25, 230, 100);
    /** classic red color for digits */
    private static final Color digitRed = new Color(255, 120, 135);

    /** num to count down */
    private int count;
    /** flag to indicate if default action is enforced after timeout */
    private final boolean doDefault;
    private boolean enforce;
    /** GUI master view */
    private final ClientView view;
    private final CenterPanel parent;
    /** digit label */
    private final JLabel digits;
    /** timer executing count down */
    private final Timer timer;

    /**
     * Instantiate the {@code DigitalClock} class counting down from {@code c}
     * seconds.
     * 
     * @param c            num of seconds to count down
     * @param v            GUI master view
     * @param reactDefault {@code true} if reaction is enforced after timeout;
     *                     {@code false} otherwise.
     */
    public DigitalClock(final int c, final ClientView masterView, final CenterPanel parentPanel,
            final boolean reactDefault) {
        super();
        count = freq * c;
        view = masterView;
        parent = parentPanel;
        doDefault = reactDefault;
        enforce = reactDefault;
        setOpaque(true);
        setLayout(null);
        setVisible(false);

        setBackground(new Color(32, 14, 10));
        setBorder(BorderFactory.createLoweredBevelBorder());
        setMinimumSize(new Dimension(w_, h_));

        digits = new JLabel(c > 0 ? String.valueOf(c) : "--", SwingConstants.RIGHT);
        digits.setOpaque(false);
        digits.setForeground(digitGreen);
        digits.setFont(MyFont.clock);
        digits.setVerticalAlignment(JLabel.CENTER);
        digits.setBounds((w_ - textw) / 2 + labelXOffset, (h_ - texth) / 2 + labelYOffset, textw, texth);
        add(digits);

        // Count down timer
        timer = new Timer(1000 / freq, new ActionListener() {
            public void actionPerformed(final ActionEvent evt) {
                if (count > 1) {
                    if (count > emergency * freq) // normal
                        digits.setText(String.valueOf(1 + (--count - 1) / freq));
                    else { // emergency
                        if (count % freq == 1) {
                            view.playClip("alarm2");
                        }
                        digits.setText(String.valueOf(1 + (--count - 1) / freq));
                        digits.setForeground(count % 2 == 1 ? digitRed : digitGreen); // switch between colors
                    }
                    parent.showChanges();
                } else {
                    endTiming();
                    view.acquireActionLock();
                    if (enforce)
                        view.performDefaultReaction();

                    view.releaseActionLock();
                }
            }
        });
        timer.setRepeats(true);
    }

    /**
     * Set enforcement status.
     * 
     * @param enforce {@code true} to enforce default reactions on time out;
     *                {@code false} to cancel enforcement
     */
    public void setEnforcement(boolean enforce) {
        this.enforce = enforce;
    }

    /**
     * Stop timer and hide the clock.
     */
    public void endTiming() {
        timer.stop();
        setVisible(false);
    }

    /**
     * Restart timing starting with {@code c} seconds. {@code c <= 0} implies the
     * clock should stuck at "--".
     * 
     * @param c Number of seconds to start with
     */
    public void restart(final int c) {
        timer.stop();
        enforce = doDefault; // Reset enforcement status
        count = freq * c;
        digits.setText(c > 0 ? String.valueOf(c) : "--");
        digits.setForeground(digitGreen);
        setVisible(true);
        if (c > 0)
            timer.restart();
    }
}