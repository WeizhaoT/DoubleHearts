package layout;

import java.awt.*;

public class PokerTableLayout implements LayoutManager2 {
    public static final String NSEC = "Nsec";
    public static final String SSEC = "Ssec";
    public static final String WSEC = "Wsec";
    public static final String ESEC = "Esec";
    public static final String[] ALLSECS = new String[] { SSEC, ESEC, NSEC, WSEC };

    public static final String HLABEL = "Hlabel";
    public static final String HBG = "Hbg";
    public static final String SLABEL = "Slabel";
    public static final String SBG = "Sbg";
    public static final String PLABEL = "Plabel";
    public static final String PBG = "Pbg";

    public static final String ARROW = "Arrow";

    public static final int secWidth = 700;
    public static final int secHeight = 180;
    public static final int scoreWidth = 600;
    public static final int scoreHeight = 300;
    public static final int helperWidth = 360;
    public static final int helperHeight = 120;
    public static final int passWidth = 180;
    public static final int passHeight = 80;

    public static final int roundArrowScale = 180;
    public static final int arrowHalfWidth = 23;
    public static final int NSArrowLen = 360;
    public static final int EWArrowLen = 600;
    public static final int NSHalfGap = 100;
    public static final int EWHalfGap = 60;

    public static final int minWidth = 700;
    public static final int minHeight = 2 * secHeight;
    public static final Dimension minDim = new Dimension(minWidth, minHeight);

    Component northSection;
    Component southSection;
    Component eastSection;
    Component westSection;

    Component helperLabel;
    Component helperBackground;

    Component scorePanel;
    Component scoreBackground;

    Component passLabel;
    Component passBackground;

    Component[][] arrows = new Component[4][4];

    public PokerTableLayout() {

    }

    public void addLayoutComponent(final Component comp, final Object constraints) {
        if (constraints == null)
            throw new IllegalArgumentException("cannot add to layout: constraint must not be null");

        synchronized (comp.getTreeLock()) {
            if (constraints instanceof String) {
                addLayoutComponent((String) constraints, comp);
            } else {
                throw new IllegalArgumentException("cannot add to layout: constraint must be a string");
            }
        }
    }

    public void addLayoutComponent(final String name, final Component comp) {
        if (NSEC.equals(name)) {
            northSection = comp;
        } else if (SSEC.equals(name)) {
            southSection = comp;
        } else if (ESEC.equals(name)) {
            eastSection = comp;
        } else if (WSEC.equals(name)) {
            westSection = comp;
        } else if (SBG.equals(name)) {
            scoreBackground = comp;
        } else if (SLABEL.equals(name)) {
            scorePanel = comp;
        } else if (HBG.equals(name)) {
            helperBackground = comp;
        } else if (HLABEL.equals(name)) {
            helperLabel = comp;
        } else if (PBG.equals(name)) {
            passBackground = comp;
        } else if (PLABEL.equals(name)) {
            passLabel = comp;
        } else if (name.startsWith(ARROW)) {
            final int p = ARROW.length();
            try {
                final int a = Integer.parseInt(name.substring(p, p + 1));
                final int b = Integer.parseInt(name.substring(p + 1, p + 2));
                arrows[a][b] = comp;
            } catch (final NumberFormatException e) {
                throw new IllegalArgumentException(
                        "cannot add to layout: avatar index \"" + name.substring(p, p + 2) + "\" not identified");
            } catch (final ArrayIndexOutOfBoundsException e) {
                throw new IllegalArgumentException(
                        "cannot add to layout: avatar index \"" + name.substring(p, p + 2) + "\" out of bounds");
            }
        } else
            throw new IllegalArgumentException("cannot add to layout: constraint \"" + name + "\" not identified");
    }

    public float getLayoutAlignmentX(final Container container) {
        return 0.5f;
    }

    public float getLayoutAlignmentY(final Container container) {
        return 0.5f;
    }

    public void removeLayoutComponent(final Component comp) {

    }

    public void invalidateLayout(final Container container) {

    }

    public void layoutContainer(final Container target) {
        synchronized (target.getTreeLock()) {
            final Insets insets = target.getInsets();
            final int height = target.getHeight();
            final int width = target.getWidth();

            final int top = insets.top;
            final int bottom = height - insets.bottom;
            final int left = insets.left;
            final int right = width - insets.right;

            int zorder = target.getComponentCount() - 1;

            if (westSection != null) {
                westSection.setBounds(left, (height - secHeight) / 2, secWidth, secHeight);
                target.setComponentZOrder(westSection, zorder--);
            }
            if (eastSection != null) {
                eastSection.setBounds(right - secWidth, (height - secHeight) / 2, secWidth, secHeight);
                target.setComponentZOrder(eastSection, zorder--);
            }
            if (northSection != null) {
                northSection.setBounds((width - secWidth) / 2, top, secWidth, secHeight);
                target.setComponentZOrder(northSection, zorder--);
            }
            if (southSection != null) {
                southSection.setBounds((width - secWidth) / 2, bottom - secHeight, secWidth, secHeight);
                target.setComponentZOrder(southSection, zorder--);
            }
            if (scoreBackground != null) {
                scoreBackground.setBounds((width - scoreWidth) / 2, (height - scoreHeight) / 2, scoreWidth,
                        scoreHeight);
                target.setComponentZOrder(scoreBackground, zorder--);
            }
            if (scorePanel != null) {
                scorePanel.setBounds((width - scoreWidth) / 2, (height - scoreHeight) / 2, scoreWidth, scoreHeight);
                target.setComponentZOrder(scorePanel, zorder--);
            }

            final int x1 = width / 2 - roundArrowScale, x2 = width / 2;
            final int y1 = height / 2 - roundArrowScale, y2 = height / 2;

            final int[][] xs = new int[][] { { 0, 0, 0, 0 }, { x2, x2, x1, x1 }, { x2 + NSHalfGap - arrowHalfWidth,
                    x2 - EWArrowLen / 2, x2 - NSHalfGap - arrowHalfWidth, x2 - EWArrowLen / 2 }, { x1, x2, x2, x1 } };

            final int[][] ys = new int[][] { { 0, 0, 0, 0 }, { y2, y1, y1, y2 }, { y2 - NSArrowLen / 2,
                    y2 - EWHalfGap - arrowHalfWidth, y2 - NSArrowLen / 2, y2 + EWHalfGap - arrowHalfWidth },
                    { y2, y2, y1, y1 } };

            final int[][] ws = new int[][] { { 0, 0, 0, 0 },
                    { roundArrowScale, roundArrowScale, roundArrowScale, roundArrowScale },
                    { 2 * arrowHalfWidth + 1, EWArrowLen, 2 * arrowHalfWidth + 1, EWArrowLen },
                    { roundArrowScale, roundArrowScale, roundArrowScale, roundArrowScale } };

            final int[][] hs = new int[][] { { 0, 0, 0, 0 },
                    { roundArrowScale, roundArrowScale, roundArrowScale, roundArrowScale },
                    { NSArrowLen, 2 * arrowHalfWidth + 1, NSArrowLen, 2 * arrowHalfWidth + 1 },
                    { roundArrowScale, roundArrowScale, roundArrowScale, roundArrowScale } };

            for (int j = 1; j < 4; j++) {
                for (int i = 0; i < 4; i++) {
                    if (arrows[j][i] != null) {
                        arrows[j][i].setBounds(xs[j][i], ys[j][i], ws[j][i], hs[j][i]);
                        target.setComponentZOrder(arrows[j][i], zorder--);
                    }
                }
            }

            if (helperBackground != null) {
                helperBackground.setBounds((width - helperWidth) / 2, (height - helperHeight) / 2, helperWidth,
                        helperHeight);
                target.setComponentZOrder(helperBackground, zorder--);
            }
            if (helperLabel != null) {
                helperLabel.setBounds((width - helperWidth) / 2, (height - helperHeight) / 2, helperWidth,
                        helperHeight);
                target.setComponentZOrder(helperLabel, zorder--);
            }

            if (passBackground != null) {
                passBackground.setBounds((width - passWidth) / 2, (height - passHeight) / 2, passWidth, passHeight);
                target.setComponentZOrder(passBackground, zorder--);
            }
            if (passLabel != null) {
                passLabel.setBounds((width - passWidth) / 2, (height - passHeight) / 2, passWidth, passHeight);
                target.setComponentZOrder(passLabel, zorder--);
            }
        }
    }

    public Dimension minimumLayoutSize(final Container container) {
        final Insets insets = container == null ? new Insets(0, 0, 0, 0) : container.getInsets();
        return new Dimension(minWidth + insets.left + insets.right, minHeight + insets.top + insets.bottom);
    }

    public Dimension preferredLayoutSize(final Container container) {
        return minimumLayoutSize(container);
    }

    public Dimension maximumLayoutSize(final Container container) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

}