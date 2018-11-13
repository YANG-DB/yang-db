package com.kayhut.fuse.dispatcher.driver;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;

import java.util.Optional;

/**
 * Created by lior.perry on 08/03/2017.
 */
public interface PageDriver {
    Optional<PageResourceInfo> create(String queryId, String cursorId, int pageSize);
    Optional<StoreResourceInfo> getInfo(String queryId, String cursorId);
    Optional<PageResourceInfo> getInfo(String queryId, String cursorId, String pageId);
    Optional<Boolean> delete(String queryId, String cursorId, String pageId);
    Optional<Object> getData(String queryId, String cursorId, String pageId);
}
