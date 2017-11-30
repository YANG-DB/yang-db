package com.kayhut.fuse.dispatcher.context;

import com.kayhut.fuse.dispatcher.resource.PageResource;

/**
 * Created by User on 06/03/2017.
 */
public class PageCreationOperationContext extends OperationContextBase<PageCreationOperationContext>{
    public interface Processor {
        PageCreationOperationContext process(PageCreationOperationContext context) throws Exception;
    }

    //region Constructors
    public PageCreationOperationContext(String queryId, String cursorId, String pageId, int pageSize) {
        this.queryId = queryId;
        this.cursorId = cursorId;
        this.pageId = pageId;
        this.pageSize = pageSize;
    }
    //endregion

    //region OperationContextBase Implementation
    @Override
    protected PageCreationOperationContext cloneImpl() {
        PageCreationOperationContext clone = new PageCreationOperationContext(this.queryId, this.cursorId, this.pageId, this.pageSize);
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
    public String getQueryId() {
        return this.queryId;
    }

    public String getCursorId() {
        return this.cursorId;
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
    private String queryId;
    private String cursorId;
    private String pageId;
    private int pageSize;
    private PageResource pageResource;
    //endregion
}
