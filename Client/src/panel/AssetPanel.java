package panel;

import ui.*;
import rule.*;
import layout.PokerGameLayout;
import element.BackgroundRect;
import main.ClientController;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.*;

public class AssetPanel extends JPanel {
    static final long serialVersionUID = 1L;

    private static final int w_ = PokerGameLayout.assetw;
    private static final int h_ = PokerGameLayout.asseth;

    private static final int symbolw = 16;
    private static final int emojiw = 28;
    private static final int emojiRealw = 18;
    private static final int symbolWidew = 32;
    private static final int insetScale = 5;
    private static final int symbYOffset = 0;
    private static final int litgap = 10;
    private static final int texth = 30;
    private static final int textw = w_ - 2 * insetScale;
    private static final int lith = 5 * texth + 2 * insetScale;
    private static final int y0 = (h_ - texth - lith - litgap) / 2;
    private static final int lity = y0 + litgap + texth;

    private static final int heartx = 66;

    private static final String[] ranks = { "A", "K", "Q", "J", "T", "9", "8", "7", "6", "5", "4", "3", "2" };

    private int score;

    private final JLabel scoreLabel;

    private final JPanel literalPanel;
    private final BackgroundRect literalBG;

    private JLabel heartSymbol;
    private JLabel spadeSymbol;
    private JLabel diamondSymbol;
    private JLabel clubSymbol;
    private JLabel doubleSymbol1;
    private JLabel doubleSymbol2;

    private JLabel heartAssetLiterals;
    private JLabel spadeAssetLiterals;
    private JLabel diamondAssetLiterals;
    private JLabel clubAssetLiterals;
    private JLabel doubleLiterals;

    private final ArrayList<Card> assets;

    public AssetPanel() {
        setLayout(null);
        setOpaque(false);
        setPreferredSize(new Dimension(w_, h_));
        setBackground(MyColors.tableGreen);

        if (ClientController.TEST_MODE)
            setBorder(BorderFactory.createLineBorder(MyColors.green));

        scoreLabel = new JLabel("Score: 0", SwingConstants.CENTER);
        scoreLabel.setFont(MyFont.smallScore);
        scoreLabel.setForeground(MyColors.text);
        setAxis(scoreLabel, 0, y0, textw);
        add(scoreLabel);

        literalPanel = new JPanel(null);
        literalPanel.setOpaque(false);
        literalPanel.setBounds(0, lity, w_, lith);
        add(literalPanel);

        literalBG = new BackgroundRect(w_, lith);
        literalBG.setBounds(literalPanel.getBounds());
        add(literalBG);

        setComponentZOrder(literalBG, 1);
        setComponentZOrder(literalPanel, 0);

        assets = new ArrayList<>();
        score = 0;
        reset();
    }

    public void reset() {
        literalPanel.removeAll();

        assets.clear();
        scoreLabel.setText("Score: 0");

        int y = 0;
        clubSymbol = new JLabel("\u2663");
        clubSymbol.setName("symb_c");
        clubSymbol.setForeground(MyColors.clubColor);
        setAxis(clubSymbol, 0, y);
        literalPanel.add(clubSymbol);

        clubAssetLiterals = new JLabel("");
        setAxis(clubAssetLiterals, symbolw, y, textw - symbolw);
        literalPanel.add(clubAssetLiterals);

        y += texth;
        diamondSymbol = new JLabel("\u2666");
        diamondSymbol.setName("symb_d");
        diamondSymbol.setForeground(MyColors.diamondColor);
        setAxis(diamondSymbol, 0, y);
        literalPanel.add(diamondSymbol);

        diamondAssetLiterals = new JLabel("");
        setAxis(diamondAssetLiterals, symbolw, y, textw - symbolw);
        literalPanel.add(diamondAssetLiterals);

        y += texth;
        spadeSymbol = new JLabel("\u2660");
        spadeSymbol.setName("symb_s");
        spadeSymbol.setForeground(MyColors.spadeColor);
        setAxis(spadeSymbol, 0, y);
        literalPanel.add(spadeSymbol);

        spadeAssetLiterals = new JLabel("");
        setAxis(spadeAssetLiterals, symbolw, y, textw - symbolw);
        literalPanel.add(spadeAssetLiterals);

        y -= 2 * texth;
        heartSymbol = new JLabel("\u2665");
        heartSymbol.setName("symb_h");
        heartSymbol.setForeground(MyColors.heartColor);
        setAxis(heartSymbol, heartx, y);
        literalPanel.add(heartSymbol);

        heartAssetLiterals = new JLabel("");
        setAxis(heartAssetLiterals, heartx + symbolw, y, textw - heartx - symbolw, 4 * texth);
        literalPanel.add(heartAssetLiterals);

        final int asseth = 4 * texth + insetScale;

        JLabel splitLabel = new JLabel();
        splitLabel.setOpaque(false);
        splitLabel.setBounds(heartx - 3, 0, 3, asseth);
        splitLabel.setBorder(BorderFactory.createLineBorder(MyColors.tableGreen, 5));
        literalPanel.add(splitLabel);

        splitLabel = new JLabel();
        splitLabel.setOpaque(false);
        splitLabel.setBounds(0, asseth - 2, w_, 3);
        splitLabel.setBorder(BorderFactory.createLineBorder(MyColors.tableGreen, 5));
        literalPanel.add(splitLabel);

        doubleSymbol1 = new JLabel("<html><font face=\"Arial\">\ud83d\uddf2 </font></html>");
        doubleSymbol1.setName("symb_2x_1");
        doubleSymbol1.setForeground(MyColors.darkYellow);
        setAxis(doubleSymbol1, 0, asseth, emojiw);
        literalPanel.add(doubleSymbol1);

        doubleSymbol2 = new JLabel("\u00d72");
        doubleSymbol2.setName("symb_2x_2");
        doubleSymbol2.setForeground(MyColors.darkYellow);
        setAxis(doubleSymbol2, emojiRealw, asseth, symbolWidew);
        literalPanel.add(doubleSymbol2);

        doubleLiterals = new JLabel("");
        setAxis(doubleLiterals, symbolWidew + emojiRealw, asseth, textw - symbolWidew - emojiRealw);
        literalPanel.add(doubleLiterals);

        for (final Component comp : literalPanel.getComponents()) {
            if (comp.getName() != null && comp.getName().substring(0, 5).equals("symb_"))
                comp.setFont(MyFont.assetSymb);
            else
                comp.setFont(MyFont.asset);

            ((JLabel) comp).setVerticalAlignment(JLabel.TOP);
        }

        showChanges();
    }

    private void fillEverything() {
        for (final String rank : ranks) {
            addAsset(rank + "H");
            addAsset(rank + "H");
        }
        addAsset("TC");
        addAsset("TC");
        addAsset("JD");
        addAsset("JD");
        addAsset("QS");
        addAsset("QSx");
        setExhibition(new String[] { "QSx", "QSx", "JDx", "JDx", "TCx", "TCx" });
    }

    private void setAxis(final JLabel item, final int x, int y, final int... scale) {
        final int width = scale.length == 0 ? symbolw : scale[0];
        final int height = scale.length <= 1 ? texth : scale[1];
        y = scale.length == 0 ? y + symbYOffset : y;
        item.setBounds(x + insetScale, y + insetScale, width, height);
    }

    public void addAsset(final String alias) {
        assets.add(new Card(alias));
        score = updatePanel();
        scoreLabel.setText("Score: " + score);
        showChanges();
    }

    public void setExhibition(final String[] aliases) {
        if (aliases.length == 0) {
            doubleLiterals.setText("");
            return;
        }

        int nc = 0, nd = 0, ns = 0;
        for (final String alias : aliases) {
            if ("TCx".equals(alias))
                nc++;
            else if ("JDx".equals(alias))
                nd++;
            else if ("QSx".equals(alias))
                ns++;
        }

        final String clubs = nc == 0 ? "" : getColoredLiteral("C", 0, nc, true) + " ";
        final String diamonds = nd == 0 ? "" : getColoredLiteral("D", 0, nd, true) + " ";
        final String spades = ns == 0 ? "" : getColoredLiteral("S", 0, ns, true);

        doubleLiterals.setText("<html>" + clubs + diamonds + spades + "</html>");
    }

    public int getScore() {
        return score;
    }

    private int updatePanel() {
        return updatePanel(2);
    }

    private int updatePanel(final int numDecks) {
        int heartScore = 0;
        int pigScore = 0;

        int numTrans = 0, numTransx = 0;
        int numSheep = 0, numSheepx = 0;
        int numPig = 0, numPigx = 0;

        int numHearts = 0;

        final int assetSize = assets.size();

        final HashMap<String, Integer> heartHist = new HashMap<>();

        for (final Card card : assets) {
            if (card.isHeart()) {
                numHearts += 1;
                heartScore += card.value();
                heartHist.compute(card.alias().substring(0, 1), (k, v) -> v == null ? 1 : v + 1);
            } else if (card.isPig()) {
                if (card.bid)
                    numPigx += 1;
                else
                    numPig += 1;
            } else if (card.isSheep()) {
                if (card.bid)
                    numSheepx += 1;
                else
                    numSheep += 1;
            } else if (card.isTransformer()) {
                if (card.bid)
                    numTransx += 1;
                else
                    numTrans += 1;
            }
        }

        clubAssetLiterals.setText(getColoredLiteral("C", numTransx, numTrans));
        diamondAssetLiterals.setText(getColoredLiteral("D", numSheepx, numSheep));
        spadeAssetLiterals.setText(getColoredLiteral("S", numPigx, numPig));
        heartAssetLiterals.setText(getHeartLiteral(heartHist));

        if (assetSize == 0)
            return 0;
        if (assetSize == numTrans + numTransx)
            return numTrans * 50 + numTransx * 100;

        pigScore = -numPig * 100 - numPigx * 200;

        if (numHearts == 13 * numDecks) {
            heartScore = -heartScore;
            pigScore = assetSize == 16 * numDecks ? -pigScore : pigScore;
        }

        heartScore += pigScore + numSheep * 100 + numSheepx * 200;
        heartScore <<= (numTrans + numTransx * 2);

        showChanges();
        return heartScore;
    }

    private String getColoredLiteral(final String suit, final int nSpecial, final int nNormal) {
        return getColoredLiteral(suit, nSpecial, nNormal, false);
    }

    private String getColoredLiteral(final String suit, final int nSpecial, final int nNormal,
            final boolean withSymbol) {
        String orig, symb;
        Color color;

        if (suit.equals("S")) {
            orig = "Q";
            symb = "\u2660";
            color = MyColors.spadeColor;
        } else if (suit.equals("D")) {
            orig = "J";
            symb = "\u2666";
            color = MyColors.diamondColor;
        } else {
            orig = "T";
            symb = "\u2663";
            color = MyColors.clubColor;
        }

        final String special = MyColors.getColoredText(orig.repeat(nSpecial), MyColors.pink);
        final String normal = MyColors.getColoredText(orig.repeat(nNormal), color);

        if (withSymbol)
            return MyColors.getColoredText(symb + orig.repeat(nNormal), color);
        else
            return "<html>" + normal + special + "</html>";
    }

    private String getHeartLiteral(final HashMap<String, Integer> heartHist) {
        final ArrayList<String> literals = new ArrayList<>();

        for (final String rank : ranks) {
            if (!heartHist.containsKey(rank))
                continue;

            final int count = heartHist.get(rank);
            literals.add(rank.repeat(count));
        }

        return "<html>" + MyColors.getColoredText(String.join(" ", literals), MyColors.heartColor) + "</html>";
    }

    public void showChanges() {
        revalidate();
        repaint();
        setVisible(true);
    }

}