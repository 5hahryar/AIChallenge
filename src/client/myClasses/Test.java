package client.myClasses;

import jdk.jshell.execution.Util;

public class Test {

    public static void main(String[] args) {
        int max=0;
        for (int i=0;i<40;i++) {
            for (int j=0;j<40;j++) {
                if (Utils.getNodeNameFromCoordinates(i, j) > max) {
                    max = Utils.getNodeNameFromCoordinates(i, j);
                }
            }
        }
        System.out.println(max);
    }
}
