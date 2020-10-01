package com.yangdb.fuse.model.query.entity;

import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.properties.EProp;

import java.util.List;

public interface EndPattern<T extends EBase> {
    T getEndEntity();
    List<EProp> getFilter();
}
