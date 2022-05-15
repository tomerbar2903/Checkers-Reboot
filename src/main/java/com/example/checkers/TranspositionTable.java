package com.example.checkers;

import java.util.Random;

public class TranspositionTable {
    public static long matrix[][][];

    public static final int PLAYERS = 2;
    public static final int PIECES = 64;
    public static final int QUEENS = 64;

    public static long randomize() {
        return new Random().nextLong();
    }

    public static void initiate() {
        TranspositionTable.matrix = new long[PLAYERS][PIECES][QUEENS];
        for (int i = 0 ; i < PLAYERS ; i++) {
            for (int j = 0 ; j < PIECES ; j++) {
                for (int k = 0 ; k < QUEENS ; k++) {
                    TranspositionTable.matrix[i][j][k] = TranspositionTable.randomize();
                }
            }
        }
    }

    public static long calcZobristCode(TotalBoardState boardState) {
        long key = 0;
        long curPieces = boardState.getLightState().getPieceBoard();
        long curQueens = boardState.getLightState().getQueenBoard();
        int counter = 0;
        while (curPieces > 0) {
            if (curPieces % 2 == 1) {
                key ^= TranspositionTable.matrix[0][counter][0];
            }
            counter++;
            curPieces >>= 1;
        }
        counter = 0;
        while (curQueens > 0) {
            if (curQueens % 2 == 1) {
                key ^= TranspositionTable.matrix[0][0][counter];
            }
            counter++;
            curQueens >>= 1;
        }

        long rivPieces = boardState.getDarkState().getPieceBoard();
        long rivQueens = boardState.getDarkState().getQueenBoard();
        while (rivPieces > 0) {
            if (rivPieces % 2 == 1) {
                key ^= TranspositionTable.matrix[1][counter][0];
            }
            counter++;
            rivPieces >>= 1;
        }
        counter = 0;
        while (rivQueens > 0) {
            if (rivQueens % 2 == 1) {
                key ^= TranspositionTable.matrix[1][0][counter];
            }
            counter++;
            rivQueens >>= 1;
        }
        return key;
    }
}
