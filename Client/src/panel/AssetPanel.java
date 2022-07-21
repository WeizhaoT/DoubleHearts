package panel;

import ui.*;
import rule.*;
import layout.PokerGameLayout;
import element.BackgroundRect;
import main.ClientController;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.stream.IntStream;

import javax.swing.*;

public class AssetPanel extends JPanel {
    static final long serialVersionUID = 1L;

    private static final int w_ = PokerGameLayout.assetw;
    private static final int h_ = PokerGameLayout.asseth;

    private static final int symbolw = 16;
    private static final int insetScale = 5;
    private static final int symbYOffset = 0;
    private static final int litgap = 10;
    private static final int texth = 30;
    private static final int textw = w_ - 2 * insetScale;
    private static final int lith = 5 * texth + 2 * insetScale;
    private static final int y0 = (h_ - texth - lith - litgap) / 2;
    private static final int lity = y0 + litgap + texth;
    private static final int exposureGap = 6;
    private static final int exposureOverflow = 30;
    private static final int exposureSymbolYOffset = -3;

    private static final int heartx = 66;

    private static final String[] ranks = { "A", "K", "Q", "J", "T", "9", "8", "7", "6", "5", "4", "3", "2" };

    private int numDecks = 2;
    private int score;

    private final JLabel scoreLabel;

    private final JPanel literalPanel;
    private final BackgroundRect literalBG;

    private JLabel heartSymbol;
    private JLabel spadeSymbol;
    private JLabel diamondSymbol;
    private JLabel clubSymbol;
    private JLabel exposureSymbol;

    private JLabel heartAssetLiterals;
    private JLabel spadeAssetLiterals;
    private JLabel diamondAssetLiterals;
    private JLabel clubAssetLiterals;
    private JLabel exposureLiterals;

    private final ArrayList<Card> assets;

    public AssetPanel() {
        setLayout(null);
        setOpaque(false);
        setPreferredSize(new Dimension(w_, h_));
        setBackground(MyColors.tableGreen);

        if (ClientController.TEST_MODE >= 2)
            setBorder(BorderFactory.createLineBorder(MyColors.green));

        scoreLabel = new JLabel("", SwingConstants.CENTER);
        scoreLabel.setText("<html>" + MyText.getScoreText() + "<font face=\"Courier new\">0</font></html>");
        scoreLabel.setFont(MyText.getScoreFont());
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

        score = 0;
        assets = new ArrayList<>();
        setupLiteralPanel();
        showChanges();
    }

    public void setNumDecks(int numDecks) {
        this.numDecks = numDecks;
    }

    private void setupLiteralPanel() {
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

        exposureSymbol = new JLabel("\u2600");
        exposureSymbol.setForeground(MyColors.quadrupled);
        exposureSymbol.setFont(new Font(Font.MONOSPACED, Font.BOLD, (int) MyFont.Size.asset));
        setAxis(exposureSymbol, 0, asseth + exposureSymbolYOffset, symbolw + exposureOverflow, texth);
        literalPanel.add(exposureSymbol);

        exposureLiterals = new JLabel("");
        setAxis(exposureLiterals, symbolw + exposureGap, asseth, textw - symbolw + exposureOverflow);
        literalPanel.add(exposureLiterals);

        for (final Component comp : literalPanel.getComponents()) {
            if (comp.getName() != null && comp.getName().contains("symb_"))
                comp.setFont(MyFont.assetSymb);
            else if (comp != exposureSymbol)
                comp.setFont(MyFont.asset);

            ((JLabel) comp).setVerticalAlignment(JLabel.TOP);
        }
    }

    public void reset() {
        score = 0;
        assets.clear();
        scoreLabel.setText("<html>" + MyText.getScoreText() + "<font face=\"Courier new\">0</font></html>");

        clubAssetLiterals.setText("");
        diamondAssetLiterals.setText("");
        spadeAssetLiterals.setText("");
        heartAssetLiterals.setText("");
        exposureLiterals.setText("");

        showChanges();
    }

    private void fillEverything() {
        String[] postfixes = new String[] { "", "x", "z" };
        for (final String rank : ranks) {
            addAsset(rank + "H" + postfixes[(new Random()).nextInt(3)]);
            addAsset(rank + "H" + postfixes[(new Random()).nextInt(3)]);
        }
        addAsset(Card.TRANS + postfixes[(new Random()).nextInt(3)]);
        addAsset(Card.TRANS + postfixes[(new Random()).nextInt(3)]);
        addAsset(Card.SHEEP + postfixes[(new Random()).nextInt(3)]);
        addAsset(Card.SHEEP + postfixes[(new Random()).nextInt(3)]);
        addAsset(Card.PIG + postfixes[(new Random()).nextInt(3)]);
        addAsset(Card.PIG + postfixes[(new Random()).nextInt(3)]);
        setExposed(new String[] { "QSz", "QSz", "JDx", "JDx", "TCx", "TCx", "AH", "AH" });
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
        scoreLabel.setText("<html>" + MyText.getScoreText() + "<font face=\"Courier new\">" + score + "</font></html>");
        showChanges();
    }

    public void setExposed(final String[] aliases) {
        if (aliases.length == 0) {
            exposureLiterals.setText("");
            return;
        }

        final String space = "<font size=\"2\"> </font>";

        int nc = 0, nd = 0, ns = 0, nh = 0;
        for (final String alias : aliases) {
            if (alias.startsWith(Card.TRANS))
                nc++;
            else if (alias.startsWith(Card.SHEEP))
                nd++;
            else if (alias.startsWith(Card.PIG))
                ns++;
            else if (alias.startsWith(Card.ACEH))
                nh++;
        }

        final String clubs = nc == 0 ? "" : getColoredLiteral("C", nc, true) + space;
        final String diamonds = nd == 0 ? "" : getColoredLiteral("D", nd, true) + space;
        final String spades = ns == 0 ? "" : getColoredLiteral("S", ns, true) + space;
        final String hearts = nh == 0 ? "" : getColoredLiteral("H", nh, true);

        exposureLiterals.setText("<html>" + clubs + diamonds + spades + hearts + "</html>");
    }

    public int getScore() {
        return score;
    }

    private int updatePanel() {
        return updatePanel(this.numDecks);
    }

    private int updatePanel(final int numDecks) {
        int total = 0;
        int heartScore = 0, pigScore = 0, sheepScore = 0, transScore = 0;

        int numTrans[] = new int[] { 0, 0, 0 };
        int numSheep[] = new int[] { 0, 0, 0 };
        int numPig[] = new int[] { 0, 0, 0 };

        int numHearts = 0;

        final int assetSize = assets.size();

        final HashMap<String, Integer> heartHist = new HashMap<>();
        final ArrayList<Card> transformers = new ArrayList<>();

        for (final Card card : assets) {
            if (card.isHeart()) {
                numHearts++;
                heartScore += card.value();
                heartHist.compute(card.fullAlias(), (k, v) -> v == null ? 1 : v + 1);
            } else if (card.isPig()) {
                numPig[card.exposed]++;
                pigScore += card.value();
            } else if (card.isSheep()) {
                numSheep[card.exposed]++;
                sheepScore += card.value();
            } else if (card.isTransformer()) {
                transScore += card.value();
                transformers.add(card);
                numTrans[card.exposed]++;
            }
        }

        clubAssetLiterals.setText(getColoredLiteral("C", numTrans));
        diamondAssetLiterals.setText(getColoredLiteral("D", numSheep));
        spadeAssetLiterals.setText(getColoredLiteral("S", numPig));
        heartAssetLiterals.setText(getHeartLiteral(heartHist));

        if (assetSize == 0)
            return 0;
        if (assetSize == IntStream.of(numTrans).sum())
            return transScore;

        if (numHearts == 13 * numDecks) {
            heartScore = -heartScore;
            pigScore = assetSize == 16 * numDecks ? -pigScore : pigScore;
        }

        total = (int) Math.round((heartScore + pigScore + sheepScore) * Card.getMult(transformers));
        return total;
    }

    private static String getColoredLiteral(final String suit, final int num, final boolean withSymbol) {
        return getColoredLiteral(suit, new int[] { num, 0, 0 }, withSymbol);
    }

    private static String getColoredLiteral(final String suit, final int[] nums) {
        return getColoredLiteral(suit, nums, false);
    }

    private static String getColoredLiteral(final String suit, final int[] nums, final boolean withSymbol) {
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
        } else if (suit.equals("C")) {
            orig = "T";
            symb = "\u2663";
            color = MyColors.clubColor;
        } else {
            orig = "A";
            symb = "\u2665";
            color = MyColors.heartColor;
        }

        final String quadrupled = MyColors.getColoredText(orig.repeat(nums[2]), MyColors.quadrupled);
        final String doubled = MyColors.getColoredText(orig.repeat(nums[1]), MyColors.doubled);
        final String normal = MyColors.getColoredText(orig.repeat(nums[0]), color);

        if (withSymbol)
            return MyColors.getColoredText(symb + orig.repeat(nums[0]), color);
        else
            return "<html>" + normal + doubled + quadrupled + "</html>";
    }

    private String getHeartLiteral(final HashMap<String, Integer> heartHist) {
        final ArrayList<String> literals = new ArrayList<>();

        for (final String rank : ranks) {
            String literal = "";
            Integer count;

            for (String postfix : new String[] { "", "x", "z" }) {
                if ((count = heartHist.get(rank + "H" + postfix)) != null) {
                    literal += MyColors.getColoredText(rank.repeat(count), postfix == "" ? MyColors.heartColor
                            : postfix == "x" ? MyColors.doubled : MyColors.quadrupled);
                }
            }
            if (!literal.isBlank())
                literals.add(literal);
        }
        return "<html>" + String.join(" ", literals) + "</html>";
    }

    public void showChanges() {
        revalidate();
        repaint();
        setVisible(true);
    }
}