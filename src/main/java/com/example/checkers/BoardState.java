package com.example.checkers;

public class BoardState {

    private long pieceBoard;
    private long queenBoard;
    private int score;

    public static final int MAX_INT = 2147483647;
    public static final int MIN_INT = -2147483648;

    public BoardState(long pBoard, long qBoard, int score) {
         this.pieceBoard = pBoard;
         this.queenBoard = qBoard;
         this.score = score;
    }

    public BoardState(boolean minmax) {
        // true - max, false - min
        this.score = (minmax) ? MIN_INT : MAX_INT;
        this.queenBoard = 0;
        this.pieceBoard = 0;
    }

    public BoardState(BoardState boardState) {
        this.pieceBoard = boardState.pieceBoard;
        this.queenBoard = boardState.queenBoard;
        this.score = boardState.score;
    }

    public BoardState() {
        this.pieceBoard = 0;
        this.queenBoard = 0;
        this.score = 0;
    }

    public void update(BoardState boardState) {
        this.pieceBoard = boardState.pieceBoard;
        this.queenBoard = boardState.queenBoard;
        this.score = boardState.score;
    }

    public long getPieceBoard() {
        return pieceBoard;
    }

    public void setPieceBoard(long pieceBoard) {
        this.pieceBoard = pieceBoard;
    }

    public long getQueenBoard() {
        return queenBoard;
    }

    public void setQueenBoard(long queenBoard) {
        this.queenBoard = queenBoard;
    }

    public double getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
