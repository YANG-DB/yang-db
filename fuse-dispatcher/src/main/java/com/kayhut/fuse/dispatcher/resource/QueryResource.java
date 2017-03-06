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
        this.cursors = new HashMap<>();
    }
    //endregion

    //region Public Methods
    public void addCursor(int key, CursorResource<ContentResponse> cursorResource) {
        this.cursors.put(key, cursorResource);
    }

    public Optional<CursorResource> getCursor(int key) {
        return Optional.ofNullable(this.cursors.get(key));
    }
    //endregion

    //region Properties
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
    private Map<Integer, CursorResource> cursors;
    //endregion
}
