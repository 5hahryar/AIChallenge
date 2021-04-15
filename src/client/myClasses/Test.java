package client.myClasses;

import jdk.jshell.execution.Util;

public class Test {

    public static void main(String[] args) {
        int name = Utils.getNodeNameFromCoordinates(70, 70);
        System.out.println(name);
        int[] c = Utils.getCoordinatesFromName(name);
        System.out.println(c[0] + ", " + c[1]);
    }
}
