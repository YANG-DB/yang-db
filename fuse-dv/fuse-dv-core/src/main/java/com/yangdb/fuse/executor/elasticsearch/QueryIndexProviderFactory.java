package com.yangdb.fuse.executor.elasticsearch;

/*-
 * #%L
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2020 The YangDb Graph Database Project
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
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.schema.IndexProvider;
import org.elasticsearch.client.Client;

import java.util.Optional;

/**
 * Index Provider factory will generate an Index Provider instance specific for a query
 * Based on the ontology
 */
public class QueryIndexProviderFactory {

    private final Client client;
    private final RawSchema schema;
    private final OntologyProvider ontologyProvider;

    @Inject
    public QueryIndexProviderFactory(Client client, RawSchema schema, OntologyProvider ontologyProvider) {
        this.client = client;
        this.schema = schema;
        this.ontologyProvider = ontologyProvider;
    }

    /**
     * generate an Index Provider based on the :
     *  * Ontology -
     *  * Query -
     *  * MappingFlavor -
     *
     * @param query
     * @param flavor
     * @return
     */
    public IndexProvider generate(Query query,MappingEntitiesFlavor flavor) {
        String ont = query.getOnt();
        Optional<Ontology> ontology = ontologyProvider.get(ont);
        if(!ontology.isPresent())
            throw new FuseError.FuseErrorException(new FuseError(String.format("No Ontology named %s found",ont),String.format("No Ontology named %s found, context: %s ",ont,this.getClass().getSimpleName())));


        return new IndexProvider();
    }
}
