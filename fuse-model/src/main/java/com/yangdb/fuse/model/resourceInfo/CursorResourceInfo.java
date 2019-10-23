package com.yangdb.fuse.model.resourceInfo;

/*-
 * #%L
 * fuse-model
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

/*-
 *
 * CursorResourceInfo.java - fuse-model - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yangdb.fuse.model.transport.cursor.CreateCursorRequest;
import javaslang.collection.Stream;

import java.util.List;

/**
 * Created by lior.perry on 06/03/2017.
 */
public class CursorResourceInfo extends ResourceInfoBase {
    //region Constructors
    public CursorResourceInfo() {}

    public CursorResourceInfo(
            String resourceUrl,
            String resourceId,
            CreateCursorRequest cursorRequest,
            String pageStoreUrl,
            PageResourceInfo...pageResourceInfos) {
        this(resourceUrl, resourceId, cursorRequest, pageStoreUrl, Stream.of(pageResourceInfos));
    }

    public CursorResourceInfo(
            String resourceUrl,
            String resourceId,
            CreateCursorRequest cursorRequest,
            String pageStoreUrl,
            Iterable<PageResourceInfo> pageResourceInfos) {
        super(resourceUrl,resourceId);
        this.pageStoreUrl = pageStoreUrl;
        this.cursorRequest = cursorRequest;
        this.pageResourceInfos = Stream.ofAll(pageResourceInfos).toJavaList();
    }
    //endregion

    //region Properties
    public String getPageStoreUrl() {
        return this.pageStoreUrl;
    }

    public void setPageStoreUrl(String pageStoreUrl) {
        this.pageStoreUrl = pageStoreUrl;
    }

    public CreateCursorRequest getCursorRequest() {
        return cursorRequest;
    }

    public void setCursorRequest(CreateCursorRequest cursorRequest) {
        this.cursorRequest = cursorRequest;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<PageResourceInfo> getPageResourceInfos() {
        return pageResourceInfos;
    }

    public void setPageResourceInfos(List<PageResourceInfo> pageResourceInfos) {
        this.pageResourceInfos = pageResourceInfos;
    }

    public FuseError getError() {
        return error;
    }

    public CursorResourceInfo error(FuseError error) {
        CursorResourceInfo clone  = new CursorResourceInfo(
                this.getResourceUrl(),
                this.getResourceId(),
                this.cursorRequest,
                this.pageStoreUrl,
                this.pageResourceInfos);

        clone.error = error;
        return clone ;
    }


    //endregion

    //region Fields
    private FuseError error;
    private CreateCursorRequest cursorRequest;
    private String pageStoreUrl;
    private List<PageResourceInfo> pageResourceInfos;
    //endregion
}
