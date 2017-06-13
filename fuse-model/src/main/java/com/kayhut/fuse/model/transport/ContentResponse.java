package com.kayhut.fuse.model.transport;

import org.jooby.Status;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Created by lior on 19/02/2017.
 */
public class ContentResponse<T> implements Response {
    public static final ContentResponse NOT_FOUND =  new ContentResponse("NOT-FOUND");
    private Status status = Status.NOT_FOUND;
    private String id;
    private T data;

    //empty ctor for jackson
    public ContentResponse() {}

    public ContentResponse(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public T getData() {
        return data;
    }

    public Status status() {
        return status;
    }

    public static class Builder<T> {
        private String id;
        private Status success;
        private Status fail;
        private ContentResponse<T> response;
        private Predicate<ContentResponse<T>> successPredicate;

        public static <S> Builder<S> builder(String id, Status success, Status fail) {
            return new Builder<>(id, success, fail);
        }

        public Builder(String id, Status success, Status fail) {
            response = new ContentResponse<>(id);
            response.id = id;
            this.success = success;
            this.fail = fail;

            this.successPredicate = response1 -> response1.data != null;
        }

        public Builder<T> data(Optional<T> data) {
            this.response.data = data.orElse(null);
            return this;
        }

        public Builder<T> successPredicate(Predicate<ContentResponse<T>> successPredicate) {
            this.successPredicate = successPredicate;
            return this;
        }

        public ContentResponse<T> compose() {
            if(this.successPredicate.test(this.response)) {
                response.status = success;
            } else {
                response.status = fail;
            }

            return response;
        }

    }
}
