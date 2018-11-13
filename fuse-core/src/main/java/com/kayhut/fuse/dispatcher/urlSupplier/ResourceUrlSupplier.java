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

import java.util.Optional;

/**
 * Created by lior.perry on 08/03/2017.
 */
public class ResourceUrlSupplier extends UrlSupplierBase {
    //region Constructors
    public ResourceUrlSupplier(String baseUrl) {
        super(baseUrl);
        this.queryId = Optional.empty();
        this.cursorId = Optional.empty();
        this.pageId = Optional.empty();
    }
    //endregion

    //region Public Methods
    public ResourceUrlSupplier queryId(String queryId) {
        ResourceUrlSupplier clone = cloneImpl();
        clone.queryId = Optional.of(queryId);
        return clone;
    }

    public ResourceUrlSupplier cursorId(String cursorId) {
        ResourceUrlSupplier clone = cloneImpl();
        clone.cursorId = Optional.of(cursorId);
        return clone;
    }

    public ResourceUrlSupplier pageId(String pageId) {
        ResourceUrlSupplier clone = cloneImpl();
        clone.pageId = Optional.of(pageId);
        return clone;
    }
    //endregion

    //region UrlSupplierBase Implementation
    @Override
    public String get() {
        if (!this.queryId.isPresent()) {
            return null;
        }

        if (!this.cursorId.isPresent()) {
            return this.baseUrl + "/query/" + this.queryId.get();
        }

        if (!this.pageId.isPresent()) {
            return this.baseUrl + "/query/" + this.queryId.get() + "/cursor/" + this.cursorId.get();
        }

        return this.baseUrl + "/query/" + this.queryId.get() + "/cursor/" + this.cursorId.get() + "/page/" + this.pageId.get();
    }
    //endregion

    //region Protected Methods
    protected ResourceUrlSupplier cloneImpl() {
        ResourceUrlSupplier clone = new ResourceUrlSupplier(this.baseUrl);
        clone.queryId = this.queryId;
        clone.cursorId = this.cursorId;
        clone.pageId = this.pageId;
        return clone;
    }
    //endregion

    //region Fields
    protected Optional<String> queryId;
    protected Optional<String> cursorId;
    protected Optional<String> pageId;
    //endregion
}
