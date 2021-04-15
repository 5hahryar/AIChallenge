package client.myClasses;

import client.World;
import client.model.Answer;
import client.model.enums.Direction;

public class MySarbaaz {

    private int turn;
    private static ExploreAgent exploreAgent;

    public Answer turn(World world, int turn) {
        //Initialize values
        this.turn = turn;
        if (exploreAgent == null) {
            exploreAgent = new ExploreAgent(world);
        }

        return new Answer(nextMoveDirectionSarbaaz(world), "", 0);
    }

    /**
     * @param world
     * @return next direction for sarbaaz to move
     */
    private Direction nextMoveDirectionSarbaaz(World world) {
        return exploreAgent.turn(world).getDirection();
    }
}