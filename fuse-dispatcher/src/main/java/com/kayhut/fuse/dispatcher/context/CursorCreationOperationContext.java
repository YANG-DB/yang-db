package com.kayhut.fuse.dispatcher.context;

import com.kayhut.fuse.dispatcher.Cursor;
import com.kayhut.fuse.dispatcher.resource.QueryResource;
import com.kayhut.fuse.model.transport.CreateCursorRequest;

/**
 * Created by User on 07/03/2017.
 */
public class CursorCreationOperationContext extends OperationContextBase<CursorCreationOperationContext> {
    public interface Processor {
        CursorCreationOperationContext process(CursorCreationOperationContext context);
    }

    //region Constructors
    public CursorCreationOperationContext(QueryResource queryResource, String cursorId, CreateCursorRequest.CursorType cursorType) {
        this.queryResource = queryResource;
        this.cursorId = cursorId;
        this.cursorType = cursorType;
    }
    //endregion

    //region Public Methods
    public CursorCreationOperationContext of(Cursor cursor) {
        CursorCreationOperationContext clone = cloneImpl();
        clone.cursor = cursor;
        return clone;
    }
    //endregion

    //region Properties
    public QueryResource getQueryResource() {
        return this.queryResource;
    }

    public String getCursorId() {
        return this.cursorId;
    }

    public CreateCursorRequest.CursorType getCursorType() {
        return this.cursorType;
    }

    public Cursor getCursor() {
        return this.cursor;
    }
    //endregion

    //region OperationContextBase Implementaion
    @Override
    protected CursorCreationOperationContext cloneImpl() {
        CursorCreationOperationContext clone = new CursorCreationOperationContext(this.queryResource, this.cursorId, this.cursorType);
        clone.cursor = this.cursor;
        return clone;
    }
    //endregion

    //region Fields
    private QueryResource queryResource;
    private String cursorId;
    private CreateCursorRequest.CursorType cursorType;
    private Cursor cursor;
    //endregion
}