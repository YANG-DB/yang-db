package com.kayhut.fuse.services.controllers;

import com.kayhut.fuse.model.transport.ContentResponse;

/**
 * Created by lior on 19/02/2017.
 */
public interface InternalsController {

    ContentResponse<String> getVersion();
    ContentResponse<String> getStatisticsProviderName();
    ContentResponse<String> getStatisticsProviderSetup();
    ContentResponse<String> refreshStatisticsProviderSetup();
}
