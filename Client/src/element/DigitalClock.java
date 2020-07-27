package element;

import ui.MyFont;
import main.ClientView;
import layout.PokerTableLayout;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.sound.sampled.*;

public class DigitalClock extends JPanel {
    static final long serialVersionUID = 1L;

    public static final int w_ = PokerTableLayout.clockWidth;
    public static final int h_ = PokerTableLayout.clockHeight;

    private static final int texth = 70;
    private static final int textw = 70;
    private static final int labelXOffset = -12;
    private static final int labelYOffset = -5;

    private static final int freq = 2;
    private static final int emergency = 10;

    private static final Color digitGreen = new Color(25, 230, 100);
    private static final Color digitRed = new Color(255, 120, 135);

    // private JLabel digits;
    private int count;
    private final ClientView view;
    private final JLabel digits;
    private final Timer timer;
    private Clip clip;

    public DigitalClock(final int c) {
        this(c, null);
    }

    public DigitalClock(final int c, final ClientView v) {
        super();
        count = freq * c;
        view = v;
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

        try {
            final AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(
                    Thread.currentThread().getContextClassLoader().getResourceAsStream("Sounds/alarm2.wav")));
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Warning: failed to load alarm sound");
            clip = null;
        }

        timer = new Timer(1000 / freq, new ActionListener() {
            public void actionPerformed(final ActionEvent evt) {
                if (count > 1) {
                    if (count > emergency * freq)
                        digits.setText(String.valueOf(1 + (--count - 1) / freq));
                    else {
                        if (count % freq == 1 && clip != null) {
                            clip.setMicrosecondPosition(0);
                            clip.start();
                        }
                        digits.setText(String.valueOf(1 + (--count - 1) / freq));
                        digits.setForeground(count % 2 == 1 ? digitRed : digitGreen);
                    }
                } else {
                    endTiming();
                    if (view != null)
                        view.performDefaultReaction();
                }
            }
        });
        timer.setRepeats(true);
    }

    public void activate() {
        setVisible(true);

        if (count > 0)
            timer.start();
    }

    public void endTiming() {
        timer.stop();
        setVisible(false);
    }

    public void restart(final int c) {
        timer.stop();
        count = freq * c;
        digits.setText(c > 0 ? String.valueOf(c) : "--");
        digits.setForeground(digitGreen);
        setVisible(true);
        if (c > 0)
            timer.restart();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(w_, h_);
    }
}