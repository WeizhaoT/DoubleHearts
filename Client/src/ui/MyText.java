package ui;

import main.ClientController;
import panel.HandPanel;

import java.awt.*;

public class MyText {
    public static int language = 1;
    public static final String CHN_FONT = "Microsoft YaHei UI";

    public static final String twoOfClubs = MyColors.getColoredText("\u26632", MyColors.clubColor);
    public static final String transformer = MyColors.getColoredText("\u2663T", MyColors.clubColor);
    public static final String sheep = MyColors.getColoredText("\u2666J", MyColors.diamondColor);
    public static final String pig = MyColors.getColoredText("\u2660Q", MyColors.spadeColor);
    public static final String hearts = MyColors.getColoredText("\u26655~A", MyColors.heartColor);

    public static final String oldEffectColor = "color=\"rgb((145, 135, 145))\"";
    public static final String transformerEffect = MyColors.getColoredText(
            "\u2663T(<font " + oldEffectColor + "><strike>\u00d72</strike></font> \u00d74)", MyColors.clubColor);
    public static final String sheepEffect = MyColors.getColoredText(
            "\u2666J(<font " + oldEffectColor + "><strike>+100</strike></font> +200)", MyColors.diamondColor);
    public static final String pigEffect = MyColors.getColoredText(
            "\u2660Q(<font " + oldEffectColor + "><strike>-100</strike></font> -200)", MyColors.spadeColor);

    public static final int NORMAL = 0;
    public static final int CARD_NUM_EXCESS = 1;
    public static final int CARD_NUM_MISMATCH = 2;
    public static final int ILLEGAL_FIRST_LEAD = 3;
    public static final int ILLEGAL_DOUBLE_LEAD = 4;
    public static final int BANNED_FIRST_ROUND_FOLLOW = 5;
    public static final int ILLEGAL_DISCARD = 6;
    public static final int ILLEGAL_PAIR_FOLLOW = 7;
    public static final int TRADE_NUM_MISMATCH = 8;
    public static final int ILLEGAL_SHOWING = 9;
    public static final int HINT_SHOWING = 10;

    public static final int NAME_BLANK = 1;
    public static final int NAME_TOO_LONG = 2;
    public static final int NAME_HAS_DILIMITER = 3;
    public static final int NAME_ENCODING = 4;

    private static final Font[] hintFonts = new Font[] {
            new Font(Font.SANS_SERIF, Font.BOLD, (int) MyFont.Size.userInfo),
            new Font(CHN_FONT, Font.PLAIN, (int) MyFont.Size.userInfo) };

    private static final Font[] sitButtonFonts = new Font[] {
            new Font(Font.SANS_SERIF, Font.BOLD, (int) MyFont.Size.smallButton),
            new Font(CHN_FONT, Font.BOLD, (int) MyFont.Size.smallButton) };

    private static final Font[] ruleTitleFonts = new Font[] {
            new Font(Font.MONOSPACED, Font.BOLD, (int) MyFont.Size.rule),
            new Font(CHN_FONT, Font.PLAIN, (int) MyFont.Size.smallRule) };

    private static final Font[] scoreTitleFonts = new Font[] {
            new Font(Font.MONOSPACED, Font.BOLD, (int) MyFont.Size.score),
            new Font(CHN_FONT, Font.BOLD, (int) MyFont.Size.score) };

    private static final Font[] errMsgFonts = new Font[] {
            new Font(Font.MONOSPACED, Font.BOLD, (int) MyFont.Size.errMsg),
            new Font(CHN_FONT, Font.BOLD, (int) MyFont.Size.errMsg) };

    private static final Font[] passHintFonts = new Font[] {
            new Font(Font.MONOSPACED, Font.BOLD, (int) MyFont.Size.smallErrMsg),
            new Font(CHN_FONT, Font.BOLD, (int) MyFont.Size.errMsg) };

    private static final Font[] buttonFonts = new Font[] {
            new Font(Font.SANS_SERIF, Font.BOLD, (int) MyFont.Size.button),
            new Font(CHN_FONT, Font.BOLD, (int) MyFont.Size.button) };

    private static final Font[] scoreFonts = new Font[] {
            new Font(Font.MONOSPACED, Font.BOLD, (int) MyFont.Size.smallScore),
            new Font(CHN_FONT, Font.BOLD, (int) MyFont.Size.smallScore) };

    private static final Font[] sectionFonts = new Font[] {
            new Font(Font.SANS_SERIF, Font.BOLD, (int) MyFont.Size.sectionText),
            new Font(CHN_FONT, Font.BOLD, (int) MyFont.Size.sectionText) };

    private static final String[] titles = new String[] { "Double Hearts", "拱猪牵羊" };
    private static final String[] sitButton = new String[] { "Sit", "坐下" };
    private static final String[] nameHint = new String[] { "Please Input Your Name:", "请输入用户名：" };
    private static final String[] avatarHint = new String[] { "Please Choose Your Avatar: ", "请选择头像：" };
    private static final String[] seatingHint = new String[] { "Failed to sit down", "无法选择此座位" };

    private static final String[] passingHint = new String[] { "Pass " + HandPanel.nTrade + " cards along arrows",
            "沿箭头传" + HandPanel.nTrade + "张牌" };
    private static final String[] readyLabels = new String[] { "READY", "开始" };
    private static final String[] passLabels = new String[] { "PASS", "不亮" };
    private static final String[] totalScoreLabels = new String[] { "Total: ", "总分：" };
    private static final String[] scoreLabels = new String[] { "Score: ", "本局分：" };
    private static final String[] ruleTitles = new String[] { "Base Effects", "基础效果" };
    private static final String[] scoreboardTitles = new String[] { "Scoreboard", "积分榜" };

    private static final String[] readyButton = new String[] { "READY", "准备" };
    private static final String[] playButton = new String[] { "PLAY", "出牌" };
    private static final String[] tradeButton = new String[] { "TRADE", "传牌" };
    private static final String[] showButton = new String[] { "SHOW", "亮牌" };
    private static final String[] passButton = new String[] { "PASS", "不亮" };
    private static final String[] lastRoundButton = new String[] { "Last Round", "上一轮" };

    private static final String[][] tableErrMsg = new String[][] {
            { "", "You must choose at most 2 cards to play", "You must choose the same number of cards to follow",
                    "You must lead the first round with only (double) " + twoOfClubs,
                    "You must lead two cards only if they are a pair",
                    "You cannot follow " + sheep + ", " + pig + ", or " + hearts
                            + " in the first round unless you have to",
                    "You must not discard when the leading suit is not empty",
                    "You must follow a pair when you have one in the leading suit",
                    "You must trade out exactly " + HandPanel.nTrade + " cards",
                    "You cannot show cards other than " + transformerEffect + ", " + sheepEffect + ", or " + pigEffect,
                    "You can show " + transformerEffect + ", " + sheepEffect + ", or " + pigEffect
                            + ". Shown cards will enjoy double effect." },
            { "", "最多只能打出2张牌", "跟牌数必须和领牌数一致", "第一轮领牌必须出单或双张" + twoOfClubs, "双张领牌必须为一对",
                    "非必要时，首轮不允许跟" + sheep + "，" + pig + "以及" + hearts, "领套非空时不可垫牌", "领套有对子时必须跟对",
                    "传牌数量必须为" + HandPanel.nTrade + "张",
                    "亮牌必须在" + transformerEffect + "、" + sheepEffect + "、或者" + pigEffect + "中选择", "选择亮出“变压器”"
                            + transformerEffect + "、“羊”" + sheepEffect + "、或者“猪”" + pigEffect + "的任意组合，亮出的牌效果翻倍" }, };

    private static final String[][] welcomeErrMsg = new String[][] {
            { "", "Name should NOT be empty or blank!", "Name should be at most 24-char long!",
                    "Name should NOT contain delimiters \"" + ClientController.RECV_DELIM + "\" or \""
                            + ClientController.SEND_DELIM + "\"!",
                    "Name is not encodable with Unicode" },
            { "", "用户名不能为空！", "用户名不可超过24字节！",
                    "用户名不可包含分隔符“" + ClientController.RECV_DELIM + "”或“" + ClientController.SEND_DELIM + "”！",
                    "用户名不可包含非Unicode字符" } };

    private static String[][] connErrMsg = new String[][] { { "Player \"", "\" has left the table" },
            { "玩家“", "”离开了牌桌" } };

    public static String getTitle() {
        return titles[language];
    }

    public static String getNameHint() {
        return nameHint[language];
    }

    public static String getAvatarHint() {
        return avatarHint[language];
    }

    public static String getSeatingHint() {
        return seatingHint[language];
    }

    public static String getWelcomeErrMsg(final int errCode) {
        return welcomeErrMsg[language][errCode];
    }

    public static String getPassingHint() {
        return passingHint[language];
    }

    public static String getSitButtonLabel() {
        return sitButton[language];
    }

    public static Font getSitButtonFont() {
        return sitButtonFonts[language];
    }

    public static Font getHintFont() {
        return hintFonts[language];
    }

    public static String getRuleTitle() {
        return ruleTitles[language];
    }

    public static Font getRuleTitleFont() {
        return ruleTitleFonts[language];
    }

    public static String getScoreboardTitle() {
        return scoreboardTitles[language];
    }

    public static Font getScoreboardTitleFont() {
        return scoreTitleFonts[language];
    }

    public static String getErrMsg(final int errCode) {
        return tableErrMsg[language][errCode];
    }

    public static Font getErrMsgFont() {
        return errMsgFonts[language];
    }

    public static Font getPassFont() {
        return passHintFonts[language];
    }

    public static String getReadyText() {
        return readyLabels[language];
    }

    public static String getPassText() {
        return passLabels[language];
    }

    public static Font getSecTextFont() {
        return sectionFonts[language];
    }

    public static String getSectionFont() {
        return passLabels[language];
    }

    public static String getTotalScoreText() {
        return totalScoreLabels[language];
    }

    public static String getScoreText() {
        return scoreLabels[language];
    }

    public static Font getScoreFont() {
        return scoreFonts[language];
    }

    public static String getButtonLabel(final String buttonName) {
        switch (buttonName) {
            case "PLAY":
                return playButton[language];
            case "READY":
                return readyButton[language];
            case "SHOW":
                return showButton[language];
            case "PASS":
                return passButton[language];
            case "TRADE":
                return tradeButton[language];
            case "LAST ROUND":
                return lastRoundButton[language];
            default:
                return "";
        }
    }

    public static Font getButtonFont() {
        return buttonFonts[language];
    }

    public static String getConnErrMsg(final String name) {
        return connErrMsg[language][0] + name + connErrMsg[language][1];
    }
}