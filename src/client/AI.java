package client;


import client.model.Answer;
import client.model.Cell;
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
    private static Direction prevDirection = Direction.UP;

    public Answer turn(World world) {
        // Enter your AI code here
        AI.turn++;
        message = "";

        //////////
//        Cell locationCell = world.getAnt().getLocationCell();
//        Cell neighborCell = world.getAnt().getNeighborCell(1, 1);
//
//        message += "-" + world.getAnt().getYCoordinate();
//        if (neighborCell != null) message += "nCl!n/";
//        if (locationCell != null) message += "lCl!n/";
//        getAvailableDirections(world);
//        return new Answer(getRandomDirection(), message, 10);
        ///////////////



        Direction nextMoveDirection = getNextMoveDirection(world);

        message += "nD:" + nextMoveDirection + "/pD:" + prevDirection;

        prevDirection = nextMoveDirection;

        return new Answer(nextMoveDirection, message, 10);
    }

    private Direction getNextMoveDirection(World world) {
        ArrayList<Direction> availableDirections = getAvailableDirections(world);
        Direction optimumDirection = findOptimumDirection(availableDirections);
        return optimumDirection;
    }

    private Direction findOptimumDirection(ArrayList<Direction> availableDirections) {
        if (availableDirections.contains(prevDirection)) return prevDirection;
        else return availableDirections.get(new Random().nextInt(availableDirections.size()));
    }

    private Direction getDirectionToOptimumCell(World world, Cell optimumCell) {
        int positionX = world.getAnt().getXCoordinate();
        int positionY = world.getAnt().getYCoordinate();

        ArrayList<int[]> possibleNextCoordinates = new ArrayList<>();
        if (world.getAnt().getNeighborCell(0, -1).getType() != CellType.WALL) possibleNextCoordinates.add(new int[]{positionX, positionY - 1}); else possibleNextCoordinates.add(null);
        if (world.getAnt().getNeighborCell(0, 1).getType() != CellType.WALL) possibleNextCoordinates.add(new int[]{positionX, positionY + 1}); else possibleNextCoordinates.add(null);
        if (world.getAnt().getNeighborCell(1, 0).getType() != CellType.WALL) possibleNextCoordinates.add(new int[]{positionX + 1, positionY}); else possibleNextCoordinates.add(null);
        if (world.getAnt().getNeighborCell(-1, 0).getType() != CellType.WALL) possibleNextCoordinates.add(new int[]{positionX - 1, positionY}); else possibleNextCoordinates.add(null);
        for (int[] nxtCoor : possibleNextCoordinates) {
            if (nxtCoor != null) {
                if (nxtCoor[0] > world.getMapWidth() || nxtCoor[1] > world.getMapHeight()) {
                    nxtCoor = null;
                }
            }
        }
//        possibleNextCoordinates.add(new int[]{positionX, positionY});

        if (optimumCell != null) {
            message += "at:" + positionX + "," + positionY + "/";
            int bestIndex = 0;
            int nextBestIndex = bestIndex;
            int[] bestCoordinate = new int[2];
            int minDistance = 100000;
            for (int i = 0; i < possibleNextCoordinates.size(); i++) {
                if (possibleNextCoordinates.get(i) != null) {
                    int yDistance = Math.abs(possibleNextCoordinates.get(i)[1] - optimumCell.getYCoordinate());
                    int xDistance = Math.abs(possibleNextCoordinates.get(i)[0] - optimumCell.getXCoordinate());
                    int manDistance = yDistance + xDistance;

                    if (manDistance < minDistance) {
                        minDistance = manDistance;
                        bestCoordinate = possibleNextCoordinates.get(i);
                        nextBestIndex = bestIndex;
                        bestIndex = i;
                    }
                }
            }
            if (bestIndex == 4) {
                if (optimumCell.getYCoordinate() != positionY || optimumCell.getXCoordinate() != positionX) {
                    bestIndex = nextBestIndex;
                }
            }
            message += "to:" + optimumCell.getXCoordinate() + "," + optimumCell.getYCoordinate() + "/";
            message += "dir:" + bestIndex + "/";
//            message += "bCo:" + bestCoordinate;
//            message += "opcel:" + optimumCell.getXCoordinate() + "," + optimumCell.getYCoordinate() + "/";
//            message += "at(" + myY + "," + myX + ")To(" + Arrays.toString(bestCoordinate) + "dir:" + bestIndex;
//            message += "ndir:" + bestIndex;
            return switch (bestIndex) {
                case 0 -> Direction.UP;
                case 1 -> Direction.DOWN;
                case 2 -> Direction.RIGHT;
                case 3 -> Direction.LEFT;
                default -> Direction.CENTER;
            };
        }
        else {
//            message += "opc?";
            return getRandomDirection();
        }
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

    private ArrayList<Direction> getAvailableDirections(World world) {
        ArrayList<Direction> availableDirections = new ArrayList<>();

        Cell up = world.getAnt().getNeighborCell(0, -1);
        Cell down = world.getAnt().getNeighborCell(0, 1);
        Cell right = world.getAnt().getNeighborCell(1, 0);
        Cell left = world.getAnt().getNeighborCell(-1, 0);

        if (up != null && up.getType() != CellType.WALL && isCellInMovingBounds(up, world)) availableDirections.add(Direction.UP);
        if (down != null && down.getType() != CellType.WALL && isCellInMovingBounds(down, world)) availableDirections.add(Direction.DOWN);
        if (right != null && right.getType() != CellType.WALL && isCellInMovingBounds(right, world)) availableDirections.add(Direction.RIGHT);
        if (left != null && left.getType() != CellType.WALL && isCellInMovingBounds(left, world)) availableDirections.add(Direction.LEFT);

        return availableDirections;
    }

    private boolean isCellInMovingBounds(Cell cell, World world) {
        boolean isIn = Math.abs(cell.getXCoordinate() - world.getAnt().getXCoordinate()) +
                Math.abs(cell.getYCoordinate() - world.getAnt().getYCoordinate()) <= 1;

        System.out.println("isCinBound: Cell at:" + cell.getXCoordinate() + "," + cell.getYCoordinate() + "..." + isIn);

        return isIn;
    }

    private Cell findOptimumCell(ArrayList<Cell> cells) {
        ArrayList<Cell> optimumCells = new ArrayList<>();

        if (!cells.isEmpty()) {
            for (Cell cell : cells) {
                if (cell.getType() == CellType.EMPTY && cell.getResource().getValue() != 0) {
                    optimumCells.add(cell);
                }
                if (cell.getType() == CellType.BASE) {
                    optimumCells.add(cell);
                }
            }
            if (!optimumCells.isEmpty()) {
                optimumCells.sort(new Comparator<Cell>() {
                    @Override
                    public int compare(Cell o1, Cell o2) {
                        return o1.getResource().getValue() - o2.getResource().getValue();
                    }
                });
                return optimumCells.get(optimumCells.size()-1);
            }
            else return null;
        }
        else return null;
    }

    private String getCellType(Cell cell) {
        if (cell.getType() == CellType.BASE) return "BA";
        if (cell.getType() == CellType.EMPTY) return "EM";
        else return "WA";
    }
}