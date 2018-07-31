package com.kayhut.fuse.client;

import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.results.QueryResultBase;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreatePageRequest;
import com.kayhut.fuse.model.transport.CreateQueryRequest;
import com.kayhut.fuse.model.transport.cursor.CreateCursorRequest;

import java.util.Map;
import java.util.function.Consumer;

public interface AsyncFuseClient {
    interface Invocation<T> {
        Invocation<T> onSuccess(Consumer<ContentResponse<T>> onSuccess);
        Invocation<T> onFailure(Consumer<Throwable> onFailure);

        Invocation<T> send();

        ContentResponse<T> get();
        boolean isComplete();
    }

    Invocation<FuseResourceInfo> getFuseInfo();
    Invocation<Ontology> getOntology(String name);

    Invocation<QueryResourceInfo> postQuery(CreateQueryRequest createQueryRequest);
    Invocation<QueryResourceInfo> getQuery(QueryResourceInfo queryResourceInfo);
    Invocation<Boolean> deleteQuery(QueryResourceInfo queryResourceInfo);

    Invocation<CursorResourceInfo> postCursor(QueryResourceInfo queryResourceInfo, CreateCursorRequest createCursorRequest);
    Invocation<CursorResourceInfo> getCursor(CursorResourceInfo cursorResourceInfo);
    Invocation<Boolean> deleteCursor(CursorResourceInfo cursorResourceInfo);

    Invocation<PageResourceInfo> postPage(CursorResourceInfo cursorResourceInfo, CreatePageRequest createPageRequest);
    Invocation<PageResourceInfo> getPage(PageResourceInfo pageResourceInfo);
    Invocation<QueryResultBase> getPageData(PageResourceInfo pageResourceInfo);
    Invocation<Boolean> deletePage(PageResourceInfo pageResourceInfo);

    Invocation<Map<String, String>> getCursorBindings();
}
