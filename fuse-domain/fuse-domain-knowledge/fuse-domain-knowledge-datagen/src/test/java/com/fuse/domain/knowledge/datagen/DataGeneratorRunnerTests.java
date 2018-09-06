package com.fuse.domain.knowledge.datagen;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Ignore;
import org.junit.Test;

import java.net.UnknownHostException;
import java.util.Arrays;

public class DataGeneratorRunnerTests {
    @Test
    @Ignore
    public void test1() throws UnknownHostException, JsonProcessingException {
        DataGeneratorRunner.run(
                new ElasticConfiguration(
                        Arrays.asList("localhost"),
                        new LightSchema("e0", "rel0", "i0", "ref0", "%08d"),
                        new LightSchema("e2", "rel2", "i2", "ref2", "%08d")),
                new ContextGenerationConfiguration("context1", "context10", 10.0, 0.1, 1.0, 1000000, 1000000, 1000000, 1000000, 1000000));
    }
}
