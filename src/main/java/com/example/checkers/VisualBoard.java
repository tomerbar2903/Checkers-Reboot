package com.example.checkers;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;
import java.util.Iterator;

public class VisualBoard extends GridPane{

    private History history;
    private IPresenter presenter;

    private Tile[][] gameBoard;  // tile board
    private static final int DIMENSION = 8;  // the dimension of the board
    private int startPosition;  // the starting position of the center of the first tile

    public VisualBoard()
    {
        super();
        this.history = new History();
        this.gameBoard = new Tile[VisualBoard.DIMENSION][VisualBoard.DIMENSION];
        this.startPosition = Tile.getTileSize() / 2;
    }

    public void setPresenter(IPresenter presenter)
    {
        this.presenter = presenter;
    }

    public void InitiateBoard()
    {
        // initiates board's tiles
        int i, j;
        boolean darkFlag = false;
        int positionX = this.startPosition, positionY = this.startPosition;
        for (i = 0 ; i < this.gameBoard.length ; i++)
        {
            for (j = 0 ; j < this.gameBoard[i].length ; j++)
            {
                Tile tile = new Tile(darkFlag, this.history);
                tile.setPresenter(this.presenter);
                this.gameBoard[i][j] = tile;
                darkFlag = !darkFlag;
                // this.gameBoard[i][j].setLayoutX(positionX);
                // this.gameBoard[i][j].setLayoutY(positionY);
                positionX += Tile.getTileSize();
            }
            positionY += Tile.getTileSize();
            positionX = this.startPosition;
            darkFlag = !darkFlag;
        }
    }

    public static int getDimension()
    {
        return VisualBoard.DIMENSION;
    }

    public void placeBoard(final int i, final int j)
    {
        getChildren().add(this.gameBoard[i][j]);
    }

    public void placePlayerPieces(GridPane gridPane, VisualPlayer player)
    {
        // places a piece of a player on the wall
        ArrayList<Piece> pieces = player.getPieces();
        for (int i = 0 ; i < pieces.size() ; i++)
        {
            Piece p = pieces.get(i);
            p.PositionPiece(gridPane, p.getX(), p.getY());
        }
    }

    public Tile[][] getGameBoard() {
        return gameBoard;
    }

    public Tile getTile(short x, short y)
    {
        return this.gameBoard[x][y];
    }

    public History getHistory() {
        return history;
    }
}

