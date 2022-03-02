package com.example.checkers;

public class CircularLinearLinkedList<T> {

    protected Node<T> head;

    public CircularLinearLinkedList()
    {
        this.head = null;
    }

    public CircularLinearLinkedList(T info)
    {
        this.head = new Node<>(info);
        this.head.setNextNode(this.head);
    }

    public boolean isEmpty()
    {
        return (this.head == null);
    }

    public void insertFirst(T info)
    {
        // inserts when:
        /*
            before adding: N == 1
            after adding: N == 2
         */
        Node<T> newNode = new Node<>(info);
        newNode.setNextNode(this.head);
        this.head.setNextNode(newNode);
        this.head = newNode;
    }

    public void insertAfter(Node node, T info)
    {
        // inserts when:
        /*
            before adding: N > 1
         */
        Node<T> newNode = new Node<>(info);
        newNode.setNextNode(node.getNextNode());
        node.setNextNode(newNode);
    }

    public void insertEnd(T info)
    {
        // inserts when:
        /*
            before adding: N > 1
            inserts after manager, changes manager
         */
        Node<T> newNode = new Node<>(info);
        newNode.setNextNode(this.head.getNextNode());
        this.head.setNextNode(newNode);
        this.head = newNode;
    }

    public T removeAfter()
    {
        // removes when
        /*
            before removing: N > 1
            after removing: N > 1
         */
        Node temp = this.head.getNextNode();
        this.head.setNextNode(temp.getNextNode());
        temp.setNextNode(null);
        return (T)temp.getInfo();
    }

}

