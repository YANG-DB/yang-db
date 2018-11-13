package com.kayhut.fuse.dispatcher.resource;

/*-
 * #%L
 * fuse-core
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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

import java.util.Date;

/**
 * Created by lior.perry on 09/03/2017.
 */
public class PageResource<T> {

    //region Constructors
    public PageResource(String pageId, T data, int requestedSize, long executionTime) {
        this.pageId = pageId;
        this.timeCreated = new Date(System.currentTimeMillis());
        this.executionTime = executionTime;
        this.data = data;
        this.requestedSize = requestedSize;
        this.isAvailable = false;
    }
    //endregion

    //region Public Methods
    public PageResource<T> withActualSize(int actualSize) {
        PageResource<T> clone = this.cloneImpl();
        clone.actualSize = actualSize;
        return clone;
    }

    public PageResource<T> available() {
        PageResource<T> clone = this.cloneImpl();
        clone.isAvailable = true;
        return clone;
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

    //region Private Methods
    private PageResource<T> cloneImpl() {
        PageResource<T> clone = new PageResource<T>(this.pageId, this.data, this.requestedSize,this.executionTime);
        clone.timeCreated = this.timeCreated;
        clone.actualSize = this.actualSize;
        clone.isAvailable = this.isAvailable;
        return clone;
    }
    //endregion

    //region Fields
    private String pageId;
    private Date timeCreated;
    private T data;
    private long executionTime;
    private int requestedSize;
    private int actualSize;
    private boolean isAvailable;
    //endregion
}
