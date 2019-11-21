package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Decoder {
    private List<Block> luminance = new ArrayList<>();
    private List<Block> chrominanceBlue = new ArrayList<>();
    private List<Block> chrominanceRed = new ArrayList<>();

    public List<int[][]> decode(List<List<Block>> encodedBlocks, int width, int height) {
        this.luminance = encodedBlocks.get(0);
        this.chrominanceBlue = encodedBlocks.get(1);
        this.chrominanceRed = encodedBlocks.get(2);
        upsample();
        return convertToRGB(width, height);
    }

    private void upsample() {
        this.chrominanceRed.forEach(Block::expand);
        this.chrominanceBlue.forEach(Block::expand);
    }

    private List<int[][]> convertToRGB(int width, int height) {
        int[][] R = new int[600][800];
        int[][] G = new int[600][800];
        int[][] B = new int[600][800];
        int pos = 0;
        for (int i = 0; i < height; i += Block.STANDARD_BLOCK_SIZE) {
            for (int j = 0; j < width; j += Block.STANDARD_BLOCK_SIZE) {
                for (int k = 0; k < Block.STANDARD_BLOCK_SIZE; k++) {
                    for (int l = 0; l < Block.STANDARD_BLOCK_SIZE; l++) {
                        double y = luminance.get(pos).getValues()[k][l];
                        double cb = chrominanceBlue.get(pos).getValues()[k][l];
                        double cr = chrominanceRed.get(pos).getValues()[k][l];

                        R[i + k][j + l] = clamp((int) (y + 1.402 * (cr - 128)), 0, 255);
                        G[i + k][j + l] = clamp((int) (y - 0.344136 * (cb - 128) - 0.714136 * (cr - 128)), 0, 255);
                        B[i + k][j + l] = clamp((int) (y + 1.7790 * (cb - 128)), 0, 255);
                    }
                }
                pos++;
            }
        }
        return Arrays.asList(R, G, B);
    }

    private int clamp(int n, int minVal, int maxVal) {
        return Math.max(Math.min(n, maxVal), minVal);
    }


}
