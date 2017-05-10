package com.kayhut.fuse.epb.tests;

import static org.mockito.Matchers.any;

/**
 * Created by moti on 4/19/2017.
 */
public class PlanCostTests {
    /*private StatisticsCostEstimator<Cost> planOpStatisticsCostEstimator;

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
        AsgEBase<EEntityBase> asgEBase = AsgEBase.Builder.<EEntityBase>get().withEBase(new EConcrete()).search();
        Assert.assertEquals(1.0, planOpStatisticsCostEstimator.estimateCost( new EntityOp(asgEBase)).cost, 0.0);
    }

    @Test
    public void planCostEUntypedTest(){
        EUntyped eBase = new EUntyped();
        eBase.setvTypes(Collections.singletonList(1));
        AsgEBase<EEntityBase> asgEBase = AsgEBase.Builder.<EEntityBase>get().withEBase(eBase).search();
        Assert.assertEquals(1.0, planOpStatisticsCostEstimator.estimateCost( new EntityOp(asgEBase)).cost, 0.0);
    }

    @Test
    public void planCostETypedTest(){
        ETyped eTyped = new ETyped();
        eTyped.seteType(1);
        AsgEBase<EEntityBase> asgEBase = AsgEBase.Builder.<EEntityBase>get().withEBase(eTyped).search();
        Assert.assertEquals(1.0, planOpStatisticsCostEstimator.estimateCost( new EntityOp(asgEBase)).cost, 0.0);
    }*/
}
