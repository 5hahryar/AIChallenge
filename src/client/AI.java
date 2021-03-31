package client;


import client.bfs.AdjList;
import client.bfs.BfsHelper;
import client.bfs.Graph;
import client.bfs.UnweightedShortestPath;
import client.model.Answer;
import client.model.Cell;
import client.model.enums.AntType;
import client.model.enums.CellType;
import client.model.enums.Direction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

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

    public Answer turn(World world) {
        // Enter your AI code here
        AI.turn++;
        message = "";

        //get next direction for unit based on it's type
        Direction nextMoveDirection;
        if (world.getAnt().getType() == AntType.KARGAR) return myKargar.turn(world);
        else nextMoveDirection = nextMoveDirectionSarbaaz(world);


        return new Answer(nextMoveDirection, message, 10);
    }

    /**
     * @param world
     * @return next direction for sarbaaz to move
     */
    private Direction nextMoveDirectionSarbaaz(World world) {
        return Direction.CENTER;
    }

    private Direction getRandomDirection() {
        return switch (new Random().nextInt(4)) {
            case 0 -> Direction.UP;
            case 1 -> Direction.DOWN;
            case 2 -> Direction.RIGHT;
            case 3 -> Direction.LEFT;
            default -> Direction.CENTER;
        };
    }
}