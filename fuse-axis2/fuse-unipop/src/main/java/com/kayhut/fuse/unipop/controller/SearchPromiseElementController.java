package com.kayhut.fuse.unipop.controller;

import org.apache.tinkerpop.gremlin.structure.Element;
import org.unipop.query.search.SearchQuery;

import java.util.Iterator;

/**
 * Created by User on 19/03/2017.
 */
public class SearchPromiseElementController implements SearchQuery.SearchController {
    //region SearchQuery.SearchController Implementation
    @Override
    public <E extends Element> Iterator<E> search(SearchQuery<E> searchQuery) {
        return null;
    }
    //endregion
}
