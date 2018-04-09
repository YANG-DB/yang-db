package com.kayhut.fuse.model.results;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.kayhut.fuse.model.transport.cursor.CreateCsvCursorRequest;
import com.kayhut.fuse.model.transport.cursor.CreateGraphCursorRequest;
import com.kayhut.fuse.model.transport.cursor.CreateGraphHierarchyCursorRequest;
import com.kayhut.fuse.model.transport.cursor.CreatePathsCursorRequest;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "resultType")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "assignments", value = AssignmentsQueryResult.class),
        @JsonSubTypes.Type(name = "csv", value = CsvQueryResult.class)
})
public abstract class QueryResultBase {
    @JsonIgnore
    public abstract int getSize();
}
