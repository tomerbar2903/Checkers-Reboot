package com.example.checkers;

public class QueueStack<T> extends DoubleLinkedList<T> {

    private Node<T> last;

    public QueueStack() {
        super();
        this.last = null;
    }

    public QueueStack(T info) {
        super(info);
        this.last = this.head;
    }

    @Override
    public void push(T info) {
        super.push(info);
        if (this.head == null) {
            this.last = null;
        }
    }

    @Override
    public T pop() {
        T info = super.pop();
        if (this.head == null) {
            this.last = null;
        }
        return info;
    }

    @Override
    public void insert(T info) {
        Node<T> temp = new Node<>(info);
        if (this.last == null) {
            this.last = this.head;
        }
        this.last.setNextNode(temp);
        temp.setPreviousNode(this.last);
        this.last = temp;
        this.size++;
    }

    @Override
    public T remove() {
        Node<T> temp = this.last;
        this.last = this.last.getPreviousNode();
        temp.setPreviousNode(null);
        this.last.setNextNode(null);
        this.size--;
        if (this.head == null) {
            this.last = null;
        }
        return temp.getInfo();
    }

    @Override
    public void print() {
        System.out.print("[" + this.head.getInfo());
        Node<T> iter = this.head.getNextNode();
        while (iter != null) {
            System.out.print(", " + iter.getInfo());
            iter = iter.getNextNode();
        }
        System.out.println("]");
    }
}
