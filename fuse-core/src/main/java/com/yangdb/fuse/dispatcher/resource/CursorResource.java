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



import com.yangdb.fuse.dispatcher.cursor.Cursor;
import com.yangdb.fuse.dispatcher.profile.CursorRuntimeProvision;
import com.yangdb.fuse.dispatcher.profile.QueryProfileInfo;
import com.yangdb.fuse.model.transport.cursor.CreateCursorRequest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by lior.perry on 06/03/2017.
 */
public class CursorResource implements CursorRuntimeProvision {
    //region Constructors
    public CursorResource(String cursorId, Cursor cursor, QueryProfileInfo profileInfo, CreateCursorRequest cursorRequest) {
        this.cursorId = cursorId;
        this.profileInfo = profileInfo;
        this.pageResources = new HashMap<>();
        this.pageStatus = new HashMap<>();
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

    public Iterable<PageResource> getPageResources() {
        return this.pageResources.values();
    }

    public Map<String, PageState> getPageStatus() {
        return pageStatus;
    }

    public Optional<PageResource> getPageResource(String pageId) {
        return Optional.ofNullable(this.pageResources.get(pageId));
    }

    public void addPageResource(String pageId, PageResource pageResource) {
        this.pageResources.put(pageId, pageResource);
        this.pageStatus.put(pageId, pageResource.getState());
    }

    public void deletePageResource(String pageId) {
        this.pageResources.remove(pageId);
        this.pageStatus.put(pageId,PageState.DELETED);
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


    @Override
    public int getActiveScrolls() {
        return getCursor().getActiveScrolls();
    }

    @Override
    public boolean clearScrolls() {
        return getCursor().clearScrolls();
    }
    //endregion

    //region Fields
    private String cursorId;
    private QueryProfileInfo profileInfo;
    private CreateCursorRequest cursorRequest;
    private Cursor cursor;
    private Date timeCreated;

    private Map<String, PageResource> pageResources;
    private Map<String, PageState> pageStatus;
    //
    private AtomicInteger pageSequence = new AtomicInteger();

    //endregion
}
