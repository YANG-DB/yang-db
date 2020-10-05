package com.yangdb.fuse.epb.utils;

import com.yangdb.fuse.epb.plan.statistics.Statistics;
import com.yangdb.fuse.model.GlobalConstants;
import com.yangdb.fuse.unipop.structure.ElementType;

import java.util.Arrays;

/**
 * Created by moti on 5/18/2017.
 */
public class BasicScenarioSetup {

    public static ScenarioMockUtil setup(){
        return ScenarioMockUtil.start(10, 100).withElementCardinality("Person", 100L).withElementCardinality("Dragon", 500L).withElementCardinality("Guild", 10L).
                withLayoutRedundancy("own", "name", GlobalConstants.EdgeSchema.DEST_NAME).
                withTimeSeriesIndex("own", ElementType.edge, "startDate", 3).
                withHistogram("firstName", generateStringHistogram())
                .build();

    }

    private static Statistics.HistogramStatistics<String> generateStringHistogram(){
        return new Statistics.HistogramStatistics<>(Arrays.asList(new Statistics.BucketInfo<>(100L, 10L, "a","z")));
    }

    //private static Statistics.HistogramStatistics<Date> generateDateHistogram(){
        //return new Statistics.HistogramStatistics<>(Arrays.asList(new Statistics.BucketInfo<Date>(100l, 10l, new Date(System.currentTimeMillis()-1000*60), "z")));
    //}


}
