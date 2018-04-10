package com.kayhut.fuse.model.resourceInfo;

import com.fasterxml.jackson.annotation.JsonInclude;
import javaslang.collection.Stream;

import java.util.Collections;
import java.util.List;

/**
 * resources
 * http://domain/fuse/query/:id
 * http://domain/fuse/query/:id/plan
 * http://domain/fuse/query/:id/v1
 * http://domain/fuse/query/:id/cursor/:sequence
 * http://domain/fuse/query/:id/cursor/:sequence/result/:sequence
 */
public class QueryResourceInfo extends ResourceInfoBase{

    //region Constructors
    public QueryResourceInfo() {}

    public QueryResourceInfo(String resourceUrl,String resourceId, String cursorStoreUrl, CursorResourceInfo...cursorResourceInfos) {
        this(resourceUrl, resourceId, cursorStoreUrl, Stream.of(cursorResourceInfos));
    }

    public QueryResourceInfo(String resourceUrl,String resourceId, String cursorStoreUrl, Iterable<CursorResourceInfo> cursorResourceInfos) {
        super(resourceUrl,resourceId);
        this.cursorStoreUrl = cursorStoreUrl;
        this.v1QueryUrl = resourceUrl +"/v1";
        this.asgUrl = resourceUrl +"/asg";
        this.explainPlanUrl = resourceUrl +"/plan";
        this.cursorResourceInfos = cursorResourceInfos == null ? Collections.emptyList() : Stream.ofAll(cursorResourceInfos).toJavaList();
    }

    //endregion

    //region Properties
    public String getCursorStoreUrl() {
        return cursorStoreUrl;
    }

    public String getExplainPlanUrl() {
        return explainPlanUrl;
    }

    public String getV1QueryUrl() {
        return v1QueryUrl;
    }

    public String getAsgUrl() { return asgUrl; }

    public FuseError getError() {
        return error;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<CursorResourceInfo> getCursorResourceInfos() {
        return cursorResourceInfos;
    }

    //endregion

    public QueryResourceInfo error(FuseError error) {
        QueryResourceInfo clone  = new QueryResourceInfo(
                this.getResourceUrl(),
                this.getResourceId(),
                this.getCursorStoreUrl(),
                this.cursorResourceInfos);

        clone.error = error;
        return clone ;
    }

    //region Fields
    private String cursorStoreUrl;
    private String explainPlanUrl;
    private String v1QueryUrl;
    private String asgUrl;

    private FuseError error;

    private List<CursorResourceInfo> cursorResourceInfos;
    //endregion
}
