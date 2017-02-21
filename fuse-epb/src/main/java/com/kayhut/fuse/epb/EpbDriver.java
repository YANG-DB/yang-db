package com.kayhut.fuse.epb;

import com.google.common.eventbus.Subscribe;
import com.kayhut.fuse.model.process.AsgData;
import com.kayhut.fuse.model.process.EpbData;

/**
 * Created by lior on 21/02/2017.
 */
public interface EpbDriver {
    @Subscribe
    EpbData process(AsgData input);
}
