package com.kayhut.fuse.assembly.knowledge;

import com.kayhut.fuse.unipop.controller.common.context.CompositeControllerContext;
import com.kayhut.fuse.unipop.controller.search.SearchOrderProvider;
import com.kayhut.fuse.unipop.controller.search.SearchOrderProviderFactory;
import com.kayhut.fuse.unipop.controller.utils.traversal.BoostingTraversalVisitor;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

public class KnowledgeSearchOrderProvider implements SearchOrderProviderFactory {
    @Override
    public SearchOrderProvider build(CompositeControllerContext context) {
        SearchOrderProvider.Sort sort = new SearchOrderProvider.Sort(FieldSortBuilder.DOC_FIELD_NAME,SortOrder.ASC);
        SearchType searchType = SearchType.DEFAULT;
        BoostingTraversalVisitor boostingTraversalVisitor = new BoostingTraversalVisitor();
        if (context.getConstraint().isPresent() && boostingTraversalVisitor.visit(context.getConstraint().get().getTraversal())) {
            sort = SearchOrderProvider.EMPTY;
            searchType = SearchType.DFS_QUERY_THEN_FETCH;
        }

        SearchOrderProvider.Sort finalSort = sort;
        SearchType finalSearchType = searchType;

        return new SearchOrderProvider() {
            @Override
            public Sort getSort(SearchRequestBuilder builder) {
                return finalSort;
            }

            @Override
            public SearchType getSearchType(SearchRequestBuilder builder) {
                return finalSearchType;
            }
        };
    }


}
