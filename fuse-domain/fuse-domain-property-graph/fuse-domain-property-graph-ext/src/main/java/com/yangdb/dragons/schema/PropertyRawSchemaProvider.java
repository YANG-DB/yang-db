package com.yangdb.dragons.schema;

/*-
 * #%L
 * fuse-domain-property-graph-ext
 * %%
 * Copyright (C) 2016 - 2019 The Fuse Graph Database Project
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
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;

import java.util.List;

public class PropertyRawSchemaProvider implements RawSchema {

    @Inject
    public PropertyRawSchemaProvider() {

    }

    @Override
    public IndexPartitions getPartition(String type) {
        return null;
    }

    @Override
    public String getIdFormat(String type) {
        return null;
    }

    @Override
    public String getPrefix(String type) {
        return null;
    }

    @Override
    public List<IndexPartitions.Partition> getPartitions(String type) {
        return null;
    }

    @Override
    public Iterable<String> indices() {
        return null;
    }
}
