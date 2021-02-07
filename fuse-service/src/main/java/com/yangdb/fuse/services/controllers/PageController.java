package com.yangdb.fuse.services.controllers;

/*-
 * #%L
 * fuse-service
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

import com.yangdb.fuse.model.resourceInfo.PageResourceInfo;
import com.yangdb.fuse.model.resourceInfo.StoreResourceInfo;
import com.yangdb.fuse.model.transport.ContentResponse;
import com.yangdb.fuse.model.transport.CreatePageRequest;
import com.yangdb.fuse.model.transport.cursor.LogicalGraphCursorRequest;

/**
 * Created by lior.perry on 19/02/2017.
 */
public interface PageController<C,D> extends Controller<C,D>{
    /**
     * get the page resource information
     * @param queryId
     * @param cursorId
     * @param createPageRequest
     * @return
     */
    ContentResponse<PageResourceInfo> create(String queryId, String cursorId, CreatePageRequest createPageRequest);
    /**
     * get the page resource information
     * @param queryId
     * @param cursorId
     * @param createPageRequest
     * @return
     */
    ContentResponse<PageResourceInfo> createAndFetch(String queryId, String cursorId, CreatePageRequest createPageRequest);
    /**
     * get the page resource information
     * @param queryId
     * @param cursorId
     * @return
     */
    ContentResponse<StoreResourceInfo> getInfo(String queryId, String cursorId);
    /**
     * get the page resource information
     * @param queryId
     * @param cursorId
     * @param pageId
     * @return
     */
    ContentResponse<PageResourceInfo> getInfo(String queryId, String cursorId, String pageId);
    /**
     * delete the page resource including the cached data
     * @param queryId
     * @param cursorId
     * @param pageId
     * @return
     */
    ContentResponse<Boolean> delete(String queryId, String cursorId, String pageId);
    /**
     * get the data from the cached page resource
     * @param queryId
     * @param cursorId
     * @param pageId
     * @return
     */
    ContentResponse<Object> getData(String queryId, String cursorId, String pageId);

    /**
     * format the data according to the specific graph format
     * @param queryId
     * @param cursorId
     * @param pageId
     * @param format
     * @return
     */
    ContentResponse<Object> format(String queryId, String cursorId, String pageId, LogicalGraphCursorRequest.GraphFormat format);
}
