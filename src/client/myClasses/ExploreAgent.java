package client.myClasses;

import client.World;
import client.model.Answer;
import client.model.Cell;
import client.model.enums.AntType;
import client.model.enums.CellType;
import client.model.enums.Direction;

import java.util.*;

public class ExploreAgent implements AIAgent {

    int[] dxs = {-1, 0, 1, 0};
    int[] dys = {0, -1, 0, 1};

    World world = null;

    Queue<XY> followingPath = new LinkedList<>();

    Cell[][] cells;

    private AntType antType;

    public ExploreAgent(World world, AntType antType) {
        this.antType = antType;
        this.world = world;
        cells = new Cell[world.getMapWidth()][world.getMapHeight()];
        for (int i = 0; i < world.getMapWidth(); i++) {
            for (int j = 0; j < world.getMapHeight(); j++) {
                cells[i][j] = new NullCell(i, j);
            }
        }
    }

    private void updateCells() {
        int d = world.getAnt().getViewDistance();
        for (int i = -d; i < d; i++) {
            for (int j = -d; j < d; j++) {
                int x = world.getAnt().getCurrentX() + i;
                int y = world.getAnt().getCurrentY() + j;
                if (x < 0) {
                    x += world.getMapWidth();
                } else if (x >= world.getMapWidth()) {
                    x -= world.getMapWidth();
                }
                if (y < 0) {
                    y += world.getMapHeight();
                } else if (y >= world.getMapHeight()) {
                    y -= world.getMapHeight();
                }

                int dx = x - world.getAnt().getCurrentX();
                int dy = y - world.getAnt().getCurrentY();

                Cell cell = world.getAnt().getVisibleMap().getRelativeCell(dx, dy);
                if (cell != null && cell.getXCoordinate() == 1 && cell.getYCoordinate() == 17) {
//                    System.out.println("ccceeelll:" + cell.getType());
                }
                if (cell != null) {
                    cells[x][y] = cell;
                }
            }
        }
    }

    private Cell getCell(int x, int y) {
        x %= world.getMapWidth();
        if (x < 0) {
            x += world.getMapWidth();
        }
        y %= world.getMapHeight();
        if (y < 0) {
            y += world.getMapHeight();
        }
        return cells[x][y];
    }


    private List<Cell> getNeighbours(Cell c) {
        ArrayList<Cell> result = new ArrayList<>();
        if (c instanceof NullCell || c.getType() == CellType.WALL) {
            return result;
        }
        for (int i = 0; i < 4; i++) {
            Cell cell = getCell(c.getXCoordinate() + dxs[i], c.getYCoordinate() + dys[i]);
            if (cell.getType() != CellType.WALL) {
                result.add(cell);
            }
        }
        if (antType == AntType.KARGAR) Collections.shuffle(result);
        return result;
    }

    private boolean findPathTo(IsTargetCell isTargetCell) {
        boolean[][] mark = new boolean[world.getMapWidth()][world.getMapHeight()];
        Queue<CellBFS> queue = new LinkedList<>();
        {
            Cell c = world.getAnt().getVisibleMap().getRelativeCell(0, 0);
            queue.add(new CellBFS(c, null));
            mark[c.getXCoordinate()][c.getYCoordinate()] = true;
        }
        while (!queue.isEmpty()) {
            CellBFS cell = queue.remove();
            if (isTargetCell.isTarget(cell.cell)) {
                List<XY> result = new ArrayList<>();
                while (cell.parent != null) {
                    result.add(new XY(cell.cell.getXCoordinate(), cell.cell.getYCoordinate()));
                    cell = cell.parent;
                }
                for (int i = result.size() - 1; i >= 0; i--) {
                    followingPath.add(result.get(i));
                }
                return true;
            }
            for (Cell nc : getNeighbours(cell.cell)) {
                if (!mark[nc.getXCoordinate()][nc.getYCoordinate()]) {
                    mark[nc.getXCoordinate()][nc.getYCoordinate()] = true;
                    queue.add(new CellBFS(nc, cell));
                }
            }
        }
        return false;
    }

    private Direction getDirectionOfXY(int x, int y, World world) {
        int upX = world.getAnt().getXCoordinate();
        int upY = world.getAnt().getYCoordinate() - 1;
        int doX = world.getAnt().getXCoordinate();
        int doY = world.getAnt().getYCoordinate() + 1;
        int riX = world.getAnt().getXCoordinate() + 1;
        int riY = world.getAnt().getYCoordinate();
        int leX = world.getAnt().getXCoordinate() -1;
        int leY = world.getAnt().getYCoordinate();

        if (upY < 0) upY = world.getMapHeight() + upY;
        if (leX < 0) leX = world.getMapWidth() + leX;
        if (doY >= world.getMapHeight()) doY = world.getMapHeight() - doY;
        if (riX >= world.getMapWidth()) riX = world.getMapWidth() - riX;

        if (x==27 && y==17){
//            System.out.println("");
        }
        if (x == upX && y == upY) {
            return Direction.UP;
        }
        if (x == doX && y == doY) {
            return Direction.DOWN;
        }
        if (x == riX && y == riY) {
            return Direction.RIGHT;
        }
        if (x == leX && y == leY) {
            return Direction.LEFT;
        }
        return null;




        //////////////////////
//        if (x < world.getAnt().getCurrentX()) {
//            return Direction.LEFT;
//        }
//        if (x > world.getAnt().getCurrentX()) {
//            return Direction.RIGHT;
//        }
//        if (y > world.getAnt().getCurrentY()) {
//            return Direction.DOWN;
//        }
//        if (y < world.getAnt().getCurrentY()) {
//            return Direction.UP;
//        }
//        return null;
    }

    @Override
    public Answer turn(World world) {
        this.world = world;
        updateCells();
        if (!followingPath.isEmpty()) {
            XY xy = followingPath.poll();
//            System.out.println("explore agent XY:" + xy.x + "," + xy.y);
            return new Answer(getDirectionOfXY(xy.x, xy.y, world));
        } else {
            if (findPathTo((cell) -> cell.getResource() == null)) {
//                System.out.println("explore map!");
                return turn(world);
            }
        }
        return new Answer(Utils.getRandomDirection());
    }
}

interface IsTargetCell {
    boolean isTarget(Cell c);
}

class XY {
    int x;
    int y;

    public XY(int x, int y) {
        this.x = x;
        this.y = y;
    }
}

class CellBFS {
    public Cell cell;
    public CellBFS parent;

    public CellBFS(Cell cell, CellBFS parent) {
        this.cell = cell;
        this.parent = parent;
    }
}

class NullCell extends Cell {
    public NullCell(int x, int y) {
        super(null, x, y, null);
    }
}