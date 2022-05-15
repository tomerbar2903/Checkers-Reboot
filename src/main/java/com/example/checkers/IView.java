package com.example.checkers;

import java.io.IOException;

public interface IView{
    // moves player from src to dest (makes queen if queen position != null)
    public void move(Position src, Position dest, Position queenPosition);

    // removes disk from board
    public void removePiece(Position piecePosition);

    // makes a player a queen
    public void makeQueen(Position queenPosition);

    // presents win message
    public void winMessage() throws IOException;

    // presents lose message
    public void loseMessage() throws IOException;

    // switch turns
    public void switchTurns();

    // indicates view - invalid move
    public void showInvalidMove(Position invalidMove);

    // show possible paths in an eating chain
    public void showOptions(ArrayList<Position> options);

    // hides possible paths in an eating chain that are already done
    public void hideCurrentOptions();

    // set pieces to be marked and view tiles as landing positions
    public void setMarked(ArrayList<Position> pieces, ArrayList<Position> tiles);
}

