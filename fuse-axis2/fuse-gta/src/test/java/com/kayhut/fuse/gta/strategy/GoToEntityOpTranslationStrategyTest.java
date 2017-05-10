package com.kayhut.fuse.gta.strategy;

import com.kayhut.fuse.gta.translation.PlanUtil;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.GoToEntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.DefaultGraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by benishue on 12-Mar-17.
 */
public class GoToEntityOpTranslationStrategyTest {
    @Test
    public void entityOpAdjacentTranslationStrategyTest1() throws Exception {
        EConcrete concrete1 = new EConcrete();
        concrete1.seteNum(1);
        concrete1.seteTag("A");

        EConcrete concrete2 = new EConcrete();
        concrete2.seteNum(2);
        concrete2.seteTag("B");

        GoToEntityOp entity1 = new GoToEntityOp(AsgEBase.Builder.<EEntityBase>get().withEBase(concrete1).build());

        Plan plan = Mockito.mock(Plan.class);
        when(plan.getOps()).thenAnswer(invocationOnMock -> Arrays.asList(entity1));

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
        when(context.getPlanOp()).thenAnswer(invocationOnMock -> plan.getOps().get(0));

        GoToEntityOpTranslationStrategy goToEntityOpTranslationStrategy = new GoToEntityOpTranslationStrategy();
        GraphTraversal traversal = goToEntityOpTranslationStrategy.apply(context, new DefaultGraphTraversal());

        Assert.assertEquals(traversal.asAdmin().getSteps().get(0).getClass().getSimpleName(), "SelectOneStep");
    }

}