package com.example.checkers;

public class Node<T> {

    private T info;
    private Node<T> nextNode;

    public Node()
    {
        this.info = null;
        this.nextNode = null;
    }

    public Node(T info) {
        this.info = info;
    }

    public T getInfo() {
        return info;
    }

    public void setInfo(T info) {
        this.info = info;
    }

    public Node<T> getNextNode() {
        return nextNode;
    }

    public void setNextNode(Node<T> nextNode) {
        this.nextNode = nextNode;
    }

    public void removeAfter()
    {
        // removes node after list
        Node temp = this.getNextNode();
        this.setNextNode(this.getNextNode().getNextNode());
        temp.setNextNode(null);
    }

    public boolean isLast()
    {
        return (this.nextNode == null);
    }

/*    @Override
    public String toString() {
        return "Node{" +
                "info=" + info +
                ", nextNode=" + nextNode +
                '}';
    }*/

    public boolean equals(Node node)
    {
        // checks if infos are identical
        return this.info.equals(node.info);
    }
}
