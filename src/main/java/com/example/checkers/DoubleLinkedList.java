package com.example.checkers;

public class DoubleLinkedList<T>{

    protected Node<T> head;
    protected int size;

    public DoubleLinkedList() {
        this.head = null;
        this.size = 0;
    }

    public DoubleLinkedList(T info) {
        this.head = new Node<>(info);
        this.size = 1;
    }

    public Node<T> getNextNode() {
        return this.head.getNextNode();
    }

    public void setNextNode(Node<T> node) {
        this.head.setNextNode(node);
    }

    public Node<T> getPreviousNode() {
        return this.head.getPreviousNode();
    }

    public void setPreviousNode(Node<T> node) {
        this.head.setPreviousNode(node);
    }

    public int getSize() {
        return size;
    }

    public void push(T info) {
        if (this.head == null) {
            this.head = new Node<>(info);
        }
        else {
            Node<T> temp = new Node<>(info);
            temp.setNextNode(this.head);
            this.head.setPreviousNode(temp);
            this.head = temp;
        }
        this.size++;
    }

    public T pop() {
        Node<T> temp = this.head;
        this.head = this.head.getNextNode();
        this.head.setPreviousNode(null);
        this.size--;
        return temp.getInfo();
    }

    public void insert(T info) {
        if (this.head == null) {
            this.head = new Node<>(info);
        }
        else {
            Node<T> temp = new Node<>(info);
            Node<T> end = this.head;
            while (end.getNextNode() != null) {
                end = end.getNextNode();
            }
            end.setNextNode(temp);
            temp.setPreviousNode(end);
        }
        this.size++;
    }

    public T remove() {
        Node<T> end = this.head;
        while (end.getNextNode() != null) {
            end = end.getNextNode();
        }
        T info = end.getInfo();
        end = end.getPreviousNode();
        end.setNextNode(null);
        this.size--;
        return info;
    }

    public void print() {
        System.out.print(this.head.getInfo());
        Node<T> iter = this.head.getNextNode();
        while (iter != null) {
            System.out.print(" <---> " + iter.getInfo());
            iter = iter.getNextNode();
        }
        System.out.println(" ---> ||");
    }
}
