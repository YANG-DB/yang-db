package com.yangdb.fuse.executor.ontology.schema;

import com.yangdb.fuse.dispatcher.driver.IdGeneratorDriver;
import com.yangdb.fuse.executor.ontology.schema.load.DefaultGraphInitiator;
import com.yangdb.fuse.executor.ontology.schema.load.GraphInitiator;
import com.yangdb.fuse.model.Range;
import com.yangdb.test.BaseITMarker;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.yangdb.fuse.executor.TestSuiteIndexProviderSuite.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class GraphInitiatorIT implements BaseITMarker {


    @Test
    public void testSchema() throws IOException {
        Set<String> strings = Arrays.asList("idx_fire_500","idx_freeze_2000","idx_fire_1500","idx_freeze_1000","guilds","own","subjectof","idx_freeze_1500","idx_fire_2000","people","idx_fire_1000","idx_freeze_500","kingdoms","know","originatedin","registeredin","memberof","horses","dragons").stream().collect(Collectors.toSet());
        Assert.assertEquals(strings,StreamSupport.stream(nestedSchema.indices().spliterator(),false).collect(Collectors.toSet()));
    }

    @Test
    public void testNestedInit() throws IOException {
        IdGeneratorDriver<Range> idGeneratorDriver = Mockito.mock(IdGeneratorDriver.class);
        when(idGeneratorDriver.getNext(anyString(),anyInt()))
                .thenAnswer(invocationOnMock -> new Range(0,1000));
        GraphInitiator initiator = new DefaultGraphInitiator(config,client,nestedProviderIfc,ontologyProvider,nestedSchema);
        Assert.assertEquals(19,initiator.init());
    }

    @Test
    public void testNestedDrop() throws IOException {
        IdGeneratorDriver<Range> idGeneratorDriver = Mockito.mock(IdGeneratorDriver.class);
        when(idGeneratorDriver.getNext(anyString(),anyInt()))
                .thenAnswer(invocationOnMock -> new Range(0,1000));
        GraphInitiator initiator = new DefaultGraphInitiator(config,client,nestedProviderIfc,ontologyProvider,nestedSchema);
        Assert.assertEquals(19,initiator.drop());
    }


}
