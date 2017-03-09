package com.kayhut.fuse.gta;

import com.kayhut.fuse.gta.translation.SimplePlanOpTranslator;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.RelationOp;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.unipop.PromiseGraph;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.DefaultGraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.SelectOneStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.VertexStep;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by benishue on 27-Feb-17.
 */
public class GremlinTranslatorAppenderTest {

    private static SimplePlanOpTranslator factory;
    private static Plan planOf2;

    @Test
    public void startTranslationStrategyTest() throws Exception {
        EConcrete concrete = new EConcrete();
        concrete.seteNum(1);
        concrete.seteTag("A");
        AsgEBase<EEntityBase> ebaseAsgEBase = AsgEBase.EBaseAsgBuilder.<EEntityBase>anEBaseAsg().withEBase(concrete).build();

        Plan plan = Mockito.mock(Plan.class);
        when(plan.getOps()).thenAnswer(invocationOnMock -> {
            List<PlanOpBase> ops = new ArrayList<>();
            ops.add(new EntityOp(ebaseAsgEBase));
            return ops;
        });
        when(plan.isFirst(any())).thenAnswer(invocationOnMock -> true);
        when(plan.getPrev(any())).thenAnswer(invocationOnMock -> {
            return Optional.empty();
        });
        GraphTraversal traversal = factory.translate(plan, new DefaultGraphTraversal());
        Assert.assertTrue(traversal.asAdmin().getSteps().iterator().hasNext());

    }

    @Test
    public void adjacentTranslationStrategyTest() throws Exception {

        EConcrete concrete1 = new EConcrete();
        concrete1.seteNum(1);
        concrete1.seteTag("A");

        EConcrete concrete2 = new EConcrete();
        concrete2.seteNum(2);
        concrete2.seteTag("B");

        EntityOp entity1 = new EntityOp(AsgEBase.EBaseAsgBuilder.<EEntityBase>anEBaseAsg().withEBase(concrete1).build());
        EntityOp entity2 = new EntityOp(AsgEBase.EBaseAsgBuilder.<EEntityBase>anEBaseAsg().withEBase(concrete2).build());

        Plan plan = Mockito.mock(Plan.class);
        when(plan.isFirst(any())).thenAnswer(invocationOnMock -> false);
        when(plan.getOps()).thenAnswer(invocationOnMock -> {
            List<PlanOpBase> ops = new ArrayList<>();
            ops.add(entity1);
            ops.add(entity2);
            return ops;
        });
        when(plan.getPrev(any())).thenAnswer(invocationOnMock -> {
            if(invocationOnMock.getArgumentAt(0,EntityOp.class).equals(entity2))
                return Optional.of(entity1);
            return Optional.empty();
        });

        GraphTraversal traversal = factory.translate(plan,new DefaultGraphTraversal());
        Assert.assertEquals(traversal.asAdmin().getSteps().get(0).getClass().getSimpleName(), "SelectOneStep"); ;

    }

    @Test
    public void EntityOpPostRelTranslationStrategyTest() throws Exception {
        EConcrete concrete = new EConcrete();
        concrete.seteNum(2);
        concrete.seteTag("A");

        Rel rel = new Rel();
        rel.seteNum(1);

        EntityOp entityOp = new EntityOp(AsgEBase.EBaseAsgBuilder.<EEntityBase>anEBaseAsg().withEBase(concrete).build());
        RelationOp relationOp = new RelationOp(AsgEBase.EBaseAsgBuilder.<Rel>anEBaseAsg().withEBase(rel).build());

        Plan plan = Mockito.mock(Plan.class);
        when(plan.isFirst(any())).thenAnswer(invocationOnMock -> false);
        when(plan.getOps()).thenAnswer(invocationOnMock -> {
            List<PlanOpBase> ops = new ArrayList<>();
            ops.add(relationOp);
            ops.add(entityOp);
            return ops;
        });
        when(plan.getPrev(any())).thenAnswer(invocationOnMock -> {
            if(invocationOnMock.getArgumentAt(0,EntityOp.class).equals(entityOp))
                return Optional.of(relationOp);
            return Optional.empty();
        });

        GraphTraversal traversal = factory.translate(plan,new DefaultGraphTraversal());
        List steps = traversal.asAdmin().getSteps();
        Assert.assertEquals(steps.get(steps.size()-1).getClass().getSimpleName(), "EdgeOtherVertexStep"); ;
    }


    @Test
    public void relationOpTranslationStrategyTest() throws Exception {
        Rel rel = new Rel();
        rel.seteNum(4);
        rel.setrType(1);
        rel.setDir("R");

        RelationOp relationOp = new RelationOp(AsgEBase.EBaseAsgBuilder.<Rel>anEBaseAsg().withEBase(rel).build());

        Plan plan = Mockito.mock(Plan.class);
        when(plan.getOps()).thenAnswer(invocationOnMock -> {
            List<PlanOpBase> ops = new ArrayList<>();
            ops.add(relationOp);
            return ops;
        });
        when(plan.isFirst(any())).thenAnswer(invocationOnMock -> false);
        GraphTraversal traversal = factory.translate(plan, new DefaultGraphTraversal());
        Assert.assertEquals(traversal.asAdmin().getSteps().get(0).getClass().getSimpleName(),"VertexStep");
        Assert.assertEquals(((VertexStep)traversal.asAdmin().getSteps().get(0)).getDirection(), Direction.OUT);
    }



    @Before
    public void setUp() throws Exception {
        factory = new SimplePlanOpTranslator(new PromiseGraph());
    }

    @BeforeClass
    public static void setUpOnce() {
        createPlanOf2();
    }

    private static void createPlanOf2() {
        AsgQuery twoEntitiesPathQuery = createTwoEntitiesPathQuery();
        planOf2 = createPlanForTwoEntitiesPathQuery(twoEntitiesPathQuery);
    }

    public static AsgQuery createTwoEntitiesPathQuery() {
        EUntyped untyped = new EUntyped();
        untyped.seteNum(3);
        AsgEBase<EUntyped> unTypedAsg3 = AsgEBase.EBaseAsgBuilder.<EUntyped>anEBaseAsg().withEBase(untyped).build();

        Rel rel = new Rel();
        rel.seteNum(2);
        AsgEBase<Rel> relAsg2 = AsgEBase.EBaseAsgBuilder.<Rel>anEBaseAsg().withEBase(rel).withNext(unTypedAsg3).build();

        EConcrete concrete = new EConcrete();
        concrete.seteNum(1);
        concrete.seteTag("A");
        AsgEBase<EConcrete> concreteAsg1 = AsgEBase.EBaseAsgBuilder.<EConcrete>anEBaseAsg().withEBase(concrete).withNext(relAsg2).build();

        Start start = new Start();
        start.seteNum(0);
        start.setNext(1);
        AsgEBase<Start> startAsg = AsgEBase.EBaseAsgBuilder.<Start>anEBaseAsg().withEBase(start).withNext(concreteAsg1).build();

        AsgQuery query = AsgQuery.AsgQueryBuilder.anAsgQuery().withStart(startAsg).build();

        return query;
    }

    public static Plan createPlanForTwoEntitiesPathQuery(AsgQuery asgQuery) {
        List<PlanOpBase> ops = new LinkedList<>();

        AsgEBase<Start> startAsg = asgQuery.getStart();
        AsgEBase<EEntityBase> entityAsg = (AsgEBase<EEntityBase>) startAsg.getNext().get(0);

        EntityOp concOp = new EntityOp(entityAsg);
        ops.add(concOp);

        AsgEBase<Rel> relBaseAsg = (AsgEBase<Rel>) entityAsg.getNext().get(0);
        RelationOp relOp = new RelationOp(relBaseAsg);
        ops.add(relOp);

        AsgEBase<EEntityBase> unBaseAsg = (AsgEBase<EEntityBase>) relBaseAsg.getNext().get(0);
        EntityOp unOp = new EntityOp(unBaseAsg);
        ops.add(unOp);

        return new Plan(ops);
    }

}