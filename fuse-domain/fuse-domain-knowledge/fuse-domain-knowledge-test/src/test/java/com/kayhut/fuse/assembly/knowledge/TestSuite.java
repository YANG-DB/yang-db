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
        KnowledgeSimpleEntityTests.class,
        KnowledgeSimpleEntityWithFilterTests.class,
        KnowledgeSimpleEntityWithRelationTests.class,

})
public class TestSuite {

    @BeforeClass
    public static void setup() throws Exception {
        Setup.setup();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        Setup.cleanup();
    }
}

