package com.yangdb.fuse.core.driver;

/*-
 * #%L
 * fuse-dv-core
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

import com.google.inject.Inject;
import com.yangdb.fuse.dispatcher.driver.PageDriverBase;
import com.yangdb.fuse.dispatcher.resource.CursorResource;
import com.yangdb.fuse.dispatcher.resource.PageResource;
import com.yangdb.fuse.dispatcher.resource.QueryResource;
import com.yangdb.fuse.dispatcher.resource.store.ResourceStore;
import com.yangdb.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.yangdb.fuse.model.results.QueryResultBase;

/**
 * Created by lior.perry on 08/03/2017.
 */
public class StandardPageDriver extends PageDriverBase {
    //region Constructors
    @Inject
    public StandardPageDriver(ResourceStore resourceStore, AppUrlSupplier urlSupplier) {
        super(resourceStore, urlSupplier);
    }
    //endregion

    //region PageDriverBase Implementation
    @Override
    protected PageResource<QueryResultBase> createResource(QueryResource queryResource, CursorResource cursorResource, String pageId, int pageSize) {
        QueryResultBase results = cursorResource.getCursor().getNextResults(pageSize);

        return new PageResource<>(pageId, results, pageSize, 0)
                        .withActualSize(results.getSize())
                        .available();
    }
    //endregion
}
