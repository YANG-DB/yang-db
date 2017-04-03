package com.kayhut.fuse.unipop.converter;

import org.apache.tinkerpop.gremlin.structure.Element;
import org.elasticsearch.search.SearchHit;

import java.util.Collections;
import java.util.Iterator;

/**
 * Created by liorp on 4/2/2017.
 */
public class CompositeConverter  {

    public <T extends Element> Iterator<T> convert(Iterator<SearchHit> iterator) {
        //todo implement
        return Collections.emptyIterator();
    }
}
