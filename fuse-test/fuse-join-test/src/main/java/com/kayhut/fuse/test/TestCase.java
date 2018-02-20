package com.kayhut.fuse.test;

import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.results.QueryResult;
import com.kayhut.fuse.test.util.FuseClient;

public abstract class TestCase {

    public abstract void run(FuseClient fuseClient) throws Exception;


    protected void testAndAssertQuery(Query query, FuseClient fuseClient) throws Exception {
        long start = System.currentTimeMillis();
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        long queryStart = System.currentTimeMillis();
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        long queryEnd = System.currentTimeMillis();
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl());
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 100000);
        Plan actualPlan = fuseClient.getPlanObject(queryResourceInfo.getExplainPlanUrl());
        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        QueryResult actualQueryResult = fuseClient.getPageData(pageResourceInfo.getDataUrl());

        long end = System.currentTimeMillis();

        System.out.println(actualPlan);
        System.out.println("Total time: " + (end -start));
        totalTime = end-start;
        planTime = queryEnd - queryStart;
        System.out.println("Assignments: " + actualQueryResult.getAssignments().size());
        System.out.println(actualQueryResult);
    }

    public long getTotalTime() {
        return totalTime;
    }

    public long getPlanTime() {
        return planTime;
    }

    private long totalTime;
    private long planTime;

}
