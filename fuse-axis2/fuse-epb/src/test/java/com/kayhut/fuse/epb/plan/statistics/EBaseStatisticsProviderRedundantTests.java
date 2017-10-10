package com.kayhut.fuse.epb.plan.statistics;

import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.ConstraintOp;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.RedundantRelProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import com.kayhut.fuse.unipop.schemaProviders.*;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
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
        when(ownSchema.getDestination()).thenReturn(Optional.of(
                new GraphEdgeSchema.End.Impl(
                        null,
                        null,
                        Collections.singletonList(
                                new GraphRedundantPropertySchema.Impl("firstName", "EntityB.firstName", "string")
                        ))));

        GraphVertexSchema graphVertexSchema = new GraphVertexSchema.Impl(
                "Guild",
                "Guild",
                Optional.empty(),
                Optional.of(new StaticIndexPartitions(Collections.emptyList())),
                Arrays.asList(
                        new GraphElementPropertySchema.Impl("firstName", "string"),
                        new GraphElementPropertySchema.Impl("lastName", "string")));

        when(graphElementSchemaProvider.getEdgeSchema(any())).thenReturn(Optional.of(ownSchema));
        when(graphElementSchemaProvider.getVertexSchema(any())).thenReturn(Optional.of(graphVertexSchema));

        graphStatisticsProvider = mock(GraphStatisticsProvider.class);
        when(graphStatisticsProvider.getEdgeCardinality(any(),any())).thenReturn(new Statistics.SummaryStatistics(1000,1000));
        when(graphStatisticsProvider.getConditionHistogram(isA(GraphEdgeSchema.class), any(), any(), any(), eq(String.class))).thenReturn(new Statistics.HistogramStatistics<>(Arrays.asList(new Statistics.BucketInfo<String>(100L, 100L,"a","z"))));
        when(graphStatisticsProvider.getConditionHistogram(isA(GraphVertexSchema.class), any(), any(), any(), eq(String.class))).thenAnswer(invocationOnMock -> {
            GraphElementPropertySchema graphElementPropertySchema = invocationOnMock.getArgumentAt(2, GraphElementPropertySchema.class);
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
