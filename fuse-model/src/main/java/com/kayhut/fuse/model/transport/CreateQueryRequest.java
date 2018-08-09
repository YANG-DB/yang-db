package com.kayhut.fuse.model.transport;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kayhut.fuse.model.execution.plan.descriptors.QueryDescriptor;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.transport.cursor.CreateCursorRequest;

/**
 * Created by lior on 19/02/2017.
 * <p>
 * Mutable structure due to json reflective builder needs...
 */
public class CreateQueryRequest {
    public enum Type {
        _stored,
        _volatile;
    }

    //region Constructors
    public CreateQueryRequest() {
        this.planTraceOptions = new PlanTraceOptions();
        this.planTraceOptions.setLevel(PlanTraceOptions.Level.none);
    }

    public CreateQueryRequest(String id, String name, Query query) {
        this();
        this.id = id;
        this.name = name;
        this.query = query;
    }

    public CreateQueryRequest(String id, String name, Query query, PlanTraceOptions planTraceOptions) {
        this(id, name, query);
        this.planTraceOptions = planTraceOptions;
    }

    public CreateQueryRequest(String id, String name, Query query, PlanTraceOptions planTraceOptions, CreateCursorRequest createCursorRequest) {
        this(id, name, query, planTraceOptions);
        this.createCursorRequest = createCursorRequest;
    }
    //endregion

    //region Properties
    public CreateQueryRequest type(Type type) {
        this.type = type;
        return this;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public String getName() {
        return name;
    }

    public Query getQuery() {
        return query;
    }

    public PlanTraceOptions getPlanTraceOptions() {
        return planTraceOptions;
    }

    public void setPlanTraceOptions(PlanTraceOptions planTraceOptions) {
        this.planTraceOptions = planTraceOptions;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public CreateCursorRequest getCreateCursorRequest() {
        return createCursorRequest;
    }

    public void setCreateCursorRequest(CreateCursorRequest createCursorRequest) {
        this.createCursorRequest = createCursorRequest;
    }
    //endregion


    @Override
    public String toString() {
        return "CreateQueryRequest{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", query=" + QueryDescriptor.toString(query) + "\n"+
                ", createCursorRequest=" + createCursorRequest!=null ? createCursorRequest.toString() : "None" +
                '}';
    }

    //region Fields
    private String id;
    //default type is volatile
    private Type type = Type._volatile;
    private String name;
    private Query query;
    private PlanTraceOptions planTraceOptions;

    private CreateCursorRequest createCursorRequest;
    //endregion
}
