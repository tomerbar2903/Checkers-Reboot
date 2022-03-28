package com.example.checkers;

public class Test {

    public static void main(String[] args) {
        DoubleLinkedList<Integer> list = new DoubleLinkedList<>();
        Integer i = 7;
        Integer i1 = 6;
        Integer i2 = 67;
        Integer i3 = 45;
        Integer i4 = 2;
        Integer i5 = 12;
        list.push(i);
        list.insert(i2);
        list.push(i1);
        System.out.println(list.remove());
        list.push(i3);
        list.insert(i4);
        System.out.println(list.pop());
        list.push(i5);
        list.print();
        System.out.println(list.getSize());

        QueueStack<Integer> queueStack = new QueueStack<>();
        queueStack.push(i);
        queueStack.insert(i2);
        queueStack.push(i1);
        System.out.println(queueStack.remove());
        queueStack.push(i3);
        queueStack.insert(i4);
        System.out.println(queueStack.pop());
        queueStack.push(i5);
        queueStack.print();
        System.out.println(queueStack.getSize());
    }

}
