public class Player {
    // Properties
    int intPlayerID;
    int intX;
    int intY;
    String strColour;
    boolean isAlive;
    boolean upPressed;
    boolean downPressed;
    boolean leftPressed;
    boolean rightPressed;

    // Methods

    // Constructor
    public Player(int intPlayerID, int intX, int intY, String strColour) {
        this.intPlayerID = intPlayerID;
        this.intX = intX;
        this.intY = intY;
        this.strColour = strColour;
        this.isAlive = true;
        this.upPressed = false;
        this.downPressed = false;
        this.leftPressed = false;
        this.rightPressed = false;
    }
}
