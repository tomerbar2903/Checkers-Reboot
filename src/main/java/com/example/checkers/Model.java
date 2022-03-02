package com.example.checkers;

public class Model implements IModel{

    private LogicalPlayer player1;
    private LogicalPlayer player2;
    private LogicalPlayer currentTurn;

    public Model()
    {
        this.player1 = new LogicalPlayer();
        this.player2 = new LogicalPlayer();
        this.currentTurn = this.player2;
        this.printBoard();
    }

    public static boolean checkIfQueen(LogicalPlayer player, long pos)
    {
        // returns true if pos in queen piece
        return ((player.getQueenBoard() & pos) != 0);
    }

    @Override
    public boolean validMove(LogicalPlayer player, long src, long dest) {
        // checks if a move is valid
        boolean queen = Model.checkIfQueen(player, src);  // is it a queen?

        // checks if destination is empty
        boolean valid = this.emptyPosition(dest);

        // destinations check masks
        long maskDark1 = dest << (BitboardEssentials.VALID_STEP1);  // moves destination one way towards the src  (up)
        long maskDark2 = dest << (BitboardEssentials.VALID_STEP2);  // moves destination one way towards the src  (up)
        long maskLight1 = dest >> (BitboardEssentials.VALID_STEP1);  // moves destination one way towards the src  (down)
        long maskLight2 = dest >> (BitboardEssentials.VALID_STEP2);  // moves destination one way towards the src  (down)

        if (!queen && valid)  // if a regular piece + destination is empty
        {
            // sets valid true if:
            //      dark -> the manipulation over dest has gotten to dest (forwards)
            //      light -> the manipulation over dest has gotten to dest (backwards)
            valid = (player.isDark()) ? ((maskDark1 & src) != 0 || (maskDark2 & src) != 0) : ((maskLight1 & src) != 0 || (maskLight2 & src) != 0);
        }

        else if (queen && valid)  // piece is a queen - moves all directions (1 only)
        {
            // if any of the four surrounding the piece is the chosen one
            valid = ((maskDark1 & src) != 0 || (maskDark2 & src) != 0 || (maskLight1 & src) != 0 || (maskLight2 & src) != 0);
        }
        return valid;
    }

    public static boolean checkOutOfBounds(long pos)
    {
        // checks if a shift result puts the position in a white tile
        return ((pos & BitboardEssentials.WHITE_TILES) == 0);
    }

    public void buildChainRegularPiece(GeneralTree<Long> root, LogicalPlayer player, long src)
    {
        // return a tree of all possible eating moves from src.
        long mask1, mask2, check1, check2;
        if (root == null)
        {
            return;
        }
        if (src == 0)  // reached the end of boundaries
        {
            return;
        }
        if (player.isDark())
        {
            mask1 = src >> BitboardEssentials.VALID_EATING_STEP1;
            mask2 = src >> BitboardEssentials.VALID_EATING_STEP2;
            // checks if the eating destination of mask1 and mask2 exists in other boards
            check1 = mask1 & (player.getQueenBoard() | player.getPieceBoard() | this.getRival().getPieceBoard() | this.getRival().getQueenBoard());
            if (check1 == 0 && this.checkRivalInTheMiddle(src, mask1))  // checks existence of the rival in the middle
            {
                if ((mask1 != 0) && checkOutOfBounds(mask1)){
                    // adds to tree
                    root.setUpperLeft(new GeneralTree<>(mask1));
                    long rivalPosition = Position.findMiddle(src, mask1);  // the middle between the src and the dest - saves it
                    boolean queenCheck = Model.checkIfQueen(this.getRival(), rivalPosition);  // check if queen was eaten (to know where to remove it)
                    this.removePiece(this.getRival(), rivalPosition);  // removes piece temporarily
                    // builds all sub chains of this path
                    this.buildChainRegularPiece(root.getUpperLeft(), player, mask1);
                    this.placePiece(this.getRival(), rivalPosition, queenCheck);  // places the piece "eaten" in the recursion
                }
                else
                {
                    return;
                }
            }
            check2 = mask2 & (player.getQueenBoard() | player.getPieceBoard() | this.getRival().getPieceBoard() | this.getRival().getQueenBoard());
            if (check2 == 0 && this.checkRivalInTheMiddle(src, mask2))  // checks existence of the rival in the middle
            {
                if ((mask2 != 0) && checkOutOfBounds(mask2)) {
                    // adds to tree
                    root.setUpperRight(new GeneralTree<>(mask2));
                    long rivalPosition = Position.findMiddle(src, mask2);  // the middle between the src and the dest - saves it
                    boolean queenCheck = Model.checkIfQueen(this.getRival(), rivalPosition);  // check if queen was eaten (to know where to remove it)
                    this.removePiece(this.getRival(), rivalPosition);  // removes piece temporarily
                    // builds all sub chains of this path
                    this.buildChainRegularPiece(root.getUpperRight(), player, mask2);
                    this.placePiece(this.getRival(), rivalPosition, queenCheck);  // places the piece "eaten" in the recursion
                }
                else
                {
                    return;
                }
            }
        }
        else
        {
            mask1 = src << BitboardEssentials.VALID_EATING_STEP1;
            mask2 = src << BitboardEssentials.VALID_EATING_STEP2;
            // checks if the eating destination of mask1 and mask2 exists in other boards
            check1 = mask1 & (player.getQueenBoard() | player.getPieceBoard() | this.getRival().getPieceBoard() | this.getRival().getQueenBoard());
            if (check1 == 0 && this.checkRivalInTheMiddle(src, mask1))  // checks existence of the rival in the middle
            {
                if ((mask1 != 0) && checkOutOfBounds(mask1)) {
                    // adds to tree
                    root.setLowerRight(new GeneralTree<>(mask1));
                    long rivalPosition = Position.findMiddle(src, mask1);  // the middle between the src and the dest - saves it
                    boolean queenCheck = Model.checkIfQueen(this.getRival(), rivalPosition);  // check if queen was eaten (to know where to remove it)
                    this.removePiece(this.getRival(), rivalPosition);  // removes piece temporarily
                    // builds all sub chains of this path
                    this.buildChainRegularPiece(root.getLowerRight(), player, mask1);
                    this.placePiece(this.getRival(), rivalPosition, queenCheck);  // places the piece "eaten" in the recursion
                }
                else
                {
                    return;
                }
            }
            check2 = mask2 & (player.getQueenBoard() | player.getPieceBoard() | this.getRival().getPieceBoard() | this.getRival().getQueenBoard());
            if (check2 == 0 && this.checkRivalInTheMiddle(src, mask2))  // checks existence of the rival in the middle
            {
                if ((mask2 != 0) && checkOutOfBounds(mask2)) {
                    // adds to tree
                    root.setLowerLeft(new GeneralTree<>(mask2));
                    long rivalPosition = Position.findMiddle(src, mask2);  // the middle between the src and the dest - saves it
                    boolean queenCheck = Model.checkIfQueen(this.getRival(), rivalPosition);  // check if queen was eaten (to know where to remove it)
                    this.removePiece(this.getRival(), rivalPosition);  // removes piece temporarily
                    // builds all sub chains of this path
                    this.buildChainRegularPiece(root.getLowerLeft(), player, mask2);
                    this.placePiece(this.getRival(), rivalPosition, queenCheck);  // places the piece "eaten" in the recursion
                }
                else
                {
                    return;
                }
            }
        }
    }

    public void buildChainQueenPiece(GeneralTree<Long> root, LogicalPlayer player, long src)
    {
        // return a tree of all possible eating moves from src.
        long mask1, mask2, mask3, mask4, check1, check2, check3, check4;
        mask1 = src >> BitboardEssentials.VALID_EATING_STEP1;  // Upper Left
        mask2 = src >> BitboardEssentials.VALID_EATING_STEP2;  // Upper Right
        mask3 = src << BitboardEssentials.VALID_EATING_STEP1;  // Lower Left
        mask4 = src << BitboardEssentials.VALID_EATING_STEP2;  // Lower Right
        if (root == null)
        {
            return;
        }
        if (src == 0)  // reached the end of boundaries
        {
            return;
        }
        // checks if the eating destination of mask1 and mask2 exists in other boards
        check1 = mask1 & (player.getQueenBoard() | player.getPieceBoard() | this.getRival().getPieceBoard() | this.getRival().getQueenBoard());
        if (check1 == 0 && this.checkRivalInTheMiddle(src, mask1))  // checks existence of the rival in the middle
        {
            if ((mask1 != 0) && checkOutOfBounds(mask1)){
                // adds to tree
                root.setUpperLeft(new GeneralTree<>(mask1));
                long rivalPosition = Position.findMiddle(src, mask1);  // the middle between the src and the dest - saves it
                boolean queenCheck = Model.checkIfQueen(this.getRival(), rivalPosition);  // check if queen was eaten (to know where to remove it)
                this.removePiece(this.getRival(), rivalPosition);  // removes piece temporarily
                // builds all sub chains of this path
                this.buildChainQueenPiece(root.getUpperLeft(), player, mask1);
                this.placePiece(this.getRival(), rivalPosition, queenCheck);  // places the piece "eaten" in the recursion
            }
            else
            {
                return;
            }
        }
        check2 = mask2 & (player.getQueenBoard() | player.getPieceBoard() | this.getRival().getPieceBoard() | this.getRival().getQueenBoard());
        if (check2 == 0 && this.checkRivalInTheMiddle(src, mask2))  // checks existence of the rival in the middle
        {
            if ((mask2 != 0) && checkOutOfBounds(mask2)) {
                // adds to tree
                root.setUpperRight(new GeneralTree<>(mask2));
                long rivalPosition = Position.findMiddle(src, mask2);  // the middle between the src and the dest - saves it
                boolean queenCheck = Model.checkIfQueen(this.getRival(), rivalPosition);  // check if queen was eaten (to know where to remove it)
                this.removePiece(this.getRival(), rivalPosition);  // removes piece temporarily
                // builds all sub chains of this path
                this.buildChainQueenPiece(root.getUpperRight(), player, mask2);
                this.placePiece(this.getRival(), rivalPosition, queenCheck);  // places the piece "eaten" in the recursion
            }
            else
            {
                return;
            }
        }
        // checks if the eating destination of mask1 and mask2 exists in other boards
        check3 = mask3 & (player.getQueenBoard() | player.getPieceBoard() | this.getRival().getPieceBoard() | this.getRival().getQueenBoard());
        if (check3 == 0 && this.checkRivalInTheMiddle(src, mask3))  // checks existence of the rival in the middle
        {
            if ((mask3 != 0) && checkOutOfBounds(mask3)) {
                // adds to tree
                root.setLowerRight(new GeneralTree<>(mask3));
                long rivalPosition = Position.findMiddle(src, mask3);  // the middle between the src and the dest - saves it
                boolean queenCheck = Model.checkIfQueen(this.getRival(), rivalPosition);  // check if queen was eaten (to know where to remove it)
                this.removePiece(this.getRival(), rivalPosition);  // removes piece temporarily
                // builds all sub chains of this path
                this.buildChainQueenPiece(root.getLowerRight(), player, mask3);
                this.placePiece(this.getRival(), rivalPosition, queenCheck);  // places the piece "eaten" in the recursion
            }
            else
            {
                return;
            }
        }
        check4 = mask4 & (player.getQueenBoard() | player.getPieceBoard() | this.getRival().getPieceBoard() | this.getRival().getQueenBoard());
        if (check4 == 0 && this.checkRivalInTheMiddle(src, mask4))  // checks existence of the rival in the middle
        {
            if ((mask4 != 0) && checkOutOfBounds(mask4)) {
                // adds to tree
                root.setLowerLeft(new GeneralTree<>(mask4));
                long rivalPosition = Position.findMiddle(src, mask4);  // the middle between the src and the dest - saves it
                boolean queenCheck = Model.checkIfQueen(this.getRival(), rivalPosition);  // check if queen was eaten (to know where to remove it)
                this.removePiece(this.getRival(), rivalPosition);  // removes piece temporarily
                // builds all sub chains of this path
                this.buildChainQueenPiece(root.getLowerLeft(), player, mask4);
                this.placePiece(this.getRival(), rivalPosition, queenCheck);  // places the piece "eaten" in the recursion
            }
            else
            {
                return;
            }
        }

    }

    public boolean checkRivalInTheMiddle(long src, long dest)
    {
        // returns true if the rival is in the middle between src and dest
        long middle = Position.findMiddle(src, dest);
        LogicalPlayer rival = this.getRival();
        return ((middle & rival.getQueenBoard()) != 0) || ((middle & rival.getPieceBoard()) != 0);
    }

    public GeneralTree<Long> getPossibleChains(LogicalPlayer player, long srcChain, long realSrc)
    {
        // src - actually the destination after eating
        GeneralTree<Long> possibleChains = new GeneralTree<>(srcChain);
        if (checkIfQueen(player, realSrc))
        {
            this.buildChainQueenPiece(possibleChains, player, srcChain);
        }
        else {
            this.buildChainRegularPiece(possibleChains, player, srcChain);
        }
        return possibleChains;
    }

    public GeneralTree<Long> chain(LogicalPlayer player, long srcChain, long realSrc)
    {
        // checks if there is a chain. return the chain / null
        GeneralTree<Long> chain = this.getPossibleChains(player, srcChain, realSrc);
        chain = (chain.isLeaf()) ? null : chain;  // if the chain contains only the src - return null
        return chain;
    }

    public boolean validEatingMove(LogicalPlayer player, long src, long dest)
    {
        // returns true if the eating move is valid
        boolean queen = ((player.getQueenBoard() & src) != 0);  // is it a queen?

        // gets rival's player object
        LogicalPlayer rival = this.getRival();
        long rivalPieces = rival.getPieceBoard();
        long rivalQueens = rival.getQueenBoard();

        // checks if destination is empty
        boolean emptyDestination = this.emptyPosition(dest);


        long maybeRival = Position.findMiddle(src, dest);

        // destination check masks
        long maskDark1 = dest << (BitboardEssentials.VALID_EATING_STEP1);  // moves destination one way towards the src  (up)
        long maskDark2 = dest << (BitboardEssentials.VALID_EATING_STEP2);  // moves destination one way towards the src  (up)
        long maskLight1 = dest >> (BitboardEssentials.VALID_EATING_STEP1);  // moves destination one way towards the src  (down)
        long maskLight2 = dest >> (BitboardEssentials.VALID_EATING_STEP2);  // moves destination one way towards the src  (down)

        emptyDestination = emptyDestination && (((maskLight1 & src) != 0) || ((maskLight2 & src) != 0) || ((maskDark1 & src) != 0) || ((maskDark2 & src) != 0));

        // rival's existence check
        long rivalMaskDark1 = dest << (BitboardEssentials.VALID_STEP1);  // moves destination one way towards the src  (up)
        long rivalMaskDark2 = dest << (BitboardEssentials.VALID_STEP2);  // moves destination one way towards the src  (up)
        long rivalMaskLight1 = dest >> (BitboardEssentials.VALID_STEP1);  // moves destination one way towards the src  (down)
        long rivalMaskLight2 = dest >> (BitboardEssentials.VALID_STEP2);  // moves destination one way towards the src  (down)

        boolean trueMove = emptyDestination;

        if (emptyDestination && !queen)  // if a regular piece + destination is empty
        {
            // sets trueMove true if:
            //      dark -> the manipulation over dest has gotten to dest (forwards) + there is a light piece in the way
            //      light -> the manipulation over dest has gotten to dest (backwards) + there is a dark piece in the way
            if (player.isDark())
            {
                emptyDestination = ((maskDark1 & src) != 0) || ((maskDark2 & src) != 0);
                // checks if there is a piece (regular / queen) in the way
                trueMove = this.checkRivalInTheMiddle(src, dest);
            }
            else
            {
                emptyDestination = ((maskLight1 & src) != 0) || ((maskLight2 & src) != 0);
                // checks if there is a piece (regular / queen) in the way
                trueMove = ((rivalMaskLight1 & rivalPieces) != 0) || ((rivalMaskLight2 & rivalPieces) != 0) || ((rivalMaskLight1 & rivalQueens) != 0) || ((rivalMaskLight2 & rivalQueens) != 0);
            }
        }

        if (queen)  // if a queen piece
        {
            // if any of the ones surrounding the queen is the chosen one


            // sets trueMove true if:
            //      dark -> the manipulation over dest has gotten to dest (forwards) + there is a light piece in the way
            //      light -> the manipulation over dest has gotten to dest (backwards) + there is a dark piece in the way
            if (emptyDestination)
            {
                trueMove = this.checkRivalInTheMiddle(src, dest);
            }
        }
        return trueMove;
    }

    public LogicalPlayer getRival()
    {
        // returns rival player to current turn
        LogicalPlayer rival = (this.currentTurn == this.player1) ? this.player2 : this.player1;
        return rival;
    }

    public void removePiece(LogicalPlayer player, long position)
    {
        // removes piece from piece board
        long opPosition = ~ position;
        long maskPiece = player.getPieceBoard() & opPosition;
        long maskQueen = player.getQueenBoard() & opPosition;
        player.setPieceBoard(maskPiece);
        player.setQueenBoard(maskQueen);
    }

    public void placePiece(LogicalPlayer player, long position, boolean queen)
    {
        // places the piece at the right board
        if (queen)
        {
            player.setQueenBoard(player.getQueenBoard() | position);  // adds the queen in the queen board
        }
        else
        {
            player.setPieceBoard(player.getPieceBoard() | position);  // adds the piece in the piece board
        }
    }

    public boolean emptyPosition(long position)
    {
        // returns true if no other piece is in this position
        boolean piece1, piece2, queen1, queen2;
        piece1 = ((position & this.player1.getPieceBoard()) == 0);
        piece2 = ((position & this.player2.getPieceBoard()) == 0);
        queen1 = ((position & this.player1.getQueenBoard()) == 0);
        queen2 = ((position & this.player2.getQueenBoard()) == 0);
        return piece1 && piece2 && queen1 && queen2;
    }

    public void updateBoards(LogicalPlayer player, long src, long dest)
    {
        // moves and updates boards according to move
        boolean queen = ((player.getQueenBoard() & src) != 0);  // is it a queen?

        // moves bits (changes board according to move
        if (queen)  // move piece in queen board
        {
            long check1 = player.getQueenBoard() | dest;  // this OR dest
            long oppositeSrc = ~ src;  // NOT src
            player.setQueenBoard(check1 & oppositeSrc);  // final AND NOT src
        }
        else {  // move piece in piece board
            long check1 = player.getPieceBoard() | dest;  // this OR dest
            long oppositeSrc = ~src;  // NOT src
            player.setPieceBoard(check1 & oppositeSrc);  // final AND NOT src
        }
    }

    @Override
    public void switchTurns()
    {
        // switch turns
        this.currentTurn = (this.currentTurn.equals(this.player1)) ? this.player2 : this.player1;
    }


    @Override
    public long checkQueen(LogicalPlayer logicalPlayer, long dest) {
        // checks if current player made a queen
        long pos = 0;
        if (logicalPlayer.isDark())
        {
            pos = (dest & BitboardEssentials.DARK_QUEEN);
        }
        else
        {
            pos = (dest & BitboardEssentials.LIGHT_QUEEN);
        }
        return pos;
    }

    public void moveFromPieceToQueen(LogicalPlayer player, long position)
    {
        // moves position into queen board
        long removePieceMask = ~ position;
        player.setQueenBoard(player.getQueenBoard() | position);
        player.setPieceBoard(player.getPieceBoard() & removePieceMask);
    }

    public LogicalPlayer getCurrentTurn() {
        return this.currentTurn;
    }

    public LogicalPlayer getPlayerFromId(int id)
    {
        // returns the right player according to his id
        LogicalPlayer logicalPlayer = (id == 1) ? this.player1 : this.player2;
        return logicalPlayer;
    }

    public void printBoard()
    {
        long board1 = this.player1.getPieceBoard(), board2 = this.player2.getPieceBoard();
        long queen1 = this.player1.getQueenBoard(), queen2 = this.player2.getQueenBoard();
        // prints board nicely to track moves
        for (int i = 0 ; i < VisualBoard.getDimension() * VisualBoard.getDimension() ; i++)
        {
            // assumes that 2 pieces can't be at the same position
            if (board1 % 2 != 0)
            {
                System.out.print("1\t");
            }
            else if (board2 % 2 != 0)
            {
                System.out.print("2\t");
            }
            else if (queen1 % 2 != 0)
            {
                System.out.print("!\t");
            }
            else if (queen2 % 2 != 0)
            {
                System.out.print("@\t");
            }
            else
            {
                System.out.print((char)0xB7 + "\t");
            }
            // goes down one row
            if ((i + 1) % VisualBoard.getDimension() == 0)
            {
                System.out.println();
            }
            board1 = board1 >> 1;
            board2 = board2 >> 1;
            queen1 = queen1 >> 1;
            queen2 = queen2 >> 1;
        }
        System.out.println("\n\n");
    }

    public int checkWin()
    {
        // returns (+) - won, (-) - lost, (0) - no one won
        boolean check1 = (this.player1.getPieceBoard() == 0 && this.player1.getQueenBoard() == 0);
        boolean check2 = (this.player2.getPieceBoard() == 0 && this.player2.getQueenBoard() == 0);
        int won = 0;
        if (check1)
        {
            won = -1;
        }
        if (check2)
        {
            won = 1;
        }
        return won;
    }
}

