package com.kayhut.fuse.epb.tests;

import com.kayhut.fuse.epb.plan.statistics.Statistics;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProvider;
import com.kayhut.fuse.model.execution.plan.Direction;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.*;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import org.mockito.Mockito;

import java.util.List;
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
            EPropGroup eProp = (EPropGroup) invocationOnMock.getArguments()[1];
            List<EProp> eProps = (eProp).geteProps();
            int id = Integer.valueOf(eProps.get(0).getpType());
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
            return new Statistics.Cardinality(total, total);
        });

        when(mock.getNodeStatistics(any())).thenAnswer(invocationOnMock -> {
            Object argument = invocationOnMock.getArguments()[0];

            if (argument instanceof EConcrete)
                return new Statistics.Cardinality(1,1);

            if (argument instanceof EUntyped)
                return new Statistics.Cardinality(maxCardinalityNode, maxCardinalityNode);

            if (argument instanceof ETyped) {
                long cost = statistics.get(PlanMockUtils.NODE_STATISTICS).get(((ETyped) argument).geteType()).longValue();
                return new Statistics.Cardinality(cost, cost);
            }
            //default
            return new Statistics.Cardinality(1, 1);


        });
        //mock statistics provider
        when(mock.getEdgeFilterStatistics(any(), any())).thenAnswer(invocationOnMock -> {
            Rel item = (Rel) invocationOnMock.getArguments()[0];
            RelPropGroup ePropGroup = (RelPropGroup) invocationOnMock.getArguments()[1];

            List<RelProp> relProps = (ePropGroup).getrProps();
            int id = Integer.valueOf(relProps.get(0).getpType());
            double factor = statistics.get(PlanMockUtils.EDGE_FILTER_STATISTICS).get(id);

            if (item != null) {
                long cost = statistics.get(PlanMockUtils.EDGE_STATISTICS).get(item.getrType()).longValue();
                double total = factor * cost;
                return new Statistics.Cardinality(total, total);
            }

            return new Statistics.Cardinality(1, 1);
        });

        when(mock.getEdgeStatistics(any())).thenAnswer(invocationOnMock -> {
            Rel item = (Rel) invocationOnMock.getArguments()[0];

            if (item != null) {
                long cost = statistics.get(PlanMockUtils.EDGE_STATISTICS).get(item.getrType()).longValue();
                return new Statistics.Cardinality(cost, cost);
            }

            //default
            return new Statistics.Cardinality(1, 1);


        });

        when(mock.getRedundantNodeStatistics(any(), any())).thenAnswer(invocationOnMock -> {
            Typed etype= (Typed) invocationOnMock.getArguments()[0];
            //todo - implement smart
            //Typed etype = (Typed) invocationOnMock.getArguments()[1];
            //EProp eprop = (EProp) invocationOnMock.getArguments()[2];
            //Direction dir = (Direction) invocationOnMock.getArguments()[3];


            long cost = statistics.get(PlanMockUtils.NODE_STATISTICS).get(etype.geteType()).longValue();
            return new Statistics.Cardinality(cost, cost);
        });


        when(mock.getRedundantEdgeStatistics(any(),any(), any())).thenAnswer(invocationOnMock -> {
            Rel rel = (Rel) invocationOnMock.getArguments()[0];
            //todo - implement smart
            //Typed etype = (Typed) invocationOnMock.getArguments()[2];
            //EPropGroup ePropGroup = (EPropGroup) invocationOnMock.getArguments()[3];
            Direction dir = (Direction) invocationOnMock.getArguments()[2];


            long cost = statistics.get(PlanMockUtils.EDGE_STATISTICS).get(rel.getrType()).longValue();
            return new Statistics.Cardinality(cost, cost);
        });


        when(mock.getGlobalSelectivity(any(), any(), any(), any())).thenAnswer(invocationOnMock -> 10);

        return mock;
    }


}
