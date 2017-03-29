package com.kayhut.fuse.gta.strategy;

import com.kayhut.fuse.gta.translation.PlanUtil;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.PlanOpWithCost;
import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.unipop.promise.PromiseGraph;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.DefaultGraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;

import static org.mockito.Mockito.when;

/**
 * Created by benishue on 12-Mar-17.
 */
public class EntityOpStartTranslationStrategyTest {
    @Test
    public void entityOpStartTranslationStrategyTest1() throws Exception {
        EConcrete concrete = new EConcrete();
        concrete.seteNum(1);
        concrete.seteTag("A");
        AsgEBase<EEntityBase> ebaseAsgEBase = AsgEBase.EBaseAsgBuilder.<EEntityBase>anEBaseAsg().withEBase(concrete).build();
        PlanOpBase planOpBase = new EntityOp(ebaseAsgEBase);

        Plan plan = Mockito.mock(Plan.class);
        when(plan.getOps()).thenAnswer(invocationOnMock -> {
            ArrayList<PlanOpWithCost> ops = new ArrayList<>();
            ops.add(new PlanOpWithCost(planOpBase, null));
            return ops;
        });


        Ontology ontology = Mockito.mock(Ontology.class);
        when(ontology.getEntityTypes()).thenAnswer(invocationOnMock ->
                {
                    ArrayList<EntityType> entityTypes = new ArrayList<>();
                    entityTypes.add(EntityType.EntityTypeBuilder.anEntityType()
                            .withEType(1).withName("Person").build());
                    return  entityTypes;
                }
        );

        PlanUtil planUtil = Mockito.mock(PlanUtil.class);
        when(planUtil.isFirst(plan.getOps(),planOpBase)).thenAnswer(invocationOnMock -> true);

        EntityOpStartTranslationStrategy entityOpStartTranslationStrategy = new EntityOpStartTranslationStrategy(new PromiseGraph());

        TranslationStrategyContext context = Mockito.mock(TranslationStrategyContext.class);
        when(context.getOntology()).thenAnswer( invocationOnMock -> ontology);
        when(context.getPlan()).thenAnswer(invocationOnMock -> plan);
        when(context.getPlanOpBase()).thenAnswer(invocationOnMock -> ((PlanOpWithCost)plan.getOps().get(0)).getOpBase());

        GraphTraversal traversal = entityOpStartTranslationStrategy.apply(context, new DefaultGraphTraversal());

        Assert.assertTrue(traversal.asAdmin().getSteps().iterator().hasNext());

    }


    @Test
    public void entityOpStartTranslationStrategyTest2() throws Exception {
        ETyped eTyped = new ETyped();
        eTyped.seteNum(1);
        eTyped.seteTag("A");
        eTyped.seteType(1);
        AsgEBase<EEntityBase> ebaseAsgEBase = AsgEBase.EBaseAsgBuilder.<EEntityBase>anEBaseAsg().withEBase(eTyped).build();
        PlanOpBase planOpBase = new EntityOp(ebaseAsgEBase);

        Plan plan = Mockito.mock(Plan.class);
        when(plan.getOps()).thenAnswer(invocationOnMock -> {
            ArrayList<PlanOpWithCost> ops = new ArrayList<>();
            ops.add(new PlanOpWithCost(planOpBase, null));
            return ops;
        });


        Ontology ontology = Mockito.mock(Ontology.class);
        when(ontology.getEntityTypes()).thenAnswer(invocationOnMock ->
                {
                    ArrayList<EntityType> entityTypes = new ArrayList<>();
                    entityTypes.add(EntityType.EntityTypeBuilder.anEntityType()
                            .withEType(1).withName("Person").build());
                    return  entityTypes;
                }
        );

        PlanUtil planUtil = Mockito.mock(PlanUtil.class);
        when(planUtil.isFirst(plan.getOps(),planOpBase)).thenAnswer(invocationOnMock -> true);

        EntityOpStartTranslationStrategy entityOpStartTranslationStrategy = new EntityOpStartTranslationStrategy(new PromiseGraph());

        TranslationStrategyContext translationStrategyContext = Mockito.mock(TranslationStrategyContext.class);
        when(translationStrategyContext.getOntology()).thenAnswer( invocationOnMock -> ontology);
        when(translationStrategyContext.getPlan()).thenAnswer(invocationOnMock -> plan);
        when(translationStrategyContext.getPlanOpBase()).thenAnswer(invocationOnMock -> ((PlanOpWithCost)plan.getOps().get(0)).getOpBase());

        GraphTraversal traversal = entityOpStartTranslationStrategy.apply(translationStrategyContext, new DefaultGraphTraversal());

        Assert.assertTrue(traversal.asAdmin().getSteps().iterator().hasNext());

    }

}