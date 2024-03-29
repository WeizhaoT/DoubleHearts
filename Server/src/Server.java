import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Server objects allow clients to connect to play Double Hearts as a new
 * player.
 *
 * @author Weizhao Tang
 */

public class Server {
    private static final int DEFAULT_PORT = 23366; // default server port

    private final int serverPort; // server port

    public static final String SEND_PREFIX = "SERVERMESSAGE";
    public static final String SEND_DELIM = "==";
    public static final String RECV_DELIM = "~~";

    public static int numDecks = 2;
    public static int numCards = 26;
    public static boolean TEST_MODE = false;

    /**
     * Constructor for Server object.
     *
     * @param serverPort Server port
     */
    public Server(final int serverPort) {
        this.serverPort = serverPort;
    }

    /**
     * Starts the server and adds connected clients to new tables as new players.
     */

    public void start() {
        System.out.println("Starting Blackjack server\nServer port: " + serverPort);
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
            final Table newTable = new Table(numDecks);
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

        for (int i = 0; i < args.length; i += 2) {
            final String option = args[i];
            String argument = null;

            try {
                argument = args[i + 1];
            } catch (final ArrayIndexOutOfBoundsException e) {
                System.err.println(
                        "Options: [-p serverPort] [-b baseScore] [-e exposureMult] [-g obtainingMult] [-d numDecks]");
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
                case "-d":
                    try {
                        numDecks = Integer.parseInt(argument);
                        if (numDecks != 1 && numDecks != 2) {
                            throw new NumberFormatException();
                        }
                    } catch (final NumberFormatException e) {
                        System.err.println("Num Decks must be 1 or 2");
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
                case "-b":
                    try {
                        Card.baseScore = Integer.parseInt(argument);
                        if (Card.baseScore <= 0) {
                            throw new NumberFormatException();
                        }
                    } catch (final NumberFormatException e) {
                        System.err.println("Base score must be a positive int");
                        System.exit(1);
                    }
                    break;
                case "-e":
                    try {
                        Card.MULT_EXP = Double.parseDouble(argument);
                        if (Card.MULT_EXP <= 0) {
                            throw new NumberFormatException();
                        }
                    } catch (final NumberFormatException e) {
                        System.err.println("Exposure multiplier must be a positive floating point number");
                        System.exit(1);
                    }
                    break;
                case "-g":
                    try {
                        Card.MULT_GET = Double.parseDouble(argument);
                        if (Card.MULT_GET <= 0) {
                            throw new NumberFormatException();
                        }
                    } catch (final NumberFormatException e) {
                        System.err.println("Obtaining multiplier must be a positive floating point number");
                        System.exit(1);
                    }
                    break;
                default:
                    System.err.println(
                            "Options: [-p serverPort] [-b baseScore] [-e exposureMult] [-g obtainingMult] [-d numDecks]");
                    System.exit(1);
                    break;
            }
        }
        final Server Server = new Server(serverPort);
        Server.start();
    }
}