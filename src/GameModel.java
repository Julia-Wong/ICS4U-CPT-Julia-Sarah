import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
// import java.util.HashMap;
// import javax.swing.event.*;

public class GameModel implements ActionListener {
    // Properties
    JFrame theFrame = new JFrame("Spleef");
    GameView thePanel = new GameView();
    JLabel titleSpleef = new JLabel("Spleef");
    JButton lobbyButton = new JButton("Lobby");
    JButton helpButton = new JButton("Help");
    JButton creditsButton = new JButton("Credits");

    JButton lobbyBackButton = new JButton("Back");
    JButton helpBackButton = new JButton("Back");
    JButton creditsBackButton = new JButton("Back");

    int[][] map = new int[16][9];
    

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

        // Add Back Buttons
        lobbyBackButton.setBounds(10, 10, 50, 50);
        lobbyBackButton.addActionListener(this);
        thePanel.add(lobbyBackButton);

        helpBackButton.setBounds(10, 10, 50, 50);
        helpBackButton.addActionListener(this);
        thePanel.add(helpBackButton);

        creditsBackButton.setBounds(10, 10, 50, 50);
        creditsBackButton.addActionListener(this);
        thePanel.add(creditsBackButton);

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

        if (isHome) {
            titleSpleef.setVisible(isHome);
            lobbyButton.setVisible(isHome);
            helpButton.setVisible(isHome);
            creditsButton.setVisible(isHome);
        }

        if (isLobby) {
            lobbyBackButton.setVisible(isLobby);
        }

        if (isHelp) {
            helpBackButton.setVisible(isHelp);
        }

        if (isCredits) {
            creditsBackButton.setVisible(isCredits);
        }
    }

    // Main Program
    public static void main(String[] args) {
        new GameModel();
    }

}