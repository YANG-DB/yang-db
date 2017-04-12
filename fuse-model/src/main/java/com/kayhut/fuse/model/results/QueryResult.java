package com.kayhut.fuse.model.results;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.kayhut.fuse.model.query.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by benishue on 21-Feb-17.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class QueryResult {
    QueryResult() {}

    public Query getPattern ()
    {
        return pattern;
    }

    public void setPattern (Query pattern)
    {
        this.pattern = pattern;
    }

    public List<Assignment> getAssignments ()
    {
        return assignments;
    }

    public void setAssignments (List<Assignment> assignments)
    {
        this.assignments = assignments;
    }

    @Override
    public String toString()
    {
        return "QueryResult [pattern = "+pattern+", assignments = "+assignments+"]";
    }

    //region Fields
    private Query pattern;
    private List<Assignment> assignments;
    //endregion

    public static final class QueryResultBuilder {
        private Query pattern;
        private List<Assignment> assignments;

        private QueryResultBuilder() {
            assignments = new ArrayList<>();
        }

        public static QueryResultBuilder aQueryResult() {
            return new QueryResultBuilder();
        }

        public QueryResultBuilder withPattern(Query pattern) {
            this.pattern = pattern;
            return this;
        }

        public QueryResultBuilder withAssignment(Assignment assignments) {
            this.assignments.add(assignments);
            return this;
        }

        public QueryResultBuilder withAssignments(List<Assignment> assignments) {
            this.assignments = assignments;
            return this;
        }

        public QueryResult build() {
            QueryResult queryResult = new QueryResult();
            queryResult.setPattern(pattern);
            queryResult.setAssignments(assignments);
            return queryResult;
        }
    }


}
