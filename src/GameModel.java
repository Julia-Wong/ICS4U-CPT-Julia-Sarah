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
    JButton backButton = new JButton("Back");

    // Lobby Buttons
    JButton alpineTundraMapButton = new JButton("Alpine Tundra Map"); // snow stone dirt
    JButton oasisDesertMapButton = new JButton("Oasis Desert Map"); // sand stone dirt
    JButton floatingIslandMapButton = new JButton("Floating Island Map"); // grass dirt sand

    int[][] map = new int[9][16]; // 0=snow, 1=grass, 2=dirt, 3=stone, 4=sand
    String[] mapFiles = {"alpineTundraMap.csv", "oasisDesertMap.csv", "floatingIslandMap.csv"};
    

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
                loadMap(mapFiles[0]);
            } else if (evt.getSource() == oasisDesertMapButton) {
                loadMap(mapFiles[1]);
            } else if (evt.getSource() == floatingIslandMapButton) {
                loadMap(mapFiles[2]);
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

            for (int r = 0; r < map.length; r++) {
                mapLine = mapFile.readLine();
                mapSplit = mapLine.split(",");
                for (int c = 0; c < map[r].length; c++) {
                    map[r][c] = Integer.parseInt(mapSplit[c]);
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
        titleSpleef.setBounds(600, 100, 400, 100);
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
        alpineTundraMapButton.setBounds(100, 300, 300, 100);
        alpineTundraMapButton.addActionListener(this);
        thePanel.add(alpineTundraMapButton);

        oasisDesertMapButton.setBounds(400, 300, 300, 100);
        oasisDesertMapButton.addActionListener(this);
        thePanel.add(oasisDesertMapButton);

        floatingIslandMapButton.setBounds(700, 300, 300, 100);
        floatingIslandMapButton.addActionListener(this);
        thePanel.add(floatingIslandMapButton);

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
        titleSpleef.setVisible(isHome);
        lobbyButton.setVisible(isHome);
        helpButton.setVisible(isHome);
        creditsButton.setVisible(isHome);

        // if on lobby screen:
        backButton.setVisible(isLobby);
        alpineTundraMapButton.setVisible(isLobby);
        oasisDesertMapButton.setVisible(isLobby);
        floatingIslandMapButton.setVisible(isLobby);

        // if on help screen:
        backButton.setVisible(isHelp);

        // if on credits screen:
        backButton.setVisible(isCredits);
    }

    // Main Program
    public static void main(String[] args) {
        new GameModel();
    }

}