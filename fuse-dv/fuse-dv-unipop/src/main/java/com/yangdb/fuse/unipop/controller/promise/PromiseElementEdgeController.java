package com.yangdb.fuse.unipop.controller.promise;

/*-
 * #%L
 * fuse-dv-unipop
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

import com.codahale.metrics.MetricRegistry;
import com.yangdb.fuse.unipop.controller.ElasticGraphConfiguration;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.elasticsearch.client.Client;
import org.unipop.query.search.SearchQuery;
import org.unipop.structure.UniGraph;

import java.util.Iterator;

/**
 * Created by lior.perry on 4/2/2017.
 */ //region PromiseElementEdgeController Implementation
public class PromiseElementEdgeController implements SearchQuery.SearchController {
    public PromiseElementEdgeController(Client client, ElasticGraphConfiguration configuration, UniGraph graph, GraphElementSchemaProvider schemaProvider, MetricRegistry metricRegistry) {}

    //region SearchQuery.SearchController Implementation
    @Override
    public <E extends Element> Iterator<E> search(SearchQuery<E> searchQuery) {
        return null;
    }
    //endregion
}
