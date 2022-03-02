package com.example.checkers;

public class Stack<E> extends LinearLinkedList<E> {

    public Stack() {
        super();
    }

    public Stack(E info) {
        super(info);
    }

    public E pop() {
        E info = this.getInfo();
        super.popFirst();
        return info;
    }

    public void print() {
        if (this.isEmpty()) {
            System.out.println("END OF STACK");
            return;
        }
        E item = this.pop();
        System.out.print(item + ", ");
        this.print();
        this.push(item);
    }
}
