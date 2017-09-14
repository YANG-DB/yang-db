package com.kayhut.fuse.unipop.controller.promise.appender;

import com.google.common.collect.Lists;
import com.kayhut.fuse.model.ontology.EPair;
import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.RelationshipType;
import com.kayhut.fuse.unipop.controller.common.context.VertexControllerContext;
import com.kayhut.fuse.unipop.controller.promise.context.PromiseVertexControllerContext;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.OntologySchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartition;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.TimeSeriesIndexPartition;
import com.kayhut.fuse.unipop.structure.ElementType;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Elad on 5/24/2017.
 */
public class PromiseEdgeIndexAppenderTest{

    private final String STATIC_INDEX_EDGE = "edge_no_ts";
    private final String TIME_SERIES_INDEX_EDGE = "edge_only_ts";
    private final List<String> STATIC_INDEX_NAMES = Lists.newArrayList("static_1","static_2");
    private final List<String> TIME_SERIES_INDEX_NAMES = Lists.newArrayList("ts_2017-01","ts_2017-02","ts_2017-03","ts_2017-04","ts_2017-05","ts_2017-06");

    @Test
    public void appendTest_ConstraintLabelOnly_StaticIndex() {

        Ontology ontology = getOntology();
        GraphElementSchemaProvider schemaProvider = getOntologySchemaProvider(ontology);
        TraversalConstraint traversalConstraint = new TraversalConstraint(__.and(__.has(T.label, STATIC_INDEX_EDGE)));

        VertexControllerContext context = new PromiseVertexControllerContext(null, schemaProvider, Optional.of(traversalConstraint),Collections.emptyList(), 0, Collections.emptyList());

        SearchBuilder searchBuilder = new SearchBuilder();

        PromiseEdgeIndexAppender indexAppender = new PromiseEdgeIndexAppender();
        indexAppender.append(searchBuilder, context);

        Assert.assertEquals(STATIC_INDEX_NAMES.size(), searchBuilder.getIndices().size());

        Stream.ofAll(searchBuilder.getIndices()).forEach(index -> Assert.assertTrue(STATIC_INDEX_NAMES.contains(index)));
    }

    @Test
    public void appendTest_ConstraintLabelOnly_TSIndex() throws ParseException {

        Ontology ontology = getOntology();
        GraphElementSchemaProvider schemaProvider = getOntologySchemaProvider(ontology);
        TraversalConstraint traversalConstraint = new TraversalConstraint(__.has(T.label, TIME_SERIES_INDEX_EDGE));

        VertexControllerContext context = new PromiseVertexControllerContext(null, schemaProvider, Optional.of(traversalConstraint),Collections.emptyList(), 0, Collections.emptyList());

        SearchBuilder searchBuilder = new SearchBuilder();

        PromiseEdgeIndexAppender indexAppender = new PromiseEdgeIndexAppender();
        indexAppender.append(searchBuilder, context);

        Assert.assertEquals(TIME_SERIES_INDEX_NAMES.size(), searchBuilder.getIndices().size());

        Stream.ofAll(searchBuilder.getIndices()).forEach(index -> Assert.assertTrue(TIME_SERIES_INDEX_NAMES.contains(index)));
    }

    @Test
    public void appendTest_ConstraintEqTime_TSIndex() throws ParseException {

        Date date = (new SimpleDateFormat("MM/dd/yyyy")).parse("01/01/2017");

        Ontology ontology = getOntology();
        GraphElementSchemaProvider schemaProvider = getOntologySchemaProvider(ontology);
        TraversalConstraint traversalConstraint = new TraversalConstraint(__.and(__.has(T.label, TIME_SERIES_INDEX_EDGE),
                                                                                 __.has("time", P.eq(date))));

        VertexControllerContext context = new PromiseVertexControllerContext(null, schemaProvider, Optional.of(traversalConstraint),Collections.emptyList(), 0, Collections.emptyList());

        SearchBuilder searchBuilder = new SearchBuilder();

        PromiseEdgeIndexAppender indexAppender = new PromiseEdgeIndexAppender();
        indexAppender.append(searchBuilder, context);

        Assert.assertEquals(1, searchBuilder.getIndices().size());

        Assert.assertEquals("ts_2017-01", searchBuilder.getIndices().toArray()[0]);

    }

    @Test
    public void appendTest_ConstraintNEqTime_TSIndex() throws ParseException {

        Date date = (new SimpleDateFormat("MM/dd/yyyy")).parse("01/01/2017");

        Ontology ontology = getOntology();
        GraphElementSchemaProvider schemaProvider = getOntologySchemaProvider(ontology);
        TraversalConstraint traversalConstraint = new TraversalConstraint(__.and(__.has(T.label, TIME_SERIES_INDEX_EDGE),
                __.has("time", P.neq(date))));

        VertexControllerContext context = new PromiseVertexControllerContext(null, schemaProvider, Optional.of(traversalConstraint),Collections.emptyList(), 0, Collections.emptyList());

        SearchBuilder searchBuilder = new SearchBuilder();

        PromiseEdgeIndexAppender indexAppender = new PromiseEdgeIndexAppender();
        indexAppender.append(searchBuilder, context);

        Assert.assertEquals(TIME_SERIES_INDEX_NAMES.size(), searchBuilder.getIndices().size());

        Stream.ofAll(searchBuilder.getIndices()).forEach(index -> Assert.assertTrue(TIME_SERIES_INDEX_NAMES.contains(index)));

    }

    @Test
    public void appendTest_ConstraintGTTime_TSIndex() throws ParseException {

        Date date = (new SimpleDateFormat("MM/dd/yyyy")).parse("03/13/2017");

        Ontology ontology = getOntology();
        GraphElementSchemaProvider schemaProvider = getOntologySchemaProvider(ontology);
        TraversalConstraint traversalConstraint = new TraversalConstraint(__.and(__.has(T.label, TIME_SERIES_INDEX_EDGE),
                __.has("time", P.gt(date))));

        VertexControllerContext context = new PromiseVertexControllerContext(null, schemaProvider, Optional.of(traversalConstraint),Collections.emptyList(), 0, Collections.emptyList());

        SearchBuilder searchBuilder = new SearchBuilder();

        PromiseEdgeIndexAppender indexAppender = new PromiseEdgeIndexAppender();
        indexAppender.append(searchBuilder, context);

        Assert.assertEquals(4, searchBuilder.getIndices().size());

        Assert.assertTrue(searchBuilder.getIndices().containsAll(Lists.newArrayList("ts_2017-03","ts_2017-04","ts_2017-05","ts_2017-06")));

    }

    @Test
    public void appendTest_ConstraintGTETime_TSIndex() throws ParseException {

        Date date = (new SimpleDateFormat("MM/dd/yyyy")).parse("03/13/2017");

        Ontology ontology = getOntology();
        GraphElementSchemaProvider schemaProvider = getOntologySchemaProvider(ontology);
        TraversalConstraint traversalConstraint = new TraversalConstraint(__.and(__.has(T.label, TIME_SERIES_INDEX_EDGE),
                __.has("time", P.gte(date))));

        VertexControllerContext context = new PromiseVertexControllerContext(null, schemaProvider, Optional.of(traversalConstraint),Collections.emptyList(), 0, Collections.emptyList());

        SearchBuilder searchBuilder = new SearchBuilder();

        PromiseEdgeIndexAppender indexAppender = new PromiseEdgeIndexAppender();
        indexAppender.append(searchBuilder, context);

        Assert.assertEquals(4, searchBuilder.getIndices().size());

        Assert.assertTrue(searchBuilder.getIndices().containsAll(Lists.newArrayList("ts_2017-03","ts_2017-04","ts_2017-05","ts_2017-06")));

    }

    @Test
    public void appendTest_ConstraintLTTime_TSIndex() throws ParseException {

        Date date = (new SimpleDateFormat("MM/dd/yyyy")).parse("03/13/2017");

        Ontology ontology = getOntology();
        GraphElementSchemaProvider schemaProvider = getOntologySchemaProvider(ontology);
        TraversalConstraint traversalConstraint = new TraversalConstraint(__.and(__.has(T.label, TIME_SERIES_INDEX_EDGE),
                __.has("time", P.lt(date))));

        VertexControllerContext context = new PromiseVertexControllerContext(null, schemaProvider, Optional.of(traversalConstraint),Collections.emptyList(), 0, Collections.emptyList());

        SearchBuilder searchBuilder = new SearchBuilder();

        PromiseEdgeIndexAppender indexAppender = new PromiseEdgeIndexAppender();
        indexAppender.append(searchBuilder, context);

        Assert.assertEquals(3, searchBuilder.getIndices().size());

        Assert.assertTrue(searchBuilder.getIndices().containsAll(Lists.newArrayList("ts_2017-01","ts_2017-02","ts_2017-03")));

    }

    @Test
    public void appendTest_ConstraintLTETime_TSIndex() throws ParseException {

        Date date = (new SimpleDateFormat("MM/dd/yyyy")).parse("03/13/2017");

        Ontology ontology = getOntology();
        GraphElementSchemaProvider schemaProvider = getOntologySchemaProvider(ontology);
        TraversalConstraint traversalConstraint = new TraversalConstraint(__.and(__.has(T.label, TIME_SERIES_INDEX_EDGE),
                __.has("time", P.lte(date))));

        VertexControllerContext context = new PromiseVertexControllerContext(null, schemaProvider, Optional.of(traversalConstraint),Collections.emptyList(), 0, Collections.emptyList());

        SearchBuilder searchBuilder = new SearchBuilder();

        PromiseEdgeIndexAppender indexAppender = new PromiseEdgeIndexAppender();
        indexAppender.append(searchBuilder, context);

        Assert.assertEquals(3, searchBuilder.getIndices().size());

        Assert.assertTrue(searchBuilder.getIndices().containsAll(Lists.newArrayList("ts_2017-01","ts_2017-02","ts_2017-03")));

    }

    @Test
    public void appendTest_ConstraintWithinTime_TSIndex() throws ParseException {

        Date date1 = (new SimpleDateFormat("MM/dd/yyyy")).parse("01/13/2017");
        Date date2 = (new SimpleDateFormat("MM/dd/yyyy")).parse("03/13/2017");
        Date date3 = (new SimpleDateFormat("MM/dd/yyyy")).parse("06/13/2017");

        Ontology ontology = getOntology();
        GraphElementSchemaProvider schemaProvider = getOntologySchemaProvider(ontology);
        TraversalConstraint traversalConstraint = new TraversalConstraint(__.and(__.has(T.label, TIME_SERIES_INDEX_EDGE),
                __.has("time", P.within(date1, date2, date3))));

        VertexControllerContext context = new PromiseVertexControllerContext(null, schemaProvider, Optional.of(traversalConstraint),Collections.emptyList(), 0, Collections.emptyList());

        SearchBuilder searchBuilder = new SearchBuilder();

        PromiseEdgeIndexAppender indexAppender = new PromiseEdgeIndexAppender();
        indexAppender.append(searchBuilder, context);

        Assert.assertEquals(3, searchBuilder.getIndices().size());

        Assert.assertTrue(searchBuilder.getIndices().containsAll(Lists.newArrayList("ts_2017-01","ts_2017-03","ts_2017-06")));

    }

    @Test
    public void appendTest_ConstraintWithoutTime_TSIndex() throws ParseException {

        Date date1 = (new SimpleDateFormat("MM/dd/yyyy")).parse("01/13/2017");
        Date date2 = (new SimpleDateFormat("MM/dd/yyyy")).parse("03/13/2017");
        Date date3 = (new SimpleDateFormat("MM/dd/yyyy")).parse("06/13/2017");

        Ontology ontology = getOntology();
        GraphElementSchemaProvider schemaProvider = getOntologySchemaProvider(ontology);
        TraversalConstraint traversalConstraint = new TraversalConstraint(__.and(__.has(T.label, TIME_SERIES_INDEX_EDGE),
                __.has("time", P.without(date1, date2, date3))));

        VertexControllerContext context = new PromiseVertexControllerContext(null, schemaProvider, Optional.of(traversalConstraint),Collections.emptyList(), 0, Collections.emptyList());

        SearchBuilder searchBuilder = new SearchBuilder();

        PromiseEdgeIndexAppender indexAppender = new PromiseEdgeIndexAppender();
        indexAppender.append(searchBuilder, context);

        Assert.assertEquals(TIME_SERIES_INDEX_NAMES.size(), searchBuilder.getIndices().size());

        Stream.ofAll(searchBuilder.getIndices()).forEach(index -> Assert.assertTrue(TIME_SERIES_INDEX_NAMES.contains(index)));

    }

    @Test
    public void appendTest_ConstraintBetweenTime_TSIndex() throws ParseException {

        Date date1 = (new SimpleDateFormat("MM/dd/yyyy")).parse("02/13/2017");
        Date date2 = (new SimpleDateFormat("MM/dd/yyyy")).parse("04/13/2017");

        Ontology ontology = getOntology();
        GraphElementSchemaProvider schemaProvider = getOntologySchemaProvider(ontology);
        TraversalConstraint traversalConstraint = new TraversalConstraint(__.and(__.has(T.label, TIME_SERIES_INDEX_EDGE),
                __.has("time", P.between(date1, date2))));

        VertexControllerContext context = new PromiseVertexControllerContext(null, schemaProvider, Optional.of(traversalConstraint),Collections.emptyList(), 0, Collections.emptyList());

        SearchBuilder searchBuilder = new SearchBuilder();

        PromiseEdgeIndexAppender indexAppender = new PromiseEdgeIndexAppender();
        indexAppender.append(searchBuilder, context);

        Assert.assertEquals(3, searchBuilder.getIndices().size());

        Assert.assertTrue(searchBuilder.getIndices().containsAll(Lists.newArrayList("ts_2017-02","ts_2017-03","ts_2017-04")));

    }

    @Test
    public void appendTest_ConstraintOutsideTime_TSIndex() throws ParseException {

        Date date1 = (new SimpleDateFormat("MM/dd/yyyy")).parse("02/13/2017");
        Date date2 = (new SimpleDateFormat("MM/dd/yyyy")).parse("04/13/2017");

        Ontology ontology = getOntology();
        GraphElementSchemaProvider schemaProvider = getOntologySchemaProvider(ontology);
        TraversalConstraint traversalConstraint = new TraversalConstraint(__.and(__.has(T.label, TIME_SERIES_INDEX_EDGE),
                __.has("time", P.outside(date1, date2))));

        VertexControllerContext context = new PromiseVertexControllerContext(null, schemaProvider, Optional.of(traversalConstraint),Collections.emptyList(), 0, Collections.emptyList());

        SearchBuilder searchBuilder = new SearchBuilder();

        PromiseEdgeIndexAppender indexAppender = new PromiseEdgeIndexAppender();
        indexAppender.append(searchBuilder, context);

        Assert.assertEquals(5, searchBuilder.getIndices().size());

        Assert.assertFalse(searchBuilder.getIndices().contains("ts_2017-03"));

    }

    @Test
    public void appendTest_ConstraintMultipleBetweenTime_TSIndex() throws ParseException {

        Date date1 = (new SimpleDateFormat("MM/dd/yyyy")).parse("02/13/2017");
        Date date2 = (new SimpleDateFormat("MM/dd/yyyy")).parse("04/13/2017");

        Date date3 = (new SimpleDateFormat("MM/dd/yyyy")).parse("01/13/2017");
        Date date4 = (new SimpleDateFormat("MM/dd/yyyy")).parse("05/13/2017");

        Date date5 = (new SimpleDateFormat("MM/dd/yyyy")).parse("03/13/2017");
        Date date6 = (new SimpleDateFormat("MM/dd/yyyy")).parse("05/13/2017");

        Ontology ontology = getOntology();
        GraphElementSchemaProvider schemaProvider = getOntologySchemaProvider(ontology);
        TraversalConstraint traversalConstraint = new TraversalConstraint(__.and(__.has(T.label, TIME_SERIES_INDEX_EDGE),
                                                                                 __.has("time",
                                                                                         P.between(date1, date2)
                                                                                                 .or(P.between(date3, date4))
                                                                                                 .or(P.between(date5, date6)))));

        VertexControllerContext context = new PromiseVertexControllerContext(null, schemaProvider, Optional.of(traversalConstraint),Collections.emptyList(), 0, Collections.emptyList());

        SearchBuilder searchBuilder = new SearchBuilder();

        PromiseEdgeIndexAppender indexAppender = new PromiseEdgeIndexAppender();
        indexAppender.append(searchBuilder, context);

        Assert.assertEquals(5, searchBuilder.getIndices().size());

        Assert.assertFalse(searchBuilder.getIndices().contains("ts_2017-06"));

    }

    @Test
    public void appendTest_ConstraintMultipleOutsideTime_TSIndex() throws ParseException {

        Date date1 = (new SimpleDateFormat("MM/dd/yyyy")).parse("02/13/2017");
        Date date2 = (new SimpleDateFormat("MM/dd/yyyy")).parse("04/13/2017");

        Date date3 = (new SimpleDateFormat("MM/dd/yyyy")).parse("03/13/2017");
        Date date4 = (new SimpleDateFormat("MM/dd/yyyy")).parse("05/13/2017");

        Date date5 = (new SimpleDateFormat("MM/dd/yyyy")).parse("05/13/2017");
        Date date6 = (new SimpleDateFormat("MM/dd/yyyy")).parse("06/13/2017");

        Ontology ontology = getOntology();
        GraphElementSchemaProvider schemaProvider = getOntologySchemaProvider(ontology);
        TraversalConstraint traversalConstraint = new TraversalConstraint(__.and(__.has(T.label, TIME_SERIES_INDEX_EDGE),
                __.has("time",
                        P.outside(date1, date2)
                                .and(P.outside(date3, date4))
                                .and(P.outside(date5, date6)))));

        VertexControllerContext context = new PromiseVertexControllerContext(null, schemaProvider, Optional.of(traversalConstraint),Collections.emptyList(), 0, Collections.emptyList());

        SearchBuilder searchBuilder = new SearchBuilder();

        PromiseEdgeIndexAppender indexAppender = new PromiseEdgeIndexAppender();
        indexAppender.append(searchBuilder, context);

        Assert.assertEquals(4, searchBuilder.getIndices().size());

        Assert.assertFalse(searchBuilder.getIndices().contains("ts_2017-03"));
        Assert.assertFalse(searchBuilder.getIndices().contains("ts_2017-04"));

    }

    @Test
    public void appendTest_ConstraintComplex1Time_TSIndex() throws ParseException {

        Date date1 = (new SimpleDateFormat("MM/dd/yyyy")).parse("02/13/2017");
        Date date2 = (new SimpleDateFormat("MM/dd/yyyy")).parse("04/13/2017");

        Date date3 = (new SimpleDateFormat("MM/dd/yyyy")).parse("01/13/2017");
        Date date4 = (new SimpleDateFormat("MM/dd/yyyy")).parse("03/13/2017");

        Date date5 = (new SimpleDateFormat("MM/dd/yyyy")).parse("05/13/2017");
        Date date6 = (new SimpleDateFormat("MM/dd/yyyy")).parse("06/13/2017");

        Ontology ontology = getOntology();
        GraphElementSchemaProvider schemaProvider = getOntologySchemaProvider(ontology);
        TraversalConstraint traversalConstraint = new TraversalConstraint(__.and(__.has(T.label, TIME_SERIES_INDEX_EDGE),
                __.has("time",
                        P.outside(date1, date2)
                                .and(P.between(date3, date4).or(P.between(date5, date6))))));

        VertexControllerContext context = new PromiseVertexControllerContext(null, schemaProvider, Optional.of(traversalConstraint),Collections.emptyList(), 0, Collections.emptyList());

        SearchBuilder searchBuilder = new SearchBuilder();

        PromiseEdgeIndexAppender indexAppender = new PromiseEdgeIndexAppender();
        indexAppender.append(searchBuilder, context);

        Assert.assertEquals(4, searchBuilder.getIndices().size());

        Assert.assertFalse(searchBuilder.getIndices().contains("ts_2017-03"));
        Assert.assertFalse(searchBuilder.getIndices().contains("ts_2017-04"));

    }

    @Test
    public void appendTest_ConstraintComplex2Time_TSIndex() throws ParseException {

        Date date1 = (new SimpleDateFormat("MM/dd/yyyy")).parse("02/13/2017");
        Date date2 = (new SimpleDateFormat("MM/dd/yyyy")).parse("04/13/2017");

        Date date3 = (new SimpleDateFormat("MM/dd/yyyy")).parse("01/13/2017");
        Date date4 = (new SimpleDateFormat("MM/dd/yyyy")).parse("02/13/2017");

        Date date5 = (new SimpleDateFormat("MM/dd/yyyy")).parse("04/13/2017");
        Date date6 = (new SimpleDateFormat("MM/dd/yyyy")).parse("06/13/2017");

        Ontology ontology = getOntology();
        GraphElementSchemaProvider schemaProvider = getOntologySchemaProvider(ontology);
        TraversalConstraint traversalConstraint = new TraversalConstraint(__.and(__.has(T.label, TIME_SERIES_INDEX_EDGE),
                __.has("time",
                        P.outside(date1, date2)
                                .or(P.between(date3, date4).or(P.between(date5, date6))))));

        VertexControllerContext context = new PromiseVertexControllerContext(null, schemaProvider, Optional.of(traversalConstraint),Collections.emptyList(), 0, Collections.emptyList());

        SearchBuilder searchBuilder = new SearchBuilder();

        PromiseEdgeIndexAppender indexAppender = new PromiseEdgeIndexAppender();
        indexAppender.append(searchBuilder, context);

        Assert.assertEquals(5, searchBuilder.getIndices().size());

        Assert.assertFalse(searchBuilder.getIndices().contains("ts_2017-03"));


    }

    @Test
    public void appendTest_ConstraintComplex3Time_TSIndex() throws ParseException {

        Date date1 = (new SimpleDateFormat("MM/dd/yyyy")).parse("02/13/2017");
        Date date2 = (new SimpleDateFormat("MM/dd/yyyy")).parse("04/13/2017");

        Date date3 = (new SimpleDateFormat("MM/dd/yyyy")).parse("01/13/2017");
        Date date4 = (new SimpleDateFormat("MM/dd/yyyy")).parse("02/13/2017");

        Date date5 = (new SimpleDateFormat("MM/dd/yyyy")).parse("05/13/2017");
        Date date6 = (new SimpleDateFormat("MM/dd/yyyy")).parse("06/13/2017");

        Date date7 = (new SimpleDateFormat("MM/dd/yyyy")).parse("04/03/2017");
        Date date8 = (new SimpleDateFormat("MM/dd/yyyy")).parse("04/13/2017");

        Date date9 = (new SimpleDateFormat("MM/dd/yyyy")).parse("04/22/2017");
        Date date10 = (new SimpleDateFormat("MM/dd/yyyy")).parse("04/30/2017");

        Ontology ontology = getOntology();
        GraphElementSchemaProvider schemaProvider = getOntologySchemaProvider(ontology);
        TraversalConstraint traversalConstraint = new TraversalConstraint(__.and(__.has(T.label, TIME_SERIES_INDEX_EDGE),
                __.has("time",
                        P.outside(date1, date2)
                                .and(P.outside(date3, date4))
                                .and(P.outside(date5, date6))
                                .and(P.between(date7, date8)
                                        .or(P.between(date9, date10))))));

        VertexControllerContext context = new PromiseVertexControllerContext(null, schemaProvider, Optional.of(traversalConstraint),Collections.emptyList(), 0, Collections.emptyList());

        SearchBuilder searchBuilder = new SearchBuilder();

        PromiseEdgeIndexAppender indexAppender = new PromiseEdgeIndexAppender();
        indexAppender.append(searchBuilder, context);

        Assert.assertEquals(1, searchBuilder.getIndices().size());

        Assert.assertTrue(searchBuilder.getIndices().contains("ts_2017-04"));


    }

    //region Private Methods
    private OntologySchemaProvider getOntologySchemaProvider(Ontology ontology) {
        return new OntologySchemaProvider(ontology, (label, elementType) -> {

            if (elementType == ElementType.edge) {
                switch(label){

                    case STATIC_INDEX_EDGE:
                        return new StaticIndexPartition(STATIC_INDEX_NAMES);

                    case TIME_SERIES_INDEX_EDGE:
                        return new TimeSeriesIndexPartition() {
                            @Override
                            public String getDateFormat() {
                                return "YYYY-MM";
                            }

                            @Override
                            public String getIndexPrefix() {
                                return "ts";
                            }

                            @Override
                            public String getIndexFormat() {
                                return getIndexPrefix() + "_%s";
                            }

                            @Override
                            public String getTimeField() {
                                return "time";
                            }

                            @Override
                            public String getIndexName(Date date) {
                                return null;
                            }

                            @Override
                            public Iterable<String> getIndices() {
                                return TIME_SERIES_INDEX_NAMES;
                            }
                        };

                   default:
                        return null;
                }
            } else {
                // must fail
                return null;
            }
        });
    }

    private Ontology getOntology() {

        Ontology ontology = mock(Ontology.class);

        List<EPair> ePairs = Arrays.asList(new EPair() {{
            seteTypeA("Dragon");
            seteTypeB("Dragon");
        }});

        RelationshipType edgeTypeNoTS = RelationshipType.Builder.get()
                                                        .withRType("Fire").withName(STATIC_INDEX_EDGE).withEPairs(ePairs).build();

        RelationshipType edgeTypeOnlyTS = RelationshipType.Builder.get()
                                                        .withRType("Fire").withName(TIME_SERIES_INDEX_EDGE).withEPairs(ePairs).build();

        when(ontology.getEntityTypes()).thenAnswer(invocationOnMock ->
                {
                    ArrayList<EntityType> entityTypes = new ArrayList<>();
                    entityTypes.add(EntityType.Builder.get()
                            .withEType("Dragon").withName("Dragon").build());
                    return entityTypes;
                }
        );

        when(ontology.getRelationshipTypes()).thenAnswer(invocationOnMock ->
                {
                    ArrayList<RelationshipType> relTypes = new ArrayList<>();
                    relTypes.add(edgeTypeNoTS);
                    relTypes.add(edgeTypeOnlyTS);
                    return relTypes;
                }
        );

        return ontology;
    }
    //endregion
}
