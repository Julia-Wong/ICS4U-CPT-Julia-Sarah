import java.awt.*;
import javax.swing.*;
// import java.awt.event.*;
// import java.io.*;
// import javax.swing.event.*;
// import javax.image.io.*;


public class GameView extends JPanel { 
    // Properties
    int intGameState = 0; // 0=home, 1=lobby, 2=help, 3=credits

    // Methods
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        if (intGameState == 0) {
            
        } else if (intGameState == 1) {

        } else if (intGameState == 2) {

        } else if (intGameState == 3) {

        }

        // Draw Images/Add images here

    }

    // Constructor
    public GameView(){
        super();
        // try {
        //     // Load Images
        // } catch(FileNotFoundException e){
        //     System.out.println("Error: File not found");
        // }
    }    
}
