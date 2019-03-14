package com.kayhut.fuse.assembly.knowledge.load;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.assembly.knowledge.KnowledgeRawSchema;
import com.kayhut.fuse.dispatcher.driver.IdGeneratorDriver;
import com.kayhut.fuse.model.Range;
import com.kayhut.fuse.model.logical.LogicalGraphModel;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.transformer.OntologyTransformer;
import com.kayhut.fuse.unipop.process.traversal.dsl.graph.__;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class KnowledgeTransformerTest {
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
     *                 "metadata": {
     *                     "label": "person",
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
     *                 "metadata": {
     *                     "label": "person",
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
        IdGeneratorDriver<Range> idGeneratorDriver = Mockito.mock(IdGeneratorDriver.class);
        when(idGeneratorDriver.getNext(anyString(),anyInt()))
                .thenAnswer(invocationOnMock -> new Range(0,1000));

        final KnowledgeTransformer transformer = new KnowledgeTransformer(ontTransformer,new KnowledgeRawSchema(), idGeneratorDriver);
        final KnowledgeContext transform = transformer.transform(graphModel);
        assertNotNull(transform);
    }
}