package com.kayhut.fuse.model.transport;

import com.kayhut.fuse.model.Content;

/**
 * Created by lior on 19/02/2017.
 */
public class Response {

    private final String id;
    private final String name;
    private final Content content;

    public Response(String id, String name, Content content) {
        this.id = id;
        this.name = name;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Content getContent() {
        return content;
    }
}
