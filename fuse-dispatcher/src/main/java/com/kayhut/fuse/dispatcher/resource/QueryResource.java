package com.kayhut.fuse.dispatcher.resource;

import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.process.Cursor;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.QueryMetadata;
import com.kayhut.fuse.model.transport.ContentResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by User on 06/03/2017.
 */
public class QueryResource {
    //region Constructors
    public QueryResource(Query query, QueryMetadata queryMetadata) {
        this.query = query;
        this.queryMetadata = queryMetadata;
        this.cursorResources = new HashMap<>();
    }
    //endregion

    //region Public Methods
    public void addCursorResource(int cursorId, CursorResource<Object> cursorResource) {
        this.cursorResources.put(cursorId, cursorResource);
    }

    public Optional<CursorResource> getCursorResource(int cursorId) {
        return Optional.ofNullable(this.cursorResources.get(cursorId));
    }

    public void deleteCursorResource(int cursorId) {
        this.cursorResources.remove(cursorId);
    }

    public int getNextCursorSequence() {
        return this.cursorSequence++;
    }
    //endregion

    //region Properties
    public Query getQuery() {
        return this.query;
    }

    public QueryMetadata getQueryMetadata() {
        return queryMetadata;
    }

    public Plan getExecutionPlan() {
        return this.executionPlan;
    }
    //endregion

    //region Fields
    private Query query;
    private QueryMetadata queryMetadata;
    private Plan executionPlan;
    private Map<Integer, CursorResource> cursorResources;

    private int cursorSequence;
    //endregion
}
