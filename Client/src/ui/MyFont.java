package ui;

import java.io.*;
import java.awt.*;

public class MyFont {
    public static final Font nameField = new Font(MyText.CHN_FONT, Font.PLAIN, (int) Size.userInfo);
    public static final Font seatName = new Font(MyText.CHN_FONT, Font.PLAIN, (int) Size.userNameSmall);
    public static final Font playerName = new Font(MyText.CHN_FONT, Font.PLAIN, (int) Size.userNameLarge);

    public static final Font asset = new Font("Courier New", Font.BOLD, (int) Size.asset);
    public static final Font assetSymb = new Font("Courier New", Font.BOLD, (int) Size.asset);
    public static final Font rule = new Font("Courier New", Font.BOLD, (int) Size.rule);
    public static final Font errMsg = new Font(Font.MONOSPACED, Font.BOLD, (int) Size.errMsg);
    public static final Font pass = new Font(Font.MONOSPACED, Font.BOLD, (int) Size.pass);
    public static final Font score = new Font(Font.MONOSPACED, Font.BOLD, (int) Size.score);
    public static final Font smallScore = new Font(Font.MONOSPACED, Font.BOLD, (int) Size.smallScore);

    public static Font clock;

    public static class Size {
        public static final float userInfo = 24.0f;
        public static final float score = 24.0f;
        public static final float smallScore = 18.0f;
        public static final float errMsg = 20.0f;
        public static final float smallErrMsg = 19.0f;
        public static final float pass = 20.0f;
        public static final float userNameSmall = 12.0f;
        public static final float userNameLarge = 18.0f;
        public static final float asset = 20.0f;
        public static final float rule = 19.0f;
        public static final float smallRule = 17.0f;
        public static final float button = 20.0f;
        public static final float smallButton = 20.0f;
        public static final float sectionText = 28.0f;
    }

    public static void registerFont() {
        for (String fontName : new String[] { "digital-7.mono", "MicrosoftYaHeiUI", "MicrosoftYaHeiUI-Bold" }) {
            try {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(Font.createFont(Font.TRUETYPE_FONT,
                        Thread.currentThread().getContextClassLoader().getResourceAsStream("ui/" + fontName + ".ttf")));
            } catch (IOException | FontFormatException e) {
                System.err.println("Error: failed to load font \"" + fontName + "\"");
            }
        }
        clock = new Font("Digital-7 Mono", Font.PLAIN, 48);
    }
}