import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Server objects allow clients to connect to play Blackjack as a new player.
 *
 * @author Jordan Segalman
 */

public class Server {
    private static final int DEFAULT_PORT = 23366; // default server port
    private static final int DEFAULT_PLAYERS_PER_TABLE = 4; // default number of players per table
    private static final int DEFAULT_NUMBER_OF_DECKS = 2; // default number of decks in shoe

    private final int serverPort; // server port
    private final int playersPerTable; // number of players per table
    private final int numberOfDecks; // number of decks in shoe

    public static final String SEND_PREFIX = "SERVERMESSAGE";
    public static final String SEND_DELIM = "==";
    public static final String RECV_DELIM = "~~";

    public static int numCards = 26;
    public static boolean TEST_MODE = false;

    /**
     * Constructor for Server object.
     *
     * @param serverPort                Server port
     * @param playersPerTable           Number of players per table
     * @param startingMoney             Amount of money players start with
     * @param minimumBet                Minimum player bet
     * @param numberOfDecks             Number of decks in shoe
     * @param minimumCardsBeforeShuffle Minimum number of cards remaining before
     *                                  shuffling the shoe
     */

    public Server(final int serverPort, final int playersPerTable, final int numberOfDecks) {
        this.serverPort = serverPort;
        this.playersPerTable = playersPerTable;
        this.numberOfDecks = numberOfDecks;
    }

    /**
     * Starts the server and adds connected clients to new tables as new players.
     */

    public void start() {
        System.out.println("Starting Blackjack server\nServer port: " + serverPort + "\nPlayers per table: "
                + playersPerTable + "\nNumber of decks: " + numberOfDecks);
        ServerSocket serverSocket = null;
        try {
            System.out.println("Creating server socket");
            serverSocket = new ServerSocket(serverPort);
        } catch (final IOException e) {
            System.err.println("Could not start Blackjack server on port " + serverPort);
            System.exit(1);
        }
        try {
            System.out.println("Listening on port " + serverPort);
            final Table newTable = new Table(numberOfDecks);
            final Thread newTableThread = new Thread(newTable);
            newTable.setTabThread(newTableThread);
            newTableThread.start();
            while (true) {
                final Socket socket = serverSocket.accept();
                System.out.println("Received request from port " + socket.getPort());
                final Player newPlayer = new Player(socket, newTable);
                newTable.addPlayer(newPlayer);
                final Thread newPlayerThread = new Thread(newPlayer);
                newPlayerThread.start();
                newTable.setPlayerThread(newPlayer, newPlayerThread);
                newTable.sendExistingSeatedPlayers(newPlayer);
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
        System.err.println("Server Ended");
    }

    /**
     * Main method of the server that creates objects and executes other methods.
     *
     * @param args String array of arguments passed to the server
     */

    public static void main(final String[] args) {
        int serverPort = DEFAULT_PORT;
        final int playersPerTable = DEFAULT_PLAYERS_PER_TABLE;
        final int numberOfDecks = DEFAULT_NUMBER_OF_DECKS;

        for (int i = 0; i < args.length; i += 2) {
            final String option = args[i];
            String argument = null;

            try {
                argument = args[i + 1];
            } catch (final ArrayIndexOutOfBoundsException e) {
                System.err.println("Options: [-p serverPort]");
                System.exit(1);
            }
            switch (option) {
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
                        numCards = Integer.parseInt(argument);
                        if (numCards <= 0 || numCards > 26) {
                            throw new NumberFormatException();
                        }
                        TEST_MODE = true;
                    } catch (final NumberFormatException e) {
                        System.err.println("Num Cards must be an integer between 1 and 26");
                        System.exit(1);
                    }
                    break;
                default:
                    System.err.println("Options: [-p serverPort]");
                    System.exit(1);
                    break;
            }
        }
        final Server Server = new Server(serverPort, playersPerTable, numberOfDecks);
        Server.start();
    }
}