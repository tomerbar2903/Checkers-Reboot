package com.example.checkers;

public class History {

    private Stack<Object> objectHistory;

    public History()
    {
        this.objectHistory = new Stack<>();
    }

    public void push(Object lastChanged)
    {
        this.objectHistory.push(lastChanged);
    }

    public Object pop()
    {
        return this.objectHistory.pop();
    }

    public boolean isEmpty()
    {
        return this.objectHistory.isEmpty();
    }

    public void emptyHistory()
    {
        while(!this.isEmpty()) {this.pop();}
    }

}
