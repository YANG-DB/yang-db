package com.yangdb.fuse.dispatcher.descriptors;

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



import com.yangdb.fuse.dispatcher.resource.QueryResource;
import com.yangdb.fuse.model.descriptors.Descriptor;

/**
 * Created by roman.margolis on 29/11/2017.
 */
public class QueryResourceDescriptor implements Descriptor<QueryResource> {
    //region Descriptor Implementation
    @Override
    public String describe(QueryResource item) {
        return String.format("Query{id: %s, name: %s, ont: %s}",
                item.getQueryMetadata().getId(),
                item.getQueryMetadata().getName(),
                item.getQuery().getOnt());
    }
    //endregion
}
