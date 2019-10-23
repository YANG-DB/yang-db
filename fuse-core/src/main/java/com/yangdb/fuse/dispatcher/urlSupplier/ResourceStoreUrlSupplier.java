package com.yangdb.fuse.dispatcher.urlSupplier;

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



/**
 * Created by lior.perry on 08/03/2017.
 */
public class ResourceStoreUrlSupplier extends ResourceUrlSupplier {
    public enum Store {
        Query,
        Cursor,
        Page,
    }

    //region Constructors
    public ResourceStoreUrlSupplier(String baseUrl, Store store) {
        super(baseUrl);
        this.store = store;
    }
    //endregion

    //region UrlSupplierBase Implementation
    @Override
    public String get() {
        switch (store) {
            case Query: return baseUrl + "/query";

            case Cursor:
                if (!this.queryId.isPresent()) {
                    return null;
                }
                return baseUrl + "/query/" + this.queryId.get() + "/cursor";

            case Page:
                if (!this.queryId.isPresent() || !this.cursorId.isPresent()) {
                    return null;
                }
                return baseUrl + "/query/" + this.queryId.get() + "/cursor/" + this.cursorId.get() + "/page";

            default: return null;
        }
    }
    //endregion

    //region Protected Methods
    protected ResourceUrlSupplier cloneImpl() {
        ResourceUrlSupplier clone = new ResourceStoreUrlSupplier(this.baseUrl, this.store);
        clone.queryId = this.queryId;
        clone.cursorId = this.cursorId;
        clone.pageId = this.pageId;
        return clone;
    }
    //endregion

    //region Fields
    protected Store store;
    //endregion
}
