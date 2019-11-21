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
        return Arrays.asList(luminance, chrominanceBlue, chrominanceRed);
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
}
