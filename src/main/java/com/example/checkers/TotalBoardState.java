package com.example.checkers;

public class TotalBoardState {

    private BoardState dark;
    private BoardState light;

    public TotalBoardState(BoardState dark, BoardState light) {
        this.dark = new BoardState(dark);
        this.light = new BoardState(light);
    }

    public TotalBoardState(boolean ai) {
        // true - max, false - min
        this.dark = new BoardState(!ai);
        this.light = new BoardState(ai);
    }

    public BoardState getDarkState() {
        return dark;
    }

    public void setDarkState(BoardState player1) {
        this.dark = new BoardState(player1);
    }

    public BoardState getLightState() {
        return light;
    }

    public void setLightState(BoardState player2) {
        this.light = new BoardState(player2);
    }

    public String toString() {
        String output = "\n";
        long total2 = this.light.getPieceBoard() | this.light.getQueenBoard();
        long total1 = this.dark.getPieceBoard() | this.dark.getQueenBoard();
        for (int i = 1; i <= 64 ; i++) {
            if (total1 % 2 != 0) {
                output += "\t1";
            }
            else if (total2 % 2 != 0) {
                output += "\t2";
            }
            else {
                output += "\t-";
            }
            if (i % 8 == 0) {
                output += "\n";
            }
            total1 >>= 1;
            total2 >>= 1;
        }
        return output + "\n";
    }
}
