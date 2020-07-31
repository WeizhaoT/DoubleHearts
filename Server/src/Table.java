import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Table objects represent a table that players can join.
 *
 * @author Weizhao Tang
 */
public class Table implements Runnable {
    public static final int tradeSize = 3;
    public static final int tradeOrder[] = new int[] { 1, 2, 3, 0 };

    private static final int lastRoundDelay = 800;
    private static final int frameEndDelay = 1000;
    private static final int endShowingDelay = 2000;

    private Thread tabThread;

    private final ArrayList<Player> table = new ArrayList<>(); // holds the players at the table
    private final Player[] seats;
    private final ConcurrentHashMap<Player, Thread> threadMap = new ConcurrentHashMap<>();
    private final Thread[] playerThreads = new Thread[4];
    private final int[] avtIndices;
    private final String[] names;

    private final AtomicBoolean[] isReady = new AtomicBoolean[4];
    private final AtomicInteger numShown = new AtomicInteger(0);

    public String[][] tradeOut;

    private final CountUpDownLatch roundReadyLatch = new CountUpDownLatch(4); // latch to wait for all
    private final CountUpDownLatch allCardsDealtLatch = new CountUpDownLatch(4);
    private final CountUpDownLatch tradeLatch = new CountUpDownLatch(4);
    private final CountUpDownLatch exhibitionLatch = new CountUpDownLatch(4);

    private AtomicInteger[] totalScore = new AtomicInteger[4];

    private int frameNum = 0;
    private int tradeGap;
    private int numberOfDecks;
    private boolean waitingForReady = false;

    /**
     * Constructor for Table object.
     */

    public Table(int numDecks) {
        numberOfDecks = numDecks;
        seats = new Player[4];
        names = new String[4];
        avtIndices = new int[] { -1, -1, -1, -1 };
        for (int i = 0; i < 4; i++)
            isReady[i] = new AtomicBoolean(false);
    }

    public int numDecks() {
        return numberOfDecks;
    }

    public void setTabThread(final Thread tabThread) {
        this.tabThread = tabThread;
    }

    public void setPlayerThread(final Player player, final Thread playerThread) {
        threadMap.put(player, playerThread);
    }

    /**
     * Table thread run method.
     */

    @Override
    public void run() {
        do {
            gameSetup();
            try {
                while (true) {
                    gameFrame();
                }
            } catch (final IOException e) {
                e.printStackTrace();
            } catch (final InterruptedException e) {
                System.err.println("\nTable thread interrupted possibly because of a on-table client dropping offline");
            }
        } while (true);
    }

    /**
     * Sets the table up for a new round of Blackjack.
     */

    public void initAll() {
        tradeOut = new String[4][tradeSize];
        for (int i = 0; i < 4; i++)
            totalScore[i] = new AtomicInteger(0);

        resetLatches();
    }

    private void gameSetup() {
        System.err.println("Setting up");
        waitingForReady = true;
        frameNum = 0;
        initAll();
    }

    private void resetFrame() {
        waitingForReady = true;
        resetReady();
        resetLatches();
        numShown.set(0);
        tradeOut = new String[4][tradeSize];
        for (final Player player : table)
            player.frameEndingLatchCountDown();
    }

    private void resetLatches() {
        roundReadyLatch.reset();
        allCardsDealtLatch.reset();
        tradeLatch.reset();
        exhibitionLatch.reset();

        for (int i = 0; i < 4; i++) {
            if (isReady[i].get())
                roundReadyLatchCountDown();
        }
    }

    private void setReady(final int seat) {
        isReady[seat].set(true);
    }

    private void resetReady() {
        for (int i = 0; i < 4; i++)
            isReady[i].set(false);

        roundReadyLatch.reset();
    }

    private void gameFrame() throws IOException, InterruptedException {
        tradeGap = tradeOrder[Math.floorMod(frameNum, tradeOrder.length)];
        final Shoe shoe = new Shoe(numberOfDecks);
        shoe.shuffle();
        int numCards = shoe.remainingCards();

        roundReadyLatch.await();
        waitingForReady = false;

        broadcastDeal(numCards / 4, numberOfDecks);
        final int[] twoClubHolders = dealAllCards(shoe, (new Random()).nextInt(4));
        allCardsDealtLatch.await();

        if (tradeGap != 0) {
            broadcastTradeStart(tradeGap);
            tradeLatch.await();
            for (int i = 0; i < 4; i++) {
                final int target = Math.floorMod(i + tradeGap, 4);
                for (final String cardAlias : tradeOut[i]) {
                    if ("2C".equals(cardAlias)) {
                        twoClubHolders[i]--;
                        twoClubHolders[target]++;
                    }
                }
                seats[target].sendTradeIn(tradeOut[i]);
            }
        }

        broadcastExhibition();
        exhibitionLatch.await();

        for (final Player player : table) {
            player.framePlayingLatchCountDown();
        }

        if (numShown.get() > 0)
            Thread.sleep(endShowingDelay);

        final int roundLeader = pickLeader(twoClubHolders);
        broadcastFirstLeader(roundLeader);

        playInTurns(numCards / 4, roundLeader);

        for (int i = 0; i < 4; i++) {
            totalScore[i].addAndGet(seats[i].getScore());
        }

        Thread.sleep(frameEndDelay);

        frameNum++;
        resetFrame();
    }

    private int[] dealAllCards(final Shoe shoe, int starter) {
        Card nextCard;
        final int i = 0;
        final int[] leaders = new int[] { 0, 0, 0, 0 };

        while ((nextCard = shoe.dealCard()) != null) {
            if (nextCard.weakEquals("2C") && i < numberOfDecks) {
                leaders[starter]++;
            }

            seats[starter].addCard(nextCard);
            starter = Math.floorMod(starter + 1, 4);
        }
        return leaders;
    }

    private int pickLeader(final int[] twoClubHolders) {
        int sum = 0;
        for (int c : twoClubHolders)
            sum += c;

        int randi = (new Random()).nextInt(sum);

        for (int i = 0; i < twoClubHolders.length; i++) {
            if ((randi -= twoClubHolders[i]) < 0)
                return i;
        }

        return twoClubHolders.length - 1;
    }

    private void playInTurns(int cardsRemain, int leader) throws IOException, InterruptedException {
        while (cardsRemain > 0) {
            int roundSize = 0;
            final ArrayList<ArrayList<Card>> cardSeq = new ArrayList<>();
            final ArrayList<Card> asset = new ArrayList<>();

            for (int turned = 0; turned < 4; turned++) {
                final int iPlayer = (leader + turned) % 4;
                final ArrayList<Card> cards = seats[iPlayer].playTurn();

                if (roundSize == 0) {
                    roundSize = cards.size();
                    cardsRemain -= roundSize;
                }

                cardSeq.add(cards);
                broadcastPlayed(turned == 0, iPlayer, cards);

                if (cardsRemain == 0)
                    Thread.sleep(lastRoundDelay);
            }

            leader = Card.roundResult(cardSeq, leader);
            for (final ArrayList<Card> cards : cardSeq) {
                for (final Card card : cards) {
                    if (card.isValuable())
                        asset.add(card);
                }
            }

            seats[leader].addAsset(asset);
            Thread.sleep(lastRoundDelay);
            broadcastAsset(leader, asset);
        }
    }

    /**
     * Adds a player to the table.
     *
     * @param player Player to add to table
     */

    public void addPlayer(final Player player) {
        synchronized (table) {
            table.add(player);
        }
    }

    /**
     * Removes a player from the table.
     *
     * @param player Player to remove from table
     */

    public void removePlayer(final Player player) {
        synchronized (table) {
            table.remove(player);
            threadMap.remove(player);
        }
    }

    public boolean sitDown(final Player player, final int seat, final int avtIndex, final String name) {
        synchronized (seats) {
            if (seats[seat] != null)
                return false;

            seats[seat] = player;
            avtIndices[seat] = avtIndex;
            names[seat] = new String(name);
            playerThreads[seat] = threadMap.get(player);

            for (final Player player2 : table)
                player2.sendSeating(seat, avtIndex, name);

            return true;
        }
    }

    public void removeFromSeat(final int seat) {
        synchronized (seats) {
            isReady[seat].set(false);
            seats[seat] = null;
            playerThreads[seat] = null;
            avtIndices[seat] = -1;
            names[seat] = null;
        }
    }

    public void dealWithConnectionLoss(final Player player, final int seat) {
        if (seat < 0) {
            System.err.println("A player off table dropped offline");
            removePlayer(player);
            return;
        }

        System.err.println("Player (" + seat + ", " + names[seat] + ") on table dropped offline");

        removePlayer(player);
        initAll();
        removeFromSeat(seat);
        broadcastReset(seat);

        if (!waitingForReady) {
            resetReady();
            for (int i = 0; i < 4; i++) {
                if (playerThreads[i] != null) {
                    playerThreads[i].interrupt();
                }
            }
        }
        tabThread.interrupt();
    }

    public void broadcastReady(final int seat) {
        setReady(seat);
        synchronized (table) {
            for (final Player player : table)
                player.sendReady(seat);
        }
    }

    public void broadcastDeal(final int numCards, final int numDecks) {
        synchronized (seats) {
            for (final Player player : seats)
                player.sendDeal(numCards, numDecks);
        }
    }

    public void broadcastTradeStart(final int tradeGap) {
        synchronized (seats) {
            for (final Player player : seats) {
                player.sendTradeStart(tradeGap);
            }
        }
    }

    public void broadcastTradeReady(final int seat) {
        synchronized (seats) {
            for (final Player player : seats) {
                player.sendTradeReady(seat);
            }
        }
    }

    public void broadcastExhibition() {
        synchronized (seats) {
            for (final Player player : seats)
                player.sendExhibition();
        }
    }

    public void broadcastShown(final int seat, final String[] cardAliases) {
        if (cardAliases != null && cardAliases.length != 0) {
            numShown.incrementAndGet();
        }
        synchronized (seats) {
            for (final Player player : seats)
                player.sendShown(seat, cardAliases);
        }
    }

    public void broadcastFirstLeader(final int seat) {
        synchronized (seats) {
            for (final Player player : seats) {
                player.sendFirstLeader(seat);
            }
        }
    }

    public void broadcastPlayed(final boolean lead, final int seat, final Collection<Card> cards) {
        synchronized (seats) {
            for (final Player player : seats)
                player.sendPlayed(lead, seat, cards);
        }
    }

    public void broadcastAsset(final int seat, final Collection<Card> asset) {
        synchronized (seats) {
            for (final Player player : seats) {
                player.sendAsset(seat, asset);
            }
        }
    }

    public void broadcastReset(final int seat) {
        synchronized (table) {
            for (final Player player : table) {
                player.sendReset(seat);
            }
        }
    }

    public void sendExistingSeatedPlayers(final Player player) {
        synchronized (avtIndices) {
            for (int seat = 0; seat < 4; seat++) {
                if (seats[seat] != null) {
                    player.sendSeating(seat, avtIndices[seat], names[seat]);
                    if (isReady[seat].get())
                        player.sendReady(seat);
                }
            }
        }
    }

    public String getTotalScore() {
        final String[] scoreLiterals = new String[totalScore.length];
        for (int i = 0; i < totalScore.length; i++)
            scoreLiterals[i] = String.valueOf(totalScore[i].get());

        return String.join(Server.SEND_DELIM, scoreLiterals);
    }

    public int getTradeGap() {
        return tradeGap;
    }

    public void allCardsDealtLatchCountDown() {
        allCardsDealtLatch.countDown();
    }

    public void roundReadyLatchCountDown() {
        roundReadyLatch.countDown();
    }

    public void tradeLatchCountDown() {
        tradeLatch.countDown();
    }

    public void exhibitionLatchCountDown() {
        exhibitionLatch.countDown();
    }
}