
package layout;

import element.ImageLabel;
import java.awt.*;

public class PlayerPanelLayout implements LayoutManager2 {

    public static final String AVATAR = "Avatar";
    public static final String TSCORE = "TotalScore";
    public static final String NAME = "Name";

    public static final int w_ = 200;
    public static final int h_ = 200;
    public static final int scoreh = 30;

    public static final int minNameh = 20;
    public static final int maxNameh = 40;
    public static final int minAvtScale = 150;
    public static final int maxAvtScale = 180;

    public static final int minh = 200;
    public static final int maxh = maxNameh + maxAvtScale + scoreh;

    public static final float avtGrowthRate = (maxAvtScale - minAvtScale) / ((float) (maxh - minh));
    public static final float nameGrowthRate = (maxNameh - minNameh) / ((float) (maxh - minh));

    Component nameLabel;
    Component scoreLabel;
    ImageLabel avatar;

    public PlayerPanelLayout() {

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
        if (AVATAR.equals(name)) {
            avatar = (ImageLabel) comp;
        } else if (TSCORE.equals(name)) {
            scoreLabel = comp;
        } else if (NAME.equals(name)) {
            nameLabel = comp;
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

            final int avatarGrowth = (int) ((height - minh) * avtGrowthRate);
            final int avatarScale = minAvtScale
                    + WelcomeLayout.projInterval(0, avatarGrowth, maxAvtScale - minAvtScale);

            final int nameGrowth = (int) ((height - minh) * nameGrowthRate);
            final int nameh = minNameh + WelcomeLayout.projInterval(0, nameGrowth, maxNameh - minNameh);

            if (scoreLabel != null) {
                scoreLabel.setBounds(left, top, right - left, scoreh);
            }
            if (avatar != null) {
                avatar.setBounds((width - avatarScale) / 2, top + scoreh, avatarScale, avatarScale);
                avatar.rescale(avatarScale);
            }
            if (nameLabel != null) {
                nameLabel.setBounds(left, bottom - nameh, right - left, nameh);
            }
        }
    }

    public Dimension minimumLayoutSize(final Container container) {
        final Insets insets = container == null ? new Insets(0, 0, 0, 0) : container.getInsets();
        return new Dimension(w_ + insets.left + insets.right, minh + insets.top + insets.bottom);
    }

    public Dimension preferredLayoutSize(final Container container) {
        return minimumLayoutSize(container);
    }

    public Dimension maximumLayoutSize(final Container container) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public static int getAvtCenterY(final int height, final int topInset) {
        final int avatarGrowth = (int) ((height - minh) * avtGrowthRate);
        final int avatarScale = minAvtScale + WelcomeLayout.projInterval(0, avatarGrowth, maxAvtScale - minAvtScale);
        final int avatarCenterY = topInset + scoreh + avatarScale / 2;
        return avatarCenterY;
    }

}