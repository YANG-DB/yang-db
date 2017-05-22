package com.kayhut.fuse.services;

import com.kayhut.fuse.services.engine2.data.DfsNonRedundantEntityRelationEntityTest;
import com.kayhut.fuse.services.engine2.data.EntityRelationEntityTest;

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
        this.testClassesToRun.add(com.kayhut.fuse.services.mockEngine.ApiDescriptorTest.class);
        this.testClassesToRun.add(com.kayhut.fuse.services.mockEngine.CatalogTest.class);
        this.testClassesToRun.add(com.kayhut.fuse.services.mockEngine.CursorTest.class);
        this.testClassesToRun.add(com.kayhut.fuse.services.mockEngine.DataTest.class);
        this.testClassesToRun.add(com.kayhut.fuse.services.mockEngine.PageTest.class);
        this.testClassesToRun.add(com.kayhut.fuse.services.mockEngine.PlanTest.class);
        this.testClassesToRun.add(com.kayhut.fuse.services.mockEngine.QueryTest.class);
        //this.testClassesToRun.add(com.kayhut.fuse.services.mockEngine.SearchTest.class);

        //engine1 tests
        //this.testClassesToRun.add(com.kayhut.fuse.services.engine1.CursorTest.class);
        //this.testClassesToRun.add(com.kayhut.fuse.services.engine1.DataTest.class);
        //this.testClassesToRun.add(com.kayhut.fuse.services.engine1.PageTest.class);
        //this.testClassesToRun.add(com.kayhut.fuse.services.engine1.QueryTest.class);

        //engine2 tests
        this.testClassesToRun.add(com.kayhut.fuse.services.engine2.CursorTest.class);
        this.testClassesToRun.add(com.kayhut.fuse.services.engine2.PageTest.class);
        this.testClassesToRun.add(com.kayhut.fuse.services.engine2.QueryTest.class);

        this.testClassesToRun.add(com.kayhut.fuse.services.engine2.data.SingleEntityTest.class);
        //this.testClassesToRun.add(com.kayhut.fuse.services.engine2.data.DfsRedundantEntityRelationEntityTest.class);
        this.testClassesToRun.add(com.kayhut.fuse.services.engine2.data.DfsNonRedundantEntityRelationEntityTest.class);
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
