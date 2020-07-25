
import java.awt.*;

public class WelcomeLayout implements LayoutManager2 {
    public static final String NSEAT = "NSEAT";
    public static final String SSEAT = "SSEAT";
    public static final String WSEAT = "WSEAT";
    public static final String ESEAT = "ESEAT";

    public static final String[] ALLSEATS = new String[] { SSEAT, ESEAT, NSEAT, WSEAT };

    public static final String TABLE = "TABLE";
    public static final String TABLEERR = "TABLEERR";
    public static final String TABLEERRBG = "TABLEERRBG";
    public static final String NAMELABEL = "NAMELABEL";
    public static final String NAME = "NAME";
    public static final String NAMEERR = "NAMEERR";
    public static final String AVTLABEL = "AVTLABEL";
    public static final String INDICATOR = "INDICATOR";
    public static final String AVATAR = "AVATAR";

    public static final int numAvt = 20;
    public static final int ncols = 5;
    public static final int nrows = 1 + (numAvt - 1) / ncols;

    public static final int seatw = 140;
    public static final int seath = 130;
    public static final int maxTableScale = 400;
    public static final int minTableScale = Math.max(seatw, seath);
    public static final int tableGap = 20;
    public static final int avatarGap = 10;
    public static final int maxAvatarScale = 150;
    public static final int minAvatarScale = 100;
    public static final int txth = 80;
    public static final int maxtxtw = 700;
    public static final int maxtxtgap = 100;

    public static final float tabWeight = 4.0f;

    public static final int leftw = (tableGap + seatw) * 2 + minTableScale;
    public static final int lefth = (tableGap + seath) * 2 + minTableScale + txth * 2;
    public static final int rightw = ncols * (minAvatarScale + avatarGap) - avatarGap;
    public static final int righth = txth * 4 + nrows * (minAvatarScale + avatarGap) - avatarGap;

    public static final int minWidth = leftw + rightw;
    public static final int minHeight = Math.max(lefth, righth);
    public static final Dimension minDim = new Dimension(minWidth, minHeight);

    Component northSeat;
    Component southSeat;
    Component eastSeat;
    Component westSeat;
    ImageLabel table;

    Component tableErr;
    Component tableErrBG;

    Component nameLabel;
    Component nameField;
    Component nameErr;
    Component avatarLabel;

    ImageLabel indicator;

    MaskedAvatar[] avatars = new MaskedAvatar[numAvt];

    private int sectionGap;

    public WelcomeLayout() {
        this(0);
    }

    public WelcomeLayout(int secGap) {
        sectionGap = secGap;
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
        if (NSEAT.equals(name)) {
            northSeat = comp;
        } else if (SSEAT.equals(name)) {
            southSeat = comp;
        } else if (ESEAT.equals(name)) {
            eastSeat = comp;
        } else if (WSEAT.equals(name)) {
            westSeat = comp;
        } else if (TABLE.equals(name)) {
            table = (ImageLabel) comp;
        } else if (TABLEERR.equals(name)) {
            tableErr = comp;
        } else if (TABLEERRBG.equals(name)) {
            tableErrBG = comp;
        } else if (NAMELABEL.equals(name)) {
            nameLabel = comp;
        } else if (NAME.equals(name)) {
            nameField = comp;
        } else if (NAMEERR.equals(name)) {
            nameErr = comp;
        } else if (AVTLABEL.equals(name)) {
            avatarLabel = comp;
        } else if (name != null && name.substring(0, AVATAR.length()).equals(AVATAR)) {
            String indexString = name.substring(AVATAR.length());
            try {
                int avtIndex = Integer.parseInt(indexString);
                avatars[avtIndex] = (MaskedAvatar) comp;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "cannot add to layout: avatar index \"" + indexString + "\" not identified");
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IllegalArgumentException(
                        "cannot add to layout: avatar index \"" + indexString + "\" out of bounds");
            }
        } else if (INDICATOR.equals(name)) {
            indicator = (ImageLabel) comp;
        } else {
            throw new IllegalArgumentException("cannot add to layout: name \"" + name + "\" not identified");
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

    private int[] getScales(int cw, int ch) {
        int tabs, avts;

        int remw = cw - avatarGap * (ncols - 1) - 2 * (seatw + tableGap) - sectionGap;
        int remlh = ch - 2 * (seath + tableGap) - txth * 2;
        int remrh = ch - avatarGap * (nrows - 1) - txth * 4;

        int tmax = remlh;
        int amax = remrh / nrows;

        if (tmax <= minTableScale) {
            tabs = minTableScale;
            avts = projInteval(minAvatarScale, (remw - tabs) / ncols, amax);
        } else if (amax <= minAvatarScale) {
            avts = minAvatarScale;
            tabs = projInteval(minTableScale, remw - avts * ncols, tmax);
        } else if (remw >= tmax + ncols * amax) {
            tabs = projInteval(minTableScale, tmax, maxTableScale);
            avts = projInteval(minAvatarScale, tmax, maxAvatarScale);
        } else if (remw <= minTableScale + ncols * minAvatarScale) {
            tabs = minTableScale;
            avts = minAvatarScale;
            System.out.println("mode4");
        } else {
            int x1 = Math.max(remw - ncols * amax, minTableScale);
            int x2 = Math.min(remw - ncols * minAvatarScale, tmax);
            int y1 = Math.max((remw - tmax) / ncols, minAvatarScale);
            int y2 = Math.min((remw - minTableScale) / ncols, amax);

            tabs = Math.min(maxTableScale, (int) (x1 + (x2 - x1) * tabWeight / (1 + tabWeight)));
            avts = Math.min(maxAvatarScale, (int) (y1 + (y2 - y1) * 1 / (1 + tabWeight)));

            System.out.println(String.format("x1 = %d, x2 = %d, tmax = %d;  y1 = %d, y2 = %d, amax = %d", x1, x2, tmax,
                    y1, y2, amax));
            System.out
                    .println(String.format("t = %d, a = %d, t + 5a = %d <= %d", tabs, avts, tabs + avts * ncols, remw));
        }

        return new int[] { tabs, avts };
    }

    private static int projInteval(int min, int mid, int max) {
        return Math.max(min, Math.min(mid, max));
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

            int ch = bottom - top;
            int cw = right - left;

            // assign scales to table and avatars with LP
            int[] scales = getScales(cw, ch);
            int tabScale = scales[0], avtScale = scales[1];

            // share middle gap if space is sufficient
            int lw = tabScale + 2 * (tableGap + seatw);
            int rw = ncols * (avtScale + avatarGap) - avatarGap;
            int midgap = cw - lw - rw;

            if (midgap >= sectionGap) {
                lw += (int) ((midgap - sectionGap) * tabWeight / (1 + tabWeight));
                rw = cw - lw - sectionGap;
            }

            // centering right half
            int nameh = txth * 3;
            int avth = txth + (avtScale + avatarGap) * nrows - avatarGap;
            int vgap = Math.max(0, Math.min(maxtxtgap, ch - nameh - avth));
            int rx1 = left + lw + sectionGap;
            int rx2 = left + lw + sectionGap + (rw - ncols * (avtScale + avatarGap) + avatarGap) / 2;
            int ry1 = top + Math.max(0, (ch - nameh - vgap - avth) / 2);
            int ry2 = ry1 + 4 * txth + vgap;

            // setting table location
            int tabx1 = left + (lw - tabScale) / 2;
            int tabx2 = left + (lw + tabScale) / 2;
            int taby1 = (height - tabScale) / 2;
            int taby2 = (height + tabScale) / 2;

            // setting text width
            int txtlw = Math.min(lw, maxtxtw);
            int txtrw = Math.min(rw, maxtxtw);
            rx1 += (rw - txtrw) / 2;

            // placing elements
            int zorder = target.getComponentCount() - 1;

            if (table != null) {
                table.setBounds(tabx1, taby1, tabScale, tabScale);
                table.rescale(tabScale);
            }
            if (westSeat != null) {
                westSeat.setBounds(tabx1 - seatw - tableGap, taby1 + (tabScale - seath) / 2, seatw, seath);
            }
            if (eastSeat != null) {
                eastSeat.setBounds(tabx2 + tableGap, taby1 + (tabScale - seath) / 2, seatw, seath);
            }
            if (northSeat != null) {
                northSeat.setBounds(tabx1 + (tabScale - seatw) / 2, taby1 - seath - tableGap, seatw, seath);
            }
            if (southSeat != null) {
                southSeat.setBounds(tabx1 + (tabScale - seatw) / 2, taby2 + tableGap, seatw, seath);
            }
            if (tableErrBG != null) {
                tableErrBG.setBounds(left + (lw - txtlw) / 2, top + (taby1 - txth) / 2, txtlw, txth);
                target.setComponentZOrder(tableErrBG, zorder--);
            }
            if (tableErr != null) {
                tableErr.setBounds(left + (lw - txtlw) / 2, top + (taby1 - tableGap - seath - txth) / 2, txtlw, txth);
                target.setComponentZOrder(tableErr, zorder--);
            }
            if (nameLabel != null) {
                nameLabel.setBounds(rx1, ry1, txtrw, txth);
            }
            if (nameField != null) {
                nameField.setBounds(rx1, ry1 + txth, txtrw, txth);
            }
            if (nameErr != null) {
                nameErr.setBounds(rx1, ry1 + txth * 2, txtrw, txth);
            }
            if (avatarLabel != null) {
                avatarLabel.setBounds(rx1, ry2 - txth, txtrw, txth);
            }
            for (int i = 0; i < numAvt; i++) {
                if (avatars[i] == null)
                    continue;

                int x = rx2 + (avtScale + avatarGap) * (i % ncols);
                int y = ry2 + (avtScale + avatarGap) * (i / ncols);

                avatars[i].setBounds(x, y, avtScale, avtScale);
                avatars[i].rescale(avtScale);
                target.setComponentZOrder(avatars[i], zorder--);
            }
            if (indicator != null) {
                int i = Integer.parseInt(indicator.getName().substring(2));

                int x = rx2 + (avtScale + avatarGap) * (i % ncols);
                int y = ry2 + (avtScale + avatarGap) * (i / ncols);

                indicator.setBounds(x, y, avtScale, avtScale);
                indicator.rescale(avtScale);
                target.setComponentZOrder(indicator, zorder--);
            }
        }
    }

    public Dimension minimumLayoutSize(Container container) {
        Insets insets = container == null ? new Insets(0, 0, 0, 0) : container.getInsets();
        return new Dimension(minWidth + sectionGap + insets.left + insets.right,
                minHeight + insets.top + insets.bottom);
    }

    public Dimension preferredLayoutSize(Container container) {
        return minimumLayoutSize(container);
    }

    public Dimension maximumLayoutSize(Container container) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

}