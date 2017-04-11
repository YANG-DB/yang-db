package com.kayhut.fuse.unipop.converter;

/**
 * Created by r on 3/16/2015.
 */
public interface ElementConverter<TElementSource, TElementDest> {
    TElementDest convert(TElementSource source);
}
