package com.example.checkers;

import javafx.fxml.FXML;
import javafx.scene.effect.Bloom;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;


public abstract class QueenPiece extends Piece{

    private static final double SHADOW_SIZE = Piece.SHADOW_RADIUS * 1.25;
    private static final DropShadow DROP_SHADOW = new DropShadow(SHADOW_SIZE, Color.BLACK);

    private static final ImagePattern DARK_QUEEN = new ImagePattern(new Image("C:\\Users\\tomer\\OneDrive\\Documents\\Hermelin\\Checkers-Final\\Checkers\\dark-QUEEN.png"));
    private static final ImagePattern LIGHT_QUEEN = new ImagePattern(new Image("C:\\Users\\tomer\\OneDrive\\Documents\\Hermelin\\Checkers-Final\\Checkers\\light-QUEEN.png"));


    public QueenPiece(short x, short y, boolean dark, History history, Presenter presenter, VisualPlayer visualPlayer) {
        super(x, y, dark, history, presenter, visualPlayer);
    }

    public QueenPiece(Piece piece) {
        super(piece);
    }


}

