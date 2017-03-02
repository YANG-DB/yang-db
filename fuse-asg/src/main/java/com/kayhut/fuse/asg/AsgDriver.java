package com.kayhut.fuse.asg;

import com.google.common.eventbus.Subscribe;
import com.kayhut.fuse.model.process.AsgData;
import com.kayhut.fuse.model.process.QueryCursorData;
import com.kayhut.fuse.model.process.QueryData;

/**
 * Created by lior on 21/02/2017.
 */
public interface AsgDriver {
    @Subscribe
    AsgData process(QueryData input);
}
