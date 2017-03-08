package com.kayhut.fuse.model.transport;

/**
 * Created by User on 07/03/2017.
 */
public class CreateCursorRequest {
    public enum CursorType {
        graph,
        paths
    }

    //region Properties
    public CursorType getCursorType() {
        return cursorType;
    }

    public void setCursorType(CursorType cursorType) {
        this.cursorType = cursorType;
    }

    //endregion

    //region Fields
    private CursorType cursorType;
    //endregion
}
