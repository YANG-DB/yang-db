package com.kayhut.fuse.dispatcher.urlSupplier;

/**
 * Created by User on 08/03/2017.
 */
public interface AppUrlSupplier {
    String resourceUrl(String queryId);
    String resourceUrl(String queryId, int cursorId);
    String resourceUrl(String queryId, int cursorId, int pageId);

    String queryStoreUrl();
    String cursorStoreUrl(String queryId);
    String pageStoreUrl(String queryId, int cursorId);
}
