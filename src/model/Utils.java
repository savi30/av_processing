package model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public final class Utils {
    public final static int[][] Q = {{6, 4, 4, 6, 10, 16, 20, 24},
            {5, 5, 6, 8, 10, 23, 24, 22},
            {6, 5, 6, 10, 16, 23, 28, 22},
            {6, 7, 9, 12, 20, 35, 32, 25},
            {7, 9, 15, 22, 27, 44, 41, 31},
            {10, 14, 22, 26, 32, 42, 45, 37},
            {20, 26, 31, 35, 41, 48, 48, 40},
            {29, 37, 38, 39, 45, 40, 41, 40}};

    public final static HashMap<Integer, List<Integer>> amplitudes = new HashMap<>() {{
        put(1, Arrays.asList(1));
        put(2, Arrays.asList(2, 3));
        put(3, Arrays.asList(4, 7));
        put(4, Arrays.asList(8, 15));
        put(5, Arrays.asList(16, 31));
        put(6, Arrays.asList(32, 63));
        put(7, Arrays.asList(64, 127));
        put(8, Arrays.asList(128, 255));
        put(9, Arrays.asList(256, 511));
        put(10, Arrays.asList(512, 1023));
    }};
}
