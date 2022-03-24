package com.example.checkers;

import javafx.geometry.Pos;

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

    public static Position findMiddle(Position pos1, Position pos2)
    {
        // finds the middle between 2 points on the board
        return new Position((short) ((pos1.getX() + pos2.getX()) / 2), (short) ((pos1.getY() + pos2.getY()) / 2));
    }

    public static long positionToLogicalNumber(Position position) {
        // converts position into a logical number
        short exponent = (short) (position.y * VisualBoard.getDimension() + position.x);
        exponent = (exponent > 0) ? exponent : (short) -exponent;
        long result = 1L << exponent;
        return result;
    }

    public static long[] extractAdjacentPositions(long bitwisePositions) {
        // extracts all adjacent (existent) positions into an array (size of 4 - max adjacent)
        /*
        CALCULATION:
        1. take the second log out of bitwisePositions. cast To int
        2. add result to array
        3. subtract result from bitwisePositions
        4. repeat until bitwisePositions = 0 (at most - 4 times -> in case of surrounded queen)
         */
        long[] positions = new long[4];
        int index = 0;
        for (; index < 4 && bitwisePositions > 0 ; index++) {
            int place = (int) BitboardEssentials.log2(bitwisePositions);
            long pos = 1L << place;
            positions[index] = pos;
            bitwisePositions -= pos;
        }
        return positions;
    }

    public static ArrayList<Long> extractPositionsLong(long bitPositions) {
        // extracts all positions into an array list
        /*
        CALCULATION:
        1. take the second log out of bitwisePositions. cast To int
        2. add result to array
        3. subtract result from bitwisePositions
        4. repeat until bitwisePositions = 0
         */
        ArrayList<Long> positions = new ArrayList<>();
        while (bitPositions > 0) {
            int place = (int) BitboardEssentials.log2(bitPositions);
            long pos = 1L << place;
            positions.add(pos);
            bitPositions -= pos;
        }
        return positions;
    }

    public static ArrayList<Position> extractPositions(long bitPositions) {
        // extracts all positions into an array list
        /*
        CALCULATION:
        1. take the second log out of bitwisePositions. cast To int
        2. add result to array
        3. subtract result from bitwisePositions
        4. repeat until bitwisePositions = 0
         */
        ArrayList<Position> positions = new ArrayList<>();
        while (bitPositions > 0) {
            int place = (int) BitboardEssentials.log2(bitPositions);
            long pos = 1L << place;
            positions.add(Position.logicalNumberToPosition(pos));
            bitPositions -= pos;
        }
        return positions;
    }
}

