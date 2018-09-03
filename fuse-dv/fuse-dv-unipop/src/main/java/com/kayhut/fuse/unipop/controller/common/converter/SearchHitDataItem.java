package com.kayhut.fuse.unipop.controller.common.converter;

import com.kayhut.fuse.unipop.controller.utils.elasticsearch.SearchHitUtils;
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
        return this.searchHit.id();
    }

    @Override
    public Map<String, Object> properties() {
        if (this.properties == null) {
            this.properties = SearchHitUtils.convertToMap(this.searchHit);
        }

        return this.properties;
    }
    //endregion

    //region Fields
    private SearchHit searchHit;
    private Map<String, Object> properties;
    //endregion
}
