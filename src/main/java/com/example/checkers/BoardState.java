package com.example.checkers;

public class BoardState {

    private long pieceBoard;
    private long queenBoard;
    private double score;

    public BoardState(long pBoard, long qBoard, double score) {
         this.pieceBoard = pBoard;
         this.queenBoard = qBoard;
         this.score = score;
    }

    public BoardState() {
        this.pieceBoard = 0;
        this.queenBoard = 0;
        this.score = 0;
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

    public void setScore(double score) {
        this.score = score;
    }
}
