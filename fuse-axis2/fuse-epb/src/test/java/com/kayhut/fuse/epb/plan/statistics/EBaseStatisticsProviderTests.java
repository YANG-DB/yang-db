package com.kayhut.fuse.epb.plan.statistics;

import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphVertexSchema;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by liorp on 4/27/2017.
 */
public class EBaseStatisticsProviderTests {

    GraphElementSchemaProvider graphElementSchemaProvider;
    GraphVertexSchema graphVertexSchema;
    GraphStatisticsProvider graphStatisticsProvider;
    Ontology ontology;

    EBaseStatisticsProvider statisticsProvider;

    @Before
    public void setUp() throws Exception {
        graphElementSchemaProvider = Mockito.mock(GraphElementSchemaProvider.class);
        graphVertexSchema = Mockito.mock(GraphVertexSchema.class);

        when(graphElementSchemaProvider.getVertexTypes()).thenReturn(Collections.singletonList("1"));
        when(graphElementSchemaProvider.getVertexSchema(any())).thenReturn(Optional.of(graphVertexSchema));

        graphStatisticsProvider = Mockito.mock(GraphStatisticsProvider.class);
        when(graphStatisticsProvider.getVertexCardinality(any())).thenReturn(new Statistics.Cardinality(1l, 1l));
        ontology = OntologyTestUtils.createDragonsOntologyShort(new Ontology());
        statisticsProvider = new EBaseStatisticsProvider(graphElementSchemaProvider, ontology, graphStatisticsProvider);
    }


    @Test
    public void eConcreteHistogramTest() {
        EConcrete eConcrete = new EConcrete();
        Statistics.Cardinality nodeStatistics = statisticsProvider.getNodeStatistics(eConcrete);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(1d, nodeStatistics.getTotal(), 0);
    }

    @Test
    public void eTypedHistogramTest() {
        ETyped eTyped = new ETyped();
        eTyped.seteType(1);
        Statistics.Cardinality nodeStatistics = statisticsProvider.getNodeStatistics(eTyped);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(1d, nodeStatistics.getTotal(), 0);
    }

    @Test
    public void eUnTypedHistogramTest() {
        EUntyped eUntyped = new EUntyped();
        eUntyped.setvTypes(Arrays.asList(1,2));
        eUntyped.setNvTypes(Arrays.asList(3,4));
        Statistics.Cardinality nodeStatistics = statisticsProvider.getNodeStatistics(eUntyped);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(2d, nodeStatistics.getTotal(), 0);
    }


}
