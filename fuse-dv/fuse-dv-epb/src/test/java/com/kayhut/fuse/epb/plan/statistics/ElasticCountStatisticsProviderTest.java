package com.kayhut.fuse.epb.plan.statistics;

import com.google.inject.Provider;
import com.kayhut.fuse.dispatcher.gta.PlanTraversalTranslator;
import com.kayhut.fuse.epb.plan.statistics.configuration.ElasticCountStatisticsConfig;
import com.kayhut.fuse.executor.ontology.UniGraphProvider;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.unipop.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.unipop.structure.UniGraph;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class ElasticCountStatisticsProviderTest {

    @Test
    public void testNodeStatistics() throws Exception {
        PlanTraversalTranslator planTraversalTranslator = Mockito.mock(PlanTraversalTranslator.class);
        Ontology ontology = Mockito.mock(Ontology.class);
        Provider<UniGraphProvider> uniGraphProvider = Mockito.mock(Provider.class);
        UniGraphProvider uniGraphProvider1 = Mockito.mock(UniGraphProvider.class);
        UniGraph uniGraph = Mockito.mock(UniGraph.class);
        GraphTraversalSource source = Mockito.mock(GraphTraversalSource.class);
        when(uniGraphProvider.get()).thenReturn(uniGraphProvider1);
        when(uniGraphProvider1.getGraph(any())).thenReturn(uniGraph);
        when(uniGraph.traversal()).thenReturn(source);
        GraphTraversal traversal = Mockito.mock(GraphTraversal.class);
        when(planTraversalTranslator.translate(any(), any())).thenReturn(traversal);
        when(traversal.count()).thenReturn(traversal);
        when(traversal.next()).thenReturn(1L);
        ElasticCountStatisticsConfig config = Mockito.mock(ElasticCountStatisticsConfig.class);

        ElasticCountStatisticsProvider provider = new ElasticCountStatisticsProvider(planTraversalTranslator,
                ontology,
                uniGraphProvider,
                config
                );


        Statistics.SummaryStatistics nodeStatistics = provider.getNodeStatistics(new EConcrete());

        Assert.assertEquals(1, nodeStatistics.getCardinality(), 0.0001);
    }

}
