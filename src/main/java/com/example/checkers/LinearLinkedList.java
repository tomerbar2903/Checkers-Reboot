package com.example.checkers;


public class LinearLinkedList<T> {

    protected Node<T> head;
    protected int length;

    public LinearLinkedList() {
        this.head = null;
        this.length = 0;
    }

    public LinearLinkedList(T info) {
        this.head = new Node<T>(info);
        this.length = 0;
    }

    public boolean isEmpty()
    {
        return (this.head == null);
    }

    public T getInfo() {
        return this.head.getInfo();
    }

    public void setInfo(T info) {
        this.head.setInfo(info);
    }

    public Node<T> getNextNode() {
        return this.head.getNextNode();
    }

    public void setNextNode(Node<T> nextNode) {
        this.head.setNextNode(nextNode);
    }

    public Node<T> getHead() {
        return head;
    }

    public void push(T info)
    {
        // inserts a node in the beginning
        Node<T> newNode = new Node<>(info);
        newNode.setNextNode(this.head);
        this.head = newNode;
        this.length++;
    }

    public void popFirst()
    {
        // returns the info of the first node + deletes it
        Node temp = this.head;
        this.head = this.head.getNextNode();
        temp.setNextNode(null);
        this.length--;
    }

    public void print()
    {
        // prints linear linked list
        Node<T> iter = this.head;
        System.out.println();
        while (iter != null)
        {
            System.out.print(iter.getInfo() + " ---> ");
            iter = iter.getNextNode();
        }
        System.out.println("null");
    }

    public void removeObject(Object object)
    {
        // removes instance of object, if not in list - doesn't do anything
        Node iter = this.head;
        while (!iter.getNextNode().getInfo().equals(object) && iter != null)  // while the next node is not the object and iterator is not null
        {
            iter = iter.getNextNode();
        }
        if (iter != null)
        {
            iter.removeAfter();
            this.length--;
        }
    }

    public void removeAt(int i)
    {
        // removes node in index i
        if (i == 0)
        {
            this.popFirst();
            this.length--;
        }
        else {
            int j = 0;
            Node iter = this.head;
            while ((++j < i) && (iter != null)) {
                iter = iter.getNextNode();
            }
            if (iter != null) {
                iter.removeAfter();
                this.length--;
            }
        }
    }

    public T get(int i)
    {
        // returns information of the node in the i index
        int j = 0;
        Node iter = this.head;
        while (j++ < i && iter != null)
        {
            iter = iter.getNextNode();
        }
        T info = (iter == null) ? null : (T) iter.getInfo();
        return info;
    }

    public String toString()
    {
        // string of linear linked list
        String s = "\n";
        Node<T> iter = this.head;
        while (iter != null)
        {
            s += iter.getInfo() + " ---> ";
            iter = iter.getNextNode();
        }
        s += "null\n";
        return s;
    }

    public int size()
    {
        // returns length of list
        return this.length;
    }
}
