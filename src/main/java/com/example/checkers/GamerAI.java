package com.example.checkers;

public abstract class GamerAI {

    public static BitMove generateMove(LogicalPlayer player, LogicalPlayer rival, long mustEat) {

        BitMove move = null;
        long totalBoard = player.getTotalBoard();
        long empty = ~ (player.getTotalBoard() | rival.getTotalBoard());

        BitMove mustEatMove = GamerAI.findMustEat(player, mustEat);
        if (mustEatMove != null) {
            move = mustEatMove;
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

    private static BitMove findMustEat(LogicalPlayer player, long destinations) {

        BitMove eatingMove = null;

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
                eatingMove = new BitMove(pos, dest);
                break;
            }

            pieces -= pos;
        }
        return eatingMove;
    }

}
