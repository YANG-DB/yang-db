package com.yangdb.fuse.model.results;

/*-
 * #%L
 * fuse-model
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

/*-
 *
 * AssignmentsQueryResult.java - fuse-model - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.yangdb.fuse.model.logical.Edge;
import com.yangdb.fuse.model.logical.Vertex;
import com.yangdb.fuse.model.query.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by benishue on 21-Feb-17.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssignmentsQueryResult<E extends Vertex,R extends Edge> extends QueryResultBase {
    //region Constructors
    public AssignmentsQueryResult() {
        this.assignments = Collections.emptyList();
    }

    public AssignmentsQueryResult(List<Assignment<E,R>> assignments) {
        this.assignments = assignments;
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

    public List<Assignment<E,R>> getAssignments ()
    {
        return assignments;
    }

    public void setAssignments (List<Assignment<E,R>> assignments)
    {
        this.assignments = assignments;
    }
    //endregion

    public String getCursorType() {
        return cursorType;
    }

    public void setCursorType(String cursorType) {
        this.cursorType = cursorType;
    }

    //region Override Methods
    @Override
    public String toString()
    {
        return "AssignmentsQueryResult [pattern = "+pattern+", assignments = "+assignments+"]";
    }
    //endregion

    //region Fields
    private Query pattern;
    private String cursorType;

    private List<Assignment<E,R>> assignments;

    @Override
    public int getSize() {
        return this.getAssignments().size();
    }
    //endregion

    public static final class Builder<E extends Vertex,R extends Edge> {
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

        public Builder withCursorType(String type) {
            this.cursorType = type;
            return this;
        }

        public Builder withAssignment(Assignment<E,R> assignments) {
            this.assignments.add(assignments);
            return this;
        }

        public Builder withAssignments(List<Assignment<E,R>> assignments) {
            this.assignments = assignments;
            return this;
        }

        public AssignmentsQueryResult<E,R> build() {
            AssignmentsQueryResult<E,R> assignmentsQueryResult = new AssignmentsQueryResult<>();
            assignmentsQueryResult.setPattern(pattern);
            assignmentsQueryResult.setAssignments(assignments);
            assignmentsQueryResult.setCursorType(cursorType);
            return assignmentsQueryResult;
        }
        //endregion

        //region Fields
        private Query pattern;
        private String cursorType;
        private List<Assignment<E,R>> assignments;
        //endregion
    }


}
