package client.myClasses;

import client.World;
import client.bfs.AdjList;
import client.bfs.BfsHelper;
import client.bfs.MyNode;
import client.dijkstra.Dijkstra;
import client.model.Answer;
import client.model.Ant;
import client.model.Cell;
import client.model.enums.AntTeam;
import client.model.enums.AntType;
import client.model.enums.CellType;
import client.model.enums.Direction;
import jdk.jshell.execution.Util;

import java.util.*;

public class MyKargar {

    private static final int MESSAGE_VALUE_RESOURCE = 5;
    private static final int MESSAGE_VALUE_MAP = 4;
    private static final int MESSAGE_VALUE_MAPRES = 8;
    private static final int MESSAGE_VALUE_BASE = 10;
    private static Direction prevDirection = Direction.UP;
    private static ArrayList<MyMessage> messages = new ArrayList<>();
    private static final AdjList graph = new AdjList(5000, false);
    private static final Dijkstra dijkstra = new Dijkstra();

    private int positionX;
    private int positionY;
    private int positionGraphName;
    private int baseX;
    private int baseY;
    private int baseGraphName;
    private int turn;
    private ArrayList<MyNode> nodesWithResources = new ArrayList<>();
    private MyNode targetNode;
    private ExploreAgent exploreAgent;
    private boolean isNewBorn = true;
    private int enemyBaseGraphName = -1;
    private boolean isEnemySarbaazInSight;
    private boolean isSwampInSight;

    public MyKargar() { }

    public Answer turn(World world, int turn) {
        //Initialize values
        this.turn = turn;
        initValues(world);

        listenToResourceMessage(world);
        listenToMapMessage(world);

        mapViewDistance(world);
        Direction nextMoveDirection = nextMoveDirectionKargar(world);

        broadcastResources();
        broadcastMap();

        MyMessage message = getMessage();

//        System.out.println("turn: " + turn);
//        System.out.println("currently at: " + positionX + "," + positionY + " name: " + positionGraphName);
//        if (targetNode != null) {
//            System.out.println("target: " + targetNode.getX() + "," + targetNode.getY() + " name: " + targetNode.getGraphName());
//        }
//        if (!nodesWithResources.isEmpty() && Utils.getNodeNameFromCoordinates(positionX, positionY) == baseGraphName) {
//            System.out.println("nodes with res: ");
//            for (MyNode node : nodesWithResources) {
//                int[] c = Utils.getCoordinatesFromName(node.getGraphName());
//                System.out.print(c[0] + "," + c[1] + "/");
//            }
//            System.out.println("");
//        }
//        System.out.println("Direction: " + nextMoveDirection);
//        System.out.println("**************** \n");
//
//        if (nextMoveDirection == Direction.CENTER) {
//            addMessage(new MyMessage("center" , 11));
//        }

        isNewBorn = false;
        return new Answer(nextMoveDirection, message.getMessage(), message.getValue());
    }

    private void initValues(World world) {
        positionX = world.getAnt().getXCoordinate();
        positionY = world.getAnt().getYCoordinate();
        baseX = world.getBaseX();
        baseY = world.getBaseY();
        positionGraphName = Utils.getNodeNameFromCell(world.getAnt().getLocationCell());
        baseGraphName = Utils.getNodeNameFromCoordinates(world.getBaseX(), world.getBaseY());
        isEnemySarbaazInSight = false;
        isSwampInSight = false;
        messages = new ArrayList<>();
        if (exploreAgent == null) {
            exploreAgent = new ExploreAgent(world, AntType.KARGAR);
        }
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
                if (neighbor != null && neighbor.getType() != CellType.WALL && neighbor.getType() != CellType.TRAP) {
                    neighborCells.add(neighbor);
                    //is enemy in cell
                    if (!neighbor.getAnts().isEmpty()){
                        for (Ant ant : neighbor.getAnts()) {
                            if (ant.getType() == AntType.SARBAAZ && ant.getTeam() == AntTeam.ENEMY) {
                                isEnemySarbaazInSight = true;
                                break;
                            }
                        }
                    }
                    //add cell to nodes with resources
                    // set value to enemy base graph name
                    if (neighbor.getType() == CellType.BASE && neighbor.getXCoordinate() != baseX) {
                        enemyBaseGraphName = Utils.getNodeNameFromCell(neighbor);
                    }
                    if (neighbor.getType() == CellType.SWAMP) isSwampInSight = true;
                    // add cell to nodes with resources
                    if (neighbor.getResource().getValue() > 0 && !nodesWithResourcesContains(Utils.getNodeNameFromCell(neighbor))) {
                        nodesWithResources.add(new MyNode(Utils.getNodeNameFromCell(neighbor), neighbor));
                    }
                    // add enemy base message
                    if (neighbor.getType() == CellType.BASE && neighbor.getXCoordinate() != baseX) {
                        addMessage(new MyMessage("*B:" + Utils.getNodeNameFromCell(neighbor) + "b", MESSAGE_VALUE_BASE));
                    }
                    // remove node from nodesWithResources if it's resource value is below 1
                    else if (neighbor.getResource().getValue() <= 0 && nodesWithResourcesContains(Utils.getNodeNameFromCell(neighbor))) {
                        if (targetNode != null && targetNode.getGraphName() == Utils.getNodeNameFromCell(neighbor)) {
                            targetNode = null;
                        }
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

            //TODO: for sarbaz do this also
            if (upY < 0) upY = world.getMapHeight() + upY;
            if (leX < 0) leX = world.getMapWidth() + leX;
            if (doY >= world.getMapHeight()) doY = world.getMapHeight() - doY;
            if (riX >= world.getMapWidth()) riX = world.getMapWidth() - riX;

            //find the up,down,right.left neighbor cells and add edge from neighbor to them, into the graph
            for (Cell relative : neighborCells) {
                if (relative.getXCoordinate() == upX && relative.getYCoordinate() == upY){
                    if (relative.getType() != CellType.WALL) {
                        addEdgeToGraph(Utils.getNodeNameFromCell(neighbor), Utils.getNodeNameFromCell(relative));
                        if (neighbor.getType() == CellType.SWAMP) {
                            dijkstra.addEdge(String.valueOf(Utils.getNodeNameFromCell(neighbor)), String.valueOf(Utils.getNodeNameFromCell(relative)), 3);
                        }
                        else {
                            dijkstra.addEdge(String.valueOf(Utils.getNodeNameFromCell(neighbor)), String.valueOf(Utils.getNodeNameFromCell(relative)), 1);
                        }
                    }
                }
                else if (relative.getXCoordinate() == doX && relative.getYCoordinate() == doY){
                    if (relative.getType() != CellType.WALL) {
                        addEdgeToGraph(Utils.getNodeNameFromCell(neighbor), Utils.getNodeNameFromCell(relative));
                        if (neighbor.getType() == CellType.SWAMP) {
                            dijkstra.addEdge(String.valueOf(Utils.getNodeNameFromCell(neighbor)), String.valueOf(Utils.getNodeNameFromCell(relative)), 3);
                        }
                        else {
                            dijkstra.addEdge(String.valueOf(Utils.getNodeNameFromCell(neighbor)), String.valueOf(Utils.getNodeNameFromCell(relative)), 1);
                        }
                    }
                }
                else if (relative.getXCoordinate() == riX && relative.getYCoordinate() == riY){
                    if (relative.getType() != CellType.WALL) {
                        addEdgeToGraph(Utils.getNodeNameFromCell(neighbor), Utils.getNodeNameFromCell(relative));
                        if (neighbor.getType() == CellType.SWAMP) {
                            dijkstra.addEdge(String.valueOf(Utils.getNodeNameFromCell(neighbor)), String.valueOf(Utils.getNodeNameFromCell(relative)), 3);
                        }
                        else {
                            dijkstra.addEdge(String.valueOf(Utils.getNodeNameFromCell(neighbor)), String.valueOf(Utils.getNodeNameFromCell(relative)), 1);
                        }
                    }
                }
                else if (relative.getXCoordinate() == leX && relative.getYCoordinate() == leY){
                    if (relative.getType() != CellType.WALL) {
                        addEdgeToGraph(Utils.getNodeNameFromCell(neighbor), Utils.getNodeNameFromCell(relative));
                        if (neighbor.getType() == CellType.SWAMP) {
                            dijkstra.addEdge(String.valueOf(Utils.getNodeNameFromCell(neighbor)), String.valueOf(Utils.getNodeNameFromCell(relative)), 3);
                        }
                        else {
                            dijkstra.addEdge(String.valueOf(Utils.getNodeNameFromCell(neighbor)), String.valueOf(Utils.getNodeNameFromCell(relative)), 1);
                        }
                    }
                }
            }
        }
    }

    /**
     * @param world
     * @return next direction for kargar to move
     */
    private Direction nextMoveDirectionKargar(World world) {
        nodesWithResources = Utils.sortMap(world, nodesWithResources, dijkstra);

        //if enemy based has been found, go to it
        if (enemyBaseGraphName != -1) {
            return getDirectionToNode(world, baseGraphName);
        }
        //if we are at target, nullify it
        if (targetNode != null && positionX == targetNode.getX() && positionY == targetNode.getY()) targetNode = null;
        //if we are at base, nullify target
        if (positionX == world.getBaseX() && positionY == world.getBaseY()) {
            targetNode = null;
        }

        //return to base if ant holds resources ELSE get another direction
        //if holding below 6 resources go for next res if no enemy is visible
        //and target is in sight
        int holdingResAmount = world.getAnt().getCurrentResource().getValue();
        if (holdingResAmount > 6) return getDirectionToNode(world, baseGraphName);
        else if (!isEnemySarbaazInSight && !nodesWithResources.isEmpty()) {
            int[] positionXY = new int[]{positionX, positionY};
            int[] targetXY = new int[]{nodesWithResources.get(0).getX(), nodesWithResources.get(0).getY()};
            if (Utils.isCellInSight(positionXY, targetXY, world)) {
                return getDirectionToNode(world, nodesWithResources.get(0).getGraphName());
            }
        }
        return getNextMoveDirection(world);
    }

    /**
     * @param world
     * @return next direction to move (the optimum one)
     */
    private Direction getNextMoveDirection(World world) {
        if (!nodesWithResources.isEmpty()) {
            //choose target if it's null
            if (targetNode == null) targetNode = nodesWithResources.get(0);
            //go to target if there is a path to it, otherwise nullify it
            if (targetNode != null) {
                //if target is different from first nodeWithRes, change it
                if (targetNode.getGraphName() != nodesWithResources.get(0).getGraphName()) {
                    targetNode = nodesWithResources.get(0);
                }
                if (isTherePathToNode(targetNode.getGraphName())) {
                    //TODO:check other nodes with resources
                    return getDirectionToNode(world, targetNode.getGraphName());
                }
                else {
//                    System.out.println("no path to target found, nullify target");
                    targetNode = null;
                }
            }
        }
        //go explore if none of the conditions above are met and we have no target
        if (targetNode == null) {
            Direction exploreDir = exploreAgent.turn(world).getDirection();
//            System.out.println("EXPLORE DIR:" + exploreDir);
            return exploreDir;

        }
        return Direction.CENTER;
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

        if (isSwampInSight) {
            // use dijkstra
            List<String> path = dijkstra.shortestPath(String.valueOf(positionGraphName), String.valueOf(nodeName));
            if (path != null) {
                //match the shortest path from DIJKSTRA to correct direction
                if (up != null && upGraphName == Integer.parseInt(path.get(1))) return Direction.UP;
                if (down != null && downGraphName == Integer.parseInt(path.get(1))) return Direction.DOWN;
                if (right != null && rightGraphName == Integer.parseInt(path.get(1))) return Direction.RIGHT;
                if (left != null && leftGraphName == Integer.parseInt(path.get(1))) return Direction.LEFT;
            }
        } else {
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
        }
        //return center if non is matched to BFS
        return Utils.getRandomDirection();
    }

    private boolean nodesWithResourcesContains(int nodeNameFromCell) {
        for (MyNode node : nodesWithResources) {
            if (node.getGraphName() == nodeNameFromCell) return true;
        }
        return false;
    }

    /**
     * @param src, graph name of source
     * @param dest graph name of destination
     * adds edge from src to dest
     */
    private void addEdgeToGraph(int src, int dest) {
            graph.addEdge(src, dest);
//            int[] cs = Utils.getCoordinatesFromName(src);
//            int[] cd = Utils.getCoordinatesFromName(dest);
//            System.out.println("edge added : " + cs[0] + "," + cs[1] + ">" + cd[0] + "," + cd[1] );
    }

    private void addMessage(MyMessage message) {
        messages.add(message);
    }

    private MyMessage getMessage() {
        if (messages.size() > 1) {
            messages.sort(new Comparator<MyMessage>() {
                @Override
                public int compare(MyMessage o1, MyMessage o2) {
                    return Integer.compare(o2.getValue(), o1.getValue());
                }
            });
        }
        if (!messages.isEmpty() && messages.get(0).getMessage().length() > 15) return messages.get(0);
        else if (!messages.isEmpty() && messages.size() > 1) {
            return new MyMessage((messages.get(0).getMessage() + messages.get(1).getMessage()), MESSAGE_VALUE_MAPRES);
        }
        return new MyMessage("", 0);
    }

    private void broadcastMap() {
        String m = "";
        LinkedList<Integer> edges = graph.getEdges(Utils.getNodeNameFromCoordinates(positionX, positionY));
        if (edges != null && !edges.isEmpty()) {
            m += "M" + Utils.getNodeNameFromCoordinates(positionX, positionY) + ",";
            for (int edgeName : edges) {
                if (!m.contains(String.valueOf(edgeName))) m += edgeName + ",";
            }
            m += "m";
        }
        if (!m.isEmpty()) addMessage(new MyMessage(m, MESSAGE_VALUE_MAP));
    }

    private void broadcastResources() {
        String m = "";
        if (nodesWithResources != null && !nodesWithResources.isEmpty()) {
            m += "R";
            for (MyNode node : nodesWithResources) {
                m += node.getGraphName() + ",";
            }
            m += "r";
        }
        if (!m.isEmpty()) addMessage(new MyMessage(m, MESSAGE_VALUE_RESOURCE));
    }

    private void listenToMapMessage(World world) {
        if (isNewBorn) {
            ArrayList<ArrayList<Integer>> data = new ArrayList<>();
            data = Utils.parseAllMapMessage(world, turn);
            if (!data.isEmpty()) {
                for (ArrayList<Integer> nodeEdges : data) {
                    if (!data.isEmpty() && data.size() > 1) {
                        for (int i = 1; i < nodeEdges.size(); i++) {
                            addEdgeToGraph(nodeEdges.get(0), nodeEdges.get(i));
                        }
                    }
                }
            }
        }
        else {
            ArrayList<Integer> data = new ArrayList<>();
            data = Utils.parseMapMessage(world, turn);
            if (!data.isEmpty() && data.size() > 1) {
                for (int i = 1; i < data.size(); i++) {
                    addEdgeToGraph(data.get(0), data.get(i));
                }
            }
        }
    }

    private void listenToResourceMessage(World world) {
        ArrayList<Integer> data = Utils.parseResourceMessage(world, turn);
        if (!data.isEmpty() && data.size() > 1) {
            for (int i=1;i<data.size();i++) {
                int[] c = Utils.getCoordinatesFromName(data.get(i));
                if (!nodesWithResourcesContains(data.get(i))) {
                    nodesWithResources.add(new MyNode(data.get(i), c[0], c[1]));
                }
            }
        }
    }

    private void listenToEnemyBaseMessage(World world) {
        if (enemyBaseGraphName == -1) {
            int base = Utils.parseBaseMessage(world, turn);
            if (base != -1) {
                enemyBaseGraphName = base;
            }
        }
    }

    private boolean isTherePathToNode(int target) {
        BfsHelper bfs = new BfsHelper(graph);
        bfs.findShortestPath(positionGraphName, target);
        return !bfs.getPathToDestination().isEmpty();
    }

    private boolean isAntInEnemyBaseArea() {
        if (enemyBaseGraphName != -1) {
            int[] enemyBaseXY = Utils.getCoordinatesFromName(enemyBaseGraphName);
            int distance = Math.abs(positionX - enemyBaseXY[0]) + Math.abs(positionY - enemyBaseXY[1]);
            return distance <= 6;
        } else return false;
    }

}
