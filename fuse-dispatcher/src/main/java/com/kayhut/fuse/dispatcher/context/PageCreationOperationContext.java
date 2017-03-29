package com.kayhut.fuse.dispatcher.context;

import com.kayhut.fuse.dispatcher.resource.CursorResource;
import com.kayhut.fuse.dispatcher.resource.PageResource;

import java.io.IOException;

/**
 * Created by User on 06/03/2017.
 */
public class PageCreationOperationContext extends OperationContextBase<PageCreationOperationContext>{
    public interface Processor {
        PageCreationOperationContext process(PageCreationOperationContext context) throws IOException;
    }

    //region Constructors
    public PageCreationOperationContext(CursorResource cursorResource, String pageId, int pageSize) {
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
    public PageCreationOperationContext of(PageResource pageResource) {
        PageCreationOperationContext clone = cloneImpl();
        clone.pageResource = pageResource;
        return clone;
    }
    //endregion

    //region Properties
    public CursorResource getCursorResource() {
        return this.cursorResource;
    }

    public String getPageId() {
        return this.pageId;
    }

    public int getPageSize() {
        return this.pageSize;
    }

    public PageResource getPageResource() {
        return this.pageResource;
    }
    //endregion

    //region Fields
    private CursorResource cursorResource;
    private String pageId;
    private int pageSize;
    private PageResource pageResource;
    //endregion
}
