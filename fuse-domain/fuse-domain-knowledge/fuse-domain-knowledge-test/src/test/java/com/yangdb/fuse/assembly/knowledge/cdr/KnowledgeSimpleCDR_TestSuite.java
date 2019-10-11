package com.yangdb.fuse.assembly.knowledge.cdr;

import com.yangdb.fuse.assembly.knowledge.Setup;
import com.yangdb.fuse.assembly.knowledge.domain.KnowledgeWriterContext;
import com.yangdb.test.BaseSuiteMarker;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.startupcheck.IndefiniteWaitOneShotStartupCheckStrategy;

import static com.yangdb.fuse.assembly.knowledge.Setup.client;
import static com.yangdb.fuse.assembly.knowledge.Setup.manager;

/**
 * Created by Roman on 21/06/2017.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        KnowledgeSimpleCdrWithCypherQueryTests.class,
        KnowledgeSimpleCdrWithJsonQueryTests.class,
        KnowledgeSimpleCdrWithV1InnerQueryTests.class
})

public class KnowledgeSimpleCDR_TestSuite implements BaseSuiteMarker {
    public static KnowledgeWriterContext ctx;

    @Rule
    public static GenericContainer yangDbContainer = new GenericContainer<>("yangdb/yang.db:v1_Oct2019")
            .withNetwork(Network.SHARED)
            .withExposedPorts(8888, 9200, 9300);


    @BeforeClass
    public static void setup() throws Exception {
/*
        yangDbContainer.withStartupCheckStrategy(new IndefiniteWaitOneShotStartupCheckStrategy());
        yangDbContainer.start();
        Setup.setup(false, true, false);
*/
        Setup.setup();
        ctx = KnowledgeWriterContext.init(client, manager.getSchema());
        long start = System.currentTimeMillis();
        long amount = DataLoader.load(ctx, "./data/cdr/cdr-small.csv");
        System.out.println(String.format("Loaded %d rows in %s ", amount, (System.currentTimeMillis() - start) / 1000));
    }

    @AfterClass
    public static void tearDown() throws Exception {
        System.out.println("KnowledgeSimpleTestSuite - teardown");
        if (ctx != null) ctx.removeCreated();
        Setup.cleanup(true, false);
    }
}



