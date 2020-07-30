package element;

import ui.*;
import layout.PokerTableLayout;

import java.util.*;
import java.awt.*;
import javax.swing.*;

/**
 * The {@code Scoreboard} class shows the frame and total scores of each player
 * after ending of each frame.
 * 
 * @author Weizhao Tang
 */
public class Scoreboard extends JPanel {
    static final long serialVersionUID = 1L;

    /** foreground color of all texts */
    private static final Color textColor = MyColors.tableGreen;

    /** width (fixed) of scoreboard */
    private static final int w_ = PokerTableLayout.scoreWidth;
    /** height (fixed) of scoreboard */
    private static final int h_ = PokerTableLayout.scoreHeight;
    /** width of all names */
    private static final int namew = 280;
    /** gap between splitter lines and panel borders */
    private static final int gap = 0;
    /** line height */
    private static final int lineh = h_ / 5;
    /** inset of text at all borders */
    private static final int inset = 10;
    /** additional width for scores to avoid display issues */
    private static final int scoreWidthOverflow = 20;

    /** names on each line */
    private final JLabel[] nameLabels = new JLabel[4];
    /** total scores on each line */
    private final JLabel[] totalScoreLabels = new JLabel[4];
    /** frame scores on each line */
    private final JLabel[] frameScoreLabels = new JLabel[4];

    /**
     * The {@code SortResult} struct packs set of winners and index mapping after
     * sorting.
     */
    private static class SortResult {
        public final HashSet<Integer> winners;
        public final int[] indices;

        public SortResult(final HashSet<Integer> winners_, final int[] indices_) {
            winners = winners_;
            indices = indices_;
        }
    }

    /**
     * The {@code Tuple} struct packs the score and the index together to make it
     * possible to track down the index mappings after sorting.
     */
    private static class Tuple {
        public final int score;
        public final int index;

        public Tuple(final int score_, final int index_) {
            score = score_;
            index = index_;
        }
    }

    /**
     * The {@code TupleComparator} class defines comparison between scores. Sorting
     * the (score, index) tuples will yield a sorted list in descending order.
     */
    private static class TupleComparator implements Comparator<Tuple> {
        public int compare(final Tuple t1, final Tuple t2) {
            return t1.score == t2.score ? 0 : (t1.score < t2.score ? 1 : -1);
        }
    }

    /**
     * Instantiate a {@code Scoreboard} object. Initialize all subitems.
     */
    public Scoreboard() {
        setLayout(null);
        setOpaque(false);

        // The title
        final JLabel title = new JLabel(MyText.getScoreboardTitle(), SwingConstants.CENTER);
        title.setForeground(textColor);
        title.setFont(MyText.getScoreboardTitleFont());
        title.setBounds(0, 0, w_, lineh);
        add(title);

        // Horizontal splitter between title and all score lines
        final JLabel horizSplitter = new JLabel();
        horizSplitter.setBounds(gap, lineh, w_ - 2 * gap, 5);
        horizSplitter.setBorder(BorderFactory.createLineBorder(MyColors.tableGreen, 5));
        add(horizSplitter);

        // Vertical splitter between all names and all scores
        final JLabel vertSplitter = new JLabel();
        vertSplitter.setBounds(namew + inset, lineh, 5, h_ - lineh - gap);
        vertSplitter.setBorder(BorderFactory.createLineBorder(MyColors.tableGreen, 5));
        add(vertSplitter);

        // Add name, total score and frame score labels to each line
        int y = lineh;
        for (int i = 0; i < 4; i++) {
            int x = inset;

            nameLabels[i] = new JLabel("", SwingConstants.CENTER);
            nameLabels[i].setForeground(textColor);
            nameLabels[i].setFont(MyFont.playerName);
            nameLabels[i].setBounds(x, y, namew, lineh);
            add(nameLabels[i]);

            x += namew;
            totalScoreLabels[i] = new JLabel("", SwingConstants.RIGHT);
            totalScoreLabels[i].setFont(MyFont.smallScore);
            totalScoreLabels[i].setBounds(x - scoreWidthOverflow, y, (w_ - namew) / 2 - inset + scoreWidthOverflow,
                    lineh);
            add(totalScoreLabels[i]);

            x += (w_ - namew) / 2 - inset;
            frameScoreLabels[i] = new JLabel("", SwingConstants.RIGHT);
            frameScoreLabels[i].setFont(MyFont.smallScore);
            frameScoreLabels[i].setBounds(x - scoreWidthOverflow, y, (w_ - namew) / 2 - inset + scoreWidthOverflow,
                    lineh);
            add(frameScoreLabels[i]);

            y += lineh;
        }

        // Add background and put it to the deepest layer
        final BackgroundRect scoreBoardBG = new BackgroundRect(w_, h_);
        scoreBoardBG.setBounds(0, 0, w_, h_);
        scoreBoardBG.setVisible(true);
        scoreBoardBG.setOpaque(false);
        add(scoreBoardBG);
        setComponentZOrder(scoreBoardBG, getComponentCount() - 1);

        revalidate();
        repaint();
        setVisible(false);
    }

    /**
     * Sort and show given scores in descending order of frame scores.
     * 
     * @param scores Frame and total scores
     * @param names  Player names
     */
    public void showScores(final int[] scores, final String[] names) {
        final SortResult frameResult = sort(scores, 0, 4);
        final SortResult totalResult = sort(scores, 4, 8);

        for (int i = 0; i < 4; i++) {
            final int j = frameResult.indices[i]; // Original index of the player at i-th place
            String scoreString = String.valueOf(scores[j]);
            final String totalScoreString = String.valueOf(scores[4 + j]);
            if (scores[j] >= 0)
                scoreString = "+" + scoreString;

            scoreString = "(" + scoreString + ")";

            nameLabels[i].setText(names[j]);
            nameLabels[i].setForeground(textColor);

            totalScoreLabels[i].setText(totalScoreString);
            totalScoreLabels[i].setForeground(totalResult.winners.contains(j) ? MyColors.heartColor : textColor);

            frameScoreLabels[i].setText(scoreString);
            frameScoreLabels[i].setForeground(frameResult.winners.contains(j) ? MyColors.heartColor : textColor);
        }
        setVisible(true);
    }

    /**
     * Sort scores in an array and generate the winner set with index mapping.
     * 
     * @param scores Original score array
     * @param start  Starting index (inclusive) of the subarray
     * @param end    Ending index (exclusive) of the subarray
     * @return A {@link SortResult} object containing the winner set and the index
     *         mapping
     */
    private static SortResult sort(final int[] scores, final int start, final int end) {
        final ArrayList<Tuple> scoreArray = new ArrayList<>();
        for (int i = start; i < end; i++)
            scoreArray.add(new Tuple(scores[i], i - start));

        Collections.sort(scoreArray, new TupleComparator());
        final HashSet<Integer> winners = new HashSet<>();
        final int[] indices = new int[end - start];

        final int topScore = scoreArray.get(0).score;
        int i = 0;
        for (final Tuple tuple : scoreArray) {
            if (tuple.score == topScore) {
                winners.add(tuple.index);
            }
            indices[i++] = tuple.index;
        }

        return new SortResult(winners, indices);
    }
}