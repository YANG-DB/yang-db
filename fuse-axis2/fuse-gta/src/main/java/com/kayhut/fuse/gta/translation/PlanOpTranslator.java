package com.kayhut.fuse.gta.translation;

import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;

/**
 * Created by moti on 3/7/2017.
 */
public interface PlanOpTranslator {
    Traversal translate(PlanOpBase op, Traversal traversal);
}
