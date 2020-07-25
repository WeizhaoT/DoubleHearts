
import java.awt.*;

public class PokerTableLayout implements LayoutManager2 {
    public static final String NSEC = "Nsec";
    public static final String SSEC = "Ssec";
    public static final String WSEC = "Wsec";
    public static final String ESEC = "Esec";
    public static final String HLABEL = "Hlabel";
    public static final String HBG = "Hbg";
    public static final String SLABEL = "Slabel";
    public static final String SBG = "Sbg";

    public static final int secWidth = 700;
    public static final int secHeight = 180;
    public static final int scoreWidth = 600;
    public static final int scoreHeight = 300;
    public static final int helperWidth = 420;
    public static final int helperHeight = 120;

    public static final int minWidth = 700;
    public static final int minHeight = 2 * secHeight;
    public static final Dimension minDim = new Dimension(minWidth, minHeight);

    Component northSection;
    Component southSection;
    Component eastSection;
    Component westSection;

    Component helperLabel;
    Component helperBackground;

    Component scoreLabel;
    Component scoreBackground;

    public PokerTableLayout() {

    }

    public void addLayoutComponent(Component comp, Object constraints) {
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

    public void addLayoutComponent(String name, Component comp) {
        switch (name) {
            case NSEC:
                northSection = comp;
                return;
            case SSEC:
                southSection = comp;
                return;
            case WSEC:
                westSection = comp;
                return;
            case ESEC:
                eastSection = comp;
                return;
            case SBG:
                scoreBackground = comp;
                return;
            case SLABEL:
                scoreLabel = comp;
                return;
            case HBG:
                helperBackground = comp;
                return;
            case HLABEL:
                helperLabel = comp;
                return;
            default:
                throw new IllegalArgumentException("cannot add to layout: constraint \"" + name + "\" not identified");
        }
    }

    public float getLayoutAlignmentX(Container container) {
        return 0.5f;
    }

    public float getLayoutAlignmentY(Container container) {
        return 0.5f;
    }

    public void removeLayoutComponent(Component comp) {

    }

    public void invalidateLayout(Container container) {

    }

    public void layoutContainer(Container target) {
        synchronized (target.getTreeLock()) {
            Insets insets = target.getInsets();
            int height = target.getHeight();
            int width = target.getWidth();

            int top = insets.top;
            int bottom = height - insets.bottom;
            int left = insets.left;
            int right = width - insets.right;

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
            if (scoreLabel != null) {
                scoreLabel.setBounds((width - scoreWidth) / 2, (height - scoreHeight) / 2, scoreWidth, scoreHeight);
                target.setComponentZOrder(scoreBackground, zorder--);
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
        }
    }

    public Dimension minimumLayoutSize(Container container) {
        return new Dimension(minWidth, minHeight);
    }

    public Dimension preferredLayoutSize(Container container) {
        return new Dimension(minWidth, minHeight);
    }

    public Dimension maximumLayoutSize(Container container) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

}