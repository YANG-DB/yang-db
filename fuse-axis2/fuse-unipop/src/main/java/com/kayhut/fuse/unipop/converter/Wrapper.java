package com.kayhut.fuse.unipop.converter;

/**
 * Created by r on 3/25/2015.
 */
public interface Wrapper<TWrapper, TWrapped> {
    public TWrapper wrap(TWrapped wrapped);
    public TWrapped unwrap();
}
