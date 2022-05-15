package com.example.checkers;

import java.util.HashMap;

public class Model implements IModel {

    private LogicalPlayer player1;
    private LogicalPlayer player2;
    private LogicalPlayer currentTurn;
    private boolean turnAI;
    private HashMap<Long, EntryTranspositionTable> transpositionTableHashMap;

    public static final int DEFAULT_SEARCH_DEPTH = 6;

    public Model() {
        this.player1 = new LogicalPlayer();
        this.player2 = new LogicalPlayer();
        this.currentTurn = this.player2;
        this.printBoard();
        this.turnAI = true;
        TranspositionTable.initiate();
        this.transpositionTableHashMap = new HashMap<>();
    }

    public void setCurrentTurn(LogicalPlayer currentTurn) {
        this.currentTurn = currentTurn;
    }

    public static boolean checkIfQueen(LogicalPlayer player, long pos) {
        // returns true if pos in queen piece
        return ((player.getQueenBoard() & pos) != 0);
    }

    @Override
    public boolean validMove(LogicalPlayer player, long src, long dest) {
        // checks if a move is valid
        long adjacentTiles = BitboardEssentials.getAdjacentMask(src, player.isDark(), Model.checkIfQueen(player, src), false);
        return ((dest & adjacentTiles) != 0) && this.emptyPosition(dest);
    }

    public void buildChainRegularPiece(GeneralTree<Long> root, LogicalPlayer player, long src) {
        // return a tree of all possible eating moves from src.
        // get possible landing destinations after an eating move
        long possibleEatingDestinations = BitboardEssentials.getCorners(src, BitboardEssentials.CHECK_EAT_DIAMETER);
        possibleEatingDestinations = BitboardEssentials.validateForPlayer(src, possibleEatingDestinations, player.isDark(), false);

        int sonIndex = 0;
        long positionInBitboard;
        int index;

        // run until all positions in possibleEatingDestinations are covered
        for (int i = 0; i < 4 && possibleEatingDestinations > 0; i++) {

            // get index in bitboard
            index = (int) BitboardEssentials.log2(possibleEatingDestinations);
            // convert into position (full-on number)
            positionInBitboard = 1L << index;

            // check if this current destination makes a valid eating move
            if (validEatingMove(player, src, positionInBitboard, false)) {
                // remove current piece (for enabling returning to the same position in the chain
                this.removePiece(player, src);
                // find eating piece's position
                long eaten = Position.findMiddle(src, positionInBitboard);
                // check if a queen was eaten (for later - when adding back to the board)
                boolean rivalQueen = Model.checkIfQueen(this.getRival(player), eaten);

                // remove rival's piece
                this.removePiece(this.getRival(player), eaten);

                // make a new node in the tree, and assign it as a son of root
                GeneralTree<Long> eatingDest = new GeneralTree<>(positionInBitboard);
                root.put(eatingDest);

                // call the function for the son
                buildChainRegularPiece(root.get(sonIndex++), player, positionInBitboard);

                // places back pieces on the board
                this.placePiece(this.getRival(player), eaten, rivalQueen);
            }
            // remove this current destination from the mask
            possibleEatingDestinations -= positionInBitboard;
        }
    }

    public void buildChainQueenPiece(GeneralTree<Long> root, LogicalPlayer player, long src) {
        // return a tree of all possible eating moves from src.

        // get possible landing destinations after an eating move
        long possibleEatingDestinations = BitboardEssentials.getCorners(src, BitboardEssentials.CHECK_EAT_DIAMETER);

        long positionInBitboard;
        int index;
        int sonIndex = 0;

        // run until all positions in possibleEatingDestinations are covered
        for (int i = 0; i < 4 && possibleEatingDestinations > 0; i++) {

            // get index in bitboard
            index = (int) BitboardEssentials.log2(possibleEatingDestinations);
            // convert into position (full-on number)
            positionInBitboard = 1L << index;

            // check if this current destination makes a valid eating move
            if (validEatingMove(player, src, positionInBitboard, true)) {
                // remove current piece (for enabling returning to the same position in the chain
                this.removePiece(player, src);
                // find eating piece's position
                long eaten = Position.findMiddle(src, positionInBitboard);
                // check if a queen was eaten (for later - when adding back to the board)
                boolean rivalQueen = Model.checkIfQueen(this.getRival(player), eaten);

                // remove rival's piece
                this.removePiece(this.getRival(player), eaten);

                // make a new node in the tree, and assign it as a son of root
                GeneralTree<Long> eatingDest = new GeneralTree<>(positionInBitboard);
                root.put(eatingDest);

                // call the function for the son
                buildChainQueenPiece(root.get(sonIndex++), player, positionInBitboard);

                // places back pieces on the board
                this.placePiece(this.getRival(player), eaten, rivalQueen);
            }
            // remove this current destination from the mask
            possibleEatingDestinations -= positionInBitboard;
        }
    }

    public boolean checkRivalInTheMiddle(long src, long dest) {
        // returns true if the rival is in the middle between src and dest
        long middle = Position.findMiddle(src, dest);
        LogicalPlayer rival = this.getRival();
        return ((middle & rival.getQueenBoard()) != 0) || ((middle & rival.getPieceBoard()) != 0);
    }

    public GeneralTree<Long> getPossibleChains(LogicalPlayer player, long srcChain, long realSrc) {
        // src - actually the destination after eating
        GeneralTree<Long> possibleChains = new GeneralTree<>(srcChain);
        if (checkIfQueen(player, realSrc)) {
            // TODO - remove queen before building tree, remove players that were eaten
            long rivalPosition = Position.findMiddle(realSrc, srcChain);  // finds the position of the eaten rival's piece
            boolean rivalQueenEaten = Model.checkIfQueen(this.getRival(), rivalPosition);
            this.removePiece(player, realSrc);
            this.removePiece(this.getRival(player), rivalPosition);
            this.buildChainQueenPiece(possibleChains, player, srcChain);
            this.placePiece(this.getRival(player), rivalPosition, rivalQueenEaten);
            this.placePiece(player, realSrc, true);
        } else {
            this.buildChainRegularPiece(possibleChains, player, srcChain);
            // this.placePiece(player, srcChain, false);
        }
        return possibleChains;
    }

    public GeneralTree<Long> chain(LogicalPlayer player, long srcChain, long realSrc) {
        // checks if there is a chain. return the chain / null
        GeneralTree<Long> chain = this.getPossibleChains(player, srcChain, realSrc);
        chain = (chain.isLeaf()) ? null : chain;  // if the chain contains only the src - return null
        return chain;
    }

    public boolean validEatingMove(LogicalPlayer player, long src, long dest, boolean isQueen) {
        // returns true if the eating move is valid
        /*
        1. generate matching destination eating mask
        2. validMove <- check if dest is in the mask
        3. checkPossibleRival <- check if rival in the middle between src and dest
        4. return validMove && checkPossibleRival
         */

        boolean validMove;

        long possibleDestinations = BitboardEssentials.getPossibleEatingDestinations(src, player.isDark(), isQueen, false);

        // check if destination lands on a valid eating destination && no piece found in dest
        validMove = ((possibleDestinations & dest) != 0) && emptyPosition(dest);

        // looks if there is a rival piece between src and dst
        boolean checkRivalExistence = (getRival(player).getTotalBoard() & Position.findMiddle(src, dest)) != 0;

        return checkRivalExistence && validMove;
    }

    public LogicalPlayer getRival() {
        // returns rival player to current turn
        LogicalPlayer rival = (this.currentTurn == this.player1) ? this.player2 : this.player1;
        return rival;
    }

    public void removePiece(LogicalPlayer player, long position) {
        // removes piece from piece board
        long opPosition = ~position;
        long maskPiece = player.getPieceBoard() & opPosition;
        long maskQueen = player.getQueenBoard() & opPosition;

        boolean checkQueen = Model.checkIfQueen(player, position);

        /* UPDATE ADJACENCY BOARD */
        {
            this.removePieceFromAdjacency(player, position);
        }

        /* UPDATING SAFE PIECE / QUEEN */
        {
            long checkEdge = BitboardEssentials.BOARD_EDGES & position;
            if (checkEdge != 0) {
                if ((checkQueen)) {
                    player.setSafeQueens(player.getSafeQueens() - 1);
                } else {
                    player.setSafePieces(player.getSafePieces() - 1);
                }
            }
        }

        /* UPDATING PIECE / QUEEN AMOUNT */
        {
            boolean isQueen = Model.checkIfQueen(player, position);
            if (isQueen) {
                player.setQueenAmount(player.getQueenAmount() - 1);
            } else {
                player.setPieceAmount(player.getPieceAmount() - 1);
            }
        }

        /* UPDATING DEFENDER PIECES / QUEENS */
        {
            long checkDefender = (player.isDark()) ? (BitboardEssentials.DARK_DEFENDERS & position) : (BitboardEssentials.LIGHT_DEFENDERS & position);
            if (checkDefender != 0) {
                if (!checkQueen) {
                    player.setDefenderPieces(player.getDefenderPieces() - 1);
                }
                else {
                    player.setDefenderQueens(player.getDefenderQueens() - 1);
                }
            }
        }

        /* UPDATING ATTACKING PIECES / QUEENS */
        {
            long checkAttacking = (player.isDark()) ? (BitboardEssentials.DARK_ATTACKERS & position) : (BitboardEssentials.LIGHT_ATTACKERS & position);
            if (checkAttacking != 0) {
                if (!checkQueen) {
                    player.setAttackingPieces(player.getAttackingPieces() - 1);
                }
                else {
                    player.setAttackingQueens(player.getAttackingQueens() - 1);
                }
            }
        }

        /* UPDATE BOTTOM ROW OCCUPIED */
        {
            long occupiedBottom = (player.isDark()) ? (BitboardEssentials.DARK_BOTTOM_ROW & position) : (BitboardEssentials.LIGHT_BOTTOM_ROW & position);
            if (occupiedBottom != 0) {
                player.setOccupiedBottomRow(player.getOccupiedBottomRow() - 1);
            }
        }

        player.setPieceBoard(maskPiece);
        player.setQueenBoard(maskQueen);
    }

    public void placePiece(LogicalPlayer player, long position, boolean queen) {
        // places the piece at the right board
        if (queen) {
            player.setQueenBoard(player.getQueenBoard() | position);  // adds the queen in the queen board
        } else {
            player.setPieceBoard(player.getPieceBoard() | position);  // adds the piece in the piece board
        }

        /* UPDATE ADJACENCY BOARD */
        {
            this.addAdjacentInDestination(player, position, queen);
        }

        /* UPDATE PIECE / QUEEN AMOUNT */
        {
            if (queen) {
                player.setQueenAmount(player.getQueenAmount() + 1);
            }
            else {
                player.setPieceAmount(player.getPieceAmount() + 1);
            }
        }

        /* UPDATE SAFE PIECE / QUEEN */
        {
            long checkEdge = BitboardEssentials.BOARD_EDGES & position;
            if (checkEdge != 0) {
                if (queen) {
                    player.setSafeQueens(player.getSafeQueens() + 1);
                } else {
                    player.setSafePieces(player.getSafePieces() + 1);
                }
            }
        }

        /* UPDATING DEFENDER PIECE / QUEEN */
        {
            long checkDefender = (player.isDark()) ? (BitboardEssentials.DARK_DEFENDERS & position) : (BitboardEssentials.LIGHT_DEFENDERS & position);
            if (checkDefender != 0) {
                if (queen) {
                    player.setDefenderQueens(player.getDefenderQueens() + 1);
                }
                else {
                    player.setDefenderPieces(player.getDefenderPieces() + 1);
                }
            }
        }

        /* UPDATING ATTACKING PIECE / QUEEN */
        {
            long checkAttack = (player.isDark()) ? (BitboardEssentials.DARK_ATTACKERS & position) : (BitboardEssentials.LIGHT_ATTACKERS & position);
            if (checkAttack != 0) {
                if (queen) {
                    player.setAttackingQueens(player.getAttackingQueens() + 1);
                }
                else {
                    player.setAttackingPieces(player.getAttackingPieces() + 1);
                }
            }
        }

        /* UPDATE BOTTOM ROW OCCUPIED */
        {
            long occupiedBottom = (player.isDark()) ? (BitboardEssentials.DARK_BOTTOM_ROW & position) : (BitboardEssentials.LIGHT_BOTTOM_ROW & position);
            if (occupiedBottom != 0) {
                player.setOccupiedBottomRow(player.getOccupiedBottomRow() + 1);
            }
        }
    }

    public boolean emptyPosition(long position) {
        // returns true if no other piece is in this position
        return (this.getBlankTilesBoard() & position) != 0;
    }

    public void updateBoards(LogicalPlayer player, long src, long dest) {
        // moves and updates boards according to move
        boolean isQueen = Model.checkIfQueen(player, src);
        boolean madeQueen = (((dest & BitboardEssentials.DARK_QUEEN) != 0) && !isQueen);


        this.removePiece(player, src);
        this.placePiece(player, dest, isQueen);
        if (madeQueen) {
            this.moveFromPieceToQueen(player, dest);
        }
    }

    @Override
    public void switchTurns() {
        // switch turns
        this.currentTurn = (this.currentTurn.equals(this.player1)) ? this.player2 : this.player1;
        this.turnAI = !this.turnAI;
    }


    @Override
    public long checkQueen(LogicalPlayer logicalPlayer, long dest) {
        // checks if current player made a queen
        long pos = 0;
        if (logicalPlayer.isDark()) {
            pos = (dest & BitboardEssentials.DARK_QUEEN);
        } else {
            pos = (dest & BitboardEssentials.LIGHT_QUEEN);
        }
        return pos;
    }

    public void moveFromPieceToQueen(LogicalPlayer player, long position) {
        // moves position into queen board
        long removePieceMask = ~position;
        player.setQueenBoard(player.getQueenBoard() | position);
        player.setPieceBoard(player.getPieceBoard() & removePieceMask);

        player.setQueenAmount(player.getQueenAmount() + 1);
        player.setPieceAmount(player.getPieceAmount() - 1);
    }

    public LogicalPlayer getCurrentTurn() {
        return this.currentTurn;
    }

    public LogicalPlayer getPlayerFromId(int id) {
        // returns the right player according to his id
        LogicalPlayer logicalPlayer = (id == 1) ? this.player1 : this.player2;
        return logicalPlayer;
    }

    public LogicalPlayer getRival(LogicalPlayer player) {
        LogicalPlayer rival = (player == this.getCurrentTurn()) ? this.getRival() : this.getCurrentTurn();
        return rival;
    }

    public void printBoard() {
        long board1 = this.player1.getPieceBoard(), board2 = this.player2.getPieceBoard();
        long queen1 = this.player1.getQueenBoard(), queen2 = this.player2.getQueenBoard();
        // prints board nicely to track moves
        for (int i = 0; i < VisualBoard.getDimension() * VisualBoard.getDimension(); i++) {
            // assumes that 2 pieces can't be at the same position
            if (board1 % 2 != 0) {
                System.out.print("1\t");
            } else if (board2 % 2 != 0) {
                System.out.print("2\t");
            } else if (queen1 % 2 != 0) {
                System.out.print("!\t");
            } else if (queen2 % 2 != 0) {
                System.out.print("@\t");
            } else {
                System.out.print((char) 0xB7 + "\t");
            }
            // goes down one row
            if ((i + 1) % VisualBoard.getDimension() == 0) {
                System.out.println();
            }
            board1 = board1 >> 1;
            board2 = board2 >> 1;
            queen1 = queen1 >> 1;
            queen2 = queen2 >> 1;
        }
        System.out.println("\n\n");
    }

    public String stringBoard() {
        long board1 = this.player1.getPieceBoard(), board2 = this.player2.getPieceBoard();
        long queen1 = this.player1.getQueenBoard(), queen2 = this.player2.getQueenBoard();
        String st = "";
        // prints board nicely to track moves
        for (int i = 0; i < VisualBoard.getDimension() * VisualBoard.getDimension(); i++) {
            // assumes that 2 pieces can't be at the same position
            if (board1 % 2 != 0) {
                st += "1\t";
            } else if (board2 % 2 != 0) {
                st += "2\t";
            } else if (queen1 % 2 != 0) {
                st += "!\t";
            } else if (queen2 % 2 != 0) {
                st += "@\t";
            } else {
                st += (char) 0xB7 + "\t";
            }
            // goes down one row
            if ((i + 1) % VisualBoard.getDimension() == 0) {
                st += "\n";
            }
            board1 = board1 >> 1;
            board2 = board2 >> 1;
            queen1 = queen1 >> 1;
            queen2 = queen2 >> 1;
        }
        st += "\n\n";
        return st;
    }

    public int checkWin() {
        // returns (+) - won, (-) - lost, (0) - no one won
        boolean check1 = (this.player1.getPieceBoard() == 0 && this.player1.getQueenBoard() == 0);
        boolean check2 = (this.player2.getPieceBoard() == 0 && this.player2.getQueenBoard() == 0);
        int won = 0;
        if (check1) {
            won = -1;
        }
        if (check2) {
            won = 1;
        }
        return won;
    }

    public void printAdjacentBoard() {
        // prints adjacent board nicely
        long a1 = this.player1.getAdjacentToRival(), a2 = this.player2.getAdjacentToRival();
        long p1 = this.player1.getPieceBoard() | this.player1.getQueenBoard();
        long p2 = this.player2.getPieceBoard() | this.player2.getQueenBoard();
        for (int i = 0; i < VisualBoard.getDimension() * VisualBoard.getDimension(); i++) {
            if (a1 % 2 != 0) {
                System.out.print("1\t");
            } else if (a2 % 2 != 0) {
                System.out.print("2\t");
            } else if (p1 % 2 == 0 && p2 % 2 == 0) {
                System.out.print((char) 0xB7 + "\t");
            } else {
                System.out.print((char) 0x25CB + "\t");
            }
            if ((i + 1) % VisualBoard.getDimension() == 0) {
                System.out.println();
            }
            a1 >>= 1;
            a2 >>= 1;
            p1 >>= 1;
            p2 >>= 1;
        }
        System.out.print("\n\n");
    }

    public long validateAdjacency(long src, LogicalPlayer player, long adjacentMask) {
        // checks if adjacent to current player are already adjacent to another piece of current player - returns a mask of all exclusive adjacent pieces
        LogicalPlayer rival = this.getRival(player);
        long finalMask = 0;
        long curAdjacent;  // each rival's adjacent mask
        int index;  // the index in the bitboard
        long pos;  // the actual number in the bitboard - ex. (index = 8  -->  pos = 256)
        for (int i = 0; i < 4 && adjacentMask > 0; i++) {  // max adjacent pieces is 4
            index = (int) BitboardEssentials.log2(adjacentMask);  // get index
            pos = 1l << index;  // convert to position on bitboard
            curAdjacent = BitboardEssentials.getCorners(pos, BitboardEssentials.ADJACENT_DIAMETER);
            curAdjacent &= ~src;  // remove src from adjacent mask
            curAdjacent &= (rival.getQueenBoard() | rival.getPieceBoard());

            // if the adjacent mask is 0 --> the piece is exclusively adjacent to src -> add to mask
            finalMask = (curAdjacent != 0) ? finalMask : finalMask | pos;

            // move to next position
            adjacentMask -= pos;
        }
        return finalMask;
    }


    public void removeAdjacentFromSource(long src) {
        // removes adjacent pieces from rival-adjacent board, if exclusively adjacent to piece at source
        /* EXAMPLE
        0 1 0 1 0
        0 0 2 0 0

        Let's say that the piece that is labeled as '1' (the right one) was moved on the board. we check all adjacent to it - so 2 is checked.
        When checked, we notice that the 2 is still adjacent to another 1, so it's not removed.

        0 1 0 0 0
        2 0 0 0 0

        Let's say that the piece that is labeled as '1' (the left one) was moved on the board. Here, the '2' will be removed from adjacentToRival board
         */
        LogicalPlayer cur = this.getCurrentTurn();
        LogicalPlayer rival = this.getRival();

        // get adjacent mask for source position - to access all rival adjacent pieces (to check and remove)
        long adjacentMaskSrc = BitboardEssentials.getCorners(src, BitboardEssentials.ADJACENT_DIAMETER);

        // get the rival adjacent pieces to source
        adjacentMaskSrc &= (rival.getAdjacentToRival());

        // returns a new mask containing only the pieces that are exclusively adjacent to source
        adjacentMaskSrc = this.validateAdjacency(src, this.getRival(), adjacentMaskSrc);
        rival.setAdjacentToRival(rival.getAdjacentToRival() & (~adjacentMaskSrc));  // remove all exclusively adjacent pieces from rival's adjacent board
        // remove src from current player's adjacency bitboard
        cur.setAdjacentToRival(cur.getAdjacentToRival() & ~src);
    }

    public void addAdjacentInDestination(LogicalPlayer player, long dest, boolean isQueen) {
        // adds new adjacent pieces according to destination
        LogicalPlayer cur = player;
        LogicalPlayer rival = this.getRival(player);

        long checkQueenAdjacent = BitboardEssentials.getAdjacentMask(dest, cur.isDark(), isQueen, true);  // check if update is needed backwards
        checkQueenAdjacent &= rival.getQueenBoard();
        // update queens on the back if needed
        if (checkQueenAdjacent != 0) rival.setAdjacentToRival(rival.getAdjacentToRival() | checkQueenAdjacent);

        // get adjacent mask according to piece
        long adjacentMaskDest = BitboardEssentials.getAdjacentMask(dest, cur.isDark(), isQueen, false);
        long rivalPiecesAdjacent = rival.getPieceBoard() & adjacentMaskDest;
        long rivalQueenAdjacent = rival.getQueenBoard() & adjacentMaskDest;
        if (rivalPiecesAdjacent != 0) {
            // update current player's piece as adjacent to rival
            cur.setAdjacentToRival(cur.getAdjacentToRival() | dest);

            // update adjacent rival pieces as adjacent
            long coAdjacentPieces = BitboardEssentials.validateForPlayer(dest, rivalPiecesAdjacent, player.isDark(), isQueen);
            rival.setAdjacentToRival(rival.getAdjacentToRival() | coAdjacentPieces);
        }
        if (rivalQueenAdjacent != 0) {
            rival.setAdjacentToRival(rival.getAdjacentToRival() | rivalQueenAdjacent);
        }
    }

    public void removePieceFromAdjacency(LogicalPlayer player, long pos) {
        // removes a certain piece from adjacent board - updates adjacent rival pieces
        LogicalPlayer rival = this.getRival(player);
        long adjacentRivals = BitboardEssentials.getCorners(pos, BitboardEssentials.ADJACENT_DIAMETER);
        adjacentRivals &= rival.getAdjacentToRival();
        adjacentRivals = this.validateAdjacency(pos, this.getRival(player), adjacentRivals);
        rival.setAdjacentToRival(rival.getAdjacentToRival() & ~adjacentRivals);
        player.setAdjacentToRival(player.getAdjacentToRival() & ~pos);
    }

    public long getBlankTilesBoard() {
        // returns a mask of all empty positions
        return ~(this.player1.getPieceBoard() | this.player1.getQueenBoard() | this.player2.getPieceBoard() | this.player2.getQueenBoard());
    }

    public long getBlankAdjacent(long pos) {
        // return a mask of all adjacent blank tiles for pos
        long adjacent = BitboardEssentials.getCorners(pos, BitboardEssentials.ADJACENT_DIAMETER);
        long blankAdjacent = adjacent & this.getBlankTilesBoard();
        return blankAdjacent;
    }

    public long generateMustEatTilesAndPieces(LogicalPlayer cur, LogicalPlayer riv) {
        // updates mustEat board for the current turn player. returns a mask of all tiles that can be chosen
        /*
        ALGORITHM:
        1. take current player's adjacentToRivalBoard
            (*) for each position:
            1.1. generate a mask of all rival adjacent pieces
                 (**) for each position:
                 1.1.1. generate a mask of all blank tiles that are adjacent to the current position
                    (***) for each blank tile:
                    1.2.1 generate a mask of possible sources for eating to land on this position (corners with side of 5)
                    1.2.2 check the common bits between major position (position of tab *)
                        if not zero:
                        1.2.2.1 add result to mustEat board of current player
                        1.2.2.2 add position of tab (***) to finalTileMask
         */
        long finalTileMask = 0;
        long curAdjacentToRival = cur.getAdjacentToRival();
        int indexCur = 0, indexRiv = 0, indexBlank = 0;
        long positionInBitboardCur, positionInBitboardRival, positionInBitboardBlank;
        long rivalAdjacentPieces;
        long blanks;
        cur.setMustEatPieces(0);

        // each potential eat move - source
        while (curAdjacentToRival > 0) {
            indexCur = (int) BitboardEssentials.log2(curAdjacentToRival);
            positionInBitboardCur = 1L << indexCur;
            rivalAdjacentPieces = BitboardEssentials.getAdjacentMask(positionInBitboardCur, cur.isDark(), Model.checkIfQueen(cur, positionInBitboardCur), false) & (riv.getTotalBoard());

            // each position of eatable piece
            for (int i = 0; i < 4 && rivalAdjacentPieces > 0; i++) {
                indexRiv = (int) BitboardEssentials.log2(rivalAdjacentPieces);
                positionInBitboardRival = 1L << indexRiv;
                // generate blank mask according to the eater - not the eaten
                blanks = BitboardEssentials.getAdjacentMask(positionInBitboardRival, riv.isDark(), Model.checkIfQueen(cur, positionInBitboardCur), true) & this.getBlankTilesBoard();

                // each adjacent blank tile
                for (int j = 0; j < 4 && blanks > 0; j++) {
                    indexBlank = (int) BitboardEssentials.log2(blanks);
                    positionInBitboardBlank = 1L << indexBlank;
                    long possibleSources = BitboardEssentials.getCorners(positionInBitboardBlank, BitboardEssentials.CHECK_EAT_DIAMETER);
                    long sources = possibleSources & positionInBitboardCur;
                    if (sources != 0) {
                        cur.setMustEatPieces(cur.getMustEatPieces() | positionInBitboardCur);
                        finalTileMask |= positionInBitboardBlank;
                    }
                    blanks -= positionInBitboardBlank;
                }
                rivalAdjacentPieces -= positionInBitboardRival;
            }
            curAdjacentToRival -= positionInBitboardCur;
        }
        return finalTileMask;
    }

    public int evaluate(LogicalPlayer player) {

        long totalBoard = player.getTotalBoard();

        LogicalPlayer rival = this.getRival(player);

        int mustBeEaten = this.analyzeMustEat(rival);
        int futureEat = this.analyzeMustEat(player);

        boolean oreoPresent = (player.isDark()) ? ((BitboardEssentials.DARK_OREO_PATTERN & totalBoard) != 0) : (((BitboardEssentials.LIGHT_OREO_PATTERN & totalBoard) != 0));
        boolean triangleOreoPresent = (player.isDark()) ? ((BitboardEssentials.DARK_TRIANGLE_PATTERN & totalBoard) != 0) : (((BitboardEssentials.LIGHT_TRIANGLE_PATTERN & totalBoard) != 0));

        int score = 8 * (this.queenAmount(player) - this.queenAmount(rival));
        score += 7 * (this.pieceAmount(player) - this.pieceAmount(rival));
        score += 3 * this.center(player) - 2 * this.center(rival);
        score += this.centerSides(player);
        score += 10 * (this.attackingQueens(player) - this.attackingQueens(rival));
        score += 7 * (this.attackingPieces(player) - this.attackingPieces(rival));
        score += 5 * this.safePieces(player);
        score += 5 * this.safeQueens(player);
        score += 5 * (this.defenderPieces(player) - this.defenderPieces(rival));
        score += 10 * (this.defenderQueens(player) - this.defenderQueens(rival));
        score += 4 * this.occuipiedButtom(player);
        score -= this.attackingQueens(rival) * 30;

        if (oreoPresent || triangleOreoPresent) {
            score += 4;
        }

        score -= 10 * mustBeEaten;
        score += 4 * futureEat;

        return score;
    }

    public static ArrayList<BitMove> findMustEat(LogicalPlayer player, long destinations) {

        ArrayList<BitMove> eatingMoves = new ArrayList<>();

        long pieces = player.getMustEatPieces();

        boolean isQueen;

        int index;
        long pos;

        while (pieces > 0) {
            index = (int) BitboardEssentials.log2(pieces);
            pos = 1L << index;
            long possibleDest = BitboardEssentials.getCorners(pos, BitboardEssentials.CHECK_EAT_DIAMETER);
            isQueen = Model.checkIfQueen(player, pos);
            possibleDest = BitboardEssentials.validateForPlayer(pos, possibleDest, player.isDark(), isQueen);
            long dests = possibleDest & destinations;
            if (dests != 0) {
                int firstMoveIndex = (int) BitboardEssentials.log2(dests);
                long dest = 1L << firstMoveIndex;
                BitMove move = new BitMove(pos, dest);
                eatingMoves.add(move);
            }

            pieces -= pos;
        }
        return eatingMoves;
    }


    public BitMove generateMove(LogicalPlayer player, LogicalPlayer rival) {

        BitMove move = null;
        long totalBoard = player.getTotalBoard();
        long empty = ~ (player.getTotalBoard() | rival.getTotalBoard());
        long mustEat = this.generateMustEatTilesAndPieces(player, rival);

        ArrayList<BitMove> mustEatMove = Model.findMustEat(player, mustEat);
        if (mustEatMove.size() != 0) {
            for (int i = 0 ; i < mustEatMove.size() ; i++) {
                boolean isQueen = Model.checkIfQueen(player, mustEatMove.get(i).getSource());
                GeneralTree<Long> chain;
                boolean ateQueen = Model.checkIfQueen(rival, Position.findMiddle(mustEatMove.get(i).getSource(), mustEatMove.get(i).getDestination()));
                this.makeImaginaryMove(player, mustEatMove.get(i).getSource(), mustEatMove.get(i).getDestination(), true, isQueen);
                if (!isQueen) {
                    chain = this.chain(player, mustEatMove.get(i).getDestination(), mustEatMove.get(i).getSource());
                    if (chain != null) {
                        QueueStack<TotalBoardState> path = new QueueStack<>();
                        this.analyzeChain(player, rival, chain, isQueen);
                        path.print();
                    }
                    else {
                        move = mustEatMove.get(i);
                    }
                }
                this.revertImaginaryMove(player, mustEatMove.get(i).getSource(), mustEatMove.get(i).getDestination(), true, ateQueen, false, true);
            }
        }

        else {

            int index;
            long pos;
            long adj;

            while (totalBoard > 0) {
                index = (int) BitboardEssentials.log2(totalBoard);
                pos = 1L << index;
                adj = BitboardEssentials.getAdjacentMask(pos, player.isDark(), Model.checkIfQueen(player, pos), false);
                long e = adj & empty;
                if (e != 0) {
                    move = new BitMove();
                    move.setSource(pos);
                    int destIndex = (int) BitboardEssentials.log2(e);
                    long dest = 1L << destIndex;
                    move.setDestination(dest);
                    break;
                }
                totalBoard -= pos;
            }
        }
        return move;
    }

    public long possibleMoves(LogicalPlayer player, long piece, boolean isQueen) {
        // return a mask of all possible destinations for a certain piece (for a certain player)
        long possible = 0;
        LogicalPlayer rival = this.getRival(player);
        long regularMove = BitboardEssentials.getAdjacentMask(piece, player.isDark(), isQueen, false);

        // make sure only blank
        regularMove &= this.getBlankTilesBoard();
        possible |= regularMove;

        long eatingMoves = BitboardEssentials.getCorners(piece, BitboardEssentials.CHECK_EAT_DIAMETER);
        eatingMoves = BitboardEssentials.validateForPlayer(piece, eatingMoves, player.isDark(), isQueen);
        // make sure only blank
        eatingMoves &= this.getBlankTilesBoard();

        int index;
        long pos;
        for (int i = 0 ; i < 4 && eatingMoves > 0 ; i++) {
            index = (int) BitboardEssentials.log2(eatingMoves);
            pos = 1L << index;
            long isEaten = Position.findMiddle(piece, pos);
            if ((isEaten & rival.getTotalBoard()) != 0) {
                possible |= pos;
            }
            eatingMoves -= pos;
        }
        return possible;
    }

    public static long filterToEatingOnly(long piece, long possibleMoves) {
        return BitboardEssentials.getCorners(piece, BitboardEssentials.CHECK_EAT_DIAMETER) & possibleMoves;
    }

    public void convertQueenToPiece(LogicalPlayer player, long pos) {
        player.setQueenBoard(player.getQueenBoard() & ~pos);
        player.setPieceBoard(player.getPieceBoard() | pos);
        player.setPieceAmount(player.getPieceAmount() + 1);
        player.setQueenAmount(player.getQueenAmount() - 1);
    }

    public boolean makeImaginaryMove(LogicalPlayer player, long src, long dest, boolean isEatingMove, boolean isQueen) {
        // makes move (call revertImaginaryMove to cancel it later)
        // returns true if move made a queen for player
        this.removePiece(player, src);
        boolean madeQueen = (player.isDark()) ? ((BitboardEssentials.DARK_QUEEN & dest) != 0) : ((BitboardEssentials.LIGHT_QUEEN & dest) != 0);
        madeQueen = (madeQueen && !isQueen);
        this.placePiece(player, dest, madeQueen || isQueen);
        if (isEatingMove) {
            LogicalPlayer rival = this.getRival(player);
            this.removePiece(rival, Position.findMiddle(src, dest));
        }
        {
            /*if (isQueen) {
                System.out.println(new BoardMove(Position.logicalNumberToPosition(src), Position.logicalNumberToPosition(dest)));
                System.out.println("Placed As Queen In Destination: " + (madeQueen || isQueen));
            }*/
        }
        return madeQueen;
    }

    public void revertImaginaryMove(LogicalPlayer player, long ogSrc, long ogDest, boolean wasEatingMove, boolean ateQueen, boolean madeQueen, boolean wasQueen) {
        // undoes a move
        this.removePiece(player, ogDest);

        // was a queen prier to the move - places accordingly
        this.placePiece(player, ogSrc, !madeQueen && wasQueen);

        if (wasEatingMove) {
            LogicalPlayer rival = this.getRival(player);
            this.placePiece(rival, Position.findMiddle(ogSrc, ogDest), ateQueen);
        }
    }

    public void eat(LogicalPlayer player, long src, long dest) {
        LogicalPlayer rival = this.getRival(player);
        this.removePiece(rival, Position.findMiddle(src, dest));
        this.placePiece(player, dest, Model.checkIfQueen(player, src));
        this.removePiece(player, src);
    }

    public static boolean isEatingMove(long src, long dest) {
        return (BitboardEssentials.getCorners(src, BitboardEssentials.CHECK_EAT_DIAMETER) & dest) != 0;
    }

    public static BitMove calculateMove(long before, long after) {
        // converts before and after board to a move

        long srcAndDest = before ^ after;
        long src = srcAndDest & before;
        long dest = srcAndDest & after;
        return new BitMove(src, dest);
    }

    public LinearLinkedList<BitMove> getBestChain(LogicalPlayer player, QueueStack<TotalBoardState> bestChain, boolean isQueen) {
        // returns best chain possible - if known that chain is existent
        LinearLinkedList<BitMove> positionsBestChain = new LinearLinkedList<>();
        TotalBoardState endState = bestChain.remove();
        int size = bestChain.getSize();
        bestChain.push(endState);
        for (int i = 0 ; i < size ; i++) {
            TotalBoardState startState = bestChain.remove();
            BitMove move;
            move = Model.calculateMove(startState.getLightState().getTotalBoard(), endState.getLightState().getTotalBoard());
            positionsBestChain.push(move);

            endState = startState;

            bestChain.push(endState);
        }
        return positionsBestChain;
    }

    public GeneralTree<TotalBoardState> analyzeChain(LogicalPlayer player, LogicalPlayer rival, GeneralTree<Long> chain, boolean isQueen) {
        // takes an eating chain - generates a general tree with scores (like mini-max)
        int aiMul = (player.isDark()) ? -1 : 1;  // to multiply evaluate return value

        if (chain.isLeaf()) {
            boolean checkQueenMade = (player.isDark()) ? ((BitboardEssentials.DARK_QUEEN & chain.getInfo()) != 0) : ((BitboardEssentials.LIGHT_QUEEN & chain.getInfo()) != 0);
            if (checkQueenMade) {
                this.moveFromPieceToQueen(player, chain.getInfo());
            }
            // to evaluate including the queen-making, then revert
            int score = this.evaluate(player) * aiMul;
            if (checkQueenMade) {
                this.convertQueenToPiece(player, chain.getInfo());
            }

            TotalBoardState t = new TotalBoardState(true);
            t.getDarkState().setPieceBoard(rival.getPieceBoard());
            t.getDarkState().setQueenBoard(rival.getQueenBoard());
            t.getLightState().setPieceBoard(player.getPieceBoard());
            t.getLightState().setQueenBoard(player.getQueenBoard());
            t.getLightState().setScore(score);
            GeneralTree<TotalBoardState> node = new GeneralTree<>(t);
            return node;
        }

        int minimax = (player.isDark()) ? BoardState.MAX_INT : BoardState.MIN_INT;

        TotalBoardState current = new TotalBoardState(!player.isDark());
        current.getDarkState().setPieceBoard(rival.getPieceBoard());
        current.getDarkState().setQueenBoard(rival.getQueenBoard());
        current.getLightState().setPieceBoard(player.getPieceBoard());
        current.getLightState().setQueenBoard(player.getQueenBoard());
        GeneralTree<TotalBoardState> myState = new GeneralTree<>(current);

        for (int i = 0 ; i < 4 ; i++) {
            GeneralTree<Long> destSon = chain.get(i);
            if (destSon != null) {
                long src = chain.getInfo();
                long dest = destSon.getInfo();
                boolean queenEaten = Model.checkIfQueen(rival, Position.findMiddle(src, dest));
                this.makeImaginaryMove(player, src, dest, true, isQueen);
                GeneralTree<TotalBoardState> maxOfSons = this.analyzeChain(player, rival, destSon, isQueen);
                myState.put(maxOfSons);
                if (!player.isDark()) {
                    if (minimax < maxOfSons.getInfo().getLightState().getScore()) {
                        minimax = maxOfSons.getInfo().getLightState().getScore();
                    }
                }
                else {
                    if (minimax > maxOfSons.getInfo().getDarkState().getScore()) {
                        minimax = maxOfSons.getInfo().getDarkState().getScore();
                    }
                }
                this.revertImaginaryMove(player, src, dest, true, queenEaten, false, isQueen);
            }
        }
        if (!player.isDark()) {
            myState.getInfo().getLightState().setScore(minimax);
        }
        else {
            myState.getInfo().getDarkState().setScore(minimax);
        }
        return myState;
    }

    public static GeneralTree<TotalBoardState> findSon(GeneralTree<TotalBoardState> root, LogicalPlayer player, int score) {
        // returns the first son that matches the score
        GeneralTree<TotalBoardState> son = null;
        for (int i = 0 ; i < 4 ; i++) {
            if (root.get(i) != null) {
                if (player.isDark()) {
                    if (((TotalBoardState) root.get(i).getInfo()).getDarkState().getScore() == score) {
                        son = root.get(i);
                    }
                }
                else {
                    if (((TotalBoardState) root.get(i).getInfo()).getLightState().getScore() == score) {
                        son = root.get(i);
                    }
                }
            }
        }
        return son;
    }

    public QueueStack<TotalBoardState> bestChain(GeneralTree<TotalBoardState> analyzeTree, LogicalPlayer player) {
        // returns the best path in an eating chain

        QueueStack<TotalBoardState> bestPath = new QueueStack<>();
        while (analyzeTree != null) {
            bestPath.insert(analyzeTree.getInfo());
            if (player.isDark()) {
                analyzeTree = Model.findSon(analyzeTree, player, analyzeTree.getInfo().getDarkState().getScore());
            }
            else {
                analyzeTree = Model.findSon(analyzeTree, player, analyzeTree.getInfo().getLightState().getScore());
            }
        }
        return bestPath;
    }

    public LinearLinkedList<BitMove> generateAIMove(LogicalPlayer cur) {

        LogicalPlayer rival = this.getRival(cur);

        long curPieces = cur.getPieceBoard();
        long curQueens = cur.getQueenBoard();
        long curAdj = cur.getAdjacentToRival();
        long curMust = cur.getMustEatPieces();

        long rivPieces = rival.getPieceBoard();;
        long rivQueens = rival.getQueenBoard();
        long rivAdj = rival.getAdjacentToRival();
        long rivMust = rival.getMustEatPieces();

        BoardState move = this.generateAIMove(cur, this.getRival(cur), BoardState.MIN_INT, BoardState.MAX_INT, Model.DEFAULT_SEARCH_DEPTH);

        cur.setPieceBoard(curPieces);
        cur.setQueenBoard(curQueens);
        cur.setAdjacentToRival(curAdj);
        cur.setMustEatPieces(curMust);
        rival.setPieceBoard(rivPieces);
        rival.setQueenBoard(rivQueens);
        rival.setAdjacentToRival(rivAdj);
        rival.setMustEatPieces(rivMust);

        long totalBoardDest = move.getPieceBoard() | move.getQueenBoard();
        long totalBoardSrc = cur.getTotalBoard();
        BitMove aiMove = Model.calculateMove(totalBoardSrc, totalBoardDest);

        LinearLinkedList<BitMove> moves = new LinearLinkedList<>();

        boolean isQueen = Model.checkIfQueen(cur, aiMove.getSource());
        // check for chain in first move of AI
        if (this.validEatingMove(cur, aiMove.getSource(), aiMove.getDestination(), Model.checkIfQueen(cur, aiMove.getSource()))) {
            GeneralTree<Long> chain = this.chain(cur, aiMove.getDestination(), aiMove.getSource());
            boolean ateQueen = Model.checkIfQueen(this.getRival(cur), Position.findMiddle(aiMove.getSource(), aiMove.getDestination()));
            if (chain != null) {
                this.makeImaginaryMove(cur, aiMove.getSource(), aiMove.getDestination(), true, isQueen);
                GeneralTree<TotalBoardState> chainScores = this.analyzeChain(cur, this.getRival(cur), chain, Model.checkIfQueen(cur, aiMove.getSource()));
                QueueStack<TotalBoardState> bestPath = this.bestChain(chainScores, cur);
                LinearLinkedList<BitMove> chainMoves = this.getBestChain(cur, bestPath, Model.checkIfQueen(cur, aiMove.getSource()));
                moves = chainMoves;
                moves.push(aiMove);
                boolean madeQueen = (cur.isDark()) ? ((BitboardEssentials.LIGHT_QUEEN & Model.getLastDestinationFromPath(bestPath)) != 0) : ((BitboardEssentials.DARK_QUEEN & Model.getLastDestinationFromPath(bestPath)) != 0);
                // this.revertImaginaryMove(cur, aiMove.getSource(), aiMove.getDestination(), true, ateQueen, madeQueen, isQueen);
                cur.setPieceBoard(curPieces);
                cur.setQueenBoard(curQueens);
                cur.setAdjacentToRival(curAdj);
                cur.setMustEatPieces(curMust);
                rival.setPieceBoard(rivPieces);
                rival.setQueenBoard(rivQueens);
                rival.setAdjacentToRival(rivAdj);
                rival.setMustEatPieces(rivMust);
            } else {
                moves.push(aiMove);
            }
        }
        else if (this.validMove(cur, aiMove.getSource(), aiMove.getDestination())){
            moves.push(aiMove);
        }
        return moves;
    }

    public void dealWithPath(LogicalPlayer player, LogicalPlayer rival, QueueStack<TotalBoardState> path) {
        // goes through path and changes boards accordingly
        TotalBoardState initial = path.pop();
        int size = path.getSize();
        path.insert(initial);
        for (int i = 0 ; i < size ; i++) {
            TotalBoardState move = path.pop();
            BitMove eatMove = Model.calculateMove(initial.getLightState().getTotalBoard(), move.getLightState().getTotalBoard());
            long src = eatMove.getSource();
            long dest = eatMove.getDestination();
            boolean isQueen = Model.checkIfQueen(player, src);
            long eaten = Position.findMiddle(src, dest);
            this.removePiece(rival, eaten);
            this.removePiece(player, src);
            this.placePiece(player, dest, isQueen);

            // so that the function doesn't delete the path
            path.insert(move);

            initial = move;
        }
    }

    public static long getLastDestinationFromPath(QueueStack<TotalBoardState> path) {
        TotalBoardState finalState = path.remove();
        TotalBoardState prierState = path.remove();
        path.insert(prierState);
        path.insert(finalState);
        BitMove move = Model.calculateMove(prierState.getLightState().getTotalBoard(), finalState.getLightState().getTotalBoard());
        return move.getDestination();
    }

    public void undoPath(LogicalPlayer player, LogicalPlayer rival, QueueStack<TotalBoardState> path, boolean madeQueen) {
        // undoes the path of eating chain
        if (madeQueen) {
            long queenPos = Model.getLastDestinationFromPath(path);
            this.convertQueenToPiece(player, queenPos);
        }
        TotalBoardState finalState = path.remove();
        int size = path.getSize();
        path.push(finalState);
        for (int i = 0 ; i < size ; i++) {
            TotalBoardState reverseMove = path.remove();
            BitMove eatMove = Model.calculateMove(reverseMove.getLightState().getTotalBoard(), finalState.getLightState().getTotalBoard());
            long ogSrc = eatMove.getSource();
            long ogDest = eatMove.getDestination();
            boolean isQueen = Model.checkIfQueen(player, ogDest);
            boolean queenEaten;
            long eaten = Position.findMiddle(ogSrc, ogDest);
            if (!player.isDark()) {
                queenEaten = (reverseMove.getDarkState().getQueenBoard() & eaten) != 0;
            }
            else {
                queenEaten = (reverseMove.getLightState().getQueenBoard() & eaten) != 0;
            }

            this.removePiece(player, ogDest);
            this.placePiece(rival, eaten, queenEaten);
            this.placePiece(player, ogSrc, isQueen);

            path.push(reverseMove);

            finalState = reverseMove;
        }
    }

    public static int max(int number1, int number2) {
        // returns maximum out of number1 and number2
        return (number1 > number2) ? number1 : number2;
    }

    public static int min(int number1, int number2) {
        // returns minimum out of number1 and number2
        return (number1 > number2) ? number2 : number1;
    }

    public BoardState generateAIMove(LogicalPlayer player, LogicalPlayer rival, int alpha, int beta, int depth) {

        BoardState minmax = new BoardState(!player.isDark());

        BoardState dark = new BoardState(player.getPieceBoard(), player.getQueenBoard(), 0);
        BoardState light = new BoardState(rival.getPieceBoard(), rival.getQueenBoard(), 0);

        EntryTranspositionTable entryTranspositionTable;

        EntryTranspositionTable stateScore = this.transpositionTableHashMap.get(TranspositionTable.calcZobristCode(new TotalBoardState(dark, light)));
        if (stateScore != null && stateScore.getDepth() >= depth) {
            if (stateScore.getFlag() == EntryTranspositionTable.exact) {
                minmax.setScore(stateScore.getScore());
                minmax.setPieceBoard(player.getPieceBoard());
                minmax.setQueenBoard(player.getQueenBoard());
                return minmax;
            }
            if (stateScore.getFlag() == EntryTranspositionTable.lower && stateScore.getScore() > alpha) {
                alpha = stateScore.getScore();
            }
            if (stateScore.getFlag() == EntryTranspositionTable.upper && stateScore.getScore() < beta) {
                beta = stateScore.getScore();
            }
            if (beta >= alpha) {
                minmax.setScore(stateScore.getScore());
                return minmax;
            }
        }

        if (player.getTotalBoard() == 0) {  // lose for current player
            return this.generateAIMove(rival, player, alpha, beta, depth);
        }
        if (rival.getTotalBoard() == 0) {  // win for current player
            int ai = (player.isDark()) ? -1 : 1;
            int v = ai * this.evaluate(player) * (Model.DEFAULT_SEARCH_DEPTH - depth) * 4;
            return new BoardState(player.getPieceBoard(), player.getQueenBoard(), v);
        }
        if (depth == 0) {

            int ai = (player.isDark()) ? -1 : 1;
            minmax = new BoardState(player.getPieceBoard(), player.getQueenBoard(), ai * this.evaluate(player));
            dark = new BoardState(player.getPieceBoard(), player.getQueenBoard(), minmax.getScore());
            light = new BoardState(rival.getPieceBoard(), rival.getQueenBoard(), 0);
            if (minmax.getScore() <= alpha) {
                entryTranspositionTable = new EntryTranspositionTable(minmax.getScore(), 0, EntryTranspositionTable.lower);
                this.transpositionTableHashMap.put(TranspositionTable.calcZobristCode(new TotalBoardState(dark, light)), entryTranspositionTable);
            }
            else if (minmax.getScore() >= beta) {
                entryTranspositionTable = new EntryTranspositionTable(minmax.getScore(), 0, EntryTranspositionTable.upper);
                this.transpositionTableHashMap.put(TranspositionTable.calcZobristCode(new TotalBoardState(dark, light)), entryTranspositionTable);
            }
            else {
                entryTranspositionTable = new EntryTranspositionTable(minmax.getScore(), 0, EntryTranspositionTable.exact);
                this.transpositionTableHashMap.put(TranspositionTable.calcZobristCode(new TotalBoardState(dark, light)), entryTranspositionTable);
            }
        }

        BoardState temp;


        long mustEat = this.generateMustEatTilesAndPieces(player, rival);
        long mustEatPieces = player.getMustEatPieces();
        long pieces = player.getPieceBoard();
        long queens = player.getQueenBoard();
        long rivalPieces = rival.getPieceBoard();
        long rivalQueens = rival.getQueenBoard();

        if (mustEat != 0) {  // have to choose between eating moves
            int index;
            long pos;
            while (mustEatPieces > 0) {
                index = (int) BitboardEssentials.log2(mustEatPieces);
                pos = 1L << index;
                boolean isQueen = Model.checkIfQueen(player, pos);
                long possibleMoves = this.possibleMoves(player, pos, isQueen);
                long eatingDestinations = Model.filterToEatingOnly(pos, possibleMoves);
                while (eatingDestinations > 0) {
                    int i = (int) BitboardEssentials.log2(eatingDestinations);
                    long dest = 1L << i;
                    boolean ateQueen = Model.checkIfQueen(rival, Position.findMiddle(pos, dest));
                    GeneralTree<Long> eatingChain = this.chain(player, dest, pos);
                    boolean madeQueen;
                    QueueStack<TotalBoardState> bestRoute = null;

                    madeQueen = this.makeImaginaryMove(player, pos, dest, true, isQueen);
                    if (eatingChain != null) {
                        GeneralTree<TotalBoardState> analyzedChain = this.analyzeChain(player, rival, eatingChain, isQueen);
                        bestRoute = this.bestChain(analyzedChain, player);
                        long lastPosition = Model.getLastDestinationFromPath(bestRoute);
                        this.dealWithPath(player, rival, bestRoute);
                        long queenMakingRow = (player.isDark()) ? (BitboardEssentials.LIGHT_BOTTOM_ROW) : (BitboardEssentials.DARK_BOTTOM_ROW);
                        long lastMoveQueen = queenMakingRow & player.getPieceBoard();
                        madeQueen = (lastMoveQueen != 0);
                        if (madeQueen) {
                            this.moveFromPieceToQueen(player, lastPosition);
                        }
                    }
                    temp = this.generateAIMove(rival, player, alpha, beta, depth - 1);
                    if (player.isDark()) {  // ---> then, find minimum
                        if (temp.getScore() < minmax.getScore()) {
                            minmax.update(temp);
                            minmax.setPieceBoard(player.getPieceBoard());
                            minmax.setQueenBoard(player.getQueenBoard());
                        }
                        beta = min(beta, temp.getScore());
                        if (beta <= alpha) {
                            if (bestRoute != null) {
                                this.undoPath(player, rival, bestRoute, madeQueen);
                                minmax.setPieceBoard(player.getPieceBoard());
                                minmax.setQueenBoard(player.getQueenBoard());
                            }
//                            minmax.setPieceBoard(player.getPieceBoard());
//                            minmax.setQueenBoard(player.getQueenBoard());
                            // this.revertImaginaryMove(player, pos, dest, true, ateQueen, madeQueen, isQueen);
                            player.setPieceBoard(pieces);
                            player.setQueenBoard(queens);
                            rival.setPieceBoard(rivalPieces);
                            rival.setQueenBoard(rivalQueens);
                            player.setMustEatPieces(mustEatPieces);
                            break;
                        }
                    } else {  // find maximum
                        if (temp.getScore() > minmax.getScore()) {
                            minmax.update(temp);
                            minmax.setPieceBoard(player.getPieceBoard());
                            minmax.setQueenBoard(player.getQueenBoard());
                            if (minmax.getScore() <= alpha) {
                                entryTranspositionTable = new EntryTranspositionTable(minmax.getScore(), 0, EntryTranspositionTable.lower);
                                this.transpositionTableHashMap.put(TranspositionTable.calcZobristCode(new TotalBoardState(dark, light)), entryTranspositionTable);
                            }
                            else if (minmax.getScore() >= beta) {
                                entryTranspositionTable = new EntryTranspositionTable(minmax.getScore(), 0, EntryTranspositionTable.upper);
                                this.transpositionTableHashMap.put(TranspositionTable.calcZobristCode(new TotalBoardState(dark, light)), entryTranspositionTable);
                            }
                            else {
                                entryTranspositionTable = new EntryTranspositionTable(minmax.getScore(), 0, EntryTranspositionTable.exact);
                                this.transpositionTableHashMap.put(TranspositionTable.calcZobristCode(new TotalBoardState(dark, light)), entryTranspositionTable);
                            }
                        }
                        alpha = max(alpha, temp.getScore());
                        if (beta <= alpha) {
                            if (bestRoute != null) {
                                this.undoPath(player, rival, bestRoute, madeQueen);
                                minmax.setPieceBoard(player.getPieceBoard());
                                minmax.setQueenBoard(player.getQueenBoard());
                                if (minmax.getScore() <= alpha) {
                                    entryTranspositionTable = new EntryTranspositionTable(minmax.getScore(), 0, EntryTranspositionTable.lower);
                                    this.transpositionTableHashMap.put(TranspositionTable.calcZobristCode(new TotalBoardState(dark, light)), entryTranspositionTable);
                                }
                                else if (minmax.getScore() >= beta) {
                                    entryTranspositionTable = new EntryTranspositionTable(minmax.getScore(), 0, EntryTranspositionTable.upper);
                                    this.transpositionTableHashMap.put(TranspositionTable.calcZobristCode(new TotalBoardState(dark, light)), entryTranspositionTable);
                                }
                                else {
                                    entryTranspositionTable = new EntryTranspositionTable(minmax.getScore(), 0, EntryTranspositionTable.exact);
                                    this.transpositionTableHashMap.put(TranspositionTable.calcZobristCode(new TotalBoardState(dark, light)), entryTranspositionTable);
                                }
                            }
//                            minmax.setPieceBoard(player.getPieceBoard());
//                            minmax.setQueenBoard(player.getQueenBoard());
                            // this.revertImaginaryMove(player, pos, dest, true, ateQueen, madeQueen, isQueen);
                            player.setPieceBoard(pieces);
                            player.setQueenBoard(queens);
                            rival.setPieceBoard(rivalPieces);
                            rival.setQueenBoard(rivalQueens);
                            player.setMustEatPieces(mustEatPieces);
                            break;
                        }
                    }
                    if (bestRoute != null) {
                        this.undoPath(player, rival, bestRoute, madeQueen);
                        minmax.setPieceBoard(player.getPieceBoard());
                        minmax.setQueenBoard(player.getQueenBoard());
                    }
//                    minmax.setPieceBoard(player.getPieceBoard());
//                    minmax.setQueenBoard(player.getQueenBoard());
                    // this.revertImaginaryMove(player, pos, dest, true, ateQueen, madeQueen, isQueen);
                    player.setPieceBoard(pieces);
                    player.setQueenBoard(queens);
                    rival.setPieceBoard(rivalPieces);
                    rival.setQueenBoard(rivalQueens);
                    player.setMustEatPieces(mustEatPieces);
                    eatingDestinations -= dest;
                }
                mustEatPieces -= pos;
            }
        } else {
            long allPieces = player.getTotalBoard();
            while (allPieces > 0) {
                int i = (int) BitboardEssentials.log2(allPieces);
                long pos = 1L << i;
                boolean isQueen = Model.checkIfQueen(player, pos);
                long possibleMoves = this.possibleMoves(player, pos, isQueen);
                while (possibleMoves > 0) {
                    int j = (int) BitboardEssentials.log2(possibleMoves);
                    long dest = 1L << j;
                    boolean eatingMove = Model.isEatingMove(pos, dest);
                    boolean ateQueen = (eatingMove) ? (Model.checkIfQueen(rival, Position.findMiddle(pos, dest))) : false;
                    boolean madeQueen = this.makeImaginaryMove(player, pos, dest, eatingMove, isQueen);
                    temp = this.generateAIMove(rival, player, alpha, beta, depth - 1);
                    if (player.isDark()) {  // ---> then, find minimum
                        if (temp.getScore() < minmax.getScore()) {
                            minmax.update(temp);
                            minmax.setPieceBoard(player.getPieceBoard());
                            minmax.setQueenBoard(player.getQueenBoard());
                            if (minmax.getScore() <= alpha) {
                                entryTranspositionTable = new EntryTranspositionTable(minmax.getScore(), 0, EntryTranspositionTable.lower);
                                this.transpositionTableHashMap.put(TranspositionTable.calcZobristCode(new TotalBoardState(dark, light)), entryTranspositionTable);
                            }
                            else if (minmax.getScore() >= beta) {
                                entryTranspositionTable = new EntryTranspositionTable(minmax.getScore(), 0, EntryTranspositionTable.upper);
                                this.transpositionTableHashMap.put(TranspositionTable.calcZobristCode(new TotalBoardState(dark, light)), entryTranspositionTable);
                            }
                            else {
                                entryTranspositionTable = new EntryTranspositionTable(minmax.getScore(), 0, EntryTranspositionTable.exact);
                                this.transpositionTableHashMap.put(TranspositionTable.calcZobristCode(new TotalBoardState(dark, light)), entryTranspositionTable);
                            }
                        }
                        beta = min(beta, temp.getScore());
                        if (beta <= alpha) {
//                            minmax.setPieceBoard(player.getPieceBoard());
//                            minmax.setQueenBoard(player.getQueenBoard());
                            // this.revertImaginaryMove(player, pos, dest, eatingMove, ateQueen, madeQueen, isQueen);
                            player.setPieceBoard(pieces);
                            player.setQueenBoard(queens);
                            rival.setPieceBoard(rivalPieces);
                            rival.setQueenBoard(rivalQueens);
                            player.setMustEatPieces(mustEatPieces);
                            break;
                        }
                    } else {  // find maximum
                        if (temp.getScore() > minmax.getScore()) {
                            minmax.update(temp);
                            minmax.setPieceBoard(player.getPieceBoard());
                            minmax.setQueenBoard(player.getQueenBoard());
                            if (minmax.getScore() <= alpha) {
                                entryTranspositionTable = new EntryTranspositionTable(minmax.getScore(), 0, EntryTranspositionTable.lower);
                                this.transpositionTableHashMap.put(TranspositionTable.calcZobristCode(new TotalBoardState(dark, light)), entryTranspositionTable);
                            }
                            else if (minmax.getScore() >= beta) {
                                entryTranspositionTable = new EntryTranspositionTable(minmax.getScore(), 0, EntryTranspositionTable.upper);
                                this.transpositionTableHashMap.put(TranspositionTable.calcZobristCode(new TotalBoardState(dark, light)), entryTranspositionTable);
                            }
                            else {
                                entryTranspositionTable = new EntryTranspositionTable(minmax.getScore(), 0, EntryTranspositionTable.exact);
                                this.transpositionTableHashMap.put(TranspositionTable.calcZobristCode(new TotalBoardState(dark, light)), entryTranspositionTable);
                            }
                        }
                        alpha = max(alpha, temp.getScore());
                        if (beta <= alpha) {
//                            minmax.setPieceBoard(player.getPieceBoard());
//                            minmax.setQueenBoard(player.getQueenBoard());
                            // this.revertImaginaryMove(player, pos, dest, eatingMove, ateQueen, madeQueen, isQueen);
                            player.setPieceBoard(pieces);
                            player.setQueenBoard(queens);
                            rival.setPieceBoard(rivalPieces);
                            rival.setQueenBoard(rivalQueens);
                            player.setMustEatPieces(mustEatPieces);
                            break;
                        }
                    }

                    // this.revertImaginaryMove(player, pos, dest, eatingMove, ateQueen, madeQueen, isQueen);
                    player.setPieceBoard(pieces);
                    player.setQueenBoard(queens);
                    rival.setPieceBoard(rivalPieces);
                    rival.setQueenBoard(rivalQueens);
                    player.setMustEatPieces(mustEatPieces);
                    possibleMoves -= dest;
                }
                allPieces -= pos;
            }
        }
        return minmax;
    }

    public int pieceAmount(LogicalPlayer player) {
        int counter = 0;
        long p = player.getPieceBoard();
        while (p > 0) {
            if (p % 2 == 1) {
                counter++;
            }
            p >>= 1;
        }
        return counter;
    }

    public int queenAmount(LogicalPlayer player) {
        int counter = 0;
        long p = player.getQueenBoard();
        while (p > 0) {
            if (p % 2 == 1) {
                counter++;
            }
            p >>= 1;
        }
        return counter;
    }

    public int safePieces(LogicalPlayer player) {
        int counter = 0;
        long p = player.getPieceBoard();
        p &= BitboardEssentials.BOARD_EDGES;
        while (p > 0) {
            if (p % 2 == 1) {
                counter++;
            }
            p >>= 1;
        }
        return counter;
    }

    public int safeQueens(LogicalPlayer player) {
        int counter = 0;
        long p = player.getQueenBoard();
        p &= BitboardEssentials.BOARD_EDGES;
        while (p > 0) {
            if (p % 2 == 1) {
                counter++;
            }
            p >>= 1;
        }
        return counter;
    }

    public int occuipiedButtom(LogicalPlayer player) {
        long mask = (player.isDark()) ? (player.getTotalBoard() & BitboardEssentials.DARK_QUEEN) : (player.getTotalBoard() & BitboardEssentials.LIGHT_QUEEN);
        int counter = 0;
        while (mask > 0) {
            if (mask % 2 == 1) {
                counter++;
            }
            mask >>= 1;
        }
        return counter;
    }

    public int defenderPieces(LogicalPlayer player) {
        long mask = (player.isDark()) ? (player.getPieceBoard() & BitboardEssentials.DARK_DEFENDERS) : (player.getPieceBoard() & BitboardEssentials.LIGHT_DEFENDERS);
        int counter = 0;
        while (mask > 0) {
            if (mask % 2 == 1) {
                counter++;
            }
            mask >>= 1;
        }
        return counter;
    }

    public int defenderQueens(LogicalPlayer player) {
        long mask = (player.isDark()) ? (player.getQueenBoard() & BitboardEssentials.DARK_DEFENDERS) : (player.getQueenBoard() & BitboardEssentials.LIGHT_DEFENDERS);
        int counter = 0;
        while (mask > 0) {
            if (mask % 2 == 1) {
                counter++;
            }
            mask >>= 1;
        }
        return counter;
    }

    public int attackingPieces(LogicalPlayer player) {
        long mask = (player.isDark()) ? (player.getPieceBoard() & BitboardEssentials.DARK_ATTACKERS) : (player.getPieceBoard() & BitboardEssentials.LIGHT_ATTACKERS);
        int counter = 0;
        while (mask > 0) {
            if (mask % 2 == 1) {
                counter++;
            }
            mask >>= 1;
        }
        return counter;
    }

    public int attackingQueens(LogicalPlayer player) {
        long mask = (player.isDark()) ? (player.getQueenBoard() & BitboardEssentials.DARK_ATTACKERS) : (player.getQueenBoard() & BitboardEssentials.LIGHT_ATTACKERS);
        int counter = 0;
        while (mask > 0) {
            if (mask % 2 == 1) {
                counter++;
            }
            mask >>= 1;
        }
        return counter;
    }

    public int center(LogicalPlayer player) {
        long mask = player.getTotalBoard() & BitboardEssentials.BOARD_CENTER;
        int counter = 0;
        while (mask > 0) {
            if (mask % 2 == 1) {
                counter++;
            }
            mask >>= 1;
        }
        return counter;
    }

    public int centerSides(LogicalPlayer player) {
        long mask = player.getTotalBoard() & BitboardEssentials.BOARD_CENTER_SIDES;
        int counter = 0;
        while (mask > 0) {
            if (mask % 2 == 1) {
                counter++;
            }
            mask >>= 1;
        }
        return counter;
    }

    public int max(int a, int b, int c, int d) {
        return Math.max(Math.max(a, b), Math.max(c, d));
    }

    public int maxSizeChain(GeneralTree<Long> chain) {
        if (chain == null) {
            return 0;
        }
        return 1 + max(maxSizeChain(chain.get(0)), maxSizeChain(chain.get(1)), maxSizeChain(chain.get(2)), maxSizeChain(chain.get(3)));
    }

    public boolean canMakeQueenInTheEnd(GeneralTree<Long> chain, LogicalPlayer player) {
        if (chain == null) {
            return false;
        }
        if (chain.isLeaf()) {
            return (player.isDark()) ? ((BitboardEssentials.DARK_QUEEN & chain.getInfo()) != 0) : ((BitboardEssentials.LIGHT_QUEEN & chain.getInfo()) != 0);
        }
        return canMakeQueenInTheEnd(chain.get(0), player) || canMakeQueenInTheEnd(chain.get(1), player) || canMakeQueenInTheEnd(chain.get(2), player) || canMakeQueenInTheEnd(chain.get(3), player);
    }

    public int analyzeMustEat(LogicalPlayer player) {

        int finalScore = 0;

        LogicalPlayer rival = this.getRival(player);

        long mustEatOriginal = player.getMustEatPieces();

        long destinations = this.generateMustEatTilesAndPieces(player, rival);
        long pieces = player.getMustEatPieces();
        player.setMustEatPieces(mustEatOriginal);

        int pieceIndex;
        long piecePosition;
        int destinationIndex;
        long destinationPosition;

        while (pieces > 0) {
            pieceIndex = (int) BitboardEssentials.log2(pieces);
            piecePosition = 1L << pieceIndex;
            long currentDestinations = destinations & (BitboardEssentials.getCorners(piecePosition, BitboardEssentials.CHECK_EAT_DIAMETER));
            while (currentDestinations > 0) {
                destinationIndex = (int) BitboardEssentials.log2(currentDestinations);
                destinationPosition = 1L << destinationIndex;
                boolean ateQueen = Model.checkIfQueen(this.getRival(player), Position.findMiddle(piecePosition, destinationPosition));
                boolean isQueen = Model.checkIfQueen(player, piecePosition);
                boolean madeQueen = this.makeImaginaryMove(player, piecePosition, destinationPosition, true, Model.checkIfQueen(player, piecePosition));
                GeneralTree<Long> chain = this.chain(player, destinationPosition, piecePosition);
                int length = this.maxSizeChain(chain) + 1;
                finalScore += length * 3;
                boolean canMakeQueen = this.canMakeQueenInTheEnd(chain, player);
                finalScore = canMakeQueen ? (finalScore + 10) : finalScore;
                this.revertImaginaryMove(player, piecePosition, destinationPosition, true, ateQueen, madeQueen, isQueen);
                currentDestinations -= destinationPosition;
            }
            pieces -= piecePosition;
        }
        return finalScore;
    }

    public boolean movable(LogicalPlayer player) {

        if (player.getMustEatPieces() > 0) {
            return true;
        }

        LogicalPlayer rival = this.getRival(player);

        long pieces = player.getPieceBoard();
        long queens = player.getQueenBoard();

        long pos;
        int index;
        while (pieces > 0) {
            index = (int) BitboardEssentials.log2(pieces);
            pos = 1L << index;
            long destinations = this.possibleMoves(player, pos, false);
            while (destinations > 0) {
                int destIndex = (int) BitboardEssentials.log2(destinations);
                long dest = 1L << destIndex;
                if (this.validMove(player, pos, dest)) {
                    return true;
                }
                destinations -= dest;
            }
            pieces -= pos;
        }
        while (queens > 0) {
            index = (int) BitboardEssentials.log2(queens);
            pos = 1L << index;
            long destinations = this.possibleMoves(player, pos, true);
            while (destinations > 0) {
                int destIndex = (int) BitboardEssentials.log2(destinations);
                long dest = 1L << destIndex;
                if (this.validMove(player, pos, dest)) {
                    return true;
                }
                destinations -= dest;
            }
            pieces -= pos;
        }
        return false;
    }
}

