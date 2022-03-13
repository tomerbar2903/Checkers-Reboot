package com.example.checkers;

public interface IPresenter {

    // sends move from view to model
    public void sendMoveToCheck(Position src, Position dest, boolean trueRegular, boolean trueEating);

    // sends move from model to view (sends new queen position, 0 if no new queen)
    public void sendMove(Position src, Position dest, long queenPos);

    // send to the view - makes the piece in the position
    public void makeQueen(Position piece);

    // send to view - display win message
    public void winMessage();

    // send to view - display lose message
    public void loseMessage();

    // continue an eating chain
    public void continueChain(Position destChosen);

    public boolean validMove(VisualPlayer player, Position src, Position dest);

    public boolean validEatingMove(VisualPlayer player, Position src, Position dest);

    public void setMeAsFirstTurn();
}

