package panel;

import ui.*;
import element.BackgroundRect;
import layout.PokerGameLayout;
import main.ClientController;

import javax.swing.*;

public class LegendPanel extends JPanel {
    static final long serialVersionUID = 1L;

    private static final int w_ = PokerGameLayout.rulew;
    private static final int h_ = PokerGameLayout.ruleh;

    private static final String plug = "X";

    private static final int insetScale = 5;
    private static final int texth = (h_ - 2 * insetScale) / 6;
    private static final int textlw = 50 - insetScale + 6;
    private static final int textrw = w_ - 2 * insetScale - textlw;
    private static final int textwOverflow = 10;
    private static final int addhorizontalInset = 20;
    private static final int splitterYOffset = 2;

    private final BackgroundRect background;

    public LegendPanel() {
        setLayout(null);
        setOpaque(false);

        if (ClientController.TEST_MODE >= 2)
            setBorder(BorderFactory.createLineBorder(MyColors.green));

        background = new BackgroundRect(w_, h_, MyColors.lightYellow);
        background.setBounds(0, 0, w_, h_);
        add(background);

        JLabel title = new JLabel(MyText.getLegendTitle(), SwingConstants.CENTER);
        title.setForeground(MyColors.tableGreen);
        title.setBounds(insetScale, 0, textlw + textrw, texth);
        title.setVerticalAlignment(JLabel.TOP);
        add(title);

        JLabel splitLabel = new JLabel();
        splitLabel.setBounds(0, texth + splitterYOffset, w_, 2);
        splitLabel.setBorder(BorderFactory.createLineBorder(MyColors.tableGreen, 5));
        add(splitLabel);

        int y = texth;
        JLabel originalX = new JLabel("<html>\u2665" + plug + "</html>");
        originalX.setForeground(MyColors.heartColor);
        originalX.setHorizontalAlignment(SwingConstants.LEFT);
        originalX.setFont(MyFont.rule);
        setLeftAxis(originalX, y);
        add(originalX);

        JLabel originalLegend = new JLabel(MyText.getLegend("base"));
        originalLegend.setForeground(MyColors.tableGreen);
        originalLegend.setHorizontalAlignment(SwingConstants.RIGHT);
        originalLegend.setFont(MyText.getLegendFont());
        setRightAxis(originalLegend, y);
        add(originalLegend);

        y += texth;
        JLabel doubleX = new JLabel(
                "<html>" + MyColors.getColoredText("\u2665", MyColors.heartColor) + plug + "</html>");
        doubleX.setForeground(MyColors.doubled);
        doubleX.setHorizontalAlignment(SwingConstants.LEFT);
        doubleX.setFont(MyFont.rule);
        setLeftAxis(doubleX, y);
        add(doubleX);

        JLabel doubleLegend = new JLabel(MyText.getLegend("2x"));
        doubleLegend.setForeground(MyColors.tableGreen);
        doubleLegend.setHorizontalAlignment(SwingConstants.RIGHT);
        doubleLegend.setFont(MyText.getLegendFont());
        setRightAxis(doubleLegend, y);
        add(doubleLegend);

        y += texth;
        JLabel quadrupleX = new JLabel(
                "<html>" + MyColors.getColoredText("\u2665", MyColors.heartColor) + plug + "</html>");
        quadrupleX.setForeground(MyColors.quadrupled);
        quadrupleX.setHorizontalAlignment(SwingConstants.LEFT);
        quadrupleX.setFont(MyFont.rule);
        setLeftAxis(quadrupleX, y);
        add(quadrupleX);

        JLabel quadrupleLegend = new JLabel(MyText.getLegend("4x"));
        quadrupleLegend.setForeground(MyColors.tableGreen);
        quadrupleLegend.setHorizontalAlignment(SwingConstants.RIGHT);
        quadrupleLegend.setFont(MyText.getLegendFont());
        setRightAxis(quadrupleLegend, y);
        add(quadrupleLegend);

        title.setFont(MyText.getRuleTitleFont());

        setComponentZOrder(background, getComponentCount() - 1);
        revalidate();
        repaint();
        setVisible(false);
    }

    private void setLeftAxis(final JLabel item, int y) {
        item.setBounds(insetScale + addhorizontalInset, y + insetScale, textlw + textwOverflow, texth);
    }

    private void setRightAxis(final JLabel item, int y) {
        item.setBounds(textlw - textwOverflow + insetScale - addhorizontalInset, y + insetScale, textrw + textwOverflow,
                texth);
    }
}