package com.example.checkers;

public class Test {

    public static void main(String[] args) {
        ArrayList<Integer> arrayList = new ArrayList();
        Integer i = 7;
        Integer i1 = 6;
        Integer i2 = 67;
        Integer i3 = 45;
        Integer i4 = 2;
        Integer i5 = 12;
        System.out.println(arrayList);
        arrayList.add(i);
        System.out.println(arrayList.size());
        arrayList.add(i1);
        arrayList.add(i2);
        arrayList.add(i3);
        arrayList.add(i4);
        arrayList.add(i5);
        System.out.println(arrayList.size());
        arrayList.remove((Integer) 45);
        System.out.println(arrayList.size());
        System.out.println(arrayList);
    }

}
