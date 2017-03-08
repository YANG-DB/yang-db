package com.kayhut.fuse.dispatcher.context;

import com.kayhut.fuse.dispatcher.resource.CursorResource;
import com.kayhut.fuse.model.results.QueryResult;

/**
 * Created by User on 06/03/2017.
 */
public class PageCreationOperationContext extends OperationContextBase<PageCreationOperationContext>{
    public interface Processor {
        PageCreationOperationContext process(PageCreationOperationContext context);
    }

    //region Constructors
    public PageCreationOperationContext(CursorResource cursorResource, int pageId, int pageSize) {
        this.cursorResource = cursorResource;
        this.pageId = pageId;
        this.pageSize = pageSize;
    }
    //endregion

    //region OperationContextBase Implementation
    @Override
    protected PageCreationOperationContext cloneImpl() {
        PageCreationOperationContext clone = new PageCreationOperationContext(this.cursorResource, this.pageId, this.pageSize);
        clone.pageResource = this.pageResource;
        return clone;
    }
    //endregion

    //region Public Methods
    public PageCreationOperationContext of(Object pageResource) {
        PageCreationOperationContext clone = cloneImpl();
        clone.pageResource = pageResource;
        return clone;
    }
    //endregion

    //region Properties
    public CursorResource getCursorResource() {
        return this.cursorResource;
    }

    public int getPageId() {
        return this.pageId;
    }

    public long getPageSize() {
        return this.pageSize;
    }

    public Object getPageResource() {
        return this.pageResource;
    }
    //endregion

    //region Fields
    private CursorResource cursorResource;
    private int pageId;
    private int pageSize;
    private Object pageResource;
    //endregion
}
