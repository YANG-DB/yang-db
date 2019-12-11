package com.yangdb.fuse.assembly;


import com.yangdb.fuse.assembly.queries.DragonsNestedNoConstraintsQueryIT;
import com.yangdb.fuse.assembly.queries.DragonsSimpleConstraintsQueryIT;
import com.yangdb.fuse.assembly.queries.DragonsSimpleNoConstraintsQueryIT;
import com.yangdb.fuse.assembly.queries.DragonsSimpleFileUploadIT;
import com.yangdb.test.BaseSuiteMarker;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.nio.file.Paths;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        DragonsSimpleFileUploadIT.class,
        DragonsSimpleNoConstraintsQueryIT.class,
        DragonsSimpleConstraintsQueryIT.class,
        DragonsNestedNoConstraintsQueryIT.class
})
public class DragonsE2EWithNonRedundantEpbTestSuite implements BaseSuiteMarker {

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
