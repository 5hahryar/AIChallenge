package client;

import client.bfs.AdjList;
import client.bfs.BfsHelper;
import client.model.Answer;
import client.model.Cell;
import client.model.enums.CellType;
import client.model.enums.Direction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

public class MyKargar {

    public MyKargar() {
    }

    private static String message = "";
    private static Direction prevDirection = Direction.UP;

    private static final AdjList graph = new AdjList(10000, false);

    private int positionX;
    private int positionY;
    private int positionGraphName;

    private int baseGraphName;

    public Answer turn(World world) {
        //Initialize values
        positionX = world.getAnt().getXCoordinate();
        positionY = world.getAnt().getYCoordinate();
        positionGraphName = Integer.parseInt(String.valueOf(positionX) + positionY);
        baseGraphName = Integer.parseInt(String.valueOf(world.getBaseX()) + world.getBaseY());

        Direction nextMoveDirection;
        nextMoveDirection = nextMoveDirectionKargar(world);

        message += "nD:" + nextMoveDirection + "/pD:" + prevDirection;

        prevDirection = nextMoveDirection;

        return new Answer(nextMoveDirection, message, 10);
    }

    /**
     * @param world
     * @return next direction for kargar to move
     */
    private Direction nextMoveDirectionKargar(World world) {
        //return to base if ant holds resources ELSE get another direction
        if (world.getAnt().getCurrentResource().getValue() > 0) return getDirectionToHome(world);
        else return getNextMoveDirection(world);
    }

    /**
     * @param world
     * @return next direction in order to go to base
     */
    private Direction getDirectionToHome(World world) {
        //get cell info of 4 main directions
        Cell up = world.getAnt().getNeighborCell(0, -1);
        Cell down = world.getAnt().getNeighborCell(0, 1);
        Cell right = world.getAnt().getNeighborCell(1, 0);
        Cell left = world.getAnt().getNeighborCell(-1, 0);

        //special node(graph) name for each direction
        int upGraphName = -1;
        int downGraphName = -1;
        int rightGraphName = -1;
        int leftGraphName = -1;

        //populate node names in format: XXYY
        if (up != null) upGraphName = Integer.parseInt(String.valueOf(up.getXCoordinate()) + String.valueOf(up.getYCoordinate()));
        if (down != null) downGraphName = Integer.parseInt(String.valueOf(down.getXCoordinate()) + String.valueOf(down.getYCoordinate()));
        if (right != null) rightGraphName = Integer.parseInt(String.valueOf(right.getXCoordinate()) + String.valueOf(right.getYCoordinate()));
        if (left != null) leftGraphName = Integer.parseInt(String.valueOf(left.getXCoordinate()) + String.valueOf(left.getYCoordinate()));

        //initialize BFS algorithm to find the shortest path
        BfsHelper bfs = new BfsHelper(graph);
        bfs.findShortestPath(positionGraphName, baseGraphName);

        //match the shortest path from BFS to correct direction
        if (up != null && upGraphName == bfs.getPathToDestination().get(0)) return Direction.UP;
        if (down != null && downGraphName == bfs.getPathToDestination().get(0)) return Direction.DOWN;
        if (right != null && rightGraphName == bfs.getPathToDestination().get(0)) return Direction.RIGHT;
        if (left != null && leftGraphName == bfs.getPathToDestination().get(0)) return Direction.LEFT;

        //return center if non is matched to BFS
        return Direction.CENTER;
    }

    /**
     * @param world
     * @return next direction to move (the optimum one)
     */
    private Direction getNextMoveDirection(World world) {
        ArrayList<MyDirection> availableDirections = getAvailableDirections(world);
        Direction optimumDirection = findOptimumDirection(availableDirections);
        return optimumDirection;
    }

    /**
     * @param availableDirections
     * @return next optimum direction to move
     */
    private Direction findOptimumDirection(ArrayList<MyDirection> availableDirections) {
        //sort available directions based on their resource value MAX .... MIN
        availableDirections.sort(new Comparator<>() {
            @Override
            public int compare(MyDirection o1, MyDirection o2) {
                return Integer.compare(o2.getCell().getResource().getValue(), o1.getCell().getResource().getValue());
            }
        });

        //if there are no directions with resources, take the previous direction if available, if not pick one randomly
        if (availableDirections.get(0).getCell().getResource().getValue() > 0) return availableDirections.get(0).getDirection();
        else {
            for (MyDirection direction : availableDirections) {
                if (direction.getDirection() == prevDirection) return direction.getDirection();
            }
            return availableDirections.get(new Random().nextInt(availableDirections.size())).getDirection();
        }
    }

    /**
     * @param world
     * @return available directions to move as MyDirection object
     */
    private ArrayList<MyDirection> getAvailableDirections(World world) {
        ArrayList<MyDirection> availableDirections = new ArrayList<>();

        //get cell info of 4 main directions
        Cell up = world.getAnt().getNeighborCell(0, -1);
        Cell down = world.getAnt().getNeighborCell(0, 1);
        Cell right = world.getAnt().getNeighborCell(1, 0);
        Cell left = world.getAnt().getNeighborCell(-1, 0);

        //if direction is not wall and in reach(not to the other side of map)
        //then add to list of available directions and the graph
        if (up != null && up.getType() != CellType.WALL && isCellInMovingBounds(up, world)) {
            availableDirections.add(new MyDirection(Direction.UP, up));
            String upGraphName = String.valueOf(up.getXCoordinate()) + String.valueOf(up.getYCoordinate());
            addEdgeToGraph(Integer.parseInt(upGraphName), positionGraphName);
        }
        if (down != null && down.getType() != CellType.WALL && isCellInMovingBounds(down, world)) {
            availableDirections.add(new MyDirection(Direction.DOWN, down));
            String downGraphName = String.valueOf(down.getXCoordinate()) + String.valueOf(down.getYCoordinate());
            addEdgeToGraph(Integer.parseInt(downGraphName), positionGraphName);
        }
        if (right != null && right.getType() != CellType.WALL && isCellInMovingBounds(right, world)) {
            availableDirections.add(new MyDirection(Direction.RIGHT, right));
            String rightGraphName = String.valueOf(right.getXCoordinate()) + String.valueOf(right.getYCoordinate());
            addEdgeToGraph(Integer.parseInt(rightGraphName), positionGraphName);
        }
        if (left != null && left.getType() != CellType.WALL && isCellInMovingBounds(left, world)) {
            availableDirections.add(new MyDirection(Direction.LEFT, left));
            String leftGraphName = String.valueOf(left.getXCoordinate()) + String.valueOf(left.getYCoordinate());
            addEdgeToGraph(Integer.parseInt(leftGraphName), positionGraphName);
        }

        //add position name to graph history
        if (!graph.contains(positionGraphName)) graph.addNodeToHistory(positionGraphName, positionX, positionY);

        return availableDirections;
    }

    /**
     * @param src, graph name of source
     * @param dest graph name of destination
     * adds edge from src to dest
     */
    private void addEdgeToGraph(int src, int dest) {
        if (!graph.contains(src)) {
            graph.addEdge(src, dest);
        }
    }

    /**
     *
     * @param cell
     * @param world
     * @return if cell is in reach or not (currently used to check if cell if in the other side of map or not)
     */
    private boolean isCellInMovingBounds(Cell cell, World world) {
        return Math.abs(cell.getXCoordinate() - world.getAnt().getXCoordinate()) +
                Math.abs(cell.getYCoordinate() - world.getAnt().getYCoordinate()) <= 1;
    }
}
