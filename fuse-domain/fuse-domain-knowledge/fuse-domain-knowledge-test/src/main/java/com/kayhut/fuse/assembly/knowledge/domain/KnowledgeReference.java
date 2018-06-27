package com.kayhut.fuse.assembly.knowledge.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;

/**
 * Created by user pc on 5/11/2018.
 */
public class KnowledgeReference {
    private final String cReferenceType = "reference";
    private Reference _ref = new Reference();
    private String _id;

    public String getId() {
       return _id;
    }

    public void setId(String value) {
        _id = value;
    }

    public Reference getRef() {
        return _ref;
    }

    public String getReferenceAsElasticJSON() throws JsonProcessingException {
        // Add needed
        _ref.setType(cReferenceType);

        ObjectMapper mapper = new ObjectMapper();
        return KnowledgeJSONMapperSingleton.getInstance().getMapper().writeValueAsString(_ref);
    }


    public void addToBulk(BulkRequestBuilder bulk, TransportClient client) {
        /*ObjectMapper mapper = new ObjectMapper();
        String index = Stream.ofAll(schema.getPartitions("reference")).map(partition -> (IndexPartitions.Partition.Range<String>) partition)
                .filter(partition -> partition.isWithin(referenceId)).map(partition -> Stream.ofAll(partition.getIndices()).get(0)).get(0);
        bulk.add(client.prepareIndex().setIndex(index).setType(KnowledgeRawSchemaSingleton.cIndexType).setId("ref" + String.format(KnowledgeRawSchemaSingleton.getInstance().getSchema().getIdFormat("reference"), _id))
                .setOpType(IndexRequest.OpType.INDEX)
                .setSource(mapper.writeValueAsString(_ref), XContentType.JSON);*/
    }
}
