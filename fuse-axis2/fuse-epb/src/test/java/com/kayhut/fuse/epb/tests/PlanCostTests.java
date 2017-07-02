package com.kayhut.fuse.epb.tests;

import static org.mockito.Matchers.any;

/**
 * Created by moti on 4/19/2017.
 */
public class PlanCostTests {
    /*private RegexPatternCostEstimator<DoubleCost> planOpStatisticsCostEstimator;

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
            public Iterable<IndexPartition> getIndexPartition() {
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

        //estimation calculator
        CostEstimator costEstimator = Mockito.mock(CostEstimator.class);
        when(costEstimator.estimateCost(any(), any())).thenReturn(new DoubleCost(1.0,1,1));
        when(costEstimator.estimateCost(any(PlanOpBase.class))).thenReturn(new DoubleCost(1.0,1,1));
        OpCostCalculator<DoubleCost, DoubleCost, Plan<DoubleCost>> costCombiner = Mockito.mock(OpCostCalculator.class);
        when(costCombiner.calculateCost(any(), any())).thenReturn(new DoubleCost(1.0,1,1));

        //tested class estimation estimator
        planOpStatisticsCostEstimator = new RegexPatternCostEstimator(statisticsProvider,costEstimator);
    }

    @Test
    public void planCostEConcreteTest(){
        AsgEBase<EEntityBase> asgEBase = AsgEBase.Builder.<EEntityBase>get().withEBase(new EConcrete()).search();
        Assert.assertEquals(1.0, planOpStatisticsCostEstimator.estimateCost( new EntityOp(asgEBase)).estimation, 0.0);
    }

    @Test
    public void planCostEUntypedTest(){
        EUntyped eBase = new EUntyped();
        eBase.setvTypes(Collections.singletonList(1));
        AsgEBase<EEntityBase> asgEBase = AsgEBase.Builder.<EEntityBase>get().withEBase(eBase).search();
        Assert.assertEquals(1.0, planOpStatisticsCostEstimator.estimateCost( new EntityOp(asgEBase)).estimation, 0.0);
    }

    @Test
    public void planCostETypedTest(){
        ETyped eTyped = new ETyped();
        eTyped.seteType(1);
        AsgEBase<EEntityBase> asgEBase = AsgEBase.Builder.<EEntityBase>get().withEBase(eTyped).search();
        Assert.assertEquals(1.0, planOpStatisticsCostEstimator.estimateCost( new EntityOp(asgEBase)).estimation, 0.0);
    }*/
}
