package com.kayhut.fuse.assembly.knowledge.domain;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

//todo - for kobi usage
public abstract class Metadata extends KnowledgeDomainBuilder {
    static SimpleDateFormat sdf;

    static {
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public String lastUpdateUser = "test";
    public String creationUser = "test";
    public String creationTime = sdf.format(new Date(System.currentTimeMillis()));
    public String lastUpdateTime = sdf.format(new Date(System.currentTimeMillis()));
    public String[] authorization = new String[]{"procedure.1", "procedure.2"};


}
