package com.example.checkers;

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
    public void sendMoveToCheck(Position src, Position dest) {

        long srcLogic = Position.positionToLogicalNumber(src);
        long destLogic = Position.positionToLogicalNumber(dest);

        boolean ok = this.model.validMove(this.model.getCurrentTurn(), srcLogic, destLogic);
        if (ok)  // regular move
        {
            long checkQueen = 0;
            if (!Model.checkIfQueen(this.model.getCurrentTurn(), srcLogic)) {
                checkQueen = this.model.checkQueen(this.model.getCurrentTurn(), destLogic);
            }
            this.model.updateBoards(this.model.getCurrentTurn(), srcLogic, destLogic);
            this.sendMove(src, dest, checkQueen);
            this.model.switchTurns();
            this.gameView.switchTurns();
        }
        else if (this.model.validEatingMove(this.model.getCurrentTurn(), srcLogic, destLogic))  // eating move
        {

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
                this.model.switchTurns();
                this.gameView.switchTurns();
            }
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
    }

    public void eat(long srcLogic, long destLogic, boolean chain)
    {
        // deals with eating
        // calculates wanted eating position
        Position src = Position.logicalNumberToPosition(srcLogic);
        Position dest = Position.logicalNumberToPosition(destLogic);
        Position eaten = new Position((short) ((src.getX() + dest.getX()) / 2), (short) ((src.getY() + dest.getY()) / 2));

        // removes from boards
        this.model.removePiece(this.model.getRival(), Position.findMiddle(srcLogic, destLogic));

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
            long newQueenPosition = (Model.checkIfQueen(this.model.getCurrentTurn(), chain.getInfo())) ? 0 : this.model.checkQueen(this.model.getCurrentTurn(), chain.getInfo());
            if (newQueenPosition != 0) {
                this.model.moveFromPieceToQueen(this.model.getCurrentTurn(), newQueenPosition);
                this.gameView.makeQueen(Position.logicalNumberToPosition(newQueenPosition));
            }
            this.model.switchTurns();
            this.gameView.switchTurns();
            return;
        }
        if (chain.hasOneSon())
        {
            GeneralTree<Long> onlySon = chain.getFirstSon();
            this.eat(chain.getInfo(), onlySon.getInfo(), !onlySon.isLeaf());  // (isLeaf() - still a chain after this move ?)
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
            ArrayList<Position> optionsToHide = this.currentChain.generateSonsListAsPosition();
            this.currentChain = this.currentChain.findSon(Position.positionToLogicalNumber(destChosen));  // changes the root to be the chosen path
            this.eat(src, Position.positionToLogicalNumber(destChosen), this.currentChain.isLeaf());  // (isLeaf() - chain after move ?)
            this.gameView.hideCurrentOptions(optionsToHide);
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

    // public void sendChainEatingMoves(GeneralTree<Long> possibleMoves)

}

