package com.yangdb.fuse.unipop.controller.promise.appender;

/*-
 * #%L
 * fuse-dv-unipop
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

import com.yangdb.fuse.unipop.controller.common.context.ElementControllerContext;
import com.yangdb.fuse.unipop.controller.search.QueryBuilder;
import com.yangdb.fuse.unipop.controller.utils.traversal.TraversalQueryTranslator;

/**
 * Created by lior.perry on 27/03/2017.
 */
public class ElementConstraintSearchAppender extends SearchQueryAppenderBase<ElementControllerContext> {
    //region SearchQueryAppenderBase Implementation
    @Override
    protected boolean append(QueryBuilder queryBuilder, ElementControllerContext context) {
        if (!context.getConstraint().isPresent()) {
            return false;
        }
        new TraversalQueryTranslator(queryBuilder.seekRoot().query().bool().filter().bool().must(), false)
                .visit(context.getConstraint().get().getTraversal());

        return true;
    }
    //endregion
}
