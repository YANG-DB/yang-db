package com.kayhut.fuse.dispatcher.resource;

import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.costs.SingleCost;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.QueryMetadata;
import javaslang.Tuple2;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by User on 06/03/2017.
 */
public class QueryResource {
    //region Constructors
    public QueryResource(Query query, QueryMetadata queryMetadata, Tuple2<Plan,SingleCost> plan) {
        this.query = query;
        this.queryMetadata = queryMetadata;
        this.cursorResources = new HashMap<>();
        this.executionPlan = plan;
    }
    //endregion

    //region Public Methods
    public void addCursorResource(String cursorId, CursorResource cursorResource) {
        this.cursorResources.put(cursorId, cursorResource);
    }

    public Iterable<CursorResource> getCursorResources() {
        return this.cursorResources.values();
    }

    public Optional<CursorResource> getCursorResource(String cursorId) {
        return Optional.ofNullable(this.cursorResources.get(cursorId));
    }

    public void deleteCursorResource(String cursorId) {
        this.cursorResources.remove(cursorId);
    }

    public String getNextCursorId() {
        return String.valueOf(this.cursorSequence++);
    }
    //endregion

    //region Properties
    public Query getQuery() {
        return this.query;
    }

    public QueryMetadata getQueryMetadata() {
        return queryMetadata;
    }

    public Tuple2<Plan,SingleCost> getExecutionPlan() {
        return this.executionPlan;
    }
    //endregion

    //region Fields
    private Query query;
    private QueryMetadata queryMetadata;
    private Tuple2<Plan,SingleCost> executionPlan;
    private Map<String, CursorResource> cursorResources;

    private int cursorSequence;
    //endregion
}
