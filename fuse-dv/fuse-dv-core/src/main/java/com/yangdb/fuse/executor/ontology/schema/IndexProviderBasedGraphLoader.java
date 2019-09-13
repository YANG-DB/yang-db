package com.yangdb.fuse.executor.ontology.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.yangdb.fuse.model.logical.LogicalGraphModel;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.schema.IndexProvider;
import javaslang.collection.Stream;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.client.Client;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class IndexProviderBasedGraphLoader implements GraphDataLoader<String, FuseError> {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    static {
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private Client client;
    private RawSchema schema;
    private IndexProvider indexProvider;
    private ObjectMapper mapper;

    @Inject
    public IndexProviderBasedGraphLoader(Client client, RawSchema schema, IndexProvider indexProvider) {
        this.client = client;
        this.schema = schema;
        this.indexProvider = indexProvider;
        this.mapper = new ObjectMapper();
    }




    @Override
    public long init()  {
        Iterable<String> allIndices = schema.indices();

        Stream.ofAll(allIndices)
                .filter(index -> client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists())
                .forEach(index -> client.admin().indices().delete(new DeleteIndexRequest(index)).actionGet());
        Stream.ofAll(allIndices).forEach(index -> client.admin().indices()
                .create(new CreateIndexRequest(index.toLowerCase())).actionGet());

        return Stream.ofAll(allIndices).count(s -> !s.isEmpty());

    }

    @Override
    public long drop()  {
        Iterable<String> indices = schema.indices();
        Stream.ofAll(indices)
                .filter(index -> client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists())
                .forEach(index -> client.admin().indices().delete(new DeleteIndexRequest(index)).actionGet());
        return Stream.ofAll(indices).count(s -> !s.isEmpty());
    }

    @Override
    public LoadResponse<String, FuseError> load(LogicalGraphModel root, Directive directive)  {
        return null;
    }

    @Override
    public LoadResponse<String, FuseError> load(File data, Directive directive)  {
        return null;
    }

}
