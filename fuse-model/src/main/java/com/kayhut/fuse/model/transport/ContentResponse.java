package com.kayhut.fuse.model.transport;

/**
 * Created by lior on 19/02/2017.
 */
public class ContentResponse<T> implements Response {
    public static final ContentResponse NOT_FOUND =  new ContentResponse("NOT-FOUND");
    private String id;
    private T data;

    public ContentResponse(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public T getData() {
        return data;
    }

    public static class Builder<T> {
        private ContentResponse response;

        public static <S> Builder<S> builder(String id) {
            return new Builder<>(id);
        }

        public Builder(String id) {
            response = new ContentResponse(id);
            response.id = id;
        }

        public Builder<T> data(T data) {
            this.response.data = data;
            return this;
        }

        public ContentResponse<T> compose() {
            return response;
        }

    }
}
