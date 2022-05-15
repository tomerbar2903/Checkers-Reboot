package com.example.checkers;

public class Test {

    public static void main(String[] args) {
        Model m = new Model();
        m.getCurrentTurn().setPieceBoard(34361835520L);
        m.getCurrentTurn().setQueenBoard(0);
        m.getCurrentTurn().setAdjacentToRival(34361835520L);
        m.getRival().setPieceBoard(396973176749293568L);
        m.getRival().setQueenBoard(0L);
        m.getRival().setAdjacentToRival(21991306297344L);
        LinearLinkedList<BitMove> moves = m.generateAIMove(m.getCurrentTurn());
        System.out.println(moves);
        m.printBoard();
    }

}
