package com.yangdb.fuse.executor.ontology.discrete;

/*-
 * #%L
 * fuse-dv-core
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
import com.yangdb.fuse.dispatcher.driver.IdGeneratorDriver;
import com.yangdb.test.BaseITMarker;
import com.yangdb.fuse.executor.ontology.schema.GraphElementSchemaProviderJsonFactory;
import com.yangdb.fuse.executor.ontology.schema.load.CSVTransformer;
import com.yangdb.fuse.executor.ontology.schema.load.DataLoaderUtils;
import com.yangdb.fuse.executor.ontology.schema.load.GraphDataLoader;
import com.yangdb.fuse.executor.ontology.schema.load.IndexProviderBasedCSVLoader;
import com.yangdb.fuse.model.results.LoadResponse;
import com.yangdb.fuse.model.GlobalConstants;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.unipop.controller.OpensearchGraphConfiguration;
import com.yangdb.fuse.unipop.controller.search.SearchOrderProvider;
import com.yangdb.fuse.unipop.promise.Constraint;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opensearch.action.admin.indices.refresh.RefreshRequest;
import org.opensearch.action.search.SearchType;
import org.unipop.configuration.UniGraphConfiguration;
import org.unipop.process.predicate.DistinctFilterP;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.yangdb.fuse.executor.TestSuiteIndexProviderSuite.*;
import static com.yangdb.fuse.model.GlobalConstants.HasKeys.CONSTRAINT;

/**
 * Element identity in traversal results (KNOWN_ISSUES Q2, Q3, Q4): loads a tiny graph through the CSV loader and runs
 * the traversals the planner emits for {@code (a:A)-[r:R]->(b:B)}, asserting on ids and types of what comes back.
 */
public class TraversalIdentityIT implements BaseITMarker {
    private static final String OWN_HEADER = "id,entityA.id,entityA.type,entityA.firstName,entityA.name,entityB.id,entityB.type,entityB.name,startDate,endDate\n";
    private static final String FIRE_HEADER = "id,entityA.id,entityA.type,entityA.name,entityA.color,entityB.id,entityB.type,entityB.name,entityB.color,date,temperature\n";

    private GraphTraversalSource g;

    @Before
    public void setUp() throws Exception {
        DataLoaderUtils.init(client, nestedSchema);

        CSVTransformer transformer = new CSVTransformer(config, ontologyProvider, nestedProviderIfc, nestedSchema,
                Mockito.mock(IdGeneratorDriver.class), client);
        IndexProviderBasedCSVLoader loader = new IndexProviderBasedCSVLoader(client, transformer, nestedSchema);

        load(loader, "Entity", "Person", "id,firstName,lastName,gender,birthDate,deathDate,height,name\n" +
                "42,p42,,male,,,,p42\n" +
                "22,p22,,male,,,,p22\n" +
                "85,p85,,male,,,,p85\n" +
                "23,p23,,male,,,,p23\n");
        load(loader, "Entity", "Dragon", "id,name,birthDate,power,gender,color\n" +
                "7,d7,,,,\n34,d34,,,,\n22,d22,,,,\n42,d42,,,,\n4,d4,,,,\n465,d465,,,,\n44,d44,,,,\n65,d65,,,,\n");
        load(loader, "Entity", "Horse", "id,name,weight,maxSpeed,distance\n7,h7,,,\n9,h9,,,\n");

        load(loader, "Relation", "Own", OWN_HEADER +
                // Person 42 owns Dragon 34 and Horse 7 (Dragon 7 exists, but nobody owns it)
                "own1,42,Person,p42,p42,34,Dragon,d34,01/01/1900,\n" +
                "own2,42,Person,p42,p42,7,Horse,h7,01/01/1900,\n" +
                // same id on both ends
                "own3,22,Person,p22,p22,22,Dragon,d22,01/01/1900,\n" +
                // Dragon 42 (same id as Person 42) is owned by Person 85
                "own4,85,Person,p85,p85,42,Dragon,d42,01/01/1900,\n" +
                // Dragon 7 is owned too, so Horse 7 and Dragon 7 meet in one bulk of owned things
                "own5,85,Person,p85,p85,7,Dragon,d7,01/01/1900,\n" +
                // Person 23 owns a dragon and a horse whose ids collide with nothing
                "own6,23,Person,p23,p23,65,Dragon,d65,01/01/1900,\n" +
                "own7,23,Person,p23,p23,9,Horse,h9,01/01/1900,\n");

        load(loader, "Relation", "Fire", FIRE_HEADER +
                // 4->465 and 44->65 concatenate to the same "Fire4465"
                "fire1,4,Dragon,d4,red,465,Dragon,d465,red,1/1/1950,100\n" +
                "fire2,44,Dragon,d44,red,65,Dragon,d65,red,1/1/1950,100\n" +
                // two distinct fire events between the same pair
                "fire3,4,Dragon,d4,red,465,Dragon,d465,red,1/1/1951,200\n");

        client.admin().indices().refresh(new RefreshRequest()).actionGet();

        // wrapped like the service does (CachedGraphElementSchemaProviderFactory)
        GraphElementSchemaProvider schemaProvider = new GraphElementSchemaProvider.Cached(
                new GraphElementSchemaProviderJsonFactory(config, nestedProviderIfc, ontologyProvider).get(ontology));
        OpensearchGraphConfiguration graphConfiguration = new OpensearchGraphConfiguration();
        graphConfiguration.setElasticGraphDefaultSearchSize(1000);
        graphConfiguration.setElasticGraphMaxSearchSize(1000);
        graphConfiguration.setElasticGraphScrollSize(1000);
        graphConfiguration.setElasticGraphScrollTime(60000);
        UniGraphConfiguration uniGraphConfiguration = new UniGraphConfiguration();
        uniGraphConfiguration.setBulkMax(1000);
        uniGraphConfiguration.setBulkStart(1000);

        g = new M1ElasticUniGraphProvider(client, graphConfiguration, uniGraphConfiguration, ont -> schemaProvider,
                context -> SearchOrderProvider.of(SearchOrderProvider.EMPTY, SearchType.DEFAULT), new MetricRegistry())
                .getGraph(ontology).traversal();
    }

    private static void load(IndexProviderBasedCSVLoader loader, String kind, String label, String csv) throws Exception {
        LoadResponse<String, FuseError> response = loader.load(kind, label, csv, GraphDataLoader.Directive.INSERT);
        Assert.assertTrue(label + " load failures: " + response.getResponses().get(1).getFailures(),
                response.getResponses().get(1).getFailures().isEmpty());
    }

    /** the traversal the planner emits for {@code (a:fromType {id})-[r:rel]->(b)} (b untyped). */
    private GraphTraversal<?, Path> outgoing(String fromType, String fromId, String rel) {
        return g.V().as("A")
                .has(CONSTRAINT, Constraint.by(__.and(__.has(T.id, fromId), __.has(T.label, fromType))))
                .outE().as("A-->B").has(CONSTRAINT, Constraint.by(__.has(T.label, rel)))
                .otherV().as("B")
                .path();
    }

    private static String vertexKey(Vertex v) {
        return v.label() + ":" + v.id();
    }

    /** "outType:outId->inType:inId" per relationship */
    private static List<String> edgeEnds(List<Path> paths) {
        return paths.stream()
                .map(p -> (Edge) p.get("A-->B"))
                .map(e -> vertexKey(e.outVertex()) + "->" + vertexKey(e.inVertex()))
                .sorted().collect(Collectors.toList());
    }

    private static List<String> targets(List<Path> paths) {
        return paths.stream().map(p -> vertexKey(p.get("B"))).sorted().collect(Collectors.toList());
    }

    // Q2: Person 42 owns Dragon 34 and Horse 7 - the horse must come back as a Horse, not as Dragon 7
    @Test
    public void testRelationshipEndpointKeepsItsStoredType() {
        List<Path> paths = outgoing("Person", "42", "Own").toList();
        Assert.assertEquals(Arrays.asList("Dragon:34", "Horse:7"), targets(paths));
        Assert.assertEquals(Arrays.asList("Person:42->Dragon:34", "Person:42->Horse:7"), edgeEnds(paths));
    }

    /**
     * the service's shape when the target entity has a filter (EntityFilterOp, e.g. projected properties): the
     * reached vertices are checked against their index through the promise-filter step
     */
    private GraphTraversal<?, Path> ownedThroughFilter(String targetType) {
        return ownedThroughFilter(__.has(T.label, "Person"), targetType);
    }

    private GraphTraversal<?, Path> ownedThroughFilter(org.apache.tinkerpop.gremlin.process.traversal.Traversal owner, String targetType) {
        return g.V().as("A")
                .has(CONSTRAINT, Constraint.by(owner))
                .bothE().as("A--B").has(CONSTRAINT, Constraint.by(__.has(T.label, "Own")))
                .otherV()
                .outE(GlobalConstants.Labels.PROMISE_FILTER)
                .has(CONSTRAINT, Constraint.by(__.has(T.label, targetType)))
                .otherV().as("B")
                .path();
    }

    private static List<String> pairs(List<Path> paths) {
        return paths.stream().map(p -> vertexKey(p.get("A")) + "->" + vertexKey(p.get("B")))
                .sorted().collect(Collectors.toList());
    }

    // Q2 through the filter step: owned things of several types reach the filter in one bulk; each is checked
    // against its own type
    @Test
    public void testFilteredTargetOfMixedTypes() {
        org.apache.tinkerpop.gremlin.process.traversal.Traversal p23 = __.and(__.has(T.id, "23"), __.has(T.label, "Person"));
        Assert.assertEquals(Collections.singletonList("Person:23->Dragon:65"), pairs(ownedThroughFilter(p23, "Dragon").toList()));
        p23 = __.and(__.has(T.id, "23"), __.has(T.label, "Person"));
        Assert.assertEquals(Collections.singletonList("Person:23->Horse:9"), pairs(ownedThroughFilter(p23, "Horse").toList()));
    }

    // Q2 through the filter step: Horse 7 and Dragon 7 reached in the same bulk stay apart
    @Test
    public void testFilteredTargetKeepsItsStoredType() {
        Assert.assertEquals(Arrays.asList("Person:22->Dragon:22", "Person:23->Dragon:65", "Person:42->Dragon:34",
                "Person:85->Dragon:42", "Person:85->Dragon:7"), pairs(ownedThroughFilter("Dragon").toList()));
        Assert.assertEquals(Arrays.asList("Person:23->Horse:9", "Person:42->Horse:7"),
                pairs(ownedThroughFilter("Horse").toList()));
    }

    // Q1: an undirected step from the target side of a relation (Dragons are only ever owned) finds the owners
    @Test
    public void testUndirectedFromTargetSide() {
        List<Path> paths = g.V().as("A")
                .has(CONSTRAINT, Constraint.by(__.and(__.has(T.id, "34"), __.has(T.label, "Dragon"))))
                .bothE().as("A--B").has(CONSTRAINT, Constraint.by(__.has(T.label, "Own")))
                .otherV().as("B")
                .path().toList();
        Assert.assertEquals(Collections.singletonList("Person:42"), targets(paths));
    }

    // Q2 (undirected): the owners of Dragon 42 must not show up as relationships of Person 42
    @Test
    public void testUndirectedRelationshipDoesNotMatchOtherTypeWithSameId() {
        List<Path> paths = g.V().as("A")
                .has(CONSTRAINT, Constraint.by(__.and(__.has(T.id, "42"), __.has(T.label, "Person"))))
                .bothE().as("A--B").has(CONSTRAINT, Constraint.by(__.has(T.label, "Own")))
                .otherV().as("B")
                .path().toList();
        Assert.assertEquals(Arrays.asList("Dragon:34", "Horse:7"), targets(paths));
    }

    // Q3: an edge whose two ends share an id is returned, with the right types on each end
    @Test
    public void testEdgeBetweenEntitiesWithTheSameIdIsReturned() {
        List<Path> paths = outgoing("Person", "22", "Own").toList();
        Assert.assertEquals(Collections.singletonList("Dragon:22"), targets(paths));
        Assert.assertEquals(Collections.singletonList("Person:22->Dragon:22"), edgeEnds(paths));
    }

    // Q4: relationship ids are unique per relationship - no collisions across pairs, no collapse within a pair
    @Test
    public void testRelationshipIdsAreUniquePerRelationship() {
        List<Path> paths = g.V().as("A")
                .has(CONSTRAINT, Constraint.by(__.has(T.label, "Dragon")))
                .outE().as("A-->B").has(CONSTRAINT, Constraint.by(__.has(T.label, "Fire")))
                .otherV().as("B")
                .path().toList();

        Assert.assertEquals(Arrays.asList("Dragon:4->Dragon:465", "Dragon:4->Dragon:465", "Dragon:44->Dragon:65"),
                edgeEnds(paths));
        Set<Object> ids = paths.stream().map(p -> ((Edge) p.get("A-->B")).id()).collect(Collectors.toSet());
        Assert.assertEquals("relationship ids must be distinct: " + ids, 3, ids.size());
    }

    // Q4: distinct() keeps one relationship per endpoint pair - and pairs whose concatenated ids collide stay apart
    @Test
    public void testDistinctKeepsOneRelationshipPerPair() {
        List<Path> paths = g.V().as("A")
                .has(CONSTRAINT, Constraint.by(__.has(T.label, "Dragon")))
                .outE().as("A-->B").has(CONSTRAINT, Constraint.by(__.and(__.has(T.label, "Fire"),
                        __.has("temperature", DistinctFilterP.distinct()))))
                .otherV().as("B")
                .path().toList();
        Assert.assertEquals(Arrays.asList("Dragon:4->Dragon:465", "Dragon:44->Dragon:65"), edgeEnds(paths));
    }

    // Q1: an undirected step returns the relationships in both directions, each with its stored orientation
    @Test
    public void testUndirectedReturnsBothDirectionsWithStoredOrientation() {
        List<Path> paths = g.V().as("A")
                .has(CONSTRAINT, Constraint.by(__.and(__.has(T.id, "465"), __.has(T.label, "Dragon"))))
                .bothE().as("A--B").has(CONSTRAINT, Constraint.by(__.has(T.label, "Fire")))
                .otherV().as("B")
                .path().toList();
        Assert.assertEquals(Arrays.asList("Dragon:4", "Dragon:4"), targets(paths));
        Assert.assertEquals(Arrays.asList("Dragon:4->Dragon:465", "Dragon:4->Dragon:465"),
                paths.stream().map(p -> (Edge) p.get("A--B"))
                        .map(e -> vertexKey(e.outVertex()) + "->" + vertexKey(e.inVertex()))
                        .sorted().collect(Collectors.toList()));

        // the same relationship has the same id whichever end it is reached from
        Set<Object> fromTarget = paths.stream().map(p -> ((Edge) p.get("A--B")).id()).collect(Collectors.toSet());
        Set<Object> fromSource = outgoing("Dragon", "4", "Fire").toList().stream()
                .map(p -> ((Edge) p.get("A-->B")).id()).collect(Collectors.toSet());
        Assert.assertEquals(fromSource, fromTarget);
    }

    // untyped node: the planner expands (p) to every entity type of the ontology, including the nested Profession
    @Test
    public void testUntypedVertexReturnsAllEntityTypes() {
        List<String> all = Arrays.asList("Person", "Profession", "Horse", "Dragon", "Kingdom", "Guild");
        List<String> vertices = g.V().has(CONSTRAINT, Constraint.by(__.has(T.label, org.apache.tinkerpop.gremlin.process.traversal.P.within(all))))
                .toList().stream().map(TraversalIdentityIT::vertexKey).sorted().collect(Collectors.toList());
        Assert.assertEquals(Arrays.asList("Dragon:22", "Dragon:34", "Dragon:4", "Dragon:42", "Dragon:44", "Dragon:465",
                "Dragon:65", "Dragon:7", "Horse:7", "Horse:9", "Person:22", "Person:23", "Person:42", "Person:85"), vertices);
    }
}
