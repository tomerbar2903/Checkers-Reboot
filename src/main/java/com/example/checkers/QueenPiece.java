package com.example.checkers;

import javafx.fxml.FXML;
import javafx.scene.effect.Bloom;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;


public abstract class QueenPiece extends Piece{

    private static final double SHADOW_SIZE = Piece.SHADOW_RADIUS * 1.25;
    private static final DropShadow DROP_SHADOW = new DropShadow(SHADOW_SIZE, Color.BLACK);

    private static final Color LIGHT_QUEEN = Color.web("0xC3A3B1");
    private static final Color DARK_QUEEN = Color.web("0x685573");

    public QueenPiece(boolean dark, History history, Presenter presenter, VisualPlayer visualPlayer) {
        super(dark, history, presenter, visualPlayer);
    }

    public QueenPiece(short x, short y, boolean dark, History history, Presenter presenter, VisualPlayer visualPlayer) {
        super(x, y, dark, history, presenter, visualPlayer);
    }

    public QueenPiece(Piece piece) {
        super(piece);
    }

    public static void convertIntoQueen(Piece piece)
    {
        // piece.color = (piece.dark) ? DARK_PIECE : LIGHT_PIECE;
        piece.color = (piece.dark) ? DARK_QUEEN : LIGHT_QUEEN;
        piece.setFill(piece.color);
        piece.setEffect(DROP_SHADOW);
    }
}

