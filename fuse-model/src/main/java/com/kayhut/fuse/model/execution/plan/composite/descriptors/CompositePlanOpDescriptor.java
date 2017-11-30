package com.kayhut.fuse.model.execution.plan.composite.descriptors;

import com.kayhut.fuse.model.descriptors.CompositeDescriptor;
import com.kayhut.fuse.model.descriptors.Descriptor;
import com.kayhut.fuse.model.descriptors.ToStringDescriptor;
import com.kayhut.fuse.model.execution.plan.AsgEBaseContainer;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.composite.CompositeAsgEBasePlanOp;
import com.kayhut.fuse.model.execution.plan.composite.CompositePlanOp;
import javaslang.collection.Stream;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by roman.margolis on 28/11/2017.
 */
public class CompositePlanOpDescriptor implements Descriptor<CompositePlanOp> {
    //region Public Static
    public static CompositePlanOpDescriptor getFull() {
        if (full == null) {
            full = getCompositePlanOpDescriptor(Mode.full);
        }

        return full;
    }

    public static CompositePlanOpDescriptor getLight() {
        if (light == null) {
            light = getCompositePlanOpDescriptor(Mode.light);
        }

        return light;
    }

    public static CompositePlanOpDescriptor getSimple() {
        if (simple == null) {
            simple = getCompositePlanOpDescriptor(Mode.simple);
        }

        return simple;
    }
    //endregion

    //Private Static Methods
    private static CompositePlanOpDescriptor getCompositePlanOpDescriptor(Mode mode) {
        CompositePlanOpDescriptor compositePlanOpDescriptor = new CompositePlanOpDescriptor(mode, null);

        Map<Class<?>, Descriptor<? extends PlanOp>> descriptors = new HashMap<>();
        descriptors.put(CompositePlanOp.class, compositePlanOpDescriptor);
        descriptors.put(CompositeAsgEBasePlanOp.class, compositePlanOpDescriptor);

        compositePlanOpDescriptor.compositeDescriptor = new CompositeDescriptor<>(descriptors, new ToStringDescriptor<>());
        return compositePlanOpDescriptor;
    }
    //endregion

    //region Static Fields
    private static CompositePlanOpDescriptor full;
    private static CompositePlanOpDescriptor light;
    private static CompositePlanOpDescriptor simple;
    //endregion

    //region Mode
    public enum Mode {
        full,
        light,
        simple
    }
    //endregion

    //region Constructors
    public CompositePlanOpDescriptor(Mode mode, CompositeDescriptor<PlanOp> compositeDescriptor) {
        this.mode = mode;
        this.compositeDescriptor = compositeDescriptor;
    }
    //endregion

    //region Descriptor Implementation
    @Override
    public String describe(CompositePlanOp compositePlanOp) {
        switch (this.mode) {
            case full: return this.fullPattern(compositePlanOp);
            case light: return this.lightPattern(compositePlanOp);
            case simple: return this.simplePattern(compositePlanOp);
        }

        return "";
    }
    //endregion

    //region Private Methods
    private String fullPattern(CompositePlanOp compositePlanOp) {
        StringBuilder sb = new StringBuilder();
        sb.append(compositePlanOp.getClass().getSimpleName()).append("{");
        compositePlanOp.getOps().forEach(op -> sb.append("[").append(this.compositeDescriptor.describe(op)).append("]:"));
        sb.deleteCharAt(sb.length() - 1);
        sb.append("}");

        return sb.toString();
    }

    private String lightPattern(CompositePlanOp compositePlanOp) {
        StringBuilder sb = new StringBuilder();
        sb.append(compositePlanOp.getClass().getSimpleName()).append("{");
        compositePlanOp.getOps().forEach(op -> sb.append(this.compositeDescriptor.describe(op)).append(":"));
        sb.deleteCharAt(sb.length() - 1);
        sb.append("}");

        return sb.toString();
    }

    private String simplePattern(CompositePlanOp compositePlanOp) {
        StringBuilder sb = new StringBuilder();
        sb.append(compositePlanOp.getClass().getSimpleName()).append("{");
        Stream.ofAll(compositePlanOp.getOps())
                .map(op -> AsgEBaseContainer.class.isAssignableFrom(op.getClass()) ?
                        Integer.toString(((AsgEBaseContainer)op).getAsgEbase().geteNum()) : Integer.toString(0))
                .forEach(eNum -> sb.append("[").append(eNum).append("]:"));
        sb.deleteCharAt(sb.length() - 1);
        sb.append("}");

        return sb.toString();
    }
    //endregion

    //region Fields
    private Mode mode;
    private CompositeDescriptor<PlanOp> compositeDescriptor;
    //endregion
}