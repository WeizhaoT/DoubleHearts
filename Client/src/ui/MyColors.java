package ui;

import java.awt.*;
import java.util.Random;

public class MyColors {
    public static final Color tableGreen = new Color(37, 93, 54);
    public static final Color text = new Color(230, 230, 230);
    public static final Color errmsg = new Color(255, 200, 235);

    public static final Color yellow = new Color(200, 200, 0);
    public static final Color green = new Color(0, 200, 50);
    public static final Color darkYellow = new Color(150, 150, 0);
    public static final Color darkBlue = new Color(30, 60, 100);
    public static final Color darkGray = new Color(110, 110, 110);
    public static final Color gray = new Color(210, 210, 210);

    public static final Color pink = new Color(200, 40, 230);
    public static final Color heartColor = new Color(224, 10, 40);
    public static final Color spadeColor = new Color(0, 50, 160);
    public static final Color diamondColor = new Color(200, 100, 30);
    public static final Color clubColor = new Color(0, 60, 30);

    public static Color randomColor() {
        final int r = (new Random()).nextInt(128) + 128;
        final int g = (new Random()).nextInt(128) + 128;
        final int b = (new Random()).nextInt(128) + 128;
        return new Color(r, g, b);
    }

    public static String getColoredText(final String text, final Color color) {
        if (text.length() == 0)
            return "";

        return "<font color=\"rgb(" + color.getRed() + ", " + color.getGreen() + ", " + color.getBlue() + ")\">" + text
                + "</font>";
    }

}