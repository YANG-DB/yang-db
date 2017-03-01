package com.kayhut.fuse.model.query;


import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by user on 16-Feb-17.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Rel extends EBase {

    public int getrType() {
        return rType;
    }

    public void setrType(int rType) {
        this.rType = rType;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getWrapper() {
        return wrapper;
    }

    public void setWrapper(String wrapper) {
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
    private String dir;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private String wrapper;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int next;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int b;
    //endregion

}
