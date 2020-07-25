package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

/**
 * BlackjackClient objects connect to the Blackjack server and coordinate
 * between the client model and view.
 *
 * @author Jordan Segalman
 */

public class ClientController {
    private static final String DEFAULT_SERVER_ADDRESS = "localhost"; // default server address
    private static final int DEFAULT_SERVER_PORT = 23366; // default server port
    private final String serverAddress; // server address
    private final int serverPort; // server port

    public static final String RECV_PREFIX = "SERVERMESSAGE";
    public static final String RECV_DELIM = "==";
    public static final String SEND_PREFIX = "FROMCLIENT";
    public static final String SEND_DELIM = "~~";

    public static boolean TEST_MODE = false;
    public static int numCards = 26;

    private boolean waitingForReady = false;

    public static int numDecks = 2;

    private ClientView view; // client GUI view
    private ClientModel model;

    /**
     * Constructor for BlackjackClient object.
     *
     * @param serverAddress Server address
     * @param serverPort    Server port
     */

    public ClientController(final String serverAddress, final int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

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

    public void sendToServer(final String... items) {
        System.err.println("To Server: " + String.join(", ", items));
        model.sendToServer(items);
    }

    public void sendToServer(final ArrayList<String> items) {
        model.sendToServer(items);
    }

    private void changeView(final String serverMessage) {
        if (serverMessage == null)
            return;

        final String[] items = serverMessage.split(RECV_DELIM);
        if (items.length <= 1 || !RECV_PREFIX.equals(items[0]))
            return;

        int seatIndex, absLoc;
        String[] cardAliases;

        switch (items[1]) {
            case "WELCOME":
                view.showWelcomePanel();
                break;
            case "TAKESEAT":
                waitingForReady = true;
                view.sitDown(Integer.parseInt(items[2]));
                break;
            case "PLAYERINFO":
                seatIndex = Integer.parseInt(items[2]);
                final int avtIndex = Integer.parseInt(items[3]);
                final String name = items[4];
                view.setPlayerInfo(seatIndex, avtIndex, name);
                break;
            case "DONOTSIT":
                view.setSeatErrMsg("Failed to sit down");
                break;
            case "NEWFRAME":
                view.enableReadyButton();
                break;
            case "ISREADY":
                seatIndex = Integer.parseInt(items[2]);
                view.setReady(seatIndex);
                view.enableHandControl(false);
                break;
            case "DEAL":
                waitingForReady = false;
                view.resetForNewFrame();
                break;
            case "ADD":
                if (view.addCard(items[2])) {
                    sendToServer("ALLDEALT");
                    view.setFirstRound(true);
                }
                break;
            case "TRADESTART":
                view.setMaskMode("TRADE");
                view.setTradeWaiting(Integer.parseInt(items[2]));
                view.enableHandControl(true);
                break;
            case "TRADEREADY":
                view.setTradeReady(Integer.parseInt(items[2]));
                break;
            case "TRADEIN":
                view.tradeInCards(getSubStrArray(items, 2));
                break;
            case "EXHIBIT":
                view.setMaskMode("SHOWABLE");
                view.enableHandControl(true);
                view.setShowCardWaiting();
                break;
            case "SHOWN":
                absLoc = Integer.parseInt(items[2]);
                view.showExhibitedCards(absLoc, getSubStrArray(items, 3));
                break;
            case "ASSET":
                absLoc = Integer.parseInt(items[2]);
                view.resetRoundProg();
                view.addAsset(absLoc, getSubStrArray(items, 3));
                view.clearLastRound();
                if (!view.isLastRound()) {
                    view.showNextWaiting(absLoc - 1);
                    view.setFirstRound(false);
                    if (view.getRelativeLoc(absLoc) == 0)
                        view.setMaskMode("NORMAL");
                    else
                        view.setMaskMode("ALL");
                }
                break;
            case "LEAD":
                absLoc = Integer.parseInt(items[2]);
                view.setLeadCards(cardAliases = getSubStrArray(items, 3));
                view.showCards(absLoc, cardAliases);
                view.incrRoundProg();
                view.showNextWaiting(absLoc);
                view.hideAllHistory();
                if (view.getRelativeLoc(absLoc) != 0)
                    view.setMaskMode("NORMAL");

                break;
            case "FOLLOW":
                absLoc = Integer.parseInt(items[2]);
                view.showCards(absLoc, getSubStrArray(items, 3));
                view.incrRoundProg();
                if (!view.isRoundEnd())
                    view.showNextWaiting(absLoc);

                view.hideAllHistory();
                break;
            case "ENDFRAME":
                waitingForReady = true;
                view.setTotalScore(getSubStrArray(items, 2));
                break;
            case "CONNRESET":
                absLoc = Integer.parseInt(items[2]);
                view.resetForPeer(absLoc, waitingForReady);
                break;
            case "GAMEOVER":
                System.err.println("GAMEOVER");
                System.exit(1);
        }

        getServerMessage();
    }

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
                System.err.println("Options: [-a serverAddress] [-p serverPort]");
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
                default:
                    System.err.println("Options: [-a serverAddress] [-p serverPort]");
                    System.exit(1);
                    break;
            }
        }

        final ClientController controller = new ClientController(serverAddress, serverPort);
        controller.start();
    }
}