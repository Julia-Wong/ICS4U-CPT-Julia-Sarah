public class Player {
    // Properties
    int intPlayerID;
    int intX;
    int intY;
    String strColour;
    boolean isAlive;

    // Methods

    // Constructor
    public Player(int intPlayerID, int intX, int intY, String strColour) {
        this.intPlayerID = intPlayerID;
        this.intX = intX;
        this.intY = intY;
        this.strColour = strColour;
        this.isAlive = true;
    }
}
