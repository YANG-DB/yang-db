package com.yangdb.fuse.unipop.controller.utils.traversal;

/*-
 * #%L
 * fuse-dv-unipop
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

import com.yangdb.fuse.unipop.step.BoostingStepWrapper;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.*;

import java.util.List;

/**
 * Created by benishue on 27-Mar-17.
 */
public class TraversalVisitor<TReturn> {
    //Public Methods
    public TReturn visit(Traversal traversal) {
        visitRecursive(traversal);
        return null;
    }
    //endregion

    //Protected Methods
    protected TReturn visitRecursive(Object o) {
        if (Traversal.class.isAssignableFrom(o.getClass())) {
            visitTraversal((Traversal) o);
        } else if (o.getClass() == OrStep.class) {
            visitOrStep((OrStep) o);
        } else if (o.getClass() == AndStep.class) {
            visitAndStep((AndStep) o);
        } else if (o.getClass() == NotStep.class) {
            visitNotStep((NotStep) o);
        } else if (o.getClass() == HasStep.class) {
            visitHasStep((HasStep) o);
        } else if (o.getClass() == WherePredicateStep.class) {
            visitWhereStep((WherePredicateStep) o);
        } else if (o.getClass() == TraversalFilterStep.class) {
            visitTraversalFilterStep((TraversalFilterStep) o);
        } else if(o.getClass() == BoostingStepWrapper.class){
          visitBoostingStep((BoostingStepWrapper) o);
        } else {
            //TODO: allow configurable behavior for unsupported or unexpected elements
            throw new UnsupportedOperationException(o.getClass() + " is not supported in promise conditions");
        }

        return null;
    }

    protected TReturn visitBoostingStep(BoostingStepWrapper o) {
        visitRecursive(o.getInnerStep());
        return null;
    }

    protected TReturn visitNotStep(NotStep<?> notStep) {
        notStep.getLocalChildren().forEach(this::visitRecursive);
        return null;
    }

    protected TReturn visitTraversal(Traversal<?, ?> traversal) {
        List<Step> steps = Stream.ofAll(traversal.asAdmin().getSteps()).toJavaList();
        for (Step step : steps) {
            visitRecursive(step);
        }

        return null;
    }

    protected TReturn visitOrStep(OrStep<?> orStep) {
        orStep.getLocalChildren().forEach(this::visitRecursive);

        return null;
    }

    protected TReturn visitAndStep(AndStep<?> andStep) {
        andStep.getLocalChildren().forEach(this::visitRecursive);
        return null;
    }

    protected TReturn visitHasStep(HasStep<?> hasStep) {
        return null;
    }

    protected TReturn visitWhereStep(WherePredicateStep<?> wherePredicateStep) {
        return null;
    }

    protected TReturn visitTraversalFilterStep(TraversalFilterStep<?> traversalFilterStep) {
        return null;
    }
    //endregion
}
