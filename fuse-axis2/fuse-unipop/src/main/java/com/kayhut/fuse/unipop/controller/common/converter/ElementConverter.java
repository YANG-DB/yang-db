package com.kayhut.fuse.unipop.controller.common.converter;

/**
 * Created by r on 3/16/2015.
 */
public interface ElementConverter<TElementSource, TElementDest> {
    Iterable<TElementDest> convert(TElementSource source);
}
