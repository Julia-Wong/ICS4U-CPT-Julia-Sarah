import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
// import java.util.HashMap;
// import javax.swing.event.*;

public class GameModel implements ActionListener {
    // Properties
    JFrame theFrame = new JFrame("Spleef");
    GameView thePanel = new GameView();

    // Home Screen
    JLabel titleSpleef = new JLabel("Spleef");
    JButton lobbyButton = new JButton("Lobby");
    JButton helpButton = new JButton("Help");
    JButton creditsButton = new JButton("Credits");

    // Back Buttons
    JButton backButton = new JButton("<- Back");

    // Lobby Buttons
    JLabel playersConnectedLabel = new JLabel("4 Players Connected"); // PLACEHOLDER
    JLabel chooseMapLabel = new JLabel("Choose a Map: ");
    JButton alpineTundraMapButton = new JButton("Alpine Tundra Map"); // snow stone dirt
    JButton oasisDesertMapButton = new JButton("Oasis Desert Map"); // sand stone dirt
    JButton floatingIslandMapButton = new JButton("Floating Island Map"); // grass dirt sand
    JButton playButton = new JButton("PLAY");

    JTextArea chatArea = new JTextArea();
    JScrollPane scrollChatArea = new JScrollPane(chatArea);
    JTextField chatInput = new JTextField();
    JButton connectChat = new JButton("Connect");

    // Connecting to Host
    SuperSocketMaster ssm;
    boolean isServer = false;
    JLabel chooseRoleLabel = new JLabel("Choose your network role: ");
    JButton chooseHostButton = new JButton("Host Game");
    JButton chooseJoinButton = new JButton("Join Game");
    int chooseRole = -1;
    JButton confirmRoleButton = new JButton("Confirm");

    // Data
    
    String[] mapFiles = {"alpineTundraMap.csv", "oasisDesertMap.csv", "floatingIslandMap.csv"};
    int intMapChoice = 0;
    int intPlayersConnected = 0;

    // Methods
    public void actionPerformed(ActionEvent evt) {
        // if on homescreen
        if (thePanel.intGameState == 0) {
            if (evt.getSource() == lobbyButton) {
                thePanel.intGameState = 1;
            } else if (evt.getSource() == helpButton) {
                thePanel.intGameState = 2;
            } else if (evt.getSource() == creditsButton) {
                thePanel.intGameState = 3;
            }
        }

        // if on lobby screen
        if (thePanel.intGameState == 1) {
            if (evt.getSource() == backButton) {
                thePanel.intGameState = 0;
            } else if (evt.getSource() == alpineTundraMapButton) {
                intMapChoice = 0;
                loadMap(mapFiles[intMapChoice]);
            } else if (evt.getSource() == oasisDesertMapButton) {
                intMapChoice = 1;
                loadMap(mapFiles[intMapChoice]);
            } else if (evt.getSource() == floatingIslandMapButton) {
                intMapChoice = 2;
                loadMap(mapFiles[intMapChoice]);
            } else if (evt.getSource() == playButton) {
                thePanel.intGameState = 4;
            } else if (evt.getSource() == connectChat) {
                thePanel.choosingNetworkRole = true;

                if (chooseRole == 0) {
                    // HOST MODE: Opens port 1337 and listens for players
                    isServer = true;
                    ssm = new SuperSocketMaster(1337, this);
                    ssm.connect();
                    chatArea.append("[SYSTEM] Server started! Waiting for players...\n");
                    thePanel.choosingNetworkRole = false;
                } else if (chooseRole == 1) {
                    // JOIN MODE: Connects to the host (localhost for testing)
                    isServer = false;
                    ssm = new SuperSocketMaster("127.0.0.1", 1337, this);
                    ssm.connect();
                    chatArea.append("[SYSTEM] Connecting to server...\n");
                    thePanel.choosingNetworkRole = false;
                }
            } else if (evt.getSource() == chooseHostButton) {
                chooseRole = 0;
            } else if (evt.getSource() == chooseJoinButton) {
                chooseRole = 1;
            } else if (evt.getSource() == confirmRoleButton) {
                thePanel.choosingNetworkRole = false;
            }

            // Bold Map if Chosen
            if (intMapChoice == 0) { // Bold Apline Tundra
                alpineTundraMapButton.setText("★ ALPINE TUNDRA ★");
            } else {
                alpineTundraMapButton.setText("Alpine Tundra Map");
            }

            if (intMapChoice == 1) { // Bold Oasis Desert
                oasisDesertMapButton.setText("★ OASIS DESERT ★");
            } else {
                oasisDesertMapButton.setText("Oasis Desert Map");
            }

            if (intMapChoice == 2) { // Bold Floating Island
                floatingIslandMapButton.setText("★ FLOATING ISLAND ★");
            } else {
                floatingIslandMapButton.setText("Floating Island Map");
            }

        }

        // if on help screen
        if (thePanel.intGameState == 2) {
            if (evt.getSource() == backButton) {
                thePanel.intGameState = 0;
            }
        }

        // if on credits screen
        if (thePanel.intGameState == 3) {
            if (evt.getSource() == backButton) {
                thePanel.intGameState = 0;
            }
        }

        showCurrentGUI();
        
    }

    public void loadMap(String fileName) {
        BufferedReader mapFile = null;
        try {
            mapFile = new BufferedReader(new FileReader(fileName));
            String mapLine;
            String[] mapSplit;

            for (int r = 0; r < thePanel.map.length; r++) {
                mapLine = mapFile.readLine();
                mapSplit = mapLine.split(",");
                for (int c = 0; c < thePanel.map[r].length; c++) {
                    thePanel.map[r][c] = Integer.parseInt(mapSplit[c]);
                }
            }
            mapFile.close();

        } catch (FileNotFoundException e) {
            System.out.println("Error: Could not find the map file: " + fileName);
        } catch (IOException e) {
            System.out.println("Error: Could not read the map file: " + fileName);
        }
    }

    // Constructor
    public GameModel(){
        // Set Panel
        thePanel.setLayout(null);
        thePanel.setPreferredSize(new Dimension(1280, 720));

        // Add Title
        titleSpleef.setBounds(500, 100, 400, 100);
        thePanel.add(titleSpleef);

        // Add Homescreen Buttons
        lobbyButton.setBounds(400, 300, 400, 100);
        lobbyButton.addActionListener(this);
        thePanel.add(lobbyButton);

        helpButton.setBounds(400, 400, 400, 100);
        helpButton.addActionListener(this);
        thePanel.add(helpButton);

        creditsButton.setBounds(400, 500, 400, 100);
        creditsButton.addActionListener(this);
        thePanel.add(creditsButton);

        // Add Back Button
        backButton.setBounds(10, 10, 80, 40);
        backButton.addActionListener(this);
        thePanel.add(backButton);

        // Add Lobby Map Options
        alpineTundraMapButton.setBounds(100, 500, 300, 100);
        alpineTundraMapButton.addActionListener(this);
        thePanel.add(alpineTundraMapButton);

        oasisDesertMapButton.setBounds(400, 500, 300, 100);
        oasisDesertMapButton.addActionListener(this);
        thePanel.add(oasisDesertMapButton);

        floatingIslandMapButton.setBounds(700, 500, 300, 100);
        floatingIslandMapButton.addActionListener(this);
        thePanel.add(floatingIslandMapButton);

        // Add Lobby Labels
        playersConnectedLabel.setBounds(500, 200, 400, 100);
        thePanel.add(playersConnectedLabel);

        chooseMapLabel.setBounds(500, 400, 400, 100);
        thePanel.add(chooseMapLabel);

        // Add Play Button
        playButton.setBounds(500, 600, 400, 100);
        playButton.addActionListener(this);
        thePanel.add(playButton);

        // Add Chat
        scrollChatArea.setBounds(950, 0, 330, 150);
        thePanel.add(scrollChatArea);

        chatInput.setBounds(950, 150, 330, 50);
        chatInput.addActionListener(this);
        thePanel.add(chatInput);

        connectChat.setBounds(950, 200, 330, 50);
        connectChat.addActionListener(this);
        thePanel.add(connectChat);

        // Add Connect Pop-up Panel
        chooseRoleLabel.setBounds(420, 220, 400, 50);
        thePanel.add(chooseRoleLabel);

        chooseHostButton.setBounds(420, 270, 100, 50);
        chooseHostButton.addActionListener(this);
        thePanel.add(chooseHostButton);

        chooseJoinButton.setBounds(620, 270, 100, 50);
        chooseJoinButton.addActionListener(this);
        thePanel.add(chooseJoinButton);

        confirmRoleButton.setBounds(800, 350, 100, 50);
        confirmRoleButton.addActionListener(this);
        thePanel.add(confirmRoleButton);

        showCurrentGUI();

        // Set Frame
        theFrame.setContentPane(thePanel);
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        theFrame.pack();
        theFrame.setVisible(true);
    }

    public void showCurrentGUI() {
        boolean isHome = thePanel.intGameState == 0;
        boolean isLobby = thePanel.intGameState == 1;
        boolean isHelp = thePanel.intGameState == 2;
        boolean isCredits = thePanel.intGameState == 3;

        // if on home screen:
        lobbyButton.setVisible(isHome);
        helpButton.setVisible(isHome);
        creditsButton.setVisible(isHome);

        // if on lobby screen:
        playersConnectedLabel.setVisible(isLobby);
        chooseMapLabel.setVisible(isLobby);
        alpineTundraMapButton.setVisible(isLobby);
        oasisDesertMapButton.setVisible(isLobby);
        floatingIslandMapButton.setVisible(isLobby);
        playButton.setVisible(isLobby);
        scrollChatArea.setVisible(isLobby);
        chatInput.setVisible(isLobby);
        connectChat.setVisible(isLobby);

        chooseRoleLabel.setVisible(isLobby && thePanel.choosingNetworkRole);
        chooseHostButton.setVisible(isLobby && thePanel.choosingNetworkRole);
        chooseJoinButton.setVisible(isLobby && thePanel.choosingNetworkRole);
        confirmRoleButton.setVisible(isLobby & thePanel.choosingNetworkRole);

        // if on help screen:

        // if on credits screen:

        // draw shared J Components
        backButton.setVisible(isLobby || isHelp || isCredits);
        titleSpleef.setVisible(isHome || isLobby);
    }

    // Main Program
    public static void main(String[] args) {
        new GameModel();
    }

}