package com.kayhut.test.generation;

public class IdGenerator {
    private int currentId = 1;

    public int nextId(){
        int x = this.currentId;
        this.currentId++;
        return x;
    }
}
