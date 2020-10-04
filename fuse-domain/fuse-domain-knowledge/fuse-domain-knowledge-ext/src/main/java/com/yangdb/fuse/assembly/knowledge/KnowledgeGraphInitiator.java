package com.yangdb.fuse.assembly.knowledge;

/*-
 * #%L
 * fuse-domain-knowledge-ext
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

import com.google.inject.Inject;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.executor.ontology.schema.load.DataLoaderUtils;
import com.yangdb.fuse.executor.ontology.schema.load.GraphInitiator;
import org.elasticsearch.client.Client;

import java.io.IOException;

public class KnowledgeGraphInitiator implements GraphInitiator {

    private final Client client;
    private final RawSchema schema;

    @Inject
    public KnowledgeGraphInitiator(Client client,RawSchema schema) {
        this.client = client;
        this.schema = schema;
    }

    @Override
    public long init(String ontology) {
        return DataLoaderUtils.init(client,schema);
    }

    @Override
    public long init()  {
        return DataLoaderUtils.init(client,schema);
    }

    @Override
    public long drop(String ontology)  {
        return DataLoaderUtils.drop(client,schema);
    }

    @Override
    public long drop() throws IOException {
        return DataLoaderUtils.drop(client,schema);
    }

    @Override
    public long createTemplate(String ontology, String schemaProvider) {
        return 0;
    }

    @Override
    public long createIndices(String ontology, String schemaProvider)  {
        return 0;
    }


}
