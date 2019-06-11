package com.kayhut.fuse.epb.plan.extenders;

/*-
 * #%L
 * fuse-dv-epb
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

import com.kayhut.fuse.dispatcher.epb.PlanExtensionStrategy;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationFilterOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationOp;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.properties.RelProp;
import javaslang.Tuple2;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;

/**
 * Created by moti on 2/27/2017.
 */
public class AllDirectionsPlanExtensionStrategy implements PlanExtensionStrategy<Plan, AsgQuery> {
    public AllDirectionsPlanExtensionStrategy() {}

    @Override
    public Iterable<Plan> extendPlan(Optional<Plan> plan, AsgQuery query) {
        List<Plan> plans = new LinkedList<>();
        if(plan.isPresent()){
            Map<Integer, AsgEBase> queryParts = SimpleExtenderUtils.flattenQuery(query);

            Tuple2<List<AsgEBase>, Map<Integer, AsgEBase>> partsTuple = SimpleExtenderUtils.removeHandledQueryParts(plan.get(), queryParts);
            List<AsgEBase> handledParts = partsTuple._1();
            Map<Integer, AsgEBase> remainingQueryParts = partsTuple._2();

            // If we have query parts that need further handling
            if(remainingQueryParts.size() > 0){
                for(AsgEBase handledPart : handledParts){
                    plans.addAll(extendPart(handledPart, remainingQueryParts, plan.get()));
                }
            }
        }
/*
        for(Plan<C> newPlan : plans){
            newPlan.setPlanComplete(SimpleExtenderUtils.checkIfPlanIsComplete(newPlan, query));
        }
*/

        return plans;
    }

    private Collection<Plan> extendPart(AsgEBase<? extends EBase> handledPartToExtend, Map<Integer, AsgEBase> queryPartsNotHandled, Plan originalPlan) {
        List<Plan> plans = new ArrayList<>();
        if(((AsgEBase) handledPartToExtend).getNext() != null){
            for(AsgEBase<? extends EBase> next : handledPartToExtend.getNext()){
                if(SimpleExtenderUtils.shouldAddElement(next) && queryPartsNotHandled.containsKey(next.geteNum())){
                    PlanOp op = createOpForElement(next);
                    plans.add(new Plan(originalPlan.getOps()).withOp(op));
                }
            }
        }

        if(SimpleExtenderUtils.shouldAdvanceToParents(handledPartToExtend)){
            for(AsgEBase<? extends  EBase> parent : handledPartToExtend.getParents()){
                if(SimpleExtenderUtils.shouldAddElement(parent) && queryPartsNotHandled.containsKey(parent.geteNum())){
                    PlanOp op = createOpForElement(parent, true);
                    plans.add(new Plan(originalPlan.getOps()).withOp(op));
                    /*Plan<C> newPlan = Plan.PlanBuilder.search(originalPlan.getPlanOps())
                            .operation(new PlanOpWithCost<C>(op,costEstimator.estimateCost(originalPlan,op)))
                            .estimation(costEstimator)
                            .compose();*/
                }
            }
        }
        return plans;
    }

    private PlanOp createOpForElement(AsgEBase element) {
        return createOpForElement(element, false);
    }

    private PlanOp createOpForElement(AsgEBase element, boolean reverseDirection) {
        if(element.geteBase() instanceof EEntityBase){
            EntityOp op = new EntityOp(element);
            return op;
        }
        if(element.geteBase() instanceof Rel){
            AsgEBase<Rel> rel = element;
            if(reverseDirection){
                Rel rel1 = rel.geteBase();
                Rel rel2 = new Rel();
                rel2.seteNum(rel1.geteNum());
                rel2.setrType(rel1.getrType());
                rel2.setWrapper(rel1.getWrapper());
                if(rel1.getDir().equals(Rel.Direction.L)){
                    rel2.setDir(Rel.Direction.R);
                }else if(rel1.getDir().equals(Rel.Direction.R)){
                    rel2.setDir(Rel.Direction.L);
                }else{
                    rel2.setDir(rel1.getDir());
                }
                rel = AsgEBase.Builder.<Rel>get().withEBase(rel2).build();
            }
            RelationOp op = new RelationOp(rel);
            return op;
        }
        if(element.geteBase() instanceof RelProp){
            RelationFilterOp op = new RelationFilterOp(element);
            return op;
        }
        throw new NotImplementedException();
    }




}
