package com.kayhut.fuse.gta.strategy;

import com.kayhut.fuse.gta.translation.PlanUtil;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.RelationOp;
import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.RelationshipType;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.DefaultGraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.VertexStep;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by benishue on 12-Mar-17.
 */
public class RelationOpTranslationStrategyTest {
    @Test
    public void relationOpTranslationStrategyTest1() throws Exception {

        EConcrete concrete1 = new EConcrete();
        concrete1.seteNum(1);
        concrete1.seteTag("A");

        Rel rel = new Rel();
        rel.seteNum(2);
        rel.setrType(1);
        rel.setDir("R");

        EConcrete concrete2 = new EConcrete();
        concrete2.seteNum(3);
        concrete2.seteTag("B");

        EntityOp entity1 = new EntityOp(AsgEBase.EBaseAsgBuilder.<EEntityBase>anEBaseAsg().withEBase(concrete1).build());
        EntityOp entity2 = new EntityOp(AsgEBase.EBaseAsgBuilder.<EEntityBase>anEBaseAsg().withEBase(concrete2).build());
        RelationOp relationOp = new RelationOp(AsgEBase.EBaseAsgBuilder.<Rel>anEBaseAsg().withEBase(rel).build());

        Plan plan = Mockito.mock(Plan.class);
        when(plan.getOps()).thenAnswer(invocationOnMock -> {
            List<PlanOpBase> ops = new ArrayList<>();
            ops.add(entity1);
            ops.add(relationOp);
            ops.add(entity2);
            return ops;
        });


        PlanUtil planUtil = Mockito.mock(PlanUtil.class);
        when(planUtil.isFirst(any(),any())).thenAnswer(invocationOnMock -> false);
        when(planUtil.getPrev(any(),any())).thenAnswer(invocationOnMock -> {
            if(invocationOnMock.getArgumentAt(0,RelationOp.class).equals(relationOp))
                return Optional.of(entity1);
            return Optional.empty();
        });
        when(planUtil.getNext(any(),any())).thenAnswer(invocationOnMock -> {
            if(invocationOnMock.getArgumentAt(0,RelationOp.class).equals(relationOp))
                return Optional.of(entity2);
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

        when(ontology.getRelationshipTypes()).thenAnswer(invocationOnMock ->
                {
                    ArrayList<RelationshipType> relTypes = new ArrayList<>();
                    relTypes.add(RelationshipType.RelationshipTypeBuilder.aRelationshipType()
                            .withRType(1).withName("Fire").build());
                    return  relTypes;
                }
        );

        TranslationStrategyContext context = Mockito.mock(TranslationStrategyContext.class);
        when(context.getOntology()).thenAnswer( invocationOnMock -> ontology);
        when(context.getPlan()).thenAnswer(invocationOnMock -> plan);
        when(context.getPlanOpBase()).thenAnswer(invocationOnMock -> relationOp);

        RelationOpTranslationStrategy relationOpTranslationStrategy = new RelationOpTranslationStrategy();
        GraphTraversal traversal = relationOpTranslationStrategy.apply(context, new DefaultGraphTraversal());
        Assert.assertEquals(traversal.asAdmin().getSteps().get(0).getClass().getSimpleName(),"VertexStep");
        Assert.assertEquals(((VertexStep)traversal.asAdmin().getSteps().get(0)).getDirection(), Direction.OUT);
    }

}