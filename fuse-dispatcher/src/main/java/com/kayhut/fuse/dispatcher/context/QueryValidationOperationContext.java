package com.kayhut.fuse.dispatcher.context;

import com.kayhut.fuse.dispatcher.utils.ValidationContext;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.QueryMetadata;

/**
 * Created by liorp on 6/11/2017.
 */
public class QueryValidationOperationContext extends OperationContextBase<QueryValidationOperationContext> {
    public interface Processor {
        ValidationContext process(QueryValidationOperationContext context) ;
    }

    //region constructors
    public QueryValidationOperationContext(QueryMetadata queryMetadata, Query query) {
        this.queryMetadata = queryMetadata;
        this.query = query;
    }
    //endregion

    //region Public Methods
    @Override
    protected QueryValidationOperationContext clone() throws CloneNotSupportedException {
        return (QueryValidationOperationContext) super.clone();
    }

    //region Private Methods
    @Override
    protected QueryValidationOperationContext cloneImpl() {
        QueryValidationOperationContext clone = new QueryValidationOperationContext(this.queryMetadata, this.query);
        clone.asgQuery = this.asgQuery;
        return clone;
    }

    public QueryValidationOperationContext of(AsgQuery asgQuery) {
        return this.cloneImpl().asg(asgQuery);
    }

    private QueryValidationOperationContext asg(AsgQuery asgQuery) {
        this.asgQuery = asgQuery;
        return this;
    }

    public QueryMetadata getQueryMetadata() {
        return queryMetadata;
    }

    public Query getQuery() {
        return query;
    }

    public AsgQuery getAsgQuery() {
        return asgQuery;
    }

    private QueryMetadata queryMetadata;
    private Query query;
    private AsgQuery asgQuery;
}
