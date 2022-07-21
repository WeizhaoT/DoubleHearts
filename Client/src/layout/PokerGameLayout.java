package layout;

import java.awt.*;
import panel.PlayerPanel;

public class PokerGameLayout implements LayoutManager2 {
    public static final String NAVT = "NAVT";
    public static final String SAVT = "SAVT";
    public static final String WAVT = "WAVT";
    public static final String EAVT = "EAVT";
    public static final String[] ALLAVTS = new String[] { SAVT, EAVT, NAVT, WAVT };

    public static final String NASSET = "NASSET";
    public static final String SASSET = "SASSET";
    public static final String WASSET = "WASSET";
    public static final String EASSET = "EASSET";
    public static final String[] ALLASSETS = new String[] { SASSET, EASSET, NASSET, WASSET };

    public static final String RULE = "RULE";
    public static final String LEGEND = "LEGEND";
    public static final String SPEAKER = "SPEAKER";
    public static final String HAND = "HAND";
    public static final String CENTER = "CENTER";

    public static final int minAvth = PlayerPanelLayout.h_;
    public static final int maxAvth = PlayerPanelLayout.maxh;
    public static final int avtw = PlayerPanelLayout.w_;

    public static final int asseth = 200;
    public static final int assetw = 200;

    public static final int rulew = 200;
    public static final int ruleh = 160;

    public static final int speakerw = 60;
    public static final int speakerh = 60;

    public static final int handh = 200;
    public static final int handw = 700;
    public static final int maxHandInset = 50;

    public static final int minCenterh = PokerTableLayout.minHeight;
    public static final float avtGrowth = 0.4f;

    public static final int minToph = Math.max(Math.max(minAvth, asseth), ruleh);
    public static final int minBottomh = Math.max(minToph, handh);
    public static final int leftw = Math.max(Math.max(avtw, assetw), rulew);
    public static final int rightw = Math.max(avtw, assetw);

    public static final int minHeight = minToph + minBottomh + PokerTableLayout.minHeight;
    public static final int minWidth = leftw + rightw + PokerTableLayout.minWidth;

    PlayerPanel northAvatar;
    PlayerPanel southAvatar;
    PlayerPanel eastAvatar;
    PlayerPanel westAvatar;

    Component northAsset;
    Component southAsset;
    Component eastAsset;
    Component westAsset;

    Component rule;
    Component legend;
    Component speaker;

    Component hand;
    Component center;

    private final int gap;
    private final int centerGap;

    public PokerGameLayout() {
        this(0);
    }

    public PokerGameLayout(final int gap) {
        this.gap = gap;
        centerGap = 0;
    }

    public PokerGameLayout(final int gap, final int centerGap) {
        this.gap = gap;
        this.centerGap = centerGap;
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
        switch (name) {
            case NAVT:
                northAvatar = (PlayerPanel) comp;
                return;
            case SAVT:
                southAvatar = (PlayerPanel) comp;
                return;
            case WAVT:
                westAvatar = (PlayerPanel) comp;
                return;
            case EAVT:
                eastAvatar = (PlayerPanel) comp;
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
            case RULE:
                rule = comp;
                return;
            case LEGEND:
                legend = comp;
                return;
            case SPEAKER:
                speaker = comp;
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

            final int extrah = bottom - top - 2 * centerGap - minToph - minBottomh - minCenterh;
            final int avth = WelcomeLayout.projInterval(minAvth, minAvth + (int) (extrah * avtGrowth / 2), maxAvth);

            final int toph = Math.max(minToph, avth);
            final int bottomh = Math.max(minBottomh, avth);

            final int ctop = top + toph + centerGap;
            final int cbottom = bottom - bottomh - centerGap;
            final int cleft = left + leftw + centerGap;
            final int cright = right - rightw - centerGap;

            final int vCenter = (ctop + cbottom) / 2;
            final int avtVCenterOffset = -PlayerPanelLayout.getAvtCenterY(avth, 0);

            final int sideAvty = ctop + (cbottom - ctop - avth) / 2;
            final int topAvtx = (width - avtw) / 2;
            final int southAvtx = left + (leftw - avtw) / 2;

            final int handYOffset = WelcomeLayout.projInterval(-maxHandInset - minAvth, -avth, -minAvth);

            int zorder = target.getComponentCount() - 1;

            if (westAvatar != null) {
                westAvatar.setBounds(left + (leftw - avtw) / 2, vCenter + avtVCenterOffset, avtw, avth);
                target.setComponentZOrder(westAvatar, zorder--);
            }
            if (eastAvatar != null) {
                eastAvatar.setBounds(right - (rightw + avtw) / 2, vCenter + avtVCenterOffset, avtw, avth);
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
            if (rule != null) {
                rule.setBounds(cleft, top + (toph - ruleh) / 2, rulew, ruleh);
                target.setComponentZOrder(rule, zorder--);
            }
            if (legend != null) {
                legend.setBounds(left + (leftw - rulew) / 2, top + (toph - ruleh) / 2, rulew, ruleh);
                target.setComponentZOrder(legend, zorder--);
            }
            if (speaker != null) {
                speaker.setBounds(right - speakerw, top, speakerw, speakerh);
                target.setComponentZOrder(speaker, zorder--);
            }
            if (hand != null) {
                hand.setBounds((width - handw) / 2, bottom + handYOffset, handw, handh);
                target.setComponentZOrder(hand, zorder--);
            }
            if (center != null) {
                center.setBounds(cleft, ctop, cright - cleft, cbottom - ctop);
                target.setComponentZOrder(center, zorder--);
            }
        }
    }

    public Dimension minimumLayoutSize(final Container container) {
        final Insets insets = container == null ? new Insets(0, 0, 0, 0) : container.getInsets();
        return new Dimension(minWidth + 2 * centerGap + insets.left + insets.right,
                minHeight + 2 * centerGap + insets.top + insets.bottom);
    }

    public Dimension preferredLayoutSize(final Container container) {
        return minimumLayoutSize(container);
    }

    public Dimension maximumLayoutSize(final Container container) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

}