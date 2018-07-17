package com.kayhut.fuse.dispatcher.driver;

import com.fasterxml.jackson.databind.JsonNode;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;

import java.util.Optional;

/**
 * Created by User on 08/03/2017.
 */
public interface PageDriver {
    Optional<PageResourceInfo> create(String queryId, String cursorId, int pageSize);
    Optional<StoreResourceInfo> getInfo(String queryId, String cursorId);
    Optional<PageResourceInfo> getInfo(String queryId, String cursorId, String pageId);
    Optional<Object> getData(String queryId, String cursorId, String pageId);
    Optional<Boolean> delete(String queryId, String cursorId, String pageId);
}
