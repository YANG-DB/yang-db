package com.kayhut.fuse.epb.plan.statistics.util;

import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.transport.TransportClient;

import java.util.Map;
import java.util.Optional;

/**
 * Created by benishue on 24-May-17.
 */
public class ElasticUtil {

    public static Optional<Map<String, Object>> getDocumentById(TransportClient client, String indexName, String documentType, String id) {
        GetResponse r = client.get((new GetRequest(indexName, documentType, id))).actionGet();
        if (r != null && r.isExists()) {
            return Optional.ofNullable(r.getSourceAsMap());
        }
        return Optional.empty();
    }
}
