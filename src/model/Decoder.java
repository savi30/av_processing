package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Decoder {
    private List<Block> luminance = new ArrayList<>();
    private List<Block> chrominanceBlue = new ArrayList<>();
    private List<Block> chrominanceRed = new ArrayList<>();
    private List<Integer> idx = new ArrayList<>();

    public List<int[][]> decode(List<List<Integer>> encodedValues, int width, int height) {
        List<List<Block>> decompressedBlocks = decompressBlocks(encodedValues);
        inverseDCT(decompressedBlocks);
        return convertToRGB(width, height);
    }

    private List<List<Block>> decompressBlocks(List<List<Integer>> encodedValues) {
        List<Block> Y = new ArrayList<>();
        List<Block> Cb = new ArrayList<>();
        List<Block> Cr = new ArrayList<>();
        for (int i = 0; i < encodedValues.size(); i += 3) {
            Y.add(getBlock(encodedValues.get(i)));
            Cb.add(getBlock(encodedValues.get(i + 1)));
            Cr.add(getBlock(encodedValues.get(i + 2)));
        }
        return Arrays.asList(Y, Cb, Cr);
    }

    private Block getBlock(List<Integer> integers) {
        double[][] values = new double[Block.STANDARD_BLOCK_SIZE][Block.STANDARD_BLOCK_SIZE];
        int elemtCount = integers.size();
        if (elemtCount <= 4) {
            values[0][0] = integers.get(1);
            return new Block(values);
        }
        boolean direction = false; // false means down true means up
        int i = 0;
        int j = 1;
        int count;
        int k;
        idx.add(0, 2);
        int DC = integers.get(1);
        values[0][0] = DC;

        while (j < Block.STANDARD_BLOCK_SIZE) {
            count = j + 1;
            if (direction) {
                //Going up
                i = j;
                k = 0;
                while (count > 0) {
                    Integer val = getValue(idx, integers, elemtCount);
                    if (val == null) {
                        return new Block(values);
                    }
                    values[i--][k++] = val;
                    count--;
                }
            } else {
                //Going down
                i = 0;
                k = j;
                while (count > 0) {
                    Integer val = getValue(idx, integers, elemtCount);
                    if (val == null) {
                        return new Block(values);
                    }
                    values[i++][k--] = val;
                    count--;
                }
            }
            direction = !direction;
            j++;
        }
        j = 1;
        while (j < Block.STANDARD_BLOCK_SIZE) {
            count = Block.STANDARD_BLOCK_SIZE - j;
            if (direction) {
                //Going up
                i = Block.STANDARD_BLOCK_SIZE - 1;
                k = j;
                while (count > 0) {
                    Integer val = getValue(idx, integers, elemtCount);
                    if (val == null) {
                        return new Block(values);
                    }
                    values[i--][k++] = val;
                    count--;
                }
            } else {
                //Going down
                i = j;
                k = Block.STANDARD_BLOCK_SIZE - 1;
                while (count > 0) {
                    Integer val = getValue(idx, integers, elemtCount);
                    if (val == null) {
                        return new Block(values);
                    }
                    values[i++][k--] = val;
                    count--;
                }
            }
            j++;
            direction = !direction;
        }

        return new Block(values);
    }

    private Integer getValue(List<Integer> idx, List<Integer> integers, int elemtCount) {
        int value;
        int i = idx.get(0);
        if (integers.get(i) > 0) {
            value = 0;
            integers.set(i, integers.get(i) - 1);
        } else {
            value = integers.get(i + 2);
            if (i + 3 >= elemtCount) {
                return null;
            }
            idx.set(0, i + 3);
        }
        return value;
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

    private void inverseDCT(List<List<Block>> encodedBlocks) {
        List<Block> Y = encodedBlocks.get(0);
        List<Block> Cb = encodedBlocks.get(1);
        List<Block> Cr = encodedBlocks.get(2);
        double[][] y_coefficient_block;
        double[][] cb_coefficient_block;
        double[][] cr_coefficient_block;
        for (int i = 0; i < Y.size(); i++) {
            // DeQuantization
            Y.get(i).multiply(Utils.Q);
            Cb.get(i).multiply(Utils.Q);
            Cr.get(i).multiply(Utils.Q);
            y_coefficient_block = new double[8][8];
            cb_coefficient_block = new double[8][8];
            cr_coefficient_block = new double[8][8];
            for (int k = 0; k < Block.STANDARD_BLOCK_SIZE; k++) {
                for (int l = 0; l < Block.STANDARD_BLOCK_SIZE; l++) {
                    y_coefficient_block[k][l] = 0.25 * sumCosInverseDCT(Y.get(i).getValues(), k, l);
                    cb_coefficient_block[k][l] = 0.25 * sumCosInverseDCT(Cb.get(i).getValues(), k, l);
                    cr_coefficient_block[k][l] = 0.25 * sumCosInverseDCT(Cr.get(i).getValues(), k, l);
                }
            }
            Y.set(i, new Block(y_coefficient_block));
            Cb.set(i, new Block(cb_coefficient_block));
            Cr.set(i, new Block(cr_coefficient_block));
            Y.get(i).add(128);
            Cb.get(i).add(128);
            Cr.get(i).add(128);
        }
        luminance = Y;
        chrominanceBlue = Cb;
        chrominanceRed = Cr;
    }

    private void resetCenteredValues() {
        this.luminance.forEach(block -> block.add(128));
        this.chrominanceBlue.forEach(block -> block.add(128));
        this.chrominanceRed.forEach(block -> block.add(128));
    }

    private double sumCosInverseDCT(double[][] values, int k, int l) {
        double result = 0.0;
        for (int i = 0; i < Block.STANDARD_BLOCK_SIZE; i++) {
            for (int j = 0; j < Block.STANDARD_BLOCK_SIZE; j++) {
                result += values[i][j]
                        * alpha(i) * alpha(j)
                        * Math.cos(((2 * k + 1) * i * Math.PI) / 16) * Math.cos(((2 * l + 1) * j * Math.PI) / 16);
            }
        }
        return result;
    }

    private int clamp(int n, int minVal, int maxVal) {
        return Math.max(Math.min(n, maxVal), minVal);
    }

    private double alpha(int n) {
        return n > 0 ? 1 : 1 / Math.sqrt(2.0);
    }

}
