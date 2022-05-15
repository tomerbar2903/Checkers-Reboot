package com.example.checkers;

import javafx.animation.FillTransition;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

public class Piece extends Circle {

    protected Position logicalPosition;
    protected ImagePattern color;
    protected History history;  // holds the piece of the last piece to change
    protected IPresenter presenter;
    protected boolean dark;  // if the piece is dark or light
    protected VisualPlayer owner;  // the owner of the piece
    protected static boolean suggestionMode = false;  // if in an eating chain, no other pieces can be clicked
    protected PieceMode pieceMode;  // for animation - do different thing according to type of piece

    // PROJECT PATH (FOR IMAGES IMPORTED)
    private static final String PROJECT_PATH = System.getProperty("user.dir");

    // EFFECTS FOR GUI
    protected static final double PIECE_RADIUS = Tile.getTileSize() / 2.75;
    protected static final int SCALE_DURATION = 150;
    protected static final double INCREASE_SIZE = 1.25;  // 125% scale
    protected static final double DECREASE_SIZE = 1;  // back to normal
    protected static final double DISAPPEAR = 0;  // disappears from view
    protected static final double SHADOW_RADIUS = 10.0;
    protected static final double TRANSITION_DURATION = 0.5;
    protected static final DropShadow DROP_SHADOW = new DropShadow(Piece.SHADOW_RADIUS, Color.BLACK);
    protected static final ImagePattern DARK_PIECE = new ImagePattern(new Image(PROJECT_PATH + "\\dark.png"));
    protected static final ImagePattern DARK_PIECE_DARKER = new ImagePattern(new Image(PROJECT_PATH + "\\dark-darker.png"));
    protected static final ImagePattern LIGHT_PIECE = new ImagePattern(new Image(PROJECT_PATH + "\\light.png"));
    protected static final ImagePattern LIGHT_PIECE_DARKER = new ImagePattern(new Image(PROJECT_PATH + "\\light-darker.png"));
    protected static final ImagePattern LIGHT_MARKED = new ImagePattern(new Image(PROJECT_PATH + "\\marked-light.png"));
    protected static final ImagePattern DARK_MARKED = new ImagePattern(new Image(PROJECT_PATH + "\\marked-dark.png"));
    protected static final ImagePattern LIGHT_MARKED_DARKER= new ImagePattern(new Image(PROJECT_PATH + "\\marked-light-darker.png"));
    protected static final ImagePattern DARK_MARKED_DARKER = new ImagePattern(new Image(PROJECT_PATH + "\\marked-dark-darker.png"));
    private static final ImagePattern DARK_QUEEN = new ImagePattern(new Image(PROJECT_PATH + "\\dark-QUEEN.png"));
    private static final ImagePattern LIGHT_QUEEN = new ImagePattern(new Image(PROJECT_PATH + "\\light-QUEEN.png"));
    private static final ImagePattern DARK_QUEEN_DARKER = new ImagePattern(new Image(PROJECT_PATH + "\\dark-QUEEN-darker.png"));
    private static final ImagePattern LIGHT_QUEEN_DARKER = new ImagePattern(new Image(PROJECT_PATH + "\\light-QUEEN-darker.png"));
    private static final ImagePattern DARK_QUEEN_MARKED = new ImagePattern(new Image(PROJECT_PATH + "\\dark-QUEEN-marked.png"));
    private static final ImagePattern LIGHT_QUEEN_MARKED = new ImagePattern(new Image(PROJECT_PATH + "\\light-QUEEN-marked.png"));
    private static final ImagePattern DARK_QUEEN_MARKED_DARKER = new ImagePattern(new Image(PROJECT_PATH + "\\dark-QUEEN-marked-darker.png"));
    private static final ImagePattern LIGHT_QUEEN_MARKED_DARKER = new ImagePattern(new Image(PROJECT_PATH + "\\light-QUEEN-marked-darker.png"));


    public Piece(short x, short y, boolean dark, History history, IPresenter presenter, VisualPlayer visualPlayer)
    {
        super();
        this.logicalPosition = new Position(x, y);
        this.presenter = presenter;
        this.pieceMode = PieceMode.REGULAR;
        this.owner = visualPlayer;
        this.dark = dark;
        if (dark)
        {
            this.color = Piece.DARK_PIECE;
        }
        else
        {
            this.color = Piece.LIGHT_PIECE;
        }
        this.setRadius(Piece.PIECE_RADIUS);
        this.setCenterX(x);
        this.setCenterY(y);
        this.setFill(this.color);
        this.setEffect(this.DROP_SHADOW);
        this.history = history;
        this.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (!suggestionMode) {
                    if (owner.isMyTurn()) {
                        if (!history.isEmpty()) {
                            Piece p = (Piece) history.pop();
                            p.undoExpand();
                            history.emptyHistory();
                        }
                        changeOnClick();
                        history.push(getCurrent());
                    }
                }
                else {
                    if (pieceMode == PieceMode.MARKED || pieceMode == PieceMode.MARKED_QUEEN) {  // only marked pieces can be clicked
                        if (owner.isMyTurn()) {
                            if (!history.isEmpty()) {
                                Piece p = (Piece) history.pop();
                                p.undoExpand();
                                history.emptyHistory();
                            }
                            changeOnClick();
                            history.push(getCurrent());
                        }
                    }
                }
            }
        });
    }

    public Piece(Piece piece)
    {
        // copy constructor
        this.setRadius(Piece.PIECE_RADIUS);
        this.setCenterX(piece.getX());
        this.setCenterY(piece.getY());
        this.pieceMode = PieceMode.REGULAR;
        this.color = piece.color;
        this.owner = piece.owner;
        this.presenter = piece.presenter;
        this.history = piece.history;
        this.logicalPosition = piece.logicalPosition;
        this.dark = piece.dark;
        this.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (!suggestionMode) {
                    if (owner.isMyTurn()) {
                        if (!history.isEmpty()) {
                            Piece p = (Piece) history.pop();
                            p.undoExpand();
                            history.emptyHistory();
                        }
                        changeOnClick();
                        history.push(getCurrent());
                    }
                }
                else {
                    if (pieceMode == PieceMode.MARKED) {  // only marked pieces can be clicked
                        if (owner.isMyTurn()) {
                            if (!history.isEmpty()) {
                                Piece p = (Piece) history.pop();
                                p.undoExpand();
                                history.emptyHistory();
                            }
                            changeOnClick();
                            history.push(getCurrent());
                        }
                    }
                }
            }
        });
    }

    public void undoExpand()
    {
        // undoes the changes when another piece is clicked
        ScaleTransition scaleTransition = new ScaleTransition(new Duration(SCALE_DURATION), this);
        scaleTransition.setToX(DECREASE_SIZE);
        scaleTransition.setToY(DECREASE_SIZE);
        scaleTransition.setInterpolator(Interpolator.EASE_BOTH);
        DropShadow d = new DropShadow(SHADOW_RADIUS, Color.BLACK);
        if (this.pieceMode == PieceMode.REGULAR) this.color = (this.dark) ? DARK_PIECE : LIGHT_PIECE;
        if (this.pieceMode == PieceMode.MARKED) this.color = (this.dark) ? DARK_MARKED : LIGHT_MARKED;
        if (this.pieceMode == PieceMode.QUEEN) this.color = (this.dark) ? DARK_QUEEN : LIGHT_QUEEN;
        if (this.pieceMode == PieceMode.MARKED_QUEEN) this.color = (this.dark) ? DARK_QUEEN_MARKED : LIGHT_QUEEN_MARKED;
        this.setFill(this.color);
        this.setEffect(d);
        scaleTransition.play();
    }

    public static void setSuggestionMode(boolean suggestionMode) {
        Piece.suggestionMode = suggestionMode;
    }

    public void setPresenter(IPresenter presenter)
    {
        this.presenter = presenter;
    }

    private Piece getCurrent()
    {
        return this;
    }

    public ScaleTransition expandPiece()
    {
        // shrinks piece nicely from the board
        ScaleTransition scaleTransition = new ScaleTransition(new Duration(SCALE_DURATION), this);
        scaleTransition.setToX(INCREASE_SIZE);
        scaleTransition.setToY(INCREASE_SIZE);
        scaleTransition.setInterpolator(Interpolator.EASE_BOTH);
        return scaleTransition;
    }

    public ScaleTransition shrinkPiece()
    {
        // shrinks piece nicely from the board
        ScaleTransition scaleTransition = new ScaleTransition(new Duration(SCALE_DURATION), this);
        scaleTransition.setToX(DISAPPEAR);
        scaleTransition.setToY(DISAPPEAR);
        scaleTransition.setInterpolator(Interpolator.EASE_BOTH);
        return scaleTransition;
    }

    public void PositionPiece(GridPane gridPane, short x, short y)
    {
        // positions piece on pane
        GridPane.setHalignment(this, HPos.CENTER);
        GridPane.setValignment(this, VPos.CENTER);
        gridPane.add(this, x, y);
    }

    public Position getPositionFromInit(Position dest)
    {
        // converts destination to vectors from initial state

        Position position = new Position();
        int srcX = (int) (this.getLayoutX() / Tile.CHECKERS_TILE_SIZE);
        position.setX((short) ((dest.getX() - srcX)));

        // converts destination to vectors from initial state
        int srcY = (int) (this.getLayoutY() / Tile.CHECKERS_TILE_SIZE);
        position.setY((short) ((dest.getY() - srcY)));

        return position;
    }

    public Position getRelativePositionToInitialState()
    {
        // returns the current relative distance vectors to initial state
        Position relative = new Position();
        int initX = (int) (this.getLayoutX() / Tile.CHECKERS_TILE_SIZE);
        int initY = (int) (this.getLayoutY() / Tile.CHECKERS_TILE_SIZE);
        int nowX = this.logicalPosition.getX();
        int nowY = this.logicalPosition.getY();
        relative.setX((short) ((nowX - initX)));
        relative.setY((short) ((nowY - initY)));
        return relative;
    }

    public TranslateTransition movePiece(short x, short y)
    {
        // moves this piece from its position to (x, y)
        // returns transition to make transitions go one after another (not simultaneously)
        // this.setTranslateX(0);
        // this.setTranslateY(0);
        Position fromInit = this.getPositionFromInit(new Position(x, y));
        Position relativeToInit = this.getRelativePositionToInitialState();
        TranslateTransition tt = new TranslateTransition(Duration.seconds(TRANSITION_DURATION), this);
        tt.setFromX(relativeToInit.getX() * Tile.CHECKERS_TILE_SIZE);
        tt.setFromY(relativeToInit.getY() * Tile.CHECKERS_TILE_SIZE);
        tt.setToX(fromInit.getX() * Tile.CHECKERS_TILE_SIZE);
        tt.setToY(fromInit.getY() * Tile.CHECKERS_TILE_SIZE);
        this.setX(x);
        this.setY(y);
        tt.setInterpolator(Interpolator.EASE_BOTH);
        // GridPane.setHalignment(this, HPos.CENTER);
        // GridPane.setValignment(this, VPos.CENTER);
        return tt;
    }

    public Position getLogicalPosition() {
        return logicalPosition;
    }

    public short getX()
    {
        return this.logicalPosition.getX();
    }

    public short getY()
    {
        return this.logicalPosition.getY();
    }

    public VisualPlayer getOwner() {
        return owner;
    }

    public void setX(short x)
    {
        // this.setCenterX(x);
        // this.setLayoutX(x);
        this.logicalPosition.setX(x);
    }

    public void setY(short y)
    {
        this.logicalPosition.setY(y);
    }

    public boolean getDark()
    {
        return this.dark;
    }

    public void convertIntoQueen()
    {
        // piece.color = (piece.dark) ? DARK_PIECE : LIGHT_PIECE;
        this.color = (this.dark) ? DARK_QUEEN : LIGHT_QUEEN;
        this.setFill(this.color);
        this.setEffect(DROP_SHADOW);
        this.pieceMode = PieceMode.QUEEN;
    }

    private void changeOnClick()
    {
        // changes the tile when he clicks on it
        ScaleTransition scaleTransition = new ScaleTransition(new Duration(SCALE_DURATION), this);
        scaleTransition.setToX(INCREASE_SIZE);
        scaleTransition.setToY(INCREASE_SIZE);
        scaleTransition.setInterpolator(Interpolator.EASE_BOTH);
        if (this.pieceMode == PieceMode.REGULAR) this.color = (this.dark) ? DARK_PIECE_DARKER : LIGHT_PIECE_DARKER;
        if (this.pieceMode == PieceMode.MARKED) this.color = (this.dark) ? DARK_MARKED_DARKER : LIGHT_MARKED_DARKER;
        if (this.pieceMode == PieceMode.QUEEN) this.color = (this.dark) ? DARK_QUEEN_DARKER : LIGHT_QUEEN_DARKER;
        if (this.pieceMode == PieceMode.MARKED_QUEEN) this.color = (this.dark) ? DARK_QUEEN_MARKED_DARKER : LIGHT_QUEEN_MARKED_DARKER;
        this.setFill(this.color);
        DropShadow d = new DropShadow(SHADOW_RADIUS * 1.25, Color.BLACK);
        setEffect(d);
        scaleTransition.play();
    }

    public void markAsMustEat() {
        // changes piece's background to match must eat marking
        this.pieceMode = (this.pieceMode == PieceMode.QUEEN) ? PieceMode.MARKED_QUEEN : PieceMode.MARKED;
        ImagePattern newBackground;
        if (this.pieceMode == PieceMode.MARKED_QUEEN) {
            newBackground = (this.dark) ? DARK_QUEEN_MARKED : LIGHT_QUEEN_MARKED;
        }
        else {
            newBackground = (this.dark) ? DARK_MARKED : LIGHT_MARKED;
        }
        this.setFill(newBackground);
        // insert into owner's suggested pieces queue
        this.owner.getSuggestedPieces().insert(this);
    }

    public void markAsRegular() {
        // changes back the color, and sets mode to regular
        this.pieceMode = (this.pieceMode == PieceMode.MARKED_QUEEN) ? PieceMode.QUEEN : PieceMode.REGULAR;
        ImagePattern preBack;
        if (this.pieceMode == PieceMode.QUEEN) {
            preBack = (this.dark) ? DARK_QUEEN : LIGHT_QUEEN;
        }
        else {
            preBack = (this.dark) ? DARK_PIECE : LIGHT_PIECE;
        }
        this.setFill(preBack);
    }
}

