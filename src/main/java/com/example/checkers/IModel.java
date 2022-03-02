package com.example.checkers;

public interface IModel {

    // returns true if move is valid for player - and does the move
    public boolean validMove(LogicalPlayer player, long src, long dest);

    // checks if a move created a queen - return position (if true), 0 (if false)
    public long checkQueen(LogicalPlayer logicalPlayer, long dest);

    // switch turns
    public void switchTurns();
}
