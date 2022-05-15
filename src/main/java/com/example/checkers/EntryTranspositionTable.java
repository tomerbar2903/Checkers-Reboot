package com.example.checkers;

public class EntryTranspositionTable {

    public static int upper = 0;
    public static int lower = -1;
    public static int exact = 1;

    private int score;
    private int depth;
    private int flag;

    public EntryTranspositionTable(int score, int depth, int flag) {
        this.score = score;
        this.depth = depth;
        this.flag = flag;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }
}



