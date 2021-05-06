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



import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yangdb.fuse.dispatcher.cursor.Cursor;
import com.yangdb.fuse.dispatcher.profile.QueryProfileInfo;
import com.yangdb.fuse.model.profile.QueryProfileStepInfoData;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.transport.cursor.CreateCursorRequest;
import org.apache.tinkerpop.gremlin.process.traversal.util.Metrics;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by lior.perry on 06/03/2017.
 */
public class CursorResource {
    //SE-DE support for an efficient store on disk
    public static byte[] serialize(ObjectMapper mapper, CursorResource cursorResource) {
        try {
            ObjectNode node = mapper.createObjectNode();
            node.put("cursorId",cursorResource.cursorId);
            node.put("cursorRequest",mapper.writeValueAsString(cursorResource.cursorRequest));
            node.put("timeCreated",mapper.writeValueAsString(cursorResource.timeCreated));
            node.put("profileInfo",mapper.writeValueAsString(cursorResource.profileInfo.infoData()));
            //cursor is not persisted yet - since we are using lazy data fetch and still need to persist the cursor's state
            return node.toString().getBytes();
        } catch (JsonProcessingException e) {
            throw new FuseError.FuseErrorException("Error serializing resource",e);
        }
    }

    public static CursorResource deserialize(ObjectMapper mapper,byte[] bytes) {
        //read - generate new cursor OR renew folder cursor state
        try {
            ObjectNode node = (ObjectNode) mapper.readTree(bytes);
            String cursorId = node.get("cursorId").asText();
            CreateCursorRequest cursorRequest = mapper.readValue(node.get("cursorId").toString(),CreateCursorRequest.class);
            List<QueryProfileStepInfoData> queryProfileStepInfoDataSteps = mapper.readValue(node.get("profileInfo").textValue(),
                    new TypeReference<List<QueryProfileStepInfoData>>() {});
            return new CursorResource(cursorId, null, new QueryProfileInfo() {
                @Override
                public Metrics measurements() {
                    return null;
                }

                @Override
                public List<QueryProfileStepInfoData> infoData() {
                    return queryProfileStepInfoDataSteps;
                }
            }, cursorRequest);
        } catch (IOException e) {
            throw new FuseError.FuseErrorException("Error deserializing resource",e);
        }
    }

    //region Constructors

    public CursorResource() {}

    public CursorResource(String cursorId, Cursor cursor, QueryProfileInfo profileInfo, CreateCursorRequest cursorRequest) {
        this.cursorId = cursorId;
        this.profileInfo = profileInfo;
        this.pageResources = new HashMap<>();
        this.cursor = cursor;
        this.cursorRequest = cursorRequest;
        this.timeCreated = new Date(System.currentTimeMillis());
    }

        //endregion

    //region Public Methods
    public String getCursorId() {
        return this.cursorId;
    }

    public Date getTimeCreated() {
        return this.timeCreated;
    }

    @JsonAnyGetter
    public Map<String, PageResource> getPageResources() {
        return this.pageResources;
    }

    public Optional<PageResource> getPageResource(String pageId) {
        return Optional.ofNullable(this.pageResources.get(pageId));
    }

    public void addPageResource(String pageId, PageResource pageResource) {
        this.pageResources.put(pageId, pageResource);
    }

    public void deletePageResource(String pageId) {
        this.pageResources.remove(pageId);
    }

    public String getNextPageId() {
        return String.valueOf(this.pageSequence.incrementAndGet());
    }

    public String getCurrentPageId() {
        return String.valueOf(this.pageSequence.get());
    }

    public String getPriorPageId() {
        return String.valueOf(this.pageSequence.get() > 0 ? this.pageSequence.get()-1 : 0);
    }

    public Cursor getCursor() {
        return cursor;
    }

    public QueryProfileInfo getProfileInfo() {
        return profileInfo;
    }

    public CreateCursorRequest getCursorRequest() {
        return this.cursorRequest;
    }
    //endregion

    //region Fields
    private AtomicInteger pageSequence = new AtomicInteger();

    private String cursorId;
    private Date timeCreated;

    private CreateCursorRequest cursorRequest;
    private Map<String, PageResource> pageResources;

    //in mem state of the cursor & profile info
    private volatile Cursor cursor;
    private volatile QueryProfileInfo profileInfo;

    //endregion
}
