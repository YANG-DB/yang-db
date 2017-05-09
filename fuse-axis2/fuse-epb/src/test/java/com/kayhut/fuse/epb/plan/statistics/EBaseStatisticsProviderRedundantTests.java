package com.kayhut.fuse.epb.plan.statistics;

import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.execution.plan.Direction;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.ConstraintOp;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import com.kayhut.fuse.unipop.schemaProviders.*;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.mockito.Matchers.any;
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
        when(graphElementSchemaProvider.getVertexTypes()).thenReturn(Arrays.asList("Guild"));
        GraphEdgeSchema ownSchema = mock(GraphEdgeSchema.class);
        when(ownSchema.getIndexPartition()).thenReturn(() -> new ArrayList<>());
        when(ownSchema.getDestination()).thenReturn(Optional.of(new GraphEdgeSchema.End(){

            @Override
            public String getIdField() {
                return null;
            }

            @Override
            public Optional<String> getType() {
                return null;
            }

            @Override
            public Optional<GraphRedundantPropertySchema> getRedundantVertexProperty(String property) {
                if(property.equals("firstName")){
                    return Optional.of(new GraphRedundantPropertySchema() {
                        @Override
                        public String getPropertyRedundantName() {
                            return "EntityB.firstName";
                        }

                        @Override
                        public String getName() {
                            return "firstName";
                        }

                        @Override
                        public String getType() {
                            return "string";
                        }
                    });
                }else{
                    return Optional.empty();
                }
            }
        }));

        GraphVertexSchema graphVertexSchema = new GraphVertexSchema() {
            @Override
            public String getType() {
                return "Guild";
            }

            @Override
            public Optional<GraphElementRouting> getRouting() {
                return null;
            }

            @Override
            public IndexPartition getIndexPartition() {
                return new IndexPartition() {
                    @Override
                    public Iterable<String> getIndices() {
                        return Arrays.asList();
                    }
                };
            }

            @Override
            public Iterable<GraphElementPropertySchema> getProperties() {
                return Arrays.asList(new GraphElementPropertySchema() {
                    @Override
                    public String getName() {
                        return "firstName";
                    }

                    @Override
                    public String getType() {
                        return "string";
                    }
                },new GraphElementPropertySchema() {
                    @Override
                    public String getName() {
                        return "lastName";
                    }

                    @Override
                    public String getType() {
                        return "string";
                    }
                } );
            }

            @Override
            public Optional<GraphElementPropertySchema> getProperty(String name) {
                return Optional.of(new GraphElementPropertySchema() {
                    @Override
                    public String getName() {
                        return name;
                    }

                    @Override
                    public String getType() {
                        return "string";
                    }
                });
            }
        };
        when(graphElementSchemaProvider.getEdgeSchema(any())).thenReturn(Optional.of(ownSchema));
        when(graphElementSchemaProvider.getVertexSchema(any())).thenReturn(Optional.of(graphVertexSchema));

        graphStatisticsProvider = mock(GraphStatisticsProvider.class);
        when(graphStatisticsProvider.getEdgeCardinality(any(),any())).thenReturn(new Statistics.Cardinality(1000,1000));
        when(graphStatisticsProvider.getConditionHistogram(isA(GraphEdgeSchema.class), any(), any(), any(), isA(String.class))).thenReturn(new Statistics.HistogramStatistics<>(Arrays.asList(new Statistics.BucketInfo<String>(100l,100l,"a","z"))));
        when(graphStatisticsProvider.getConditionHistogram(isA(GraphVertexSchema.class), any(), any(), any(), isA(String.class))).thenAnswer(invocationOnMock -> {
            GraphElementPropertySchema graphElementPropertySchema = invocationOnMock.getArgumentAt(2, GraphElementPropertySchema.class);
            if(graphElementPropertySchema.getName().equals("firstName"))
                return new Statistics.HistogramStatistics<>(Arrays.asList(new Statistics.BucketInfo<String>(100l,100l,"a","z")));
            return new Statistics.HistogramStatistics<>(Arrays.asList(new Statistics.BucketInfo<String>(200l,200l,"a","z")));
        });
        when(graphStatisticsProvider.getVertexCardinality(any(), any())).thenReturn(new Statistics.Cardinality(500,50));
        statisticsProvider = new EBaseStatisticsProvider(graphElementSchemaProvider, ontology, graphStatisticsProvider);
    }


    @Test
    public void nonRedundantEdgePropTest(){
        Rel rel = new Rel();
        rel.setrType(2);
        RelPropGroup relFilter = new RelPropGroup();

        RelProp prop = new RelProp();
        prop.setpType("8");
        Constraint constraint = new Constraint();
        constraint.setExpr(new Date());
        constraint.setOp(ConstraintOp.eq);
        prop.setCon(constraint);
        relFilter.setrProps(Collections.singletonList(prop));

        ETyped eTyped = new ETyped();
        eTyped.seteType(4);
        EPropGroup propGroup = new EPropGroup();
        ArrayList<EProp> props = new ArrayList<>();
        EProp eProp = new EProp();
        eProp.setpType("2");
        Constraint con = new Constraint();
        con.setOp(ConstraintOp.eq);
        con.setExpr("abc");
        prop.setCon(con);
        props.add(eProp);
        propGroup.seteProps(props);
        Statistics.Cardinality redundantEdgeStatistics = statisticsProvider.getRedundantEdgeStatistics(rel, relFilter, eTyped, propGroup, Direction.out);
        Assert.assertNotNull(redundantEdgeStatistics);
        Assert.assertEquals(1000, redundantEdgeStatistics.getTotal(), 0.1);
    }

    @Test
    public void redundantEdgePropTest(){
        Rel rel = new Rel();
        rel.setrType(2);
        RelPropGroup relFilter = new RelPropGroup();

        RelProp prop = new RelProp();
        prop.setpType("8");
        Constraint constraint = new Constraint();
        constraint.setExpr(new Date());
        constraint.setOp(ConstraintOp.eq);
        prop.setCon(constraint);
        relFilter.setrProps(Collections.singletonList(prop));

        ETyped eTyped = new ETyped();
        eTyped.seteType(4);
        EPropGroup propGroup = new EPropGroup();
        ArrayList<EProp> props = new ArrayList<>();
        EProp eProp = new EProp();
        eProp.setpType("1");
        Constraint con = new Constraint();
        con.setOp(ConstraintOp.ge);
        con.setExpr("abc");
        eProp.setCon(con);
        props.add(eProp);
        propGroup.seteProps(props);
        Statistics.Cardinality redundantEdgeStatistics = statisticsProvider.getRedundantEdgeStatistics(rel, relFilter, eTyped, propGroup, Direction.out);
        Assert.assertNotNull(redundantEdgeStatistics);
        Assert.assertEquals(100l, redundantEdgeStatistics.getTotal(), 0.1);
    }


    @Test
    public void redundantNodePropTest(){
        Rel rel = new Rel();
        rel.setrType(2);
        RelPropGroup relFilter = new RelPropGroup();
        RelProp prop = new RelProp();
        prop.setpType("8");
        Constraint constraint = new Constraint();
        constraint.setExpr(new Date());
        constraint.setOp(ConstraintOp.eq);
        prop.setCon(constraint);
        relFilter.setrProps(Collections.singletonList(prop));

        ETyped eTyped = new ETyped();
        eTyped.seteType(4);
        EPropGroup propGroup = new EPropGroup();
        ArrayList<EProp> props = new ArrayList<>();
        EProp eProp = new EProp();
        eProp.setpType("1");
        Constraint con = new Constraint();
        con.setOp(ConstraintOp.ge);
        con.setExpr("abc");
        eProp.setCon(con);
        props.add(eProp);

        eProp = new EProp();
        eProp.setpType("2");
        con = new Constraint();
        con.setOp(ConstraintOp.ge);
        con.setExpr("abc");
        eProp.setCon(con);
        props.add(eProp);
        propGroup.seteProps(props);
        Statistics.Cardinality redundantEdgeStatistics = statisticsProvider.getRedundantNodeStatistics(rel, eTyped, propGroup, Direction.out);
        Assert.assertNotNull(redundantEdgeStatistics);
        Assert.assertEquals(100l, redundantEdgeStatistics.getTotal(), 0.1);
    }

}
