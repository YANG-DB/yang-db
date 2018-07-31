package com.kayhut.fuse.model.transport;

import com.fasterxml.jackson.annotation.*;
import com.kayhut.fuse.model.results.TextContent;
import org.jooby.Status;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Created by lior on 19/02/2017.
 */
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@JsonPropertyOrder( {"requestId", "status", "elapsed", "renderElapsed", "totalElapsed", "dataType", "data" } )
public class ContentResponse<T> implements Response, TextContent {
    public static <T> ContentResponse<T> notFound() {
        return Builder.<T>builder(Status.NOT_FOUND, Status.NOT_FOUND)
                .data(Optional.empty())
                .compose();
    }

    public static <T> ContentResponse<T> internalError(Exception ex) {
        return Builder.<T>builder(Status.SERVER_ERROR, Status.SERVER_ERROR)
                .data(Optional.empty())
                .error(ex)
                .compose();
    }

    //region Constructors
    public ContentResponse() {
    }
    //endregion

    //region Properties
    public String getRequestId() {
        return this.requestId;
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public String getElapsed() {
        return String.format("%08d", this.elapsed);
    }

    @JsonIgnore
    public long elapsed() {
        return this.elapsed;
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public String getRenderElapsed() {
        return String.format("%08d", this.renderElapsed);
    }

    @JsonIgnore
    public long renderElapsed() {
        return this.renderElapsed;
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public String getTotalElapsed() {
        return String.format("%08d", this.totalElapsed);
    }

    @JsonIgnore
    public long totalElapsed() {
        return this.totalElapsed;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public ExternalMetadata getExternal() {
        return this.external;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public T getData() {
        return this.data;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getDataType() {
        return dataType;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("status")
    public Status status() {
        return this.status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Exception getError() {
        return this.error;
    }

    @Override
    public String content() {
        if(this.data != null && TextContent.class.isAssignableFrom(this.data.getClass())){
            return ((TextContent)this.data).content();
        }else{
            return this.toString();
        }
    }
    //endregion

    @Override
    public String toString() {
        return String.format("ContentResponse{status=%s, requestId=%s, external=%s, elapsed=%d, data=%s}",
                status,
                requestId,
                external,
                elapsed,
                data);
    }

    //region Fields
    private Status status = Status.NOT_FOUND;
    private String requestId;
    private long elapsed;
    private long renderElapsed;
    private long totalElapsed;
    private ExternalMetadata external;
    private T data;
    private String dataType;
    private Exception error;
    //endregion

    //region Builder
    public static class Builder<T> {
        public static <S> Builder<S> builder(Status success, Status fail) {
            return new Builder<>(success, fail);
        }

        public static <S> Builder<S> builder(ContentResponse<S> response) {
            return new Builder<>(response);
        }

        //region Constructors
        public Builder(Status success, Status fail) {
            this.response = new ContentResponse<>();
            this.success = success;
            this.fail = fail;

            this.successPredicate = response1 -> response1.data != null && response1.error == null;
        }

        public Builder(ContentResponse<T> response) {
            this.response = response;
            this.success = response.status;
            this.fail = response.status;

            this.successPredicate = response1 -> response1.data != null && response1.error == null;
        }
        //endregion

        //region Properties
        public Builder<T> requestId(String requestId) {
            this.response.requestId = requestId;
            return this;
        }

        public Builder<T> external(ExternalMetadata external) {
            this.response.external = external;
            return this;
        }

        public Builder<T> elapsed(long elapsed) {
            this.response.elapsed = elapsed;
            return this;
        }

        public Builder<T> renderElapsed(long renderElapsed) {
            this.response.renderElapsed = renderElapsed;
            return this;
        }

        public Builder<T> totalElapsed(long totalElapsed) {
            this.response.totalElapsed = totalElapsed;
            return this;
        }

        public Builder<T> data(Optional<T> data) {
            this.response.data = data.orElse(null);
            this.response.dataType = data.map(data1 -> data1.getClass().getSimpleName()).orElse(null);
            return this;
        }

        public Builder<T> error(Exception ex) {
            this.response.error = ex;
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
        //endregion

        //region Fields
        private Status success;
        private Status fail;
        private ContentResponse<T> response;
        private Predicate<ContentResponse<T>> successPredicate;
        //endregion
    }
    //endregion
}
