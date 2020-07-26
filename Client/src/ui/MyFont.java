package ui;

import java.awt.Font;

public class MyFont {
    public static final Font asset = new Font("Courier New", Font.BOLD, (int) Size.asset);
    public static final Font assetSymb = new Font("Courier New", Font.BOLD, (int) Size.asset);
    public static final Font rule = new Font("Courier New", Font.BOLD, (int) Size.rule);
    public static final Font score = new Font("Courier New", Font.BOLD, (int) Size.score);
    public static final Font smallScore = new Font("Courier New", Font.BOLD, (int) Size.smallScore);

    public static class Size {
        public static final float userInfo = 24.0f;
        public static final float score = 24.0f;
        public static final float smallScore = 18.0f;
        public static final float errMsg = 20.0f;
        public static final float userNameSmall = 12.0f;
        public static final float userNameLarge = 18.0f;
        public static final float asset = 20.0f;
        public static final float rule = 20.0f;
        public static final float button = 14.0f;
    }
}