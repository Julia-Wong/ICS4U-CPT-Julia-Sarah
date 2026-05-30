import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
// import javax.swing.event.*;

public class GameModel implements ActionListener, KeyListener {
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
    JLabel setupErrorLabel = new JLabel();

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

    JButton homeButton = new JButton("Home");
    JLabel gameOverLabel = new JLabel("Game Over!");
    JLabel winnerLabel = new JLabel();

    // Data
    String[] mapFiles = {"alpineTundraMap.csv", "oasisDesertMap.csv", "floatingIslandMap.csv"};
    int intMapChoice = -1;
    int intPlayersConnected = 0;
    ArrayList<Player> playerList = new ArrayList<Player>();
    int[][] crumbleTileTimer = new int[9][16];

    // Methods
    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == ssm && ssm != null) {
                // Read and separate incoming messages
                String incomingMessage = ssm.readText();
                String[] message = incomingMessage.split(",");
                String word = message[0];

                if (word.equals("hello")) {
                    intPlayersConnected += 1;
                    String guestColour = message[1];

                    int intSpawnX = 0;
                    int intSpawnY = 0;

                    if (intPlayersConnected == 2) {
                        // Spawn in bottom right
                        intSpawnX = 9 * 80;
                        intSpawnY = 6 * 80;
                    } else if (intPlayersConnected == 3) {
                        // Spawn in bottom left
                        intSpawnX = 6 * 80;
                        intSpawnY = 6 * 80;
                    } else if (intPlayersConnected == 4) {
                        // Spawn in top right
                        intSpawnX = 9 * 80;
                        intSpawnY = 2 * 80;
                    }

                    playerList.add(new Player(intPlayersConnected, intSpawnX, intSpawnY, guestColour));
                    playersConnectedLabel.setText(intPlayersConnected + " Player(s) Connected");
                    chatArea.append("[SYSTEM] Guest joined as " + guestColour + "!\n");
                    
                    if (isServer) {
                        ssm.sendText("hostInfo," + intPlayersConnected + "," + strPlayerColour + "," + intMapChoice);
                    }
                } else if (word.equals("hostInfo")) {
                    intPlayersConnected = Integer.parseInt(message[1]);
                    String hostColour = message[2];
                    int hostMap = Integer.parseInt(message[3]);

                    intMapChoice = hostMap;
                    if (intMapChoice != -1) {
                        loadMap(mapFiles[intMapChoice]);
                    }

                    playerList.clear();
                    playerList.add(new Player(1, 6 * 80, 2 * 80, hostColour));

                    int intHostSpawnX = 0;
                    int intHostSpawnY = 0;

                    if (intPlayersConnected == 2) {
                        // Spawn in bottom right
                        intHostSpawnX = 9 * 80;
                        intHostSpawnY = 6 * 80;
                    } else if (intPlayersConnected == 3) {
                        // Spawn in bottom left
                        intHostSpawnX = 6 * 80;
                        intHostSpawnY = 6 * 80;
                    } else if (intPlayersConnected == 4) {
                        // Spawn in top right
                        intHostSpawnX = 9 * 80;
                        intHostSpawnY = 2 * 80;
                    }
                    
                    playerList.add(new Player(intPlayersConnected, intHostSpawnX, intHostSpawnY, strPlayerColour));
                    playersConnectedLabel.setText(intPlayersConnected + " Player(s) Connected");

                } else if (word.equals("map")) {
                    intMapChoice = Integer.parseInt(message[1]);
                    loadMap(mapFiles[intMapChoice]);
                    chatArea.append("[SYSTEM] Host changed map to option " + intMapChoice + "\n");
                } else if (word.equals("start")) {
                    thePanel.intGameState = 4;
                    chatArea.append("[SYSTEM] Game starting!\n");
                    theTimer.start();

                    thePanel.setFocusable(true);
                    thePanel.requestFocusInWindow();
                } else if (word.equals("chat")) {
                    if (message.length >= 3) {
                        chatArea.append("[" + message[1] + "] " + message[2] + "\n");
                    }
                } else if (word.equals("move")) {
                    String movingPlayerColour = message[1];
                    int newX = Integer.parseInt(message[2]);
                    int newY = Integer.parseInt(message[3]);

                    for (Player p: playerList) {
                        if (p.strColour.equals(movingPlayerColour)) {
                            p.intX = newX;
                            p.intY = newY;
                            break;
                        }
                    }
                } else if (word.equals("break")) {
                    // update tile health
                    int row = Integer.parseInt(message[1]);
                    int col = Integer.parseInt(message[2]);
                    int newHealth = Integer.parseInt(message[3]);

                    thePanel.tileHealth[row][col] = newHealth;
                } else if (word.equals("die")) {
                    String playerDead = message[1];

                    for (Player p: playerList) {
                        if (p.strColour.equals(playerDead)) {
                            p.isAlive = false;
                            break;
                        }
                    }
                }

                showCurrentGUI();
                return;

            }
            
        if (evt.getSource() == theTimer) {
            Player myPlayer = null;
            for (Player p: playerList) {
                if (p.strColour.equals(strPlayerColour)) {
                    myPlayer = p;
                    break;
                }
            }

            // move players
                if (myPlayer != null && myPlayer.isAlive) {
                    int intOldX = myPlayer.intX;
                    int intOldY = myPlayer.intY;

                    if (myPlayer.upPressed == true) {
                        myPlayer.intY -= intGameSpeed;
                    }
                    if (myPlayer.downPressed == true) {
                        myPlayer.intY += intGameSpeed;
                    }
                    if (myPlayer.rightPressed == true) {
                        myPlayer.intX += intGameSpeed;
                    }
                    if (myPlayer.leftPressed == true) {
                        myPlayer.intX -= intGameSpeed;
                    }

                    if (myPlayer.intX != intOldX || myPlayer.intY != intOldY) {
                        ssm.sendText("move," + strPlayerColour + "," + myPlayer.intX + "," + myPlayer.intY);
                    }
                }

                // check collisions

                // screen boundaries
                if (myPlayer.intX < 0) {
                    myPlayer.intX = 0;
                }
                if (myPlayer.intX > 1240) {
                    myPlayer.intX = 1240;
                }
                if (myPlayer.intY < 0) {
                    myPlayer.intY = 0;
                }
                if (myPlayer.intY > 680) {
                    myPlayer.intY = 680;
                }

                int playerRow = (myPlayer.intY + 20) / 80;
                int playerCol = (myPlayer.intX + 20) / 80;
                int currentTileHeatlh = thePanel.tileHealth[playerRow][playerCol];

                // check tiles
                if (currentTileHeatlh == 3) { // check death
                    myPlayer.isAlive = false;
                    ssm.sendText("die," + strPlayerColour);
                } else { // degrade tile
                    if (playerRow != myPlayer.intCurrentRow || playerCol != myPlayer.intCurrentCol) { // if on new tile
                        // update player location
                        myPlayer.intCurrentRow = playerRow;
                        myPlayer.intCurrentCol = playerCol;
                        myPlayer.intFramesOnTile = 0;

                        if (thePanel.tileHealth[playerRow][playerCol] < 2) {
                            // damage tile
                            thePanel.tileHealth[playerRow][playerCol] += 1;

                            // send network message
                            ssm.sendText("break," + playerRow + "," + playerCol + "," + thePanel.tileHealth[playerRow][playerCol]);
                        } else if (thePanel.tileHealth[playerRow][playerCol] == 2) {
                            if (crumbleTileTimer[playerRow][playerCol] == 0) {
                                crumbleTileTimer[playerRow][playerCol] = 30;
                            }
                        }
                        
                    } else { // if standing on same tile
                        myPlayer.intFramesOnTile += 1;
                        if (myPlayer.intFramesOnTile >= 30) {
                            thePanel.tileHealth[playerRow][playerCol] += 1;
                            myPlayer.intFramesOnTile = 0;
                            ssm.sendText("break," + playerRow + "," + playerCol + "," + thePanel.tileHealth[playerRow][playerCol]);
                        }
                    }

                }

            for (int r = 0; r < thePanel.map.length; r++) {
                for (int c = 0; c < thePanel.map[r].length; c++) {
                    if (crumbleTileTimer[r][c] > 0) {
                        crumbleTileTimer[r][c]--;

                        if (crumbleTileTimer[r][c] == 0) {
                            thePanel.tileHealth[r][c] = 3;
                            ssm.sendText("break," + r + "," + c + ",3");
                        }
                    }
                    
                }
            }

            // check for winner
            if (thePanel.intGameState == 4) {
                int aliveCount = 0;
                String winnerName = "";

                for (Player p: playerList) {
                    if (p.isAlive) {
                        aliveCount++;
                        winnerName = p.strColour;
                    }
                }

                if (aliveCount <=1) {
                    thePanel.intGameState = 5;
                    theTimer.stop();

                    if (aliveCount == 1) {
                        winnerLabel.setText(winnerName + " WINS!");
                    } else {
                        winnerLabel.setText("DRAW! Everyone Fell!");
                    }

                    showCurrentGUI();
                }
            }

            thePanel.repaint();
            return;
        }

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
                boolean hasDuplicateColours = false;

                // go compare all player colours to one another
                for (int i = 0; i < playerList.size(); i++) {
                    for (int j = i + 1; j < playerList.size(); j++) {
                        if (playerList.get(i).strColour.equals(playerList.get(j).strColour)) {
                            hasDuplicateColours = true;
                        }
                    }
                }

                // check all rules before playing
                if (intMapChoice == -1) {
                    chatArea.append("[SYSTEM] Please select a map first!\n");
                } else if (playerList.size() < 2) {
                    chatArea.append("[SYSTEM] Minimum 2 players required!\n");
                } else if (playerList.size() > 4) {
                    chatArea.append("[SYSTEM] Maximum 4 players allowed!\n");
                } else if (hasDuplicateColours) {
                    chatArea.append("[SYSTEM] Players cannot share the same colour!\n");
                } else {
                    thePanel.intGameState = 4;
                    if (isServer) {
                        ssm.sendText("start");
                    }
                    theTimer.start();
                    thePanel.repaint();

                    thePanel.setFocusable(true);
                    thePanel.requestFocusInWindow();
                }
                
            } else if (evt.getSource() == chooseHostButton) {
                chooseRole = 0;
            } else if (evt.getSource() == chooseJoinButton) {
                chooseRole = 1;
            } else if (evt.getSource() == confirmRoleButton) {
                if (chooseRole == -1) {
                    setupErrorLabel.setText("Please select Host or Join first!");
                    return;
                }
                
                if (strPlayerColour == null) {
                    setupErrorLabel.setText("Please select a player colour!");
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
                    
                    playerList.add(new Player(intPlayersConnected, 6 * 80, 2 * 80, strPlayerColour));
                    playersConnectedLabel.setText(intPlayersConnected + " Player(s) Connected");
                    setupErrorLabel.setText("");

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
                        setupErrorLabel.setText("");
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

        // if on end screen
        if (thePanel.intGameState == 5) {
            if (evt.getSource() == homeButton) {
                thePanel.intGameState = 0;
            }      
        }

        showCurrentGUI();
    }   

    public void keyTyped(KeyEvent evt) {

    }

    public void keyPressed(KeyEvent evt) {
        if (thePanel.intGameState != 4) {
            return;
        }
        
        Player myPlayer = null;
        for (Player p: playerList) {
            if (p.strColour.equals(strPlayerColour)) {
                myPlayer = p;
                break;
            }
        }

        if (myPlayer == null) {
            return;
        }

        if (evt.getKeyChar() == 'w' || evt.getKeyChar() == 'W') {
            myPlayer.upPressed = true;
        } else if (evt.getKeyChar() == 's' || evt.getKeyChar() == 'S') {
            myPlayer.downPressed = true;
        } else if (evt.getKeyChar() == 'a' || evt.getKeyChar() == 'A') {
            myPlayer.leftPressed = true;
        } else if (evt.getKeyChar() == 'd' || evt.getKeyChar() == 'D') {
            myPlayer.rightPressed = true;
        }
    }

    public void keyReleased(KeyEvent evt) {
        if (thePanel.intGameState != 4) {
            return;
        }

        Player myPlayer = null;
        for (Player p: playerList) {
            if (p.strColour.equals(strPlayerColour)) {
                myPlayer = p;
                break;
            }
        }

        if (myPlayer == null) {
            return;
        }

        if (evt.getKeyChar() == 'w' || evt.getKeyChar() == 'W') {
            myPlayer.upPressed = false;
        } else if (evt.getKeyChar() == 's' || evt.getKeyChar() == 'S') {
            myPlayer.downPressed = false;
        } else if (evt.getKeyChar() == 'a' || evt.getKeyChar() == 'A') {
            myPlayer.leftPressed = false;
        } else if (evt.getKeyChar() == 'd' || evt.getKeyChar() == 'D') {
            myPlayer.rightPressed = false;
        }
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

        // Key Listener
        thePanel.addKeyListener(this);

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

        setupErrorLabel.setBounds(20, 600, 400, 50);
        thePanel.add(setupErrorLabel);

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

        // game over screen GUI
        gameOverLabel.setBounds(300, 200, 400, 50);
        thePanel.add(gameOverLabel);

        winnerLabel.setBounds(300, 250, 400, 50);
        thePanel.add(winnerLabel);

        homeButton.setBounds(300, 500, 300, 100);
        homeButton.addActionListener(this);
        thePanel.add(homeButton);

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
        boolean isEndScreen = thePanel.intGameState == 5;

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
        setupErrorLabel.setVisible(isLobby && thePanel.choosingNetworkRole);

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


        // if on end screen
        gameOverLabel.setVisible(isEndScreen);
        winnerLabel.setVisible(isEndScreen);
        homeButton.setVisible(isEndScreen);
    }

    // Main Program
    public static void main(String[] args) {
        new GameModel();
    }

}