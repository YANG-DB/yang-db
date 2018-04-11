package com.kayhut.fuse.unipop.controller.common.converter;

import org.elasticsearch.search.SearchHit;

import java.util.Map;

/**
 * Created by roman.margolis on 14/03/2018.
 */
public class SearchHitDataItem implements DataItem {
    //region Constructors
    public SearchHitDataItem(SearchHit searchHit) {
        this.searchHit = searchHit;
    }
    //endregion

    //region DataItem Implementation
    @Override
    public Object id() {
        return searchHit.id();
    }

    @Override
    public Map<String, Object> properties() {
        return searchHit.sourceAsMap();
    }
    //endregion

    //region Fields
    private SearchHit searchHit;
    //endregion
}
