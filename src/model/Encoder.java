package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Encoder {
    private List<Block> luminance = new ArrayList<>();
    private List<Block> chrominanceBlue = new ArrayList<>();
    private List<Block> chrominanceRed = new ArrayList<>();


    public List<List<Block>> encode(PPM ppm) {
        List<List<Block>> result;
        subSample(ppm);
        result = forwardDiscreteCosineTransformation();
        return result;
    }

    private List<List<Block>> forwardDiscreteCosineTransformation() {
        upsample();
        centerValues();
        return computeCoefficientBlocks();
    }

    private List<List<Block>> computeCoefficientBlocks() {
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
        return Arrays.asList(luminance, chrominanceBlue, chrominanceRed);
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
}
