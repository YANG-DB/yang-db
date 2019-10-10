package com.yangdb.fuse.unipop.controller.utils.traversal;

/*-
 *
 * fuse-dv-unipop
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import com.yangdb.fuse.unipop.step.BoostingStepWrapper;
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
