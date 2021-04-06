package client.myClasses;

import client.World;
import client.bfs.AdjList;
import client.bfs.BfsHelper;
import client.bfs.MyNode;
import client.model.Answer;
import client.model.Cell;
import client.model.enums.CellType;
import client.model.enums.Direction;

import java.util.*;

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
    private ArrayList<MyNode> nodesWithResources = new ArrayList<>();
    private MyNode targetNode;
    private int turn;

    public Answer turn(World world, int turn) {
        //Initialize values
        this.turn = turn;
        positionX = world.getAnt().getXCoordinate();
        positionY = world.getAnt().getYCoordinate();
        positionGraphName = Utils.getNodeNameFromCell(world.getAnt().getLocationCell());
        baseGraphName = Utils.getNodeNameFromCoordinates(world.getBaseX(), world.getBaseY());
        message = "";
//        if (prevDirection == null) prevDirection = getRandomDirection();

        System.out.println("currently at: " + positionX + "," + positionY);

        Direction nextMoveDirection;
        nextMoveDirection = nextMoveDirectionKargar(world);

//        broadcastResources();
        broadcastMap();

//        System.out.println("nodeswithres:" + nodesWithResources.toString());
        System.out.println("");
        System.out.println("nodes with res:");
        for (MyNode node: nodesWithResources) {
            System.out.print(node.getGraphName() + "/");
        }
        if (targetNode != null) System.out.println("target:" + targetNode.getGraphName());
        System.out.println("pos name : " + Utils.getNodeNameFromCell(world.getAnt().getLocationCell()));

        prevDirection = nextMoveDirection;

        System.out.println("direction" + nextMoveDirection);
        System.out.println("message: " + message);
        return new Answer(nextMoveDirection, message, 10);
    }

    private void broadcastMap() {
        LinkedList<Integer> edges = graph.getEdges(Utils.getNodeNameFromCoordinates(positionX, positionY));
        if (edges != null && !edges.isEmpty()) {
            message += "*M:" + Utils.getNodeNameFromCoordinates(positionX, positionY) + ",";
            for (int edgeName : edges) {
                if (!message.contains(String.valueOf(edgeName))) message += edgeName + ",";
            }
            message += "/";
        }
    }

    private void broadcastResources() {
        if (nodesWithResources != null && !nodesWithResources.isEmpty()) {
            message += "*R:";
            for (MyNode node : nodesWithResources) {
                message += node.getGraphName() + ",";
            }
            message += "/";
        }
    }

    private void listenToMapMessage(World world) {
        ArrayList<Integer> data = Utils.parseMapMessage(world, turn);
        if (!data.isEmpty() && data.size() > 1) {
            for (int i=1;i<data.size();i++) {
                addEdgeToGraph(data.get(0), data.get(i));
            }
        }
    }

    /**
     * @param world
     * @return next direction for kargar to move
     */
    private Direction nextMoveDirectionKargar(World world) {
//        listenToResourceMessage(world);
        listenToMapMessage(world);
        mapViewDistance(world);
        nodesWithResources = Utils.sortMap(world, nodesWithResources);
        //return to base if ant holds resources ELSE get another direction
        if (world.getAnt().getCurrentResource().getValue() > 0) return getDirectionToNode(world, baseGraphName);
        else return getNextMoveDirection(world);
    }

    private void listenToResourceMessage(World world) {
        if (!world.getChatBox().getAllChatsOfTurn(turn-1).isEmpty()) {
            String lastChat = world.getChatBox().getAllChatsOfTurn(turn - 1).get(0).getText();
            if (!lastChat.isEmpty()) {
                int codeIndex = lastChat.indexOf("*R:");
                int nextIndex = lastChat.indexOf(',');
                String name = lastChat.substring(codeIndex + 5, nextIndex);
                int nodeName = Integer.parseInt(name);
                if (!nodesWithResourcesContains(nodeName)) {
                    nodesWithResources.add(new MyNode(nodeName, -1, -1));
                    System.out.println("res from chat:" + nodeName);
                }
            }
        }
    }



    /**
     * @param world
     * @return next direction in order to go to base
     */
    private Direction getDirectionToNode(World world, int nodeName) {
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
        if (up != null) upGraphName = Utils.getNodeNameFromCell(up);
        if (down != null) downGraphName = Utils.getNodeNameFromCell(down);
        if (right != null) rightGraphName = Utils.getNodeNameFromCell(right);
        if (left != null) leftGraphName = Utils.getNodeNameFromCell(left);

        //initialize BFS algorithm to find the shortest path
        BfsHelper bfs = new BfsHelper(graph);
        bfs.findShortestPath(positionGraphName, nodeName);

        //match the shortest path from BFS to correct direction
        if (bfs.getPathToDestination().size() > 0) {
            if (up != null && upGraphName == bfs.getPathToDestination().get(0)) return Direction.UP;
            if (down != null && downGraphName == bfs.getPathToDestination().get(0)) return Direction.DOWN;
            if (right != null && rightGraphName == bfs.getPathToDestination().get(0)) return Direction.RIGHT;
            if (left != null && leftGraphName == bfs.getPathToDestination().get(0)) return Direction.LEFT;
        }

        //return center if non is matched to BFS
        return Utils.getRandomDirection();
    }

    /**
     * @param world
     * @return next direction to move (the optimum one)
     */
    private Direction getNextMoveDirection(World world) {
        //if nodes with resources isn't empty go to first node in that list
        if (!nodesWithResources.isEmpty()) {
            //choose a target randomly, and null it if in base
            if (positionX ==world.getBaseX() && positionY == world.getBaseY()) {
                targetNode = null;
            }
            if (targetNode == null) targetNode = nodesWithResources.get(new Random().nextInt(nodesWithResources.size()));
            //null target if we are in it
            if (targetNode != null && positionX == targetNode.getX() && positionY == targetNode.getY()) targetNode = null;
            if (targetNode != null) return getDirectionToNode(world, targetNode.getGraphName());
        }
        ArrayList<MyDirection> availableDirections = getAvailableDirections(world);
        Direction optimumDirection = findOptimumDirection(availableDirections, world);
        return optimumDirection;
    }

    /**
     * scans the viewable neighbors and maps them into graph
     * @param world
     */
    private void mapViewDistance(World world) {
        ArrayList<Cell> neighborCells = new ArrayList<>();
        int viewDistance = world.getAnt().getViewDistance();

        //get neighbor cells in view distance
        for (int i=-viewDistance;i<=viewDistance;i++) {
            for (int j=-viewDistance;j<=world.getAnt().getViewDistance();j++) {
                Cell neighbor = world.getAnt().getNeighborCell(i, j);
                if (neighbor != null && neighbor.getType() != CellType.WALL) {
                    neighborCells.add(neighbor);
                    //add cell to nodes with resources
                    if (neighbor.getResource().getValue() > 0 && !nodesWithResourcesContains(Utils.getNodeNameFromCell(neighbor))) {
                        nodesWithResources.add(new MyNode(Utils.getNodeNameFromCell(neighbor), neighbor));
                    }
                    //remove node from nodesWithResources if it's resource value is below 1
                    else if (neighbor.getResource().getValue() <= 0 && nodesWithResourcesContains(Utils.getNodeNameFromCell(neighbor))) {
                        nodesWithResources.removeIf(node -> node.getGraphName() == Utils.getNodeNameFromCell(neighbor));
                    }
                }
            }
        }



        //get relatives of each neighbor and add their edges to graph
        for (Cell neighbor : neighborCells) {
            int upX = neighbor.getXCoordinate();
            int upY = neighbor.getYCoordinate() - 1;
            int doX = neighbor.getXCoordinate();
            int doY = neighbor.getYCoordinate() + 1;
            int riX = neighbor.getXCoordinate() + 1;
            int riY = neighbor.getYCoordinate();
            int leX = neighbor.getXCoordinate() -1;
            int leY = neighbor.getYCoordinate();

            //find the up,down,right.left neighbor cells and add edge from neighbor to them, into the graph
            for (Cell relative : neighborCells) {
                if (relative.getXCoordinate() == upX && relative.getYCoordinate() == upY){
                    if (relative.getType() != CellType.WALL) {
                        addEdgeToGraph(Utils.getNodeNameFromCell(neighbor), Utils.getNodeNameFromCell(relative));
                    }
                }
                else if (relative.getXCoordinate() == doX && relative.getYCoordinate() == doY){
                    if (relative.getType() != CellType.WALL) {
                        addEdgeToGraph(Utils.getNodeNameFromCell(neighbor), Utils.getNodeNameFromCell(relative));
                    }
                }
                else if (relative.getXCoordinate() == riX && relative.getYCoordinate() == riY){
                    if (relative.getType() != CellType.WALL) {
                        addEdgeToGraph(Utils.getNodeNameFromCell(neighbor), Utils.getNodeNameFromCell(relative));
                    }
                }
                else if (relative.getXCoordinate() == leX && relative.getYCoordinate() == leY){
                    if (relative.getType() != CellType.WALL) {
                        addEdgeToGraph(Utils.getNodeNameFromCell(neighbor), Utils.getNodeNameFromCell(relative));
                    }
                }
            }
        }
    }


    /**
     * @param availableDirections
     * @return next optimum direction to move
     */
    private Direction findOptimumDirection(ArrayList<MyDirection> availableDirections, World world) {
        //sort available directions based on their resource value MAX .... MIN
        availableDirections.sort(new Comparator<>() {
            @Override
            public int compare(MyDirection o1, MyDirection o2) {
                return Integer.compare(o2.getCell().getResource().getValue(), o1.getCell().getResource().getValue());
            }
        });

        //if there are no directions with resources, take the previous direction if available, if not pick one randomly
        if (availableDirections.get(0).getCell().getResource().getValue() > 0) {
            //add node to list of nodes with resources
            if (!nodesWithResourcesContains(Utils.getNodeNameFromCell(availableDirections.get(0).getCell()))) {
                nodesWithResources.add(new MyNode(Utils.getNodeNameFromCell(availableDirections.get(0).getCell()), availableDirections.get(0).getCell()));
            }
            return availableDirections.get(0).getDirection();
        }
        else {
            for (MyDirection direction : availableDirections) {
                if (direction.getDirection() == prevDirection) return direction.getDirection();
            }
            return availableDirections.get(new Random().nextInt(availableDirections.size())).getDirection();
        }
    }

    private boolean nodesWithResourcesContains(int nodeNameFromCell) {
        for (MyNode node : nodesWithResources) {
            if (node.getGraphName() == nodeNameFromCell) return true;
        }
        return false;
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
        if (up != null && up.getType() != CellType.WALL && Utils.isCellInMovingBounds(up, world)) {
            availableDirections.add(new MyDirection(Direction.UP, up));
            addEdgeToGraph(Utils.getNodeNameFromCell(up), positionGraphName);
        }
        if (down != null && down.getType() != CellType.WALL && Utils.isCellInMovingBounds(down, world)) {
            availableDirections.add(new MyDirection(Direction.DOWN, down));
            addEdgeToGraph(Utils.getNodeNameFromCell(down), positionGraphName);
        }
        if (right != null && right.getType() != CellType.WALL && Utils.isCellInMovingBounds(right, world)) {
            availableDirections.add(new MyDirection(Direction.RIGHT, right));
            addEdgeToGraph(Utils.getNodeNameFromCell(right), positionGraphName);
        }
        if (left != null && left.getType() != CellType.WALL && Utils.isCellInMovingBounds(left, world)) {
            availableDirections.add(new MyDirection(Direction.LEFT, left));
            addEdgeToGraph(Utils.getNodeNameFromCell(left), positionGraphName);
        }

        return availableDirections;
    }

    /**
     * @param src, graph name of source
     * @param dest graph name of destination
     * adds edge from src to dest
     */
    private void addEdgeToGraph(int src, int dest) {
            graph.addEdge(src, dest);
//            System.out.println("edge added : " + src + ">" +dest);
    }
}
