package client.bfs;

/**
 * A BFS over a unweighted graph from a source vertex, say u, gives a shortest path from u to every other vertex.
 * Note: This is true for both directed an undirected graph but only if the source vertex of shortest path is the node
 * on which bfs was executed.
 */
public class UnweightedShortestPath extends BFS {

    public UnweightedShortestPath(Graph g) {
        super(g);
    }

    public void findShortestPath(final int source, final int destination) {
        bfs(source);
        printParent();
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

    public static void main(String[] args) {
        Graph graph = new AdjList(1000, false);
        graph.addEdge(1, 2);
        graph.addEdge(1, 3);
        graph.addEdge(2, 3);
        graph.addEdge(3, 4);
        graph.addEdge(3, 5);
        graph.addEdge(5, 4);
        graph.addEdge(4, 6);



        final UnweightedShortestPath usp = new UnweightedShortestPath(graph);
        usp.findShortestPath(1, 6);
    }
}
