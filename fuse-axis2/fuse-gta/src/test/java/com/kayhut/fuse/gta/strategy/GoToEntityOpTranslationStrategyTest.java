package com.kayhut.fuse.gta.strategy;

import com.kayhut.fuse.asg.AsgQueryStore;
import com.kayhut.fuse.asg.util.AsgQueryUtils;
import com.kayhut.fuse.gta.translation.TranslationContext;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.GoToEntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by benishue on 12-Mar-17.
 */
public class GoToEntityOpTranslationStrategyTest {
    @Test
    public void test_entity1_rel2_entity3_goto1() throws Exception {
        AsgQuery query = AsgQueryStore.simpleQuery1("name", "ontName");
        Plan plan = new Plan(
                new GoToEntityOp(AsgQueryUtils.<EEntityBase>getElement(query, 1).get())
        );

        TranslationContext context = Mockito.mock(TranslationContext.class);

        GoToEntityOpTranslationStrategy strategy = new GoToEntityOpTranslationStrategy();
        GraphTraversal actualTraversal = strategy.translate(__.start(), plan, plan.getOps().get(0), context);
        GraphTraversal expectedTraversal = __.start().select("A");
        Assert.assertEquals(expectedTraversal, actualTraversal);
    }

}