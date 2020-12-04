package com.yangdb.fuse.executor.elasticsearch.terms.transport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.executor.elasticsearch.terms.model.Edge;
import com.yangdb.fuse.executor.elasticsearch.terms.model.Vertex;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.util.HashMap;

public class GraphExploreResponseTest {

    final ObjectMapper mapper = new ObjectMapper();

    final String GRAPH_EXPLOR_ERESPONSE_CONTENT = "{\"took\":{\"millis\":145,\"nanos\":145000000,\"hours\":0,\"minutes\":0,\"seconds\":0,\"days\":0,\"microsFrac\":145000.0,\"millisFrac\":145.0,\"micros\":145000,\"secondsFrac\":0.145,\"minutesFrac\":0.002416666666666667,\"hoursFrac\":4.027777777777778E-5,\"daysFrac\":1.6782407407407408E-6,\"stringRep\":\"145ms\"},\"tookInMillis\":145,\"timedOut\":false,\"vertices\":[{\"field\":\"name\",\"term\":\"Wayed\",\"weight\":0.69,\"bg\":10,\"fg\":0,\"stepsDepth\":0,\"id\":\"name:Wayed\"},{\"field\":\"name\",\"term\":\"Wayed doc\",\"weight\":0.687,\"bg\":10,\"fg\":0,\"stepsDepth\":0,\"id\":\"name:Wayed doc\"}],\"edgesMap\":[{\"from\":{\"field\":\"name\",\"term\":\"Wayed\",\"weight\":0.69,\"bg\":10,\"fg\":0,\"stepsDepth\":0,\"id\":\"name:Wayed\"},\"to\":{\"field\":\"name\",\"term\":\"Wayed doc\",\"weight\":0.687,\"bg\":10,\"fg\":0,\"stepsDepth\":0,\"id\":\"name:Wayed doc\"},\"weight\":0.689,\"docCount\":12,\"id\":\"name:Wayed->name:Wayed doc\"}],\"returnDetailedInfo\":true}";

    @Test
    @Ignore("Todo verify deserializers work properly")
    public void testDeserialization() throws JsonProcessingException {
        GraphExploreResponse response = mapper.readValue(GRAPH_EXPLOR_ERESPONSE_CONTENT, GraphExploreResponse.class);
        Assert.assertTrue(response.isReturnDetailedInfo());
        Assert.assertFalse(response.isTimedOut());
        Assert.assertEquals(response.getTookInMillis(), 45);
        Assert.assertEquals(response.getVertices().size(), 2);
        Assert.assertEquals(response.getEdgesMap().size(), 1);

    }

    @Test
    public void testSerialization() throws JsonProcessingException {
        GraphExploreResponse exploreResponse = new GraphExploreResponse();
        //edges map
        HashMap<Edge.EdgeId, Edge> edgesMap = new HashMap<>();
        Vertex.VertexId source = new Vertex.VertexId("name", "Wayed");
        Vertex.VertexId target = new Vertex.VertexId("name", "Wayed doc");

        Vertex from = new Vertex("name", "Wayed", 0.69, 0, 10, 0);
        Vertex to = new Vertex("name", "Wayed doc", 0.687, 0, 10, 0);
        edgesMap.put(new Edge.EdgeId(source, target), new Edge(from, to, 0.689, 12));
        exploreResponse.setEdgesMap(edgesMap);

        exploreResponse.setReturnDetailedInfo(true);
        exploreResponse.setTimedOut(false);
        exploreResponse.setTookInMillis(145);

        HashMap<Vertex.VertexId, Vertex> vertices = new HashMap<>();
        vertices.put(source, from);
        vertices.put(target, to);
        exploreResponse.setVertices(vertices);

        exploreResponse.setShardFailures(ShardSearchFailure.EMPTY_ARRAY);

        String json = mapper.writeValueAsString(exploreResponse);

        JSONAssert.assertEquals(
                GRAPH_EXPLOR_ERESPONSE_CONTENT,
                json,
                JSONCompareMode.LENIENT);
    }

}
