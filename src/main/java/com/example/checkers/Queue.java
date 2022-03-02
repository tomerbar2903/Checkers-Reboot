package com.example.checkers;

public class Queue<T> extends CircularLinearLinkedList<T>{

    private Node<T> beforeHead;

    public Queue()
    {
        super();
        this.beforeHead = null;
    }

    public Queue(T info)
    {
        super(info);
        this.beforeHead = null;
    }

    public void insert(T info)
    {
        if (this.isEmpty())
        {
            this.head = new Node<>(info);
            this.head.setNextNode(this.head);
        }
        else
        {
            this.beforeHead = this.head;
            this.insertEnd(info);
        }

    }

    public T remove()
    {
        T info;
        if (this.head.getNextNode() == this.beforeHead)
        {
            // sets head to point at itself
            this.head.setNextNode(this.beforeHead.getNextNode());
            this.beforeHead.setNextNode(null);
            info = this.beforeHead.getInfo();
            this.beforeHead = null;
        }
        else if (this.head.getNextNode() == this.head)
        {
            info = this.head.getInfo();
            this.head = null;
        }
        else
        {
            info = this.head.getNextNode().getInfo();
            this.removeAfter();
        }
        return info;
    }

    public void print()
    {
        Node<T> iter = (this.head != null) ? this.head.getNextNode() : null;
        System.out.println();
        System.out.print("{ ");
        while(iter != this.head && iter != null)
        {
            System.out.print(iter.getInfo() + ", ");
            iter = iter.getNextNode();
        }
        if (iter != null)
        {
            System.out.print(iter.getInfo());
        }
        System.out.println(" }");
    }

}

