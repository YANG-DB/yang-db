package com.kayhut.fuse.unipop.controller.utils.traversal;

import com.kayhut.fuse.unipop.step.BoostingStepWrapper;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.*;

public class BoostingTraversalVisitor {
    //Public Methods
    public boolean visit(Traversal traversal) {
        return visitRecursive(traversal);
    }
    //endregion

    //Protected Methods
    protected boolean visitRecursive(Object o) {
        if (Traversal.class.isAssignableFrom(o.getClass())) {
            return visitTraversal((Traversal) o);
        } else if (o.getClass() == OrStep.class) {
            return visitOrStep((OrStep) o);
        } else if (o.getClass() == AndStep.class) {
            return visitAndStep((AndStep) o);
        } else if (o.getClass() == NotStep.class) {
            return visitNotStep((NotStep) o);
        } else if (o.getClass() == HasStep.class) {
            return visitHasStep((HasStep) o);
        } else if (o.getClass() == TraversalFilterStep.class) {
            return visitTraversalFilterStep((TraversalFilterStep) o);
        } else if(o.getClass() == BoostingStepWrapper.class){
            return visitBoostingStep((BoostingStepWrapper) o);
        } else {
            //TODO: allow configurable behavior for unsupported or unexpected elements
            throw new UnsupportedOperationException(o.getClass() + " is not supported in promise conditions");
        }
    }

    protected boolean visitBoostingStep(BoostingStepWrapper o) {
        return true;
    }

    protected boolean visitNotStep(NotStep<?> notStep) {
        return notStep.getLocalChildren().stream().map(this::visitRecursive).reduce((a,b) -> a || b ).orElse(false);
    }

    protected boolean visitTraversal(Traversal<?, ?> traversal) {
        return traversal.asAdmin().getSteps().stream().map(this::visitRecursive).reduce((a,b) -> a || b ).orElse(false);
    }

    protected boolean visitOrStep(OrStep<?> orStep) {
        return orStep.getLocalChildren().stream().map(this::visitRecursive).reduce((a,b) -> a || b ).orElse(false);
    }

    protected boolean visitAndStep(AndStep<?> andStep) {
        return andStep.getLocalChildren().stream().map(this::visitRecursive).reduce((a,b) -> a || b ).orElse(false);
    }

    protected boolean visitHasStep(HasStep<?> hasStep)
    {
        return false;
    }

    protected boolean visitTraversalFilterStep(TraversalFilterStep<?> traversalFilterStep) {
        return traversalFilterStep.getLocalChildren().stream().map(this::visitRecursive).reduce((a,b) -> a || b ).orElse(false);


    }

}