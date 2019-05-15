package com.kayhut.fuse.executor.cursor.discrete;

/*-
 * #%L
 * fuse-dv-core
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

import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.executor.cursor.TraversalCursorContext;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantType;
import com.kayhut.fuse.model.results.Assignment;
import com.kayhut.fuse.model.results.AssignmentsQueryResult;

import static com.kayhut.fuse.model.results.AssignmentsQueryResult.Builder.instance;


public class ForwardOnlyPathsTraversalCursor extends PathsTraversalCursor {
    //region Factory
    public static class Factory implements CursorFactory {
        //region CursorFactory Implementation
        @Override
        public Cursor createCursor(Context context) {
            return new ForwardOnlyPathsTraversalCursor((TraversalCursorContext) context);
        }
        //endregion
    }
    //endregion

    //region Constructors
    public ForwardOnlyPathsTraversalCursor(TraversalCursorContext context) {
        super(context);
    }
    //endregion

    //region Private Methods
    protected AssignmentsQueryResult toQuery(int numResults) {
        AssignmentsQueryResult.Builder builder = instance();
        AsgQuery asgQuery = getContext().getQueryResource().getAsgQuery();
        //check is Or quant exists -> if so take any largest path
        if (AsgQueryUtil.element(asgQuery, Quant1.class).isPresent()
                && AsgQueryUtil.element(asgQuery, Quant1.class).get().geteBase().getqType().equals(QuantType.some)) {
            //in case of Union -> return all sub-paths regardless of max elements in path
            (getContext().getTraversal().next(numResults)).forEach(path -> {
                builder.withAssignment(toAssignment(path));
            });
        } else {
            int nodes = AsgQueryUtil.elements(asgQuery, EEntityBase.class).size();
            int edges = AsgQueryUtil.elements(asgQuery, Rel.class).size();
            final Query pattern = getContext().getQueryResource().getQuery();
            builder.withPattern(pattern);
            //build assignments
            (getContext().getTraversal().next(numResults)).forEach(path -> {
                Assignment assignments = toAssignment(path);
                if (assignments.getEntities().size() == nodes && assignments.getRelationships().size() == edges) {
                    builder.withAssignment(assignments);
                }
            });
        }
        return builder.build();
    }

}
