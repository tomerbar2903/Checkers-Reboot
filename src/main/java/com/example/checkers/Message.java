package com.example.checkers;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.*;

public class Message extends Text {

    protected String mainMessage;
    protected Color mainColor;


    protected static final Color WIN_MESSAGE_COLOR = Color.web("0x3D6188");
    protected static final Color LOSE_MESSAGE_COLOR = Color.web("0x974747");
    protected static final String WIN_MESSAGE = "YOU WON!";
    protected static final String LOSE_MESSAGE = "YOU LOST...";
    protected static final Font FONT = Font.font("Eras Bold ITC", FontWeight.SEMI_BOLD, FontPosture.REGULAR, 12);
    protected static final double SHADOW_SIZE = 10;
    protected static final DropShadow DROP_SHADOW = new DropShadow(SHADOW_SIZE, Color.BLACK);

    public Message(boolean winLose)
    {
        // winLose (true - won, false - lost)
        super();
        this.mainMessage = (winLose) ? WIN_MESSAGE : LOSE_MESSAGE;
        this.setText(this.mainMessage);
        this.mainColor = (winLose) ? WIN_MESSAGE_COLOR : LOSE_MESSAGE_COLOR;
        this.setFill(this.mainColor);
        this.setFont(FONT);
        this.setScaleX(7);
        this.setScaleY(9);
        this.setTextAlignment(TextAlignment.CENTER);
        this.setStroke(Color.BLACK);
        this.setStrokeWidth(0.3);
        this.setStrokeType(StrokeType.CENTERED);
        this.setStrokeLineJoin(StrokeLineJoin.ROUND);
        this.setEffect(DROP_SHADOW);
    }

    public void positionMessage(GridPane gridPane)
    {
        // positions message onto board
        GridPane.setHalignment(this, HPos.LEFT);
        GridPane.setValignment(this, VPos.CENTER);
        gridPane.add(this, VisualBoard.getDimension() / 2, VisualBoard.getDimension() / 2);
    }

}

