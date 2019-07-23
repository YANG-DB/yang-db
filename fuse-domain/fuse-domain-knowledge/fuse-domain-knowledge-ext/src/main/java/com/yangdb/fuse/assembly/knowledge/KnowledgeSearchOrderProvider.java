package com.yangdb.fuse.assembly.knowledge;

/*-
 * #%L
 * fuse-domain-knowledge-ext
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.yangdb.fuse.unipop.controller.common.context.CompositeControllerContext;
import com.yangdb.fuse.unipop.controller.search.SearchOrderProvider;
import com.yangdb.fuse.unipop.controller.search.SearchOrderProviderFactory;
import com.yangdb.fuse.unipop.controller.utils.traversal.BoostingTraversalVisitor;
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
