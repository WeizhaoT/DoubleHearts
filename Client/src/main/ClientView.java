package main;

import ui.*;
import panel.*;
import layout.*;
import element.*;

import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;

/**
 * ClientView objects create the GUI for a Blackjack player.
 *
 * @author Jordan Segalman
 */

public class ClientView extends JFrame implements ActionListener {
    static final long serialVersionUID = 1L;

    private final ClientController controller; // client GUI controller

    public int mySeatIndex = -1024;
    public String myName;
    public int myAvatarIndex;

    private int roundProg;

    private boolean firstRoundStarted;

    private final int[] allAvtIndices = new int[] { -1, -1, -1, -1 };
    private final boolean[] allReady = new boolean[] { false, false, false, false };
    private final String[] allPlayerNames = new String[] { null, null, null, null };

    private WelcomeLayout welcomeLayout;
    private PokerGameLayout pokerGameLayout;

    private JPanel welcomePanel;
    private JPanel turnPanel;

    private final SeatPanel[] seatPanels = new SeatPanel[4];

    private JLabel seatErrLabel;
    private JLabel userInfoErrLabel;
    private JTextField nameField;

    private final MaskedAvatar[] avatars = new MaskedAvatar[WelcomeLayout.numAvt];
    private JLabel indicator;

    private MouseAdapter indicatorAdapter;

    // turn panel components

    private final PlayerPanel[] playerPanels = new PlayerPanel[4];
    private final AssetPanel[] assetPanels = new AssetPanel[4];
    private HandPanel handPanel;
    private CenterPanel centerPanel;

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
        setTitle("Pig");

        // setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        // setLayout(new CardLayout());
        // showChanges();
    }

    /**
     * Creates all of the panels.
     */

    private void createPanels() {
        welcomeLayout = new WelcomeLayout(20);
        pokerGameLayout = new PokerGameLayout(20, 20);

        createWelcomePanel();
        createTurnPanel();
        setLayout(new FullWindowLayout(welcomeLayout, pokerGameLayout));
        pack();
        setResizable(true);
        setVisible(true);
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

        welcomePanel.add(ImageLabel.getTable(), WelcomeLayout.TABLE);

        for (int i = 0; i < 4; i++) {
            seatPanels[i] = new SeatPanel(i, this);
            welcomePanel.add(seatPanels[i], WelcomeLayout.ALLSEATS[i]);
        }

        seatErrLabel = new JLabel("", SwingConstants.CENTER);
        seatErrLabel.setForeground(MyColors.errmsg);
        seatErrLabel.setFont(seatErrLabel.getFont().deriveFont(MyFont.Size.errMsg));
        welcomePanel.add(seatErrLabel, WelcomeLayout.TABLEERR);

        final JLabel nameLabel = new JLabel("Please Input Your Name: ", SwingConstants.CENTER);
        nameLabel.setForeground(MyColors.text);
        nameLabel.setFont(nameLabel.getFont().deriveFont(MyFont.Size.userInfo));
        welcomePanel.add(nameLabel, WelcomeLayout.NAMELABEL);

        nameField = new JTextField("", SwingConstants.CENTER);
        nameField.setFont(nameField.getFont().deriveFont(MyFont.Size.userInfo));
        welcomePanel.add(nameField, WelcomeLayout.NAME);

        userInfoErrLabel = new JLabel("", SwingConstants.CENTER);
        userInfoErrLabel.setForeground(MyColors.errmsg);
        userInfoErrLabel.setFont(userInfoErrLabel.getFont().deriveFont(MyFont.Size.errMsg));
        welcomePanel.add(userInfoErrLabel, WelcomeLayout.NAMEERR);

        final JLabel avatarLabel = new JLabel("<html><center>Please Choose Your Avatar: </center></html>",
                SwingConstants.CENTER);
        avatarLabel.setForeground(MyColors.text);
        avatarLabel.setFont(avatarLabel.getFont().deriveFont(MyFont.Size.userInfo));
        welcomePanel.add(avatarLabel, WelcomeLayout.AVTLABEL);

        myAvatarIndex = (new Random()).nextInt(WelcomeLayout.numAvt);
        indicator = ImageLabel.getIndicator();
        indicator.setName("on" + myAvatarIndex);
        indicator.setOpaque(false);
        welcomePanel.add(indicator, WelcomeLayout.INDICATOR);

        indicatorAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) {
                    return;
                }

                synchronized (this) {
                    final MaskedAvatar avatar = (MaskedAvatar) e.getComponent();
                    final int i = Integer.parseInt(avatar.getName());
                    chooseAvatar(i);
                }
            }
        };

        for (int i = 0; i < WelcomeLayout.numAvt; i++) {
            avatars[i] = new MaskedAvatar(i, WelcomeLayout.minAvatarScale);
            avatars[i].addMouseListener(indicatorAdapter);

            welcomePanel.add(avatars[i], WelcomeLayout.AVATAR + i);
        }

        chooseAvatar(myAvatarIndex);

        add(welcomePanel, BorderLayout.NORTH);
    }

    /**
     * Creates the turn panel.
     */

    private void createTurnPanel() {
        turnPanel = new JPanel(pokerGameLayout);
        turnPanel.setBackground(MyColors.tableGreen);
        turnPanel.setVisible(false);

        centerPanel = new CenterPanel();
        centerPanel.setOpaque(false);
        turnPanel.add(centerPanel, PokerGameLayout.CENTER);

        for (int i = 0; i < 4; i++) {
            playerPanels[i] = new PlayerPanel();
            turnPanel.add(playerPanels[i], PokerGameLayout.ALLAVTS[i]);

            assetPanels[i] = new AssetPanel();
            turnPanel.add(assetPanels[i], PokerGameLayout.ALLASSETS[i]);
        }

        handPanel = new HandPanel(controller, this);
        turnPanel.add(handPanel, PokerGameLayout.HAND);

        final MouseAdapter adaptor = new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    handPanel.clickMidButton();
                }
            }
        };

        turnPanel.addMouseListener(adaptor);
        add(turnPanel, BorderLayout.SOUTH);
        // add(turnPanel, PanelNames.TURNPANEL.toString());
    }

    private void chooseAvatar(int index) {
        indicator.setName("on" + index);
        myAvatarIndex = index;
        for (int j = 0; j < WelcomeLayout.numAvt; j++) {
            if (j != index)
                avatars[j].maskOn();
        }

        avatars[index].maskOff();
        showChanges();
    }

    /**
     * Shows the welcome panel.
     */

    public void showWelcomePanel() {
        turnPanel.setVisible(false);
        getContentPane().setBackground(MyColors.darkBlue);
        welcomePanel.setVisible(true);
        showChanges();
    }

    /**
     * Shows the turn panel.
     */

    public void showTurnPanel() {
        welcomePanel.setVisible(false);
        getContentPane().setBackground(MyColors.tableGreen);
        turnPanel.setVisible(true);
        playerPanels[0].setPlayer(myAvatarIndex, myName);
        setTitle("Double Hearts - " + myName);

        for (int i = 0; i < 4; i++) {
            if (allPlayerNames[i] != null) {
                playerPanels[getRelativeLoc(i)].setPlayer(allAvtIndices[i], allPlayerNames[i]);
                if (allReady[i])
                    centerPanel.showReady(getRelativeLoc(i));
            }
        }
        showChanges();
    }

    public void setPlayerInfo(final int seatIndex, final int avatarIndex, final String name) {
        allAvtIndices[seatIndex] = avatarIndex;
        allPlayerNames[seatIndex] = name;

        if (mySeatIndex < 0) {
            seatPanels[seatIndex].setPlayer(avatarIndex, name);
        } else {
            playerPanels[getRelativeLoc(seatIndex)].setPlayer(avatarIndex, name);
        }
        showChanges();
    }

    public boolean addCard(final String alias) {
        return handPanel.addCard(alias);
    }

    public void sitDown(final int seat) {
        mySeatIndex = seat;
        allPlayerNames[seat] = myName;
        allAvtIndices[seat] = myAvatarIndex;
        showTurnPanel();
    }

    public void setSeatErrMsg(final String errmsg) {
        seatErrLabel.setText(errmsg);
        showChanges();
    }

    public void setPlayErrMsg(final String errmsg) {
        centerPanel.setErrMsg(errmsg);

        for (final PlayerPanel playerPanel : playerPanels)
            playerPanel.showChanges();

        showChanges();
    }

    public void setMaskMode(final String mode) {
        handPanel.setMaskMode(mode);
        showChanges();
    }

    public void updateFeasibleCard() {
        handPanel.updateFeasibleCard();
        showChanges();
    }

    public void enableMouseControl(final boolean b) {
        handPanel.enableMouseControl(b);
    }

    public void enableReadyButton() {
        handPanel.enableReadyButton();
        showChanges();
    }

    public void enableHandControl(final boolean b) {
        handPanel.enableMouseControl(b);
    }

    public void setFirstRound(final boolean reset) {
        if (reset) {
            firstRoundStarted = false;
            handPanel.setFirstRound(true);
        } else if (!firstRoundStarted) {
            firstRoundStarted = true;
        } else {
            handPanel.setFirstRound(false);
            handPanel.enableSeeLastRoundButton(true);
        }
    }

    public void setReady(final int seatIndex) {
        allReady[seatIndex] = true;
        if (mySeatIndex >= 0) {
            final int playerIndex = getRelativeLoc(seatIndex);
            centerPanel.showReady(playerIndex);
            if (playerIndex == 0)
                handPanel.disableMidButton();
        }
        showChanges();
    }

    public void addAsset(final int absLoc, final String[] asset) {
        final int playerIndex = getRelativeLoc(absLoc);

        if (playerIndex == 0) {
            handPanel.setLeadCards(null);
        }

        for (final String alias : asset)
            assetPanels[playerIndex].addAsset(alias);

        showChanges();
    }

    public void showCards(final int absLoc, final String[] aliases) {
        final int playerIndex = getRelativeLoc(absLoc);
        centerPanel.showCards(playerIndex, aliases);
        showChanges();
    }

    public void showExhibitedCards(final int absLoc, final String[] aliases) {
        final int playerIndex = getRelativeLoc(absLoc);

        if (aliases.length > 0) {
            centerPanel.showCards(playerIndex, aliases);
        } else {
            centerPanel.showPass(playerIndex);
        }
        assetPanels[playerIndex].setExhibition(aliases);
        if (playerIndex == 0) {
            handPanel.disableMidButton();
            handPanel.enableMouseControl(true);
            handPanel.setExhibitedCards();
            centerPanel.setErrMsg("");
        }
        showChanges();
    }

    public void clearLastRound() {
        handPanel.enableSeeLastRoundButton(false);
        centerPanel.allSaveHistory();
        if (!handPanel.isEmpty())
            handPanel.showSeeLastRoundButton(true);

        showChanges();
    }

    public void showAllHistory() {
        centerPanel.allShowHistory();
        showChanges();
    }

    public void hideAllHistory() {
        centerPanel.allHideHistory();
        showChanges();
    }

    public void showNextWaiting(final int absLoc) {
        final int playerIndex = getRelativeLoc(absLoc + 1);
        if (playerIndex == 0) {
            if (!handPanel.isEmpty()) {
                handPanel.enablePlayButton();
                if (!handPanel.autoPlayLastCard())
                    if (!handPanel.autoFollowLastRound())
                        handPanel.autoChooseOnlyOption();
            }
        } else
            centerPanel.showWaitingIfIdle(playerIndex);

        showChanges();
    }

    public void setShowCardWaiting() {
        handPanel.enableShowButton();
        centerPanel.allShowWaiting();
        centerPanel.setErrMsg("You can show " + MyColors.getColoredText("\u2663 T", MyColors.clubColor) + " (x2), "
                + MyColors.getColoredText("\u2666 J", MyColors.diamondColor) + " (+100), or "
                + MyColors.getColoredText("\u2660 Q", MyColors.spadeColor)
                + " (-100). Shown cards will enjoy double effect.");
        showChanges();
    }

    public void setTradeWaiting(final int frame) {
        handPanel.enableTradeButton();
        centerPanel.setFrameIndex(frame);
        centerPanel.showAllArrows();
        centerPanel.enablePassingHints(true);
        showChanges();
    }

    public void setTradeReady(final int absLoc) {
        final int loc = getRelativeLoc(absLoc);
        centerPanel.flipArrow(loc);
        if (loc == 0) {
            centerPanel.enablePassingHints(false);
            handPanel.disableMidButton();
        }

        showChanges();
    }

    public void tradeInCards(final String[] cardAliases) {
        centerPanel.reset();
        handPanel.tradeIn(cardAliases);
        showChanges();
    }

    public void setLeadCards(final String[] aliases) {
        roundProg = 0;
        handPanel.setLeadCards(aliases);
    }

    public void setTotalScore(final String[] scores) {
        for (int i = 0; i < 4; i++) {
            playerPanels[getRelativeLoc(i)].setTotalScore(Integer.parseInt(scores[i]));
        }
        handPanel.showSeeLastRoundButton(false);
        hideAllHistory();
        centerPanel.setScoreBoard(this);
        showChanges();
    }

    public void resetForNewFrame() {
        for (int i = 0; i < 4; i++) {
            allReady[i] = false;
        }

        for (final AssetPanel assetPanel : assetPanels) {
            assetPanel.reset();
        }

        handPanel.reset();
        centerPanel.reset();
        showChanges();
    }

    public void resetForPeer(final int absLoc, final boolean waitingForReady) {
        allReady[absLoc] = false;
        if (mySeatIndex < 0) {
            seatPanels[absLoc].reset();
        } else {
            final int loc = getRelativeLoc(absLoc);
            playerPanels[loc].clear();
            centerPanel.clearSection(loc);
            if (!waitingForReady) {
                resetForNewFrame();
            }

            showChanges();
            centerPanel.setErrMsg("Player \"" + allPlayerNames[absLoc] + "\" has left the table");
        }
    }

    public String[] getNames() {
        final String[] names = new String[4];
        for (int i = 0; i < 4; i++) {
            names[i] = playerPanels[i].getName();
        }
        return names;
    }

    public int[] getScores() {
        final int[] scores = new int[8];
        for (int i = 0; i < 4; i++) {
            scores[i] = assetPanels[i].getScore();
            scores[i + 4] = playerPanels[i].getTotalScore();
        }
        return scores;
    }

    public int getRelativeLoc(final int absLoc) {
        return mySeatIndex <= -1024 ? absLoc : Math.floorMod(absLoc - mySeatIndex, 4);
    }

    public boolean isLastRound() {
        return handPanel.isEmpty();
    }

    public void resetRoundProg() {
        roundProg = 0;
    }

    public void incrRoundProg() {
        roundProg++;
    }

    public boolean isRoundEnd() {
        return roundProg >= 4;
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e Event generated by component action
     */

    @Override
    public void actionPerformed(final ActionEvent e) {
        final Object target = e.getSource();

        if (((Component) target).getName().substring(0, 10).equals("sitButton_")) {
            String inputName;
            final String seatIndexString = ((Component) target).getName().substring(10, 11);
            // try {
            // inputName = new String(nameField.getText().getBytes("UTF-8"));
            // } catch (UnsupportedEncodingException err) {
            // System.err.println("Warning: name cannot be encoded");
            // inputName = nameField.getText();
            // }
            inputName = nameField.getText();

            if (inputName.isEmpty() || inputName.isBlank()) {
                userInfoErrLabel.setText("Name should NOT be empty or blank!");
            } else if (inputName.length() > 32) {
                userInfoErrLabel.setText("Name should be at most 32-char long!");
            } else if (inputName.contains(ClientController.RECV_DELIM)
                    || inputName.contains(ClientController.SEND_DELIM)) {
                userInfoErrLabel.setText("Name should NOT contain delimiters \"" + ClientController.RECV_DELIM
                        + "\" or \"" + ClientController.SEND_DELIM + "\" !");
            } else {
                myName = new String(inputName);
                controller.sendToServer("SITDOWN", seatIndexString, String.valueOf(myAvatarIndex), myName);
            }
        }
    }
}