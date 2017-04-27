package com.kayhut.fuse.epb.tests;

import com.kayhut.fuse.epb.plan.statistics.Statistics;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProvider;
import com.kayhut.fuse.model.execution.plan.Direction;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.*;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by liorp on 4/26/2017.
 */
public class StatisticsMockUtils {

    static StatisticsProvider build(Map<String, Map<Integer, Double>> statistics, long maxCardinalityNode, long maxCardinalityEdge) {
        StatisticsProvider mock = Mockito.mock(StatisticsProvider.class);


        //mock statistics provider
        when(mock.getNodeFilterStatistics(any(), any())).thenAnswer(invocationOnMock -> {
            EEntityBase item = (EEntityBase) invocationOnMock.getArguments()[0];
            EProp eProp = (EProp) invocationOnMock.getArguments()[1];
            int id = Integer.valueOf((eProp).getpType());
            double factor = statistics.get(PlanMockUtils.NODE_FILTER_STATISTICS).get(id);

            long cost = 1;
            if (item instanceof EConcrete)
                cost = 1;

            if (item instanceof EUntyped)
                return maxCardinalityNode;

            if (item instanceof ETyped) {
                cost = statistics.get(PlanMockUtils.NODE_STATISTICS).get(((ETyped) item).geteType()).longValue();
            }

            double total = factor * cost;
            return new Statistics.HistogramStatistics(Collections.singletonList(new Statistics.BucketInfo<>((long)total, (long)total, "a", "z")));
        });

        when(mock.getNodeStatistics(any())).thenAnswer(invocationOnMock -> {
            Object argument = invocationOnMock.getArguments()[0];

            if (argument instanceof EConcrete)
                return new Statistics.HistogramStatistics(Collections.singletonList(new Statistics.BucketInfo<>(1l, 1l, "a", "z")));

            if (argument instanceof EUntyped)
                return new Statistics.HistogramStatistics(Collections.singletonList(new Statistics.BucketInfo<>(maxCardinalityNode, maxCardinalityNode, "a", "z")));

            if (argument instanceof ETyped) {
                long cost = statistics.get(PlanMockUtils.NODE_STATISTICS).get(((ETyped) argument).geteType()).longValue();
                return new Statistics.HistogramStatistics(Collections.singletonList(new Statistics.BucketInfo<>(cost, cost, "a", "z")));
            }
            //default
            return new Statistics.HistogramStatistics(Collections.singletonList(new Statistics.BucketInfo<>(1l, 1l, "a", "z")));


        });
        //mock statistics provider
        when(mock.getEdgeFilterStatistics(any(), any())).thenAnswer(invocationOnMock -> {
            Rel item = (Rel) invocationOnMock.getArguments()[0];
            RelProp eProp = (RelProp) invocationOnMock.getArguments()[1];

            int id = Integer.valueOf((eProp).getpType());
            double factor = statistics.get(PlanMockUtils.EDGE_FILTER_STATISTICS).get(id);

            if (item instanceof Rel) {
                long cost = statistics.get(PlanMockUtils.EDGE_STATISTICS).get(item.getrType()).longValue();
                double total = factor * cost;
                return new Statistics.HistogramStatistics(Collections.singletonList(new Statistics.BucketInfo<>((long)total, (long)total, "a", "z")));
            }

            return new Statistics.HistogramStatistics(Collections.singletonList(new Statistics.BucketInfo<>(1l, 1l, "a", "z")));
        });

        when(mock.getEdgeStatistics(any())).thenAnswer(invocationOnMock -> {
            Rel item = (Rel) invocationOnMock.getArguments()[0];

            if (item instanceof Rel) {
                long cost = statistics.get(PlanMockUtils.EDGE_STATISTICS).get(item.getrType()).longValue();
                return new Statistics.HistogramStatistics(Collections.singletonList(new Statistics.BucketInfo<>(cost, cost, "a", "z")));
            }

            //default
            return new Statistics.HistogramStatistics(Collections.singletonList(new Statistics.BucketInfo<>(1l, 1l, "a", "z")));


        });

        when(mock.getRedundantNodeStatistics(any(), any(), any(), any())).thenAnswer(invocationOnMock -> {
            Rel rel = (Rel) invocationOnMock.getArguments()[0];
            //todo - implement smart
            Typed etype = (Typed) invocationOnMock.getArguments()[1];
            EProp eprop = (EProp) invocationOnMock.getArguments()[2];
            Direction dir = (Direction) invocationOnMock.getArguments()[3];


            long cost = statistics.get(PlanMockUtils.NODE_STATISTICS).get(etype.geteType()).longValue();
            return new Statistics.HistogramStatistics(Collections.singletonList(new Statistics.BucketInfo<>(cost, cost, "a", "z")));
        });


        when(mock.getRedundantEdgeStatistics(any(), any(), any(), any())).thenAnswer(invocationOnMock -> {
            Rel rel = (Rel) invocationOnMock.getArguments()[0];
            //todo - implement smart
            Typed etype = (Typed) invocationOnMock.getArguments()[1];
            EProp eprop = (EProp) invocationOnMock.getArguments()[2];
            Direction dir = (Direction) invocationOnMock.getArguments()[3];


            long cost = statistics.get(PlanMockUtils.EDGE_STATISTICS).get(rel.getrType()).longValue();
            return new Statistics.HistogramStatistics(Collections.singletonList(new Statistics.BucketInfo<>(cost, cost, "a", "z")));
        });


        when(mock.getGlobalSelectivity(any(), any(), any())).thenAnswer(invocationOnMock -> 10);

        return mock;
    }


}
