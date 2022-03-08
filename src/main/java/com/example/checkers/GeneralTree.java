package com.example.checkers;

import com.example.checkers.Position;
import java.util.ArrayList;

public class GeneralTree<E> {

    // sons array structure
    /*      1       2

            4       3
     */

    private E info;
    private GeneralTree<E>[] sons;

    private static final short POSSIBLE_MOVES = 4;

    public GeneralTree()
    {
        this.info = null;
        for (int i = 0 ; i < POSSIBLE_MOVES ; i++)
        {
            this.sons[i] = null;
        }
    }

    public GeneralTree(E info)
    {
        this.info = info;
        this.sons = new GeneralTree[POSSIBLE_MOVES];
        for (int i = 0 ; i < POSSIBLE_MOVES ; i++)
        {
            this.sons[i] = null;
        }
    }

    public GeneralTree(E info, GeneralTree[] sons)
    {
        this.info = info;
        this.sons = new GeneralTree[POSSIBLE_MOVES];
        for (int i = 0 ; i < POSSIBLE_MOVES ; i++)
        {
            this.sons[i] = sons[i];
        }
    }

    public E getInfo() {
        return info;
    }

    public void setInfo(E info) {
        this.info = info;
    }

    public GeneralTree getUpperLeft()
    {
        return this.sons[0];
    }

    public GeneralTree getUpperRight()
    {
        return this.sons[1];
    }

    public GeneralTree getLowerRight()
    {
        return this.sons[2];
    }

    public GeneralTree getLowerLeft()
    {
        return this.sons[3];
    }

    public void setUpperLeft(GeneralTree son)
    {
        this.sons[0] = son;
    }

    public void setUpperRight(GeneralTree son)
    {
        this.sons[1] = son;
    }

    public void setLowerRight(GeneralTree son)
    {
        this.sons[2] = son;
    }

    public void setLowerLeft(GeneralTree son)
    {
        this.sons[3] = son;
    }

    public void put(GeneralTree info, int index) {
        // puts info inside the array at index
        if (index < POSSIBLE_MOVES)
            this.sons[index] = info;
    }

    public GeneralTree get(int index) {
        // returns the son at index
        GeneralTree son = null;
        if (index < 4) son = this.sons[index];
        return son;
    }

    public boolean isLeaf()
    {
        return (this.sons[0] == null && this.sons[1] == null && this.sons[2] == null && this.sons[3] == null);
    }

    public GeneralTree<E> findNode(GeneralTree<E> tree, E info)
    {
        if (tree == null)
        {
            return null;
        }
        if (tree.getInfo().equals(info))
        {
            return tree;
        }
        GeneralTree<E> upperLeftSearch = findNode(tree.getUpperLeft(), info);
        GeneralTree<E> upperRightSearch = findNode(tree.getUpperRight(), info);
        GeneralTree<E> lowerRightSearch = findNode(tree.getLowerRight(), info);
        GeneralTree<E> lowerLeftSearch = findNode(tree.getLowerLeft(), info);
        GeneralTree<E> retVal = null;
        if (upperLeftSearch != null)
        {
            retVal = upperLeftSearch;
        }
        if (upperRightSearch != null)
        {
            retVal = upperRightSearch;
        }
        if (lowerRightSearch != null)
        {
            retVal = lowerRightSearch;
        }
        if (lowerLeftSearch != null)
        {
            retVal = lowerLeftSearch;
        }
        return retVal;
    }

    public GeneralTree<E> findSon(E info)
    {
        // returns the son that has info in it (considering sons can't have the same info)
        GeneralTree<E> son = null;
        for (int i = 0 ; i < POSSIBLE_MOVES ; i++)
        {
            if (this.sons[i] != null) {
                if (this.sons[i].getInfo().equals(info)) {
                    son = this.sons[i];
                }
            }
        }
        return son;
    }

    public ArrayList<E> generateSonsList()
    {
        // generates an array list of all sons of the tree (max = 4) O(1)
        ArrayList<E> arrayList = new ArrayList<>();
        if (this.getUpperLeft() != null) {
            arrayList.add((E) this.getUpperLeft().info);
        }
        if (this.getUpperRight() != null) {
            arrayList.add((E) this.getUpperRight().info);
        }
        if (this.getLowerRight() != null) {
            arrayList.add((E) this.getLowerRight().info);
        }
        if (this.getLowerLeft() != null) {
            arrayList.add((E) this.getLowerLeft().info);
        }
        return arrayList;
    }

    public ArrayList<Position> generateSonsListAsPosition()
    {
        // generates an array list of all sons of the tree (max = 4) O(1)
        ArrayList<Position> array = new ArrayList<>();
        if (this.getUpperLeft() != null) {
            array.add(Position.logicalNumberToPosition((Long) this.getUpperLeft().info));
        }
        if (this.getUpperRight() != null) {
            array.add(Position.logicalNumberToPosition((Long) this.getUpperRight().info));
        }
        if (this.getLowerRight() != null) {
            array.add(Position.logicalNumberToPosition((Long) this.getLowerRight().info));
        }
        if (this.getLowerLeft() != null) {
            array.add(Position.logicalNumberToPosition((Long) this.getLowerLeft().info));
        }
        return array;
    }

    public boolean hasOneSon()
    {
        // returns true if tree has 1 son (not including sub-trees)
        char counter = 0;
        for (int i = 0 ; i < POSSIBLE_MOVES ; i++)
        {
            counter = (this.sons[i] == null) ? counter : (char) (counter + 1);
        }
        return (counter == 1);
    }

    public GeneralTree getFirstSon()
    {
        // returns first son available
        GeneralTree<E> node = null;
        node = (this.sons[0] != null) ? this.sons[0] : node;
        node = (this.sons[1] != null) ? this.sons[1] : node;
        node = (this.sons[2] != null) ? this.sons[2] : node;
        node = (this.sons[3] != null) ? this.sons[3] : node;
        return node;
    }

    public static void print(GeneralTree tree)
    {
        if (tree == null)
        {
            System.out.println();
            return;
        }
        System.out.println("-- " + tree.info + "'s sons:");
        if (tree.getUpperLeft() != null)
            System.out.println("\t\t>>> 1: " + tree.getUpperLeft().getInfo());
        if (tree.getUpperRight() != null)
            System.out.println("\t\t>>> 2: " + tree.getUpperRight().getInfo());
        if (tree.getLowerRight() != null)
            System.out.println("\t\t>>> 3: " + tree.getLowerRight().getInfo());
        if (tree.getLowerLeft() != null)
            System.out.println("\t\t>>> 4: " + tree.getLowerLeft().getInfo());
        print(tree.getUpperLeft());
        print(tree.getUpperRight());
        print(tree.getLowerRight());
        print(tree.getLowerLeft());
    }

    public static void printAsPositions(GeneralTree tree)
    {
        if (tree == null)
        {
            System.out.println();
            return;
        }
        System.out.println("-- " + Position.logicalNumberToPosition((Long) tree.info) + "'s sons:");
        if (tree.getUpperLeft() != null)
            System.out.println("\t\t>>> Upper Left: " + Position.logicalNumberToPosition((Long) tree.getUpperLeft().getInfo()));
        if (tree.getUpperRight() != null)
            System.out.println("\t\t>>> Upper Right: " + Position.logicalNumberToPosition((Long) tree.getUpperRight().getInfo()));
        if (tree.getLowerRight() != null)
            System.out.println("\t\t>>> Lower Right: " + Position.logicalNumberToPosition((Long) tree.getLowerRight().getInfo()));
        if (tree.getLowerLeft() != null)
            System.out.println("\t\t>>> Lower Left: " + Position.logicalNumberToPosition((Long) tree.getLowerLeft().getInfo()));
        printAsPositions(tree.getUpperLeft());
        printAsPositions(tree.getUpperRight());
        printAsPositions(tree.getLowerRight());
        printAsPositions(tree.getLowerLeft());
    }
}

