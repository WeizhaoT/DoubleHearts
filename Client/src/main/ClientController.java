package main;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import ui.*;

/**
 * Client objects connect to the server and coordinate between the client model
 * and view.
 *
 * @author Weizhao Tang
 */
public class ClientController {
    /** default server address */
    private static final String DEFAULT_SERVER_ADDRESS = "localhost";
    /** default server port */
    private static final int DEFAULT_SERVER_PORT = 23366;
    /** server address */
    private final String serverAddress;
    /** server port */
    private final int serverPort;

    /** prefix of message from server */
    public static final String RECV_PREFIX = "SERVERMESSAGE";
    /** delimiter of items in incoming message */
    public static final String RECV_DELIM = "==";
    /** prefix of message to server */
    public static final String SEND_PREFIX = "FROMCLIENT";
    /** delimiter of items in outgoing message */
    public static final String SEND_DELIM = "~~";

    /** flag indicating if in test mode */
    public static boolean TEST_MODE = false;
    /** number of cards in each hand */
    public static int numCards = 26;

    /** status flag indicating if self is waiting for a game to start */
    private boolean waitingForReady = false;
    /** number of card decks each frame */
    public static int numDecks = 2;

    /** client GUI frame */
    private ClientView view;
    /** client model handling communication */
    private ClientModel model;

    /**
     * Constructor for Client object.
     *
     * @param serverAddress Server address
     * @param serverPort    Server port
     */
    public ClientController(final String serverAddress, final int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    /**
     * Start a Swing worker that listens to server.
     */
    private void getServerMessage() {
        final SwingWorker<String, String> swingWorker = new SwingWorker<>() {
            @Override
            public String doInBackground() throws Exception {
                return model.getServerMessage();
            }

            @Override
            public void done() {
                try {
                    changeView(get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        };

        swingWorker.execute();
    }

    /**
     * Send message to server.
     * 
     * @param items message items to pack
     */
    public void sendToServer(final String... items) {
        if (ClientController.TEST_MODE)
            System.err.println("To Server: " + String.join(", ", items));

        model.sendToServer(items);
    }

    /**
     * React to incoming message.
     * 
     * @param serverMessage Message from server
     */
    private void changeView(final String serverMessage) {
        if (serverMessage == null)
            return;

        final String[] items = serverMessage.split(RECV_DELIM);
        if (items.length <= 1 || !RECV_PREFIX.equals(items[0]))
            return;

        int seatIndex, absLoc, timeLimit;
        String[] cardAliases;

        switch (items[1]) {
            case "WELCOME":
                view.showWelcomePanel(); // Open window upon receipt
                break;
            case "TAKESEAT":
                view.sitDown(Integer.parseInt(items[2]));
                break;
            case "PLAYERINFO":
                seatIndex = Integer.parseInt(items[2]);
                final int avtIndex = Integer.parseInt(items[3]);
                final String name = items[4];
                view.setPlayerInfo(seatIndex, avtIndex, name);
                break;
            case "DONOTSIT":
                view.showSeatErrMsg();
                break;
            case "NEWFRAME":
                waitingForReady = true;
                view.enableReadyButton();
                break;
            case "ISREADY":
                seatIndex = Integer.parseInt(items[2]);
                view.setReady(seatIndex);
                view.enableHandControl(false);
                break;
            case "DEAL": // Start dealing cards
                waitingForReady = false;
                view.resetForNewFrame();
                view.setNumDealingCards(Integer.parseInt(items[2]), Integer.parseInt(items[3]));
                break;
            case "ADD": // Deal one card
                if (view.addCard(items[2])) {
                    sendToServer("ALLDEALT");
                    // view.setFirstRound(true);
                }
                break;
            case "TRADESTART": // Start trading
                view.enterTradingPhase(Integer.parseInt(items[2]), Integer.parseInt(items[3]));
                view.enableHandControl(true);
                break;
            case "TRADEREADY":
                view.setTradeReady(Integer.parseInt(items[2]));
                break;
            case "TRADEIN":
                view.tradeInCards(getSubStrArray(items, 2));
                break;
            case "EXHIBIT": // Start showing
                view.enableHandControl(true);
                view.enterShowingPhase(Integer.parseInt(items[2]));
                break;
            case "SHOWN":
                absLoc = Integer.parseInt(items[2]);
                view.displayShownCards(absLoc, getSubStrArray(items, 3));
                break;
            case "OPENING":
                view.openFrame(Integer.parseInt(items[3]), Integer.parseInt(items[2]));
                break;
            case "ASSET": // Start a new round and record assets of last round
                timeLimit = Integer.parseInt(items[2]);
                absLoc = Integer.parseInt(items[3]);
                view.addAsset(absLoc, timeLimit, getSubStrArray(items, 4));
                break;
            case "LEAD":
            case "FOLLOW":
                timeLimit = Integer.parseInt(items[2]);
                absLoc = Integer.parseInt(items[3]);
                cardAliases = getSubStrArray(items, 4);
                view.playTurn("LEAD".equals(items[1]), timeLimit, absLoc, cardAliases);
                break;
            case "ENDFRAME":
                waitingForReady = true;
                view.setTotalScore(getSubStrArray(items, 2));
                break;
            case "CONNRESET": // Received when a player stops connection to server
                absLoc = Integer.parseInt(items[2]);
                view.resetForDisconnection(absLoc, waitingForReady);
                break;
            case "GAMEOVER":
                System.err.println("GAMEOVER");
                System.exit(1);
        }
        getServerMessage();
    }

    /**
     * Obtain the subarray of given array.
     * 
     * @param items Origial array of items
     * @param start Starting index of subarray
     * @return Subarray
     */
    private String[] getSubStrArray(final String[] items, final int start) {
        if (start >= items.length)
            return new String[0];

        return Arrays.asList(items).subList(start, items.length).toArray(new String[0]);
    }

    /**
     * Sets up the client GUI and gets the first message from the server.
     */
    public void start() {
        System.out.println("Starting client\n");
        view = new ClientView(this);
        model = new ClientModel(serverAddress, serverPort);
        getServerMessage();
    }

    /**
     * Main method of the client that creates objects and executes other methods.
     *
     * @param args String array of arguments passed to the client
     */
    public static void main(final String[] args) {
        String serverAddress = DEFAULT_SERVER_ADDRESS;
        int serverPort = DEFAULT_SERVER_PORT;
        for (int i = 0; i < args.length; i += 2) {
            final String option = args[i];
            String argument = null;
            try {
                argument = args[i + 1];
            } catch (final ArrayIndexOutOfBoundsException e) {
                System.err.println("Options: [-a serverAddress] [-p serverPort] [-l (en|ch)]");
                System.exit(1);
            }
            switch (option) {
                case "-a":
                    serverAddress = argument;
                    break;
                case "-p":
                    try {
                        serverPort = Integer.parseInt(argument);
                    } catch (final NumberFormatException e) {
                        System.err.println("Server port must be an integer");
                        System.exit(1);
                    }
                    break;
                case "-t":
                    try {
                        TEST_MODE = true;
                        numCards = Integer.parseInt(argument);
                        if (numCards <= 0 || numCards > 26) {
                            throw new NumberFormatException();
                        }
                    } catch (final NumberFormatException e) {
                        System.err.println("Num Cards must be an integer between 1 and 26");
                        System.exit(1);
                    }
                    break;
                case "-l":
                    if (argument.strip().toLowerCase().equals("en"))
                        MyText.language = 0;
                    else if (argument.strip().toLowerCase().equals("ch"))
                        MyText.language = 1;
                    else {
                        System.err.println("Language must be en(English) or ch(Chinese)");
                        System.exit(1);
                    }
                    break;
                default:
                    System.err.println("Options: [-a serverAddress] [-p serverPort] [-l (en|ch)]");
                    System.exit(1);
                    break;
            }
        }

        MyFont.registerFont();

        final ClientController controller = new ClientController(serverAddress, serverPort);
        controller.start();
    }
}