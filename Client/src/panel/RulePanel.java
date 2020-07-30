package panel;

import ui.*;
import element.BackgroundRect;
import layout.PokerGameLayout;
import main.ClientController;

import java.awt.*;
import javax.swing.*;

public class RulePanel extends JPanel {
    static final long serialVersionUID = 1L;

    private static final int w_ = PokerGameLayout.rulew;
    private static final int h_ = PokerGameLayout.ruleh;

    private static final int insetScale = 5;
    private static final int texth = (h_ - 2 * insetScale) / 6;
    private static final int textlw = w_ / 2 - insetScale + 6;
    private static final int textrw = w_ / 2 - insetScale - 6;
    private static final int textwOverflow = 10;
    private static final int textGap = 7;
    private static final int splitterYOffset = 2;

    private final BackgroundRect background;

    public RulePanel() {
        setLayout(null);
        setOpaque(false);
        setPreferredSize(new Dimension(w_, h_));

        if (ClientController.TEST_MODE)
            setBorder(BorderFactory.createLineBorder(MyColors.green));

        background = new BackgroundRect(w_, h_, MyColors.lightYellow);
        background.setBounds(0, 0, w_, h_);
        add(background);

        JLabel title = new JLabel(MyText.getRuleTitle(), SwingConstants.CENTER);
        title.setForeground(MyColors.tableGreen);
        title.setBounds(insetScale, 0, textlw + textrw, texth);
        title.setVerticalAlignment(JLabel.TOP);
        add(title);

        int y = texth;
        JLabel transformer = new JLabel("\u2663T    \u00d72");
        transformer.setForeground(MyColors.clubColor);
        setLeftAxis(transformer, y);
        add(transformer);

        JLabel jHearts = new JLabel("\u2665J  -20");
        jHearts.setForeground(MyColors.heartColor);
        setRightAxis(jHearts, y);
        add(jHearts);

        y += texth;
        JLabel sheep = new JLabel("\u2666J  +100");
        sheep.setForeground(MyColors.diamondColor);
        setLeftAxis(sheep, y);
        add(sheep);

        JLabel qHearts = new JLabel("\u2665Q  -30");
        qHearts.setForeground(MyColors.heartColor);
        setRightAxis(qHearts, y);
        add(qHearts);

        y += texth;
        JLabel pig = new JLabel("\u2660Q  -100");
        pig.setForeground(MyColors.spadeColor);
        setLeftAxis(pig, y);
        add(pig);

        JLabel kHearts = new JLabel("\u2665K  -40");
        kHearts.setForeground(MyColors.heartColor);
        setRightAxis(kHearts, y);
        add(kHearts);

        y += texth;
        JLabel hearts1 = new JLabel("\u26652~4   0");
        hearts1.setForeground(MyColors.heartColor);
        setLeftAxis(hearts1, y);
        add(hearts1);

        JLabel aHearts = new JLabel("\u2665A  -50");
        aHearts.setForeground(MyColors.heartColor);
        setRightAxis(aHearts, y);
        add(aHearts);

        y += texth;
        JLabel hearts2 = new JLabel("\u26655~T -10");
        hearts2.setForeground(MyColors.heartColor);
        setLeftAxis(hearts2, y);
        add(hearts2);

        JLabel splitLabel = new JLabel();
        splitLabel.setBounds(insetScale + textlw - 1, texth + splitterYOffset, 2, h_ - texth - splitterYOffset);
        splitLabel.setBorder(BorderFactory.createLineBorder(MyColors.tableGreen, 5));
        add(splitLabel);

        splitLabel = new JLabel();
        splitLabel.setBounds(0, texth + splitterYOffset, w_, 2);
        splitLabel.setBorder(BorderFactory.createLineBorder(MyColors.tableGreen, 5));
        add(splitLabel);

        for (final Component comp : getComponents()) {
            comp.setFont(MyFont.rule);
        }

        title.setFont(MyText.getRuleTitleFont());

        setComponentZOrder(background, getComponentCount() - 1);
        revalidate();
        repaint();
        setVisible(true);
    }

    private void setLeftAxis(final JLabel item, int y) {
        item.setBounds(insetScale, y + insetScale, textlw + textwOverflow, texth);
    }

    private void setRightAxis(final JLabel item, int y) {
        item.setBounds(insetScale + textlw + textGap, y + insetScale, textrw - textGap + textwOverflow, texth);
    }
}