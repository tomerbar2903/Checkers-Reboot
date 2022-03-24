package com.example.checkers;

import java.lang.reflect.Array;

public class ArrayList<T>{

    private T[] array;
    private int injectionIndex;

    private static final int INITIAL_SIZE = 50;
    private static final int ADDITION_SIZE = 20;

    public ArrayList() {
        this.array = (T[]) new Object[INITIAL_SIZE];
        this.injectionIndex = 0;
    }

    private T[] realloc() {
        T[] dest = (T[]) new Object[this.array.length + ADDITION_SIZE];
        for (int i = 0 ; i < this.array.length ; i++) {
            dest[i] = this.array[i];
        }
        return dest;
    }

    public void add(T item) {
        if (this.injectionIndex == array.length) {
            this.array = realloc();
        }
        this.array[this.injectionIndex++] = item;
    }

    public T get(int index) {
        T o = null;
        if (index < this.injectionIndex) {
            o = this.array[index];
        }
        return o;
    }

    private static void clearHoles(Object[] arr, int holeIndex, int lastFull) {
        int following = holeIndex + 1;
        while (following <= lastFull) {
            arr[holeIndex] = arr[following];
            holeIndex ++;
            following ++;
        }
        arr[lastFull] = null;
    }

    private static int find(Object[] arr, Object o) {
        boolean found = false;
        int i;
        for (i = 0 ; i < arr.length ; i++) {
            if (arr[i].equals(o)) {
                found = true;
                break;
            }
        }
        int index = (found) ? i : -1;
        return index;
    }

    public void remove(int index) {
        if (this.injectionIndex > index) {
            this.array[index] = null;
            clearHoles(this.array, index, this.injectionIndex - 1);
            this.injectionIndex --;
        }
    }

    public void remove(Object o) {
        int offset = find(this.array, o);
        if (offset != -1) {
            this.remove(offset);
        }
    }

    public int size() {
        return this.injectionIndex;
    }

    @Override
    public String toString() {
        String output = "[";
        if (this.injectionIndex > 0) {
            output += this.array[0].toString();
            for (int i = 1 ; i < this.injectionIndex ; i++) {
                output += ", " + this.array[i].toString();
            }
        }
        output += "]";
        return output;
    }
}
