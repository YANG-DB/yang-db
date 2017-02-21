package com.kayhut.fuse.model.results;

import java.util.List;

/**
 * Created by benishue on 21-Feb-17.
 */
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
        return "ClassPojo [pattern = "+pattern+", assignments = "+assignments+"]";
    }

    //region Fields
    private Pattern pattern;
    private List<Assignment> assignments;
    //endregion
}
