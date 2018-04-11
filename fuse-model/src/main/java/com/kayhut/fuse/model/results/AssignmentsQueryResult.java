package com.kayhut.fuse.model.results;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.kayhut.fuse.model.query.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by benishue on 21-Feb-17.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AssignmentsQueryResult extends QueryResultBase {
    //region Constructors
    public AssignmentsQueryResult() {
        this.assignments = Collections.emptyList();
    }
    //endregion

    //region Properties
    public Query getPattern ()
    {
        return pattern;
    }

    public String getResultType(){
        return "assignments";
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
    //endregion

    //region Override Methods
    @Override
    public String toString()
    {
        return "AssignmentsQueryResult [pattern = "+pattern+", assignments = "+assignments+"]";
    }
    //endregion

    //region Fields
    private Query pattern;
    private List<Assignment> assignments;

    @Override
    public int getSize() {
        return this.getAssignments().size();
    }
    //endregion

    public static final class Builder {
        //region Constructors
        private Builder() {
            assignments = new ArrayList<>();
        }
        //endregion

        //region Static
        public static Builder instance() {
            return new Builder();
        }
        //endregion

        //region Public Methods
        public Builder withPattern(Query pattern) {
            this.pattern = pattern;
            return this;
        }

        public Builder withAssignment(Assignment assignments) {
            this.assignments.add(assignments);
            return this;
        }

        public Builder withAssignments(List<Assignment> assignments) {
            this.assignments = assignments;
            return this;
        }

        public AssignmentsQueryResult build() {
            AssignmentsQueryResult assignmentsQueryResult = new AssignmentsQueryResult();
            assignmentsQueryResult.setPattern(pattern);
            assignmentsQueryResult.setAssignments(assignments);
            return assignmentsQueryResult;
        }
        //endregion

        //region Fields
        private Query pattern;
        private List<Assignment> assignments;
        //endregion
    }


}
