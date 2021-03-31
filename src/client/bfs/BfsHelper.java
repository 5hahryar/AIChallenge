package client.bfs;

public class BfsHelper extends BFS{

    public BfsHelper(Graph graph) {
        super(graph);
    }

    public void findShortestPath(int source, int destination) {
        bfs(source);
//        printParent();
        // After executing bfs parent array can be used to find shortest path from source -> destination
        printPath(destination);
    }

    @Override
    void processVertexEarly(int u) {

    }

    @Override
    void processEdge(int u, int v) {

    }

    @Override
    void processVertexLate(int v) {

    }
}
