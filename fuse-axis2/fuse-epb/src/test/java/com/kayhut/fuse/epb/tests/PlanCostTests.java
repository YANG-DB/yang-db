package com.kayhut.fuse.epb.tests;

import com.kayhut.fuse.epb.plan.cost.PlanOpStatisticsCostEstimator;
import com.kayhut.fuse.epb.plan.cost.calculation.CostCalculator;
import com.kayhut.fuse.epb.plan.statistics.QueryItemStatisticsProvider;
import com.kayhut.fuse.epb.plan.statistics.RawGraphStatisticsProvider;
import com.kayhut.fuse.epb.plan.statistics.Statistics;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.costs.SingleCost;
import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementRouting;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphVertexSchema;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by moti on 4/19/2017.
 */
public class PlanCostTests {
    private PlanOpStatisticsCostEstimator<SingleCost> planOpStatisticsCostEstimator;

    @Before
    public void setup() {
        RawGraphStatisticsProvider rawGraphStatisticsProvider = new RawGraphStatisticsProvider();
        GraphElementSchemaProvider graphElementSchemaProvider = Mockito.mock(GraphElementSchemaProvider.class);
        when(graphElementSchemaProvider.getVertexSchema(any())).thenReturn(Optional.of(new GraphVertexSchema() {
            @Override
            public String getType() {
                return null;
            }

            @Override
            public Optional<GraphElementRouting> getRouting() {
                return null;
            }

            @Override
            public Iterable<IndexPartition> getIndexPartitions() {
                return null;
            }
        }));

        Ontology ontology = Mockito.mock(Ontology.class);
        EntityType entityType = new EntityType();
        entityType.seteType(1);
        when(ontology.getEntityTypes()).thenReturn(Collections.singletonList(entityType));
        QueryItemStatisticsProvider statisticsProvider = new QueryItemStatisticsProvider(rawGraphStatisticsProvider, graphElementSchemaProvider,ontology);
        CostCalculator<SingleCost, Statistics, PlanOpBase> planOpCostCalculator = Mockito.mock(CostCalculator.class);
        when(planOpCostCalculator.calculateCost(any(), any())).thenReturn(new SingleCost(1.0));
        CostCalculator<SingleCost, SingleCost, Plan<SingleCost>> costCombiner = Mockito.mock(CostCalculator.class);
        when(costCombiner.calculateCost(any(), any())).thenReturn(new SingleCost(1.0));
        planOpStatisticsCostEstimator = new PlanOpStatisticsCostEstimator<>(statisticsProvider,
                planOpCostCalculator,
                costCombiner);
    }

    @Test
    public void planCostEConcreteTest(){
        AsgEBase<EEntityBase> asgEBase = AsgEBase.EBaseAsgBuilder.<EEntityBase>anEBaseAsg().withEBase(new EConcrete()).build();
        Assert.assertEquals(1.0, planOpStatisticsCostEstimator.estimateCost(Optional.empty(), new EntityOp(asgEBase)).getCost(), 0.0);
    }

    @Test
    public void planCostEUntypedTest(){
        AsgEBase<EEntityBase> asgEBase = AsgEBase.EBaseAsgBuilder.<EEntityBase>anEBaseAsg().withEBase(new EUntyped()).build();
        Assert.assertEquals(1.0, planOpStatisticsCostEstimator.estimateCost(Optional.empty(), new EntityOp(asgEBase)).getCost(), 0.0);
    }

    @Test
    public void planCostETypedTest(){
        ETyped eTyped = new ETyped();
        eTyped.seteType(1);
        AsgEBase<EEntityBase> asgEBase = AsgEBase.EBaseAsgBuilder.<EEntityBase>anEBaseAsg().withEBase(eTyped).build();
        Assert.assertEquals(1.0, planOpStatisticsCostEstimator.estimateCost(Optional.empty(), new EntityOp(asgEBase)).getCost(), 0.0);
    }
}
