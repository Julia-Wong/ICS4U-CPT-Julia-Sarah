import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

public class GameModel implements ActionListener, KeyListener {
    // === PROPERTIES ===

    // 1. Main Window Properties
    JFrame theFrame = new JFrame("Spleef");
    GameView thePanel = new GameView();
    Timer theTimer = new Timer(1000/60, this);

    // 2. Main Menu Components
    JButton lobbyButton = new JButton("Lobby");
    JButton helpButton = new JButton("Help");
    JButton creditsButton = new JButton("Credits");
    JButton backButton = new JButton("← Back");

    // 3. Network Connection GUI
    JLabel chooseRoleLabel = new JLabel("Choose your network role: ");
    JButton chooseHostButton = new JButton("Host Game");
    JButton chooseJoinButton = new JButton("Join Game");

    JLabel enterIPAddressLabel = new JLabel("Enter Host's IP Address: ");
    JTextField enterIPAddress = new JTextField();

    JLabel choosePlayerColourLabel = new JLabel("Choose player colour: ");
    JButton redButton = new JButton("Red");
    JButton blueButton = new JButton("Blue");
    JButton greenButton = new JButton("Green");
    JButton purpleButton = new JButton("Purple");

    JButton confirmRoleButton = new JButton("Confirm");
    JLabel setupErrorLabel = new JLabel();

    // 4. Host Specific Game Lobby Modifiers
    JLabel chooseDifficultyLabel = new JLabel("Choose game difficulty: ");
    JButton easyButton = new JButton("Easy");
    JButton mediumButton = new JButton("Medium");
    JButton hardButton = new JButton("Hard");

    // 5. General Lobby Screen Components
    JLabel playersConnectedLabel = new JLabel();
    JLabel joiningIPInfo = new JLabel();

    JLabel chooseMapLabel = new JLabel("Choose a Map: ");
    JButton alpineTundraMapButton = new JButton("Alpine Tundra Map"); // snow stone 
    JButton oasisDesertMapButton = new JButton("Oasis Desert Map"); // sand dirt
    JButton floatingIslandMapButton = new JButton("Floating Island Map"); // grass dirt sand
    JButton playButton = new JButton("PLAY");

    JTextArea chatArea = new JTextArea();
    JScrollPane scrollChatArea = new JScrollPane(chatArea);
    JTextField chatInput = new JTextField();

    // 6. Playable DEMO Buttons
    JButton demoButton = new JButton("Demo");
    JButton demoResetButton = new JButton("Reset Demo");
    JButton demoBackButton = new JButton("Exit Demo");

    // 7. Post-Game Layout
    JLabel gameOverLabel = new JLabel("Game Over!");
    JLabel winnerLabel = new JLabel();
    JButton homeButton = new JButton("Home");

    // 8. Networking Data
    SuperSocketMaster ssm;
    boolean isServer = false;
    int chooseRole = -1; // -1=unassigned, 0=host, 1=player
    int intGameSpeed = 5;
    int intGameDifficulty = 2; // 1=easy, 2=medium, 3=hard
    String strPlayerColour;
    String strIPAddress;

    // 9. Game Data
    String[] mapFiles = {"alpineTundraMap.csv", "oasisDesertMap.csv", "floatingIslandMap.csv"};
    int intMapChoice = -1;
    int intPlayersConnected = 0;
    ArrayList<Player> playerList = new ArrayList<Player>();
    int[][] crumbleTileTimer = new int[9][16];

    // === METHODS ===

    /**
     * Processes all triggers, including network messages, timers, and buttons.
     * @param evt The ActionEvent captured from a button, network socket, or timer.
     */
    public void actionPerformed(ActionEvent evt) {

        // 1. Network Receiver
        if (ssm != null && evt.getSource() == ssm) {

            // Read and separate incoming messages
            String incomingMessage = ssm.readText();
            String[] message = incomingMessage.split(",");
            String word = message[0];

            // A new guest joins the server
            if (word.equals("hello")) {
                intPlayersConnected += 1;
                String guestColour = message[1];

                // Set spawn points for each player
                int intSpawnX = 0;
                int intSpawnY = 0;
                if (intPlayersConnected == 2) {
                    intSpawnX = 9 * 80;
                    intSpawnY = 6 * 80;
                } else if (intPlayersConnected == 3) {
                    intSpawnX = 6 * 80;
                    intSpawnY = 6 * 80;
                } else if (intPlayersConnected == 4) {
                    intSpawnX = 9 * 80;
                    intSpawnY = 2 * 80;
                }

                playerList.add(new Player(intPlayersConnected, intSpawnX, intSpawnY, guestColour));
                playersConnectedLabel.setText(intPlayersConnected + " Player(s) Connected");
                chatArea.append("[SYSTEM] Guest joined as " + guestColour + "!\n");
                
                if (isServer) {
                    ssm.sendText("hostInfo," + intPlayersConnected + "," + strPlayerColour + "," + intMapChoice + "," + intGameSpeed);
                }
            } 

            // Transfers data from host to the players
            else if (word.equals("hostInfo")) {
                intPlayersConnected = Integer.parseInt(message[1]);
                String hostColour = message[2];
                int hostMap = Integer.parseInt(message[3]);
                intGameSpeed = Integer.parseInt(message[4]);

                intMapChoice = hostMap;
                if (intMapChoice != -1) {
                    loadMap(mapFiles[intMapChoice]);
                }

                // Lets all players know who the host is
                playerList.clear();
                playerList.add(new Player(1, 6 * 80, 2 * 80, hostColour));

                // Sets spawn point for host
                int intHostSpawnX = 0;
                int intHostSpawnY = 0;
                if (intPlayersConnected == 2) {
                    intHostSpawnX = 9 * 80;
                    intHostSpawnY = 6 * 80;
                } else if (intPlayersConnected == 3) {
                    intHostSpawnX = 6 * 80;
                    intHostSpawnY = 6 * 80;
                } else if (intPlayersConnected == 4) {
                    intHostSpawnX = 9 * 80;
                    intHostSpawnY = 2 * 80;
                }
                
                playerList.add(new Player(intPlayersConnected, intHostSpawnX, intHostSpawnY, strPlayerColour));
                playersConnectedLabel.setText(intPlayersConnected + " Player(s) Connected");
            } 
            
            // Transfer host game set up info to players
            else if (word.equals("map")) {
                intMapChoice = Integer.parseInt(message[1]);
                loadMap(mapFiles[intMapChoice]);
                chatArea.append("[SYSTEM] Host changed map to option " + intMapChoice + "\n");
            } else if (word.equals("start")) {
                thePanel.intGameState = 4;
                chatArea.append("[SYSTEM] Game starting!\n");
                theTimer.start();
                thePanel.setFocusable(true);
                thePanel.requestFocusInWindow();
            } 
            
            // Texting over network
            else if (word.equals("chat")) {
                if (message.length >= 3) {
                    chatArea.append("[" + message[1] + "] " + message[2] + "\n");
                }
            } 
            
            // Game mechanics shared over network
            else if (word.equals("move")) {
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
                // Update tile health
                int row = Integer.parseInt(message[1]);
                int col = Integer.parseInt(message[2]);
                int newHealth = Integer.parseInt(message[3]);
                thePanel.tileHealth[row][col] = newHealth;
            } else if (word.equals("die")) {
                // Remove players from game when they die
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
            
        // 2. Game Timer
        if (evt.getSource() == theTimer) {
            
            // During multiplayer game
            if (thePanel.intGameState == 4) {
                // Determine which player object is their character
                Player myPlayer = null;
                for (Player p: playerList) {
                    if (p.strColour.equals(strPlayerColour)) {
                        myPlayer = p;
                        break;
                    }
                }

                // Move player
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

                // Screen boundaries
                if (myPlayer != null) {
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

                    // Determine where players are
                    int playerRow = (myPlayer.intY + 20) / 80;
                    int playerCol = (myPlayer.intX + 20) / 80;
                    int currentTileHealth = thePanel.tileHealth[playerRow][playerCol];

                    // Process player contact with tiles
                    if (currentTileHealth == 3) { // Check death
                        myPlayer.isAlive = false;
                        ssm.sendText("die," + strPlayerColour);
                    } else { // Check tile
                        // If on new tile
                        if (playerRow != myPlayer.intCurrentRow || playerCol != myPlayer.intCurrentCol) { 
                            myPlayer.intCurrentRow = playerRow;
                            myPlayer.intCurrentCol = playerCol;
                            myPlayer.intFramesOnTile = 0;

                            if (thePanel.tileHealth[playerRow][playerCol] < 2) { // Damage Tile
                                thePanel.tileHealth[playerRow][playerCol] += 1;
                                ssm.sendText("break," + playerRow + "," + playerCol + "," + thePanel.tileHealth[playerRow][playerCol]);
                            } else if (thePanel.tileHealth[playerRow][playerCol] == 2) { // Set off crumble timer
                                if (crumbleTileTimer[playerRow][playerCol] == 0) {
                                    crumbleTileTimer[playerRow][playerCol] = 30;
                                }
                            }
                        } 

                        // If standing on same tile
                        else { 
                            myPlayer.intFramesOnTile += 1;
                            if (myPlayer.intFramesOnTile >= 30) {
                                thePanel.tileHealth[playerRow][playerCol] += 1;
                                myPlayer.intFramesOnTile = 0;
                                ssm.sendText("break," + playerRow + "," + playerCol + "," + thePanel.tileHealth[playerRow][playerCol]);
                            }
                        }
                    }
                }
                

                // Decrement tile for crumbling effect
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

                // Check for winner
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

            // If on DEMO screen
            if (thePanel.intGameState == 6) {
                Player myPlayerDemo = null;
                if (!playerList.isEmpty()) {
                    myPlayerDemo = playerList.get(0);
                }

                // Movement
                if (myPlayerDemo != null && myPlayerDemo.isAlive) {
                    if (myPlayerDemo.upPressed == true) {
                        myPlayerDemo.intY -= intGameSpeed;
                    }
                    if (myPlayerDemo.downPressed == true) {
                        myPlayerDemo.intY += intGameSpeed;
                    }
                    if (myPlayerDemo.rightPressed == true) {
                        myPlayerDemo.intX += intGameSpeed;
                    }
                    if (myPlayerDemo.leftPressed == true) {
                        myPlayerDemo.intX -= intGameSpeed;
                    }
                }

                // Bounds
                if (myPlayerDemo.intX < 0) {
                    myPlayerDemo.intX = 0;
                } if (myPlayerDemo.intX > 1240) {
                    myPlayerDemo.intX = 1240;
                } if (myPlayerDemo.intY < 0) {
                    myPlayerDemo.intY = 0;
                } if (myPlayerDemo.intY > 680) {
                    myPlayerDemo.intY = 680;
                }

                // Tiles
                int playerRowDemo = (myPlayerDemo.intY + 20)/80;
                int playerColDemo = (myPlayerDemo.intX + 20)/80;

                if (thePanel.tileHealth[playerRowDemo][playerColDemo] == 3) { // Check death
                    myPlayerDemo.isAlive = false;
                } else { // Check tiles
                    if (playerRowDemo != myPlayerDemo.intCurrentRow || playerColDemo != myPlayerDemo.intCurrentCol) { // Moved to new tile
                        myPlayerDemo.intCurrentRow = playerRowDemo;
                        myPlayerDemo.intCurrentCol = playerColDemo;
                        myPlayerDemo.intFramesOnTile = 0;

                        if (thePanel.tileHealth[playerRowDemo][playerColDemo] < 2) { // Damage tile
                            thePanel.tileHealth[playerRowDemo][playerColDemo] += 1;
                        } else if (thePanel.tileHealth[playerRowDemo][playerColDemo] == 2) { // Start crumble timer
                            if (crumbleTileTimer[playerRowDemo][playerColDemo] == 0) {
                                crumbleTileTimer[playerRowDemo][playerColDemo] = 30;
                            }
                        }
                    } else { // Stayed on same tile
                        myPlayerDemo.intFramesOnTile += 1;
                        if (myPlayerDemo.intFramesOnTile >= 30) {
                            thePanel.tileHealth[playerRowDemo][playerColDemo] += 1;
                            myPlayerDemo.intFramesOnTile = 0;
                        }
                    }
                }
            }

            // Countdown for crumble timer
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 16; c++) {
                    if (crumbleTileTimer[r][c] > 0) {
                        crumbleTileTimer[r][c]--;

                        if (crumbleTileTimer[r][c] == 0) {
                            thePanel.tileHealth[r][c] = 3;
                        }
                    }
                }
            }
            showCurrentGUI();
            thePanel.repaint();
            return;
        }

        // 3. Home Screen
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

        // 4. Lobby Screen
        if (thePanel.intGameState == 1) {
            if (evt.getSource() == backButton) {
                // Disconnect from lobby
                if (ssm != null) {
                    ssm.disconnect();
                    ssm = null;
                }

                playerList.clear();
                intPlayersConnected = 0;
                strPlayerColour = null;
                chooseRole = -1;
                isServer = false;
                thePanel.tileHealth = new int[9][16];
                crumbleTileTimer = new int[9][16];
                intMapChoice = -1;

                chatArea.setText("");
                chatInput.setText("");
                playersConnectedLabel.setText("");
                joiningIPInfo.setText("");
                setupErrorLabel.setText("");
                thePanel.intGameState = 0;
            } 
            
            // Load maps
            else if (evt.getSource() == alpineTundraMapButton) {
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
            } 
            
            // Start Game
            else if (evt.getSource() == playButton) {

                // Compares player colours to one another
                boolean hasDuplicateColours = false;
                for (int i = 0; i < playerList.size(); i++) {
                    for (int j = i + 1; j < playerList.size(); j++) {
                        if (playerList.get(i).strColour.equals(playerList.get(j).strColour)) {
                            hasDuplicateColours = true;
                        }
                    }
                }

                // Check all rules before playing
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
                
            } 
            
            // Set up screen elements
            else if (evt.getSource() == chooseHostButton) {
                chooseRole = 0;
            } else if (evt.getSource() == chooseJoinButton) {
                chooseRole = 1;
            } 
            
            // Connect to server
            else if (evt.getSource() == confirmRoleButton) {
                if (chooseRole == -1) {
                    setupErrorLabel.setText("Please select Host or Join first!");
                    return;
                }
                
                if (strPlayerColour == null) {
                    setupErrorLabel.setText("Please select a player colour!");
                    return;
                }

                thePanel.choosingNetworkRole = false;

                // Chose Host
                if (chooseRole == 0) {
                    isServer = true;
                    ssm = new SuperSocketMaster(1337, this);
                    ssm.connect();
                    
                    String myRealIP = ssm.getMyAddress();
                    joiningIPInfo.setText("Hosting! Tell Guest IP: " + myRealIP);
                    chatArea.append("[SYSTEM] Server started! Waiting for players...\n");

                    intPlayersConnected += 1;
                    playerList.add(new Player(intPlayersConnected, 6 * 80, 2 * 80, strPlayerColour));
                    playersConnectedLabel.setText(intPlayersConnected + " Player(s) Connected");
                    setupErrorLabel.setText("");

                } 
                
                // Chose Joiner
                else if (chooseRole == 1) {
                    isServer = false;
                    strIPAddress = enterIPAddress.getText();

                    if (strIPAddress == null || strIPAddress.trim().equals("")) {
                        strIPAddress = "127.0.0.1";
                    }

                    ssm = new SuperSocketMaster(strIPAddress, 1337, this);
                    boolean connected = ssm.connect();

                    if (connected == true){
                        chatArea.append("[SYSTEM] Connected to server...\n");

                        ssm.sendText("hello," + strPlayerColour);
                        setupErrorLabel.setText("");
                    } else {
                        chatArea.append("[SYSTEM] Failed to connect to server. Please check the IP address and try again.\n");
                        ssm = null;
                        thePanel.choosingNetworkRole = true;
                    }
                }
            } 
            
            // Game Difficulty Buttons
            else if (evt.getSource() == easyButton) {
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
            } 
            
            // Character Colour Buttons
            else if (evt.getSource() == redButton) {
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
            } 
            
            // Enter IP Address
            else if (evt.getSource() == enterIPAddress) {
                strIPAddress = enterIPAddress.getText();
            } 
            
            // Network Chat Messages
            else if (evt.getSource() == chatInput){
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
            
            // Highlight Chosen Map
            if (intMapChoice == 0) { // Bold Alpine Tundra
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

        // 5. Help Screen
        if (thePanel.intGameState == 2) {
            if (evt.getSource() == backButton) {
                thePanel.intGameState = 0;
                playerList.clear();
            } 
        }
           
        // 6. Demo Game
        if (evt.getSource() == demoButton || evt.getSource() == demoResetButton) {
            thePanel.intGameState = 6;

            // Create a demo 3x3 grid
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 16; c++) {
                    thePanel.map[r][c] = 0;
                    thePanel.tileHealth[r][c] = 3;
                    crumbleTileTimer[r][c] = 0;
                }
            }

            for (int r = 3; r <= 5; r++) {
                for (int c = 6; c <= 8; c++) {
                    thePanel.tileHealth[r][c] = 0;
                }
            }

            playerList.clear();
            strPlayerColour = "Red";
            playerList.add(new Player(1, 7*80, 4*80, strPlayerColour));

            theTimer.start();
            thePanel.repaint();
            thePanel.setFocusable(true);
            thePanel.requestFocusInWindow();
        } 
        
        if (evt.getSource() == demoBackButton) {
            theTimer.stop();
            thePanel.intGameState = 2;   
        }
        
        // 7. Credits Screen
        if (thePanel.intGameState == 3) {
            if (evt.getSource() == backButton) {
                thePanel.intGameState = 0;
            }
        }

        // 8. End Screen
        if (thePanel.intGameState == 5) {
            if (evt.getSource() == homeButton) {
                // Leave game
                if (ssm != null) {
                    ssm.disconnect();
                    ssm = null;
                }

                // Clear memory & reset game for the next play
                playerList.clear();
                intPlayersConnected = 0;
                strPlayerColour = null;
                chooseRole = -1;
                isServer = false;
                thePanel.tileHealth = new int[9][16];
                crumbleTileTimer = new int[9][16];
                intMapChoice = -1;

                chatArea.setText("");
                chatInput.setText("");
                playersConnectedLabel.setText("");
                joiningIPInfo.setText("");
                setupErrorLabel.setText("");

                thePanel.intGameState = 0;
            }      
        }
        
        showCurrentGUI();
    }   

    /**
     * Required by KeyListener interface. Invoked when a key has been
     * typed, resulting in an outputted code of characters.
     * @param evt The KeyEvent containing the character data.
     */
    public void keyTyped(KeyEvent evt) {
        // Intentionally left blank
    }

    /**
     * Invoked when a key is pressed, initiating player movement.
     * @param evt The KeyEvent tracking which key was pressed.
     */
    public void keyPressed(KeyEvent evt) {
        // Only move characters if they are currently playing
        if (thePanel.intGameState != 4 && thePanel.intGameState != 6) {
            return;
        }
        
        // Determine which player object is the users
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

        // Flag directions the player is trying to move
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

    /**
     * Invoked when a key is released, which halts player movement.
     * @param evt The KeyEvent tracking which key was released.
     */
    public void keyReleased(KeyEvent evt) {
        if (thePanel.intGameState != 4 && thePanel.intGameState != 6) {
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

        // Turn off direction booleans for movement
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

    /**
     * Reads a CSV file to create a 2D map grid array.
     * Parses through integer tile IDs to construct the game's arena floor
     * while catching all possible errors.
     * @param fileName The name of the target map data file.
     */
    public void loadMap(String fileName) {
        BufferedReader mapFile = null;
        try {
            mapFile = new BufferedReader(new FileReader(fileName));
            String mapLine;
            String[] mapSplit;

            // Goes through the 2D array to create the map
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

    // === CONSTRUCTOR ===
    public GameModel(){
        // 1. Game Setup
        thePanel.setLayout(null);
        thePanel.setPreferredSize(new Dimension(1280, 720));
        thePanel.addKeyListener(this);

        // 2. Home Screen GUI
        lobbyButton.setBounds(465, 325, 350, 95);
        lobbyButton.addActionListener(this);
        thePanel.add(lobbyButton);

        helpButton.setBounds(465, 440, 350, 95);
        helpButton.addActionListener(this);
        thePanel.add(helpButton);

        creditsButton.setBounds(465, 555, 350, 95);
        creditsButton.addActionListener(this);
        thePanel.add(creditsButton);

        // 3. Navigation Buttons
        backButton.setBounds(10, 10, 80, 40);
        backButton.addActionListener(this);
        thePanel.add(backButton);

        // 4. Tutorial/DEMO
        demoButton.setBounds(765, 655, 50, 55);
        demoButton.addActionListener(this);
        thePanel.add(demoButton);

        demoResetButton.setBounds(490, 300, 300, 50);
        demoResetButton.addActionListener(this);
        thePanel.add(demoResetButton);

        demoBackButton.setBounds(10, 10, 80, 40);
        demoBackButton.addActionListener(this);
        thePanel.add(demoBackButton);

        // 5. Game Setup: Choose Role
        chooseRoleLabel.setBounds(420, 220, 400, 50);
        thePanel.add(chooseRoleLabel);

        chooseHostButton.setBounds(420, 270, 100, 50);
        chooseHostButton.addActionListener(this);
        thePanel.add(chooseHostButton);

        chooseJoinButton.setBounds(620, 270, 100, 50);
        chooseJoinButton.addActionListener(this);
        thePanel.add(chooseJoinButton);

        // 6. Game Setup: Enter IP Address
        enterIPAddressLabel.setBounds(420, 300, 400, 50);
        thePanel.add(enterIPAddressLabel);

        enterIPAddress.setBounds(425, 350, 400, 50);
        enterIPAddress.addActionListener(this);
        thePanel.add(enterIPAddress);

        // 7. Game Setup: Choose Player Colour
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

        // 8. Game Setup: Confirm Role
        confirmRoleButton.setBounds(700, 650, 100, 50);
        confirmRoleButton.addActionListener(this);
        thePanel.add(confirmRoleButton);

        setupErrorLabel.setBounds(20, 600, 400, 50);
        thePanel.add(setupErrorLabel);

        // 9. Host-Specific Game Settings: Difficulty Level
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

        // 10. Host-Specific Game Settings: Map Choice
        chooseMapLabel.setBounds(500, 400, 400, 100);
        thePanel.add(chooseMapLabel);

        alpineTundraMapButton.setBounds(100, 500, 300, 100);
        alpineTundraMapButton.addActionListener(this);
        thePanel.add(alpineTundraMapButton);

        oasisDesertMapButton.setBounds(400, 500, 300, 100);
        oasisDesertMapButton.addActionListener(this);
        thePanel.add(oasisDesertMapButton);

        floatingIslandMapButton.setBounds(700, 500, 300, 100);
        floatingIslandMapButton.addActionListener(this);
        thePanel.add(floatingIslandMapButton);

        // 11. Host-Specific Game Settings: Play
        playButton.setBounds(500, 600, 400, 100);
        playButton.addActionListener(this);
        thePanel.add(playButton);

        // 12. Main Multiplayer Lobby GUI
        playersConnectedLabel.setBounds(500, 200, 400, 100);
        thePanel.add(playersConnectedLabel);

        joiningIPInfo.setBounds(600, 400, 500, 50);
        thePanel.add(joiningIPInfo);

        // 13. Lobby Chat
        scrollChatArea.setBounds(950, 0, 330, 150);
        thePanel.add(scrollChatArea);

        chatInput.setBounds(950, 150, 330, 50);
        chatInput.addActionListener(this);
        thePanel.add(chatInput);

        // 14. Game Over Layout
        gameOverLabel.setBounds(300, 200, 400, 50);
        thePanel.add(gameOverLabel);

        winnerLabel.setBounds(300, 250, 400, 50);
        thePanel.add(winnerLabel);

        homeButton.setBounds(300, 500, 300, 100);
        homeButton.addActionListener(this);
        thePanel.add(homeButton);

        // 15. Desktop Window Initialization
        showCurrentGUI();

        theFrame.setContentPane(thePanel);
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        theFrame.pack();
        theFrame.setVisible(true);
    }

    /**
     * Manages GUI visibility based on current state.
     */
    public void showCurrentGUI() {
        // 1. Declare All Game States & Sync Players
        boolean isHome = thePanel.intGameState == 0;

        boolean isLobby = thePanel.intGameState == 1;
        boolean isLobbySetup = isLobby && thePanel.choosingNetworkRole;
        boolean isLobbySetupHost = isLobbySetup && chooseRole == 0;
        boolean isLobbySetupJoiner = isLobbySetup && chooseRole == 1;
        boolean isLobbySetupGeneral = isLobbySetup && chooseRole != -1;

        boolean isLobbyGeneral = isLobby && !thePanel.choosingNetworkRole;
        boolean isLobbyHost = isLobbyGeneral && chooseRole == 0;


        boolean isHelp = thePanel.intGameState == 2;

        boolean isCredits = thePanel.intGameState == 3;

        boolean isEndScreen = thePanel.intGameState == 5;

        boolean isDemo = thePanel.intGameState == 6;
        boolean playerIsDead = playerList.isEmpty() || !playerList.get(0).isAlive;

        thePanel.currentPlayers = this.playerList;
        thePanel.repaint();

        // 2. Home Screen
        lobbyButton.setVisible(isHome);
        helpButton.setVisible(isHome);
        creditsButton.setVisible(isHome);
        backButton.setVisible(isLobby || isHelp || isCredits);

        // 3. Lobby Setup: Choose Role
        chooseRoleLabel.setVisible(isLobbySetup);
        chooseHostButton.setVisible(isLobbySetup);
        chooseJoinButton.setVisible(isLobbySetup);

        // 4. Lobby Setup: Host Role
        chooseDifficultyLabel.setVisible(isLobbySetupHost);
        easyButton.setVisible(isLobbySetupHost);
        mediumButton.setVisible(isLobbySetupHost);
        hardButton.setVisible(isLobbySetupHost);

        // 5. Lobby Setup: Joiner Role
        enterIPAddressLabel.setVisible(isLobbySetupJoiner);
        enterIPAddress.setVisible(isLobbySetupJoiner);

        // 6. Lobby Setup: General GUI
        choosePlayerColourLabel.setVisible(isLobbySetupGeneral);
        redButton.setVisible(isLobbySetupGeneral);
        blueButton.setVisible(isLobbySetupGeneral);
        greenButton.setVisible(isLobbySetupGeneral);
        purpleButton.setVisible(isLobbySetupGeneral);

        confirmRoleButton.setVisible(isLobbySetup);
        setupErrorLabel.setVisible(isLobbySetup);

        // 7. Lobby: General GUI
        playersConnectedLabel.setVisible(isLobbyGeneral);
        scrollChatArea.setVisible(isLobbyGeneral);
        chatInput.setVisible(isLobbyGeneral);

        // 8. Lobby: Host GUI
        joiningIPInfo.setVisible(isLobbyHost);
        chooseMapLabel.setVisible(isLobbyHost);
        alpineTundraMapButton.setVisible(isLobbyHost);
        oasisDesertMapButton.setVisible(isLobbyHost);
        floatingIslandMapButton.setVisible(isLobbyHost);
        playButton.setVisible(isLobbyHost);

        // 9. DEMO Screen
        demoButton.setVisible(isHelp);
        demoBackButton.setVisible(isDemo);
        demoResetButton.setVisible(isDemo && playerIsDead);

        // 10. Game Over Screen
        gameOverLabel.setVisible(isEndScreen);
        winnerLabel.setVisible(isEndScreen);
        homeButton.setVisible(isEndScreen);
    }

    // === MAIN PROGRAM ===
    public static void main(String[] args) {
        new GameModel();
    }
}