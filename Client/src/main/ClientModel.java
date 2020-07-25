package main;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * BlackjackClientModel objects hold client information.
 *
 * @author Jordan Segalman
 */

public class ClientModel {
    private static final int MESSAGE_WAIT_TIME = 300; // time to wait between server messages
    private Socket socket; // socket on server address and port
    private BufferedReader in; // in to server
    private PrintWriter out; // out from server

    /**
     * Constructor for ClientModel object.
     *
     * @param serverAddress Server address
     * @param serverPort    Server port
     */

    public ClientModel(final String serverAddress, final int serverPort) {
        try {
            socket = new Socket(serverAddress, serverPort);
        } catch (final IOException e) {
            System.err.println("No Blackjack server running on port " + serverPort + " at address " + serverAddress);
            System.exit(1);
        }
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
     * Gets a message sent by the server.
     *
     * @return message sent by the server
     */

    public String getServerMessage() {
        String serverMessage = null;
        try {
            Thread.sleep(MESSAGE_WAIT_TIME);
            while (serverMessage == null)
                serverMessage = in.readLine();

            final String[] items = serverMessage.split(ClientController.RECV_DELIM);
            if (!items[1].equals("ADD"))
                System.err.println("From Server: " + serverMessage);
        } catch (final SocketException e) {
            if (e.getMessage().contains("Connection reset")) {
                System.err.println("Lost Connection to Server");
                System.exit(1);
            }
            e.printStackTrace();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return serverMessage;
    }

    /**
     * Sends a message to the server.
     *
     * @param clientMessage Message to send to server
     */

    public void sendToServer(final String... items) {
        final ArrayList<String> filteredMsgs = new ArrayList<>();

        for (final String msg : items) {
            if (msg.length() > 0)
                filteredMsgs.add(msg);
        }

        final String output = ClientController.SEND_PREFIX + ClientController.SEND_DELIM
                + String.join(ClientController.SEND_DELIM, filteredMsgs);

        out.println(output);
    }

    public void sendToServer(final ArrayList<String> items) {
        out.println(ClientController.SEND_PREFIX + ClientController.SEND_DELIM
                + String.join(ClientController.SEND_DELIM, items));
    }

    /**
     * Sends a message to the server to quit the game and closes the socket.
     */

    public void quitGame() {
        sendToServer("QUIT");
        try {
            socket.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}