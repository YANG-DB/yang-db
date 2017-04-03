package com.kayhut.fuse.unipop;

import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import com.kayhut.fuse.unipop.controller.UniGraphProvider;
import com.kayhut.fuse.unipop.converter.CompositeConverter;
import com.kayhut.fuse.unipop.schemaProviders.EmptyGraphElementSchemaProvider;
import org.elasticsearch.client.Client;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;

/**
 * Created by User on 19/03/2017.
 */
public class UniGraphProviderTest {
    Client client;
    ElasticGraphConfiguration configuration;

    @Before
    public void setUp() throws Exception {
        client = mock(Client.class);
        configuration = mock(ElasticGraphConfiguration.class);

    }

    @Test
    public void unigraphProvider() throws Exception {
        UniGraphProvider provider = new UniGraphProvider(client,configuration,new EmptyGraphElementSchemaProvider(),new CompositeConverter());
        //region ControllerManagerFactory Implementation
        Assert.assertNotNull(provider);
        Assert.assertNotNull(provider.getGraph());
        Assert.assertNotNull(provider.getGraph().getControllerManager());
        Assert.assertEquals(provider.getGraph().getControllerManager().getControllers().size(),2);
    }
}
