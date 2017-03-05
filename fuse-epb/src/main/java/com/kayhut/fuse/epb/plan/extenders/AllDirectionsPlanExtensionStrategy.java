package com.kayhut.fuse.epb.plan.extenders;

import com.kayhut.fuse.epb.plan.PlanExtensionStrategy;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;

/**
 * Created by moti on 2/27/2017.
 */
public class AllDirectionsPlanExtensionStrategy implements PlanExtensionStrategy<Plan, AsgQuery> {
    @Override
    public Iterable<Plan> extendPlan(Plan plan, AsgQuery query) {
        List<Plan> plans = new LinkedList<>();
        if(plan != null){
            Map<Integer, AsgEBase> queryParts = SimpleExtenderUtils.flattenQuery(query);

            List<AsgEBase> handledParts = SimpleExtenderUtils.removeHandledParts(plan, queryParts);
            if(queryParts.size() > 0){
                for(AsgEBase handledPart : handledParts){
                    extendPart(handledPart, queryParts, plans, plan);
                }
            }
        }
        return plans;
    }

    private void extendPart(AsgEBase<? extends EBase> handledPartToExtend, Map<Integer, AsgEBase> queryPartsNotHandled, List<Plan> plans, Plan originalPlan) {
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
    }

    private PlanOpBase createOpForElement(AsgEBase element) {
        if(element.geteBase() instanceof EEntityBase){
            EEntityBase eEntityBase = (EEntityBase) element.geteBase();
            EntityOp op = new EntityOp(eEntityBase);
            op.seteNum(element.geteNum());
            op.setEntity((EEntityBase)element.geteBase());
            return op;
        }
        if(element.geteBase() instanceof Rel){
            Rel rel = (Rel) element.geteBase();
            RelationOp op = new RelationOp(rel);
            op.setRelation(rel);
            op.seteNum(element.geteNum());
            return op;
        }
        if(element.geteBase() instanceof RelProp){
            RelProp rel = (RelProp) element.geteBase();
            RelationFilterOp op = new RelationFilterOp(rel);
            op.seteNum(rel.geteNum());
            op.setRelProp((RelProp)element.geteBase());
            return op;
        }
        throw new NotImplementedException();
    }




}
