import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.HashMap;
import javax.swing.event.*;

public class GameModel {
    // Properties
    // Set Frame & Panels
    JFrame theFrame = new JFrame("Spleef");
    JPanel thePanel = new JPanel();

    int[][] map = new int[16][9];
    // public HashMap<String, Player> activePlayers;

    // Methods
    public void loadMap(String csvFile) {

    }

    public void movePlayer(String id, int newX, int newY) {

    }

    public void damageTile(int row, int col) {

    }

    public void checkFall() {

    }

    // Constructor
    public GameModel(){
        // Set Panel
        thePanel.setLayout(null);
        thePanel.setPreferredSize(new Dimension(1280, 720));

        // Set Frame
        theFrame.setContentPane(thePanel);
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        theFrame.pack();
        theFrame.setVisible(true);
    }

    // Main Program
    public static void main(String[] args){
        new GameModel();
    }

}