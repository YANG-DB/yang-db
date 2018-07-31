package com.kayhut.fuse.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.kayhut.fuse.client.deserializer.ContentResponseDeserializer;
import com.kayhut.fuse.dispatcher.cursor.CompositeCursorFactory;
import com.kayhut.fuse.dispatcher.cursor.CreateCursorRequestDeserializer;
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
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpContentResponse;
import org.eclipse.jetty.client.HttpResponse;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class AsyncRestFuseClient implements AsyncFuseClient, AutoCloseable {
    //region Constructors
    public AsyncRestFuseClient(String fuseUrl) throws Exception {
        this(fuseUrl, 100 * 1024 * 1024);
    }

    public AsyncRestFuseClient(String fuseUrl, int maxContentLength) throws Exception {
        this.maxContentLength = maxContentLength;

        this.httpClient = new HttpClient();
        this.httpClient.start();

        this.fuseUrl = fuseUrl;

        initializeObjectMapper();
        this.fuseResourceInfo = this.getFuseInfo().send().get().getData();
    }
    //endregion

    //region AsyncFuseClient Implementation
    @Override
    public Invocation<FuseResourceInfo> getFuseInfo() {
        return new Invocation<>(this, this.fuseUrl);
    }

    @Override
    public AsyncFuseClient.Invocation<Ontology> getOntology(String name) {
        return new Invocation<>(this, this.fuseResourceInfo.getCatalogStoreUrl() + "name");
    }

    @Override
    public Invocation<QueryResourceInfo> postQuery(CreateQueryRequest createQueryRequest) {
        return new Invocation<>(this, "POST", this.fuseResourceInfo.getQueryStoreUrl(), createQueryRequest, this.maxContentLength);
    }

    @Override
    public Invocation<QueryResourceInfo> getQuery(QueryResourceInfo queryResourceInfo) {
        return new Invocation<>(this, queryResourceInfo.getResourceUrl());
    }

    @Override
    public Invocation<Boolean> deleteQuery(QueryResourceInfo queryResourceInfo) {
        return new Invocation<>(this, "DELETE", queryResourceInfo.getResourceUrl());
    }

    @Override
    public Invocation<CursorResourceInfo> postCursor(QueryResourceInfo queryResourceInfo, CreateCursorRequest createCursorRequest) {
        return new Invocation<>(this, "POST", queryResourceInfo.getCursorStoreUrl(), createCursorRequest, this.maxContentLength);
    }

    @Override
    public Invocation<CursorResourceInfo> getCursor(CursorResourceInfo cursorResourceInfo) {
        return new Invocation<>(this, cursorResourceInfo.getResourceUrl());
    }

    @Override
    public Invocation<Boolean> deleteCursor(CursorResourceInfo cursorResourceInfo) {
        return new Invocation<>(this, "DELETE", cursorResourceInfo.getResourceUrl());
    }

    @Override
    public Invocation<PageResourceInfo> postPage(CursorResourceInfo cursorResourceInfo, CreatePageRequest createPageRequest) {
        return new Invocation<>(this, "POST", cursorResourceInfo.getPageStoreUrl(), createPageRequest, this.maxContentLength);
    }

    @Override
    public Invocation<PageResourceInfo> getPage(PageResourceInfo pageResourceInfo) {
        return new Invocation<>(this, pageResourceInfo.getResourceUrl());
    }

    @Override
    public Invocation<QueryResultBase> getPageData(PageResourceInfo pageResourceInfo) {
        return new Invocation<>(this, pageResourceInfo.getDataUrl());
    }

    @Override
    public Invocation<Boolean> deletePage(PageResourceInfo pageResourceInfo) {
        return new Invocation<>(this, "DELETE", pageResourceInfo.getResourceUrl());
    }

    @Override
    public Invocation<Map<String, String>> getCursorBindings() {
        return new Invocation<>(this, this.fuseUrl + "/internal/cursorBindings");
    }
    //endregion

    //region AutoClosable Implementation
    @Override
    public void close() throws Exception {
        this.httpClient.stop();
    }
    //endregion

    //region Private Methods
    private void initializeObjectMapper() {
        this.objectMapper = new ObjectMapper();
        initializeContentResponseDeserialization();
        initializeCursorBindingDeserialization();
    }

    private void initializeContentResponseDeserialization() {
        Map<String, Class> classMap = new HashMap<>();
        classMap.put(HashMap.class.getSimpleName(), Map.class);
        classMap.put(LinkedHashMap.class.getSimpleName(), Map.class);
        classMap.put(FuseResourceInfo.class.getSimpleName(), FuseResourceInfo.class);
        classMap.put(QueryResourceInfo.class.getSimpleName(), QueryResourceInfo.class);
        classMap.put(CursorResourceInfo.class.getSimpleName(), CursorResourceInfo.class);
        classMap.put(PageResourceInfo.class.getSimpleName(), PageResourceInfo.class);


        SimpleModule module = new SimpleModule();
        module.addDeserializer(ContentResponse.class, new ContentResponseDeserializer(classMap));
        this.objectMapper.registerModule(module);
    }

    private void initializeCursorBindingDeserialization() {
        Map<String, Class<? extends CreateCursorRequest>> cursorBindingClasses = Stream.ofAll(this.getCursorBindings().send().get().getData().entrySet())
                .toJavaMap(entry -> {
                    try {
                        return new Tuple2<>(entry.getKey(), (Class<? extends CreateCursorRequest>)Class.forName(entry.getValue()));
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        return new Tuple2<>(entry.getKey(), null);
                    }
                });

        SimpleModule module = new SimpleModule();
        module.addDeserializer(CreateCursorRequest.class,
                new CreateCursorRequestDeserializer(Stream.ofAll(cursorBindingClasses.entrySet())
                        .map(entry -> new CompositeCursorFactory.Binding(entry.getKey(), entry.getValue(), null))
                        .toJavaList()));
        this.objectMapper.registerModule(module);
    }
    //endregion

    //region Fields
    private String fuseUrl;
    private HttpClient httpClient;
    private FuseResourceInfo fuseResourceInfo;

    private ObjectMapper objectMapper;

    private int maxContentLength;
    //endregion

    //region Invocation
    private static class Invocation<TResult> extends BufferingResponseListener implements AsyncFuseClient.Invocation<TResult> {
        //region Constructors
        public Invocation(AsyncRestFuseClient client, String url) {
            this(client, "GET", url, null, 100 * 1024 * 1024);
        }

        public Invocation(AsyncRestFuseClient client, String method, String url) {
            this(client, method, url, null, 100 * 1024 * 1024);
        }

        public Invocation(AsyncRestFuseClient client, String method, String url, Object body, int maxLength) {
            super(maxLength);

            this.client = client;
            this.method = method;
            this.url = url;
            this.body = body;

            this.onSuccess = data -> {};
            this.onFailure = ex -> { throw new RuntimeException(ex); };

            this.completableFuture = new CompletableFuture<>();
        }
        //endregion

        //region Invocation Implementation
        @Override
        public AsyncFuseClient.Invocation<TResult> onSuccess(Consumer<ContentResponse<TResult>> onSuccess) {
            this.onSuccess = onSuccess;
            return this;
        }

        @Override
        public AsyncFuseClient.Invocation<TResult> onFailure(Consumer<Throwable> onFailure) {
            this.onFailure = onFailure;
            return this;
        }

        @Override
        public Invocation<TResult> send() {
            try {
                Request request = this.client.httpClient.newRequest(this.url).method(this.method);

                if (this.body != null) {
                    request.content(new StringContentProvider(
                            "application/json",
                            this.client.objectMapper.writeValueAsString(this.body),
                            StandardCharsets.UTF_8));
                }

                request.send(this);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            return this;
        }

        @Override
        public ContentResponse<TResult> get() {
            this.completableFuture.join();
            return this.contentResponse;
        }

        @Override
        public boolean isComplete() {
            return this.completableFuture.isDone();
        }
        //endregion

        //region CompleteListener Implementation
        @Override
        public void onComplete(Result result) {
            try {
                if (result.isSucceeded()) {
                    ContentResponse<TResult> contentResponse = this.client.objectMapper.readValue(
                            super.getContentAsString(),
                            new TypeReference<ContentResponse<TResult>>(){});

                    this.contentResponse = contentResponse;
                    this.onSuccess.accept(contentResponse);

                } else {
                    this.onFailure.accept(result.getFailure());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                this.completableFuture.complete(true);
            }
        }
        //endregion

        //region Fields
        private String url;
        private String method;
        private Object body;

        private AsyncRestFuseClient client;
        private Consumer<ContentResponse<TResult>> onSuccess;
        private Consumer<Throwable> onFailure;

        private ContentResponse<TResult> contentResponse;

        private CompletableFuture<Boolean> completableFuture;
        //endregion
    }
    //endregion
}
