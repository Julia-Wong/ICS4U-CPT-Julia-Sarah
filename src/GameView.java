import java.awt.*;
import javax.swing.*;
import java.awt.image.*;
// import java.awt.event.*;
import java.io.*;
// import javax.swing.event.*;
import javax.imageio.*;


public class GameView extends JPanel { 
    // Properties
    int intGameState = 0; // 0=home, 1=lobby, 2=help, 3=credits, 4=play, 5=gameover
    int[][] map = new int[9][16]; // 0=snow, 1=grass, 2=dirt, 3=stone, 4=sand
    int[][] tileHealth = new int[9][16]; // 0=full, 1=slightly cracked, 2=cracked, 3=air

    BufferedImage imgSnowFull = null;
    BufferedImage imgSnowSlightlyCracked = null;
    BufferedImage imgSnowCracked = null;

    boolean choosingNetworkRole = false;

    // Methods
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        if (intGameState == 0) {
            
        } else if (intGameState == 1) {
            if (choosingNetworkRole) {
                g.setColor(new Color(191, 236, 255));
                g.fillRect(250, 200, 680, 520);
            }
        } else if (intGameState == 2) {

        } else if (intGameState == 3) {

        } else if (intGameState == 4) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, 1280, 720);

            for (int r = 0; r < map.length; r++) {
                for (int c = 0; c < map[r].length; c++) {

                    if (map[r][c] == 0) {
                        if (tileHealth[r][c] == 0 && imgSnowFull != null) {
                            g.drawImage(imgSnowFull, c * 80, r * 80, null);
                        } else if (tileHealth[r][c] == 1 && imgSnowSlightlyCracked != null) {
                            g.drawImage(imgSnowSlightlyCracked, c * 80, r * 80, null);
                        } else if (tileHealth[r][c] == 2 && imgSnowCracked != null) {
                            g.drawImage(imgSnowCracked, c * 80, r * 80, null);
                        } 
                        
                    }

                }
            }
        } else if (intGameState == 5) {

        }

        // Draw Images/Add images here

    }

    // Constructor
    public GameView(){
        super();

        imgSnowFull = loadImage(".media/fullSnowTile.png");
        imgSnowSlightlyCracked = loadImage(".media/slightCrackedSnowTile.png");
        imgSnowCracked = loadImage(".media/crackedSnowTile.png");

    }  
    
    public BufferedImage loadImage(String file) {
        try {
            BufferedImage tempImage = ImageIO.read(new File(file));
            return tempImage;
        } catch (FileNotFoundException e){
            System.out.println("Error: File not found" + file);
        } catch (IOException e) {
            System.out.println("Error: Error accessing file" + file);
        }
        return null;

    }
}
