package com.example.checkers;

import java.util.ArrayList;

public class VisualPlayer {

    private History history;
    private ArrayList<Piece> pieces;
    private IPresenter presenter;
    private static int counter = 0;
    private int id;
    private static boolean playerCheck = false;  // if he's me + upper board
    private boolean me;
    private boolean myTurn;  // is my turn

    public VisualPlayer(History history) {
        this.history = history;
        this.pieces = new ArrayList<Piece>();
        this.id = ++VisualPlayer.counter;
        this.me = VisualPlayer.playerCheck = !VisualPlayer.playerCheck;
        this.myTurn = true;
    }

    public void setPresenter(IPresenter presenter)
    {
        this.presenter = presenter;
    }

    public void InitiatePiecePosition()
    {
        if (this.me)
        {
            for (short i = 5 ; i < VisualBoard.getDimension() ; i++)
            {
                short k = 0;
                if (i == 6)
                {
                    k = 1;
                }
                for (short j = k; j < VisualBoard.getDimension() ; j += 2)
                {
                    Piece piece = new Piece(j, i, this.me, this.history, this.presenter, this);
                    piece.setPresenter(this.presenter);
                    this.pieces.add(piece);
                }
            }
        }
        else
        {
            for (short i = 0 ; i < 3 ; i++)
            {
                short k = 1;
                if (i == 1)
                {
                    k = 0;
                }
                for (short j = k; j < VisualBoard.getDimension() ; j += 2)
                {
                    Piece piece = new Piece(j, i, this.me, this.history, this.presenter, this);
                    piece.setPresenter(this.presenter);
                    this.pieces.add(piece);
                }
            }
        }
    }

    public ArrayList<Piece> getPieces() {
        return pieces;
    }

    public boolean isMyTurn() {
        return myTurn;
    }

    public void setMyTurn(boolean myTurn) {
        this.myTurn = myTurn;
    }

    public int getId() {
        return id;
    }

    public boolean isMe() {
        return me;
    }
}

