import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.image.io.*;


public class GameView extend JPanel{ 
    // Properties

    // Methods
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        // Draw Images/Add images here

    }

    // Constructor
    public GameView(){
        super();
        try{
            // Load Images
        }catch(FileNotFoundException e){
            System.out.println("Error: File not found");
        }
    
    }
}
