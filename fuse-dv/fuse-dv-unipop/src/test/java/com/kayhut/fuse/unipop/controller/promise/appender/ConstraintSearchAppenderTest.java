package com.kayhut.fuse.unipop.controller.promise.appender;

import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.unipop.controller.common.appender.ConstraintSearchAppender;
import com.kayhut.fuse.unipop.controller.common.context.CompositeControllerContext;
import com.kayhut.fuse.unipop.controller.common.context.VertexControllerContext;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.*;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.TimeSeriesIndexPartitions;
import com.kayhut.fuse.unipop.step.BoostingStepWrapper;
import com.kayhut.fuse.unipop.structure.ElementType;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.elasticsearch.action.search.SearchAction;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Client;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.stubbing.Answer;
import org.unipop.process.predicate.Text;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.kayhut.fuse.model.OntologyTestUtils.OWN;
import static com.kayhut.fuse.model.OntologyTestUtils.START_DATE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by lior.perry on 29/03/2017.
 */
public class ConstraintSearchAppenderTest {
    private static String INDEX_PREFIX = "idx-";
    private static String INDEX_FORMAT = "idx-%s";
    private static String DATE_FORMAT_STRING = "yyyy-MM-dd-HH";
    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING);

    private GraphElementSchemaProvider buildSchemaProvider(Ontology.Accessor ont) {
        Iterable<GraphVertexSchema> vertexSchemas =
                Stream.ofAll(ont.entities())
                        .map(entity -> (GraphVertexSchema) new GraphVertexSchema.Impl(
                                entity.geteType(),
                                entity.geteType().equals(OntologyTestUtils.PERSON.name) ? new StaticIndexPartitions(Arrays.asList("Persons1", "Persons2")) :
                                        entity.geteType().equals(OntologyTestUtils.DRAGON.name) ? new StaticIndexPartitions(Arrays.asList("Dragons1", "Dragons2")) :
                                                new StaticIndexPartitions(Collections.singletonList("idx1"))))
                        .toJavaList();

        Iterable<GraphEdgeSchema> edgeSchemas =
                Stream.ofAll(ont.relations())
                        .map(relation -> (GraphEdgeSchema) new GraphEdgeSchema.Impl(
                                relation.getrType(),
                                new GraphElementConstraint.Impl(__.has(T.label, relation.getrType())),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(relation.getePairs().get(0).geteTypeA() + "IdA"),
                                        Optional.of(relation.getePairs().get(0).geteTypeA()))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(relation.getePairs().get(0).geteTypeB() + "IdB"),
                                        Optional.of(relation.getePairs().get(0).geteTypeB()))),
                                Direction.OUT,
                                Optional.of(new GraphEdgeSchema.DirectionSchema.Impl("direction", "out", "in")),
                                Optional.empty(),
                                Optional.of(relation.getrType().equals(OWN.getName()) ?
                                        new TimeSeriesIndexPartitions() {
                                            @Override
                                            public Optional<String> getPartitionField() {
                                                return Optional.of(START_DATE.name);
                                            }

                                            @Override
                                            public Iterable<Partition> getPartitions() {
                                                return Collections.singletonList(() ->
                                                        IntStream.range(0, 3).mapToObj(i -> new Date(System.currentTimeMillis() - 60 * 60 * 1000 * i)).
                                                                map(this::getIndexName).collect(Collectors.toList()));
                                            }

                                            @Override
                                            public String getDateFormat() {
                                                return DATE_FORMAT_STRING;
                                            }

                                            @Override
                                            public String getIndexPrefix() {
                                                return INDEX_PREFIX;
                                            }

                                            @Override
                                            public String getIndexFormat() {
                                                return INDEX_FORMAT;
                                            }

                                            @Override
                                            public String getTimeField() {
                                                return START_DATE.name;
                                            }

                                            @Override
                                            public String getIndexName(Date date) {
                                                return String.format(getIndexFormat(), DATE_FORMAT.format(date));
                                            }
                                        } : new StaticIndexPartitions(Collections.singletonList("idx1"))),
                                Collections.emptyList()))
                        .toJavaList();

        return new OntologySchemaProvider(ont.get(), new GraphElementSchemaProvider.Impl(vertexSchemas, edgeSchemas));
    }

    private Ontology.Accessor ont;
    private GraphElementSchemaProvider graphElementSchemaProvider;

    @Before
    public void setUp() {
        ont = new Ontology.Accessor(OntologyTestUtils.createDragonsOntologyShort());
        graphElementSchemaProvider = buildSchemaProvider(ont);
    }

    @Test
    public void testSimpleNoConstraintAppender() {
        SearchBuilder searchBuilder = new SearchBuilder();
        CompositeControllerContext context = mock(CompositeControllerContext.class);
        VertexControllerContext vertexControllerContext = mock(VertexControllerContext.class);
        Vertex vertex = mock(Vertex.class);
        Client client = mock(Client.class);
        when(client.prepareSearch()).then((Answer<SearchRequestBuilder>) invocation -> new SearchRequestBuilder(client, mock(SearchAction.class)));
        when(vertex.label()).then((Answer<String>) invocation -> "Person");

        when(context.getVertexControllerContext()).then((Answer<Optional<VertexControllerContext>>) invocation -> Optional.of(vertexControllerContext));
        when(context.getBulkVertices()).then((Answer<Iterable<Vertex>>) invocation -> Collections.singleton(vertex));
        when(context.getConstraint()).thenAnswer((Answer<Optional<TraversalConstraint>>) invocation -> Optional.empty());
        when(context.getElementType()).thenAnswer((Answer<ElementType>) invocation -> ElementType.vertex);
        when(context.getSchemaProvider()).thenAnswer((Answer<GraphElementSchemaProvider>) invocation -> graphElementSchemaProvider);

        ConstraintSearchAppender searchAppender = new ConstraintSearchAppender();
        searchAppender.append(searchBuilder, context);
        SearchRequestBuilder builder = searchBuilder.build(client, false);
        Assert.assertEquals("{\"size\":0,\"query\":{\"bool\":{\"filter\":[{\"bool\":{\"must\":[{\"term\":{\"type\":{\"value\":\"Person\",\"boost\":1.0}}}],\"adjust_pure_negative\":true,\"boost\":1.0}}],\"adjust_pure_negative\":true,\"boost\":1.0}},\"_source\":false}", builder.toString());
    }

    @Test
    public void testSimpleAndConstraintAppender() {
        SearchBuilder searchBuilder = new SearchBuilder();
        CompositeControllerContext context = mock(CompositeControllerContext.class);
        VertexControllerContext vertexControllerContext = mock(VertexControllerContext.class);
        Vertex vertex = mock(Vertex.class);
        Client client = mock(Client.class);
        when(client.prepareSearch()).then((Answer<SearchRequestBuilder>) invocation -> new SearchRequestBuilder(client, mock(SearchAction.class)));
        when(vertex.label()).then((Answer<String>) invocation -> "Person");

        when(context.getVertexControllerContext()).then((Answer<Optional<VertexControllerContext>>) invocation -> Optional.of(vertexControllerContext));
        when(context.getBulkVertices()).then((Answer<Iterable<Vertex>>) invocation -> Collections.singleton(vertex));

        Step<?, Object> hasStep = __.start().has("abc", P.eq("123")).asAdmin().getEndStep();
        GraphTraversal.Admin<Object, Object> boostingTraversal = __.start().asAdmin().addStep(new BoostingStepWrapper<>(hasStep, 100));
        BoostingStepWrapper<?, Object> wrappedAndStep = new BoostingStepWrapper<>(__.and(boostingTraversal, __.has("edf", P.eq("bla bla"))).asAdmin().getEndStep(), 1);
        TraversalConstraint constraint = new TraversalConstraint(__.start().asAdmin().addStep(wrappedAndStep));
        when(context.getConstraint()).thenAnswer((Answer<Optional<TraversalConstraint>>) invocation -> Optional.of(constraint));
        when(context.getElementType()).thenAnswer((Answer<ElementType>) invocation -> ElementType.vertex);
        when(context.getSchemaProvider()).thenAnswer((Answer<GraphElementSchemaProvider>) invocation -> graphElementSchemaProvider);

        ConstraintSearchAppender searchAppender = new ConstraintSearchAppender();
        searchAppender.append(searchBuilder, context);
        SearchRequestBuilder builder = searchBuilder.build(client, false);
        Assert.assertEquals("{\"size\":0,\"query\":{\"bool\":{\"must\":[{\"bool\":{\"must\":[{\"term\":{\"abc\":{\"value\":\"123\",\"boost\":100.0}}}],\"filter\":[{\"bool\":{\"must\":[{\"term\":{\"edf\":{\"value\":\"bla bla\",\"boost\":1.0}}}],\"adjust_pure_negative\":true,\"boost\":1.0}}],\"adjust_pure_negative\":true,\"boost\":1.0}}],\"filter\":[{\"bool\":{\"must\":[{\"term\":{\"type\":{\"value\":\"Person\",\"boost\":1.0}}}],\"adjust_pure_negative\":true,\"boost\":1.0}}],\"adjust_pure_negative\":true,\"boost\":1.0}},\"_source\":false}",builder.toString() );
    }

    @Test
    public void testSimpleOrConstraintAppender() {
        SearchBuilder searchBuilder = new SearchBuilder();
        CompositeControllerContext context = mock(CompositeControllerContext.class);
        VertexControllerContext vertexControllerContext = mock(VertexControllerContext.class);
        Vertex vertex = mock(Vertex.class);
        Client client = mock(Client.class);
        when(client.prepareSearch()).then((Answer<SearchRequestBuilder>) invocation -> new SearchRequestBuilder(client, mock(SearchAction.class)));
        when(vertex.label()).then((Answer<String>) invocation -> "Person");

        when(context.getVertexControllerContext()).then((Answer<Optional<VertexControllerContext>>) invocation -> Optional.of(vertexControllerContext));
        when(context.getBulkVertices()).then((Answer<Iterable<Vertex>>) invocation -> Collections.singleton(vertex));

        Step<?, Object> hasStep = __.start().has("abc", P.eq("123")).asAdmin().getEndStep();
        GraphTraversal.Admin<Object, Object> boostingTraversal = __.start().asAdmin().addStep(new BoostingStepWrapper<>(hasStep, 100));
        BoostingStepWrapper<?, Object> wrappedAndStep = new BoostingStepWrapper<>(__.or(boostingTraversal, __.has("edf", P.eq("bla bla"))).asAdmin().getEndStep(), 1);
        TraversalConstraint constraint = new TraversalConstraint(__.start().asAdmin().addStep(wrappedAndStep));
        when(context.getConstraint()).thenAnswer((Answer<Optional<TraversalConstraint>>) invocation -> Optional.of(constraint));
        when(context.getElementType()).thenAnswer((Answer<ElementType>) invocation -> ElementType.vertex);
        when(context.getSchemaProvider()).thenAnswer((Answer<GraphElementSchemaProvider>) invocation -> graphElementSchemaProvider);

        ConstraintSearchAppender searchAppender = new ConstraintSearchAppender();
        searchAppender.append(searchBuilder, context);
        SearchRequestBuilder builder = searchBuilder.build(client, false);
        Assert.assertEquals("{\"size\":0,\"query\":{\"bool\":{\"must\":[{\"bool\":{\"filter\":[{\"bool\":{\"should\":[{\"term\":{\"edf\":{\"value\":\"bla bla\",\"boost\":1.0}}}],\"adjust_pure_negative\":true,\"boost\":1.0}}],\"should\":[{\"term\":{\"abc\":{\"value\":\"123\",\"boost\":100.0}}}],\"adjust_pure_negative\":true,\"boost\":1.0}}],\"filter\":[{\"bool\":{\"must\":[{\"term\":{\"type\":{\"value\":\"Person\",\"boost\":1.0}}}],\"adjust_pure_negative\":true,\"boost\":1.0}}],\"adjust_pure_negative\":true,\"boost\":1.0}},\"_source\":false}",builder.toString() );
    }

    @Test
    public void testSimpleAndHierarchyConstraintAppender() {
        SearchBuilder searchBuilder = new SearchBuilder();
        CompositeControllerContext context = mock(CompositeControllerContext.class);
        VertexControllerContext vertexControllerContext = mock(VertexControllerContext.class);
        Vertex vertex = mock(Vertex.class);
        Client client = mock(Client.class);
        when(client.prepareSearch()).then((Answer<SearchRequestBuilder>) invocation -> new SearchRequestBuilder(client, mock(SearchAction.class)));
        when(vertex.label()).then((Answer<String>) invocation -> "Person");

        when(context.getVertexControllerContext()).then((Answer<Optional<VertexControllerContext>>) invocation -> Optional.of(vertexControllerContext));
        when(context.getBulkVertices()).then((Answer<Iterable<Vertex>>) invocation -> Collections.singleton(vertex));

        Step<?, Object> hasStep = __.start().has("abc", P.eq("123")).asAdmin().getEndStep();
        GraphTraversal.Admin<Object, Object> boostingTraversal = __.start().asAdmin().addStep(new BoostingStepWrapper<>(hasStep, 100));
        BoostingStepWrapper<?, Object> wrappedAndStep = new BoostingStepWrapper<>(__.and(boostingTraversal, __.has("edf", P.eq("bla bla"))).asAdmin().getEndStep(), 5);

        Step<?, Object> andStep = __.and(__.start().has("qwerty", Text.like("*bla*")), __.start().asAdmin().addStep(wrappedAndStep)).asAdmin().getEndStep();
        BoostingStepWrapper wrappedOuterAndStep = new BoostingStepWrapper(andStep, 1);
        TraversalConstraint constraint = new TraversalConstraint(__.start().asAdmin().addStep(wrappedOuterAndStep));
        when(context.getConstraint()).thenAnswer((Answer<Optional<TraversalConstraint>>) invocation -> Optional.of(constraint));
        when(context.getElementType()).thenAnswer((Answer<ElementType>) invocation -> ElementType.vertex);
        when(context.getSchemaProvider()).thenAnswer((Answer<GraphElementSchemaProvider>) invocation -> graphElementSchemaProvider);

        ConstraintSearchAppender searchAppender = new ConstraintSearchAppender();
        searchAppender.append(searchBuilder, context);
        SearchRequestBuilder builder = searchBuilder.build(client, false);
        Assert.assertEquals("{\"size\":0,\"query\":{\"bool\":{\"must\":[{\"bool\":{\"must\":[{\"bool\":{\"must\":[{\"term\":{\"abc\":{\"value\":\"123\",\"boost\":100.0}}}],\"filter\":[{\"bool\":{\"must\":[{\"term\":{\"edf\":{\"value\":\"bla bla\",\"boost\":1.0}}}],\"adjust_pure_negative\":true,\"boost\":1.0}}],\"adjust_pure_negative\":true,\"boost\":5.0}}],\"filter\":[{\"bool\":{\"must\":[{\"wildcard\":{\"qwerty\":{\"wildcard\":\"*bla*\",\"boost\":1.0}}}],\"adjust_pure_negative\":true,\"boost\":1.0}}],\"adjust_pure_negative\":true,\"boost\":1.0}}],\"filter\":[{\"bool\":{\"must\":[{\"term\":{\"type\":{\"value\":\"Person\",\"boost\":1.0}}}],\"adjust_pure_negative\":true,\"boost\":1.0}}],\"adjust_pure_negative\":true,\"boost\":1.0}},\"_source\":false}",builder.toString() );
        //        ES 5 regexp plugin
//        Assert.assertEquals("{\"size\":0,\"query\":{\"bool\":{\"must\":[{\"bool\":{\"must\":[{\"bool\":{\"must\":[{\"term\":{\"abc\":{\"value\":\"123\",\"boost\":100.0}}}],\"filter\":[{\"bool\":{\"must\":[{\"term\":{\"edf\":{\"value\":\"bla bla\",\"boost\":1.0}}}],\"adjust_pure_negative\":true,\"boost\":1.0}}],\"adjust_pure_negative\":true,\"boost\":5.0}}],\"filter\":[{\"bool\":{\"must\":[{\"script\":{\"script\":{\"source\":\"wildcard\",\"lang\":\"native\",\"params\":{\"expression\":\"*bla*\",\"field\":\"qwerty\"}},\"boost\":1.0}}],\"adjust_pure_negative\":true,\"boost\":1.0}}],\"adjust_pure_negative\":true,\"boost\":1.0}}],\"filter\":[{\"bool\":{\"must\":[{\"term\":{\"type\":{\"value\":\"Person\",\"boost\":1.0}}}],\"adjust_pure_negative\":true,\"boost\":1.0}}],\"adjust_pure_negative\":true,\"boost\":1.0}},\"_source\":false}",builder.toString() );
    }

    @Test
    public void testSimpleAndWithOrHierarchyConstraintAppender() {
        SearchBuilder searchBuilder = new SearchBuilder();
        CompositeControllerContext context = mock(CompositeControllerContext.class);
        VertexControllerContext vertexControllerContext = mock(VertexControllerContext.class);
        Vertex vertex = mock(Vertex.class);
        Client client = mock(Client.class);
        when(client.prepareSearch()).then((Answer<SearchRequestBuilder>) invocation -> new SearchRequestBuilder(client, mock(SearchAction.class)));
        when(vertex.label()).then((Answer<String>) invocation -> "Person");

        when(context.getVertexControllerContext()).then((Answer<Optional<VertexControllerContext>>) invocation -> Optional.of(vertexControllerContext));
        when(context.getBulkVertices()).then((Answer<Iterable<Vertex>>) invocation -> Collections.singleton(vertex));

        Step<?, Object> hasStep = __.start().has("abc", P.eq("123")).asAdmin().getEndStep();
        GraphTraversal.Admin<Object, Object> boostingTraversal = __.start().asAdmin().addStep(new BoostingStepWrapper<>(hasStep, 100));
        BoostingStepWrapper<?, Object> wrappedAndStep = new BoostingStepWrapper<>(__.and(boostingTraversal, __.has("edf", P.eq("bla bla"))).asAdmin().getEndStep(), 5);

        Step<?, Object> orStep = __.or(__.start().has("qwerty", Text.like("*bla*")), __.start().asAdmin().addStep(wrappedAndStep)).asAdmin().getEndStep();
        BoostingStepWrapper wrappedOuterAndStep = new BoostingStepWrapper(orStep, 1);
        TraversalConstraint constraint = new TraversalConstraint(__.start().asAdmin().addStep(wrappedOuterAndStep));
        when(context.getConstraint()).thenAnswer((Answer<Optional<TraversalConstraint>>) invocation -> Optional.of(constraint));
        when(context.getElementType()).thenAnswer((Answer<ElementType>) invocation -> ElementType.vertex);
        when(context.getSchemaProvider()).thenAnswer((Answer<GraphElementSchemaProvider>) invocation -> graphElementSchemaProvider);

        ConstraintSearchAppender searchAppender = new ConstraintSearchAppender();
        searchAppender.append(searchBuilder, context);
        SearchRequestBuilder builder = searchBuilder.build(client, false);
        Assert.assertEquals("{\"size\":0,\"query\":{\"bool\":{\"must\":[{\"bool\":{\"filter\":[{\"bool\":{\"should\":[{\"wildcard\":{\"qwerty\":{\"wildcard\":\"*bla*\",\"boost\":1.0}}}],\"adjust_pure_negative\":true,\"boost\":1.0}}],\"should\":[{\"bool\":{\"must\":[{\"term\":{\"abc\":{\"value\":\"123\",\"boost\":100.0}}}],\"filter\":[{\"bool\":{\"must\":[{\"term\":{\"edf\":{\"value\":\"bla bla\",\"boost\":1.0}}}],\"adjust_pure_negative\":true,\"boost\":1.0}}],\"adjust_pure_negative\":true,\"boost\":5.0}}],\"adjust_pure_negative\":true,\"boost\":1.0}}],\"filter\":[{\"bool\":{\"must\":[{\"term\":{\"type\":{\"value\":\"Person\",\"boost\":1.0}}}],\"adjust_pure_negative\":true,\"boost\":1.0}}],\"adjust_pure_negative\":true,\"boost\":1.0}},\"_source\":false}",builder.toString());
//        ES 5 regexp plugin
//        Assert.assertEquals("{\"size\":0,\"query\":{\"bool\":{\"must\":[{\"bool\":{\"filter\":[{\"bool\":{\"should\":[{\"script\":{\"script\":{\"source\":\"wildcard\",\"lang\":\"native\",\"params\":{\"expression\":\"*bla*\",\"field\":\"qwerty\"}},\"boost\":1.0}}],\"adjust_pure_negative\":true,\"boost\":1.0}}],\"should\":[{\"bool\":{\"must\":[{\"term\":{\"abc\":{\"value\":\"123\",\"boost\":100.0}}}],\"filter\":[{\"bool\":{\"must\":[{\"term\":{\"edf\":{\"value\":\"bla bla\",\"boost\":1.0}}}],\"adjust_pure_negative\":true,\"boost\":1.0}}],\"adjust_pure_negative\":true,\"boost\":5.0}}],\"adjust_pure_negative\":true,\"boost\":1.0}}],\"filter\":[{\"bool\":{\"must\":[{\"term\":{\"type\":{\"value\":\"Person\",\"boost\":1.0}}}],\"adjust_pure_negative\":true,\"boost\":1.0}}],\"adjust_pure_negative\":true,\"boost\":1.0}},\"_source\":false}",builder.toString());
    }
}
