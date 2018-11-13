package com.kayhut.fuse.services.controllers;

/*-
 * #%L
 * fuse-service
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

import com.fasterxml.jackson.databind.JsonNode;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreatePageRequest;

/**
 * Created by lior.perry on 19/02/2017.
 */
public interface PageController {
    ContentResponse<PageResourceInfo> create(String queryId, String cursorId, CreatePageRequest createPageRequest);
    ContentResponse<PageResourceInfo> createAndFetch(String queryId, String cursorId, CreatePageRequest createPageRequest);
    ContentResponse<StoreResourceInfo> getInfo(String queryId, String cursorId);
    ContentResponse<PageResourceInfo> getInfo(String queryId, String cursorId, String pageId);
    ContentResponse<Object> getData(String queryId, String cursorId, String pageId);
    ContentResponse<Boolean> delete(String queryId, String cursorId, String pageId);
}
