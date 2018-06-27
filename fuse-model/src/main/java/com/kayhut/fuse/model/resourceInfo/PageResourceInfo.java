package com.kayhut.fuse.model.resourceInfo;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by User on 08/03/2017.
 */
public class PageResourceInfo extends ResourceInfoBase{
    //region Constructor
    public PageResourceInfo() {}

    public PageResourceInfo(
            String resourceUrl,
            String resourceId,
            int requestedPageSize,
            int actualPageSize,
            long executionTime,
            boolean isAvailable) {
        this(resourceUrl, resourceId, requestedPageSize, actualPageSize, executionTime, isAvailable, null);
    }

    public PageResourceInfo(
            String resourceUrl,
            String resourceId,
            int requestedPageSize,
            int actualPageSize,
            long executionTime,
            boolean isAvailable,
            Object data) {
        super(resourceUrl,resourceId);
        this.executionTime = executionTime;
        this.dataUrl = this.getResourceUrl() + "/data";
        this.elasticQueryUrl = resourceUrl +"/elastic";
        this.requestedPageSize = requestedPageSize;
        this.actualPageSize = actualPageSize;
        this.isAvailable = isAvailable;
        this.data = data;
    }
    //region Properties
    public String getDataUrl() {
        return this.dataUrl;
    }

    public int getRequestedPageSize() {
        return this.requestedPageSize;
    }

    public int getActualPageSize() {
        return this.actualPageSize;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public boolean isAvailable() {
        return this.isAvailable;
    }

    public void setDataUrl(String dataUrl) {
        this.dataUrl = dataUrl;
    }

    public void setRequestedPageSize(int requestedPageSize) {
        this.requestedPageSize = requestedPageSize;
    }

    public void setActualPageSize(int actualPageSize) {
        this.actualPageSize = actualPageSize;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public String getElasticQueryUrl() { return elasticQueryUrl; }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    //endregion

    //region Fields
    private long executionTime;
    private String dataUrl;
    private int requestedPageSize;
    private int actualPageSize;
    private boolean isAvailable;
    private Object data;
    private String elasticQueryUrl;
    //endregion
}
