import java.io.*;
import java.net.Socket;
import java.util.*;

/**
 * A player object represents a player in Double Hearts.
 *
 * @author Weizhao Tang
 */

public class Player implements Runnable {
    private static final String timeLimitTrade = "45";
    private static final String timeLimitShow = "20";
    private static final String timeLimitPlay = "40";

    private final Table table; // table to join
    private BufferedReader in; // in to client
    private PrintWriter out; // out from client

    private int seatIndex = -1;
    private String name;
    private Asset assets; // player hand to hold cards

    private boolean normal = true;

    private final CountUpDownLatch framePlayingLatch = new CountUpDownLatch(1);
    private final CountUpDownLatch frameEndingLatch = new CountUpDownLatch(1);

    private final CountUpDownLatch seatLatch = new CountUpDownLatch(1);
    private final CountUpDownLatch readyLatch = new CountUpDownLatch(1);
    private final CountUpDownLatch dealLatch = new CountUpDownLatch(1);
    private final CountUpDownLatch tradeLatch = new CountUpDownLatch(1);
    private final CountUpDownLatch showLatch = new CountUpDownLatch(1);
    private final CountUpDownLatch cardLatch = new CountUpDownLatch(1);

    private final ArrayList<Card> playedCards = new ArrayList<>();

    private WaitStatus status = WaitStatus.SITDOWN;

    private Thread main;
    private final Thread listenerThread;

    private enum WaitStatus {
        SITDOWN, READY, ALLDEALT, TRADE, SHOW, PLAY,
    }

    /**
     * Constructor for Player object.
     *
     * @param socket Socket from server socket
     * @param table  Table the player joined
     */

    public Player(final Socket socket, final Table table) {
        this.table = table;
        listenerThread = new Thread(new Listener());
        try {
            final InputStreamReader isr = new InputStreamReader(socket.getInputStream(), "UTF-8"); // input stream
                                                                                                   // reader from
            // socket

            in = new BufferedReader(isr);
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Player thread run method.
     */

    @Override
    public void run() {
        sendToClient("WELCOME");

        main = Thread.currentThread();
        assets = new Asset();

        listenerThread.start();
        resetFrame();

        try {
            seatLatch.await();
        } catch (final InterruptedException e) {
            e.printStackTrace();
            return;
        }

        do {
            try {
                playPigFrame();
            } catch (final InterruptedException e) {
                System.err.println("Player " + seatIndex + " \"" + name + "\" interrupted");
                resetFrame();
            }
        } while (normal);
    }

    private class Listener implements Runnable {
        public void run() {
            String clientMessage;

            try {
                while (true) {
                    if ((clientMessage = in.readLine()) != null) {
                        parseMessage(clientMessage);
                    }
                }
            } catch (final IOException e) {
                normal = false;
                main.interrupt();
                table.dealWithConnectionLoss(Player.this, seatIndex);
            }
        }
    }

    private void parseMessage(final String clientMessage) {
        final String[] items = clientMessage.split(Server.RECV_DELIM);
        System.err.println("From Client: " + seatIndex + " \"" + name + "\": " + String.join(", ", items));

        if (!status.toString().equals(items[1])) {
            System.err.println("Warning: improbable message under status " + status);
        }

        switch (items[1]) {
            case "SITDOWN":
                final int seat = Integer.parseInt(items[2]);
                final int avtIndex = Integer.parseInt(items[3]);
                name = items[4];
                if (table.sitDown(Player.this, seat, avtIndex, name)) {
                    sendToClient("TAKESEAT", items[2]);
                    seatIndex = seat;
                } else {
                    sendToClient("DONOTSIT");
                }
                seatLatch.countDown();
                break;
            case "READY":
                table.broadcastReady(seatIndex);
                dealLatch.reset();
                readyLatch.countDown();
                break;
            case "ALLDEALT":
                tradeLatch.reset();
                showLatch.reset();
                dealLatch.countDown();
                break;
            case "TRADE":
                table.broadcastTradeReady(seatIndex);
                for (int i = 0; i < 3; i++) {
                    table.tradeOut[seatIndex][i] = items[2 + i];
                }
                showLatch.reset();
                tradeLatch.countDown();
                break;
            case "SHOW":
                for (int i = 2; i < items.length; i++)
                    items[i] += "x";

                table.broadcastShown(seatIndex, getSubStrArray(items, 2));
                cardLatch.reset();
                showLatch.countDown();
                break;
            case "PLAY":
                playedCards.clear();
                for (int i = 2; i < items.length; i++)
                    playedCards.add(new Card(items[i]));

                cardLatch.countDown();
                break;
            default:
                System.err.println("Warning: message not recognized");
        }
    }

    private void playPigFrame() throws InterruptedException {
        sendToClient("NEWFRAME");

        status = WaitStatus.READY;
        readyLatch.await();
        table.roundReadyLatchCountDown();

        status = WaitStatus.ALLDEALT;
        dealLatch.await();
        table.allCardsDealtLatchCountDown();

        if (table.getTradeGap() != 0) {
            status = WaitStatus.TRADE;
            tradeLatch.await();
            table.tradeLatchCountDown();
        }

        status = WaitStatus.SHOW;
        showLatch.await();
        table.exhibitionLatchCountDown();

        framePlayingLatch.await();
        status = WaitStatus.PLAY;

        frameEndingLatch.await();

        resetFrame();

        sendToClient("ENDFRAME", table.getTotalScore());
    }

    public void addCard(final Card card) {
        sendToClient("ADD", card.fullAlias());
    }

    public void addAsset(final ArrayList<Card> cards) {
        for (final Card card : cards)
            assets.addAsset(card);
    }

    public int getScore() {
        return assets.getScore(Table.numberOfDecks);
    }

    private void resetFrame() {
        assets.clear();

        framePlayingLatch.reset();
        frameEndingLatch.reset();

        readyLatch.reset();
        dealLatch.reset();
        tradeLatch.reset();
        showLatch.reset();
        cardLatch.reset();
    }

    public void sendSeating(final int seatIndex, final int avtIndex, final String name) {
        sendToClient("PLAYERINFO", String.valueOf(seatIndex), String.valueOf(avtIndex), name);
    }

    public void sendReady(final int seatIndex) {
        sendToClient("ISREADY", String.valueOf(seatIndex));
    }

    public void sendDeal() {
        sendToClient("DEAL");
    }

    public void sendTradeStart(final int tradeGap) {
        sendToClient("TRADESTART", timeLimitTrade, String.valueOf(tradeGap));
    }

    public void sendTradeReady(final int seat) {
        sendToClient("TRADEREADY", String.valueOf(seat));
    }

    public void sendTradeIn(final String[] cardAliases) {
        sendToClient("TRADEIN", String.join(Server.SEND_DELIM, cardAliases));
    }

    public void sendExhibition() {
        sendToClient("EXHIBIT", timeLimitShow);
    }

    public void sendShown(final int seatIndex, final String[] cardAliases) {
        sendToClient("SHOWN", String.valueOf(seatIndex), String.join(Server.SEND_DELIM, cardAliases));
    }

    public void sendPlayed(final boolean lead, final int seatIndex, final Collection<Card> cards) {
        sendToClient(lead ? "LEAD" : "FOLLOW", timeLimitPlay, String.valueOf(seatIndex),
                Card.concatCards(Server.SEND_DELIM, cards));
    }

    public void sendAsset(final int seatIndex, final Collection<Card> cards) {
        sendToClient("ASSET", timeLimitPlay, String.valueOf(seatIndex), Card.concatCards(Server.SEND_DELIM, cards));
    }

    public void sendReset(final int seatIndex) {
        sendToClient("CONNRESET", String.valueOf(seatIndex));
    }

    public ArrayList<Card> playTurn() throws InterruptedException {
        cardLatch.await();
        cardLatch.reset();
        return playedCards;
    }

    public void framePlayingLatchCountDown() {
        framePlayingLatch.countDown();
    }

    public void frameEndingLatchCountDown() {
        frameEndingLatch.countDown();
    }

    private String[] getSubStrArray(final String[] items, final int start) {
        return Arrays.asList(items).subList(start, items.length).toArray(new String[0]);
    }

    private void sendToClient(final String... msgs) {
        if (!msgs[0].equals("ADD"))
            System.err.println("To Client " + seatIndex + " \"" + name + "\": " + String.join(", ", msgs));

        final ArrayList<String> filteredMsgs = new ArrayList<>();

        for (final String msg : msgs) {
            if (msg.length() > 0)
                filteredMsgs.add(msg);
        }

        final String output = Server.SEND_PREFIX + Server.SEND_DELIM + String.join(Server.SEND_DELIM, filteredMsgs);
        out.println(output);
    }
}