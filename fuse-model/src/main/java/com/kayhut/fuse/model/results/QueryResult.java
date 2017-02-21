package com.kayhut.fuse.model.results;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Created by benishue on 21-Feb-17.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class QueryResult {

    public Pattern getPattern ()
    {
        return pattern;
    }

    public void setPattern (Pattern pattern)
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
    private Pattern pattern;
    private List<Assignment> assignments;
    //endregion
}
