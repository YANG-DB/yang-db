package com.kayhut.fuse.model.execution.plan.descriptors;

import com.google.inject.Inject;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.descriptors.Descriptor;
import com.kayhut.fuse.model.execution.plan.AsgEBasePlanOp;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.Cost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.entity.EntityJoinOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityNoOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.entity.GoToEntityOp;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import com.kayhut.fuse.model.query.quant.QuantBase;

import java.util.*;

/**
 * Created by roman.margolis on 28/11/2017.
 */
public class PlanWithCostDescriptor<P, C> implements Descriptor<PlanWithCost<P, C>> {
    //region Constructors
    @Inject
    public PlanWithCostDescriptor(Descriptor<? super P> planDescriptor, Descriptor<? super C> costDescriptor) {
        this.planDescriptor = planDescriptor;
        this.costDescriptor = costDescriptor;
    }
    //endregion

    //region Descriptor Implementation
    @Override
    public String describe(PlanWithCost<P, C> planWithCost) {
        return "{" +
                " plan:" + this.planDescriptor.describe(planWithCost.getPlan()) + "," + "\n" +
                " cost:" + this.costDescriptor.describe(planWithCost.getCost()) + "\n" +
                "}";
    }
    //endregion

    public static String print(PlanWithCost<Plan, PlanDetailedCost> planWithCost) {
        List<String> builder = new LinkedList<>();
        builder.add("cost:" + planWithCost.getCost().getGlobalCost()+"\n");
        print(builder, planWithCost.getPlan().getOps(), 1);
        return builder.toString();
    }


    static <T extends EBase> String print(List<String> builder, List<PlanOp> ops, int level) {
        Map<Integer, Integer> cursorLocations = new HashMap<>();
        builder.add("");
        int currentLine = 0;
        for (int i = 0; i < ops.size(); i++) {
            AsgEBasePlanOp<T> planOp = (AsgEBasePlanOp<T>) ops.get(i);
            String text = QueryDescriptor.getPrefix(isTail(planOp), planOp.getAsgEbase().geteBase()) + shortLabel(planOp, new StringJoiner(":"));
            if (planOp instanceof GoToEntityOp) {
                char[] zeros = new char[cursorLocations.get(((GoToEntityOp) planOp).getAsgEbase().geteNum()) - 5];
                Arrays.fill(zeros, ' ');
                builder.add("\n" + String.valueOf(zeros) + text);
                level++;
            } else if (planOp instanceof EntityJoinOp) {
                char[] zeros = new char[cursorLocations.getOrDefault(((EntityJoinOp) planOp).getAsgEbase().geteNum(), 0)];
                Arrays.fill(zeros, ' ');
                builder.add("\n" + String.valueOf(zeros) + text);
                level++;
                builder.add("\n\t\t" + print(new ArrayList<>(), ((EntityJoinOp) planOp).getLeftBranch().getOps(), 0));
                level++;
                builder.add("\n\t\t" + print(new ArrayList<>(), ((EntityJoinOp) planOp).getRightBranch().getOps(), 0));
                level++;
            } else if (planOp instanceof EntityNoOp) {
                char[] zeros = new char[cursorLocations.get(((EntityNoOp) planOp).getAsgEbase().geteNum()) - 5];
                Arrays.fill(zeros, ' ');
                builder.add("\n" + String.valueOf(zeros) + text);
                level++;
            } else {
                builder.set(level + currentLine, builder.get(level + currentLine) + text);
                cursorLocations.put(planOp.getAsgEbase().geteNum(), builder.get(level + currentLine).length() - 1);
            }
        }
        return builder.toString();
    }

    private static <T extends EBase> boolean isTail(AsgEBasePlanOp<T> planOp) {
        return planOp instanceof EntityNoOp || planOp instanceof GoToEntityOp || planOp instanceof EntityJoinOp;
    }

    private static <T extends EBase> String shortLabel(AsgEBasePlanOp<T> element, StringJoiner joiner) {
        if (element instanceof GoToEntityOp) {
            return joiner.add("goTo[" + element.getAsgEbase().geteNum() + "]").toString();
        }
        if (element instanceof EntityJoinOp) {
            AsgQueryDescriptor.shortLabel(element.getAsgEbase(), joiner);
            joiner.add("join[" + element.getAsgEbase().geteNum() + "]");
            return joiner.toString();
        }
        if (element instanceof EntityNoOp) {
            return joiner.add("Opt[" + element.getAsgEbase().geteNum() + "]").toString();
        } else
            return AsgQueryDescriptor.shortLabel(element.getAsgEbase(), joiner);
    }

    //region Fields
    private Descriptor<? super P> planDescriptor;
    private Descriptor<? super C> costDescriptor;
    //endregion
}