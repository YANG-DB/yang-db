package com.yangdb.fuse.executor.ontology.schema;

/*-
 * #%L
 * fuse-dv-core
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.yangdb.fuse.dispatcher.driver.IdGeneratorDriver;
import com.yangdb.fuse.model.Range;
import com.yangdb.fuse.model.logical.LogicalEdge;
import com.yangdb.fuse.model.logical.LogicalGraphModel;
import com.yangdb.fuse.model.logical.LogicalNode;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.results.Relationship;
import com.yangdb.fuse.model.schema.Entity;
import com.yangdb.fuse.model.schema.IndexProvider;
import com.yangdb.fuse.model.schema.Relation;
import javaslang.collection.Stream;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class IndexProviderBasedGraphLoader implements GraphDataLoader<String, FuseError> {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    static {
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private Client client;
    private RawSchema schema;
    private IndexProvider indexProvider;
    private ObjectMapper mapper;
    private Ontology.Accessor accessor;
    private IdGeneratorDriver<Range> idGenerator;
    private static Map<String, Range.StatefulRange> ranges = new HashMap<>();



    @Inject
    public IndexProviderBasedGraphLoader(Client client, Ontology ontology, RawSchema schema, IndexProvider indexProvider, IdGeneratorDriver<Range> idGenerator) {
        this.client = client;
        this.schema = schema;
        this.indexProvider = indexProvider;
        this.accessor = new Ontology.Accessor(ontology);
        this.idGenerator = idGenerator;
        this.mapper = new ObjectMapper();
    }


    @Override
    public long init() {
        Iterable<String> allIndices = schema.indices();

        Stream.ofAll(allIndices)
                .filter(index -> client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists())
                .forEach(index -> client.admin().indices().delete(new DeleteIndexRequest(index)).actionGet());
        Stream.ofAll(allIndices).forEach(index -> client.admin().indices()
                .create(new CreateIndexRequest(index.toLowerCase())).actionGet());

        return Stream.ofAll(allIndices).count(s -> !s.isEmpty());

    }

    @Override
    public long drop() {
        Iterable<String> indices = schema.indices();
        Stream.ofAll(indices)
                .filter(index -> client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists())
                .forEach(index -> client.admin().indices().delete(new DeleteIndexRequest(index)).actionGet());
        return Stream.ofAll(indices).count(s -> !s.isEmpty());
    }

    @Override
    public LoadResponse<String, FuseError> load(LogicalGraphModel root, Directive directive) {
        BulkRequestBuilder builder = client.prepareBulk();

        root.getNodes()
                .stream()
                .filter(n -> accessor.eType(n.label()).isPresent())
                .forEach(n ->
                        builder.add(buildIndexRequest(builder,n)));
        return null;
    }

    private IndexRequest buildIndexRequest(BulkRequestBuilder bulk,LogicalNode node) {
        String label = node.label();
        //get first
        Entity entity = indexProvider.getEntity(label).get();
        String index;
        switch (entity.getPartition()) {
            case "static":
                index = entity.getProps().getValues().get(0);
                break;
            // todo manage time based partitions

            default:
                index = node.label().toLowerCase();
        }

        IndexRequestBuilder request = client.prepareIndex()
                .setIndex(index)
                .setType(label)
                .setId(String.format("%s.%s",schema.getPrefix(label),getRange(label).next()))
                .setOpType(IndexRequest.OpType.INDEX)
                .setSource(toString(entity,node,accessor), XContentType.JSON);
        bulk.add(request);
        return new IndexRequest();
    }

    public static Map<String,Object> toString(Entity entity, LogicalNode node, Ontology.Accessor accessor) {
        Map<String,Object> element = new HashMap<>();
        //populate metadata
        node.metadata().entrySet()
                .stream()
                .filter(m->accessor.entity$(node.label()).containsMetadata(m.getKey()))
                .forEach(m-> element.put(accessor.property$(m.getKey()).getpType(),m.getValue()));
        //populate prop[erties
        switch (entity.getType()) {
            case "Index":
                break;
            // todo manage nested index fields
            default:
        }
        return null;
    }

    public static String toString(LogicalEdge edge, Ontology.Accessor accessor) {

        return null;
    }

    @Override
    public LoadResponse<String, FuseError> load(File data, Directive directive) {
        return null;
    }

    public Range.StatefulRange getRange(String type) {
        //init ranges
        Range.StatefulRange statefulRange = ranges.computeIfAbsent(type,
                s -> new Range.StatefulRange(idGenerator.getNext(type, 1000)));

        if (statefulRange.hasNext())
            return statefulRange;
        //update ranges
        ranges.put(type, new Range.StatefulRange(idGenerator.getNext(type, 1000)));
        //return next range
        return ranges.get(type);
    }

}
