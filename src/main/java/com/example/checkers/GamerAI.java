package com.example.checkers;

public abstract class GamerAI {

    public static BitMove generateMove(LogicalPlayer player, LogicalPlayer rival) {

        BitMove move = null;
        long totalBoard = player.getTotalBoard();
        long empty = ~ (player.getTotalBoard() | rival.getTotalBoard());

        int index;
        long pos;
        long adj;

        while (totalBoard > 0) {
            index = (int) BitboardEssentials.log2(totalBoard);
            pos = 1L << index;
            adj = BitboardEssentials.getCorners(pos, BitboardEssentials.ADJACENT_DIAMETER);
            long e = adj & empty;
            if (e != 0) {
                move = new BitMove();
                move.setSource(pos);
                int destIndex = (int) BitboardEssentials.log2(e);
                long dest = 1L << destIndex;
                move.setDestination(dest);
                break;
            }
            totalBoard -= index;
        }
        return move;
    }

}
