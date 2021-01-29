package com.yangdb.fuse.model;

import com.yangdb.fuse.model.query.quant.QuantType;

public interface Container<T> extends Next<T> {
    QuantType getqType();
}
