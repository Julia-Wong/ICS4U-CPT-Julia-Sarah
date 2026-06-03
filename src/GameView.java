import java.awt.*;
import javax.swing.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import java.util.ArrayList;

public class GameView extends JPanel { 
    // === PROPERTIES ===

    // 1. Load Tile Graphics
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

    // 2. Load Player Graphics
    BufferedImage imgPlayerRed = null;
    BufferedImage imgPlayerBlue = null;
    BufferedImage imgPlayerGreen = null;
    BufferedImage imgPlayerPurple = null;

    // 3. Load Full-Screen Overlays
    BufferedImage imgInstructionPage = null;
    BufferedImage imgCreditsPage = null;
    BufferedImage imgHomeScreen = null;

    // 4. Game Data
    int intGameState = 0; // 0=home, 1=lobby, 2=help, 3=credits, 4=play, 5=gameover, 6=demo
    int[][] map = new int[9][16]; // 0=snow, 1=grass, 2=dirt, 3=stone, 4=sand
    int[][] tileHealth = new int[9][16]; // 0=full, 1=slightly cracked, 2=cracked, 3=air(void)
    ArrayList<Player> currentPlayers = new ArrayList<Player>();
    boolean choosingNetworkRole = false;
    BufferedImage[][] tileImages = new BufferedImage[5][3];

    // === METHODS ===
    /**
     * Automatically triggered on repaint requests to paint backgrounds,
     * graphics, maps, and players.
     * @param g The Graphics painting tool
     */
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        // 1. Home
        if (intGameState == 0) {
            if (imgHomeScreen != null) {
                g.drawImage(imgHomeScreen, 0, 0, null);
            }
        } 
        
        // 2. Lobby
        else if (intGameState == 1) {
            if (choosingNetworkRole == false && currentPlayers != null) {
                // Display all current players in the server
                for (int i = 0; i < currentPlayers.size(); i++) {
                    Player p = currentPlayers.get(i);
                    BufferedImage spriteToDraw = getPlayerSprite(p.strColour);
                    g.drawImage(spriteToDraw, i * 100 + 300, 300, null);
                }
            }
        } 
        
        // 3. Background Graphic Layouts
        else if (intGameState == 2) { // Help page
            g.drawImage(imgInstructionPage, 0, 0,null);
        } else if (intGameState == 3) { // Credits page
            g.drawImage(imgCreditsPage, 0, 0,null);
        } 
        
        // 4. Gameplay/DEMO
        else if (intGameState == 4 || intGameState == 6) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, 1280, 720);

            // Draw map
            for (int r = 0; r < map.length; r++) {
                for (int c = 0; c < map[r].length; c++) {
                    int type = map[r][c];
                    int health = tileHealth[r][c];

                    if (health >= 0 && health < 3 && type >= 0 && type < 5) {
                        BufferedImage currentImg = tileImages[type][health];

                        if (currentImg != null) {
                            g.drawImage(currentImg, c * 80, r * 80, null);
                        }
                    }
                }
            }
        
            // Draw players
            for (Player p: currentPlayers) {
                if (p.isAlive) {
                    BufferedImage spriteToDraw = getPlayerSprite(p.strColour);
                    // draw players
                    if (spriteToDraw != null) {
                        g.drawImage(spriteToDraw, p.intX, p.intY, null);
                    }
                }
            }

        }
    }

    /**
     * Assigns a player's sprite based on their player's string colour.
     * @param colour The string identifier of the player's colour.
     * @return The corresponding BufferedImage, or null if the colour is invalid.
     */
    public BufferedImage getPlayerSprite(String colour) {
        if (colour == null) {
            return null;
        } else if (colour.equals("Red")) {
            return imgPlayerRed;
        } else if (colour.equals("Blue")) {
            return imgPlayerBlue;
        } else if (colour.equals("Green")) {
            return imgPlayerGreen;
        } else if (colour.equals("Purple")) {
            return imgPlayerPurple;
        }
        return null;
    }

    /**
     * Locates and reads image files.
     * @param file The relative path of the target image.
     * @return The constructed BufferedImage, or null if the file is missing/corrupt.
     */
    public BufferedImage loadImage(String file) {
        try {
            BufferedImage tempImage = ImageIO.read(new File(file));
            return tempImage;
        } catch (FileNotFoundException e){
            System.out.println("Error: File not found: " + file);
        } catch (IOException e) {
            System.out.println("Error: Error accessing file: " + file);
        }
        return null;
    }

    // === CONSTRUCTOR ===

    /**
     * Initializes the GameView panel and pre-loads all graphics such as
     * tiles, players, and background overlays.
     */
    public GameView(){
        super();
        
        // 1. Load Map Tiles
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

        // 2. Load Player Images
        imgPlayerRed = loadImage(".media/playerRed.png");
        imgPlayerBlue = loadImage(".media/playerBlue.png");
        imgPlayerGreen = loadImage(".media/playerGreen.png");
        imgPlayerPurple = loadImage(".media/playerPurple.png");

        // 3. Load Full-Screen UI Overlays
        imgInstructionPage = loadImage(".media/instructionsPage.png");
        imgCreditsPage = loadImage(".media/creditsPage.png");
        imgHomeScreen = loadImage(".media/homescreenSpleefBg.png");

        // 4. Assign Tile Images to 2D Array
        tileImages = new BufferedImage[][] {
            {imgSnowFull, imgSnowSlightlyCracked, imgSnowCracked},
            {imgGrassFull, imgGrassSlightlyCracked, imgGrassCracked},
            {imgDirtFull, imgDirtSlightlyCracked, imgDirtCracked},
            {imgStoneFull, imgStoneSlightlyCracked, imgStoneCracked},
            {imgSandFull, imgSandSlightlyCracked, imgSandCracked}
        };
    }  
}
