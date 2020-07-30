package element;

import ui.MyColors;
import layout.PokerGameLayout;

import java.awt.*;
import javax.swing.*;

/**
 * A {@code Speaker} object is a switch of sound effects in form of a speaker
 * icon.
 * 
 * @author Weizhao Tang
 */
public class Speaker extends JPanel {
    private static final long serialVersionUID = 1L;

    /** Y-offset of vertically-centered icon */
    private static final int labelYOffset = -1;
    /** width of icon label */
    private static final int textw = 70;
    /** height of icon label */
    private static final int texth = 60;

    /** width of {@code Speaker} object */
    private static final int w_ = PokerGameLayout.speakerw;
    /** height of {@code Speaker} object */
    private static final int h_ = PokerGameLayout.speakerh;

    /** x-coordinate of icon label */
    private static final int textx = (w_ - textw) / 2;
    /** y-coordinate of icon label */
    private static final int texty = (h_ - texth) / 2 + labelYOffset;

    /** flag for mute */
    private boolean mute = false;
    /** label of speaker icon */
    private final JLabel speakerLabel;
    /** semi-translucent background */
    private final BackgroundRect background;

    /**
     * Instantiate the {@code Speaker} class with fully preset settings.
     */
    public Speaker() {
        super();
        setLayout(null);
        setOpaque(false);

        background = new BackgroundRect(w_, h_);
        background.setBounds(0, 0, w_, h_);
        add(background);

        // speaker with 3 waves
        speakerLabel = new JLabel("\ud83d\udd0a", SwingConstants.CENTER);
        speakerLabel.setVerticalAlignment(SwingConstants.CENTER);
        speakerLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 60));
        speakerLabel.setForeground(MyColors.tableGreen);
        speakerLabel.setBounds(textx, texty, textw, texth);
        add(speakerLabel);

        setComponentZOrder(speakerLabel, 0);
    }

    /**
     * Get current mute state.
     * 
     * @return Current mute state
     */
    public boolean getMute() {
        return mute;
    }

    /**
     * Switch mute state and return state after switching.
     * 
     * @return State after switching
     */
    public boolean switchMute() {
        // if mute, set label text to a muted speaker
        speakerLabel.setText((mute = !mute) ? "\ud83d\udd07" : "\ud83d\udd0a");
        return mute;
    }
}