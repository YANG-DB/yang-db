package com.kayhut.fuse.assembly.knowledge;

import com.kayhut.fuse.unipop.controller.common.context.CompositeControllerContext;
import com.kayhut.fuse.unipop.controller.search.SearchOrderProvider;
import com.kayhut.fuse.unipop.controller.search.SearchOrderProviderFactory;
import com.kayhut.fuse.unipop.step.BoostingStepWrapper;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.*;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

public class KnowledgeSearchOrderProvider implements SearchOrderProviderFactory {
    @Override
    public SearchOrderProvider build(CompositeControllerContext context) {
        SearchOrderProvider.Sort sort = new SearchOrderProvider.Sort(FieldSortBuilder.DOC_FIELD_NAME,SortOrder.ASC);
        SearchType searchType = SearchType.DEFAULT;
        BoostingTraversalVisitor boostingTraversalVisitor = new BoostingTraversalVisitor();
        if (context.getConstraint().isPresent() && boostingTraversalVisitor.visit(context.getConstraint().get().getTraversal())) {
            sort = SearchOrderProvider.EMPTY;
            searchType = SearchType.DFS_QUERY_THEN_FETCH;
        }

        SearchOrderProvider.Sort finalSort = sort;
        SearchType finalSearchType = searchType;

        return new SearchOrderProvider() {
            @Override
            public Sort getSort(SearchRequestBuilder builder) {
                return finalSort;
            }

            @Override
            public SearchType getSearchType(SearchRequestBuilder builder) {
                return finalSearchType;
            }
        };
    }

    private class BoostingTraversalVisitor{
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
}
