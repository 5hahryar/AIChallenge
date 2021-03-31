package client.bfs;

public class MyNode {

    public MyNode(int graphName, int x, int y) {
        this.graphName = graphName;
        this.x = x;
        this.y = y;
    }

    private int graphName;
    private int x;
    private int y;

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
}
