package com.kayhut.fuse.dispatcher.context;

import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.process.Cursor;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.QueryMetadata;

import java.util.*;

/**
 * Created by lior on 22/02/2017.
 */
public final class QueryExecutionContext {
    public enum Phase {
        asg,
        epb,
        cursor
    }

    //region Fields
    private List<Phase> phaseList;
    private QueryMetadata queryMetadata;
    private Query query;
    private AsgQuery asgQuery;
    private Plan executionPlan;
    private Cursor cursor;
    //endregion

    //region constructors
    public QueryExecutionContext(QueryMetadata queryMetadata, Query query) {
        this.phaseList = new ArrayList<>();
        this.queryMetadata = queryMetadata;
        this.query = query;
    }
    //endregion

    //region Public Methods
    @Override
    protected QueryExecutionContext clone() throws CloneNotSupportedException {
        return (QueryExecutionContext)super.clone();
    }

    //endregion

    //region properties
    public QueryMetadata getQueryMetadata() {
        return queryMetadata;
    }

    public Query getQuery() {
        return query;
    }

    public AsgQuery getAsgQuery() {
        return asgQuery;
    }

    public Cursor getCursor() {
        return cursor;
    }

    public Plan getExecutionPlan() {
        return executionPlan;
    }

    public QueryExecutionContext of(AsgQuery asgQuery) {
        return this.cloneImpl().asg(asgQuery);
    }

    public QueryExecutionContext of(Plan executionPlan) {
        return this.cloneImpl().executionPlan(executionPlan);
    }

    public QueryExecutionContext of(Cursor cursor) {
        return this.cloneImpl().cursor(cursor);
    }
    //endregion

    //region Private Methods
    protected QueryExecutionContext cloneImpl() {
        QueryExecutionContext clone = new QueryExecutionContext(this.queryMetadata, this.query);
        clone.asgQuery = this.asgQuery;
        clone.executionPlan = this.executionPlan;
        clone.phaseList = new ArrayList<>(this.phaseList);
        return clone;
    }

    private QueryExecutionContext asg(AsgQuery asgQuery) {
        this.asgQuery = asgQuery;
        this.phaseList.add(Phase.asg);
        return this;
    }

    private QueryExecutionContext executionPlan(Plan executionPlan) {
        this.executionPlan = executionPlan;
        this.phaseList.add(Phase.epb);
        return this;
    }

    private QueryExecutionContext cursor(Cursor cursor) {
        this.cursor = cursor;
        this.phaseList.add(Phase.cursor);
        return this;
    }

    public boolean phase(Phase phase) {
        return phaseList.contains(phase);
    }
    //endregion
}
