package com.kayhut.fuse.dispatcher.context;

import com.kayhut.fuse.dispatcher.resource.CursorResource;
import com.kayhut.fuse.model.results.QueryResult;

/**
 * Created by User on 06/03/2017.
 */
public class CursorExecutionContext {

    public CursorExecutionContext(CursorResource cursorResource, int resultId, long fetchSize) {
        this.cursorResource = cursorResource;
        this.resultId = resultId;
        this.fetchSize = fetchSize;
    }

    //region method
    public CursorExecutionContext of(QueryResult queryResult) {
        CursorExecutionContext context = new CursorExecutionContext(this.cursorResource, this.resultId, this.fetchSize);
        context.result = queryResult;
        return context;
    }
    //endregion

    //region Properties
    public CursorResource getCursorResource() {
        return this.cursorResource;
    }

    public int getResultId() {
        return resultId;
    }

    public long getFetchSize() {
        return fetchSize;
    }

    public QueryResult getResult() {
        return this.result;
    }
    //endregion

    //region Fields
    private CursorResource cursorResource;
    private int resultId;
    private long fetchSize;
    private QueryResult result;
    //endregion
}
