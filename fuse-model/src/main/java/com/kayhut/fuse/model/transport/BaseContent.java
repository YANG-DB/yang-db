package com.kayhut.fuse.model.transport;

import com.kayhut.fuse.model.Content;

/**
 * Created by lior on 19/02/2017.
 */
public abstract class BaseContent<T> implements Content<T> {
    private boolean completed;
    private String id;

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }

    public static BaseContent of(String url) {
        return new BaseContent() {
            @Override
            public long getResults() {
                return 0;
            }

            @Override
            public Object getData() {
                return "{}";
            }

        };
    }
}
