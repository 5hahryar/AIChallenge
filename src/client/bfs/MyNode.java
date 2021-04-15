package client.bfs;

import client.model.Cell;
import client.model.enums.ResourceType;

public class MyNode {

    private int graphName;
    private int x;
    private int y;
    private int resourceValue;
    private ResourceType resourceType;

    public MyNode(int graphName, int x, int y) {
        this.graphName = graphName;
        this.x = x;
        this.y = y;
    }

    public MyNode(int graphName, Cell cell) {
        this.graphName = graphName;
        this.x = cell.getXCoordinate();
        this.y = cell.getYCoordinate();
        this.resourceValue = cell.getResource().getValue();
        this.resourceType = cell.getResource().getType();
    }

//    public MyNode(int graphName, int x, int y, ResourceType resourceType) {
//        this.graphName = graphName;
//        this.x = x;
//        this.y = y;
//        this.resourceType = resourceType;
//    }

    public int getResourceValue() {
        return resourceValue;
    }

    public void setResourceValue(int resourceValue) {
        this.resourceValue = resourceValue;
    }

    public int getGraphName() {
        return graphName;
    }

    public void setGraphName(int graphName) {
        this.graphName = graphName;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }
}
