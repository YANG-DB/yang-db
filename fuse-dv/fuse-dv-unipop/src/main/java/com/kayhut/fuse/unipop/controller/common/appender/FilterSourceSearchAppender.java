package com.kayhut.fuse.unipop.controller.common.appender;

/*-
 * #%L
 * fuse-dv-unipop
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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

import com.kayhut.fuse.unipop.controller.common.appender.SearchAppender;
import com.kayhut.fuse.unipop.controller.common.context.SelectContext;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.predicates.SelectP;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;

/**
 * Created by Roman on 24/05/2017.
 */
public class FilterSourceSearchAppender implements SearchAppender<SelectContext> {
    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, SelectContext context) {
        for(HasContainer selectPHasContainer : context.getSelectPHasContainers()) {
            searchBuilder = appendSelectP(searchBuilder, selectPHasContainer.getKey(), selectPHasContainer.getPredicate());
        }

        return true;
    }
    //endregion

    //region Private Methods
    private SearchBuilder appendSelectP(SearchBuilder searchBuilder, String name, P<?> predicate) {
        if (!(predicate.getBiPredicate() instanceof SelectP)) {
            return searchBuilder;
        }

        SelectP selectP = (SelectP)predicate.getBiPredicate();
        switch (selectP) {
            case raw:
                searchBuilder.getIncludeSourceFields().add(predicate.getValue().toString());
                break;
        }

        return searchBuilder;
    }
    //endregion
}
