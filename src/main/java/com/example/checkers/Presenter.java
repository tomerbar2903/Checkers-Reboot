package com.example.checkers;

import javafx.geometry.Pos;

import java.util.ArrayList;

public class Presenter implements IPresenter{

    private IView gameView;
    private Model model;
    private GeneralTree<Long> currentChain;

    private static final int WAIT_BETWEEN_EATS = 1000;

    public Presenter()
    {
        this.gameView = null;
        this.model = new Model();
        this.currentChain = null;
    }

    public void setGameView(IView v)
    {
        this.gameView = v;
    }

    @Override
    public void sendMoveToCheck(Position src, Position dest, boolean trueRegular, boolean trueEating) {

        long srcLogic = Position.positionToLogicalNumber(src);
        long destLogic = Position.positionToLogicalNumber(dest);

        boolean isQueen = Model.checkIfQueen(this.model.getCurrentTurn(), srcLogic);

        if (trueRegular)  // regular move
        {
            long checkQueen = 0;

            if (!isQueen) {
                checkQueen = this.model.checkQueen(this.model.getCurrentTurn(), destLogic);
            }
            this.model.removeAdjacentFromSource(srcLogic);
            this.model.addAdjacentInDestination(this.model.getCurrentTurn(), destLogic, isQueen);
            this.model.updateBoards(this.model.getCurrentTurn(), srcLogic, destLogic);
            this.sendMove(src, dest, checkQueen);
            this.model.switchTurns();
            this.gameView.switchTurns();
        }
        else if (trueEating)  // eating move
        {
            this.model.removePieceFromAdjacency(this.model.getCurrentTurn(), srcLogic);
            this.model.removePieceFromAdjacency(this.model.getRival(), Position.findMiddle(srcLogic, destLogic));  // removes eaten piece from adjacency board
            GeneralTree<Long> chain = this.model.chain(this.model.getCurrentTurn(), destLogic, srcLogic);
            if (chain != null)
            {
                this.eat(srcLogic, destLogic, true);
                GeneralTree.printAsPositions(chain);
                this.dealWithChain(chain);
            }
            else
            {
                this.eat(srcLogic, destLogic, false);
                this.model.addAdjacentInDestination(this.model.getCurrentTurn(), destLogic, isQueen);
            }
            this.model.switchTurns();
            this.gameView.switchTurns();
        }
        else  // invalid move
        {
            this.gameView.showInvalidMove(dest);
        }

        // checks for win
        int winCheck = this.model.checkWin();
        if (winCheck == 1)
        {
            this.gameView.winMessage();
        }
        if (winCheck == -1)
        {
            this.gameView.loseMessage();
        }
        this.handleMustEat();
    }

    public void handleMustEat() {
        // runs in a loop until there are no "must eat" moves
        long mustEat = this.model.generateMustEatTilesAndPieces();
        if (mustEat != 0) {
            ArrayList<Position> pieces = Position.extractPositions(this.model.getCurrentTurn().getMustEatPieces());
            ArrayList<Position> tiles = Position.extractPositions(mustEat);
            this.gameView.setMarked(pieces, tiles);
        }
    }

    public void eat(long srcLogic, long destLogic, boolean chain)
    {
        // deals with eating
        // calculates wanted eating position
        Position src = Position.logicalNumberToPosition(srcLogic);
        Position dest = Position.logicalNumberToPosition(destLogic);
        Position eaten = Position.findMiddle(src, dest);
        long eatenLogic = Position.findMiddle(srcLogic, destLogic);

        // removes from boards
        this.model.removePiece(this.model.getRival(), eatenLogic);

        // check queen after eating
        long madeQueenCheck = 0;
        if (!chain) {  // if the chain is finished
            if (!Model.checkIfQueen(this.model.getCurrentTurn(), srcLogic))
                madeQueenCheck = this.model.checkQueen(this.model.getCurrentTurn(), destLogic);
        }
        this.model.updateBoards(this.model.getCurrentTurn(), srcLogic, destLogic);
        this.gameView.removePiece(eaten);
        // send eating move to view
        this.sendMove(src, dest, madeQueenCheck);
    }

    public void dealWithChain(GeneralTree<Long> chain)
    {
        if (chain.isLeaf())
        {
            // newQueenPosition - 0 if already a queen or not a queen, the position otherwise
            boolean isQueen = Model.checkIfQueen(this.model.getCurrentTurn(), chain.getInfo());
            long newQueenPosition = (isQueen) ? 0 : this.model.checkQueen(this.model.getCurrentTurn(), chain.getInfo());
            if (newQueenPosition != 0) {
                this.model.moveFromPieceToQueen(this.model.getCurrentTurn(), newQueenPosition);
                this.gameView.makeQueen(Position.logicalNumberToPosition(newQueenPosition));
            }
            this.model.addAdjacentInDestination(this.model.getCurrentTurn(), chain.getInfo(), isQueen);
            return;
        }
        if (chain.hasOneSon())
        {
            GeneralTree<Long> onlySon = chain.getFirstSon();
            this.model.removePieceFromAdjacency(this.model.getCurrentTurn(), chain.getInfo());
            long eaten = Position.findMiddle(chain.getInfo(), onlySon.getInfo());
            this.model.removePieceFromAdjacency(this.model.getRival(), eaten);
            this.eat(chain.getInfo(), onlySon.getInfo(), !onlySon.isLeaf());  // (isLeaf() - still a chain after this move ?)
            this.model.printAdjacentBoard();
            this.dealWithChain(onlySon);
            return;
        }
        ArrayList<Position> options = chain.generateSonsListAsPosition();
        this.currentChain = chain;  // saves junction
        this.gameView.showOptions(options);
        Position src = Position.logicalNumberToPosition(chain.getInfo());
    }

    public void continueChain(Position destChosen)
    {
        // continues the chain when user chose a destination
        if (this.currentChain != null) {
            long src = this.currentChain.getInfo();
            this.currentChain = this.currentChain.findSon(Position.positionToLogicalNumber(destChosen));  // changes the root to be the chosen path
            this.eat(src, Position.positionToLogicalNumber(destChosen), this.currentChain.isLeaf());  // (isLeaf() - chain after move ?)
            this.gameView.hideCurrentOptions();
            this.dealWithChain(this.currentChain);  // deals with sub-chains
            this.currentChain = null;  // re-initiates the current chain tree
        }
    }

    public LogicalPlayer visualPlayerToLogical(VisualPlayer visualPlayer)
    {
        // returns the appropriate player according to id
        return this.model.getPlayerFromId(visualPlayer.getId());
    }

    @Override
    public void sendMove(Position src, Position dest, long queenPos) {
        // moves with mode - if queenPos != 0, make a queen
        Position position = Position.logicalNumberToPosition(queenPos);
        if (queenPos != 0) {
            this.model.moveFromPieceToQueen(this.model.getCurrentTurn(), queenPos);
            this.gameView.move(src, dest, position);  // makes new queen
        }
        else
        {
            this.gameView.move(src, dest, null);  // no new queen
        }
        this.model.printBoard();
    }

    @Override
    public void makeQueen(Position piece) {
        this.gameView.makeQueen(piece);
    }

    @Override
    public void winMessage() {
        this.gameView.winMessage();
    }

    @Override
    public void loseMessage() {
        this.gameView.loseMessage();
    }

    private LogicalPlayer convert(VisualPlayer player) {
        LogicalPlayer player1 = (player.isMe()) ? (this.model.getPlayerFromId(1)) : this.model.getPlayerFromId(2);  // get appropriate player
        return player1;
    }

    @Override
    public boolean validMove(VisualPlayer player, Position src, Position dest) {
        // checks with the model if a move is fine (regular)
        LogicalPlayer player1 = this.convert(player);
        long srcLogic, destLogic;
        srcLogic = Position.positionToLogicalNumber(src);
        destLogic = Position.positionToLogicalNumber(dest);
        return this.model.validMove(player1, srcLogic, destLogic);
    }

    @Override
    public boolean validEatingMove(VisualPlayer player, Position src, Position dest) {
        // checks with the model if a move is fine (eating)
        LogicalPlayer player1 = this.convert(player);
        long srcLogic, destLogic;
        srcLogic = Position.positionToLogicalNumber(src);
        destLogic = Position.positionToLogicalNumber(dest);
        return this.model.validEatingMove(player1, srcLogic, destLogic, Model.checkIfQueen(player1, srcLogic));
    }
}

