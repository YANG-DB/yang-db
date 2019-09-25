package com.yangdb.fuse.assembly;


import com.yangdb.fuse.assembly.queries.DragonsSimpleE2ETest;
import com.yangdb.fuse.assembly.queries.DragonsSimpleFileUploadE2ETest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        DragonsSimpleFileUploadE2ETest.class,
        DragonsSimpleE2ETest.class
})
@Ignore
public class DragonsE2ETestSuite {

    @BeforeClass
    public static void setup() throws Exception {
        System.out.println("DragonsE2ETestSuite - setup");
        Setup.setup();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        System.out.println("DragonsE2ETestSuite - teardown");
        Setup.cleanup();
    }
}
