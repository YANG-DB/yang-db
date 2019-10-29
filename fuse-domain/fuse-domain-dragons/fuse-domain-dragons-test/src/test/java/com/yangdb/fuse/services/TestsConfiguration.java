package com.yangdb.fuse.services;

import com.yangdb.fuse.services.engine2.data.*;
import com.yangdb.fuse.services.mockEngine.*;

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
        this.testClassesToRun.add(ApiDescriptorIT.class);
        this.testClassesToRun.add(CatalogIT.class);
        this.testClassesToRun.add(CursorIT.class);
        this.testClassesToRun.add(DataIT.class);
        this.testClassesToRun.add(PageIT.class);
        this.testClassesToRun.add(PlanIT.class);
        this.testClassesToRun.add(QueryIT.class);
        //this.testClassesToRun.add(com.yangdb.fuse.services.mockEngine.SearchTest.class);

        //engine1 tests
        //this.testClassesToRun.add(com.yangdb.fuse.services.engine1.CursorTest.class);
        //this.testClassesToRun.add(com.yangdb.fuse.services.engine1.DataTest.class);
        //this.testClassesToRun.add(com.yangdb.fuse.services.engine1.PageTest.class);
        //this.testClassesToRun.add(com.yangdb.fuse.services.engine1.QueryTest.class);

        //engine2 tests
        this.testClassesToRun.add(com.yangdb.fuse.services.engine2.CursorIT.class);
        this.testClassesToRun.add(com.yangdb.fuse.services.engine2.PageIT.class);
        this.testClassesToRun.add(com.yangdb.fuse.services.engine2.QueryIT.class);

        this.testClassesToRun.add(PromiseEdgeIT.class);
        this.testClassesToRun.add(SingleEntityIT.class);
        this.testClassesToRun.add(DfsRedundantEntityRelationEntityIT.class);
        this.testClassesToRun.add(DfsNonRedundantEntityRelationEntityIT.class);
        this.testClassesToRun.add(CsvCursorIT.class);
        this.testClassesToRun.add(SmartEpbRedundantEntityRelationEntityIT.class);
        this.testClassesToRun.add(SmartEpbM2RedundantEntityRelationEntityIT.class);

        this.testClassesToRun.add(JoinE2EEpbMockIT.class);
        this.testClassesToRun.add(JoinE2EIT.class);
        this.testClassesToRun.add(SmartEpbCountIT.class);
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
