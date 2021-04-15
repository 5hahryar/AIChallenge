package client.myClasses;

import client.model.Cell;
import client.model.enums.Direction;

public class MyDirection{

    private Direction direction;
    private int weight;
    private Cell cell;

    public MyDirection(Direction direction, int weight) {
        this.direction = direction;
        this.weight = weight;
    }

    public MyDirection(Direction direction, Cell cell) {
        this.direction = direction;
        this.weight = -1;
        this.cell = cell;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public Cell getCell() {
        return cell;
    }

    public void setCell(Cell cell) {
        this.cell = cell;
    }

}
