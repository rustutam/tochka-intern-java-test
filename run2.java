package tochka;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class run2 {
    // Константы для символов ключей и дверей
    private static final char[] KEYS_CHAR = new char[26];
    private static final char[] DOORS_CHAR = new char[26];

    static {
        for (int i = 0; i < 26; i++) {
            KEYS_CHAR[i] = (char) ('a' + i);
            DOORS_CHAR[i] = (char) ('A' + i);
        }
    }

    private static class State {
        int[] positions; // x0, y0, x1, y1, x2, y2, x3, y3
        int keys;

        State(int[] positions, int keys) {
            this.positions = positions.clone();
            this.keys = keys;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            State state = (State) o;
            return keys == state.keys && Arrays.equals(positions, state.positions);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(keys);
            result = 31 * result + Arrays.hashCode(positions);
            return result;
        }
    }

    private static class Point {
        int x, y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private static final int[][] dirs = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

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
        List<Point> robots = new ArrayList<>();
        int totalKeys = 0;

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                if (data[i][j] == '@') {
                    robots.add(new Point(j, i));
                    data[i][j] = '.'; // Mark as walkable
                } else if (Character.isLowerCase(data[i][j])) {
                    totalKeys |= 1 << (data[i][j] - 'a');
                }
            }
        }

        if (robots.size() != 4) {
            return Integer.MAX_VALUE;
        }

        int[] initialPositions = new int[8];
        for (int i = 0; i < 4; i++) {
            Point p = robots.get(i);
            initialPositions[2 * i] = p.x;
            initialPositions[2 * i + 1] = p.y;
        }
        State initialState = new State(initialPositions, 0);

        Queue<State> queue = new LinkedList<>();
        Map<State, Integer> stepsMap = new HashMap<>();
        Set<State> visited = new HashSet<>();

        queue.add(initialState);
        stepsMap.put(initialState, 0);
        visited.add(initialState);

        while (!queue.isEmpty()) {
            State current = queue.poll();
            int steps = stepsMap.get(current);

            if (current.keys == totalKeys) {
                return steps;
            }

            for (int robot = 0; robot < 4; robot++) {
                int x = current.positions[2 * robot];
                int y = current.positions[2 * robot + 1];

                for (int[] dir : dirs) {
                    int nx = x + dir[0];
                    int ny = y + dir[1];

                    if (ny < 0 || ny >= data.length || nx < 0 || nx >= data[ny].length) {
                        continue;
                    }

                    char cell = data[ny][nx];
                    if (cell == '#') {
                        continue;
                    }

                    if (Character.isUpperCase(cell)) {
                        int requiredKey = cell - 'A';
                        if ((current.keys & (1 << requiredKey)) == 0) {
                            continue;
                        }
                    }

                    int[] newPositions = current.positions.clone();
                    newPositions[2 * robot] = nx;
                    newPositions[2 * robot + 1] = ny;

                    int newKeys = current.keys;
                    if (Character.isLowerCase(cell)) {
                        newKeys |= 1 << (cell - 'a');
                    }

                    State newState = new State(newPositions, newKeys);

                    if (!visited.contains(newState)) {
                        visited.add(newState);
                        stepsMap.put(newState, steps + 1);
                        queue.add(newState);
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
}