package model;

public class Block {
    public static final int STANDARD_BLOCK_SIZE = 8;
    public static final int COMPRESSED_BLOCK_SIZE = 4;
    private double[][] values = new double[8][8];
    private boolean _compressed;

    public Block() {
    }

    public Block(double[][] values) {
        this.values = values;
    }

    public double[][] getValues() {
        return values;
    }

    public void setValues(double[][] values) {
        if (values.length == COMPRESSED_BLOCK_SIZE) {
            this._compressed = true;
        }
        this.values = values;
    }

    public void insert(double value, int i, int j) {
        this.values[i][j] = value;
    }

    public void compress() {
        if (!_compressed) {
            _compressed = true;
            double[][] x = new double[4][4];
            for (int i = 0; i < STANDARD_BLOCK_SIZE; i += 2) {
                for (int j = 0; j < STANDARD_BLOCK_SIZE; j += 2) {
                    x[i / 2][j / 2] = (values[i][j] + values[i + 1][j] + values[i][j + 1] + values[i + 1][j + 1]) / 4;
                }
            }
            this.values = x;
        }
    }

    public void expand() {
        if (_compressed) {
            _compressed = false;
            double[][] x = new double[8][8];
            for (int i = 0; i < STANDARD_BLOCK_SIZE; i += 2) {
                for (int j = 0; j < STANDARD_BLOCK_SIZE; j += 2) {
                    x[i][j] = this.values[i / 2][j / 2];
                    x[i + 1][j] = this.values[i / 2][j / 2];
                    x[i][j + 1] = this.values[i / 2][j / 2];
                    x[i + 1][j + 1] = this.values[i / 2][j / 2];
                }
            }
            this.values = x;
        }
    }

    public void sunstract(int value) {
        int limit = _compressed ? COMPRESSED_BLOCK_SIZE : STANDARD_BLOCK_SIZE;
        for (int i = 0; i < limit; i++) {
            for (int j = 0; j < limit; j++) {
                this.values[i][j] -= value;
            }
        }
    }


    public void add(int value) {
        int limit = _compressed ? COMPRESSED_BLOCK_SIZE : STANDARD_BLOCK_SIZE;
        for (int i = 0; i < limit; i++) {
            for (int j = 0; j < limit; j++) {
                this.values[i][j] += value;
            }
        }
    }


    public void multiply(int value) {
        int limit = _compressed ? COMPRESSED_BLOCK_SIZE : STANDARD_BLOCK_SIZE;
        for (int i = 0; i < limit; i++) {
            for (int j = 0; j < limit; j++) {
                this.values[i][j] *= value;
            }
        }
    }

    public void divide(int value, boolean round) {
        int limit = _compressed ? COMPRESSED_BLOCK_SIZE : STANDARD_BLOCK_SIZE;
        for (int i = 0; i < limit; i++) {
            for (int j = 0; j < limit; j++) {
                this.values[i][j] /= value;
                if (round) {
                    this.values[i][j] = Math.round(this.values[i][j]);
                }
            }
        }
    }

    public void multiply(int[][] value) {
        int limit = _compressed ? COMPRESSED_BLOCK_SIZE : STANDARD_BLOCK_SIZE;
        for (int i = 0; i < limit; i++) {
            for (int j = 0; j < limit; j++) {
                this.values[i][j] *= value[i][j];
            }
        }
    }

    public void divide(int[][] value, boolean round) {
        int limit = _compressed ? COMPRESSED_BLOCK_SIZE : STANDARD_BLOCK_SIZE;
        for (int i = 0; i < limit; i++) {
            for (int j = 0; j < limit; j++) {
                this.values[i][j] /= value[i][j];
                if (round) {
                    this.values[i][j] = Math.round(this.values[i][j]);
                }
            }
        }
    }

    public void round() {
        int limit = _compressed ? COMPRESSED_BLOCK_SIZE : STANDARD_BLOCK_SIZE;
        for (int i = 0; i < limit; i++) {
            for (int j = 0; j < limit; j++) {
                this.values[i][j] = Math.round(this.values[i][j]);
            }
        }
    }

    public static String print2DArray(double[][] matrix) {
        String output = new String();
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                output += " (" + matrix[i][j] + ") ";
            }
            output += "\n";
        }
        return output;
    }
}