package com.kayhut.fuse.gta;

import com.google.common.eventbus.Subscribe;
import com.kayhut.fuse.model.process.EpbData;
import com.kayhut.fuse.model.process.GtaData;

/**
 * Created by lior on 21/02/2017.
 */
public interface GtaDriver {
    @Subscribe
    GtaData process(EpbData input);
}
