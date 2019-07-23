package com.yangdb.fuse.model.resourceInfo;

/*-
 * #%L
 * PageResourceInfo.java - fuse-model - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by lior.perry on 08/03/2017.
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
