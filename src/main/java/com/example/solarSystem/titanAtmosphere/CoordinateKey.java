package com.example.solarSystem.titanAtmosphere;

public class CoordinateKey {

    private final int row;
    private final int col;

    public CoordinateKey(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CoordinateKey)) return false;
        CoordinateKey other = (CoordinateKey) o;
        return this.row == other.row && this.col == other.col;
    }

    @Override
    public int hashCode() {
        return 31 * row + col;
    }

    @Override
    public String toString() {
        return "(" + row + ", " + col + ")";
    }


}

