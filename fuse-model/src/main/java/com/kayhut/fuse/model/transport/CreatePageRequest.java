package com.kayhut.fuse.model.transport;

/*-
 * #%L
 * CreatePageRequest.java - fuse-model - kayhut - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
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

/**
 * Created by lior.perry on 22/02/2017.
 */
public class CreatePageRequest {
    //region Constructors
    public CreatePageRequest() {}

    public CreatePageRequest(int pageSize) {
        this(pageSize, false);
    }

    public CreatePageRequest(int pageSize, boolean fetch) {
        this.pageSize = pageSize;
        this.fetch = fetch;
    }
    //endregion

    //region Properties
    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public boolean isFetch() {
        return fetch;
    }

    public void setFetch(boolean fetch) {
        this.fetch = fetch;
    }
    //endregion

    @Override
    public String toString() {
        return "CreatePageRequest{" +
                "pageSize=" + pageSize +
                ", fetch=" + fetch +
                '}';
    }

    //region Fields
    private int pageSize = 1000;
    private boolean fetch;
    //endregion
}
