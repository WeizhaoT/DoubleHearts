package main;

import ui.*;
import panel.*;
import layout.*;
import element.*;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.*;
import javax.sound.sampled.*;

/**
 * {@code ClientView} objects create the top-level GUI for a player.
 *
 * @author Weizhao Tang
 */

public class ClientView extends JFrame implements ActionListener {
    static final long serialVersionUID = 1L;

    /** Main routine that takes server messages and pass commands */
    private final ClientController controller;

    /** Seat index of self, in {0, 1, 2, 3} */
    public int mySeatIndex = -1024;
    /** Name of self, set after server's confirmation of seating */
    public String myName;
    /** Index of self-chosen avatar */
    public int myAvatarIndex;

    /** Number of players that has shown cards */
    private int numShown;
    /** Number of players that has played cards this round */
    private int roundProg;

    /** Avatar indices of all players, indexed by absolute seating position */
    private final int[] allAvtIndices = new int[] { -1, -1, -1, -1 };
    /** Ready flags of all players, indexed by absolute seating position */
    private final boolean[] allReady = new boolean[] { false, false, false, false };
    /** Names of all players, indexed by absolute seating position */
    private final String[] allPlayerNames = new String[] { null, null, null, null };

    /** Layout object of welcome panel */
    private WelcomeLayout welcomeLayout;
    /** Layout object of table panel */
    private PokerGameLayout pokerGameLayout;

    /** Welcome panel, where players input names, select avatars and take seats */
    private JPanel welcomePanel;
    /** Poker game panel, where players play cards */
    private JPanel pokerGamePanel;

    // Welcome panel components

    /** List of seats on the welcome panel */
    private final SeatPanel[] seatPanels = new SeatPanel[4];
    /** Error message label for seating failure */
    private JLabel seatErrLabel;
    /** Error message label for illegal name input */
    private JLabel userInfoErrLabel;
    /** Text field for name input */
    private JTextField nameField;
    /** List of avatars available to choose */
    private final MaskedAvatar[] avatars = new MaskedAvatar[WelcomeLayout.numAvt];
    /** Avatar indicator showing which one has been chosen */
    private JLabel indicator;

    // Poker game panel components

    /** List of panels for players, including name, avatar and total score */
    private final PlayerPanel[] playerPanels = new PlayerPanel[4];
    /** List of panels for players, including their asset cards in this frame */
    private final AssetPanel[] assetPanels = new AssetPanel[4];
    /** Hand of cards and interactive buttons */
    private HandPanel handPanel;
    private RulePanel rulePanel;
    /** Speaker label to turn on/off the sound effects */
    private Speaker speaker;
    /** Center panel including card showing panels, error message labels, etc. */
    private CenterPanel centerPanel;

    /** Map of all loaded sound effect clips */
    private static final HashMap<String, Clip> clipMap = new HashMap<>();

    private final ReentrantLock actionLock = new ReentrantLock();

    /**
     * Constructor for ClientView object.
     *
     * @param controller Client GUI controller
     */

    public ClientView(final ClientController controller) {
        this.controller = controller;
        setupWindowListener(this.controller);

        setupFrame();
        createPanels();
    }

    /**
     * Sets up the window listener.
     *
     * @param controller Client GUI controller
     */

    private void setupWindowListener(final ClientController controller) {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                final int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to quit?", null,
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
    }

    /**
     * Sets up the frame.
     */
    private void setupFrame() {
        setTitle(MyText.getTitle());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }

    /**
     * Creates all of the panels.
     */
    private void createPanels() {
        welcomeLayout = new WelcomeLayout(20);
        pokerGameLayout = new PokerGameLayout(20, 20);

        setLayout(new FullWindowLayout(welcomeLayout, pokerGameLayout));
        createWelcomePanel();
        createPokerGamePanel();
        pack();
        setResizable(true);
        setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH); // Maximize window at start
    }

    /**
     * Load all sound effects and register them in map
     */
    public static void loadAllSoundEffects() {
        for (final String filename : new String[] { "drop", "play", "alarm2", "deal" }) {
            try (InputStream inputStream = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("Sounds/" + filename + ".wav")) {
                final BufferedInputStream bufferedStream = new BufferedInputStream(inputStream);
                final AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bufferedStream);
                final Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                clipMap.put(filename, clip);
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                System.err.println("Error: failed to load " + filename + ".wav: " + e.getClass());
                System.exit(1);
            }
        }
    }

    /**
     * Shows changes made to GUI.
     */
    public void showChanges() {
        revalidate();
        repaint();
        setVisible(true);
    }

    /**
     * Creates the welcome panel.
     */
    private void createWelcomePanel() {
        welcomePanel = new JPanel(welcomeLayout);
        welcomePanel.setBackground(MyColors.darkBlue);

        // Table image at center of seats
        welcomePanel.add(ImageLabel.getTable(), WelcomeLayout.TABLE);

        // Seats of SeatPanel class, including an avatar, a seat button and a name
        for (int i = 0; i < 4; i++) {
            seatPanels[i] = new SeatPanel(i, this);
            welcomePanel.add(seatPanels[i], WelcomeLayout.ALLSEATS[i]);
        }

        // Error message label for failing to take a seat
        seatErrLabel = new JLabel("", SwingConstants.CENTER);
        seatErrLabel.setForeground(MyColors.errmsg);
        seatErrLabel.setFont(MyText.getErrMsgFont());
        welcomePanel.add(seatErrLabel, WelcomeLayout.TABLEERR);

        // Hint label "Please input your name"
        final JLabel nameLabel = new JLabel(MyText.getNameHint(), SwingConstants.CENTER);
        nameLabel.setForeground(MyColors.text);
        nameLabel.setFont(MyText.getHintFont());
        welcomePanel.add(nameLabel, WelcomeLayout.NAMELABEL);

        // Text field where players input their names
        nameField = new JTextField("");
        nameField.setHorizontalAlignment(SwingConstants.CENTER);
        nameField.setFont(MyFont.nameField);
        welcomePanel.add(nameField, WelcomeLayout.NAME);

        // Error message label for illegal names
        userInfoErrLabel = new JLabel("", SwingConstants.CENTER);
        userInfoErrLabel.setForeground(MyColors.errmsg);
        userInfoErrLabel.setFont(MyText.getErrMsgFont());
        welcomePanel.add(userInfoErrLabel, WelcomeLayout.NAMEERR);

        // Hint label "Please choose your avatar"
        final JLabel avatarLabel = new JLabel(MyText.getAvatarHint(), SwingConstants.CENTER);
        avatarLabel.setForeground(MyColors.text);
        avatarLabel.setFont(MyText.getHintFont());
        welcomePanel.add(avatarLabel, WelcomeLayout.AVTLABEL);

        // Choose an avatar randomly in advance
        myAvatarIndex = (new Random()).nextInt(WelcomeLayout.numAvt);

        // Indicator of chosen avatar
        indicator = ImageLabel.getIndicator();
        indicator.setOpaque(false);
        welcomePanel.add(indicator, WelcomeLayout.INDICATOR);

        // Mouse listener of all avatars; players press left button on an avatar to
        // choose it
        final MouseAdapter indicatorAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) {
                    return;
                }

                synchronized (this) {
                    final MaskedAvatar avatar = (MaskedAvatar) e.getComponent();
                    final int i = Integer.parseInt(avatar.getName());
                    chooseAvatar(i);
                    showChanges();
                }
            }
        };

        // Setup all avatars and their common mouse listener
        for (int i = 0; i < WelcomeLayout.numAvt; i++) {
            avatars[i] = new MaskedAvatar(i, WelcomeLayout.minAvatarScale);
            avatars[i].addMouseListener(indicatorAdapter);
            welcomePanel.add(avatars[i], WelcomeLayout.AVATAR + i);
        }

        // Choose the sampled avatar
        chooseAvatar(myAvatarIndex);

        add(welcomePanel, FullWindowLayout.WELCOME);
    }

    /**
     * Creates the turn panel.
     */
    private void createPokerGamePanel() {
        pokerGamePanel = new JPanel(pokerGameLayout);
        pokerGamePanel.setBackground(MyColors.tableGreen);
        pokerGamePanel.setVisible(false);

        // Center of the poker game panel; representing the card table
        pokerGamePanel.add(centerPanel = new CenterPanel(this), PokerGameLayout.CENTER);

        // Initialize player panels and their assets
        for (int i = 0; i < 4; i++) {
            pokerGamePanel.add(playerPanels[i] = new PlayerPanel(), PokerGameLayout.ALLAVTS[i]);
            pokerGamePanel.add(assetPanels[i] = new AssetPanel(), PokerGameLayout.ALLASSETS[i]);
        }

        // Speaker switch, rule cheatsheet and hand of cards
        pokerGamePanel.add(new LegendPanel(), PokerGameLayout.LEGEND);
        pokerGamePanel.add(rulePanel = new RulePanel(), PokerGameLayout.RULE);
        pokerGamePanel.add(speaker = new Speaker(), PokerGameLayout.SPEAKER);
        pokerGamePanel.add(handPanel = new HandPanel(controller, this), PokerGameLayout.HAND);

        // Setup mouse listener for speaker icon
        speaker.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    // Click on the icon to switch muting states
                    if (speaker.switchMute()) {
                        for (final Clip clip : clipMap.values()) {
                            clip.stop();
                        }
                    }
                }
            }
        });

        // Setup mouse listener for entire panel
        pokerGamePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    // Right button is an easy alternative way to click the middle button at hand
                    handPanel.clickMidButton();
                }
            }
        });

        add(pokerGamePanel, FullWindowLayout.TURN);
    }

    /**
     * Set error message for illegal name input.
     * 
     * @param errCode Error code for the reason why input name is rejected
     */
    private void setNameInputErrMsg(final int errCode) {
        userInfoErrLabel.setText(MyText.getWelcomeErrMsg(errCode));
    }

    /**
     * Choose an avatar at welcome page.
     * 
     * @param index Index of the avatar
     */
    private void chooseAvatar(final int index) {
        myAvatarIndex = index;
        indicator.setName("on" + index);
        // Mask out all other avatars
        for (int j = 0; j < WelcomeLayout.numAvt; j++) {
            avatars[j].enableMask(j != index);
        }
    }

    /**
     * Play sound effect clip.
     * 
     * @param name Name of the sound effect
     */
    public void playClip(final String name) {
        final Clip clip = clipMap.get(name);
        if (clip != null && !speaker.getMute()) {
            clip.setMicrosecondPosition(0);
            clip.start();
        }
    }

    /**
     * Shows the welcome panel.
     */
    public void showWelcomePanel() {
        pokerGamePanel.setVisible(false);
        getContentPane().setBackground(MyColors.darkBlue);
        welcomePanel.setVisible(true);
        showChanges();
    }

    /**
     * Shows the poker game panel.
     */
    private void showPokerGamePanel() {
        welcomePanel.setVisible(false);
        getContentPane().setBackground(MyColors.tableGreen);
        rulePanel.reset();
        pokerGamePanel.setVisible(true);
        setTitle(MyText.getTitle() + " - " + myName);

        playerPanels[0].setPlayer(myAvatarIndex, myName);

        // Load other players from recorded states
        for (int i = 0; i < 4; i++) {
            if (allPlayerNames[i] != null) {
                playerPanels[getRelativeLoc(i)].setPlayer(allAvtIndices[i], allPlayerNames[i]);
                if (allReady[i])
                    centerPanel.showReady(getRelativeLoc(i));
            }
        }
        showChanges();
    }

    /**
     * Load another player when receiving broadcast from server.
     * 
     * @param seatIndex   Absolute seat index of the new player
     * @param avatarIndex Index chosen by the new player
     * @param name        Name of the new player
     */
    public void setPlayerInfo(final int seatIndex, final int avatarIndex, final String name) {
        allAvtIndices[seatIndex] = avatarIndex;
        allPlayerNames[seatIndex] = name;

        if (mySeatIndex < 0) { // Not seated yet
            seatPanels[seatIndex].setPlayer(avatarIndex, name);
        } else { // Already seated
            playerPanels[getRelativeLoc(seatIndex)].setPlayer(avatarIndex, name);
        }
        showChanges();
    }

    /**
     * Receive a card dealt by server.
     * 
     * @param alias Alias of the card
     * @return {@code true} if this is the last card; {@code false} otherwise
     */
    public boolean addCard(final String alias) {
        playClip("deal");
        return handPanel.addCard(alias);
    }

    /**
     * Sit down at chosen seat after server's permission.
     * 
     * @param seat absolute location of seat
     */
    public void sitDown(final int seat) {
        mySeatIndex = seat;
        allPlayerNames[seat] = myName;
        allAvtIndices[seat] = myAvatarIndex;
        showPokerGamePanel();
    }

    /**
     * Set a player to ready state.
     * 
     * @param seatIndex Absolute index of the ready player
     */
    public void setReady(final int seatIndex) {
        allReady[seatIndex] = true; // update state even when self is not seated
        if (mySeatIndex >= 0) {
            final int playerIndex = getRelativeLoc(seatIndex);
            centerPanel.showReady(playerIndex);
            if (playerIndex == 0)
                handPanel.disableMidButton();
        }
        showChanges();
    }

    /**
     * Set number of cards to be dealt in this round.
     * 
     * @param num number of cards
     */
    public void setNumDealingCards(final int numCards, final int numDecks) {
        handPanel.setNumDealingCards(numCards);
        for (AssetPanel assetPanel : assetPanels) {
            assetPanel.setNumDecks(numDecks);
        }
    }

    /**
     * Set flags indicating if this is first round in a frame.
     * 
     * @param reset {@code true} to reset for new frame; {@code false} to decrement
     *              the counter.
     */
    // public void setFirstRound(final boolean reset) {
    // if (reset) {
    // firstRoundStarted = false;
    // handPanel.setFirstRound(true);
    // } else if (!firstRoundStarted) {
    // firstRoundStarted = true;
    // } else {
    // handPanel.setFirstRound(false);
    // handPanel.enableSeeLastRoundButton(true);
    // }
    // }

    public void openFrame(final int absLoc, final int timeLimit) {
        final int playerIndex = getRelativeLoc(absLoc);
        roundProg = 0; // Reset round counter

        handPanel.setFirstRound(true);
        if (playerIndex == 0) {
            handPanel.setLeadCards(null); // Self is leading
        }

        // Set "Last round" button disabled (but possibly still visible)
        handPanel.showSeeLastRoundButton(true); // Enable the "Last round" button
        handPanel.enableSeeLastRoundButton(false);

        centerPanel.reset(); // Save all cards played last round

        showWaiting(timeLimit, playerIndex); // Start digital timer for player
        // If self is leading, mask out infeasible cards; otherwise, all cards are shown
        // feasible before the leading cards are played
        handPanel.setMaskMode(playerIndex == 0 ? "NORMAL" : "ALL");
    }

    /**
     * Add all asset cards of this round to a player. Initiate a new round.
     * 
     * @param absLoc    Absolute location of the recipient
     * @param timeLimit Time limit to lead a card
     * @param asset     Names of all asset cards
     */
    public void addAsset(final int absLoc, final int timeLimit, final String[] asset) {
        final int playerIndex = getRelativeLoc(absLoc);
        roundProg = 0; // Reset round counter

        if (playerIndex == 0) {
            handPanel.setLeadCards(null); // Self is leading
        }

        for (final String alias : asset)
            assetPanels[playerIndex].addAsset(alias);

        // Set "Last round" button disabled (but possibly still visible)
        handPanel.setFirstRound(false);
        handPanel.enableSeeLastRoundButton(true);
        centerPanel.allSaveHistory(); // Save all cards played last round
        if (!handPanel.isEmpty()) {
            showWaiting(timeLimit, playerIndex); // Start digital timer for player
            // If self is leading, mask out infeasible cards; otherwise, all cards are shown
            // feasible before the leading cards are played
            handPanel.setMaskMode(playerIndex == 0 ? "NORMAL" : "ALL");
        }
        showChanges();
    }

    /**
     * Play cards in current player's turn.
     * 
     * @param lead        {@code true} if the player is leading; {@code false}
     *                    otherwise
     * @param timeLimit   Time limit of the next player's turn
     * @param absLoc      Absolute location of the current player
     * @param cardAliases Aliases of cards played this round
     */
    public void playTurn(final boolean lead, final int timeLimit, final int absLoc, final String[] cardAliases) {
        final int playerIndex = getRelativeLoc(absLoc);
        if (lead) {
            handPanel.setLeadCards(cardAliases);
            // If self just played, remove masks on all cards
            handPanel.setMaskMode(playerIndex == 0 ? "ALL" : "NORMAL");
        }

        centerPanel.allHideHistory(); // Enforce player to quit looking at last round
        centerPanel.endTiming(playerIndex); // End digital timer of current player
        centerPanel.showCards(playerIndex, cardAliases);

        // Start timing the next player if this is not the last turn in a round
        if (++roundProg < 4)
            showWaiting(timeLimit, getRelativeLoc(absLoc + 1));

        playClip("play");
        showChanges();
    }

    /**
     * Start timer for the player in turn.
     * 
     * @param timeLimit   Time limit to play cards
     * @param playerIndex Relative location of player
     */
    private void showWaiting(final int timeLimit, final int playerIndex) {
        if (playerIndex == 0 && !handPanel.isEmpty()) {
            handPanel.enablePlayButton();
            // UI helpers for playing to the rule
            if (!handPanel.autoPlayLastCard())
                if (!handPanel.autoFollowLastRound())
                    handPanel.autoChooseOnlyOption();
        }
        // Start digital timer
        centerPanel.showWaiting(timeLimit, playerIndex);
        showChanges();
    }

    /**
     * Enter card showing phase
     * 
     * @param timeLimit Time limit of card showing
     */
    public void enterShowingPhase(final int timeLimit) {
        numShown = 0;
        handPanel.setMaskMode("EXPOSABLE");
        handPanel.enableShowButton();
        centerPanel.setCornerTimer(timeLimit);
        centerPanel.setErrMsg(MyText.HINT_SHOWING, handPanel.checkExposables());
        showChanges();
    }

    /**
     * Display cards shown by given player.
     * 
     * @param absLoc  Absolute position of the player
     * @param aliases Aliases of cards shown, empty when showing is passed
     */
    public void displayExposedCards(final int absLoc, final String[] aliases) {
        final int playerIndex = getRelativeLoc(absLoc);

        if (aliases.length > 0) {
            centerPanel.showCards(playerIndex, aliases);
            playClip("drop");
        } else {
            centerPanel.showPass(playerIndex);
        }
        assetPanels[playerIndex].setExposed(aliases);
        rulePanel.updateEffects(aliases);
        handPanel.applyExposure(aliases);
        centerPanel.allApplyExposure(aliases);
        if (playerIndex == 0) {
            handPanel.putAllCards();
            handPanel.disableMidButton();
            handPanel.enableMouseControl(true);
            centerPanel.setErrMsg(MyText.NORMAL);
        }

        if (++numShown == 4) {
            centerPanel.endCornerTiming(); // Reset the whole table when phase ends
        }
        showChanges();
    }

    /**
     * Enter trading phase.
     * 
     * @param timeLimit Time limit of card trading
     * @param tradeGap  Distance to the recipient in counter-clockwise direction
     */
    public void enterTradingPhase(final int timeLimit, final int tradeGap) {
        handPanel.setMaskMode("TRADE");
        handPanel.enableTradeButton();
        centerPanel.setCornerTimer(timeLimit);
        centerPanel.setTradeGap(tradeGap);
        centerPanel.showAllArrows();
        centerPanel.enablePassingHints(true);
        showChanges();
    }

    /**
     * Set indicator when a player is done trading.
     * 
     * @param absLoc Absolute direction of the player
     */
    public void setTradeReady(final int absLoc) {
        final int loc = getRelativeLoc(absLoc);
        playClip("deal");
        centerPanel.flipArrow(loc);
        if (loc == 0) {
            centerPanel.enablePassingHints(false);
            handPanel.disableMidButton();
        }
        showChanges();
    }

    /**
     * Receive the cards traded in from another player.
     * 
     * @param cardAliases Aliases of cards traded in
     */
    public void tradeInCards(final String[] cardAliases) {
        centerPanel.reset();
        handPanel.tradeIn(cardAliases);
        showChanges();
    }

    /**
     * Set total scores of all players and open the scoreboard.
     * 
     * @param scores total scores
     */
    public void setTotalScore(final String[] scores) {
        for (int i = 0; i < 4; i++) {
            playerPanels[getRelativeLoc(i)].setTotalScore(Integer.parseInt(scores[i]));
        }
        handPanel.showSeeLastRoundButton(false);
        centerPanel.allHideHistory();
        centerPanel.setScoreBoard();
        showChanges();
    }

    /**
     * Reset the game panel for new frame.
     */
    public void resetForNewFrame() {
        for (int i = 0; i < 4; i++) {
            allReady[i] = false;
            assetPanels[i].reset();
        }
        rulePanel.reset();
        handPanel.reset();
        centerPanel.reset();
        showChanges();
    }

    /**
     * React to disconnection of a player.
     * 
     * @param absLoc          Absolute location of disconnected player
     * @param waitingForReady {@code true} if a frame is not yet started;
     *                        {@code false} if a game is going on
     */
    public void resetForDisconnection(final int absLoc, final boolean waitingForReady) {
        allReady[absLoc] = false;
        if (mySeatIndex < 0) {
            seatPanels[absLoc].reset();
        } else {
            final int loc = getRelativeLoc(absLoc);
            playerPanels[loc].clear();
            for (PlayerPanel playerPanel : playerPanels)
                playerPanel.setTotalScore(0);

            centerPanel.clearSection(loc);
            if (!waitingForReady) {
                resetForNewFrame();
            } else if (!allReady[mySeatIndex]) {
                handPanel.enableReadyButton();
            }

            centerPanel.setConnErrMsg(allPlayerNames[absLoc]);
            showChanges();
        }
    }

    /**
     * Force to perform a reaction when time runs out.
     */
    public void performDefaultReaction() {
        handPanel.performDefaultReaction();
    }

    /**
     * Get all player names.
     * 
     * @return List of player names starting with self
     */
    public String[] getNames() {
        final String[] names = new String[4];
        for (int i = 0; i < 4; i++) {
            names[i] = playerPanels[i].getName();
        }
        return names;
    }

    /**
     * Get all player scores.
     * 
     * @return List of player scores and total scores starting with selfs
     */
    public int[] getScores() {
        final int[] scores = new int[8];
        for (int i = 0; i < 4; i++) {
            scores[i] = assetPanels[i].getScore();
            scores[i + 4] = playerPanels[i].getTotalScore();
        }
        return scores;
    }

    /**
     * Get relative location given the absolute location.
     * 
     * @param absLoc Absolute location
     * @return Relative location
     */
    public int getRelativeLoc(final int absLoc) {
        return mySeatIndex <= -1024 ? absLoc : Math.floorMod(absLoc - mySeatIndex, 4);
    }

    /**
     * @return {@code true} if is last round; {@code false} otherwise
     */
    public boolean isLastRound() {
        return handPanel.isEmpty();
    }

    /**
     * Show error message when failed to sit down.
     */
    public void showSeatErrMsg() {
        seatErrLabel.setText(MyText.getSeatingHint());
        showChanges();
    }

    /**
     * Show error message or hints when playing.
     * 
     * @param errCode Error code of the message
     */
    public void setPlayErrMsg(final int errCode, final int... flags) {
        centerPanel.setErrMsg(errCode, flags);
    }

    /**
     * Enable the ready button.
     */
    public void enableReadyButton() {
        handPanel.enableReadyButton();
    }

    /**
     * Enable or disable mouse listener on cards in hand.
     * 
     * @param b {@code true} to enable; {@code false} to disable
     */
    public void enableHandControl(final boolean b) {
        handPanel.enableMouseControl(b);
    }

    /**
     * Show all cards played during last round.
     */
    public void showAllHistory() {
        centerPanel.allShowHistory();
    }

    public void setClockEnforcer(boolean b) {
        centerPanel.setCurrentClockEnforcer(b);
    }

    public void acquireActionLock() {
        actionLock.lock();
    }

    public void releaseActionLock() {
        actionLock.unlock();
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e Event generated by component action
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        final Object target = e.getSource();

        // React to clicking sit buttons. Check if name is legal and send request to
        // server
        if (((Component) target).getName().substring(0, 10).equals("sitButton_")) {
            String inputName;
            final String seatIndexString = ((Component) target).getName().substring(10, 11);
            inputName = nameField.getText();

            if (inputName.isEmpty() || inputName.isBlank()) {
                setNameInputErrMsg(MyText.NAME_BLANK);
                return;
            } else if (inputName.contains(ClientController.RECV_DELIM)
                    || inputName.contains(ClientController.SEND_DELIM)) {
                setNameInputErrMsg(MyText.NAME_HAS_DILIMITER);
                return;
            } else {
                try {
                    if (inputName.getBytes("Unicode").length > 24) {
                        setNameInputErrMsg(MyText.NAME_TOO_LONG);
                        return;
                    }
                } catch (final UnsupportedEncodingException err) {
                    setNameInputErrMsg(MyText.NAME_ENCODING);
                    return;
                }
            }
            myName = new String(inputName);
            setNameInputErrMsg(MyText.NORMAL);
            controller.sendToServer("SITDOWN", seatIndexString, String.valueOf(myAvatarIndex), myName);
        }
    }
}