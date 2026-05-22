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

    BufferedImage imgGrassFull = null;
    BufferedImage imgGrassSlightlyCracked = null;
    BufferedImage imgGrassCracked = null;

    BufferedImage imgDirtFull = null;
    BufferedImage imgDirtSlightlyCracked = null;
    BufferedImage imgDirtCracked = null;

    BufferedImage imgStoneFull = null;
    BufferedImage imgStoneSlightlyCracked = null;
    BufferedImage imgStoneCracked = null;

    BufferedImage imgSandFull = null;
    BufferedImage imgSandSlightlyCracked = null;
    BufferedImage imgSandCracked = null;

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

                    if (map[r][c] == 0) { // draw snow
                        if (tileHealth[r][c] == 0 && imgSnowFull != null) {
                            g.drawImage(imgSnowFull, c * 80, r * 80, null);
                        } else if (tileHealth[r][c] == 1 && imgSnowSlightlyCracked != null) {
                            g.drawImage(imgSnowSlightlyCracked, c * 80, r * 80, null);
                        } else if (tileHealth[r][c] == 2 && imgSnowCracked != null) {
                            g.drawImage(imgSnowCracked, c * 80, r * 80, null);
                        } 
                    } else if (map[r][c] == 1) { // draw grass
                        if (tileHealth[r][c] == 0 && imgGrassFull != null) {
                            g.drawImage(imgGrassFull, c * 80, r * 80, null);
                        } else if (tileHealth[r][c] == 1 && imgGrassSlightlyCracked != null) {
                            g.drawImage(imgGrassSlightlyCracked, c * 80, r * 80, null);
                        } else if (tileHealth[r][c] == 2 && imgGrassCracked != null) {
                            g.drawImage(imgGrassCracked, c * 80, r * 80, null);
                        } 
                    } else if (map[r][r] == 2) { // draw dirt
                        if (tileHealth[r][c] == 0 && imgDirtFull != null) {
                            g.drawImage(imgDirtFull, c * 80, r * 80, null);
                        } else if (tileHealth[r][c] == 1 && imgDirtSlightlyCracked != null) {
                            g.drawImage(imgDirtSlightlyCracked, c * 80, r * 80, null);
                        } else if (tileHealth[r][c] == 2 && imgDirtCracked != null) {
                            g.drawImage(imgDirtCracked, c * 80, r * 80, null);
                        } 
                    } else if (map[r][c] == 3) { // draw stone
                        if (tileHealth[r][c] == 0 && imgStoneFull != null) {
                            g.drawImage(imgStoneFull, c * 80, r * 80, null);
                        } else if (tileHealth[r][c] == 1 && imgStoneSlightlyCracked != null) {
                            g.drawImage(imgStoneSlightlyCracked, c * 80, r * 80, null);
                        } else if (tileHealth[r][c] == 2 && imgStoneCracked != null) {
                            g.drawImage(imgStoneCracked, c * 80, r * 80, null);
                        } 
                    } else if (map[r][c] == 4) { // draw sand
                        if (tileHealth[r][c] == 0 && imgSandFull != null) {
                            g.drawImage(imgSandFull, c * 80, r * 80, null);
                        } else if (tileHealth[r][c] == 1 && imgSandSlightlyCracked != null) {
                            g.drawImage(imgSandSlightlyCracked, c * 80, r * 80, null);
                        } else if (tileHealth[r][c] == 2 && imgSandCracked != null) {
                            g.drawImage(imgSandCracked, c * 80, r * 80, null);
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
        imgSnowSlightlyCracked = loadImage(".media/slightlyCrackedSnowTile.png");
        imgSnowCracked = loadImage(".media/crackedSnowTile.png");

        imgGrassFull = loadImage(".media/fullGrassTile.png");
        imgGrassSlightlyCracked = loadImage(".media/slightlyCrackedGrassTile.png");
        imgGrassCracked = loadImage(".media/crackedGrassTile.png");

        imgDirtFull = loadImage(".media/fullDirtTile.png");
        imgDirtSlightlyCracked = loadImage(".media/slightlyCrackedDirtTile.png");
        imgDirtCracked = loadImage(".media/crackedDirtTile.png");

        imgStoneFull = loadImage(".media/fullStoneTile.png");
        imgStoneSlightlyCracked = loadImage(".media/slightlyCrackedStoneTile.png");
        imgStoneCracked = loadImage(".media/crackedStoneTile.png");

        imgSandFull = loadImage(".media/fullSandTile.png");
        imgSandSlightlyCracked = loadImage(".media/slightlyCrackedSandTile.png");
        imgSandCracked = loadImage(".media/crackedSandTile.png");

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
