import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;


public class run2 {
    // Константы для символов ключей и дверей
    private static final char[] KEYS_CHAR = new char[26];
    private static final char[] DOORS_CHAR = new char[26];

    private static final int[] DIRECTION_X = {-1, 1, 0, 0};
    private static final int[] DIRECTION_Y = {0, 0, -1, 1};


    static {
        for (int i = 0; i < 26; i++) {
            KEYS_CHAR[i] = (char) ('a' + i);
            DOORS_CHAR[i] = (char) ('A' + i);
        }
    }

    // Чтение данных из стандартного ввода
    private static char[][] getInput() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        List<String> lines = new ArrayList<>();
        String line;


        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            lines.add(line);
        }


        char[][] maze = new char[lines.size()][];
        for (int i = 0; i < lines.size(); i++) {
            maze[i] = lines.get(i).toCharArray();
        }


        return maze;
    }

    private static int solve(char[][] data) {
        Map<Character, int[]> keyCoordinates = new HashMap<>();
        List<int[]> robots = new ArrayList<>();
        int rows = data.length;
        int columns = data[0].length;

        // Поиск координат роботов и ключей
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                char cell = data[i][j];
                if (cell == '@') {
                    robots.add(new int[]{i, j});
                } else if (cell >= 'a' && cell <= 'z') {
                    keyCoordinates.put(cell, new int[]{i, j});
                }
            }
        }
        List<int[]> nodes = new ArrayList<>(robots);
        List<Character> keys = new ArrayList<>(keyCoordinates.keySet());
        List<List<Edge>> graph = new ArrayList<>();
        Map<Character, Integer> keyPos = new HashMap<>();
        Collections.sort(keys);

        for (int i = 0; i < keys.size(); i++) {
            char key = keys.get(i);
            nodes.add(keyCoordinates.get(key));
            keyPos.put(key, i + 4);
        }

        for (int i = 0; i < nodes.size(); i++) {
            graph.add(new ArrayList<>());
        }

        for (int node = 0; node < nodes.size(); node++) {
            boolean[][] visitedCoordinate = new boolean[rows][columns];
            Queue<Point> queue = new ArrayDeque<>();

            int startX = nodes.get(node)[0];
            int startY = nodes.get(node)[1];
            visitedCoordinate[startX][startY] = true;
            queue.add(new Point(startX, startY, 0, new HashSet<>()));

            while (!queue.isEmpty()) {
                Point currPoint = queue.poll();
                char cell = data[currPoint.x][currPoint.y];
                Set<Character> doors = new HashSet<>(currPoint.doors);

                if (cell >= 'A' && cell <= 'Z') {
                    doors.add(cell);
                }

                if (cell >= 'a' && cell <= 'z') {
                    int newFromNode = keyPos.get(cell);

                    if (newFromNode != node) {
                        graph.get(node).add(new Edge(newFromNode, currPoint.dist, doors));
                    }
                }

                for (int direction = 0; direction < 4; direction++) {
                    int newX = currPoint.x + DIRECTION_X[direction];
                    int newY = currPoint.y + DIRECTION_Y[direction];

                    if (newX >= 0 && newX < rows && newY >= 0 && newY < columns
                            && !visitedCoordinate[newX][newY] && data[newX][newY] != '#') {
                        visitedCoordinate[newX][newY] = true;
                        queue.add(new Point(newX, newY, currPoint.dist + 1, doors));
                    }
                }
            }
        }

        PriorityQueue<Maze> priorityQueue = new PriorityQueue<>();
        Map<Maze, Integer> mazeSteps = new HashMap<>();

        Maze maze = new Maze(new int[]{0, 1, 2, 3}, Collections.emptySet(), 0);
        mazeSteps.put(maze, 0);
        priorityQueue.add(maze);

        while (!priorityQueue.isEmpty()) {
            Maze currentMaze = priorityQueue.poll();

            if (currentMaze.stepValue != mazeSteps.getOrDefault(currentMaze, Integer.MAX_VALUE)) {
                continue;
            }

            if (currentMaze.keys.size() == keys.size()) {
                return currentMaze.stepValue;
            }

            for (int robot = 0; robot < 4; robot++) {
                int currentCoordinate = currentMaze.robotCoordinates[robot];

                for (Edge edge : graph.get(currentCoordinate)) {
                    char key = keys.get(edge.to - 4);

                    if (currentMaze.keys.contains(key)) {
                        continue;
                    }

                    boolean isDoorsOpen = true;
                    for (char door : edge.doorsRequired) {
                        if (!currentMaze.keys.contains(Character.toLowerCase(door))) {
                            isDoorsOpen = false;
                            break;
                        }
                    }
                    if (!isDoorsOpen) {
                        continue;
                    }

                    Set<Character> newKeys = new HashSet<>(currentMaze.keys);
                    newKeys.add(key);

                    int[] newCoordinates = currentMaze.robotCoordinates.clone();
                    newCoordinates[robot] = edge.to;

                    Maze newMaze = new Maze(newCoordinates, newKeys, currentMaze.stepValue + edge.dist);

                    int currentValue = mazeSteps.getOrDefault(newMaze, Integer.MAX_VALUE);

                    if (newMaze.stepValue < currentValue) {
                        mazeSteps.put(newMaze, newMaze.stepValue);
                        priorityQueue.add(newMaze);
                    }
                }
            }
        }

        return Integer.MAX_VALUE;
    }

    public static void main(String[] args) throws IOException {
        char[][] data = getInput();
        int result = solve(data);

        if (result == Integer.MAX_VALUE) {
            System.out.println("No solution found");
        } else {
            System.out.println(result);
        }
    }

    private static class Edge {
        public int to;
        public int dist;
        public Set<Character> doorsRequired;

        private Edge(int to, int dist, Set<Character> doorsRequired) {
            this.to = to;
            this.dist = dist;
            this.doorsRequired = doorsRequired;
        }
    }

    private static class Point {
        public int x;
        public int y;
        public int dist;
        public Set<Character> doors;

        public Point(int x, int y, int dist, Set<Character> doors) {
            this.x = x;
            this.y = y;
            this.dist = dist;
            this.doors = doors;
        }
    }

    private static class Maze implements Comparable<Maze> {
        public int[] robotCoordinates;
        public Set<Character> keys;
        public int stepValue;

        public Maze(int[] robotCoordinates, Set<Character> keys, int stepValue) {
            this.robotCoordinates = robotCoordinates.clone();
            this.keys = new HashSet<>(keys);
            this.stepValue = stepValue;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null) {
                return false;
            }
            if (getClass() != o.getClass()) {
                return false;
            }

            Maze s = (Maze) o;
            return Arrays.equals(robotCoordinates, s.robotCoordinates) && keys.equals(s.keys);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(robotCoordinates) * 31 + keys.hashCode();
        }

        @Override
        public int compareTo(Maze o) {
            return Integer.compare(this.stepValue, o.stepValue);
        }
    }
}