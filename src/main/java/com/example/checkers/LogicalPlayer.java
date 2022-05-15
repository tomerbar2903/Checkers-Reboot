package com.example.checkers;

public class LogicalPlayer {
    private long pieceBoard;  // this current player's board - without queens
    private long queenBoard;  // this current player's queen board
    private long adjacentToRival;  // this player's pieces that are adjacent to the rival
    private long mustEatPieces;  // the player's pieces that have to commit a specific eating move
    private static int counter = 0;
    private int id;
    private boolean dark;  // if this player is the dark one

    // board state properties
    private int pieceAmount;
    private int queenAmount;
    private int safePieces;
    private int safeQueens;
    private int defenderPieces;  // amount of pieces in the lower 2 rows
    private int defenderQueens;
    private int attackingPieces;  // amount of pieces in the upper 3 rows
    private int attackingQueens;
    private int occupiedBottomRow;  // for potential queen making (for rival)

    private static final int INITIAL_PIECE_AMOUNT = 12;
    private static final int INITIAL_QUEEN_AMOUNT = 0;

    private static final int INITIAL_SAFE_PIECES = 6;
    private static final int INITIAL_SAFE_QUEENS = 0;

    private static final int INITIAL_DEFENDER_PIECES = 8;
    private static final int INITIAL_DEFENDER_QUEENS = 0;

    private static final int INITIAL_ATTACKING_PIECES = 0;
    private static final int INITIAL_ATTACKING_QUEENS = 0;

    private static final int INITIAL_OCCUPIED_BOTTOM_ROW = 4;

    public LogicalPlayer()
    {
        this.queenBoard = 0x0000;
        this.mustEatPieces = 0;
        this.adjacentToRival = 0;
        this.id = ++LogicalPlayer.counter;
        this.dark = (this.id == 1);
        this.pieceBoard = (this.dark) ? BitboardEssentials.DARK_INIT : BitboardEssentials.LIGHT_INIT;  // initial board
        this.pieceAmount = LogicalPlayer.INITIAL_PIECE_AMOUNT;
        this.queenAmount = LogicalPlayer.INITIAL_QUEEN_AMOUNT;
        this.safePieces = LogicalPlayer.INITIAL_SAFE_PIECES;
        this.safeQueens = LogicalPlayer.INITIAL_SAFE_QUEENS;
        this.defenderPieces = LogicalPlayer.INITIAL_DEFENDER_PIECES;
        this.defenderQueens = LogicalPlayer.INITIAL_DEFENDER_QUEENS;
        this.attackingPieces = LogicalPlayer.INITIAL_ATTACKING_PIECES;
        this.attackingQueens = LogicalPlayer.INITIAL_ATTACKING_QUEENS;
        this.occupiedBottomRow = LogicalPlayer.INITIAL_OCCUPIED_BOTTOM_ROW;
    }

    public int getOccupiedBottomRow() {
        return occupiedBottomRow;
    }

    public void setOccupiedBottomRow(int occupiedBottomRow) {
        this.occupiedBottomRow = occupiedBottomRow;
    }

    public int getDefenderPieces() {
        return defenderPieces;
    }

    public void setDefenderPieces(int defenderPieces) {
        this.defenderPieces = defenderPieces;
    }

    public int getDefenderQueens() {
        return defenderQueens;
    }

    public void setDefenderQueens(int defenderQueens) {
        this.defenderQueens = defenderQueens;
    }

    public int getAttackingPieces() {
        return attackingPieces;
    }

    public void setAttackingPieces(int attackingPieces) {
        this.attackingPieces = attackingPieces;
    }

    public int getAttackingQueens() {
        return attackingQueens;
    }

    public void setAttackingQueens(int attackingQueens) {
        this.attackingQueens = attackingQueens;
    }

    public int getSafePieces() {
        return safePieces;
    }

    public void setSafePieces(int safePieces) {
        this.safePieces = safePieces;
    }

    public int getSafeQueens() {
        return safeQueens;
    }

    public void setSafeQueens(int safeQueens) {
        this.safeQueens = safeQueens;
    }

    public int getPieceAmount() {
        return pieceAmount;
    }

    public void setPieceAmount(int pieceAmount) {
        this.pieceAmount = pieceAmount;
    }

    public int getQueenAmount() {
        return queenAmount;
    }

    public void setQueenAmount(int queenAmount) {
        this.queenAmount = queenAmount;
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

    public long getTotalBoard() {
        return this.pieceBoard | this.queenBoard;
    }

    public void printDetails(){
        System.out.println("Piece Amount: " + this.pieceAmount);
        System.out.println("Queen Amount: " + this.queenAmount);
        System.out.println("Piece Safe: " + this.safePieces);
        System.out.println("Safe Queens: " + this.safeQueens);
        System.out.println("Defender Pieces: " + this.defenderPieces);
        System.out.println("Defender Queens: " + this.defenderQueens);
        System.out.println("Attacking Pieces: " + this.attackingPieces);
        System.out.println("Attacking Queens: " + this.attackingQueens);
    }
}

