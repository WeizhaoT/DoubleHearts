package element;

import ui.*;

import java.awt.*;
import javax.swing.*;

public class LabelWithBG extends JPanel {
    static final long serialVersionUID = 1L;

    private static final Dimension preferred = new Dimension(100, 100);

    private int w_;
    private int h_;
    private final JLabel textLabel = new JLabel();
    private final BackgroundRect background = new BackgroundRect(preferred.width, preferred.height);

    public LabelWithBG(int w, int h) {
        w_ = w;
        h_ = h;
        setLayout(null);
        setOpaque(false);

        textLabel.setOpaque(false);
        textLabel.setBounds(0, 0, w, h);
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setForeground(MyColors.tableGreen);
        textLabel.setFont(MyText.getErrMsgFont());
        add(textLabel);

        background.rescale(w, h);
        add(background, 1);

        revalidate();
        repaint();
        setVisible(false);
    }

    public LabelWithBG() {
        this(preferred.width, preferred.height);
    }

    public void setLabelFont(Font font) {
        textLabel.setFont(font);
    }

    public void setLabelText(String text) {
        textLabel.setText(text);
        setVisible(!text.isEmpty());
    }

    public void setBounds(int x, int y) {
        textLabel.setBounds(0, 0, w_, h_);
        background.setBounds(0, 0, w_, h_);
        super.setBounds(x, y, w_, h_);
    }

    @Override
    public void setBounds(int x, int y, int w, int h) {
        textLabel.setBounds(0, 0, w_ = w, h_ = h);
        background.setBounds(0, 0, w, h);
        super.setBounds(x, y, w, h);
    }
}