package com.yangdb.fuse.assembly.knowledge.cdr;

import com.yangdb.fuse.assembly.knowledge.Setup;
import com.yangdb.fuse.assembly.knowledge.domain.KnowledgeWriterContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import static com.yangdb.fuse.assembly.knowledge.Setup.*;

/**
 * Created by Roman on 21/06/2017.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        KnowledgeSimpleCdrWithCypherQueryTests.class,
        KnowledgeSimpleCdrWithJsonQueryTests.class,
        KnowledgeSimpleCdrWithV1InnerQueryTests.class,
        KnowledgeSimpleCdrWithV1QueryTests.class
})

public class KnowledgeCypherCDRTestSuite {
    public static KnowledgeWriterContext ctx;

    @BeforeClass
    public static void setup() throws Exception {
        Setup.setup();
        ctx = KnowledgeWriterContext.init(client, manager.getSchema());
        long start = System.currentTimeMillis();
        long amount = DataLoader.load( ctx, "./data/cdr/cdr-small.csv");
        System.out.println(String.format("Loaded %d rows in %s ",amount,(System.currentTimeMillis()-start)/1000));
    }

    @AfterClass
    public static void tearDown() throws Exception {
        System.out.println("KnowledgeSimpleTestSuite - teardown");
        if(ctx!=null) ctx.removeCreated();
        Setup.cleanup();
    }
}



