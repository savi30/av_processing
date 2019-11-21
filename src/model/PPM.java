package model;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PPM {
    private List<int[][]> RGB = new ArrayList<>();
    private List<double[][]> YCrCb = new ArrayList<>();
    private int width;
    private int height;
    private int maxValue;
    private String info;
    private String format;

    public List<int[][]> getRGB() {
        return RGB;
    }

    public void setRGB(List<int[][]> RGB) {
        this.RGB = RGB;
    }

    public List<double[][]> getYCrCb() {
        return YCrCb;
    }

    public void setYCrCb(List<double[][]> YCrCb) {
        this.YCrCb = YCrCb;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public static PPM fromFile(String fileName) throws IOException {
        Path path = Paths.get(fileName);
        BufferedReader bufferedReader = Files.newBufferedReader(path);
        PPM ppm = new PPM();
        ppm.setFormat(bufferedReader.readLine().strip());
        ppm.setInfo(bufferedReader.readLine().strip());
        String[] dimensions = bufferedReader.readLine().strip().split(" ");
        ppm.setWidth(Integer.parseInt(dimensions[0]));
        ppm.setHeight(Integer.parseInt(dimensions[1]));
        ppm.setMaxValue(Integer.parseInt(bufferedReader.readLine().strip()));
        ppm.YCrCb.add(new double[600][800]);
        ppm.YCrCb.add(new double[600][800]);
        ppm.YCrCb.add(new double[600][800]);
        for (int i = 0; i < ppm.getHeight(); i++) {
            for (int j = 0; j < ppm.getWidth(); j++) {
                int R = Integer.parseInt(bufferedReader.readLine().strip());
                int G = Integer.parseInt(bufferedReader.readLine().strip());
                int B = Integer.parseInt(bufferedReader.readLine().strip());
                ppm.YCrCb.get(0)[i][j] = 0.299 * R + 0.587 * G + 0.114 * B;
                ppm.YCrCb.get(1)[i][j] = 128 - 0.168736 * R - 0.331264 * G + 0.5 * B;
                ppm.YCrCb.get(2)[i][j] = 128 + 0.5 * R - 0.418688 * G - 0.081312 * B;
            }
        }
        return ppm;
    }
}
