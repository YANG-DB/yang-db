package com.kayhut.fuse.gta.strategy;

import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.gta.strategy.common.GoToEntityOpTranslationStrategy;
import com.kayhut.fuse.gta.translation.TranslationContext;
import com.kayhut.fuse.model.OntologyTestUtils.DRAGON;
import com.kayhut.fuse.model.OntologyTestUtils.PERSON;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.GoToEntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import static com.kayhut.fuse.model.OntologyTestUtils.OWN;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.rel;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.typed;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static org.mockito.Matchers.any;

/**
 * Created by benishue on 12-Mar-17.
 */
public class GoToEntityOpTranslationStrategyTest {
    public static AsgQuery simpleQuery1(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, PERSON.type,"A"))
                .next(rel(2,OWN.getrType(),R))
                .next(typed(3, DRAGON.type,"B")).build();
    }

    @Test
    public void test_entity1_rel2_entity3_goto1() throws Exception {
        AsgQuery query = simpleQuery1("name", "ontName");
        Plan plan = new Plan(
                new GoToEntityOp(AsgQueryUtil.<EEntityBase>element(query, 1).get())
        );

        TranslationContext context = Mockito.mock(TranslationContext.class);

        GoToEntityOpTranslationStrategy strategy = new GoToEntityOpTranslationStrategy();
        GraphTraversal actualTraversal = strategy.translate(__.start(), plan, plan.getOps().get(0), context);
        GraphTraversal expectedTraversal = __.start().select("A");
        Assert.assertEquals(expectedTraversal, actualTraversal);
    }

}