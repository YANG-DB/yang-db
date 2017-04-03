package com.kayhut.fuse.unipop.converter;

import org.apache.tinkerpop.gremlin.structure.Element;

/**
 * Created by r on 3/25/2015.
 */
public interface ElementWrapper<TWrapped> extends Element, Wrapper<Element, TWrapped>{
}
