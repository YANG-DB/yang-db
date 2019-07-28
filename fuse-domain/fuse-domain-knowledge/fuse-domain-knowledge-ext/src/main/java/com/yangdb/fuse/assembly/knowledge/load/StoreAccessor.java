package com.yangdb.fuse.assembly.knowledge.load;

/*-
 * #%L
 * fuse-domain-knowledge-ext
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

import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.yangdb.fuse.assembly.knowledge.KnowledgeRawSchema.ENTITY;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

public class StoreAccessor {

    public static Optional<Map> findEntityById(String fieldName, String id, String label, RawSchema schema, Client client) {
        //get all indices that accommodate the label
        List<String> indices = schema.getPartitions(label).stream()
                .flatMap(partition -> StreamSupport.stream(partition.getIndices().spliterator(), false))
                .collect(Collectors.toList());

        SearchRequestBuilder builder = client.prepareSearch(indices.toArray(new String[indices.size()]));
        builder.setQuery(
                boolQuery().filter(
                                boolQuery()
                                    .filter(termQuery("type", ENTITY))
                                    .must(termQuery(fieldName, id))));
        SearchResponse response = builder.get();
        if(response.getHits().totalHits == 0)
            return Optional.empty();

        //assuming a single relevant reasult
        SearchHit hits = response.getHits().getHits()[0];
        return Optional.of(hits.getSourceAsMap());


    }
}
