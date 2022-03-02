package com.example.checkers;

public class Position {

    private static final int BASE = 2;

    private short x;  // x axis
    private short y;  // y axis

    public Position()
    {
        this.x = 0;
        this.y = 0;
    }

    public Position(short x, short y) {
        this.x = x;
        this.y = y;
    }

    public short getX() {
        return x;
    }

    public void setX(short x) {
        this.x = x;
    }

    public short getY() {
        return y;
    }

    public void setY(short y) {
        this.y = y;
    }

    public boolean equals(Position l2) {
        // returns true, if locations are equal
        return (this.x == l2.x) && (this.y == l2.y);
    }

    public static short convertPosition(int p)
    {
        // converts the amount of pixels to a logical position
        return (short) (p / ((short)Tile.getTileSize()));
    }

    @Override
    public String toString()
    {
        // prints point
        return "(" + this.getX() + ", " + this.getY() + ")";
    }

    public static Position logicalNumberToPosition(long pos) {
        // converts logical number to a position
        Position position = new Position();
        double log1 = Math.log10(pos) / Math.log10(BASE);  // log2(pos)
        short log1Fine = (short) (log1);
        short row = (short) (log1Fine % (VisualBoard.getDimension()));
        short col = (short) (log1Fine / (VisualBoard.getDimension()));
        position.setX(row);
        position.setY(col);
        return position;
    }

    public static long findMiddle(long pos1, long pos2)
    {
        // finds the middle between 2 points on the board
        double exp1 = Math.log10(pos1) / Math.log10(2);  // log2(pos)
        double exp2 = Math.log10(pos2) / Math.log10(2);  // log2(pos)
        int middle = (int) ((exp1 + exp2) / 2);
        return 1L << middle;
    }

    public static long positionToLogicalNumber(Position position) {
        // converts position into a logical number
        short exponent = (short) (position.y * VisualBoard.getDimension() + position.x);
        exponent = (exponent > 0) ? exponent : (short) -exponent;
        long result = 1L << exponent;
        return result;
    }
}

