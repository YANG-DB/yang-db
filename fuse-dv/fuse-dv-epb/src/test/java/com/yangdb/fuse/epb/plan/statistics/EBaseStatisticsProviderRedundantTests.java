package com.yangdb.fuse.epb.plan.statistics;

import com.yangdb.fuse.model.OntologyTestUtils;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.properties.RedundantRelProp;
import com.yangdb.fuse.model.query.properties.RelProp;
import com.yangdb.fuse.model.query.properties.RelPropGroup;
import com.yangdb.fuse.unipop.schemaProviders.*;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by moti on 5/9/2017.
 */
public class EBaseStatisticsProviderRedundantTests {
    private EBaseStatisticsProvider statisticsProvider;
    private Ontology ontology;
    private GraphElementSchemaProvider graphElementSchemaProvider;
    private GraphStatisticsProvider graphStatisticsProvider;


    @Before
    public void setup(){
        ontology = OntologyTestUtils.createDragonsOntologyShort();
        graphElementSchemaProvider = mock(GraphElementSchemaProvider.class);
        when(graphElementSchemaProvider.getVertexLabels()).thenReturn(Arrays.asList("Guild"));
        GraphEdgeSchema ownSchema = mock(GraphEdgeSchema.class);
        when(ownSchema.getIndexPartitions()).thenReturn(Optional.of(new StaticIndexPartitions(Collections.emptyList())));
        when(ownSchema.getEndB()).thenReturn(Optional.of(
                new GraphEdgeSchema.End.Impl(
                        null,
                        null,
                        Collections.singletonList(
                                new GraphRedundantPropertySchema.Impl("firstName", "EntityB.firstName", "string")
                        ))));

        GraphVertexSchema graphVertexSchema = new GraphVertexSchema.Impl(
                "Guild",
                new GraphElementConstraint.Impl(__.has(T.label, "Guild")),
                Optional.empty(),
                Optional.of(new StaticIndexPartitions(Collections.emptyList())),
                Arrays.asList(
                        new GraphElementPropertySchema.Impl("firstName", "string"),
                        new GraphElementPropertySchema.Impl("lastName", "string")));

        when(graphElementSchemaProvider.getEdgeSchemas(any())).thenReturn(Collections.singletonList(ownSchema));
        when(graphElementSchemaProvider.getVertexSchemas(any())).thenReturn(Collections.singletonList(graphVertexSchema));

        graphStatisticsProvider = mock(GraphStatisticsProvider.class);
        when(graphStatisticsProvider.getEdgeCardinality(any(),any())).thenReturn(new Statistics.SummaryStatistics(1000,1000));
        when(graphStatisticsProvider.getConditionHistogram(isA(GraphEdgeSchema.class), any(), any(), any(), eq(String.class))).thenReturn(new Statistics.HistogramStatistics<>(Arrays.asList(new Statistics.BucketInfo<String>(100L, 100L,"a","z"))));
        when(graphStatisticsProvider.getConditionHistogram(isA(GraphVertexSchema.class), any(), any(), any(), eq(String.class))).thenAnswer(invocationOnMock -> {
            GraphElementPropertySchema graphElementPropertySchema = invocationOnMock.getArgument(2, GraphElementPropertySchema.class);
            if(graphElementPropertySchema.getName().equals("firstName"))
                return new Statistics.HistogramStatistics<>(Arrays.asList(new Statistics.BucketInfo<String>(100L, 100L,"a","z")));
            return new Statistics.HistogramStatistics<>(Arrays.asList(new Statistics.BucketInfo<String>(200L, 200L,"a","z")));
        });
        when(graphStatisticsProvider.getVertexCardinality(any(), any())).thenReturn(new Statistics.SummaryStatistics(500,50));
        statisticsProvider = new EBaseStatisticsProvider(graphElementSchemaProvider, new Ontology.Accessor(ontology), graphStatisticsProvider);
    }
    
    @Test
    public void redundantNodePropTest(){
        Rel rel = new Rel();
        rel.setrType("Dragon");
        RelProp prop = new RelProp();
        prop.setpType("color");
        Constraint constraint = new Constraint();
        constraint.setExpr(new Date());
        constraint.setOp(ConstraintOp.eq);
        prop.setCon(constraint);
        RedundantRelProp redundantRelProp = RedundantRelProp.of(0, "EntityB.firstName", "lastName", Constraint.of(ConstraintOp.ge, "abc"));

        RelPropGroup relFilter = new RelPropGroup(Arrays.asList(prop, redundantRelProp));

        ETyped eTyped = new ETyped();
        eTyped.seteType("Guild");


        Statistics.SummaryStatistics redundantEdgeStatistics = statisticsProvider.getRedundantNodeStatistics(eTyped, relFilter);
        Assert.assertNotNull(redundantEdgeStatistics);
        Assert.assertEquals(200L, redundantEdgeStatistics.getTotal(), 0.1);
    }

}
