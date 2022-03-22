package com.example.checkers;

public class ArrayList<T>{

    private LinearLinkedList<T> list;

    public ArrayList() {
        this.list = new LinearLinkedList<>();
    }

    public int size() {
        return this.list.size();
    }

    public void add(T item) {
        this.list.push(item);
    }
}
