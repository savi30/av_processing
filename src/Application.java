import model.Block;
import model.Decoder;
import model.Encoder;
import model.PPM;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Application {

    public static void main(String... args) throws IOException {
        PPM ppm = PPM.fromFile("src/data/nt-P3.ppm");
        Encoder encoder = new Encoder();
        List<List<Integer>> encodedBlocks = encoder.encode(ppm);
        Decoder decoder = new Decoder();
        List<int[][]> imageData = decoder.decode(encodedBlocks, ppm.getWidth(), ppm.getHeight());
        saveImage(imageData, ppm.getHeight(), ppm.getWidth(), ppm.getFormat(), ppm.getInfo(), ppm.getMaxValue());
    }

    private static void saveImage(List<int[][]> imageData, int height, int width, String format, String info, int maxValue) throws IOException {
        File file = new File("src/data/img.ppm");
        file.createNewFile();
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, false));
        bufferedWriter.append(format + "\n");
        bufferedWriter.append(info + "\n");
        bufferedWriter.append(width + " " + height + "\n");
        bufferedWriter.append(maxValue + "\n");
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                bufferedWriter.append(imageData.get(0)[i][j] + "\n");
                bufferedWriter.append(imageData.get(1)[i][j] + "\n");
                bufferedWriter.append(imageData.get(2)[i][j] + "\n");
            }
        }
        bufferedWriter.close();
    }


}
