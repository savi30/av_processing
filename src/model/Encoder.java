package model;

import java.util.ArrayList;
import java.util.List;

public class Encoder {
    private List<Block> luminance = new ArrayList<>();
    private List<Block> chrominanceBlue = new ArrayList<>();
    private List<Block> chrominanceRed = new ArrayList<>();


    public List<List<Integer>> encode(PPM ppm) {
        List<List<Block>> result;
        subSample(ppm);
        forwardDiscreteCosineTransformation();
        return compress();
    }

    private void forwardDiscreteCosineTransformation() {
        upsample();
        centerValues();
        computeCoefficientBlocks();
    }

    private List<List<Integer>> compress() {
        List<List<Integer>> compressedBlocks = new ArrayList<>();
        for (int i = 0; i < luminance.size(); i++) {
            compressedBlocks.add(compressArray(zigZagParse(luminance.get(i))));
            compressedBlocks.add(compressArray(zigZagParse(chrominanceBlue.get(i))));
            compressedBlocks.add(compressArray(zigZagParse(chrominanceRed.get(i))));
        }
        return compressedBlocks;
    }

    private List<Integer> compressArray(List<Double> values) {
        List<Integer> result = new ArrayList<>();
        result.add(getSize(values.get(0)));
        result.add(values.get(0).intValue());
        int runLength = 0;
        for (int i = 1; i < values.size(); i++) {
            if (values.get(i) == 0) {
                runLength++;
                continue;
            }
            result.add(runLength);
            result.add(getSize(values.get(i)));
            result.add(values.get(i).intValue());
            runLength = 0;
        }
        if (runLength == 63) {
            result.add(0);
            result.add(0);
        }
        return result;
    }

    private List<Double> zigZagParse(Block block) {
        List<Double> result = new ArrayList<>();
        double[][] values = block.getValues();
        int i = 0;
        int j = 0;
        boolean direction = true; // false means down true means up
        int count;
        int k;
        while (j < Block.STANDARD_BLOCK_SIZE) {
            count = j + 1;
            if (direction) {
                //Going up
                i = j;
                k = 0;
                while (count > 0) {
                    result.add(values[i--][k++]);
                    count--;
                }
            } else {
                //Going down
                i = 0;
                k = j;
                while (count > 0) {
                    result.add(values[i++][k--]);
                    count--;
                }
            }
            j++;
            direction = !direction;
        }
        j = 1;
        while (j < Block.STANDARD_BLOCK_SIZE) {
            count = Block.STANDARD_BLOCK_SIZE - j;
            if (direction) {
                //Going up
                i = Block.STANDARD_BLOCK_SIZE - 1;
                k = j;
                while (count > 0) {
                    result.add(values[i--][k++]);
                    count--;
                }
            } else {
                //Going down
                i = j;
                k = Block.STANDARD_BLOCK_SIZE - 1;
                while (count > 0) {
                    result.add(values[i++][k--]);
                    count--;
                }
            }
            j++;
            direction = !direction;
        }
        return result;
    }


    private void computeCoefficientBlocks() {
        double[][] y_coefficient_block;
        double[][] cb_coefficient_block;
        double[][] cr_coefficient_block;
        for (int i = 0; i < luminance.size(); i++) {
            y_coefficient_block = new double[8][8];
            cb_coefficient_block = new double[8][8];
            cr_coefficient_block = new double[8][8];
            for (int k = 0; k < Block.STANDARD_BLOCK_SIZE; k++) {
                for (int l = 0; l < Block.STANDARD_BLOCK_SIZE; l++) {
                    y_coefficient_block[k][l] = getCoefficient(luminance.get(i).getValues(), k, l);
                    cb_coefficient_block[k][l] = getCoefficient(chrominanceBlue.get(i).getValues(), k, l);
                    cr_coefficient_block[k][l] = getCoefficient(chrominanceRed.get(i).getValues(), k, l);

                }
            }
            // Quantization
            this.luminance.set(i, quantize(new Block(y_coefficient_block)));
            this.chrominanceBlue.set(i, quantize(new Block(cb_coefficient_block)));
            this.chrominanceRed.set(i, quantize(new Block(cr_coefficient_block)));
        }
    }

    private Block quantize(Block coefficient_block) {
        coefficient_block.divide(Utils.Q, true);
        return coefficient_block;
    }

    private double getCoefficient(double[][] values, int k, int l) {
        return 0.25 * alpha(k) * alpha(l) * sumCosDCT(values, k, l);
    }

    private void upsample() {
        chrominanceBlue.forEach(Block::expand);
        chrominanceRed.forEach(Block::expand);
    }

    private void subSample(PPM ppm) {
        Block y_block;
        Block cb_block;
        Block cr_block;
        for (int i = 0; i < ppm.getHeight(); i += Block.STANDARD_BLOCK_SIZE) {
            for (int j = 0; j < ppm.getWidth(); j += Block.STANDARD_BLOCK_SIZE) {
                y_block = new Block();
                cb_block = new Block();
                cr_block = new Block();
                for (int k = 0; k < Block.STANDARD_BLOCK_SIZE; k++) {
                    for (int l = 0; l < Block.STANDARD_BLOCK_SIZE; l++) {
                        y_block.insert(ppm.getYCrCb().get(0)[i + k][j + l], k, l);
                        cb_block.insert(ppm.getYCrCb().get(1)[i + k][j + l], k, l);
                        cr_block.insert(ppm.getYCrCb().get(2)[i + k][j + l], k, l);
                    }
                }
                luminance.add(y_block);
                chrominanceBlue.add(cb_block);
                chrominanceRed.add(cr_block);
            }
        }
        chrominanceBlue.forEach(Block::compress);
        chrominanceRed.forEach(Block::compress);
    }

    private void centerValues() {
        this.luminance.forEach(block -> block.sunstract(128));
        this.chrominanceBlue.forEach(block -> block.sunstract(128));
        this.chrominanceRed.forEach(block -> block.sunstract(128));
    }

    private double sumCosDCT(double[][] values, int k, int l) {
        double result = 0.0;
        for (int i = 0; i < Block.STANDARD_BLOCK_SIZE; i++) {
            for (int j = 0; j < Block.STANDARD_BLOCK_SIZE; j++) {
                result += values[i][j] * Math.cos(((2 * i + 1) * k * Math.PI) / 16) * Math.cos(((2 * j + 1) * l * Math.PI) / 16);
            }
        }
        return result;
    }

    private double alpha(int n) {
        return n > 0 ? 1 : 1 / Math.sqrt(2.0);
    }

    private Integer getSize(Double aDouble) {
        int value = aDouble.intValue();
        if (value == Utils.amplitudes.get(1).get(0) || value == (Utils.amplitudes.get(1).get(0) * -1)) {
            return 1;
        } else {
            final int[] amp = {1};
            Utils.amplitudes.entrySet().stream().skip(1).forEach(entry -> {
                if ((value > entry.getValue().get(0) && value < entry.getValue().get(1))
                        || (value > entry.getValue().get(1) * -1 && value < entry.getValue().get(0) * -1)) {
                    amp[0] = entry.getKey();
                }
            });
            return amp[0];
        }
    }
}
