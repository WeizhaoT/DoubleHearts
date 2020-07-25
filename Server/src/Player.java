import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;

/**
 * A player object represents a player in Double Hearts.
 *
 * @author Weizhao Tang
 */

public class Player implements Runnable {
    private Table table; // table to join
    private BufferedReader in; // in to client
    private PrintWriter out; // out from client

    private int seatIndex = -1;
    private String name;
    private Hand hand; // player hand to hold cards
    private Property assets; // player hand to hold cards

    private boolean normal = true;

    private CountDownLatch frameInitLatch; // latch to wait for all players to join game
    private CountDownLatch framePlayingLatch; // latch to wait for all players to join game
    private CountDownLatch frameEndingLatch; // latch to wait for all players to join game

    private final CountUpDownLatch seatLatch = new CountUpDownLatch(1);
    private final CountUpDownLatch readyLatch = new CountUpDownLatch(1);
    private final CountUpDownLatch dealLatch = new CountUpDownLatch(1);
    private final CountUpDownLatch tradeLatch = new CountUpDownLatch(1);
    private final CountUpDownLatch showLatch = new CountUpDownLatch(1);
    private final CountUpDownLatch cardLatch = new CountUpDownLatch(1);

    private final ArrayList<Card> playedCards = new ArrayList<>();

    // private final Listener listener;
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

    public Player(Socket socket, Table table) {
        this.table = table;
        listenerThread = new Thread(new Listener());
        try {
            InputStreamReader isr = new InputStreamReader(socket.getInputStream(), "UTF-8"); // input stream reader from
                                                                                             // socket

            in = new BufferedReader(isr);
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
        } catch (IOException e) {
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
        hand = new Hand();
        assets = new Property();

        listenerThread.start();
        resetFrame();

        waitForSeating();

        do {
            try {
                playPigFrame();
            } catch (InterruptedException e) {
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
            } catch (IOException e) {
                normal = false;
                main.interrupt();
                table.dealWithConnectionLoss(Player.this, seatIndex);
            }
        }
    }

    private void parseMessage(String clientMessage) {
        String[] items = clientMessage.split(Server.RECV_DELIM);
        System.err.println("From Client: " + String.join(", ", items));

        if (!status.toString().equals(items[1])) {
            System.err.println("Warning: message improbable under status " + status);
            // return;
        }

        switch (items[1]) {
            case "SITDOWN":
                int seat = Integer.parseInt(items[2]);
                int avtIndex = Integer.parseInt(items[3]);
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
                readyLatch.countDown();
                break;
            case "ALLDEALT":
                dealLatch.countDown();
                break;
            case "TRADE":
                table.broadcastTradeReady(seatIndex);
                for (int i = 0; i < 3; i++) {
                    table.tradeOut[seatIndex][i] = items[2 + i];
                }
                tradeLatch.countDown();
                break;
            case "SHOW":
                for (int i = 2; i < items.length; i++)
                    items[i] += "x";

                table.broadcastShown(seatIndex, getSubStrArray(items, 2));
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
        sendToClient("NEWFRAME", String.valueOf(table.numDecks()));

        status = WaitStatus.READY;
        synchronized (readyLatch) {
            readyLatch.await();
        }
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

        if (!normal)
            return;

        frameInitLatch.await();

        resetFrame();

        String totalScores = table.getTotalScore();
        sendToClient("ENDFRAME", totalScores);
    }

    public void addCard(Card card) {
        hand.addCard(card);
        sendToClient("ADD", card.fullAlias());
    }

    public void addAsset(ArrayList<Card> cards) {
        for (Card card : cards)
            assets.addProperty(card);
    }

    public int getScore() {
        return assets.propertyScore(table.numDecks());
    }

    private void resetFrame() {
        hand.clear();
        assets.clear();

        frameInitLatch = new CountDownLatch(1);
        framePlayingLatch = new CountDownLatch(1);
        frameEndingLatch = new CountDownLatch(1);

        readyLatch.reset();
        dealLatch.reset();
        tradeLatch.reset();
        showLatch.reset();
        cardLatch.reset();
    }

    public void sendSeating(int seatIndex, int avtIndex, String name) {
        sendToClient("PLAYERINFO", String.valueOf(seatIndex), String.valueOf(avtIndex), name);
    }

    public void sendReady(int seatIndex) {
        sendToClient("ISREADY", String.valueOf(seatIndex));
    }

    public void sendDeal() {
        sendToClient("DEAL");
    }

    public void sendTradeStart(int tradeGap) {
        sendToClient("TRADESTART", String.valueOf(tradeGap));
    }

    public void sendTradeReady(int seat) {
        sendToClient("TRADEREADY", String.valueOf(seat));
    }

    public void sendTradeIn(String[] cardAliases) {
        sendToClient("TRADEIN", String.join(Server.SEND_DELIM, cardAliases));
    }

    public void sendExhibition() {
        sendToClient("EXHIBIT");
    }

    public void sendShown(int seatIndex, String[] cardAliases) {
        sendToClient("SHOWN", String.valueOf(seatIndex), String.join(Server.SEND_DELIM, cardAliases));
    }

    public void sendPlayed(boolean lead, int seatIndex, Collection<Card> cards) {
        sendToClient(lead ? "LEAD" : "FOLLOW", String.valueOf(seatIndex), Card.concatCards(Server.SEND_DELIM, cards));
    }

    public void sendAsset(int seatIndex, Collection<Card> cards) {
        sendToClient("ASSET", String.valueOf(seatIndex), Card.concatCards(Server.SEND_DELIM, cards));
    }

    public void sendReset(int seatIndex) {
        sendToClient("CONNRESET", String.valueOf(seatIndex));
    }

    public ArrayList<Card> playTurn() throws InterruptedException {
        cardLatch.await();
        cardLatch.reset();
        return playedCards;
    }

    private void waitForSeating() {
        try {
            seatLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Hand getHand() {
        return hand;
    }

    private String[] getSubStrArray(String[] items, int start) {
        return Arrays.asList(items).subList(start, items.length).toArray(new String[0]);
    }

    private void sendToClient(String... msgs) {
        if (!msgs[0].equals("ADD"))
            System.err.println("To Client " + seatIndex + ": " + String.join(", ", msgs));

        ArrayList<String> filteredMsgs = new ArrayList<>();

        for (String msg : msgs) {
            if (msg.length() > 0)
                filteredMsgs.add(msg);
        }

        String output = Server.SEND_PREFIX + Server.SEND_DELIM + String.join(Server.SEND_DELIM, filteredMsgs);
        out.println(output);
    }

    /**
     * Decrements the start latch.
     */

    public void frameInitLatchCountDown() {
        frameInitLatch.countDown();
    }

    public void framePlayingLatchCountDown() {
        framePlayingLatch.countDown();
    }

    public void frameEndingLatchCountDown() {
        frameEndingLatch.countDown();
    }
}