package client.myClasses;

import client.World;
import client.bfs.MyNode;
import client.model.Cell;
import client.model.Chat;
import client.model.enums.Direction;
import client.model.enums.ResourceType;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Utils {

    public static Direction getRandomDirection() {
        int rand = new Random().nextInt(4);
        if (rand == 0) return Direction.UP;
        if (rand == 1) return Direction.DOWN;
        if (rand == 2) return Direction.RIGHT;
        if (rand == 3) return Direction.LEFT;
        return Direction.CENTER;

//        return switch (new Random().nextInt(4)) {
//            case 0 -> Direction.UP;
//            case 1 -> Direction.DOWN;
//            case 2 -> Direction.RIGHT;
//            case 3 -> Direction.LEFT;
//            default -> Direction.CENTER;
//        };
    }

    public static int getNodeNameFromCoordinates(int x, int y) {
        int a = x;
        int b = y;
        int value = (a + b) * (a + b + 1) / 2 + b;

        return value;
    }

    public static int getNodeNameFromCell(Cell cell) {
        int a = cell.getXCoordinate();
        int b = cell.getYCoordinate();
        int value = (a + b) * (a + b + 1) / 2 + b;

        return value;
    }

    public static int[] getCoordinatesFromName(int name) {
        int t = (int) (Math.floor((Math.sqrt(8 * name + 1) - 1) / 2));
        int x = t * (t + 3) / 2 - name;
        int y = name - t * (t + 1) / 2;
        return new int[]{x, y}; //Returning an array containing the two numbers
    }

    /**
     *
     * @param cell
     * @param world
     * @return if cell is in reach or not (currently used to check if cell if in the other side of map or not)
     */
    public static boolean isCellInMovingBounds(Cell cell, World world) {
        return Math.abs(cell.getXCoordinate() - world.getAnt().getXCoordinate()) +
                Math.abs(cell.getYCoordinate() - world.getAnt().getYCoordinate()) <= 1;
    }

    /**
     * sort the list of nodes with resources, by their distance to current position MIN...MAX
     * @param world
     */
    public static ArrayList<MyNode> sortMap(World world, ArrayList<MyNode> nodesWithResources) {
        int positionX = world.getAnt().getXCoordinate();
        int positionY = world.getAnt().getYCoordinate();

        if (nodesWithResources != null && nodesWithResources.size() > 1) {
            nodesWithResources.sort(new Comparator<MyNode>() {
                @Override
                public int compare(MyNode o1, MyNode o2) {
                    int o2Distance = Math.abs(positionX-o2.getX()) + Math.abs(positionY-o2.getY());
                    int o1Distance = Math.abs(positionX-o1.getX()) + Math.abs(positionY-o1.getY());

                    if (o1.getResourceType() != null && o1.getResourceType() == ResourceType.GRASS) o1Distance *= 2;
                    if (o2.getResourceType() != null && o2.getResourceType() == ResourceType.GRASS) o2Distance *= 2;

                    return Integer.compare(o1Distance, o2Distance);
                }
            });
        }
        return nodesWithResources;
    }

    public static ArrayList<Integer> parseMapMessage(World world, int turn) {
        ArrayList<Integer> data = new ArrayList<>();
        List<Chat> allChats = world.getChatBox().getAllChats();
        allChats = sortChatOnTurn(allChats);
        if (!allChats.isEmpty()) {
            String lastChat = allChats.get(0).getText();
            if (!lastChat.isEmpty() && lastChat.contains("*M:")) {
                int codeIndex = lastChat.indexOf("*M:");
                if (lastChat.contains("*R:")) {
                    lastChat = lastChat.substring(codeIndex, lastChat.indexOf('m'));
                }
                codeIndex = lastChat.indexOf("*M:");
                int nextIndex = lastChat.indexOf(',');
                int srcNodeName = Integer.parseInt(lastChat.substring(codeIndex+3, nextIndex));
                data.add(srcNodeName);

                lastChat = lastChat.substring(nextIndex+1);
                while (lastChat.contains(",")) {
                    int edge = Integer.parseInt(lastChat.substring(0, lastChat.indexOf(',')));
                    data.add(edge);
                    lastChat = lastChat.substring(lastChat.indexOf(',')+1);
                }
            }
        }

        return data;
    }

    private static List<Chat> sortChatOnTurn(List<Chat> allChats) {
        allChats.sort(new Comparator<Chat>() {
            @Override
            public int compare(Chat o1, Chat o2) {
                return Integer.compare(o2.getTurn(), o1.getTurn());
            }
        });
        return allChats;
    }

    public static ArrayList<ArrayList<Integer>> parseAllMapMessage(World world, int turn) {
        ArrayList<ArrayList<Integer>> data = new ArrayList<>();
        List<Chat> chats = world.getChatBox().getAllChats();
        if (!chats.isEmpty()) {
            for (Chat chat : chats) {
                ArrayList<Integer> nodeEdges = new ArrayList<>();
                String lastChat = chat.getText();
                if (!lastChat.isEmpty() && lastChat.contains("*M:")) {
                    int codeIndex = lastChat.indexOf("*M:");
                    if (lastChat.contains("*R:")) {
                        lastChat = lastChat.substring(codeIndex, lastChat.indexOf('m'));
                    }
                    codeIndex = lastChat.indexOf("*M:");
                    int nextIndex = lastChat.indexOf(',');
                    int srcNodeName = Integer.parseInt(lastChat.substring(codeIndex + 3, nextIndex));
                    nodeEdges.add(srcNodeName);

                    lastChat = lastChat.substring(nextIndex + 1);
                    while (lastChat.contains(",")) {
                        int edge = Integer.parseInt(lastChat.substring(0, lastChat.indexOf(',')));
                        nodeEdges.add(edge);
                        lastChat = lastChat.substring(lastChat.indexOf(',') + 1);
                    }
                }
                data.add(nodeEdges);
            }
        }

        return data;
    }

    public static ArrayList<Integer> parseResourceMessage(World world, int turn) {
        ArrayList<Integer> data = new ArrayList<>();
        List<Chat> allChats = world.getChatBox().getAllChats();
        allChats = sortChatOnTurn(allChats);
        if (!allChats.isEmpty()) {
            String lastChat = allChats.get(0).getText();
            if (!lastChat.isEmpty() && lastChat.contains("*R:")) {
                int codeIndex = lastChat.indexOf("*R:");
                if (lastChat.contains("*M:")) {
                    lastChat = lastChat.substring(codeIndex, lastChat.indexOf('r'));
                }
                codeIndex = lastChat.indexOf("*R:");
                int nextIndex = lastChat.indexOf(',');
                lastChat = lastChat.substring(codeIndex+3);
                while (lastChat.contains(",")) {
                    int node = Integer.parseInt(lastChat.substring(0, lastChat.indexOf(',')));
                    data.add(node);
                    lastChat = lastChat.substring(lastChat.indexOf(',')+1);
                }
            }
        }

        return data;
    }

    public static int parseBaseMessage(World world, int turn) {
        int data = -1;
        List<Chat> allChats = world.getChatBox().getAllChats();
        allChats = sortChatOnTurn(allChats);
        if (!allChats.isEmpty()) {
            String lastChat = allChats.get(0).getText();
            if (!lastChat.isEmpty() && lastChat.contains("*B:")) {
                int codeIndex = lastChat.indexOf("*B:");
                int endIndex = lastChat.indexOf('b');
                lastChat = lastChat.substring(codeIndex+3, endIndex);
                data = Integer.parseInt(lastChat);
            }
        }

        return data;
    }

    public static void createLog() {
        try {
            File myObj = new File("myLog.txt");
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public static void writeLog(String m) {
        try {
            FileWriter myWriter = new FileWriter("myLog.txt");
            myWriter.write(m);
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }


}
