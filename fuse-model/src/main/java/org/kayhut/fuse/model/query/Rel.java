package org.kayhut.fuse.model.query;

/**
 * Created by user on 16-Feb-17.
 */
public class Rel extends EBase {

    public int getrType() {
        return rType;
    }

    public void setrType(int rType) {
        this.rType = rType;
    }

    public char getDir() {
        return dir;
    }

    public void setDir(char dir) {
        this.dir = dir;
    }

    public char getWrapper() {
        return wrapper;
    }

    public void setWrapper(char wrapper) {
        this.wrapper = wrapper;
    }

    public int getNext() {
        return next;
    }

    public void setNext(int next) {
        this.next = next;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }

    //region Fields
    private int rType;
    private char dir;
    private char wrapper;
    private int next;
    private int b;
    //endregion

}
