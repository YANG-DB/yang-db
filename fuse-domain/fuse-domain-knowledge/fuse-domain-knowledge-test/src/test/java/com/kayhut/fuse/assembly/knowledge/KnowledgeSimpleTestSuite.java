package com.kayhut.fuse.assembly.knowledge;

import com.kayhut.fuse.assembly.knowledge.domain.KnowledgeWriterContext;
import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.kayhut.fuse.services.FuseApp;
import com.kayhut.fuse.services.FuseRunner;
import org.jooby.Jooby;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.File;
import java.nio.file.Paths;

import static com.kayhut.fuse.assembly.knowledge.Setup.client;
import static com.kayhut.fuse.assembly.knowledge.Setup.manager;

/**
 * Created by Roman on 21/06/2017.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        //KnowledgeSimpleEntityWithRelationTests.class,
        //KnowledgeSimpleEntityWithFilterTests.class,
        //KnowledgeSimpleEntityTests.class,
        KnowledgeSimpleEntityWithFilterE2ETests.class,
        KnowledgeSimpleEvalueWithFilterE2ETests.class,
        KnowledgeSimpleEfileWithFilterE2ETests.class,
        KnowledgeSimpleReferenceWithFilterE2ETests.class,
        KnowledgeSimpleRelationWithFilterE2ETests.class,
        KnowledgeSimpleRvalueWithFilterE2ETests.class,
        KnowledgeSimpleInsightWithFilterE2ETests.class,
        KnowledgeSimpleEntityAndEvalueWithFilterE2ETests.class,
        KnowledgeSimpleEntityAndEfileWithFilterE2ETests.class,
        KnowledgeSimpleEntityAndInsightWithFilterE2ETests.class,
        KnowledgeSimpleEntityAndRelationWithFilterE2ETests.class
})

public class KnowledgeSimpleTestSuite {
    public static KnowledgeWriterContext ctx;

    @BeforeClass
    public static void setup() throws Exception {
        System.out.println("KnowledgeSimpleTestSuite - setup");
        Setup.setup();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        System.out.println("KnowledgeSimpleTestSuite - teardown");
        Setup.cleanup();
    }
}
