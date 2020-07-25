
import java.util.Random;
import java.awt.*;
import javax.swing.*;

public class test {
    public static final int w_ = 680;
    public static final int h_ = 500;
    public static final int errw = 220;
    public static final int errh = 120;

    public static final int gap = 30;

    public static final int shownw = 170;
    public static final int shownh = 140;
    public static final int avtw = 180;
    public static final int avth = 200;
    public static final int assetw = 200;
    public static final int asseth = 200;
    public static final int handw = 700;
    public static final int handh = 200;
    public static final int tabw = 700;
    public static final int tabh = 360;
    public static final int tabsecw = PokerTableLayout.secWidth;
    public static final int tabsech = PokerTableLayout.secHeight;
    public static final int helpw = PokerTableLayout.helperWidth;
    public static final int helph = PokerTableLayout.helperHeight;
    public static final int scorew = PokerTableLayout.scoreWidth;
    public static final int scoreh = PokerTableLayout.scoreHeight;
    public static final int seatw = WelcomeLayout.seatw;
    public static final int seath = WelcomeLayout.seath;
    public static final int txth = WelcomeLayout.txth;
    public static final int txtw = WelcomeLayout.maxtxtw;

    public static final Dimension TABLE_DIMENSION = new Dimension(w_, h_);
    public static final Dimension CARD_DIMENSION = new Dimension(95, 130);
    public static final int CARD_GAP = 24;
    public static final Color gray = new Color(210, 210, 210);
    public static final Color textBG = new Color(gray.getRed(), gray.getGreen(), gray.getBlue(), 190);

    public static int centerLoc(int containerScale, int objectScale) {
        return (containerScale - objectScale) / 2;
    }

    public static class PlaceHolder extends JPanel {
        static final long serialVersionUID = 1L;

        private int w_, h_;

        public PlaceHolder(int w, int h) {
            w_ = w;
            h_ = h;
            setOpaque(false);
            setMinimumSize(new Dimension(w_, h_));
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(w_, h_);
        }
    }

    public static class DrawRect extends JPanel {
        static final long serialVersionUID = 1L;

        private int w_, h_;

        public DrawRect(int w, int h) {
            w_ = w;
            h_ = h;
            int grayScale = (new Random()).nextInt(128) + 64;
            setOpaque(true);
            setBackground(new Color(grayScale, grayScale, grayScale, 127));
            setMinimumSize(new Dimension(w_, h_));
        }

        // @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            int scale = Math.min(w_ - gap, h_ - gap);
            int roundScale = (int) (scale * .15f);

            g.setColor(textBG);
            g.fillRoundRect(gap / 2, gap / 2, w_ - gap, h_ - gap, roundScale, roundScale);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(w_, h_);
        }
    }

    public static void addComponentsToWelcomePane(Container pane) {
        pane.setLayout(new WelcomeLayout(20));
        pane.setBackground(new Color(31, 64, 120));

        DrawRect rect = new DrawRect(seatw, seath);
        pane.add(rect, WelcomeLayout.SSEAT);

        rect = new DrawRect(seatw, seath);
        pane.add(rect, WelcomeLayout.NSEAT);

        rect = new DrawRect(seatw, seath);
        pane.add(rect, WelcomeLayout.ESEAT);

        rect = new DrawRect(seatw, seath);
        pane.add(rect, WelcomeLayout.WSEAT);

        pane.add(ImageLabel.getTable(), WelcomeLayout.TABLE);

        rect = new DrawRect(txtw, txth);
        pane.add(rect, WelcomeLayout.TABLEERR);

        rect = new DrawRect(txtw, txth);
        pane.add(rect, WelcomeLayout.NAMELABEL);

        rect = new DrawRect(txtw, txth);
        pane.add(rect, WelcomeLayout.NAME);

        rect = new DrawRect(txtw, txth);
        pane.add(rect, WelcomeLayout.NAMEERR);

        rect = new DrawRect(txtw, txth);
        pane.add(rect, WelcomeLayout.AVTLABEL);

        for (int i = 0; i < WelcomeLayout.numAvt; i++) {
            pane.add(new MaskedAvatar(i, WelcomeLayout.minAvatarScale), WelcomeLayout.AVATAR + i);
        }

        pane.add(ImageLabel.getIndicator(), WelcomeLayout.INDICATOR);
    }

    public static void addComponentsToBorderAltPane(Container pane) {
        pane.setLayout(new PokerGameLayout(20));
        // pane.setMinimumSize(TABLE_DIMENSION);
        pane.setBackground(new Color(31, 154, 100));

        JPanel centerPanel = new JPanel(new PokerTableLayout());
        centerPanel.setOpaque(false);
        // centerPanel.setMinimumSize(PokerTableLayout.minDim);
        // centerPanel.setPreferredSize(PokerTableLayout.minDim);
        pane.add(centerPanel, PokerGameLayout.CENTER);

        DrawRect rect = new DrawRect(tabsecw, tabsech);
        centerPanel.add(rect, PokerTableLayout.NSEC);

        rect = new DrawRect(tabsecw, tabsech);
        centerPanel.add(rect, PokerTableLayout.WSEC);

        rect = new DrawRect(tabsecw, tabsech);
        centerPanel.add(rect, PokerTableLayout.SSEC);

        rect = new DrawRect(tabsecw, tabsech);
        centerPanel.add(rect, PokerTableLayout.ESEC);

        rect = new DrawRect(scorew, scoreh);
        centerPanel.add(rect, PokerTableLayout.SBG);

        rect = new DrawRect(helpw, helph);
        centerPanel.add(rect, PokerTableLayout.HBG);

        rect = new DrawRect(avtw, avth);
        pane.add(rect, PokerGameLayout.NAVT);

        rect = new DrawRect(avtw, avth);
        pane.add(rect, PokerGameLayout.SAVT);

        rect = new DrawRect(avtw, avth);
        pane.add(rect, PokerGameLayout.WAVT);

        rect = new DrawRect(avtw, avth);
        pane.add(rect, PokerGameLayout.EAVT);

        rect = new DrawRect(assetw, asseth);
        pane.add(rect, PokerGameLayout.NASSET);

        rect = new DrawRect(assetw, asseth);
        pane.add(rect, PokerGameLayout.SASSET);

        rect = new DrawRect(assetw, asseth);
        pane.add(rect, PokerGameLayout.WASSET);

        rect = new DrawRect(assetw, asseth);
        pane.add(rect, PokerGameLayout.EASSET);

        rect = new DrawRect(handw, handh);
        pane.add(rect, PokerGameLayout.HAND);
    }

    public static void refresh(Container pane) {
        pane.revalidate();
        pane.repaint();
        pane.setVisible(true);
    }

    /**
     * Create the GUI and show it. For thread safety, this method should be invoked
     * from the event-dispatching thread.
     */
    private static void createAndShowGUI() {
        // Create and set up the window.
        JFrame frame = new JFrame("AbsoluteLayoutDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Container frame = frame_.getContentPane();

        // frame.setPreferredSize(TABLE_DIMENSION);
        // frame.setMinimumSize(TABLE_DIMENSION);
        frame.setLocationRelativeTo(null);
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();

        addComponentsToWelcomePane(frame.getContentPane());
        // addComponentsToBorderAltPane(frame.getContentPane());

        // frame.revalidate();
        // frame.repaint();
        frame.pack();
        frame.setVisible(true);

        // frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        // frame.setMaximizedBounds(env.getMaximumWindowBounds());

        System.out.println(frame.getSize() + ", " + env.getMaximumWindowBounds());
        // frame.setVisible(true);
    }

    public static void main(String[] args) {
        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });

        // String[] strs = new String[] { "A", "B", "C" };

        // for (int i = 0; i < 5; i++) {
        // String[] sub = Arrays.asList(strs).subList(i, strs.length).toArray(new
        // String[0]);
        // System.out.println(i + ": " + sub.length + "; " + String.join(", ", sub));
        // }
    }
}