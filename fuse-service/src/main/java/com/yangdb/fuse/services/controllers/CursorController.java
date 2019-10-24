package com.yangdb.fuse.services.controllers;

/*-
 * #%L
 * fuse-service
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

import com.yangdb.fuse.model.resourceInfo.CursorResourceInfo;
import com.yangdb.fuse.model.resourceInfo.StoreResourceInfo;
import com.yangdb.fuse.model.transport.ContentResponse;
import com.yangdb.fuse.model.transport.cursor.CreateCursorRequest;

import java.util.Optional;

/**
 * Created by lior.perry on 22/02/2017.
 */

public interface CursorController<C,D> extends Controller<C,D>{
    /**
     *
     * @param queryId
     * @param createCursorRequest
     * @return
     */
    ContentResponse<CursorResourceInfo> create(String queryId, CreateCursorRequest createCursorRequest);

    /**
     *
     * @param queryId
     * @return
     */
    ContentResponse<StoreResourceInfo> getInfo(String queryId);

    /**
     *
     * @param queryId
     * @param cursorId
     * @return
     */
    ContentResponse<CursorResourceInfo> getInfo(String queryId, String cursorId);

    /**
     *
     * @param queryId
     * @param cursorId
     * @return
     */
    ContentResponse<Boolean> delete(String queryId, String cursorId);


}
