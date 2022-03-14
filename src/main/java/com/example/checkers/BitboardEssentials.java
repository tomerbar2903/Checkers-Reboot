package com.example.checkers;

import javafx.geometry.Pos;

import java.util.ArrayList;

public abstract class BitboardEssentials {

    // WHITE TILES MASK
    public static final long WHITE_TILES = 0xaa55aa55aa55aa55L;

    // DARK QUEEN MASKS
    public static final long DARK_QUEEN = 0xaa;


    // LIGHT QUEEN MASKS
    public static final long LIGHT_QUEEN = 0x5500000000000000L;

    // GAME INITIATE MASKS
    public  static final long DARK_INIT = 0x55aa550000000000L;
    public  static final long LIGHT_INIT = 0xaa55aa;

    // SIMPLE VALID MOVES - STEP MASKS
    public static final int VALID_STEP1 = VisualBoard.getDimension() + 1;
    public static final int VALID_STEP2 = VisualBoard.getDimension() - 1;

    // SIMPLE VALID EATING MOVES MASKS
    public static final int VALID_EATING_STEP1 = 2 * VALID_STEP1;
    public static final int VALID_EATING_STEP2 = 2 * VALID_STEP2;

    public static final int ADJACENT_DIAMETER = 3;
    public static final int CHECK_EAT_DIAMETER = 5;

    public static final long SPECIAL_POSITION = 4611686018427387904l;

    // POSITION CALCULATORS
    public static long getMaxLeft(long position, int maxSideMove) {
        // returns the position where is maxSideMove left to position, or the max left if maxSideMove is too long
        long left;
        int boardSize = VisualBoard.getDimension();
        int counter = 0;
        long copy = position;
        EdgeType checkEdge = getEdgeType(copy, boardSize);

        while (checkEdge != EdgeType.LEFT_EDGE && checkEdge != EdgeType.BOTTOM_LEFT && checkEdge != EdgeType.TOP_LEFT && counter < maxSideMove / 2) {
            counter++;
            copy >>= 1;
            checkEdge = getEdgeType(copy, boardSize);
        }
        left = position >> counter;
        return left;
    }

    public static long getLeft(long position, int maxSide) {
        // returns the position of the left side with max side steps. if edge arrived - return 0
        long left;
        int boardSize = VisualBoard.getDimension();
        int counter = 0;
        long copy = position;
        EdgeType checkEdge = getEdgeType(copy, boardSize);

        while (checkEdge != EdgeType.LEFT_EDGE && checkEdge != EdgeType.BOTTOM_LEFT && checkEdge != EdgeType.TOP_LEFT && counter < maxSide) {
            counter++;
            copy >>= 1;
            checkEdge = getEdgeType(copy, boardSize);
        }
        left = (counter == maxSide) ? position >> counter : 0;  // if not fully topped to maxSide - return 0
        return left;
    }

    public static long getMaxRight(long position, int maxSideMove) {
        // returns the position where is maxSideMove right to position, or the max right if maxSideMove is too long
        long right;
        int boardSize = VisualBoard.getDimension();
        int counter = 0;
        long copy = position;

        EdgeType checkEdge = getEdgeType(copy, boardSize);

        while (checkEdge != EdgeType.RIGHT_EDGE && checkEdge != EdgeType.BOTTOM_RIGHT && checkEdge != EdgeType.TOP_RIGHT && counter < maxSideMove / 2) {
            counter++;
            copy <<= 1;
            checkEdge = getEdgeType(copy, boardSize);
        }
        right = position << counter;
        return right;
    }

    public static long getRight(long position, int maxSide) {
        // returns the position of the right side with max side steps. if edge arrived - return 0
        long right;
        int boardSize = VisualBoard.getDimension();
        int counter = 0;
        long copy = position;
        EdgeType checkEdge = getEdgeType(copy, boardSize);

        while (checkEdge != EdgeType.RIGHT_EDGE && checkEdge != EdgeType.BOTTOM_RIGHT && checkEdge != EdgeType.TOP_RIGHT && counter < maxSide) {
            counter++;
            copy <<= 1;
            checkEdge = getEdgeType(copy, boardSize);
        }
        right = (counter == maxSide) ? position << counter : 0;
        return right;
    }

    public static long getMaxTop(long position, int maxSideMove) {
        // returns the position where is maxSideMove up to position, or the max top if maxSideMove is too long
        long top;
        int boardSize = VisualBoard.getDimension();
        int counter = 0;
        long copy = position;

        EdgeType checkEdge = getEdgeType(copy, boardSize);

        while (checkEdge != EdgeType.TOP_EDGE && checkEdge != EdgeType.TOP_LEFT && checkEdge != EdgeType.TOP_RIGHT && counter < maxSideMove / 2) {
            counter++;
            copy >>= boardSize;
            checkEdge = getEdgeType(copy, boardSize);
        }
        top = position >> (counter * boardSize);
        return top;
    }

    public static long getTop(long position, int maxSide) {
        // returns the position of the top side with max side steps. if edge arrived - return 0
        long top;
        int boardSize = VisualBoard.getDimension();
        int counter = 0;
        long copy = position;

        EdgeType checkEdge = getEdgeType(copy, boardSize);

        while (checkEdge != EdgeType.TOP_EDGE && checkEdge != EdgeType.TOP_LEFT && checkEdge != EdgeType.TOP_RIGHT && counter < maxSide) {
            counter++;
            copy >>= boardSize;
            checkEdge = getEdgeType(copy, boardSize);
        }
        top = (counter == maxSide) ? position >> (counter * boardSize) : 0;
        return top;
    }

    public static long getMaxBottom(long position, int maxSideMove) {
        // returns the position where is maxSideMove left to position, or the max left if maxSideMove is too long
        long bottom;
        int boardSize = VisualBoard.getDimension();
        int counter = 0;
        long copy = position;

        EdgeType checkEdge = getEdgeType(copy, boardSize);

        while (checkEdge != EdgeType.BOTTOM_EDGE && checkEdge != EdgeType.BOTTOM_LEFT && checkEdge != EdgeType.BOTTOM_RIGHT && counter < maxSideMove / 2) {
            counter++;
            copy <<= boardSize;
            checkEdge = getEdgeType(copy, boardSize);
        }
        bottom = position << (counter * boardSize);
        return bottom;
    }

    public static long getBottom(long position, int maxSide) {
        // returns the position of the bottom side with max side steps. if edge arrived - return 0
        long bottom;
        int boardSize = VisualBoard.getDimension();
        int counter = 0;
        long copy = position;

        EdgeType checkEdge = getEdgeType(copy, boardSize);

        while (checkEdge != EdgeType.BOTTOM_EDGE && checkEdge != EdgeType.BOTTOM_LEFT && checkEdge != EdgeType.BOTTOM_RIGHT && counter < maxSide) {
            counter++;
            copy <<= boardSize;
            checkEdge = getEdgeType(copy, boardSize);
        }
        bottom = (counter == maxSide) ? position << (counter * boardSize) : 0;
        return bottom;
    }

    public static long getMaxBottomRight(long bottom, long right, int boardSize) {
        // returns the point with x(right) y(bottom).
        /*
        a chance that it will return (7, 7) - negative number. if so, shift 1 to the left
         */
        /*
            BOTTOM-RIGHT POSITION:
            index of bottom minus index of right. Divide by dimension (cast to int), add 1. Multiply by dimension
         */
        long bottomRight = right << ((int) (((log2(bottom) - log2(right))) / boardSize + 1) * boardSize);
        bottomRight = (bottomRight < 0) ? (SPECIAL_POSITION) : bottomRight;
        return bottomRight;
    }

    public static long getBottomRight(long bottom, long right, int boardSize) {
        // returns the point with x(right) y(bottom) (if both arent 0)
        long bottomRight = 0;
        if (bottom != 0 && right != 0) {
            bottomRight = getMaxBottomRight(bottom, right, boardSize);
        }
        return bottomRight;
    }

    public static long getMaxBottomLeft(long bottom, long left, int boardSize) {
        /*
            BOTTOM-LEFT POSITION:
            index of bottom minus index of left. Divide by dimension (cast to int). Multiply by dimension
         */
        // x(left) y(bottom)
        bottom = (bottom < 0) ? (Position.positionToLogicalNumber(new Position((short) 6, (short) 7))) : bottom;
        long bottomLeft = left << ((int) (((log2(bottom) - log2(left))) / boardSize) * boardSize);
        return bottomLeft;
    }

    public static long getBottomLeft(long bottom, long left, int boardSize) {
        // x(left) y(bottom) (if both arent 0)
        bottom = (bottom < 0) ? (SPECIAL_POSITION) : bottom;
        long bottomLeft = 0;
        if (bottom != 0 && left != 0) {
            bottomLeft = getMaxBottomLeft(bottom, left, boardSize);
        }
        return bottomLeft;
    }

    public static long getMaxTopRight(long top, long right, int boardSize) {
        /*
            TOP-RIGHT POSITION:
            index of right minus index of top. Divide by dimension (cast to int). Multiply by dimension
         */
        // x(right) y(top)
        long newRight = (right > 0) ? right : SPECIAL_POSITION;
        long topRight = newRight >> ((int) (((log2(newRight) - log2(top))) / boardSize) * boardSize);
        if (right < 0) {
            topRight <<= 1;
        }
        return topRight;
    }

    public static long getTopRight(long top, long right, int boardSize) {
        // x(right) y(top) (if both arent 0)
        long topRight = 0;
        if (top != 0 && right != 0) {
            topRight = getMaxTopRight(top, right, boardSize);
        }
        return topRight;
    }

    public static long getMaxTopLeft(long top, long left, int boardSize) {
         /*
            TOP-LEFT POSITION:
            index of left minus index of top. Divide by dimension (cast to int), add 1. Multiply by dimension
         */
        // x(left) y(top)
        long topLeft = left >> ((int) (((log2(left) - log2(top)) / boardSize + 1)) * boardSize);
        return topLeft;
    }

    public static long getTopLeft(long top, long left, int boardSize) {
        // x(left) y(top) (if both arent 0)
        long topLeft = 0;
        if (top != 0 && left != 0) {
            topLeft = getMaxTopLeft(top, left, boardSize);
        }
        return topLeft;
    }

    // Calculation Functions
    public static long getSquare(long positionCenter, int sideLength) {
        // returns a mask of a square around positionCenter with a side length of sideLength
        long top, bottom, left, right;
        int boardSize = VisualBoard.getDimension();

        /*
        TOP EDGE POSITION
         */
        {
            top = getMaxTop(positionCenter, sideLength);
        }

        /*
        BOTTOM EDGE POSITION
         */
        {
            bottom = getMaxBottom(positionCenter, sideLength);
        }

        /*
        LEFT EDGE POSITION
         */
        {
            left = getMaxLeft(positionCenter, sideLength);
        }

        /*
        RIGHT EDGE POSITION
         */
        {
            right = getMaxRight(positionCenter, sideLength);
        }

        long topLeft, topRight, bottomLeft, bottomRight;

        /*
        CALCULATE CORNERS
         */
        topLeft = getMaxTopLeft(top, left, boardSize);
        topRight = getMaxTopRight(top,right, boardSize);
        bottomLeft = getMaxBottomLeft(bottom, left, boardSize);
        bottomRight = getMaxBottomRight(bottom, right, boardSize);

        long totalMask;
        /*
        GENERATE TOTAL MASK
        Start at topLeft, shift 1 left until reached to topRight. shift topLeft and topRight to the left boardSize times, until
        topLeft reached to bottomRight

        There's a chance that bottomRight will be negative at (7, 7). for that, we make bottomRight to be at (6, 7). (7, 7) is not a black tile anyway
         */
        {
            totalMask = 0;
            long current = topLeft;
            while (current <= bottomRight) {
                for (totalMask |= current ; current <= topRight && current < bottomRight ; current <<= 1) {
                    totalMask |= current;
                }
                // if end is reached
                if (current == bottomRight){
                    totalMask |= bottomRight;
                    break;
                }
                current = (topLeft <<= boardSize);
                topRight <<= boardSize;
                // if topRight reaches bottom right - it might get negative as well
                topRight = (topRight > 0) ? topRight : (-1) * (topRight >> 1);
            }
        }

        return totalMask;
    }

    public static long getAdjacentMask(long pos, boolean dark, boolean isQueen, boolean reversed) {
        // returns a mask of the adjacent pieces to pos
        long finalMask = getCorners(pos, ADJACENT_DIAMETER);
        finalMask = (reversed) ? validateForPlayer(pos, finalMask, !dark, isQueen) : validateForPlayer(pos, finalMask, dark, isQueen);
        return finalMask;
    }

    public static long getCorners(long pos, int maxSide) {
        // returns a mask of all positions in the corners for pos.
        // if the corner is exceeding an edge, it's not going to get included in the mask
        long top = getTop(pos, maxSide / 2);
        long bottom = getBottom(pos, maxSide / 2);
        long left = getLeft(pos, maxSide / 2);
        long right = getRight(pos, maxSide / 2);

        long topLeft = getTopLeft(top, left, VisualBoard.getDimension());
        long topRight = getTopRight(top, right, VisualBoard.getDimension());
        long bottomLeft = getBottomLeft(bottom, left, VisualBoard.getDimension());
        long bottomRight = getBottomRight(bottom, right, VisualBoard.getDimension());

        return topLeft | topRight | bottomLeft | bottomRight;
    }

    public static long validateForPlayer(long pos, long mask, boolean dark, boolean isQueen) {
        // returns a mask that deletes all bits that don't make sense for current player
        /*
        MASK                      DARK                          LIGHT
        1 0 0 0 1                 1 0 0 0 1                     0 0 0 0 0
        0 0 0 0 0   VALIDATION    0 0 0 0 0     VALIDATION      0 0 0 0 0
        0 0 0 0 0   ---------->   0 0 0 0 0     ---------->     0 0 0 0 0
        0 0 0 0 0                 0 0 0 0 0                     0 0 0 0 0
        1 0 0 0 1                 0 0 0 0 0                     1 0 0 0 1
         */

        // basically, the function deletes all bigger / smaller bits off of mask (depending on dark)
        long finalMask = mask;
        if (!isQueen) {
            long deletionMask = -1;
            int indexPos = (int) log2(pos);  // the bit where pos is represented
            deletionMask = deletionMask << indexPos;
            deletionMask = (dark) ? ~deletionMask : deletionMask;
            finalMask = mask & deletionMask;
        }
        return finalMask;
    }

    public static long getPossibleEatingDestinations(long pos, boolean dark, boolean isQueen, boolean reversed) {
        // returns all possible landing positions after a single eating move

        // EXAMPLES --> not considering edges (function will consider)
        /*
        FOR LIGHT REGULAR PIECE:
        0 0 p 0 0
        0 0 0 0 0
        1 0 0 0 1
         */
        /*
        FOR DARK REGULAR PIECE:
        1 0 0 0 1
        0 0 0 0 0
        0 0 p 0 0
         */
        /*
        FOR A QUEEN PIECE:
        1 0 0 0 1
        0 0 0 0 0
        0 0 p 0 0
        0 0 0 0 0
        1 0 0 0 1
         */

        // generate queen's mask, which is a general mask for non-queen pieces (deletion of rear pieces happens next)
        long finalMask;
        finalMask = getBlankMask(pos, 5);  // max side of square is 5

        // generate appropriate mask for regular piece
        finalMask = (reversed) ? validateForPlayer(pos, finalMask, !dark, isQueen) : validateForPlayer(pos, finalMask, dark, isQueen);
        return finalMask;
    }

    public static double log2(long n) { return Math.log(n) / Math.log(2); }

    public static EdgeType getEdgeType(long position, int dimension) {
        // returns edge type if the position is on an Edge (NOT_EDGE - if not an edge)
        
        EdgeType edgeType = EdgeType.NOT_EDGE;

        // converts into index in matrix (bitboard)
        int bitboardPosition = (int) (Math.log(position) / Math.log(2));

        // side edge
        /*
            the position on the bit-board -> translated to index in matrix is on side edge if:
                --> the value of the index is a multiplication of the dimension value
                            OR
                --> the value of the index is a multiplication of the dimension value, minus 1
         */
        boolean left = (bitboardPosition % dimension == 0);
        boolean right = (bitboardPosition % dimension == dimension - 1);
        edgeType = left ? EdgeType.LEFT_EDGE : edgeType;  // update edge type
        edgeType = right ? EdgeType.RIGHT_EDGE : edgeType;  // update edge type

        // bottom up edge
        /*
            the position on the bit-board -> translated to index in matrix is on bottom/up edge if:
                --> BOTTOM EDGE (56 <= x < 64):  the value of the index is between the squared value of the dimension and the value of
                dimension squared, minus dimension
                --> UP EDGE (0 <= x < 8):  the value of the index is between 0 and dimension value
         */
        int maxCellBottom = dimension * dimension;
        int minCellBottom = maxCellBottom - dimension;
        // check bottom edge
        boolean bottom = ((bitboardPosition < maxCellBottom && bitboardPosition >= minCellBottom));

        // check upper edge
        boolean top = (bitboardPosition >= 0 && bitboardPosition < dimension);
        
        edgeType = (bottom) ? EdgeType.BOTTOM_EDGE : edgeType;  // update edge type

        edgeType = (top) ? EdgeType.TOP_EDGE : edgeType;

        // UPDATE CORNER TYPE IF NEEDED
        edgeType = (top && left) ? EdgeType.TOP_LEFT : edgeType;
        edgeType = (top && right) ? EdgeType.TOP_RIGHT : edgeType;
        edgeType = (bottom && left) ? EdgeType.BOTTOM_LEFT : edgeType;
        edgeType = (bottom && right) ? EdgeType.BOTTOM_RIGHT : edgeType;
        
        return edgeType;
    }
    
    public static long getBlankMask(long position, int maxSide) {
        // returns a mask surrounding position in this way:
        /* maxSide = 5
        1 0 0 0 1
        0 0 0 0 0
        0 0 P 0 0
        0 0 0 0 0
        1 0 0 0 1
         */
        int boardSize = VisualBoard.getDimension();
        
        long top = getMaxTop(position, maxSide);
        long bottom = getMaxBottom(position, maxSide);
        long left = getMaxLeft(position, maxSide);
        long right = getMaxRight(position, maxSide);
        
        long topLeft = getMaxTopLeft(top, left, boardSize);
        long topRight = getMaxTopRight(top, right, boardSize);
        long bottomLeft = getMaxBottomLeft(bottom, left, boardSize);
        long bottomRight = getMaxBottomRight(bottom, right, boardSize);
        
        return topLeft | topRight | bottomLeft | bottomRight;
    }

    public static void printBoard(long board) {
        for (int i = 1 ; i <= VisualBoard.getDimension() * VisualBoard.getDimension() ; i++) {
            if (board % 2 != 0) {
                System.out.print((char) 0x25CF + "\t");
            }
            else {
                System.out.print((char) 0x25CC + "\t");
            }
            if (i % VisualBoard.getDimension() == 0) {
                System.out.println();
            }
            board >>= 1;
        }
        System.out.println("\n\n");
    }
}
