package com.kayhut.fuse.gta;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.context.CursorCreationOperationContext;
import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.dispatcher.ontolgy.OntologyProvider;
import com.kayhut.fuse.executor.cursor.TraversalCursorFactory;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.costs.SingleCost;
import com.kayhut.fuse.model.ontology.Ontology;
import javaslang.Tuple2;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.structure.Element;

import static com.kayhut.fuse.model.Utils.submit;

/**
 * Created by liorp on 3/16/2017.
 */
public class GtaTraversalCursorProcessor implements CursorCreationOperationContext.Processor {
    //region Constructors
    @Inject
    public GtaTraversalCursorProcessor(
            EventBus eventBus,
            OntologyProvider provider,
            GremlinTranslator gremlinTranslator,
            CursorFactory cursorFactory) {
        this.eventBus = eventBus;
        this.provider = provider;
        this.gremlinTranslator = gremlinTranslator;
        this.cursorFactory = cursorFactory;
        this.eventBus.register(this);
    }
    //endregion

    //region CursorCreationOperationContext.Processor implementation
    @Override
    @Subscribe
    public CursorCreationOperationContext process(CursorCreationOperationContext context) {
        if (context.getCursor() != null) {
            return context;
        }
        //execute gta plan ==> traversal extraction
        Plan<SingleCost> executionPlan = context.getQueryResource().getExecutionPlan();
        Ontology ontology = provider.get(context.getQueryResource().getQuery().getOnt()).get();

        Traversal<Element, Path> traversal = gremlinTranslator.translate(ontology, executionPlan);

        //submit
        Cursor cursor = this.cursorFactory.createCursor(new TraversalCursorFactory.TraversalCursorContext(ontology, context.getQueryResource(), traversal));
        return submit(eventBus, context.of(cursor));

    }
    //endregion

    //region Fields
    private final EventBus eventBus;
    private OntologyProvider provider;
    private GremlinTranslator gremlinTranslator;
    private CursorFactory cursorFactory;
    //endregion
}
