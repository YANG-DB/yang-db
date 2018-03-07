package com.kayhut.fuse.model.execution.plan.composite.descriptors;

import com.kayhut.fuse.model.descriptors.CompositeDescriptor;
import com.kayhut.fuse.model.descriptors.Descriptor;
import com.kayhut.fuse.model.descriptors.ToStringDescriptor;
import com.kayhut.fuse.model.execution.plan.AsgEBaseContainer;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.composite.CompositePlanOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityJoinOp;
import com.kayhut.fuse.model.execution.plan.entity.GoToEntityOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationOp;
import javaslang.collection.Stream;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Created by roman.margolis on 30/11/2017.
 */
public class IterablePlanOpDescriptor implements Descriptor<Iterable<PlanOp>> {
    //region Public Static
    public static IterablePlanOpDescriptor getFull() {
        if (full == null) {
            full = getIterablePlanOpDescriptor(Mode.full);
        }

        return full;
    }

    public static IterablePlanOpDescriptor getLight() {
        if (light == null) {
            light = getIterablePlanOpDescriptor(Mode.light);
        }

        return light;
    }

    public static IterablePlanOpDescriptor getSimple() {
        if (simple == null) {
            simple = getIterablePlanOpDescriptor(Mode.simple);
        }

        return simple;
    }
    //endregion

    //Private Static Methods
    private static IterablePlanOpDescriptor getIterablePlanOpDescriptor(Mode mode) {
        IterablePlanOpDescriptor iterablePlanOpDescriptor = new IterablePlanOpDescriptor(mode, null);

        Map<Class<?>, Descriptor<? extends PlanOp>> descriptors = new HashMap<>();
        descriptors.put(CompositePlanOp.class, new CompositePlanOpDescriptor(iterablePlanOpDescriptor));
        descriptors.put(EntityJoinOp.class, new EntityJoinOpDescriptor(iterablePlanOpDescriptor));

        iterablePlanOpDescriptor.compositeDescriptor = new CompositeDescriptor<>(descriptors, new ToStringDescriptor<>());
        return iterablePlanOpDescriptor;
    }
    //endregion

    //region Static Fields
    private static IterablePlanOpDescriptor full;
    private static IterablePlanOpDescriptor light;
    private static IterablePlanOpDescriptor simple;
    //endregion

    //region Mode
    public enum Mode {
        full,
        light,
        simple
    }
    //endregion

    //region Constructors
    public IterablePlanOpDescriptor(Mode mode, CompositeDescriptor<PlanOp> compositeDescriptor) {
        this.mode = mode;
        this.compositeDescriptor = compositeDescriptor;
    }
    //endregion

    //region Descriptor Implementation
    @Override
    public String describe(Iterable<PlanOp> items) {
        switch (this.mode) {
            case full: return this.fullPattern(items);
            case light: return this.lightPattern(items);
            case simple: return this.simplePattern(items);
        }

        return "";
    }
    //endregion

    //region Private Methods
    private String fullPattern(Iterable<PlanOp> planOps) {
        StringJoiner sj = new StringJoiner(":", "[", "]");
        planOps.forEach(op -> sj.add(this.compositeDescriptor.describe(op)));
        return sj.toString();
    }

    /**
     * used by RegexPatternCostEstimator - Do Not change
     * @param planOps
     * @return
     */
    private String lightPattern(Iterable<PlanOp> planOps) {
        StringJoiner sj = new StringJoiner(":", "", "");
        planOps.forEach(op -> sj.add(op.getClass().getSimpleName()));
        return sj.toString();
    }

    private String simplePattern(Iterable<PlanOp> planOps) {
        StringJoiner sj = new StringJoiner(":", "[", "]");
        Stream.ofAll(planOps)
                .map(op -> AsgEBaseContainer.class.isAssignableFrom(op.getClass()) ?
                        Integer.toString(((AsgEBaseContainer)op).getAsgEbase().geteNum()) : Integer.toString(0))
                .forEach(sj::add);
        return sj.toString();
    }
    //endregion

    //region Fields
    private Mode mode;
    private CompositeDescriptor<PlanOp> compositeDescriptor;
    //endregion
}
