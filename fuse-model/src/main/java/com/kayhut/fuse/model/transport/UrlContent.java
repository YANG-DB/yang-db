package com.kayhut.fuse.model.transport;

import com.kayhut.fuse.model.Content;

/**
 * Created by lior on 19/02/2017.
 */
public abstract class UrlContent implements Content {
    private boolean completed;
    private String url;
    private String id;

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

}
