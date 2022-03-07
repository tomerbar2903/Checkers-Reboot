package com.example.checkers;

public class LogicalPlayer {
    private long pieceBoard;  // this current player's board - without queens
    private long queenBoard;  // this current player's queen board
    private long adjacentToRival;  // this player's pieces that are adjacent to the rival
    private long mustEatPieces;  // the player's pieces that have to commit a specific eating move
    private static int counter = 0;
    private int id;
    private boolean dark;  // if this player is the dark one

    public LogicalPlayer()
    {
        this.queenBoard = 0x0000;
        this.mustEatPieces = 0;
        this.adjacentToRival = 0;
        this.id = ++LogicalPlayer.counter;
        this.dark = (this.id == 1);
        this.pieceBoard = (this.dark) ? BitboardEssentials.DARK_INIT : BitboardEssentials.LIGHT_INIT;  // initial board
    }

    public boolean isDark() {
        return dark;
    }

    public void setPieceBoard(long pieceBoard) {
        this.pieceBoard = pieceBoard;
    }

    public long getAdjacentToRival() {
        return adjacentToRival;
    }

    public void setAdjacentToRival(long adjacentToRival) {
        this.adjacentToRival = adjacentToRival;
    }

    public long getMustEatPieces() {
        return mustEatPieces;
    }

    public void setMustEatPieces(long adjacentRivalBoard) {
        this.mustEatPieces = adjacentRivalBoard;
    }

    public long getPieceBoard() {
        return pieceBoard;
    }

    public long getQueenBoard() {
        return queenBoard;
    }

    public static int getCounter() {
        return counter;
    }

    public int getId() {
        return id;
    }

    public void setQueenBoard(long queenBoard) {
        this.queenBoard = queenBoard;
    }

    public void printBoard()
    {
        // prints board
        long board = this.pieceBoard;
        for (int i = 0 ; i < VisualBoard.getDimension() * VisualBoard.getDimension() ; i++)
        {
            System.out.print(board % 2 + "\t");
            board = board >> 1;
            if ((i + 1) % VisualBoard.getDimension() == 0)
            {
                System.out.println();
            }
        }
        System.out.println("\n\n");
    }
}

