package client.myClasses;

import client.World;
import client.model.Answer;
import client.model.Cell;
import client.model.enums.CellType;
import client.model.enums.Direction;

import java.util.*;

public class ExploreAgent implements AIAgent {

    int[] dxs = {-1, 0, 1, 0};
    int[] dys = {0, -1, 0, 1};

    World world = null;

    Queue<XY> followingPath = new LinkedList<>();

    Cell[][] cells;

    public ExploreAgent(World world) {
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
            result.add(getCell(c.getXCoordinate() + dxs[i], c.getYCoordinate() + dys[i]));
        }
//        Collections.shuffle(result);
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

    private Direction getDirectionOfXY(int x, int y) {
        if (x < world.getAnt().getCurrentX()) {
            return Direction.LEFT;
        }
        if (x > world.getAnt().getCurrentX()) {
            return Direction.RIGHT;
        }
        if (y > world.getAnt().getCurrentY()) {
            return Direction.DOWN;
        }
        if (y < world.getAnt().getCurrentY()) {
            return Direction.UP;
        }
        return null;
    }

    @Override
    public Answer turn(World world) {
        this.world = world;
        updateCells();
        if (!followingPath.isEmpty()) {
            XY xy = followingPath.poll();
            return new Answer(getDirectionOfXY(xy.x, xy.y));
        } else {
            if (findPathTo((cell) -> cell.getResource() == null)) {
                System.out.println("explore map!");
                return turn(world);
            }
        }
        return new Answer(Direction.DOWN);
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