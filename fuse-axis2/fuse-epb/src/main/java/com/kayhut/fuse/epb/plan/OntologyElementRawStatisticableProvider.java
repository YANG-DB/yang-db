package com.kayhut.fuse.epb.plan;

import com.kayhut.fuse.epb.plan.statistics.RawGraphStatisticableItemInfo;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchema;

/**
 * Created by moti on 4/18/2017.
 */
public interface OntologyElementRawStatisticableProvider {
    RawGraphStatisticableItemInfo getRawStatisticable(EBase eBase);
}
