package com.yangdb.fuse.assembly.knowledge.load;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.assembly.knowledge.KnowledgeRawSchemaShort;
import com.yangdb.fuse.dispatcher.driver.IdGeneratorDriver;
import com.yangdb.fuse.executor.ontology.schema.load.GraphDataLoader;
import com.yangdb.fuse.model.Range;
import com.yangdb.fuse.model.logical.LogicalGraphModel;
import com.yangdb.fuse.model.ontology.transformer.OntologyTransformer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class KnowledgeGraphTransformerTest {
    private ObjectMapper mapper = new ObjectMapper();
    private OntologyTransformer ontTransformer;
    private LogicalGraphModel graphModel;

    @Before
    public void setUp() throws Exception {
        final URL resource = getClass().getResource("/ontology/KnowledgeTransformation.json");
        ontTransformer = mapper.readValue(resource, OntologyTransformer.class);
        final InputStream stream = getClass().getResourceAsStream("/data/knowledge_graph.json");
        graphModel =  mapper.readValue(stream,LogicalGraphModel.class);

    }

    /**
     * load the given input json graph - all must comply with the ontology and physical schema bounded
     *
     * Example:
     * {
     *         "nodes": [
     *             {
     *                 "id": "0",
     *                 "label": "Person",
"type": "logical",
     *                 "metadata": {
     *                     "user-defined": "values"
     *                 }
     *                 "properties":{
     *                     "fName": "first name",
     *                     "lName":"last name",
     *                     "born": "12/12/2000",
     *                     "age": "19",
     *                     "email": "myName@fuse.com",
     *                     "address": {
     *                             "state": "my state",
     *                             "street": "my street",
     *                             "city": "my city",
     *                             "zip": "gZip"
     *                     }
     *                 }
     *             },
     *             {
     *                 "id": "10",
     *                 "label": "Person",
"type": "logical",
     *                 "metadata": {
     *                     "user-defined": "values"
     *                 }
     *                 "properties":{
     *                     "fName": "another first name",
     *                     "lName":"another last name",
     *                     "age": "20",
     *                     "born": "1/1/1999",
     *                     "email": "notMyName@fuse.com",
     *                     "address": {
     *                             "state": "not my state",
     *                             "street": "not my street",
     *                             "city": "not my city",
     *                             "zip": "not gZip"
     *                     }
     *                 }
     *             }
     *         ],
     *         "edges": [
     *             {
     *                 "id": 100,
     *                 "source": "0",
     *                 "target": "1",
     *                 "metadata": {
     *                     "label": "knows",
     *                     "user-defined": "values"
     *                 },
     *                 "properties":{
     *                      "date":"01/01/2000",
     *                      "medium": "facebook"
     *                 }
     *             },
     *             {
     *                 "id": 101,
     *                 "source": "0",
     *                 "target": "1",
     *                 "metadata": {
     *                     "label": "called",
     *                     "user-defined": "values"
     *                 },
     *                 "properties":{
     *                      "date":"01/01/2000",
     *                      "duration":"120",
     *                      "medium": "cellular"
     *                      "sourceLocation": "40.06,-71.34"
     *                      "sourceTarget": "41.12,-70.9"
     *                 }
     *             }
     *         ]
     * }
     * @return
     * @throws IOException
     */
    @Test
    public void transform() {
        StoreAccessor client = Mockito.mock(StoreAccessor.class);
        when(client.findEntityById(anyString(),anyString(), anyString(),any() ))
                .thenAnswer(invocationOnMock -> Optional.of(new HashMap()));

        IdGeneratorDriver<Range> idGeneratorDriver = Mockito.mock(IdGeneratorDriver.class);
        when(idGeneratorDriver.getNext(anyString(),anyInt()))
                .thenAnswer(invocationOnMock -> new Range(0,1000));


        final KnowledgeGraphTransformer transformer = new KnowledgeGraphTransformer(new KnowledgeRawSchemaShort(), ontTransformer, idGeneratorDriver,client);
        final KnowledgeContext transform = transformer.transform(graphModel, GraphDataLoader.Directive.INSERT);
        assertNotNull(transform);
        assertEquals(2,transform.getEntities().size());

        assertEquals(2,transform.getEntities().get(0).additionalProperties.size());
        assertEquals(2,transform.getEntities().get(0).additional.size());
        assertEquals(2,transform.getEntities().get(0).hasRel.size());
        assertEquals(2,transform.getEntities().get(1).additionalProperties.size());
        assertEquals(2,transform.getEntities().get(1).additional.size());
        assertEquals(2,transform.getEntities().get(1).hasRel.size());

        assertEquals(2,transform.getRelations().size());
        assertEquals(2,transform.getRelations().get(0).hasValues.size());
        assertEquals(2,transform.getRelations().get(0).additionalProperties.size());
        assertEquals(5,transform.getRelations().get(1).hasValues.size());
        assertEquals(2,transform.getRelations().get(1).additionalProperties.size());

        assertEquals(14,transform.geteValues().size());
        assertEquals(7,transform.getrValues().size());
    }
}
