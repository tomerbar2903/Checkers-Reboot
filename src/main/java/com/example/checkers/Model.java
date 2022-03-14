package com.example.checkers;

public class Model implements IModel {

    private LogicalPlayer player1;
    private LogicalPlayer player2;
    private LogicalPlayer currentTurn;
    private boolean turnAI;

    public enum AdjacencyType {TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, NOT_ADJACENT}

    public Model() {
        this.player1 = new LogicalPlayer();
        this.player2 = new LogicalPlayer();
        this.currentTurn = this.player2;
        this.printBoard();
        this.turnAI = true;
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
                root.put(eatingDest, sonIndex);

                // call the function for the son
                buildChainRegularPiece(root.get(sonIndex), player, positionInBitboard);

                // places back pieces on the board
                this.placePiece(player, src, false);
                this.placePiece(this.getRival(player), eaten, rivalQueen);

                // increments the index for later inserting operations on SONS array of the tree
                sonIndex++;
            }
            // remove this current destination from the mask
            possibleEatingDestinations -= positionInBitboard;
        }
    }

    public void buildChainQueenPiece(GeneralTree<Long> root, LogicalPlayer player, long src) {
        // return a tree of all possible eating moves from src.

        // get possible landing destinations after an eating move
        long possibleEatingDestinations = BitboardEssentials.getCorners(src, BitboardEssentials.CHECK_EAT_DIAMETER);

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
                root.put(eatingDest, sonIndex);

                // call the function for the son
                buildChainQueenPiece(root.get(sonIndex), player, positionInBitboard);

                // places back pieces on the board
                this.placePiece(player, src, true);
                this.placePiece(this.getRival(player), eaten, rivalQueen);

                // increments the index for later inserting operations on SONS array of the tree
                sonIndex++;
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
            this.placePiece(this.getRival(), rivalPosition, rivalQueenEaten);
            this.placePiece(player, realSrc, true);
        } else {
            this.buildChainRegularPiece(possibleChains, player, srcChain);
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
        boolean checkRivalExistence = this.checkRivalInTheMiddle(src, dest);

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
    }

    public boolean emptyPosition(long position) {
        // returns true if no other piece is in this position
        boolean piece1, piece2, queen1, queen2;
        piece1 = ((position & this.player1.getPieceBoard()) == 0);
        piece2 = ((position & this.player2.getPieceBoard()) == 0);
        queen1 = ((position & this.player1.getQueenBoard()) == 0);
        queen2 = ((position & this.player2.getQueenBoard()) == 0);
        return piece1 && piece2 && queen1 && queen2;
    }

    public void updateBoards(LogicalPlayer player, long src, long dest) {
        // moves and updates boards according to move
        boolean queen = ((player.getQueenBoard() & src) != 0);  // is it a queen?

        // moves bits (changes board according to move)
        if (queen)  // move piece in queen board
        {
            /* updates destination piece */
            long check1 = player.getQueenBoard() | dest;  // this OR dest
            /* deletes source piece */
            long oppositeSrc = ~src;  // NOT src
            player.setQueenBoard(check1 & oppositeSrc);  // final AND NOT src
        } else {  // move piece in piece board
            /* updates destination piece */
            long check1 = player.getPieceBoard() | dest;  // this OR dest
            /* deletes source piece */
            long oppositeSrc = ~src;  // NOT src
            player.setPieceBoard(check1 & oppositeSrc);  // final AND NOT src
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

    public AdjacencyType getAdjacencyType(long pos1, long pos2) {
        // returns the adjacency type between pos1 (src) pos2 (relative)
        AdjacencyType adjacencyType = (pos1 << (VisualBoard.getDimension() + 1) == pos2) ? AdjacencyType.TOP_LEFT : AdjacencyType.NOT_ADJACENT;
        adjacencyType = (pos1 << (VisualBoard.getDimension() - 1) == pos2) ? AdjacencyType.TOP_RIGHT : adjacencyType;
        adjacencyType = (pos1 >> (VisualBoard.getDimension() - 1) == pos2) ? AdjacencyType.BOTTOM_LEFT : adjacencyType;
        adjacencyType = (pos1 >> (VisualBoard.getDimension() + 1) == pos2) ? AdjacencyType.BOTTOM_RIGHT : adjacencyType;
        return adjacencyType;
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

    public long generateMustEatTilesAndPieces() {
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
        long curAdjacentToRival = this.currentTurn.getAdjacentToRival();
        int indexCur = 0, indexRiv = 0, indexBlank = 0;
        long positionInBitboardCur, positionInBitboardRival, positionInBitboardBlank;
        long rivalAdjacentPieces;
        long blanks;
        LogicalPlayer cur = this.currentTurn;
        LogicalPlayer riv = this.getRival();
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
                for (int j = 0; j < 4 && blanks > 0; i++) {
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
}

