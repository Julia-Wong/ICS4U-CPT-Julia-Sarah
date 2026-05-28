import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
// import javax.swing.event.*;

public class GameModel implements ActionListener {
    // Properties
    JFrame theFrame = new JFrame("Spleef");
    GameView thePanel = new GameView();
    Timer theTimer = new Timer(1000/60, this);

    // Home Screen
    JLabel titleSpleef = new JLabel("Spleef");
    JButton lobbyButton = new JButton("Lobby");
    JButton helpButton = new JButton("Help");
    JButton creditsButton = new JButton("Credits");

    // Back Buttons
    JButton backButton = new JButton("← Back");

    // Lobby Buttons
    JLabel playersConnectedLabel = new JLabel();
    JLabel chooseMapLabel = new JLabel("Choose a Map: ");
    JButton alpineTundraMapButton = new JButton("Alpine Tundra Map"); // snow stone 
    JButton oasisDesertMapButton = new JButton("Oasis Desert Map"); // sand dirt
    JButton floatingIslandMapButton = new JButton("Floating Island Map"); // grass dirt sand
    JButton playButton = new JButton("PLAY");

    JTextArea chatArea = new JTextArea();
    JScrollPane scrollChatArea = new JScrollPane(chatArea);
    JTextField chatInput = new JTextField();

    // Connecting to Host
    SuperSocketMaster ssm;
    boolean isServer = false;
    int chooseRole = -1; // 0=host, 1=player
    int intGameSpeed = 5;
    int intGameDifficulty = 2; // 1=easy, 2=medium, 3=hard
    String strPlayerColour;
    String strIPAddress;

    // Connecting Pop-up
    JLabel chooseRoleLabel = new JLabel("Choose your network role: ");
    JButton chooseHostButton = new JButton("Host Game");
    JButton chooseJoinButton = new JButton("Join Game");
    JButton confirmRoleButton = new JButton("Confirm");

    // Host-Specific Difficulty Selectors
    JLabel chooseDifficultyLabel = new JLabel("Choose game difficulty: ");
    JButton easyButton = new JButton("Easy");
    JButton mediumButton = new JButton("Medium");
    JButton hardButton = new JButton("Hard");

    // Joiner-Specific Color Selectors
    JLabel enterIPAddressLabel = new JLabel("Enter Host's IP Address: ");
    JTextField enterIPAddress = new JTextField();
    JLabel joiningIPInfo = new JLabel();
    JLabel choosePlayerColourLabel = new JLabel("Choose player colour: ");
    JButton redButton = new JButton("Red");
    JButton blueButton = new JButton("Blue");
    JButton greenButton = new JButton("Green");
    JButton purpleButton = new JButton("Purple");

    // Data
    String[] mapFiles = {"alpineTundraMap.csv", "oasisDesertMap.csv", "floatingIslandMap.csv"};
    int intMapChoice = -1;
    int intPlayersConnected = 0;
    ArrayList<Player> playerList = new ArrayList<Player>();

    // Methods
    public void actionPerformed(ActionEvent evt) {
        // if on homescreen
        if (thePanel.intGameState == 0) {
            if (evt.getSource() == lobbyButton) {
                thePanel.intGameState = 1;
                thePanel.choosingNetworkRole = true;
                chooseRole = -1;
            } else if (evt.getSource() == helpButton) {
                thePanel.intGameState = 2;
            } else if (evt.getSource() == creditsButton) {
                thePanel.intGameState = 3;
            }
        }

        // if on lobby screen
        if (thePanel.intGameState == 1) {
            if (evt.getSource() == backButton) {
                if (thePanel.intGameState == 1 && !thePanel.choosingNetworkRole && chooseRole != -1) {
                    intPlayersConnected -= 1;
                }
                thePanel.intGameState = 0;
            } else if (evt.getSource() == alpineTundraMapButton) {
                intMapChoice = 0;
                loadMap(mapFiles[intMapChoice]);
                chatArea.append("[LOBBY] Map chosen: ALPINE TUNDRA\n");
                if (isServer) {
                    ssm.sendText("map,0");
                }
            } else if (evt.getSource() == oasisDesertMapButton) {
                intMapChoice = 1;
                loadMap(mapFiles[intMapChoice]);
                chatArea.append("[LOBBY] Map chosen: OASIS DESERT\n");
                if (isServer) {
                    ssm.sendText("map,1");
                }
            } else if (evt.getSource() == floatingIslandMapButton) {
                intMapChoice = 2;
                loadMap(mapFiles[intMapChoice]);
                chatArea.append("[LOBBY] Map chosen: FLOATING ISLAND\n");
                if (isServer) {
                    ssm.sendText("map,2");
                }
            } else if (evt.getSource() == playButton) {
                thePanel.intGameState = 4;
                if (isServer) {
                    ssm.sendText("start");
                }
                theTimer.start();
            } else if (evt.getSource() == chooseHostButton) {
                chooseRole = 0;
            } else if (evt.getSource() == chooseJoinButton) {
                chooseRole = 1;
            } else if (evt.getSource() == confirmRoleButton) {
                if (chooseRole == -1) {
                    chatArea.append("[SYSTEM] Please select a Host or Join before confirming.\n");
                    return;
                }
                
                if (strPlayerColour == null) {
                    chatArea.append("[SYSTEM] Please select a player colour before confirming.\n");
                    return;
                }

                thePanel.choosingNetworkRole = false;

                if (chooseRole == 0) {
                    // HOST MODE
                    isServer = true;
                    ssm = new SuperSocketMaster(1337, this);
                    ssm.connect();
                    
                    String myRealIP = ssm.getMyAddress();
                    joiningIPInfo.setText("Hosting! Tell Guest IP: " + myRealIP);
                    chatArea.append("[SYSTEM] Server started! Waiting for players...\n");

                    // Add in host player
                    intPlayersConnected += 1;
                    //int randomX = (int)(Math.random() * 1280) + 1; 
                    //int randomY = (int)(Math.random() * 720) + 1;
                    
                    playerList.add(new Player(intPlayersConnected, 6*80, 2*80, strPlayerColour));
                    playersConnectedLabel.setText(intPlayersConnected + " Player(s) Connected");

                } else if (chooseRole == 1) {
                    // JOIN MODE
                    isServer = false;
                    strIPAddress = enterIPAddress.getText();

                    if (strIPAddress == null || strIPAddress.trim().equals("")) {
                        strIPAddress = "127.0.0.1";
                    }

                    ssm = new SuperSocketMaster(strIPAddress, 1337, this);
                    boolean connected = ssm.connect();

                    // If connected sucessfully
                    if (connected == true){
                        chatArea.append("[SYSTEM] Connected to server...\n");

                        ssm.sendText("hello," + strPlayerColour);
                    // If failed to connect
                    } else {
                        chatArea.append("[SYSTEM] Failed to connect to server. Please check the IP address and try again.\n");
                        ssm = null;
                        thePanel.choosingNetworkRole = true;
                    }
                }
            } else if (evt.getSource() == easyButton) {
                intGameDifficulty = 1;
                intGameSpeed = 3;
                chatArea.append("[SYSTEM] Game Difficulty set to: EASY (Speed: Slow)\n");
            } else if (evt.getSource() == mediumButton) {
                intGameDifficulty = 2;
                intGameSpeed = 5;
                chatArea.append("[SYSTEM] Game Difficulty set to: MEDIUM (Speed: Normal)\n");
            } else if (evt.getSource() == hardButton) {
                intGameDifficulty = 3;
                intGameSpeed = 7;
                chatArea.append("[SYSTEM] Game Difficulty set to: HARD (Speed: Fast!)\n");
            } else if (evt.getSource() == redButton) {
                strPlayerColour = "Red";
                chatArea.append("[LOBBY] Player color set to: RED\n");
            } else if (evt.getSource() == blueButton) {
                strPlayerColour = "Blue";
                chatArea.append("[LOBBY] Player color set to: BLUE\n");
            } else if (evt.getSource() == greenButton) {
                strPlayerColour = "Green";
                chatArea.append("[LOBBY] Player color set to: GREEN\n");
            } else if (evt.getSource() == purpleButton) {
                strPlayerColour = "Purple";
                chatArea.append("[LOBBY] Player color set to: PURPLE\n");
            } else if (evt.getSource() == enterIPAddress) {
                strIPAddress = enterIPAddress.getText();
            } else if (evt.getSource() == chatInput){
                // Chat Messages
                String chatMessage = chatInput.getText();

                if (chatMessage != null && !chatMessage.trim().equals("")) {
                    if (ssm != null) {
                        chatArea.append("[YOU] " + chatMessage + "\n");
                        ssm.sendText("chat," + strPlayerColour + "," + chatMessage);
                        chatInput.setText("");
                    } else {
                        chatArea.append("[SYSTEM] You are not connected yet! Please connect first.\n");
                    }
                   
                }
            } else if (evt.getSource() == ssm && ssm != null) {
                // Read and separate incoming messages
                String incomingMessage = ssm.readText();
                String[] message = incomingMessage.split(",", 3);
                String word = message[0];

                if (word.equals("hello")) {
                    intPlayersConnected += 1;
                    String guestColour = message[1];

                    playerList.add(new Player(intPlayersConnected, 600, 300, guestColour));
                    playersConnectedLabel.setText(intPlayersConnected + " Player(s) Connected");
                    chatArea.append("[SYSTEM] Guest joined as " + guestColour + "!\n");
                    
                    if (isServer) {
                        ssm.sendText("hostInfo," + intPlayersConnected + "," + strPlayerColour);
                    }
                } else if (word.equals("hostInfo")) {
                    intPlayersConnected = Integer.parseInt(message[1]);
                    String hostColour = message[2];

                    playerList.add(new Player(1, 400, 300, hostColour));

                    if (intPlayersConnected == 2) {
                        // Spawn in bottom right
                        playerList.add(new Player(intPlayersConnected, 9*80, 6*80, strPlayerColour));
                    } else if (intPlayersConnected == 3) {
                        // Spawn in bottom left
                        playerList.add(new Player(intPlayersConnected, 6*80, 6*80, strPlayerColour));
                    } else if (intPlayersConnected == 4) {
                        // Spawn in top right
                        playerList.add(new Player(intPlayersConnected, 9*80, 2*80, strPlayerColour));
                    }
                    
                    playersConnectedLabel.setText(intPlayersConnected + " Player(s) Connected");

                } else if (word.equals("map")) {
                    intMapChoice = Integer.parseInt(message[1]);
                    loadMap(mapFiles[intMapChoice]);
                    chatArea.append("[SYSTEM] Host changed map to option " + intMapChoice + "\n");
                } else if (word.equals("start")) {
                    thePanel.intGameState = 4;
                    chatArea.append("[SYSTEM] Game starting!\n");
                } else if (word.equals("chat")) {
                    if (message.length >= 3) {
                        chatArea.append("[" + message[1] + "] " + message[2] + "\n");
                    }
                }
                showCurrentGUI();
                return;

            } else if (evt.getSource() == theTimer) {
                for (Player p: playerList) {
                    // move players
                    
                    // check collisions

                }

                thePanel.repaint();
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

        // Add Connect Pop-up Panel
        chooseRoleLabel.setBounds(420, 220, 400, 50);
        thePanel.add(chooseRoleLabel);

        chooseHostButton.setBounds(420, 270, 100, 50);
        chooseHostButton.addActionListener(this);
        thePanel.add(chooseHostButton);

        chooseJoinButton.setBounds(620, 270, 100, 50);
        chooseJoinButton.addActionListener(this);
        thePanel.add(chooseJoinButton);

        // Host Specific Options
        chooseDifficultyLabel.setBounds(420, 500, 400, 50);
        thePanel.add(chooseDifficultyLabel);

        easyButton.setBounds(350, 550, 100, 100);
        easyButton.addActionListener(this);
        thePanel.add(easyButton);

        mediumButton.setBounds(450, 550, 100, 100);
        mediumButton.addActionListener(this);
        thePanel.add(mediumButton);

        hardButton.setBounds(550, 550, 100, 100);
        hardButton.addActionListener(this);
        thePanel.add(hardButton);

        // Player Specific Optoins
        enterIPAddressLabel.setBounds(420, 300, 400, 50);
        thePanel.add(enterIPAddressLabel);

        enterIPAddress.setBounds(425, 350, 400, 50);
        enterIPAddress.addActionListener(this);
        thePanel.add(enterIPAddress);

        joiningIPInfo.setBounds(600, 400, 500, 50);
        thePanel.add(joiningIPInfo);
        
        choosePlayerColourLabel.setBounds(420, 400, 400, 50);
        thePanel.add(choosePlayerColourLabel);

        redButton.setBounds(350, 450, 100, 100);
        // redButton.setBackground(Color.RED);
        redButton.addActionListener(this);
        thePanel.add(redButton);

        blueButton.setBounds(450, 450, 100, 100);
        // blueButton.setForeground(Color.WHITE);
        // blueButton.setBackground(Color.BLUE);
        blueButton.addActionListener(this);
        thePanel.add(blueButton);

        greenButton.setBounds(550, 450, 100, 100);
        // greenButton.setBackground(Color.GREEN);
        greenButton.addActionListener(this);
        thePanel.add(greenButton);

        purpleButton.setBounds(650, 450, 100, 100);
        // purpleButton.setBackground(Color.MAGENTA);
        purpleButton.addActionListener(this);
        thePanel.add(purpleButton);

        confirmRoleButton.setBounds(700, 650, 100, 50);
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

        thePanel.currentPlayers = this.playerList;
        thePanel.repaint();

        // draw shared J Components
        backButton.setVisible(isLobby || isHelp || isCredits);
        titleSpleef.setVisible(isHome || isLobby);

        // if on home screen:
        lobbyButton.setVisible(isHome);
        helpButton.setVisible(isHome);
        creditsButton.setVisible(isHome);

        // if on lobby screen:
        // choosing host or player
        chooseRoleLabel.setVisible(isLobby && thePanel.choosingNetworkRole);
        chooseHostButton.setVisible(isLobby && thePanel.choosingNetworkRole);
        chooseJoinButton.setVisible(isLobby && thePanel.choosingNetworkRole);

        // chose host
        chooseDifficultyLabel.setVisible(isLobby && thePanel.choosingNetworkRole && chooseRole == 0);
        easyButton.setVisible(isLobby && thePanel.choosingNetworkRole && chooseRole == 0);
        mediumButton.setVisible(isLobby && thePanel.choosingNetworkRole && chooseRole == 0);
        hardButton.setVisible(isLobby && thePanel.choosingNetworkRole && chooseRole == 0);

        // chose player
        enterIPAddressLabel.setVisible(isLobby && thePanel.choosingNetworkRole && chooseRole == 1);
        enterIPAddress.setVisible(isLobby && thePanel.choosingNetworkRole && chooseRole == 1);

        choosePlayerColourLabel.setVisible(isLobby && thePanel.choosingNetworkRole && chooseRole != -1);
        redButton.setVisible(isLobby && thePanel.choosingNetworkRole && chooseRole != -1);
        blueButton.setVisible(isLobby && thePanel.choosingNetworkRole && chooseRole != -1);
        greenButton.setVisible(isLobby && thePanel.choosingNetworkRole && chooseRole != -1);
        purpleButton.setVisible(isLobby && thePanel.choosingNetworkRole && chooseRole != -1);

        // confirm
        confirmRoleButton.setVisible(isLobby & thePanel.choosingNetworkRole);

        // real lobby
        joiningIPInfo.setVisible(isLobby && !thePanel.choosingNetworkRole && chooseRole == 0);
        playersConnectedLabel.setVisible(isLobby && !thePanel.choosingNetworkRole);
        scrollChatArea.setVisible(isLobby && !thePanel.choosingNetworkRole);
        chatInput.setVisible(isLobby && !thePanel.choosingNetworkRole);

        chooseMapLabel.setVisible(isLobby && !thePanel.choosingNetworkRole && isServer);
        alpineTundraMapButton.setVisible(isLobby && !thePanel.choosingNetworkRole && isServer);
        oasisDesertMapButton.setVisible(isLobby && !thePanel.choosingNetworkRole && isServer);
        floatingIslandMapButton.setVisible(isLobby && !thePanel.choosingNetworkRole && isServer);
        playButton.setVisible(isLobby && !thePanel.choosingNetworkRole && isServer);
        

        // if on help screen:

        // if on credits screen:

    }

    // Main Program
    public static void main(String[] args) {
        new GameModel();
    }

}