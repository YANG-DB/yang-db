package com.kayhut.fuse.gta.strategy.discrete;

/*-
 * #%L
 * fuse-dv-gta
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

import com.kayhut.fuse.dispatcher.gta.PlanTraversalTranslator;
import com.kayhut.fuse.dispatcher.gta.TranslationContext;
import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.gta.strategy.PlanOpTranslationStrategy;
import com.kayhut.fuse.gta.strategy.PlanOpTranslationStrategyBase;
import com.kayhut.fuse.gta.strategy.utils.ConversionUtil;
import com.kayhut.fuse.gta.translation.ChainedPlanOpTraversalTranslator;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.OptionalOp;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.entity.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityJoinOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationFilterOp;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.properties.BasePropGroup;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.constraint.WhereByConstraint;
import javaslang.collection.Stream;
import javaslang.control.Option;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.WhereTraversalStep;

import java.util.*;

import static java.util.stream.Stream.*;

public class WhereByOpTranslationStrategy extends PlanOpTranslationStrategyBase {
    //region Constructors
    public WhereByOpTranslationStrategy() {
        super(planOp -> planOp.getClass().equals(EntityFilterOp.class));
    }
    //endregion

    //region PlanOpTranslationStrategyBase Implementation
    @Override
    protected GraphTraversal<?, ?> translateImpl(GraphTraversal traversal, PlanWithCost<Plan, PlanDetailedCost> planWithCost, PlanOp planOp, TranslationContext context) {
        EntityFilterOp filterOp = (EntityFilterOp) planOp;
        EntityOp entityOp = PlanUtil.prev(planWithCost.getPlan(), planOp, EntityOp.class).get();
        String entityTag = entityOp.getAsgEbase().geteBase().geteTag();
        EPropGroup group = filterOp.getAsgEbase().geteBase();

        if (!group.getProps().isEmpty() || !group.getGroups().isEmpty()) {
            List<EProp> props = group.getProps();
            List<EProp> groupProps = Stream.ofAll(group.getGroups()).flatMap(BasePropGroup::getProps).toJavaList();
            Optional<EProp> whereClause = of(props, groupProps)
                    .flatMap(Collection::stream)
                    .filter(this::isWhereClause)
                    .findAny();

            if (whereClause.isPresent()) {
                WhereByConstraint constraint = (WhereByConstraint) whereClause.get().getCon();
                traversal.asAdmin().where(entityTag, ConversionUtil.convertConstraint(constraint))
                        .by(constraint.getProjectedField());
            }
        }


        return traversal;
    }

    private boolean isWhereClause(EProp p) {
        return p.getCon() != null && p.getCon() instanceof WhereByConstraint;
    }
    //endregion

}
