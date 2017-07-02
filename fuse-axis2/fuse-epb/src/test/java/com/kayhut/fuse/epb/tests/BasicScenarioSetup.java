package com.kayhut.fuse.epb.tests;

import com.kayhut.fuse.epb.plan.statistics.Statistics;
import com.kayhut.fuse.unipop.structure.ElementType;

import java.util.Arrays;
import java.util.Date;

/**
 * Created by moti on 5/18/2017.
 */
public class BasicScenarioSetup {

    public static ScenarioMockUtil setup(){
        return ScenarioMockUtil.start(10, 100).withElementCardinality("Person", 100L).withElementCardinality("Dragon", 500L).withElementCardinality("Guild", 10L).
                withLayoutRedundancy("own", "name", "entityB.name").
                withTimeSeriesIndex("own", ElementType.edge, "startDate", 3).
                withHistogram("firstName", generateStringHistogram());

    }

    private static Statistics.HistogramStatistics<String> generateStringHistogram(){
        return new Statistics.HistogramStatistics<>(Arrays.asList(new Statistics.BucketInfo<String>(100L, 10L, "a","z")));
    }

    //private static Statistics.HistogramStatistics<Date> generateDateHistogram(){
        //return new Statistics.HistogramStatistics<>(Arrays.asList(new Statistics.BucketInfo<Date>(100l, 10l, new Date(System.currentTimeMillis()-1000*60), "z")));
    //}


}
