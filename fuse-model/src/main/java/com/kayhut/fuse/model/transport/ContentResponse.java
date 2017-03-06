package com.kayhut.fuse.model.transport;

import com.kayhut.fuse.model.query.QueryMetadata;
import com.kayhut.fuse.model.process.QueryResourceResult;

/**
 * Created by lior on 19/02/2017.
 */
public class ContentResponse<T> implements BaseResponse {
    public static final ContentResponse EMPTY =  new ContentResponse("NOT-FOUND");
    private String id;
    private Content<T> content;

    public ContentResponse(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Content getContent() {
        return content;
    }

    public static class ResponseBuilder<T> {
        private ContentResponse response;

        public static <S> ResponseBuilder<S> builder(String id) {
            return new ResponseBuilder<>(id);
        }

        public ResponseBuilder(String id) {
            response = new ContentResponse(id);
            response.id = id;
        }

        public ResponseBuilder<T> data(Content<T> data) {
            this.response.content = data;
            return this;
        }

        public ContentResponse<T> compose() {
            return response;
        }

    }
}
