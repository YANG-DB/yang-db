package com.kayhut.fuse.dispatcher.driver;

import java.util.Optional;

/**
 * Created by lior on 21/02/2017.
 */
public interface InternalsDriver {
    Optional<String> getStatisticsProviderName();
    Optional<String> getStatisticsProviderSetup();
    Optional<String> refreshStatisticsProviderSetup();
}
