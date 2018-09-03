package com.kayhut.fuse.unipop.controller.search;

import com.kayhut.fuse.unipop.controller.common.context.CompositeControllerContext;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

public class DefaultSearchOrderProvider implements SearchOrderProviderFactory {
    @Override
    public SearchOrderProvider build(CompositeControllerContext context) {
        return new SearchOrderProvider() {
            @Override
            public Sort getSort(SearchRequestBuilder builder) {
                return new Sort(FieldSortBuilder.DOC_FIELD_NAME,SortOrder.ASC);
            }

            @Override
            public SearchType getSearchType(SearchRequestBuilder builder) {
                return SearchType.DEFAULT;
            }
        };
    }
}
