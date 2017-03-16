package com.kayhut.fuse.epb.plan.extenders;

import com.kayhut.fuse.epb.plan.PlanExtensionStrategy;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import javaslang.Tuple2;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;

/**
 * Created by moti on 2/27/2017.
 */
public class AllDirectionsPlanExtensionStrategy implements PlanExtensionStrategy<Plan, AsgQuery> {
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
        return plans;
    }

    private Collection<Plan> extendPart(AsgEBase<? extends EBase> handledPartToExtend, Map<Integer, AsgEBase> queryPartsNotHandled, Plan originalPlan) {
        List<Plan> plans = new ArrayList<>();
        if(SimpleExtenderUtils.shouldAdvanceToNext(handledPartToExtend)){
            for(AsgEBase<? extends EBase> next : handledPartToExtend.getNext()){
                if(SimpleExtenderUtils.shouldAddElement(next) && queryPartsNotHandled.containsKey(next.geteNum())){
                    PlanOpBase op = createOpForElement(next);
                    Plan newPlan = new Plan(new LinkedList<>(originalPlan.getOps()));
                    newPlan.getOps().add(op);
                    plans.add(newPlan);
                }
            }
        }
        return plans;
    }

    private PlanOpBase createOpForElement(AsgEBase element) {
        if(element.geteBase() instanceof EEntityBase){
            EntityOp op = new EntityOp(element);
            return op;
        }
        if(element.geteBase() instanceof Rel){
            RelationOp op = new RelationOp(element);
            return op;
        }
        if(element.geteBase() instanceof RelProp){
            RelationFilterOp op = new RelationFilterOp(element);
            return op;
        }
        throw new NotImplementedException();
    }




}
