package layout;

import java.awt.*;

public class FullWindowLayout implements LayoutManager2 {
    private final int minw;
    private final int minh;

    private static final int hborder = 20;
    private static final int vborder = 20;

    public static final String WELCOME = "W";
    public static final String TURN = "T";

    public FullWindowLayout(final WelcomeLayout wLayout, final PokerGameLayout pLayout) {
        final Dimension d1 = wLayout.minimumLayoutSize(null);
        final Dimension d2 = pLayout.minimumLayoutSize(null);
        minw = hborder * 2 + Math.max(d1.width, d2.width);
        minh = vborder * 2 + Math.max(d1.height, d2.height);
    }

    public void addLayoutComponent(final Component comp, final Object constraints) {
    }

    public void addLayoutComponent(final String name, final Component comp) {
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

            final int top = insets.top + vborder;
            final int bottom = height - insets.bottom - vborder;
            final int left = insets.left + hborder;
            final int right = width - insets.right - hborder;

            for (final Component comp : target.getComponents())
                comp.setBounds(left, top, right - left, bottom - top);
        }
    }

    public Dimension minimumLayoutSize(final Container container) {
        final Insets insets = container == null ? new Insets(0, 0, 0, 0) : container.getInsets();
        return new Dimension(minw + insets.left + insets.right + 2 * hborder,
                minh + insets.top + insets.bottom + 2 * vborder);
    }

    public Dimension preferredLayoutSize(final Container container) {
        return minimumLayoutSize(container);
    }

    public Dimension maximumLayoutSize(final Container container) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

}