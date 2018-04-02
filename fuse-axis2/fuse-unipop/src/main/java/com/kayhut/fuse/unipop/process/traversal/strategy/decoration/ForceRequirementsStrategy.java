package com.kayhut.fuse.unipop.process.traversal.strategy.decoration;

import org.apache.tinkerpop.gremlin.process.computer.traversal.strategy.decoration.VertexProgramStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategies;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.EmptyStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.RequirementsStep;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.AbstractTraversalStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.TraverserRequirement;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Roman on 04/07/2017.
 */
public final class ForceRequirementsStrategy extends AbstractTraversalStrategy<TraversalStrategy.DecorationStrategy> implements TraversalStrategy.DecorationStrategy {

    private final Set<TraverserRequirement> requirements = new HashSet<>();

    private ForceRequirementsStrategy() {
    }

    @Override
    public void apply(final Traversal.Admin<?, ?> traversal) {
        traversal.addStep(new RequirementsStep<>(traversal, this.requirements));
    }

    public static void addRequirements(final TraversalStrategies traversalStrategies, final TraverserRequirement... requirements) {
        ForceRequirementsStrategy strategy = (ForceRequirementsStrategy) traversalStrategies.toList().stream().filter(s -> s instanceof ForceRequirementsStrategy).findAny().orElse(null);
        if (null == strategy) {
            strategy = new ForceRequirementsStrategy();
            traversalStrategies.addStrategies(strategy);
        } else {
            final ForceRequirementsStrategy cloneStrategy = new ForceRequirementsStrategy();
            cloneStrategy.requirements.addAll(strategy.requirements);
            strategy = cloneStrategy;
            traversalStrategies.addStrategies(strategy);
        }
        Collections.addAll(strategy.requirements, requirements);
    }
}
