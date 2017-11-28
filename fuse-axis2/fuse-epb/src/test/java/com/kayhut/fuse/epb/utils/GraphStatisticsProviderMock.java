package com.kayhut.fuse.epb.utils;

import com.kayhut.fuse.epb.plan.statistics.GraphStatisticsProvider;
import com.kayhut.fuse.epb.utils.PlanMockUtils;
import org.mockito.Mockito;

/**
 * Created by moti on 5/16/2017.
 */
public interface GraphStatisticsProviderMock  {
    static GraphStatisticsProvider mock(PlanMockUtils.PlanMockBuilder planBuilder) {
        GraphStatisticsProvider mock = Mockito.mock(GraphStatisticsProvider.class);

        //mock statistics provider
        return mock;
    }
}
