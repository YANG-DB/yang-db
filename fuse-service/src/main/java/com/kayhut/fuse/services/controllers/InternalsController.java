package com.kayhut.fuse.services.controllers;

import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.cursor.CreateCursorRequest;

import java.util.Map;

/**
 * Created by lior on 19/02/2017.
 */
public interface InternalsController {

    ContentResponse<String> getVersion();
    ContentResponse<Long> getSnowflakeId();
    ContentResponse<Map<String, Class<? extends CreateCursorRequest>>> getCursorBindings();
    ContentResponse<String> getStatisticsProviderName();
    ContentResponse<String> getStatisticsProviderSetup();
    ContentResponse<String> refreshStatisticsProviderSetup();
}
