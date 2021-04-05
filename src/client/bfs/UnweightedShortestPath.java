package client.bfs;

import java.util.ArrayList;
import java.util.Random;

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
        ArrayList<MyNode> nodes = new ArrayList<>();
        Graph graph = new AdjList(10000, false);

        for (int a=0;a<31;a++) {
            for (int b=0;b<31;b++) {
                int value = (a + b) * (a + b + 1) / 2 + b;
                nodes.add(new MyNode(value, a, b));
            }
        }

        for (int i=0;i<20;i++) {
            graph.addEdge(1234, 2234);
            graph.addEdge(2234, 1234);
            graph.addEdge(1234, 2234);
            graph.addEdge(2234, 5462);
            graph.addEdge(2234, 4453);
            graph.addEdge(4453, 7811);
            graph.addEdge(5462, 7811);
        }

        final UnweightedShortestPath usp = new UnweightedShortestPath(graph);
//        graph.printGraph();
        usp.findShortestPath(1234, 7811);
    }
}
