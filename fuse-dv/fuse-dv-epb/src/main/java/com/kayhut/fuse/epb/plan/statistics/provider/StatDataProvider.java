package com.kayhut.fuse.epb.plan.statistics.provider;

import java.util.List;
import java.util.Map;

/**
 * Created by Roman on 21/06/2017.
 */
public interface StatDataProvider {
    Iterable<Map<String, Object>> getStatDataItems(
            Iterable<String> indices,
            Iterable<String> types,
            Iterable<String> fields,
            Map<String, Object> constraints);
}
