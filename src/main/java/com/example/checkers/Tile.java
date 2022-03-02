package com.example.checkers;

import javafx.animation.FillTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class Tile extends Rectangle{

    protected Color color;
    private History history;
    private IPresenter presenter;
    private boolean state;  // if the tile is suggested as an option: false - regular, true  - suggestion
    private static boolean suggestionMode = false;  // if in an eating chain, no other tiles can be clicked
    private static Queue<Tile> suggestedTiles = new Queue<>();

    // FADE CONSTANTS
    protected static final double FILL_TRANSITION_DURATION = 250;
    protected static final Color FILL_COLOR = Color.web("0x7B3829");

    protected static final int CHECKERS_TILE_SIZE = 100;
    protected static final Color TILE_COLOR_DARK = Color.web("0x586B6A");
    protected static final Color TILE_COLOR_LIGHT = Color.web("0xE4D9C8");
    protected static final Color TILE_COLOR_WAIT_FOR_INPUT = Color.web("0x405365");

    public Tile(int width, int height, Color color)
    {
        super(width, height, color);
        this.state = false;
        this.color = color;
    }

    public Tile(boolean dark, History history)
    {
        // dark tile if true, light otherwise
        super(Tile.CHECKERS_TILE_SIZE, Tile.CHECKERS_TILE_SIZE);
        this.history = history;
        this.state = false;
        if (dark)
        {
            this.color = Tile.TILE_COLOR_DARK;
            this.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    // TODO - CHECK IF VALID MOVE (SEND REQUEST)
                    // makes destination position out of event
                    short destX = Position.convertPosition((int) mouseEvent.getSceneX());
                    short destY = Position.convertPosition((int) mouseEvent.getSceneY());
                    Position dest = new Position(destX, destY);
                    if (!history.isEmpty()) {
                        Piece srcPiece = (Piece) history.pop();
                        Position src = srcPiece.getLogicalPosition();
                        srcPiece.undoExpand();
                        presenter.sendMoveToCheck(src, dest);
                        history.emptyHistory();
                    }
                    else if (state && suggestionMode)  // if is currently part of an eating chain
                    {
                        presenter.continueChain(dest);
                    }
                }
            });
        }
        else
        {
            this.color = Tile.TILE_COLOR_LIGHT;
        }
        this.setFill(this.color);
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public static void setSuggestionMode(boolean suggestionMode) {
        Tile.suggestionMode = suggestionMode;
    }

    public void fillTile()
    {
        // changes a color of a specific tile - when invalid destination
        FillTransition fillTransition = new FillTransition(new Duration(FILL_TRANSITION_DURATION), this);
        fillTransition.setToValue(FILL_COLOR);
        fillTransition.play();
        fillTransition.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                // undo the fill transition
                unFillTile();
            }
        });
    }

    public void showAsOption()
    {
        // sets the color to blue, sets the state to true - now it's suggested
        this.state = true;
        FillTransition fillTransition = new FillTransition(new Duration(FILL_TRANSITION_DURATION), this);
        fillTransition.setToValue(TILE_COLOR_WAIT_FOR_INPUT);
        Tile.suggestedTiles.insert(this);  // pushes this tile into the queue, to be removed later
        fillTransition.play();
    }

    public static void hideOptions()
    {
        // empties queue and sets color to be back to normal
        Tile current;
        while (!Tile.suggestedTiles.isEmpty())
        {
            current = Tile.suggestedTiles.remove();
            current.setState(false);
            current.unFillTile();
        }
    }

    public void unFillTile()
    {
        // sets tile as original
        Tile.suggestionMode = false;
        this.state = false;  // not suggested anymore
        FillTransition fillTransition = new FillTransition(new Duration(FILL_TRANSITION_DURATION), this);
        fillTransition.setToValue(TILE_COLOR_DARK);
        fillTransition.play();
    }

    public void setPresenter(IPresenter presenter)
    {
        this.presenter = presenter;
    }

    private Tile getCurrent()
    {
        return this;
    }

    public static int getTileSize()
    {
        return Tile.CHECKERS_TILE_SIZE;
    }
}

