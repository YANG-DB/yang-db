package com.kayhut.fuse.assembly.knowledge.cypher;

import com.kayhut.fuse.assembly.knowledge.*;
import com.kayhut.fuse.assembly.knowledge.load.KnowledgeWriterContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by Roman on 21/06/2017.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        KnowledgeInsertLogicalGraphTest.class,
        KnowledgeMassInsertionGraphTest.class,
        KnowledgeMassInsertionPathTest.class,
        KnowledgePathMultiStepsForwardOnlyCursorTest.class,
        KnowledgeSimpleEntityTests.class
})

public class KnowledgeSimpleCypherTestSuite {
    public static KnowledgeWriterContext ctx;

    @BeforeClass
    public static void setup() throws Exception {
        System.out.println("KnowledgeSimpleCypherTestSuite - setup");
        Setup.setup();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        System.out.println("KnowledgeSimpleCypherTestSuite - teardown");
        Setup.cleanup();
    }
}
