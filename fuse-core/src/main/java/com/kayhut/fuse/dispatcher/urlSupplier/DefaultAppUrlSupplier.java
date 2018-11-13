package com.kayhut.fuse.dispatcher.urlSupplier;

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

/**
 * Created by lior.perry on 08/03/2017.
 */
public class DefaultAppUrlSupplier implements AppUrlSupplier {
    //region Constructors
    public DefaultAppUrlSupplier(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    //endregion

    //region AppUrlSupplier Implementation
    @Override
    public String resourceUrl(String queryId) {
        return new ResourceUrlSupplier(this.baseUrl).queryId(queryId).get();
    }

    @Override
    public String resourceUrl(String queryId, String cursorId) {
        return new ResourceUrlSupplier(this.baseUrl).queryId(queryId).cursorId(cursorId).get();
    }

    @Override
    public String resourceUrl(String queryId, String cursorId, String pageId) {
        return new ResourceUrlSupplier(this.baseUrl).queryId(queryId).cursorId(cursorId).pageId(pageId).get();
    }

    @Override
    public String baseUrl() {
        return baseUrl;
    }

    @Override
    public String queryStoreUrl() {
        return new ResourceStoreUrlSupplier(this.baseUrl, ResourceStoreUrlSupplier.Store.Query).get();
    }

    @Override
    public String cursorStoreUrl(String queryId) {
        return new ResourceStoreUrlSupplier(this.baseUrl, ResourceStoreUrlSupplier.Store.Cursor).queryId(queryId).get();
    }

    @Override
    public String pageStoreUrl(String queryId, String cursorId) {
        return new ResourceStoreUrlSupplier(this.baseUrl, ResourceStoreUrlSupplier.Store.Page).queryId(queryId).cursorId(cursorId).get();
    }

    @Override
    public String catalogStoreUrl() {
        return this.baseUrl + "/catalog/ontology";
    }
    //endregion

    //region Fields
    protected String baseUrl;
    //endregion
}
