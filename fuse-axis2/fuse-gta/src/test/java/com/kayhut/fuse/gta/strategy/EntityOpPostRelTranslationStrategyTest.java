package com.kayhut.fuse.gta.strategy;

import com.kayhut.fuse.gta.translation.PlanUtil;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.DefaultGraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by benishue on 12-Mar-17.
 */
public class EntityOpPostRelTranslationStrategyTest {
    @Test
    public void entityOpPostRelTranslationStrategyTest1() throws Exception {
        EConcrete concrete = new EConcrete();
        concrete.seteNum(2);
        concrete.seteTag("A");

        Rel rel = new Rel();
        rel.seteNum(1);

        EntityOp entityOp = new EntityOp(AsgEBase.EBaseAsgBuilder.<EEntityBase>anEBaseAsg().withEBase(concrete).build());
        RelationOp relationOp = new RelationOp(AsgEBase.EBaseAsgBuilder.<Rel>anEBaseAsg().withEBase(rel).build());

        Plan plan = Mockito.mock(Plan.class);
        when(plan.getOps()).thenAnswer(invocationOnMock -> {
            List<PlanOpWithCost> ops = new ArrayList<>();
            ops.add(new PlanOpWithCost(relationOp, null));
            ops.add(new PlanOpWithCost(entityOp, null));
            return ops;
        });


        PlanUtil planUtil = Mockito.mock(PlanUtil.class);
        when(planUtil.getPrev(any(),any())).thenAnswer(invocationOnMock -> {
            if(invocationOnMock.getArgumentAt(0,EntityOp.class).equals(entityOp))
                return Optional.of(relationOp);
            return Optional.empty();
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

        TranslationStrategyContext context = Mockito.mock(TranslationStrategyContext.class);
        when(context.getOntology()).thenAnswer( invocationOnMock -> ontology);
        when(context.getPlan()).thenAnswer(invocationOnMock -> plan);
        when(context.getPlanOpBase()).thenAnswer(invocationOnMock -> entityOp);


        EntityOpPostRelTranslationStrategy entityOpPostRelTranslationStrategy = new EntityOpPostRelTranslationStrategy();
        GraphTraversal traversal = entityOpPostRelTranslationStrategy.apply(context,new DefaultGraphTraversal());
        List steps = traversal.asAdmin().getSteps();
        Assert.assertEquals(steps.get(0).getClass().getSimpleName(), "EdgeOtherVertexStep");
    }



}