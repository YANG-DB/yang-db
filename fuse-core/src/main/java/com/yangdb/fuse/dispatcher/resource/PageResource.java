package com.yangdb.fuse.dispatcher.resource;

/*-
 * #%L
 * fuse-core
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


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.results.AssignmentsQueryResult;

import java.io.IOException;
import java.util.Date;

/**
 * Created by lior.perry on 09/03/2017.
 */
public class PageResource<T> {

    //SE-DE support for an efficient store on disk
    public static byte[] serialize(ObjectMapper mapper, PageResource resource) {
        try {
            ObjectNode node = mapper.createObjectNode();
            node.put("pageId",resource.pageId);
            node.put("actualSize",resource.actualSize);
            node.put("executionTime",resource.executionTime);
            node.put("timeCreated",resource.timeCreated.getTime());
            node.put("requestedSize",resource.requestedSize);
            //lazy fetch data - only on explicit request by client
            node.put("data", AssignmentsQueryResult.serialize(mapper,(AssignmentsQueryResult)resource.getData()));
            return node.toString().getBytes();
        } catch (Throwable e) {
            throw new FuseError.FuseErrorException("Error serializing resource",e);
        }
    }

    public static PageResource deserialize(ObjectMapper mapper,byte[] bytes) {
        //read - generate new cursor OR renew folder cursor state
        try {
            ObjectNode node = (ObjectNode) mapper.readTree(bytes);
            String pageId = node.get("pageId").asText();
            int requestedSize = node.get("requestedSize").asInt();
            long executionTime = node.get("executionTime").asLong();
            long timeCreated = node.get("timeCreated").asLong();
            AssignmentsQueryResult data = AssignmentsQueryResult.deserialize(mapper,node.get("data").binaryValue());
            return new PageResource(pageId,data,requestedSize,timeCreated,executionTime);
        } catch (IOException e) {
            throw new FuseError.FuseErrorException("Error deserializing resource",e);
        }
    }

    //region Constructors

    public PageResource() {}

    public PageResource(String pageId, T data, int requestedSize, long executionTime) {
        this(pageId,data,requestedSize,System.currentTimeMillis(),executionTime);
    }

    public PageResource(String pageId, T data, int requestedSize, long timeCreated, long executionTime) {
        this.pageId = pageId;
        this.timeCreated = new Date(timeCreated);
        this.executionTime = executionTime;
        this.data = data;
        this.requestedSize = requestedSize;
        this.isAvailable = false;
    }
    //endregion

    //region Public Methods
    public PageResource<T> withActualSize(int actualSize) {
        this.actualSize = actualSize;
        return this;
    }

    public PageResource<T> available() {
        this.isAvailable = true;
        return this;
    }
    //endregion

    //region properties
    public String getPageId() {
        return this.pageId;
    }

    public Date getTimeCreated() {
        return this.timeCreated;
    }

    public T getData() {
        return this.data;
    }

    public int getRequestedSize() {
        return this.requestedSize;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public int getActualSize() {
        return this.actualSize;
    }

    public boolean isAvailable() {
        return this.isAvailable;
    }
    //endregion

    //region Fields
    private String pageId;
    private Date timeCreated;
    private long executionTime;
    private int requestedSize;
    private int actualSize;
    private boolean isAvailable;
    //in mem state of the actual data
    private volatile T data;
    //endregion
}
