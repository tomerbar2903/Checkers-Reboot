package com.example.checkers;

import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.io.IOException;

public class View extends Application implements IView {

    private VisualBoard board;
    private VisualPlayer visualPlayer1;
    private VisualPlayer visualPlayer2;
    private VisualPlayer currentTurn;
    private IPresenter presenter;

    private TransitionHandler transitionHandler;

    private static final double QUEEN_TRANSITION_DURATION = 0.55;
    private static final String ICON_PATH = "C:\\Users\\tomer\\OneDrive\\Documents\\Hermelin\\Checkers\\src\\Logo.png";
    private static final String WIN_MESSAGE = "YOU WON!";
    private static final String LOSE_MESSAGE = "YOU LOST...";

    public View() {
        super();
        this.presenter = null;
        this.board = new VisualBoard();
        this.visualPlayer1 = new VisualPlayer(this.board.getHistory());
        this.visualPlayer2 = new VisualPlayer(this.board.getHistory());
        this.currentTurn = this.visualPlayer2;
        this.visualPlayer1.setMyTurn(false);
        this.transitionHandler = new TransitionHandler();
    }

    public void setMeAsFirstTurn() {
        this.visualPlayer1.setMyTurn(true);
        this.currentTurn = this.visualPlayer1;
        this.visualPlayer2.setMyTurn(false);
        this.presenter.setMeAsFirstTurn();
    }

    public void setPresenter(IPresenter presenter) {
        this.presenter = presenter;
        this.visualPlayer1.setPresenter(this.presenter);
        this.visualPlayer2.setPresenter(this.presenter);
        this.board.setPresenter(this.presenter);
    }

    public Piece searchPiece(Position position) {
        // returns the piece in position, null if it doesn't exist
        ArrayList<Piece> arr1 = this.visualPlayer1.getPieces();
        ArrayList<Piece> arr2 = this.visualPlayer2.getPieces();
        Piece p;
        for (int i = 0; i < arr1.size(); i++) {
            p = arr1.get(i);
            if (p.getLogicalPosition().equals(position)) {
                return p;
            }
        }
        for (int i = 0; i < arr2.size(); i++) {
            p = arr2.get(i);
            if (p.getLogicalPosition().equals(position)) {
                return p;
            }
        }
        return null;
    }

    public void assignPiecesToPlayers() {
        // assigns all relevant pieces
        this.visualPlayer1.initiatePiecePosition();
        this.visualPlayer2.initiatePiecePosition();
    }

    @Override
    public void move(Position src, Position dest, Position queenPosition) {
        // moves piece from source to destination
        Piece piece1 = searchPiece(src);
        piece1.toFront();
        TranslateTransition translateTransition = piece1.movePiece(dest.getX(), dest.getY());
        // transition at the end
        translateTransition.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                // so that it doesn't run A-synced
                if (queenPosition != null) {
                    makeQueen(queenPosition);
                }
            }
        });
        this.transitionHandler.insert(translateTransition);
    }

    @Override
    public void showInvalidMove(Position invalidMove) {
        // pops tile as red
        this.board.getGameBoard()[invalidMove.getX()][invalidMove.getY()].fillTile();  // the tile in the position should turn red
    }

    @Override
    public void removePiece(Position piecePosition) {
        // removes a piece from the board
        Piece p = searchPiece(piecePosition);
        ScaleTransition scaleTransition = p.expandPiece();
        ScaleTransition shrink = p.shrinkPiece();
        shrink.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                board.getChildren().remove(p);
            }
        });
        scaleTransition.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                shrink.play();
            }
        });
        this.transitionHandler.insert(scaleTransition);
        p.getOwner().getPieces().remove(p);
    }

    @Override
    public void showOptions(ArrayList<Position> options) {
        // shows the options, turns off click-ability of all others
        Tile.setSuggestionMode(true);
        Piece.setSuggestionMode(true);  // no piece can be clicked
        for (int i = 0; i < options.size(); i++) {
            Position position = options.get(i);
            this.board.getGameBoard()[position.getX()][position.getY()].showAsOption(TileState.CHAIN);
        }
    }

    @Override
    public void hideCurrentOptions() {
        // make tiles regular again, set modes to true - no suggesting any more
        // tiles and pieces can be clicked again
        Tile.setSuggestionMode(false);
        Piece.setSuggestionMode(false);
        Tile.hideOptions();
    }

    @Override
    public void makeQueen(Position queenPosition) {
        // converts a piece into a queen
        Piece piece = this.searchPiece(queenPosition);

        // sets up transition
        TranslateTransition xchange1 = new TranslateTransition(Duration.seconds(QUEEN_TRANSITION_DURATION));
        xchange1.setNode(piece);

        // sets src and dest positions of first transition
        Position src, dest;
        if (piece.getDark()) {
            dest = new Position(piece.getX(), (short) (piece.getY() - 1));
        } else {
            dest = new Position(piece.getX(), (short) (piece.getY() + 1));
        }

        // changes position to fit initial state
        src = piece.getRelativePositionToInitialState();
        dest = piece.getPositionFromInit(dest);

        // sets start position of transition
        xchange1.setFromX(src.getX() * Tile.CHECKERS_TILE_SIZE);
        xchange1.setFromY(src.getY() * Tile.CHECKERS_TILE_SIZE);

        // sets end position of transition
        xchange1.setToX(dest.getX() * Tile.CHECKERS_TILE_SIZE);
        xchange1.setToY(dest.getY() * Tile.CHECKERS_TILE_SIZE);
        TranslateTransition moveBack = this.moveBackQueen(piece);
        xchange1.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                QueenPiece.convertIntoQueen(piece);  // converts visually to a queen
                moveBack.play();
            }
        });
        this.transitionHandler.insert(xchange1);
    }

    public TranslateTransition moveBackQueen(Piece queen) {
        // moves it to original position
        TranslateTransition xchange1 = new TranslateTransition(Duration.seconds(QUEEN_TRANSITION_DURATION));
        xchange1.setNode(queen);
        Position src = queen.getRelativePositionToInitialState(), dest;
        if (queen.getDark()) {
            dest = new Position(queen.getX(), (short) (queen.getY()));
            src.setY((short) (src.getY() - 1));
        } else {
            dest = new Position(queen.getX(), (short) (queen.getY()));
            src.setY((short) (src.getY() + 1));
        }
        dest = queen.getPositionFromInit(dest);

        xchange1.setFromX(src.getX() * Tile.CHECKERS_TILE_SIZE);
        xchange1.setFromY(src.getY() * Tile.CHECKERS_TILE_SIZE);

        xchange1.setToX(dest.getX() * Tile.CHECKERS_TILE_SIZE);
        xchange1.setToY(dest.getY() * Tile.CHECKERS_TILE_SIZE);
        return xchange1;
    }

    public VisualPlayer getCurrentTurn() {
        // returns current player turn
        return this.currentTurn;
    }


    @Override
    public void switchTurns() {
        // places the next turn at top of queue
        if (this.currentTurn.equals(this.visualPlayer1)) {
            this.currentTurn.setMyTurn(false);  // turns off buttons for player 1
            this.currentTurn = this.visualPlayer2;  // changes current player
            this.currentTurn.setMyTurn(true);  // turns its buttons on
        } else if (this.currentTurn.equals(this.visualPlayer2)) {
            this.currentTurn.setMyTurn(false);  // turns off buttons for player 2
            this.currentTurn = this.visualPlayer1;  // changes current player
            this.currentTurn.setMyTurn(true);  // turns its buttons on
        }
    }

    @Override
    public void presentBoard() throws IOException {
        Stage stage = new Stage();
        this.start(stage);
    }

    @Override
    public void winMessage() throws IOException {
        // adds winning message on the screen
        Stage stage1 = new Stage();
        FXMLLoader loader = new FXMLLoader(View.class.getResource("closing-scene.fxml"));
        Scene closing = new Scene(loader.load());
        ClosingController c = loader.getController();
        c.setMessage(View.WIN_MESSAGE);
        stage1.setScene(closing);
        closing.setUserData(this.board.getScene().getWindow());
        stage1.show();
    }

    @Override
    public void loseMessage() throws IOException {
        // adds loosing message on the screen
        Stage stage1 = new Stage();
        FXMLLoader loader = new FXMLLoader(View.class.getResource("closing-scene.fxml"));
        Scene closing = new Scene(loader.load());
        ClosingController c = loader.getController();
        c.setMessage(View.LOSE_MESSAGE);
        stage1.setScene(closing);
        closing.setUserData(this.board.getScene().getWindow());
        stage1.show();
    }

    @Override
    public String playerRequest() {
        return null;
    }

    @Override
    public void setMarked(ArrayList<Position> pieces, ArrayList<Position> tiles) {
        // sets all pieces to marked
        Tile.setSuggestionMode(true);
        Piece.setSuggestionMode(true);  // no piece can be clicked, but the marked ones
        for (int i = 0 ; i < pieces.size() ; i++) {
            Piece piece = searchPiece(pieces.get(i));
            piece.markAsMustEat();
        }
        for (int i = 0 ; i < tiles.size() ; i++) {
            this.board.getGameBoard()[tiles.get(i).getX()][tiles.get(i).getY()].showAsOption(TileState.OPTION);
        }
    }

    @Override
    public void start(Stage stage) throws IOException {

        Presenter presenter = new Presenter();
        this.setPresenter(presenter);
        this.board.InitiateBoard();
        this.assignPiecesToPlayers();
        presenter.setGameView(this);
        this.transitionHandler.start();
        for (int i = 0; i < VisualBoard.getDimension(); i++) {
            for (int j = 0; j < VisualBoard.getDimension(); j++) {
                this.board.add(this.board.getGameBoard()[i][j], i, j);
            }
        }
        this.board.placePlayerPieces(this.board, this.visualPlayer1);
        this.board.placePlayerPieces(this.board, this.visualPlayer2);
        Stage stage1 = new Stage();
        FXMLLoader loader = new FXMLLoader(View.class.getResource("opening-scene.fxml"));
        Scene opening = new Scene(loader.load());
        stage1.setScene(opening);
        stage1.show();
        stage1.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                stage1.close();
                if (OpeningController.startClicked) {
                    Scene scene = new Scene(board);
                    stage.setTitle("Impossible Checkers");
                    stage.setScene(scene);
                    stage.setResizable(false);
                    stage.getIcons().add(new Image(ICON_PATH));
                    OpeningMetadata meFirst = (OpeningMetadata) stage1.getUserData();
                    if (meFirst.isWantsToStart()) {
                        setMeAsFirstTurn();
                    }
                    else {
                        try {
                            presenter.generateMoveAI(0);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    stage.show();
                }
                else {
                    transitionHandler.kill();
                }
            }
        });
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                transitionHandler.kill();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}

