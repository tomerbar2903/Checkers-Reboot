package com.example.checkers;

import java.io.IOException;

public interface IPresenter {

    // sends move from view to model
    public void sendMoveToCheck(Position src, Position dest, boolean trueRegular, boolean trueEating, boolean isAiMove) throws IOException;

    // sends move from model to view (sends new queen position, 0 if no new queen)
    public void sendMove(Position src, Position dest, long queenPos);

    // continue an eating chain
    public void continueChain(Position destChosen);

    public boolean validMove(VisualPlayer player, Position src, Position dest);

    public boolean validEatingMove(VisualPlayer player, Position src, Position dest);

    public void setMeAsFirstTurn();

    public void generateMoveAI() throws IOException;
}

