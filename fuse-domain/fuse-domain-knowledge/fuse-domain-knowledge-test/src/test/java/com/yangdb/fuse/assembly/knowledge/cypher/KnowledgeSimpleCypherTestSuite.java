package com.yangdb.fuse.assembly.knowledge.cypher;

import com.yangdb.fuse.assembly.knowledge.*;
import com.yangdb.fuse.assembly.knowledge.load.KnowledgeWriterContext;
import com.yangdb.fuse.model.resourceInfo.ResultResourceInfo;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.IOException;
import java.net.URL;

import static com.yangdb.fuse.assembly.knowledge.Setup.fuseClient;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeReaderContext.KNOWLEDGE;

/**
 * Created by Roman on 21/06/2017.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        KnowledgeInsertLogicalGraphTest.class,
        KnowledgeLoadMergeLogicalGraphTest.class,
        KnowledgePathMultiStepsForwardOnlyCursorTest.class,
        KnowledgeSimpleEntityTests.class,
        KnowledgeUploadLogicalGraphTest.class,
        KnowledgeMassInsertionGraphTest.class,
})

public class KnowledgeSimpleCypherTestSuite {
    public static KnowledgeWriterContext ctx;

    @BeforeClass
    public static void setup() throws Exception {
        System.out.println("KnowledgeSimpleCypherTestSuite - setup");
        Setup.setup();
        loadData();

    }

    @AfterClass
    public static void tearDown() throws Exception {
        System.out.println("KnowledgeSimpleCypherTestSuite - teardown");
        Setup.cleanup(true,false);
    }

    private static void loadData() throws IOException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("./data/logical/multi_steps.json");
        ResultResourceInfo info = fuseClient.loadData(KNOWLEDGE, resource);
        Assert.assertNotNull(info);
    }

}



