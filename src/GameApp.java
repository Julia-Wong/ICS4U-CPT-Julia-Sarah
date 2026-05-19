import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;

public class GameApp{
    // Properties
    // Set Frame & Panels
    JFrame theFrame = new JFrame("Spleef");
    JPanel thePanel = new JPanel();

    // Methods
    

    // Constructor
    public GameApp(){
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
        new GameApp();
    }

    // JULIA CAN U SEE THIS
}