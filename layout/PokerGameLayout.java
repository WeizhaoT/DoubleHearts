
import java.awt.*;

public class PokerGameLayout implements LayoutManager2 {
    public static final String NAVT = "NAVT";
    public static final String SAVT = "SAVT";
    public static final String WAVT = "WAVT";
    public static final String EAVT = "EAVT";
    public static final String NASSET = "NASSET";
    public static final String SASSET = "SASSET";
    public static final String WASSET = "WASSET";
    public static final String EASSET = "EASSET";
    public static final String HAND = "HAND";
    public static final String CENTER = "CENTER";

    public static final int avth = 200;
    public static final int avtw = 180;
    public static final int asseth = 200;
    public static final int assetw = 200;
    public static final int handh = 200;
    public static final int handw = 700;

    public static final int toph = Math.max(avth, asseth);
    public static final int bottomh = Math.max(toph, handh);
    public static final int leftw = Math.max(avtw, assetw);
    public static final int rightw = Math.max(avtw, assetw);

    public static final int minHeight = toph + bottomh + PokerTableLayout.minHeight;
    public static final int minWidth = leftw + rightw + PokerTableLayout.minWidth;

    Component northAvatar;
    Component southAvatar;
    Component eastAvatar;
    Component westAvatar;

    Component northAsset;
    Component southAsset;
    Component eastAsset;
    Component westAsset;

    Component hand;
    Component center;

    private int gap;
    private int centerGap;

    public PokerGameLayout() {
        this(0);
    }

    public PokerGameLayout(int gap) {
        this.gap = gap;
        centerGap = 0;
    }

    public PokerGameLayout(int gap, int centerGap) {
        this.gap = gap;
        this.centerGap = centerGap;
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
            case NAVT:
                northAvatar = comp;
                return;
            case SAVT:
                southAvatar = comp;
                return;
            case WAVT:
                westAvatar = comp;
                return;
            case EAVT:
                eastAvatar = comp;
                return;
            case NASSET:
                northAsset = comp;
                return;
            case SASSET:
                southAsset = comp;
                return;
            case WASSET:
                westAsset = comp;
                return;
            case EASSET:
                eastAsset = comp;
                return;
            case HAND:
                hand = comp;
                return;
            case CENTER:
                center = comp;
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

            int ctop = top + toph + centerGap;
            int cbottom = bottom - bottomh - centerGap;
            int cleft = left + leftw + centerGap;
            int cright = right - rightw - centerGap;

            int sideAvty = ctop + (cbottom - ctop - avth) / 2;
            int topAvtx = (width - avtw) / 2;
            int southAvtx = left + (leftw - avtw) / 2;

            int zorder = target.getComponentCount() - 1;

            if (westAvatar != null) {
                westAvatar.setBounds(left + (leftw - avtw) / 2, sideAvty, avtw, avth);
                target.setComponentZOrder(westAvatar, zorder--);
            }
            if (eastAvatar != null) {
                eastAvatar.setBounds(right - (rightw + avtw) / 2, sideAvty, avtw, avth);
                target.setComponentZOrder(eastAvatar, zorder--);
            }
            if (southAvatar != null) {
                southAvatar.setBounds(southAvtx, bottom - (bottomh + avth) / 2, avtw, avth);
                target.setComponentZOrder(southAvatar, zorder--);
            }
            if (northAvatar != null) {
                northAvatar.setBounds(topAvtx, top + (toph - avth) / 2, avtw, avth);
                target.setComponentZOrder(northAvatar, zorder--);
            }
            if (westAsset != null) {
                westAsset.setBounds(left + (leftw - assetw) / 2, sideAvty - gap - asseth, assetw, asseth);
                target.setComponentZOrder(westAsset, zorder--);
            }
            if (eastAsset != null) {
                eastAsset.setBounds(right - (rightw + assetw) / 2, sideAvty - gap - asseth, assetw, asseth);
                target.setComponentZOrder(eastAsset, zorder--);
            }
            if (northAsset != null) {
                northAsset.setBounds(topAvtx + avtw + gap, (toph - asseth) / 2, assetw, asseth);
                target.setComponentZOrder(northAsset, zorder--);
            }
            if (southAsset != null) {
                southAsset.setBounds(right - (rightw + assetw) / 2, bottom - (bottomh + asseth) / 2, assetw, asseth);
                target.setComponentZOrder(southAsset, zorder--);
            }
            if (hand != null) {
                hand.setBounds((width - handw) / 2, bottom - (bottomh + handh) / 2, handw, handh);
                target.setComponentZOrder(hand, zorder--);
            }
            if (center != null) {
                center.setBounds(cleft, ctop, cright - cleft, cbottom - ctop);
                target.setComponentZOrder(center, zorder--);
            }
        }
    }

    public Dimension minimumLayoutSize(Container container) {
        return new Dimension(minWidth + 2 * centerGap, minHeight + 2 * centerGap);
    }

    public Dimension preferredLayoutSize(Container container) {
        return new Dimension(minWidth + 2 * centerGap, minHeight + 2 * centerGap);
    }

    public Dimension maximumLayoutSize(Container container) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

}