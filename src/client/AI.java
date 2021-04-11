package client;


import client.model.Answer;
import client.model.enums.AntType;
import client.model.enums.Direction;
import client.myClasses.MyKargar;
import client.myClasses.MySarbaaz;
import client.myClasses.Utils;

/**
 * You must put your code in this class {@link AI}.
 * This class has {@link #turn}, to do orders while game is running;
 */

public class AI {
    /**
     * this method is for participants' code
     *
     * @param world is your data for the game (read the documentation on {@link client.World})
     * the return value is a {@link client.model.Answer} which consists of Direction for your
     * next destination in map (the necessary parameter), the Message (not necessary) for your
     * chat message and the value (if there is any message) for your message value.
     */
    static int turn = 0;
    private static String message = "";

    private MyKargar myKargar = new MyKargar();
    private MySarbaaz mySarbaaz = new MySarbaaz();
    private static boolean isLogFileCreated = false;

    public Answer turn(World world) {
        if (!isLogFileCreated) {
            Utils.createLog();
            isLogFileCreated = true;
        }
        // Enter your AI code here
        AI.turn++;
        message = "";

        //get next direction for unit based on it's type
        Direction nextMoveDirection;
        if (world.getAnt().getType() == AntType.KARGAR) {
            System.out.println("turn:" + turn);
            System.out.println("*******");
            return myKargar.turn(world, turn);
        }
        else return mySarbaaz.turn(world, turn);
    }
}