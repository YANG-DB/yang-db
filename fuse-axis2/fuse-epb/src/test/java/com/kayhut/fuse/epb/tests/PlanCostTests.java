package com.kayhut.fuse.epb.tests;

import com.kayhut.fuse.epb.plan.cost.StatisticsCostEstimator;
import com.kayhut.fuse.epb.plan.cost.calculation.OpCostCalculator;
import com.kayhut.fuse.epb.plan.statistics.QueryItemStatisticsProvider;
import com.kayhut.fuse.epb.plan.statistics.provider.ElasticSearchStatisticsProvider;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.costs.Cost;
import com.kayhut.fuse.model.execution.plan.costs.CostEstimator;
import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementRouting;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphVertexSchema;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartition;
import org.elasticsearch.client.Client;
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
    private StatisticsCostEstimator<Cost> planOpStatisticsCostEstimator;

    @Before
    public void setup() {
        //elastic statistics provider
        Client client = Mockito.mock(Client.class);
        ElasticGraphConfiguration configuration = Mockito.mock(ElasticGraphConfiguration.class);
        ElasticSearchStatisticsProvider elasticSearchStatisticsProvider = new ElasticSearchStatisticsProvider(configuration, client);

        //graph schema provider
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

        //ontology mock
        Ontology ontology = Mockito.mock(Ontology.class);
        EntityType entityType = new EntityType();
        entityType.seteType(1);
        entityType.setName("name");

        when(ontology.getEntityTypes()).thenReturn(Collections.singletonList(entityType));
        QueryItemStatisticsProvider statisticsProvider = new QueryItemStatisticsProvider(elasticSearchStatisticsProvider, graphElementSchemaProvider,ontology);

        //cost calculator
        CostEstimator costEstimator = Mockito.mock(CostEstimator.class);
        when(costEstimator.estimateCost(any(), any())).thenReturn(new Cost(1.0,1,1));
        when(costEstimator.estimateCost(any(PlanOpBase.class))).thenReturn(new Cost(1.0,1,1));
        OpCostCalculator<Cost, Cost, Plan<Cost>> costCombiner = Mockito.mock(OpCostCalculator.class);
        when(costCombiner.calculateCost(any(), any())).thenReturn(new Cost(1.0,1,1));

        //tested class cost estimator
        planOpStatisticsCostEstimator = new StatisticsCostEstimator(statisticsProvider,costEstimator);
    }

    @Test
    public void planCostEConcreteTest(){
        AsgEBase<EEntityBase> asgEBase = AsgEBase.EBaseAsgBuilder.<EEntityBase>anEBaseAsg().withEBase(new EConcrete()).build();
        Assert.assertEquals(1.0, planOpStatisticsCostEstimator.estimateCost( new EntityOp(asgEBase)).cost, 0.0);
    }

    @Test
    public void planCostEUntypedTest(){
        EUntyped eBase = new EUntyped();
        eBase.setvTypes(Collections.singletonList(1));
        AsgEBase<EEntityBase> asgEBase = AsgEBase.EBaseAsgBuilder.<EEntityBase>anEBaseAsg().withEBase(eBase).build();
        Assert.assertEquals(1.0, planOpStatisticsCostEstimator.estimateCost( new EntityOp(asgEBase)).cost, 0.0);
    }

    @Test
    public void planCostETypedTest(){
        ETyped eTyped = new ETyped();
        eTyped.seteType(1);
        AsgEBase<EEntityBase> asgEBase = AsgEBase.EBaseAsgBuilder.<EEntityBase>anEBaseAsg().withEBase(eTyped).build();
        Assert.assertEquals(1.0, planOpStatisticsCostEstimator.estimateCost( new EntityOp(asgEBase)).cost, 0.0);
    }
}
