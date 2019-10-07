package com.yangdb.fuse.assembly;


import com.yangdb.fuse.assembly.queries.DragonsSimpleE2ETest;
import com.yangdb.fuse.assembly.queries.DragonsSimpleFileUploadE2ETest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.nio.file.Paths;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        DragonsSimpleFileUploadE2ETest.class,
        DragonsSimpleE2ETest.class
})
public class DragonsE2EWithNonRedundantEpbTestSuite {

    @BeforeClass
    public static void setup() throws Exception {
        System.out.println("DragonsE2EWithNonRedundantEpbTestSuite - setup");
        Setup.withPath(Paths.get( "src","resources", "assembly", "Dragons", "config", "application.test.engine3.m1.dfs.dragons.public.conf"));
        Setup.setup();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        System.out.println("DragonsE2EWithNonRedundantEpbTestSuite - teardown");
//        Setup.cleanup();
    }
}
