package client.myClasses;

import client.dijkstra.Dijkstra;
import jdk.jshell.execution.Util;

public class Test {

    public static void main(String[] args) {
        Dijkstra d = new Dijkstra();
        d.addEdge("a", "b", 1);
        System.out.println(d.shortestPath("a", "b"));
    }
}
