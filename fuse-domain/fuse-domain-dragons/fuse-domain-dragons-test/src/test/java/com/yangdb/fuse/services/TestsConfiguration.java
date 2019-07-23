package com.yangdb.fuse.services;

import com.yangdb.fuse.services.engine2.data.CsvCursorTests;
import com.yangdb.fuse.services.engine2.data.JoinE2EEpbMockTests;
import com.yangdb.fuse.services.engine2.data.JoinE2ETests;
import com.yangdb.fuse.services.engine2.data.SmartEpbCountTests;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Roman on 05/04/2017.
 */
public class TestsConfiguration {
    //region Static
    public static TestsConfiguration instance = new TestsConfiguration();
    //endregion

    //region Constructors
    public TestsConfiguration() {
        this.testClassesToRun = new HashSet<>();

        //mockEngine tests
        this.testClassesToRun.add(com.yangdb.fuse.services.mockEngine.ApiDescriptorTest.class);
        this.testClassesToRun.add(com.yangdb.fuse.services.mockEngine.CatalogTest.class);
        this.testClassesToRun.add(com.yangdb.fuse.services.mockEngine.CursorTest.class);
        this.testClassesToRun.add(com.yangdb.fuse.services.mockEngine.DataTest.class);
        this.testClassesToRun.add(com.yangdb.fuse.services.mockEngine.PageTest.class);
        this.testClassesToRun.add(com.yangdb.fuse.services.mockEngine.PlanTest.class);
        this.testClassesToRun.add(com.yangdb.fuse.services.mockEngine.QueryTest.class);
        //this.testClassesToRun.add(com.yangdb.fuse.services.mockEngine.SearchTest.class);

        //engine1 tests
        //this.testClassesToRun.add(com.yangdb.fuse.services.engine1.CursorTest.class);
        //this.testClassesToRun.add(com.yangdb.fuse.services.engine1.DataTest.class);
        //this.testClassesToRun.add(com.yangdb.fuse.services.engine1.PageTest.class);
        //this.testClassesToRun.add(com.yangdb.fuse.services.engine1.QueryTest.class);

        //engine2 tests
        this.testClassesToRun.add(com.yangdb.fuse.services.engine2.CursorTest.class);
        this.testClassesToRun.add(com.yangdb.fuse.services.engine2.PageTest.class);
        this.testClassesToRun.add(com.yangdb.fuse.services.engine2.QueryTest.class);

        this.testClassesToRun.add(com.yangdb.fuse.services.engine2.data.PromiseEdgeTest.class);
        this.testClassesToRun.add(com.yangdb.fuse.services.engine2.data.SingleEntityTest.class);
        this.testClassesToRun.add(com.yangdb.fuse.services.engine2.data.DfsRedundantEntityRelationEntityTest.class);
        this.testClassesToRun.add(com.yangdb.fuse.services.engine2.data.DfsNonRedundantEntityRelationEntityTest.class);
        this.testClassesToRun.add(CsvCursorTests.class);
        this.testClassesToRun.add(com.yangdb.fuse.services.engine2.data.SmartEpbRedundantEntityRelationEntityTest.class);
        this.testClassesToRun.add(com.yangdb.fuse.services.engine2.data.SmartEpbM2RedundantEntityRelationEntityTest.class);

        this.testClassesToRun.add(JoinE2EEpbMockTests.class);
        this.testClassesToRun.add(JoinE2ETests.class);
        this.testClassesToRun.add(SmartEpbCountTests.class);
    }
    //endregion

    //region Public Methods
    public boolean shouldRunTestClass(Class testClass) {
        return testClassesToRun.contains(testClass);
    }
    //endregion

    //region Fields
    private Set<Class> testClassesToRun;
    //endregion
}
